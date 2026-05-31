package com.hotmail.kalebmarc.textfighter.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RANKING")
public class RankingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int    level;
    private int    totalKills;
    private int    xp;
    private LocalDateTime playedAt;

    public RankingEntry() {}

    public Long          getId()         { return id;         }
    public String        getPlayerName() { return playerName; }
    public int           getLevel()      { return level;      }
    public int           getTotalKills() { return totalKills; }
    public int           getXp()         { return xp;         }
    public LocalDateTime getPlayedAt()   { return playedAt;   }

    public void setId(Long id)                   { this.id         = id;         }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setLevel(int level)              { this.level      = level;      }
    public void setTotalKills(int totalKills)    { this.totalKills = totalKills; }
    public void setXp(int xp)                    { this.xp         = xp;         }
    public void setPlayedAt(LocalDateTime t)     { this.playedAt   = t;          }
}
