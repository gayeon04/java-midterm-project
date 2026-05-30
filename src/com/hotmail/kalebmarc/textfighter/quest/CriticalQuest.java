package com.hotmail.kalebmarc.textfighter.quest;

/**
 * [Step 5] CriticalQuest - 크리티컬 히트 퀘스트
 *
 * 크리티컬을 N번 내면 완료.
 * Observer Pattern으로 CRITICAL_HIT 이벤트 수신.
 */
public class CriticalQuest extends Quest {

    private final int requiredCrits;
    private int       currentCrits = 0;

    public CriticalQuest(String title, int requiredCrits, int rewardCoins) {
        super(title, "크리티컬 히트를 " + requiredCrits + "번 내세요.", rewardCoins);
        this.requiredCrits = requiredCrits;
    }

    @Override
    public void onEvent(GameEvent event, Object data) {
        if (completed) return;
        if (event != GameEvent.CRITICAL_HIT) return;

        currentCrits++;
        com.hotmail.kalebmarc.textfighter.main.Ui.println("   [퀘스트] " + title + ": " + currentCrits + " / " + requiredCrits);
        if (currentCrits >= requiredCrits) {
            complete();
        }
    }
}