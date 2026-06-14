package com.hotmail.kalebmarc.textfighter.api.dto;

import java.time.LocalDateTime;

public record StatsResponse(
        String playerName,
        int level,
        int hp,
        int maxHp,
        int gold,
        int xp,
        LocalDateTime savedAt
) {}
