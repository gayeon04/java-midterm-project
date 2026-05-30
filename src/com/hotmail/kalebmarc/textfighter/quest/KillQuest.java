package com.hotmail.kalebmarc.textfighter.quest;

/**
 * [Step 5] KillQuest - 적 처치 퀘스트
 *
 * 특정 적을 N번 처치하면 완료.
 * ENEMY_KILLED 이벤트 수신 시 카운트 증가.
 *
 * 적용 개념: Observer Pattern 구현체, OOP 심화
 */
public class KillQuest extends Quest {

    private final String targetEnemy;   // null이면 모든 적 처치 카운트
    private final int    requiredKills;
    private int          currentKills = 0;

    public KillQuest(String title, String targetEnemy, int requiredKills, int rewardCoins) {
        super(title,
                targetEnemy != null
                        ? targetEnemy + "을(를) " + requiredKills + "번 처치하세요."
                        : "적을 " + requiredKills + "번 처치하세요.",
                rewardCoins);
        this.targetEnemy   = targetEnemy;
        this.requiredKills = requiredKills;
    }

    @Override
    public void onEvent(GameEvent event, Object data) {
        if (completed) return;
        if (event != GameEvent.ENEMY_KILLED) return;

        // targetEnemy가 null이면 모든 적 카운트
        // 아니면 이름 일치할 때만 카운트
        boolean matches = (targetEnemy == null)
                || (data != null && data.toString().equalsIgnoreCase(targetEnemy));

        if (matches) {
            currentKills++;
            com.hotmail.kalebmarc.textfighter.main.Ui.println("   [퀘스트] " + title + ": " + currentKills + " / " + requiredKills);
            if (currentKills >= requiredKills) {
                complete();
            }
        }
    }

    public int getCurrentKills() { return currentKills;  }
    public int getRequiredKills(){ return requiredKills; }
}