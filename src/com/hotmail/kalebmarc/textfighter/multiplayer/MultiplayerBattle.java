package com.hotmail.kalebmarc.textfighter.multiplayer;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 턴 기반 1:1 전투 진행 관리자.
 *
 * 프로토콜 (턴당 메시지 흐름):
 *   공격 측:  ATTACK:n  →
 *             ← HP:m        (수비 측이 피해 적용 후 현재 HP 전송)
 *             ← ATTACK:k   OR  DEFEND:j   (수비 측 다음 행동)
 *             HP:p         →   (공격 측 피해/회복 ack)
 *
 *   방어 측:  DEFEND:n  →     (수비 측이 회복 후 전송, ack 없음)
 *             ← 다음 행동 수신
 */
public class MultiplayerBattle {

    private final boolean isHost;
    private final BattleServer server;
    private final BattleClient client;

    private int myHp    = 100;
    private int enemyHp = 100;
    private boolean myTurn;   // 호스트 선공

    public MultiplayerBattle(boolean isHost) {
        this.isHost = isHost;
        if (isHost) {
            server = new BattleServer();
            client = null;
        } else {
            server = null;
            client = new BattleClient();
        }
        myTurn = isHost;
    }

    // ── 연결 / 핸드셰이크 ─────────────────────────────────────────────

    public void startAsHost() throws IOException {
        System.out.println("[호스트] 포트 9999 대기 중...");
        server.start();
        System.out.println("[호스트] 게스트 연결됨");
        server.sendMessage(BattleMessage.ready());
        String r = server.receiveMessage();
        System.out.println("[호스트] 핸드셰이크 완료: " + r);
        System.out.println("[호스트] 전투 시작! 선공입니다.");
    }

    public void startAsClient(String host) throws IOException {
        System.out.println("[게스트] " + host + " 에 연결 중...");
        client.connect(host);
        System.out.println("[게스트] 연결 성공");
        String r = client.receiveMessage();
        System.out.println("[게스트] 핸드셰이크: " + r);
        client.sendMessage(BattleMessage.ready());
        System.out.println("[게스트] 전투 시작! 호스트가 선공입니다.");
    }

    // ── 행동 전송 ─────────────────────────────────────────────────────

    /** 랜덤 데미지(5~20) 공격 → ATTACK:n 전송 → 턴 넘기기 */
    public void attack() throws IOException {
        int damage = ThreadLocalRandom.current().nextInt(5, 21);
        sendMessage(BattleMessage.attack(damage));
        System.out.println("[공격] " + damage + " 데미지 전송");
        myTurn = false;
    }

    /** 랜덤 방어(3~10) 회복 → DEFEND:n 전송 → 턴 넘기기 */
    public void defend() throws IOException {
        int shield = ThreadLocalRandom.current().nextInt(3, 11);
        myHp = Math.min(100, myHp + shield);
        sendMessage(BattleMessage.defend(shield));
        System.out.println("[방어] +" + shield + " HP 회복, 현재 HP: " + myHp);
        myTurn = false;
    }

    // ── 수신 처리 ─────────────────────────────────────────────────────

    /**
     * 상대 메시지를 한 건 수신해 상태를 갱신한다. (블로킹)
     *
     * ATTACK:n  → myHp -= n → HP:myHp 전송 → myTurn=true
     *             → myHp <= 0 이면 RESULT:LOSE 전송 → "RESULT:LOSE" 반환
     * DEFEND:n  → enemyHp += n → myTurn=true
     * HP:n      → enemyHp = n  (공격 후 ack — myTurn 변경 없음)
     * RESULT:LOSE → 상대 패배 = 내 승리 → "RESULT:WIN" 반환
     *
     * @return 수신 원문 (또는 "RESULT:WIN" / "RESULT:LOSE" / "DISCONNECT")
     */
    public String processTurn() throws IOException {
        String raw = receiveMessage();
        if (raw == null) return "DISCONNECT";

        String[] parts = BattleMessage.parse(raw);
        String type  = parts[0];
        String value = parts.length > 1 ? parts[1] : "";

        switch (type) {
            case "ATTACK" -> {
                int dmg = Integer.parseInt(value);
                myHp -= dmg;
                System.out.println("[수신] 상대 공격 " + dmg + " 데미지 → 내 HP: " + myHp);
                sendMessage(BattleMessage.hp(myHp));
                myTurn = true;
                if (myHp <= 0) {
                    sendMessage(BattleMessage.result(false));
                    return "RESULT:LOSE";
                }
            }
            case "DEFEND" -> {
                int recovered = Integer.parseInt(value);
                enemyHp = Math.min(100, enemyHp + recovered);
                System.out.println("[수신] 상대 방어 (+" + recovered + " HP), 상대 HP: " + enemyHp);
                myTurn = true;
            }
            case "HP" -> {
                enemyHp = Integer.parseInt(value);
                System.out.println("[수신] 상대 HP 업데이트: " + enemyHp);
                // myTurn 변경 없음 (ATTACK 후 ack)
            }
            case "RESULT" -> {
                // 상대가 RESULT:LOSE 를 보냄 → 상대 패배 = 내 승리
                if ("LOSE".equals(value)) return "RESULT:WIN";
                return "RESULT:" + value;
            }
        }
        return raw;
    }

    // ── 공개 상태 조회 ────────────────────────────────────────────────

    public boolean isMyTurn()  { return myTurn;   }
    public int     getMyHp()   { return myHp;     }
    public int     getEnemyHp(){ return enemyHp;  }

    public void close() {
        try { if (server != null) server.close(); } catch (Exception ignored) {}
        try { if (client != null) client.close(); } catch (Exception ignored) {}
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────

    private void sendMessage(String msg) throws IOException {
        if (isHost) server.sendMessage(msg);
        else        client.sendMessage(msg);
    }

    private String receiveMessage() throws IOException {
        if (isHost) return server.receiveMessage();
        else        return client.receiveMessage();
    }
}
