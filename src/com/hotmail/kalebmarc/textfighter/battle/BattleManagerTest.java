package com.hotmail.kalebmarc.textfighter.battle;

/**
 * [Step 3] BattleManager + Strategy Pattern 테스트
 */
public class BattleManagerTest {

    public static void main(String[] args) {
        System.out.println("=== Step 3: BattleManager (Strategy Pattern) 테스트 ===\n");

        // ① 근접 전략으로 시작
        System.out.println("① 근접 무기 (주먹) 전략:");
        BattleManager manager = new BattleManager(AttackStrategies.MELEE);

        for (int i = 0; i < 3; i++) {
            int dmg = manager.attack(5, 10, "주먹");
            manager.takeDamage(8, "좀비");
        }
        manager.printLog();

        // ② 런타임에 전략 교체 - 저격 전략으로
        System.out.println("\n② 무기 변경 → 저격총 전략으로 교체:");
        manager.reset();
        manager.setStrategy(AttackStrategies.SNIPER);

        for (int i = 0; i < 4; i++) {
            int dmg = manager.attack(20, 35, "저격총");
            if (dmg > 0) manager.takeDamage(5, "좀비");
        }
        manager.printLog();

        // ③ 고차 함수 - 성향 보너스 전략
        System.out.println("\n③ 공격적 성향 보너스 (+5) 적용:");
        manager.reset();
        AttackStrategy aggressiveStrategy = AttackStrategies.withBonus(AttackStrategies.MELEE, 5);
        manager.setStrategy(aggressiveStrategy);

        for (int i = 0; i < 3; i++) {
            manager.attack(5, 10, "주먹 (공격적)");
        }
        manager.printLog();

        // ④ 전투 통계 출력
        System.out.println("\n④ 전투 통계:");
        System.out.println("   총 가한 데미지 : " + manager.getTotalDamageDealt());
        System.out.println("   총 받은 데미지 : " + manager.getTotalDamageTaken());
        System.out.println("   총 턴 수       : " + manager.getTurnsPlayed());
        System.out.println("   크리티컬 횟수  : " + manager.getCriticalHits());
        System.out.println("   빗나간 횟수    : " + manager.getMissCount());

        // ⑤ 람다로 커스텀 전략 즉석 정의
        System.out.println("\n⑤ 람다로 즉석 커스텀 전략 정의:");
        AttackStrategy berserker = (min, max) -> {
            // 체력이 낮을수록 강해지는 버서커 전략
            int baseDmg = min + (int)(Math.random() * (max - min + 1));
            return (int)(baseDmg * 1.3); // 항상 130% 데미지
        };
        manager.reset();
        manager.setStrategy(berserker);
        manager.attack(10, 20, "버서커 주먹");
        manager.printLog();

        System.out.println("\n=== 테스트 완료 ===");
    }
}