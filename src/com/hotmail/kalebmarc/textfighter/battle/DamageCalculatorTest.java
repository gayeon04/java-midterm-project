package com.hotmail.kalebmarc.textfighter.battle;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD — Red → Green → Refactor
 *
 * 1단계(Red):   이 파일만 먼저 작성 → DamageCalculator 없으므로 컴파일 에러 (정상)
 * 2단계(Green): DamageCalculator.java 구현 → 테스트 통과
 * 3단계(Refactor): 중복 제거, 가독성 개선
 */
@DisplayName("DamageCalculator - TDD로 만드는 데미지 계산기")
class DamageCalculatorTest {

    @Test
    @DisplayName("기본 공격력에 레벨 보너스가 더해진다")
    void base_damage_plus_level_bonus() {
        // Arrange
        int baseDamage = 10;
        int level = 5;

        // Act
        int result = DamageCalculator.calculate(baseDamage, level);

        // Assert — 레벨당 2 보너스: 10 + (5 * 2) = 20
        assertThat(result).isEqualTo(20);
    }

    @Test
    @DisplayName("레벨 1에서 calculate는 baseDamage + 2를 반환한다")
    void level_one_adds_two() {
        // Arrange
        int baseDamage = 10;

        // Act
        int result = DamageCalculator.calculate(baseDamage, 1);

        // Assert
        assertThat(result).isEqualTo(12);
    }

    @ParameterizedTest(name = "base={0}, level={1} → {2}")
    @CsvSource({
        "10, 1, 12",
        "10, 5, 20",
        "20, 3, 26",
        "0,  1,  2"
    })
    @DisplayName("다양한 입력에서 calculate 결과가 정확하다")
    void calculate_various_inputs(int base, int level, int expected) {
        // Act
        int result = DamageCalculator.calculate(base, level);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("크리티컬 히트 시 데미지가 1.5배가 된다")
    void critical_hit_multiplies_damage() {
        // Act
        int result = DamageCalculator.withCritical(20);

        // Assert
        assertThat(result).isEqualTo(30); // 20 * 1.5
    }

    @Test
    @DisplayName("레벨 0 이하면 IllegalArgumentException이 발생한다")
    void level_zero_throws_exception() {
        // Act & Assert
        assertThatThrownBy(() -> DamageCalculator.calculate(10, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("레벨");
    }

    @Test
    @DisplayName("음수 레벨도 IllegalArgumentException이 발생한다")
    void negative_level_throws_exception() {
        // Act & Assert
        assertThatThrownBy(() -> DamageCalculator.calculate(10, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
