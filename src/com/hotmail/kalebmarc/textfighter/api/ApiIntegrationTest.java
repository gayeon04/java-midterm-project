package com.hotmail.kalebmarc.textfighter.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotmail.kalebmarc.textfighter.db.JpaManager;
import com.hotmail.kalebmarc.textfighter.db.SaveService;
import com.hotmail.kalebmarc.textfighter.main.User;
import com.hotmail.kalebmarc.textfighter.player.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API 통합 테스트 — MockMvc + 인메모리 H2
 *
 * 검증 항목:
 *   [Auth]       Auth-1 ~ Auth-2
 *   [Stats]      Stats-1 ~ Stats-4
 *   [Leaderboard] LB-1 ~ LB-4
 *
 * 의도적 실패 항목:
 *   Stats-1, Stats-2, LB-1 → 토큰 없음/잘못된 토큰 → 401 기대
 *   (현재 SecurityConfig는 기본 403 반환 → 수정 필요)
 */
@SpringBootTest(
        classes = ApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                // Spring DataSource는 인메모리 H2 사용 (파일 생성 방지)
                "spring.datasource.url=jdbc:h2:mem:apitest;DB_CLOSE_DELAY=-1",
                "spring.h2.console.enabled=false"
        }
)
@AutoConfigureMockMvc
@DisplayName("REST API 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private JwtUtil       jwtUtil;
    @Autowired private ObjectMapper  objectMapper;

    private static final String PLAYER = "ApiTestHero";

    @BeforeAll
    static void setUpTestData() {
        // JpaManager를 인메모리 H2로 초기화 (다른 테스트가 파일 DB를 열었을 수 있음)
        if (JpaManager.isInitialized()) JpaManager.getInstance().close();
        System.setProperty("textfighter.persistenceUnit", "textfighter-test-pu");

        User.setName(PLAYER);
        Health.set(80, 100);
        Xp.setAll(1500, 2000, 3);
        Coins.set(200, false);
        Stats.totalKills = 15;

        SaveService svc = new SaveService();
        svc.saveGame("TEST");
        svc.saveRanking();
    }

    @AfterAll
    static void tearDown() {
        if (JpaManager.isInitialized()) JpaManager.getInstance().close();
        System.clearProperty("textfighter.persistenceUnit");
    }

    private String token() {
        return jwtUtil.generateToken(PLAYER);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ══ AuthController ═══════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("Auth-1. 올바른 비밀번호(1234) → 200 + token/username 반환")
    void login_correctPassword_returns200AndToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "hero", "password", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("hero"));
    }

    @Test @Order(2)
    @DisplayName("Auth-2. 잘못된 비밀번호 → 401")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "hero", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    // ══ StatsController ══════════════════════════════════════════════

    @Test @Order(3)
    @DisplayName("Stats-1. 토큰 없음 → 401 [인증 없는 요청은 401이어야 한다]")
    void stats_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/stats/" + PLAYER))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(4)
    @DisplayName("Stats-2. 유효하지 않은 토큰 → 401")
    void stats_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/stats/" + PLAYER)
                        .header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(5)
    @DisplayName("Stats-3. 유효한 토큰 + 존재하는 플레이어 → 200 + 모든 필드 검증")
    void stats_validToken_existingPlayer_returns200() throws Exception {
        mockMvc.perform(get("/api/stats/" + PLAYER)
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value(PLAYER))
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.hp").value(80))
                .andExpect(jsonPath("$.maxHp").value(100))
                .andExpect(jsonPath("$.gold").value(200))
                .andExpect(jsonPath("$.xp").value(1500))
                .andExpect(jsonPath("$.savedAt").isNotEmpty());
    }

    @Test @Order(6)
    @DisplayName("Stats-4. 유효한 토큰 + 존재하지 않는 플레이어 → 404")
    void stats_validToken_missingPlayer_returns404() throws Exception {
        mockMvc.perform(get("/api/stats/nonexistent-player-xyz")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound());
    }

    // ══ LeaderboardController ═════════════════════════════════════════

    @Test @Order(7)
    @DisplayName("LB-1. 토큰 없음 → 401")
    void leaderboard_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(8)
    @DisplayName("LB-2. 유효한 토큰 → 200 + rankings 배열")
    void leaderboard_validToken_returns200WithArray() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings").isArray());
    }

    @Test @Order(9)
    @DisplayName("LB-3. 첫 번째 항목이 rank/playerName/level/totalKills/xp 필드 보유")
    void leaderboard_firstEntryHasAllFields() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.rankings[0].playerName").isNotEmpty())
                .andExpect(jsonPath("$.rankings[0].level").isNumber())
                .andExpect(jsonPath("$.rankings[0].totalKills").isNumber())
                .andExpect(jsonPath("$.rankings[0].xp").isNumber());
    }

    @Test @Order(10)
    @DisplayName("LB-4. rank 번호가 1부터 순서대로 증가")
    void leaderboard_rankNumbersAreSequential() throws Exception {
        String body = mockMvc.perform(get("/api/leaderboard")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode rankings = objectMapper.readTree(body).get("rankings");
        assertTrue(rankings.size() > 0, "랭킹에 데이터가 있어야 한다");
        for (int i = 0; i < rankings.size(); i++) {
            assertEquals(i + 1, rankings.get(i).get("rank").asInt(),
                    "rank[" + i + "]는 " + (i + 1) + "이어야 한다");
        }
    }
}
