package com.hotmail.kalebmarc.textfighter.api.dto;

import com.hotmail.kalebmarc.textfighter.db.entity.RankingEntry;

public record RankingDto(int rank, String playerName, int level, int totalKills, int xp) {

    public RankingDto(int rank, RankingEntry entry) {
        this(rank, entry.getPlayerName(), entry.getLevel(), entry.getTotalKills(), entry.getXp());
    }
}
