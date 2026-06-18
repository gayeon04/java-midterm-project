package com.hotmail.kalebmarc.textfighter.db;

import com.hotmail.kalebmarc.textfighter.db.entity.RankingEntry;
import com.hotmail.kalebmarc.textfighter.db.entity.SaveSlot;
import com.hotmail.kalebmarc.textfighter.main.User;
import com.hotmail.kalebmarc.textfighter.player.Coins;
import com.hotmail.kalebmarc.textfighter.player.Health;
import com.hotmail.kalebmarc.textfighter.player.Stats;
import com.hotmail.kalebmarc.textfighter.player.Xp;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SaveService - JPA 저장/조회 테스트")
class SaveServiceTest {

    private SaveService saveService;

    @BeforeAll
    static void initTestDb() {
        // 인메모리 H2 사용 — 실제 파일 DB를 건드리지 않음
        System.setProperty("textfighter.persistenceUnit", "textfighter-test-pu");
    }

    @AfterAll
    static void closeDb() {
        JpaManager.getInstance().close();
        System.clearProperty("textfighter.persistenceUnit");
    }

    @BeforeEach
    void setUp() {
        // Arrange — 테스트용 플레이어 세팅
        saveService = new SaveService();
        User.setName("TestHero");
        Health.set(80, 100);
        Xp.setAll(1500, 2000, 3);
        Coins.set(200, false);
        Stats.totalKills = 0;
    }

    @AfterEach
    void tearDown() {
        // 정적 필드 초기화 — 테스트 간 독립성 보장
        User.setName("Player");
        Health.set(0, 100);
        Xp.setAll(0, 500, 1);
        Coins.set(0, false);
        Stats.totalKills = 0;
    }

    @Test
    @DisplayName("saveGame 호출 후 loadLatest로 동일한 데이터를 조회할 수 있다")
    void save_and_load_returns_same_data() {
        // Act
        saveService.saveGame("MANUAL");
        Optional<SaveSlot> result = saveService.loadLatest("TestHero");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPlayerName()).isEqualTo("TestHero");
        assertThat(result.get().getLevel()).isEqualTo(3);
        assertThat(result.get().getHp()).isEqualTo(80);
        assertThat(result.get().getSaveType()).isEqualTo("MANUAL");
    }

    @Test
    @DisplayName("loadLatest는 여러 번 저장했을 때 가장 최근 슬롯을 반환한다")
    void load_latest_returns_most_recent() {
        // Arrange — 두 번 저장. 두 번째 저장이 최신
        saveService.saveGame("AUTO");
        Health.set(50, 100);           // HP 변경 후 재저장
        saveService.saveGame("MANUAL");

        // Act
        Optional<SaveSlot> result = saveService.loadLatest("TestHero");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getHp()).isEqualTo(50);
        assertThat(result.get().getSaveType()).isEqualTo("MANUAL");
    }

    @Test
    @DisplayName("getTopRankings는 최대 10개를 반환한다")
    void top_rankings_max_10() {
        // Act
        saveService.saveRanking();
        List<RankingEntry> rankings = saveService.getTopRankings();

        // Assert
        assertThat(rankings).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 플레이어 조회 시 Optional.empty를 반환한다")
    void load_nonexistent_player_returns_empty() {
        // Act
        Optional<SaveSlot> result = saveService.loadLatest("NonExistent");

        // Assert
        assertThat(result).isEmpty();
    }
}
