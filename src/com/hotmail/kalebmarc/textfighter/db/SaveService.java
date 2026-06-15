package com.hotmail.kalebmarc.textfighter.db;

import com.hotmail.kalebmarc.textfighter.battle.BattleRecord;
import com.hotmail.kalebmarc.textfighter.db.entity.BattleLog;
import com.hotmail.kalebmarc.textfighter.db.entity.QuestLog;
import com.hotmail.kalebmarc.textfighter.db.entity.RankingEntry;
import com.hotmail.kalebmarc.textfighter.db.entity.SaveSlot;
import com.hotmail.kalebmarc.textfighter.main.User;
import com.hotmail.kalebmarc.textfighter.player.Coins;
import com.hotmail.kalebmarc.textfighter.player.Health;
import com.hotmail.kalebmarc.textfighter.player.Stats;
import com.hotmail.kalebmarc.textfighter.player.Xp;
import com.hotmail.kalebmarc.textfighter.util.GameLogger;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SaveService {

    // ── 쓰기 메서드 ─────────────────────────────────────────────────────────

    /** 자동/수동 저장 — 플레이어 스냅샷을 DB에 persist */
    public void saveGame(String saveType) {
        try (EntityManager em = JpaManager.getInstance().createEntityManager()) {
            em.getTransaction().begin();
            try {
                SaveSlot slot = new SaveSlot();
                slot.setPlayerName(User.name());
                slot.setHp(Health.get());
                slot.setMaxHp(Health.getOutOf());
                slot.setLevel(Xp.getLevel());
                slot.setXp(Xp.get());
                slot.setGold(Coins.get());
                slot.setSavedAt(LocalDateTime.now());
                slot.setSaveType(saveType);
                em.persist(slot);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                GameLogger.getInstance().error("게임 저장 실패: " + e.getMessage());
            }
        }
    }

    /** 전투 결과 기록 — BattleRecord의 이벤트 카운트를 BattleLog로 변환 */
    public void saveBattleLog(BattleRecord record, String enemyName) {
        try (EntityManager em = JpaManager.getInstance().createEntityManager()) {
            em.getTransaction().begin();
            try {
                BattleLog log = new BattleLog();
                log.setPlayerName(record.getPlayerName());
                log.setTotalKills((int) record.countEvents(BattleRecord.EventType.KILL));
                log.setCritCount((int) record.countEvents(BattleRecord.EventType.CRITICAL));
                log.setEnemyName(enemyName);
                log.setLoggedAt(LocalDateTime.now());
                em.persist(log);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                GameLogger.getInstance().error("전투 로그 저장 실패: " + e.getMessage());
            }
        }
    }

    /** 완료된 퀘스트 기록 */
    public void saveQuestLog(String questName, int rewardCoins) {
        try (EntityManager em = JpaManager.getInstance().createEntityManager()) {
            em.getTransaction().begin();
            try {
                QuestLog log = new QuestLog();
                log.setPlayerName(User.name());
                log.setQuestName(questName);
                log.setRewardCoins(rewardCoins);
                log.setCompletedAt(LocalDateTime.now());
                em.persist(log);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                GameLogger.getInstance().error("퀘스트 로그 저장 실패: " + e.getMessage());
            }
        }
    }

    /** 게임 종료 시 랭킹 등록 — 플레이어당 최고 기록만 유지 (upsert) */
    public void saveRanking() {
        try (EntityManager em = JpaManager.getInstance().createEntityManager()) {
            em.getTransaction().begin();
            try {
                List<RankingEntry> existing = em.createQuery(
                        "SELECT r FROM RankingEntry r WHERE r.playerName = :name",
                        RankingEntry.class)
                        .setParameter("name", User.name())
                        .getResultList();

                RankingEntry entry;
                if (!existing.isEmpty()) {
                    RankingEntry prev = existing.get(0);
                    int curLevel = Xp.getLevel(), curXp = Xp.get();
                    // 이전 기록보다 못하면 업데이트 안 함
                    if (curLevel < prev.getLevel() ||
                            (curLevel == prev.getLevel() && curXp <= prev.getXp())) {
                        em.getTransaction().rollback();
                        return;
                    }
                    entry = prev;
                } else {
                    entry = new RankingEntry();
                    entry.setPlayerName(User.name());
                    em.persist(entry);
                }

                entry.setLevel(Xp.getLevel());
                entry.setTotalKills(Stats.totalKills);
                entry.setXp(Xp.get());
                entry.setPlayedAt(LocalDateTime.now());
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                GameLogger.getInstance().error("랭킹 저장 실패: " + e.getMessage());
            }
        }
    }

    // ── 읽기 메서드 ─────────────────────────────────────────────────────────

    /** 가장 최근 SaveSlot 로드 (Optional 반환) */
    public Optional<SaveSlot> loadLatest(String playerName) {
        try (EntityManager em = JpaManager.getInstance().createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM SaveSlot s WHERE s.playerName = :name ORDER BY s.savedAt DESC",
                    SaveSlot.class
            )
            .setParameter("name", playerName)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
        } catch (Exception e) {
            GameLogger.getInstance().error("세이브 로드 실패: " + e.getMessage());
            return Optional.empty();
        }
    }

    /** 상위 10개 랭킹 조회 */
    public List<RankingEntry> getTopRankings() {
        try (EntityManager em = JpaManager.getInstance().createEntityManager()) {
            return em.createQuery(
                    "SELECT r FROM RankingEntry r ORDER BY r.level DESC, r.xp DESC",
                    RankingEntry.class
            )
            .setMaxResults(10)
            .getResultList();
        } catch (Exception e) {
            GameLogger.getInstance().error("랭킹 조회 실패: " + e.getMessage());
            return List.of();
        }
    }
}
