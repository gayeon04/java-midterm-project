package com.hotmail.kalebmarc.textfighter.battle;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BattleManager - 전투 전략 테스트")
class BattleManagerTest {

    private BattleManager manager;

    @BeforeEach
    void setUp() {
        manager = new BattleManager(AttackStrategies.MELEE);
    }

    // ── MELEE ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("MELEE 전략")
    class MeleeStrategy {

        @Test
        @DisplayName("근접 공격은 설정한 범위 안의 데미지를 반환한다")
        void melee_damage_is_within_range() {
            // Arrange
            int min = 5, max = 15;

            // Act
            int damage = manager.attack(min, max, "주먹");

            // Assert
            assertThat(damage).isBetween(min, max);
        }

        @ParameterizedTest(name = "min={0}, max={1} 범위")
        @CsvSource({"1,5", "5,15", "10,20", "15,30"})
        @DisplayName("다양한 범위에서 데미지가 항상 범위 내에 있다")
        void melee_damage_various_ranges(int min, int max) {
            // Arrange
            BattleManager m = new BattleManager(AttackStrategies.MELEE);

            // Act & Assert — 100회 반복해도 범위 이탈 없음
            for (int i = 0; i < 100; i++) {
                int damage = m.attack(min, max, "주먹");
                assertThat(damage).isBetween(min, max);
            }
        }

        @Test
        @DisplayName("공격 후 turnsPlayed가 1 증가한다")
        void attack_increments_turns_played() {
            // Arrange
            int before = manager.getTurnsPlayed();

            // Act
            manager.attack(5, 10, "주먹");

            // Assert
            assertThat(manager.getTurnsPlayed()).isEqualTo(before + 1);
        }

        @Test
        @DisplayName("reset 후 모든 통계가 0이 된다")
        void reset_clears_all_stats() {
            // Arrange — 통계 누적
            manager.attack(5, 10, "주먹");
            manager.takeDamage(8, "좀비");

            // Act
            manager.reset();

            // Assert
            assertThat(manager.getTotalDamageDealt()).isZero();
            assertThat(manager.getTotalDamageTaken()).isZero();
            assertThat(manager.getTurnsPlayed()).isZero();
            assertThat(manager.getCriticalHits()).isZero();
            assertThat(manager.getMissCount()).isZero();
        }
    }

    // ── SNIPER ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SNIPER 전략")
    class SniperStrategy {

        @BeforeEach
        void useSniper() {
            manager.setStrategy(AttackStrategies.SNIPER);
        }

        @Test
        @DisplayName("저격 전략 전환 시 getBattleLog에 전략 변경 로그가 남는다")
        void sniper_strategy_log_recorded() {
            // Assert
            assertThat(manager.getBattleLog())
                .anyMatch(log -> log.contains("전략 변경"));
        }

        @Test
        @DisplayName("저격 전략의 데미지는 0 이상이다 (빗나감 포함)")
        void sniper_damage_is_non_negative() {
            // Act & Assert — 30회 반복
            for (int i = 0; i < 30; i++) {
                int damage = manager.attack(10, 20, "저격총");
                assertThat(damage).isGreaterThanOrEqualTo(0);
            }
        }
    }

    // ── 피해 수신 ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("피해 수신")
    class TakeDamage {

        @Test
        @DisplayName("takeDamage 호출 시 totalDamageTaken이 증가한다")
        void take_damage_increments_total() {
            // Arrange
            int dmg = 20;

            // Act
            manager.takeDamage(dmg, "좀비");

            // Assert
            assertThat(manager.getTotalDamageTaken()).isEqualTo(dmg);
        }

        @Test
        @DisplayName("takeDamage를 여러 번 호출하면 누적 합산된다")
        void take_damage_accumulates() {
            // Act
            manager.takeDamage(10, "좀비");
            manager.takeDamage(15, "고블린");

            // Assert
            assertThat(manager.getTotalDamageTaken()).isEqualTo(25);
        }
    }

    // ── 전략 교체 ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("런타임 전략 교체")
    class StrategySwitch {

        @Test
        @DisplayName("withBonus 전략은 기본 데미지에 보너스가 더해진다")
        void bonus_strategy_adds_bonus_damage() {
            // Arrange — 보너스 100이면 최소 데미지도 105 이상
            AttackStrategy bonus = AttackStrategies.withBonus(AttackStrategies.MELEE, 100);

            // Act
            manager.setStrategy(bonus);
            int damage = manager.attack(5, 10, "보너스 무기");

            // Assert
            assertThat(damage).isGreaterThanOrEqualTo(105);
        }

        @Test
        @DisplayName("전략 교체 후 getBattleLog 크기가 1 이상이다")
        void log_grows_after_strategy_change() {
            // Act
            manager.setStrategy(AttackStrategies.SHOTGUN);

            // Assert
            assertThat(manager.getBattleLog()).isNotEmpty();
        }
    }
}
