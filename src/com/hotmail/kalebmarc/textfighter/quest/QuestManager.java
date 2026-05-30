package com.hotmail.kalebmarc.textfighter.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [Step 5] QuestManager - Observer Pattern의 Subject
 *
 * 게임 이벤트 발생 시 등록된 모든 Quest(Observer)에게 알림.
 * 기존 Game.java에 최소한의 코드만 추가해서 연동 가능.
 *
 * 연동 예시 (Game.java 전투 처치 시):
 *   QuestManager.getInstance().notify(GameEvent.ENEMY_KILLED, Enemy.get().getName());
 *
 * 적용 개념:
 *   - Observer Pattern (Subject 역할)
 *   - Singleton Pattern (getInstance)
 *   - 컬렉션 (List<GameObserver>)
 *   - Stream (완료된 퀘스트 필터, 진행 중 퀘스트 조회)
 *   - 람다 (forEach)
 */
public class QuestManager {

    // ── Singleton (static holder — lazy + thread-safe, lock 없음) ──────
    private QuestManager() {}

    private static class Holder {
        static final QuestManager INSTANCE = new QuestManager();
    }

    public static QuestManager getInstance() {
        return Holder.INSTANCE;
    }

    // ── Observer 목록 ────────────────────────────────────
    private final List<GameObserver> observers = new ArrayList<>();

    /**
     * 퀘스트 등록 (subscribe).
     */
    public void subscribe(GameObserver observer) {
        observers.add(observer);
        com.hotmail.kalebmarc.textfighter.main.Ui.println("   [퀘스트 등록] " + observer.getName());
    }

    /**
     * 퀘스트 해제 (unsubscribe).
     */
    public void unsubscribe(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * 이벤트 발행 - 등록된 모든 Observer에게 알림.
     * Stream forEach + 람다로 간결하게 처리.
     */
    public void notify(GameEvent event, Object data) {
        observers.forEach(o -> o.onEvent(event, data));
    }

    // ── Stream 활용 조회 메서드 ───────────────────────────

    /**
     * 완료된 퀘스트 목록 (Stream filter).
     */
    public List<GameObserver> getCompletedQuests() {
        return observers.stream()
                .filter(o -> o instanceof Quest && ((Quest) o).isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * 진행 중인 퀘스트 목록.
     */
    public List<GameObserver> getActiveQuests() {
        return observers.stream()
                .filter(o -> o instanceof Quest && !((Quest) o).isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * 전체 퀘스트 진행 상황 출력.
     */
    public void printStatus() {
        com.hotmail.kalebmarc.textfighter.main.Ui.println("\n[퀘스트 현황]");
        com.hotmail.kalebmarc.textfighter.main.Ui.println("  진행 중: " + getActiveQuests().size() + "개");
        com.hotmail.kalebmarc.textfighter.main.Ui.println("  완료:    " + getCompletedQuests().size() + "개");

        getActiveQuests().stream()
                .filter(o -> o instanceof Quest)
                .map(o -> (Quest) o)
                .forEach(q -> com.hotmail.kalebmarc.textfighter.main.Ui.println("  ⏳ " + q.getTitle() + " - " + q.getDescription()));

        getCompletedQuests().stream()
                .filter(o -> o instanceof Quest)
                .map(o -> (Quest) o)
                .forEach(q -> com.hotmail.kalebmarc.textfighter.main.Ui.println("  ✅ " + q.getTitle()));
    }

    public int getTotalObservers() { return observers.size(); }
}