package com.hotmail.kalebmarc.textfighter.multiplayer;

import java.util.Scanner;

/**
 * 콘솔 텍스트 멀티 전투 테스트.
 *
 * 터미널 1:  java MultiplayerTest host
 * 터미널 2:  java MultiplayerTest guest [IP 주소]  (기본값: localhost)
 *
 * 1=공격, 2=방어  입력 → HP 0 이하면 패배
 */
public class MultiplayerTest {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("사용법: MultiplayerTest host  OR  MultiplayerTest guest [IP]");
            return;
        }

        Scanner sc = new Scanner(System.in);

        if ("host".equals(args[0])) {
            MultiplayerBattle battle = new MultiplayerBattle(true);
            battle.startAsHost();
            runBattleLoop(battle, sc);
        } else {
            String host = args.length > 1 ? args[1] : "localhost";
            MultiplayerBattle battle = new MultiplayerBattle(false);
            battle.startAsClient(host);
            runBattleLoop(battle, sc);
        }
    }

    private static void runBattleLoop(MultiplayerBattle battle, Scanner sc) throws Exception {
        while (true) {
            System.out.println("\n[내 HP: " + battle.getMyHp()
                             + " | 상대 HP: " + battle.getEnemyHp() + "]");

            if (battle.isMyTurn()) {
                // ── 내 행동 ──────────────────────────────────────────
                System.out.print("행동 선택 (1=공격, 2=방어): ");
                String input = sc.nextLine().trim();
                if ("1".equals(input)) battle.attack();
                else                   battle.defend();

                // 상대 응답 수신 (공격 시: HP ack / 방어 시: 상대 행동)
                String r1 = battle.processTurn();
                if (r1.startsWith("RESULT")) { printResult(r1); break; }

                // 공격 후 ack(HP:n)를 받았으면 아직 상대 차례 → 상대 행동 추가 수신
                if (!battle.isMyTurn()) {
                    System.out.println("[대기] 상대 행동 기다리는 중...");
                    String r2 = battle.processTurn();
                    if (r2.startsWith("RESULT")) { printResult(r2); break; }
                }

            } else {
                // ── 상대 행동 대기 ────────────────────────────────────
                System.out.println("[대기] 상대 행동 기다리는 중...");
                String r = battle.processTurn();
                if (r.startsWith("RESULT")) { printResult(r); break; }
            }
        }

        battle.close();
        System.out.println("전투 종료.");
    }

    private static void printResult(String response) {
        if (response.contains("WIN")) System.out.println("\n★  승리! ★");
        else                          System.out.println("\n✗  패배...");
    }
}
