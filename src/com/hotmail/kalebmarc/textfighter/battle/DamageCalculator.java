package com.hotmail.kalebmarc.textfighter.battle;

/**
 * TDD — Green 단계: DamageCalculatorTest를 통과시키는 최소 구현.
 */
public class DamageCalculator {

    private static final int LEVEL_BONUS_PER_LEVEL = 2;
    private static final double CRITICAL_MULTIPLIER = 1.5;

    private DamageCalculator() {}

    /**
     * @param baseDamage 기본 공격력
     * @param level      플레이어 레벨 (1 이상)
     * @return baseDamage + level * 2
     * @throws IllegalArgumentException 레벨이 0 이하일 때
     */
    public static int calculate(int baseDamage, int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("레벨은 1 이상이어야 합니다. 입력값: " + level);
        }
        return baseDamage + (level * LEVEL_BONUS_PER_LEVEL);
    }

    /**
     * @param damage 원본 데미지
     * @return (int)(damage * 1.5)
     */
    public static int withCritical(int damage) {
        return (int) (damage * CRITICAL_MULTIPLIER);
    }
}
