package com.hotmail.kalebmarc.textfighter.db;

import com.hotmail.kalebmarc.textfighter.battle.BattleRecord;
import com.hotmail.kalebmarc.textfighter.db.entity.RankingEntry;
import com.hotmail.kalebmarc.textfighter.db.entity.SaveSlot;
import com.hotmail.kalebmarc.textfighter.main.User;
import com.hotmail.kalebmarc.textfighter.player.Coins;
import com.hotmail.kalebmarc.textfighter.player.Health;
import com.hotmail.kalebmarc.textfighter.player.Xp;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPA 통합 테스트 — H2 인메모리 DB에 직접 읽고 씀
 *
 * 검증 항목:
 *   1. JpaManager Singleton
 *   2. saveGame → DB에 1건 저장 확인
 *   3. loadLatest → Optional.isPresent() + 값 검증
 *   4. saveBattleLog (BattleRecord.countEvents 경유)
 *   5. saveRanking + getTopRankings (JPQL + Stream)
 *   6. saveQuestLog
 *   7. EMF 정상 종료
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaTest {

    private static SaveService service;

    @BeforeAll
    static void setUp() {
        // 인메모리 H2 사용 — 파일 오염 없이 테스트 격리
        System.setProperty("textfighter.persistenceUnit", "textfighter-test-pu");

        // 플레이어 정적 필드 초기화 (User.java에 hp 없음 — 각 클래스 직접 설정)
        User.setName("TestHero");
        Health.set(80, 100);
        Xp.setAll(1500, 2000, 3);
        Coins.set(200, false);

        service = new SaveService();
    }

    @Test
    @Order(1)
    void singleton_sameInstance() {
        JpaManager a = JpaManager.getInstance();
        JpaManager b = JpaManager.getInstance();
        assertSame(a, b, "JpaManager는 항상 동일 인스턴스여야 한다");
    }

    @Test
    @Order(2)
    void saveGame_persistsOneRecord() {
        service.saveGame("MANUAL");

        Optional<SaveSlot> slot = service.loadLatest("TestHero");
        assertTrue(slot.isPresent(), "저장 후 loadLatest는 값이 있어야 한다");
    }

    @Test
    @Order(3)
    void loadLatest_correctValues() {
        service.saveGame("MANUAL");

        Optional<SaveSlot> slot = service.loadLatest("TestHero");
        assertTrue(slot.isPresent());

        SaveSlot s = slot.get();
        System.out.println("[loadLatest] name=" + s.getPlayerName()
                + "  hp=" + s.getHp() + "/" + s.getMaxHp()
                + "  lv=" + s.getLevel()
                + "  xp=" + s.getXp()
                + "  gold=" + s.getGold());

        assertEquals("TestHero", s.getPlayerName());
        assertEquals(80,   s.getHp());
        assertEquals(100,  s.getMaxHp());
        assertEquals(3,    s.getLevel());
        assertEquals(1500, s.getXp());
        assertEquals(200,  s.getGold());
    }

    @Test
    @Order(4)
    void saveBattleLog_usesCountEvents() {
        BattleRecord record = new BattleRecord("TestHero");
        record.record(BattleRecord.EventType.KILL,     1, "Zombie");
        record.record(BattleRecord.EventType.KILL,     1, "Goblin");
        record.record(BattleRecord.EventType.CRITICAL, 50, "Zombie");

        assertDoesNotThrow(() -> service.saveBattleLog(record, "Zombie"));
    }

    @Test
    @Order(5)
    void saveRanking_andGetTopRankings() {
        service.saveRanking();

        List<RankingEntry> rankings = service.getTopRankings();
        assertFalse(rankings.isEmpty(), "랭킹에 최소 1건은 있어야 한다");

        System.out.println("[랭킹 TOP " + rankings.size() + "]");
        rankings.stream()
                .map(r -> "  " + r.getPlayerName()
                        + " | lv." + r.getLevel()
                        + " | kills:" + r.getTotalKills()
                        + " | xp:" + r.getXp())
                .forEach(System.out::println);
    }

    @Test
    @Order(6)
    void saveQuestLog_noException() {
        assertDoesNotThrow(() -> service.saveQuestLog("좀비 사냥꾼", 50));
    }

    @AfterAll
    static void tearDown() {
        JpaManager.getInstance().close();
        System.clearProperty("textfighter.persistenceUnit");
        System.out.println("[JpaTest] EMF 정상 종료");
    }
}
