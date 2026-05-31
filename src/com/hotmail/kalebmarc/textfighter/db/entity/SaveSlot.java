package com.hotmail.kalebmarc.textfighter.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SAVE_SLOT")
public class SaveSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int    hp;
    private int    maxHp;
    private int    level;
    private int    gold;
    private int    xp;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    private String saveType;  // "AUTO" | "MANUAL"

    public SaveSlot() {}

    public Long          getId()         { return id;         }
    public String        getPlayerName() { return playerName; }
    public int           getHp()         { return hp;         }
    public int           getMaxHp()      { return maxHp;      }
    public int           getLevel()      { return level;      }
    public int           getGold()       { return gold;       }
    public int           getXp()         { return xp;         }
    public LocalDateTime getSavedAt()    { return savedAt;    }
    public String        getSaveType()   { return saveType;   }

    public void setId(Long id)                   { this.id         = id;         }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setHp(int hp)                    { this.hp         = hp;         }
    public void setMaxHp(int maxHp)              { this.maxHp      = maxHp;      }
    public void setLevel(int level)              { this.level      = level;      }
    public void setGold(int gold)                { this.gold       = gold;       }
    public void setXp(int xp)                    { this.xp         = xp;         }
    public void setSavedAt(LocalDateTime savedAt){ this.savedAt    = savedAt;    }
    public void setSaveType(String saveType)     { this.saveType   = saveType;   }
}
