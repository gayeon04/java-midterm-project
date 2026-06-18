package com.hotmail.kalebmarc.textfighter.multiplayer;

/**
 * 콜론(:) 구분 단순 프로토콜
 *
 * ATTACK:15     → 15 데미지 공격
 * DEFEND:8      → 8 HP 방어 (회복)
 * HP:75         → 현재 HP 동기화 (ATTACK 수신 후 ack)
 * CHAT:안녕     → 채팅
 * READY         → 전투 준비 완료
 * RESULT:WIN    → 승패 결과
 * RESULT:LOSE
 */
public class BattleMessage {

    public static String attack(int damage)  { return "ATTACK:" + damage; }
    public static String defend(int shield)  { return "DEFEND:" + shield; }
    public static String hp(int currentHp)   { return "HP:" + currentHp; }
    public static String chat(String text)   { return "CHAT:" + text; }
    public static String ready()             { return "READY"; }
    public static String result(boolean win) { return "RESULT:" + (win ? "WIN" : "LOSE"); }

    /** raw 메시지를 { 타입, 값 } 배열로 파싱. 값이 없으면 배열 길이 1. */
    public static String[] parse(String raw) {
        if (raw == null) return new String[]{"", ""};
        return raw.split(":", 2);
    }
}
