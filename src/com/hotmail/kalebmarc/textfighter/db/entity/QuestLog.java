package com.hotmail.kalebmarc.textfighter.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUEST_LOG")
public class QuestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private String questName;
    private int    rewardCoins;  // Quest.rewardCoins 기준 (XP 아님)
    private LocalDateTime completedAt;

    public QuestLog() {}

    public Long          getId()          { return id;          }
    public String        getPlayerName()  { return playerName;  }
    public String        getQuestName()   { return questName;   }
    public int           getRewardCoins() { return rewardCoins; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setId(Long id)                    { this.id          = id;          }
    public void setPlayerName(String playerName)  { this.playerName  = playerName;  }
    public void setQuestName(String questName)    { this.questName   = questName;   }
    public void setRewardCoins(int rewardCoins)   { this.rewardCoins = rewardCoins; }
    public void setCompletedAt(LocalDateTime t)   { this.completedAt = t;           }
}
