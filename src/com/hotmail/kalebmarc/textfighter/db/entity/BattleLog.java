package com.hotmail.kalebmarc.textfighter.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BATTLE_LOG")
public class BattleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int    totalKills;
    private int    critCount;
    private String enemyName;
    private LocalDateTime loggedAt;

    public BattleLog() {}

    public Long          getId()         { return id;         }
    public String        getPlayerName() { return playerName; }
    public int           getTotalKills() { return totalKills; }
    public int           getCritCount()  { return critCount;  }
    public String        getEnemyName()  { return enemyName;  }
    public LocalDateTime getLoggedAt()   { return loggedAt;   }

    public void setId(Long id)                   { this.id         = id;         }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setTotalKills(int totalKills)    { this.totalKills = totalKills; }
    public void setCritCount(int critCount)      { this.critCount  = critCount;  }
    public void setEnemyName(String enemyName)   { this.enemyName  = enemyName;  }
    public void setLoggedAt(LocalDateTime t)     { this.loggedAt   = t;          }
}
