package com.hotmail.kalebmarc.textfighter.battle;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BattleAnalyzer - 전투 통계 분석 테스트")
class BattleAnalyzerTest {

    private BattleAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        // Arrange — 공통 분석기
        analyzer = new BattleAnalyzer();
    }

    // ── 칭호 판정 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("도망을 2번 이상 치면 COWARD 칭호를 받는다")
    void ran_away_twice_gets_coward_title() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.ATTACK,   8, "주먹");
        record.record(BattleRecord.EventType.RAN_AWAY, 0, "도망");
        record.record(BattleRecord.EventType.RAN_AWAY, 0, "도망");

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getTitle()).isEqualTo(PlayerTitle.COWARD);
    }

    @Test
    @DisplayName("전투 효율 85% 이상이면 PERFECTIONIST 칭호를 받는다")
    void high_efficiency_gets_perfectionist_title() {
        // Arrange — 큰 데미지, 작은 피해 → 효율 94%
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.ATTACK, 90, "저격총");
        record.record(BattleRecord.EventType.HIT,     5, "좀비");

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getTitle()).isEqualTo(PlayerTitle.PERFECTIONIST);
    }

    @Test
    @DisplayName("크리티컬 3회 이상이면 BERSERKER 칭호를 받는다")
    void three_criticals_gets_berserker_title() {
        // Arrange — 효율 < 85%, 도망 없음, 크리 3회
        // MISS 1개 추가 → missCount != 0이므로 SNIPER_GOD 규칙(missCount==0 && dealt>40)을 우회
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.CRITICAL, 20, "크리!");
        record.record(BattleRecord.EventType.CRITICAL, 15, "크리!");
        record.record(BattleRecord.EventType.CRITICAL, 10, "크리!");
        record.record(BattleRecord.EventType.MISS,      0, "빗나감");
        record.record(BattleRecord.EventType.HIT,      60, "좀비");  // 받은 피해 > 가한 피해

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getTitle()).isEqualTo(PlayerTitle.BERSERKER);
    }

    @Test
    @DisplayName("포션 3회 이상 사용 시 POTION_ADDICT 칭호를 받는다")
    void three_potions_gets_potion_addict_title() {
        // Arrange — 효율 < 85%, 도망 없음, 크리 없음, 포션 3회
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.ATTACK,      10, "주먹");
        record.record(BattleRecord.EventType.HIT,         20, "좀비");
        record.record(BattleRecord.EventType.POTION_USED,  0, "포션");
        record.record(BattleRecord.EventType.POTION_USED,  0, "포션");
        record.record(BattleRecord.EventType.POTION_USED,  0, "포션");

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getTitle()).isEqualTo(PlayerTitle.POTION_ADDICT);
    }

    @Test
    @DisplayName("이벤트 없는 레코드는 기본값 LUCKY 칭호를 받는다")
    void empty_record_gets_lucky_title() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getTitle()).isEqualTo(PlayerTitle.LUCKY);
    }

    // ── 효율 계산 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("분석 결과의 효율은 0~100 사이여야 한다")
    void efficiency_is_between_0_and_100() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.ATTACK, 20, "주먹");
        record.record(BattleRecord.EventType.HIT,    10, "좀비");

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getEfficiency()).isBetween(0.0, 100.0);
    }

    @Test
    @DisplayName("이벤트 없으면 효율은 0이다")
    void empty_record_efficiency_is_zero() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");

        // Act
        BattleAnalyzer.AnalysisResult result = analyzer.analyze(record);

        // Assert
        assertThat(result.getEfficiency()).isZero();
    }

    // ── BattleRecord Stream 분석 ──────────────────────────────────────────

    @Test
    @DisplayName("ATTACK/CRITICAL 이벤트 값의 합이 getTotalDamageDealt와 일치한다")
    void total_damage_dealt_matches_event_sum() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.ATTACK,   15, "주먹");
        record.record(BattleRecord.EventType.ATTACK,   20, "주먹");
        record.record(BattleRecord.EventType.HIT,      10, "좀비"); // 포함 안 됨

        // Act & Assert
        assertThat(record.getTotalDamageDealt()).isEqualTo(35);
    }

    @Test
    @DisplayName("countEvents는 특정 타입의 이벤트 수만 정확히 센다")
    void count_events_counts_correct_type() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.MISS, 0, "빗나감");
        record.record(BattleRecord.EventType.MISS, 0, "빗나감");
        record.record(BattleRecord.EventType.ATTACK, 10, "주먹");

        // Act & Assert
        assertThat(record.countEvents(BattleRecord.EventType.MISS)).isEqualTo(2);
        assertThat(record.countEvents(BattleRecord.EventType.ATTACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("getMaxSingleDamage는 ATTACK/CRITICAL 중 최댓값을 반환한다")
    void max_single_damage_returns_max_attack() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");
        record.record(BattleRecord.EventType.ATTACK, 10, "주먹");
        record.record(BattleRecord.EventType.ATTACK, 50, "저격총");
        record.record(BattleRecord.EventType.HIT,   999, "좀비"); // 제외됨

        // Act & Assert
        assertThat(record.getMaxSingleDamage()).isEqualTo(50);
    }

    @Test
    @DisplayName("이벤트 없을 때 getMaxSingleDamage는 0을 반환한다")
    void max_single_damage_is_zero_when_empty() {
        // Arrange
        BattleRecord record = new BattleRecord("hero");

        // Act & Assert
        assertThat(record.getMaxSingleDamage()).isZero();
    }
}
