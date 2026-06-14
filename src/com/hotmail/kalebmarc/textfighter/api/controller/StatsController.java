package com.hotmail.kalebmarc.textfighter.api.controller;

import com.hotmail.kalebmarc.textfighter.api.dto.StatsResponse;
import com.hotmail.kalebmarc.textfighter.db.SaveService;
import com.hotmail.kalebmarc.textfighter.db.entity.SaveSlot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final SaveService saveService = new SaveService();

    @GetMapping("/{playerName}")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String playerName) {
        Optional<SaveSlot> opt = saveService.loadLatest(playerName);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SaveSlot s = opt.get();
        return ResponseEntity.ok(new StatsResponse(
                s.getPlayerName(),
                s.getLevel(),
                s.getHp(),
                s.getMaxHp(),
                s.getGold(),
                s.getXp(),
                s.getSavedAt()
        ));
    }
}
