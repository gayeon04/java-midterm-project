package com.hotmail.kalebmarc.textfighter.api.controller;

import com.hotmail.kalebmarc.textfighter.api.dto.LeaderboardResponse;
import com.hotmail.kalebmarc.textfighter.api.dto.RankingDto;
import com.hotmail.kalebmarc.textfighter.db.SaveService;
import com.hotmail.kalebmarc.textfighter.db.entity.RankingEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

    private final SaveService saveService = new SaveService();

    @GetMapping("/leaderboard")
    public LeaderboardResponse getLeaderboard() {
        List<RankingEntry> list = saveService.getTopRankings();
        List<RankingDto> rankings = IntStream.range(0, list.size())
                .mapToObj(i -> new RankingDto(i + 1, list.get(i)))
                .collect(Collectors.toList());
        return new LeaderboardResponse(rankings);
    }
}
