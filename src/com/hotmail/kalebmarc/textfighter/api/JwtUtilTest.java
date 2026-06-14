package com.hotmail.kalebmarc.textfighter.api;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 단위 테스트 — Spring 컨텍스트 없이 @Value 필드를 리플렉션으로 주입
 *
 * 검증 항목:
 *   1. generateToken — null 아님
 *   2. generateToken — header.payload.signature 형식
 *   3. extractUsername — 입력값과 동일
 *   4. validateToken — 유효한 토큰 → true
 *   5. validateToken — 만료 토큰 → false
 *   6. validateToken — 서명 변조 → false
 *   7. validateToken — 비정상 입력 → false
 */
@DisplayName("JwtUtil — 단위 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtUtilTest {

    private static JwtUtil jwtUtil;
    private static final String SECRET = "textfighter-secret-key-must-be-256bits-long-enough";
    private static final long EXPIRATION_MS = 3_600_000L;

    @BeforeAll
    static void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        injectField("secret",     SECRET);
        injectField("expiration", EXPIRATION_MS);
    }

    private static void injectField(String name, Object value) throws Exception {
        Field f = JwtUtil.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(jwtUtil, value);
    }

    // ── 1. generateToken ─────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("1. generateToken — null 또는 빈 문자열 아님")
    void generateToken_returnsNonBlank() {
        String token = jwtUtil.generateToken("hero");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test @Order(2)
    @DisplayName("2. generateToken — header.payload.signature 세 부분 구조")
    void generateToken_hasThreePartFormat() {
        String token = jwtUtil.generateToken("hero");
        assertEquals(3, token.split("\\.").length,
                "JWT 형식은 header.payload.signature 세 부분이어야 한다");
    }

    // ── 2. extractUsername ────────────────────────────────────────

    @Test @Order(3)
    @DisplayName("3. extractUsername — 생성 시 입력한 username과 일치")
    void extractUsername_matchesGeneratedToken() {
        String username = "myPlayer";
        String token    = jwtUtil.generateToken(username);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    // ── 3. validateToken ──────────────────────────────────────────

    @Test @Order(4)
    @DisplayName("4. validateToken — 방금 생성한 유효 토큰 → true")
    void validateToken_validToken_returnsTrue() {
        assertTrue(jwtUtil.validateToken(jwtUtil.generateToken("hero")));
    }

    @Test @Order(5)
    @DisplayName("5. validateToken — 만료된 토큰 → false")
    void validateToken_expiredToken_returnsFalse() throws Exception {
        injectField("expiration", -60_000L);   // 1분 전 만료
        String expired = jwtUtil.generateToken("hero");
        injectField("expiration", EXPIRATION_MS); // 원복
        assertFalse(jwtUtil.validateToken(expired), "만료된 토큰은 false여야 한다");
    }

    @Test @Order(6)
    @DisplayName("6. validateToken — 서명 변조 → false")
    void validateToken_tamperedSignature_returnsFalse() {
        String token    = jwtUtil.generateToken("hero");
        // 마지막 '.' 이후를 가짜 서명으로 교체
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "fakeSignatureXXXYYY";
        assertFalse(jwtUtil.validateToken(tampered), "변조된 서명은 false여야 한다");
    }

    @Test @Order(7)
    @DisplayName("7. validateToken — 빈 문자열 / 단일 파트 / JWT 아닌 값 → 모두 false")
    void validateToken_malformedInputs_allReturnFalse() {
        assertFalse(jwtUtil.validateToken(""),              "빈 문자열");
        assertFalse(jwtUtil.validateToken("only-one-part"), "단일 파트");
        assertFalse(jwtUtil.validateToken("not.a.jwt"),     "잘못된 JWT");
    }
}
