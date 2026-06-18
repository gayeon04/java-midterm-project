package com.hotmail.kalebmarc.textfighter.multiplayer;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 멀티플레이어 소켓 전투 시나리오 테스트 (JUnit 5)
 *
 * 각 시나리오는 실제 TCP 소켓(localhost:9999)을 사용한다.
 * 서버 스레드와 클라이언트 스레드를 동시에 실행하고
 * CompletableFuture 로 결과를 수집해 검증한다.
 */
@DisplayName("멀티플레이어 전투 시나리오 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiplayerBattleTest {

    private static final int TIMEOUT = 15; // seconds per test

    // ════════════════════════════════════════════════════════════════
    // 시나리오 1 — BattleMessage 인코딩 검증 (소켓 불필요)
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(1)
    @DisplayName("시나리오 1: BattleMessage 인코딩 검증")
    void scenario1_MessageEncoding() {
        assertEquals("ATTACK:15",    BattleMessage.attack(15));
        assertEquals("DEFEND:8",     BattleMessage.defend(8));
        assertEquals("HP:75",        BattleMessage.hp(75));
        assertEquals("RESULT:WIN",   BattleMessage.result(true));
        assertEquals("RESULT:LOSE",  BattleMessage.result(false));
        assertEquals("READY",        BattleMessage.ready());
        assertEquals("CHAT:안녕",    BattleMessage.chat("안녕"));
        System.out.println("[시나리오 1] 모든 인코딩 검증 통과");
    }

    // ════════════════════════════════════════════════════════════════
    // 시나리오 2 — BattleMessage 파싱 검증 (소켓 불필요)
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(2)
    @DisplayName("시나리오 2: BattleMessage 파싱 검증")
    void scenario2_MessageParsing() {
        // 콜론 구분 일반 메시지
        String[] a = BattleMessage.parse("ATTACK:15");
        assertEquals(2,        a.length, "ATTACK:15 → 2토큰");
        assertEquals("ATTACK", a[0]);
        assertEquals("15",     a[1]);

        // RESULT 파싱
        String[] b = BattleMessage.parse("RESULT:LOSE");
        assertEquals("RESULT", b[0]);
        assertEquals("LOSE",   b[1]);

        // 콜론 없는 메시지(READY)
        String[] c = BattleMessage.parse("READY");
        assertEquals(1,       c.length, "READY → 1토큰");
        assertEquals("READY", c[0]);

        // null 처리 → {"",""}
        String[] d = BattleMessage.parse(null);
        assertEquals(2, d.length, "null → 2토큰");
        assertEquals("", d[0]);

        // 두 번째 토큰 안에 콜론 포함 (CHAT)
        String[] e = BattleMessage.parse("CHAT:안녕:반가워");
        assertEquals(2,           e.length);
        assertEquals("CHAT",      e[0]);
        assertEquals("안녕:반가워", e[1], "split(:,2) 이므로 두 번째 콜론은 분리하지 않음");

        System.out.println("[시나리오 2] 모든 파싱 검증 통과");
    }

    // ════════════════════════════════════════════════════════════════
    // 시나리오 3 — 소켓 연결 및 READY 핸드셰이크
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(3)
    @DisplayName("시나리오 3: 소켓 연결 및 READY 핸드셰이크")
    void scenario3_ConnectionHandshake() throws Exception {
        CompletableFuture<String> serverGot  = new CompletableFuture<>();
        CompletableFuture<String> clientGot  = new CompletableFuture<>();

        Thread serverThread = new Thread(() -> {
            try {
                BattleServer server = new BattleServer();
                server.start();
                String msg = server.receiveMessage();
                server.sendMessage(BattleMessage.ready());
                server.close();
                serverGot.complete(msg);
            } catch (Exception e) { serverGot.completeExceptionally(e); }
        });
        serverThread.setDaemon(true);

        Thread clientThread = new Thread(() -> {
            try {
                BattleClient client = new BattleClient();
                client.connect("localhost");
                client.sendMessage(BattleMessage.ready());
                String response = client.receiveMessage();
                client.close();
                clientGot.complete(response);
            } catch (Exception e) { clientGot.completeExceptionally(e); }
        });
        clientThread.setDaemon(true);

        serverThread.start();
        Thread.sleep(150);
        clientThread.start();

        assertEquals("READY", serverGot .get(TIMEOUT, TimeUnit.SECONDS), "서버 수신 메시지");
        assertEquals("READY", clientGot .get(TIMEOUT, TimeUnit.SECONDS), "클라이언트 수신 메시지");
        System.out.println("[시나리오 3] READY 핸드셰이크 성공");
    }

    // ════════════════════════════════════════════════════════════════
    // 시나리오 4 — 공격 1회: 게스트 HP 감소 + 호스트 동기화 확인
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(4)
    @DisplayName("시나리오 4: 공격 1회 — HP 감소 및 호스트-게스트 동기화")
    void scenario4_SingleAttack_HpSync() throws Exception {
        // [hpBefore, hpAfter]
        CompletableFuture<int[]>  guestFuture  = new CompletableFuture<>();
        CompletableFuture<Integer> hostSeenHp  = new CompletableFuture<>();

        Thread hostThread = new Thread(() -> {
            try {
                MultiplayerBattle host = new MultiplayerBattle(true);
                host.startAsHost();
                host.attack();
                host.processTurn();            // HP:n ack 수신
                hostSeenHp.complete(host.getEnemyHp());
                host.close();
            } catch (Exception e) { hostSeenHp.completeExceptionally(e); }
        });
        hostThread.setDaemon(true);

        Thread guestThread = new Thread(() -> {
            try {
                MultiplayerBattle guest = new MultiplayerBattle(false);
                guest.startAsClient("localhost");
                int before = guest.getMyHp();
                guest.processTurn();           // ATTACK:n 수신 → 데미지 적용 → HP ack 전송
                int after = guest.getMyHp();
                guest.close();
                guestFuture.complete(new int[]{before, after});
            } catch (Exception e) { guestFuture.completeExceptionally(e); }
        });
        guestThread.setDaemon(true);

        hostThread.start();
        Thread.sleep(150);
        guestThread.start();

        int[] hp     = guestFuture.get(TIMEOUT, TimeUnit.SECONDS);
        int   seen   = hostSeenHp .get(TIMEOUT, TimeUnit.SECONDS);
        int   damage = hp[0] - hp[1];

        System.out.printf("[시나리오 4] 게스트 HP: %d → %d (데미지: %d), 호스트 확인: %d%n",
                          hp[0], hp[1], damage, seen);

        assertEquals(100, hp[0], "전투 시작 HP는 100");
        assertTrue(damage >= 5 && damage <= 20, "데미지는 5~20 범위여야 함, 실제: " + damage);
        assertEquals(hp[1], seen, "호스트가 동기화한 HP와 게스트 실제 HP 일치");
    }

    // ════════════════════════════════════════════════════════════════
    // 시나리오 5 — 방어 후 HP 회복 + 100 상한 유지 + 호스트 동기화
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(5)
    @DisplayName("시나리오 5: 방어 후 HP 회복 및 상한 100 유지")
    void scenario5_Defend_HpRecovers() throws Exception {
        // [hpAfterAttack, hpAfterDefend]
        CompletableFuture<int[]>  guestFuture   = new CompletableFuture<>();
        CompletableFuture<Integer> hostAfterDef  = new CompletableFuture<>();

        Thread hostThread = new Thread(() -> {
            try {
                MultiplayerBattle host = new MultiplayerBattle(true);
                host.startAsHost();
                host.attack();
                host.processTurn();            // HP:n ack
                host.processTurn();            // DEFEND:k 수신
                hostAfterDef.complete(host.getEnemyHp());
                host.close();
            } catch (Exception e) { hostAfterDef.completeExceptionally(e); }
        });
        hostThread.setDaemon(true);

        Thread guestThread = new Thread(() -> {
            try {
                MultiplayerBattle guest = new MultiplayerBattle(false);
                guest.startAsClient("localhost");
                guest.processTurn();           // ATTACK:n → 데미지 → HP ack 전송
                int hpAfterAttack = guest.getMyHp();
                guest.defend();                // HP 회복 → DEFEND:k 전송
                int hpAfterDefend = guest.getMyHp();
                guest.close();
                guestFuture.complete(new int[]{hpAfterAttack, hpAfterDefend});
            } catch (Exception e) { guestFuture.completeExceptionally(e); }
        });
        guestThread.setDaemon(true);

        hostThread.start();
        Thread.sleep(150);
        guestThread.start();

        int[] hp   = guestFuture .get(TIMEOUT, TimeUnit.SECONDS);
        int   seen = hostAfterDef.get(TIMEOUT, TimeUnit.SECONDS);

        System.out.printf("[시나리오 5] 공격 후 HP: %d, 방어 후 HP: %d (회복: %d), 호스트 확인: %d%n",
                          hp[0], hp[1], hp[1] - hp[0], seen);

        assertTrue(hp[1] > hp[0],  "방어 후 HP가 공격 후 HP보다 높아야 함");
        assertTrue(hp[1] <= 100,   "방어 후 HP는 100 초과 불가");
        assertEquals(hp[1], seen,  "호스트 동기화 HP와 게스트 실제 HP 일치");
    }

    // ════════════════════════════════════════════════════════════════
    // 시나리오 6 — 전투 완주: WIN / LOSE 대칭성
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(6)
    @DisplayName("시나리오 6: 전투 완주 — WIN / LOSE 결과 대칭성")
    void scenario6_FullBattle_ResultSymmetry() throws Exception {
        CompletableFuture<String> hostFuture  = new CompletableFuture<>();
        CompletableFuture<String> guestFuture = new CompletableFuture<>();

        Thread hostThread = new Thread(() -> {
            try {
                MultiplayerBattle host = new MultiplayerBattle(true);
                host.startAsHost();
                String result = runAttackOnlyLoop(host);
                host.close();
                hostFuture.complete(result);
            } catch (Exception e) { hostFuture.completeExceptionally(e); }
        });
        hostThread.setDaemon(true);

        Thread guestThread = new Thread(() -> {
            try {
                MultiplayerBattle guest = new MultiplayerBattle(false);
                guest.startAsClient("localhost");
                String result = runAttackOnlyLoop(guest);
                guest.close();
                guestFuture.complete(result);
            } catch (Exception e) { guestFuture.completeExceptionally(e); }
        });
        guestThread.setDaemon(true);

        hostThread.start();
        Thread.sleep(150);
        guestThread.start();

        String hostResult  = hostFuture .get(TIMEOUT, TimeUnit.SECONDS);
        String guestResult = guestFuture.get(TIMEOUT, TimeUnit.SECONDS);

        System.out.println("[시나리오 6] 호스트: " + hostResult + " | 게스트: " + guestResult);

        assertTrue(hostResult .startsWith("RESULT"), "호스트 결과는 RESULT 포함");
        assertTrue(guestResult.startsWith("RESULT"), "게스트 결과는 RESULT 포함");

        boolean hostWins  = hostResult .contains("WIN");
        boolean guestWins = guestResult.contains("WIN");
        assertTrue(hostWins ^ guestWins, "정확히 한 쪽만 승리 (XOR)");
    }

    // ════════════════════════════════════════════════════════════════
    // 시나리오 7 — 3라운드 HP 연속 변화 추적 (공격 → 방어 → 공격)
    // ════════════════════════════════════════════════════════════════
    @Test
    @Order(7)
    @DisplayName("시나리오 7: 공격→방어→공격 3라운드 HP 변화 및 동기화")
    void scenario7_ThreeRound_HpChanges() throws Exception {
        // guest: [hpR1, hpR2, hpR3]
        CompletableFuture<int[]> guestFuture = new CompletableFuture<>();
        // host enemy view: [enemyR1, enemyR2, enemyR3]
        CompletableFuture<int[]> hostFuture  = new CompletableFuture<>();

        Thread hostThread = new Thread(() -> {
            try {
                MultiplayerBattle host = new MultiplayerBattle(true);
                host.startAsHost();

                host.attack();
                host.processTurn();                    // HP:n ack
                int e1 = host.getEnemyHp();

                host.processTurn();                    // DEFEND:k 수신
                int e2 = host.getEnemyHp();

                host.attack();
                host.processTurn();                    // HP:q ack
                int e3 = host.getEnemyHp();

                host.close();
                hostFuture.complete(new int[]{e1, e2, e3});
            } catch (Exception e) { hostFuture.completeExceptionally(e); }
        });
        hostThread.setDaemon(true);

        Thread guestThread = new Thread(() -> {
            try {
                MultiplayerBattle guest = new MultiplayerBattle(false);
                guest.startAsClient("localhost");

                guest.processTurn();                   // 라운드 1: ATTACK 수신
                int r1 = guest.getMyHp();

                guest.defend();                        // 라운드 2: 방어
                int r2 = guest.getMyHp();

                guest.processTurn();                   // 라운드 3: ATTACK 수신
                int r3 = guest.getMyHp();

                guest.close();
                guestFuture.complete(new int[]{r1, r2, r3});
            } catch (Exception e) { guestFuture.completeExceptionally(e); }
        });
        guestThread.setDaemon(true);

        hostThread.start();
        Thread.sleep(150);
        guestThread.start();

        int[] g = guestFuture.get(TIMEOUT, TimeUnit.SECONDS);
        int[] h = hostFuture .get(TIMEOUT, TimeUnit.SECONDS);

        System.out.printf("[시나리오 7] 게스트 HP: 100 →[공격]→ %d →[방어]→ %d →[공격]→ %d%n",
                          g[0], g[1], g[2]);
        System.out.printf("[시나리오 7] 호스트 확인:              %d              %d              %d%n",
                          h[0], h[1], h[2]);

        assertTrue(g[0] < 100, "라운드1 후 HP 감소");
        assertTrue(g[1] > g[0], "라운드2 후 방어로 HP 회복");
        assertTrue(g[2] < g[1], "라운드3 후 HP 재감소");
        assertTrue(g[1] <= 100, "HP는 100 초과 불가");

        assertEquals(g[0], h[0], "라운드1 호스트-게스트 HP 동기화");
        assertEquals(g[1], h[1], "라운드2 방어 후 동기화");
        assertEquals(g[2], h[2], "라운드3 호스트-게스트 HP 동기화");
    }

    // ════════════════════════════════════════════════════════════════
    // 헬퍼: 항상 공격만 하는 자동 전투 루프
    // ════════════════════════════════════════════════════════════════
    private String runAttackOnlyLoop(MultiplayerBattle battle) throws Exception {
        while (true) {
            if (battle.isMyTurn()) {
                battle.attack();
                String r1 = battle.processTurn();      // HP ack (공격 후) 또는 직접 RESULT
                if (r1.startsWith("RESULT")) return r1;
                if (!battle.isMyTurn()) {
                    // 공격 ack를 받은 뒤 아직 상대 턴 → 상대 행동 수신
                    String r2 = battle.processTurn();
                    if (r2.startsWith("RESULT")) return r2;
                }
            } else {
                String r = battle.processTurn();       // 상대 행동 수신
                if (r.startsWith("RESULT")) return r;
            }
        }
    }
}
