# Text Fighter — 고급 자바 프로그래밍 업그레이드 과제

> 학번: 20231650 &nbsp;&nbsp; 이름: 조가연

---

## 프로젝트명

**Text Fighter Upgrade**  
기존 텍스트 기반 RPG 전투 게임을 Java 고급 기능, JavaFX GUI, JPA 저장 시스템, REST API, JWT 인증, 테스트, 멀티플레이 실험 기능까지 확장한 프로젝트입니다.

---

## 프로그램 개요

Text Fighter는 원본 오픈소스 텍스트 RPG 게임을 기반으로, 수업에서 학습한 Java 개념을 실제 코드 구조 개선에 적용한 프로젝트입니다.  
1차 개선(OOP·디자인 패턴·멀티스레드)을 기반으로, 이번 업그레이드 버전에서 다음 기능을 추가했습니다.

- **JavaFX GUI**: 콘솔 중심 게임에 GUI 화면과 랭킹 팝업 추가
- **JPA 기반 저장 시스템**: 플레이어 전투 기록과 점수를 DB에 저장
- **REST API**: 저장된 플레이어 기록과 랭킹을 외부에서 조회 가능하도록 API 제공
- **최소 JWT 인증**: 로그인 API에서 토큰 발급 후 보호 API 접근 시 검증
- **JavaFX ↔ REST 연동**: JavaFX 화면에서 `HttpClient`로 API 호출 후 랭킹 표시
- **JUnit5 + AssertJ 테스트**: 전투 전략, 저장 서비스, 전투 분석 로직 단위 테스트
- **멀티플레이 실험 기능**: `ServerSocket` 기반 1:1 전투 대전 구조 실험

**원본 프로젝트:** https://github.com/hhaslam11/Text-Fighter

---

## 업그레이드 목표

이번 버전의 핵심 목표는 단순히 기능을 많이 추가하는 것이 아니라, 수업에서 배운 개념들이 하나의 프로그램 안에서 어떻게 연결되는지 보여주는 것입니다.

| 구분 | 기존 개선 버전 | 업그레이드 버전 |
|------|---------------|----------------|
| 실행 방식 | 콘솔 기반 텍스트 게임 | 콘솔 + JavaFX GUI |
| 데이터 저장 | 자동저장 로그 중심 | JPA + H2 DB 기반 플레이어 기록 저장 |
| 외부 연동 | 없음 | REST API 제공 |
| 인증 | 없음 | 최소 JWT 발급/검증 |
| 화면 기능 | 터미널 출력 | JavaFX 랭킹 팝업 |
| 테스트 | Step별 수동 테스트 | JUnit5 + AssertJ 단위 테스트 |
| 네트워크 | 없음 | ServerSocket 기반 1:1 전투 실험 |
| 발표 포인트 | Java 문법/패턴 적용 | GUI-API-DB-Test가 연결되는 구조 |

---

## 사용 기술

| 분야 | 기술 |
|------|------|
| Language | Java 21 |
| Build Tool | Gradle |
| GUI | JavaFX |
| API | Spring Boot REST API |
| Persistence | JPA (Hibernate 6.4), H2 Database |
| Auth | JWT |
| Test | JUnit5, AssertJ |
| Network | ServerSocket (P2P 1:1) |
| Java Core | OOP, Generic, Collection, Lambda, Stream, Optional, Thread |
| Design Pattern | Factory, Strategy, Observer, Singleton |

---

## 사용한 주요 자바 개념

| 개념 | 적용 위치 | 설명 |
|------|----------|------|
| 인터페이스 / OOP | `Item`, `GameObserver`, `EnemyFactory`, `AttackStrategy` | 역할을 인터페이스로 분리하고 다형성 기반으로 확장 |
| 제네릭 | `Inventory<T extends Item>` | 타입 안전한 범용 인벤토리 구현 |
| 컬렉션 | `List`, `Map`, `LinkedHashMap` | 아이템, 적, 전투 기록, 랭킹 데이터 관리 |
| 람다 / 스트림 | `BattleAnalyzer`, `EnemyRegistry`, Leaderboard 정렬 | 선언형 데이터 처리 |
| Optional | `EnemyRegistry.create()`, `Inventory.findFirst()` | null 방어 코드 감소 |
| Predicate | `BattleAnalyzer` | 칭호 판정 조건 분리 |
| enum | `PlayerTitle`, `GameEvent`, `BattleRecord.EventType` | 타입 안전한 상수 및 칭호·이벤트 관리 |
| Factory Pattern | `EnemyFactory`, `EnemyRegistry` | 적 생성 로직 분리 |
| Strategy Pattern | `AttackStrategy`, `BattleManager` | 공격 전략 런타임 교체 |
| Observer Pattern | `GameObserver`, `QuestManager` | 전투 이벤트와 퀘스트 시스템 분리 |
| Singleton Pattern | `QuestManager`, `GameLogger`, `JpaManager` | 전역 관리 객체 제공 |
| 멀티스레드 | `AutoSaveTask`, Multiplayer Thread | 자동저장, 소켓 연결 비동기 처리 |
| synchronized / volatile | `GameLogger` | 멀티스레드 환경에서 안전한 로그 기록 |
| AtomicInteger | `AutoSaveTask.saveCount` | 스레드 안전한 저장 횟수 카운터 |
| JPA | `entity/SaveSlot`, `JpaManager`, `SaveService` | 플레이어 기록 영속화 |
| REST API | `StatsController`, `AuthController`, `LeaderboardController` | 게임 데이터를 HTTP로 제공 |
| JWT | `JwtUtil`, `JwtAuthFilter` | 토큰 발급과 검증 |
| JavaFX | `GameFXApp`, `GameFXWindow`, `MultiplayerLobbyController` | GUI 화면 및 API 연동 |
| JUnit5 + AssertJ | `BattleManagerTest`, `BattleAnalyzerTest`, `InventoryTest`, `SaveServiceTest`, `DamageCalculatorTest` | 핵심 로직 검증 |

---

## 전체 구조

```text
src/com/hotmail/kalebmarc/textfighter/
│
├── main/
│   ├── Game.java                         기존 콘솔 게임 실행 및 시스템 연동
│   ├── GameFXApp.java                    JavaFX Application 진입점
│   ├── GameFXWindow.java                 JavaFX 메인 창 (랭킹 팝업 포함)
│   ├── MultiplayerLobbyController.java   멀티플레이 로비 UI
│   └── MultiplayerBattleController.java  멀티플레이 전투 UI
│
├── inventory/                            [1차 개선] 제네릭 인벤토리
│   ├── Item.java
│   ├── Inventory.java
│   ├── PotionItem.java
│   └── FirstAidItem.java
│
├── enemy/                                [1차 개선] Factory Pattern
│   ├── EnemyFactory.java
│   └── EnemyRegistry.java
│
├── battle/                               [1차 개선] Strategy + Stream 분석
│   ├── AttackStrategy.java
│   ├── AttackStrategies.java
│   ├── BattleManager.java
│   ├── BattleRecord.java
│   ├── PlayerTitle.java
│   ├── BattleAnalyzer.java
│   └── DamageCalculator.java             [업그레이드] TDD로 구현한 데미지 계산기
│
├── quest/                                [1차 개선] Observer Pattern
│   ├── GameEvent.java
│   ├── GameObserver.java
│   ├── Quest.java
│   ├── KillQuest.java
│   ├── CriticalQuest.java
│   └── QuestManager.java
│
├── util/                                 [1차 개선] Thread + Singleton
│   ├── AutoSaveTask.java
│   └── GameLogger.java
│
├── db/                                   [업그레이드] JPA 저장 시스템
│   ├── JpaManager.java                   Singleton EMF 관리
│   ├── SaveService.java                  저장/조회 트랜잭션 처리
│   └── entity/
│       ├── SaveSlot.java                 플레이어 스냅샷 엔티티
│       ├── BattleLog.java                전투 결과 엔티티
│       ├── RankingEntry.java             랭킹 엔티티
│       └── QuestLog.java                 퀘스트 완료 엔티티
│
├── api/                                  [업그레이드] REST API + JWT
│   ├── ApiApplication.java               Spring Boot 진입점
│   ├── JwtUtil.java                      JWT 발급/파싱
│   ├── JwtAuthFilter.java                JWT 검증 필터
│   ├── SecurityConfig.java               Spring Security 설정
│   ├── LeaderboardClient.java            JavaFX에서 API 호출용 클라이언트
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── StatsController.java
│   │   └── LeaderboardController.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── StatsResponse.java
│       ├── RankingDto.java
│       └── LeaderboardResponse.java
│
└── multiplayer/                          [업그레이드] 멀티플레이 실험 (P2P)
    ├── BattleServer.java                 호스트 소켓 서버
    ├── BattleClient.java                 게스트 소켓 클라이언트
    ├── BattleMessage.java                메시지 포맷 유틸
    └── MultiplayerBattle.java            P2P 전투 진행 관리
```

---

## 실행 방법

### 환경

- Java 21 이상
- Gradle
- Windows 10/11
- JavaFX 실행 환경
- H2 Database

---

### 1. 콘솔 게임 + JavaFX GUI 실행

기존 텍스트 RPG 게임을 실행합니다.  
게임 시작 시 JavaFX 창이 함께 열리며, 랭킹 보기 버튼을 통해 REST API 연동 팝업을 확인할 수 있습니다.

```bash
./gradlew run
```

Windows PowerShell:

```powershell
.\gradlew run
```

> JavaFX 랭킹 팝업을 확인하려면 REST API 서버가 먼저 실행되어 있어야 합니다.

---

### 2. REST API 서버 실행

JPA 저장 시스템, 로그인, 랭킹 조회 API를 실행합니다.

```bash
./gradlew runApi
```

Windows PowerShell:

```powershell
.\gradlew runApi
```

서버 실행 후 기본 주소:

```text
http://localhost:8080
```

---

### 3. 테스트 실행

```bash
./gradlew test
```

---

### 4. 멀티플레이 소켓 연결 테스트

멀티플레이는 전체 게임이 아니라, 1:1 전투 한 판을 네트워크로 주고받는 실험 기능입니다.  
게임 내 멀티플레이 메뉴에서 직접 실행하거나, 아래 Gradle 태스크로 소켓 연결만 별도 테스트할 수 있습니다.

```powershell
# 터미널 1 — 호스트 (포트 9999 대기)
.\gradlew runConnectionServer

# 터미널 2 — 게스트 (localhost 연결)
.\gradlew runConnectionClient
```

---

## 1차 개선 요약 (업그레이드 전)

| 기능 | 클래스 | 적용 개념 |
|------|--------|----------|
| 제네릭 인벤토리 | `Inventory<T extends Item>` | Generic, Stream, Optional |
| Factory Pattern | `EnemyRegistry` | Lambda, Factory |
| Strategy Pattern | `BattleManager`, `AttackStrategies` | Strategy, Functional Interface |
| Stream 전투 분석 | `BattleAnalyzer`, `BattleRecord` | Stream, Predicate, enum |
| Observer Pattern | `QuestManager`, `Quest` | Observer, abstract class |
| 멀티스레드 자동저장 | `AutoSaveTask`, `GameLogger` | Thread, AtomicInteger, Singleton |

자세한 내용은 `README_v0.md`를 참고하세요.

---

## 업그레이드 기능

### 1. JPA 기반 Save System

기존 자동저장은 로그 또는 파일 중심 기능에 가까웠습니다.  
업그레이드 버전에서는 전투 결과와 플레이어 상태를 JPA Entity로 저장하고, `SaveService`를 통해 저장/조회 로직을 분리했습니다.

```java
saveService.saveGame("MANUAL");
```

**저장 데이터 예시 — `SaveSlot` 엔티티**

| 필드 | 설명 |
|------|------|
| `playerName` | 플레이어 이름 |
| `hp` / `maxHp` | 현재 HP / 최대 HP |
| `level` | 플레이어 레벨 |
| `xp` | 경험치 |
| `gold` | 보유 골드 |
| `saveType` | 저장 타입 (AUTO / MANUAL) |
| `savedAt` | 저장 시각 |

**적용 개념**

- Entity (`SaveSlot`, `BattleLog`, `RankingEntry`, `QuestLog`)
- EntityManager / EntityManagerFactory
- Service Layer
- JPA / Hibernate 6.4
- H2 Database (파일 DB / 테스트용 인메모리 DB)

---

### 2. REST API

저장된 플레이어 기록과 랭킹을 외부에서 조회할 수 있도록 REST API를 추가했습니다.  
이 기능을 통해 콘솔 게임의 데이터를 JavaFX GUI에서도 사용할 수 있게 되었습니다.

| Method | URL | 인증 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/login` | 필요 없음 | 플레이어 이름으로 간단 로그인 후 JWT 발급 |
| `GET` | `/api/stats/{playerName}` | 필요 | 특정 플레이어 최근 저장 기록 조회 |
| `GET` | `/api/leaderboard` | 필요 없음 | 레벨/경험치 기준 상위 10위 랭킹 조회 |

### 로그인 요청 예시

```http
POST /api/login
Content-Type: application/json

{
  "playerName": "gayeon"
}
```

### 로그인 응답 예시

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 보호 API 요청 예시

```http
GET /api/stats/gayeon
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### 3. 최소 JWT 인증

OAuth2나 RBAC까지 구현하지 않고, 수업 핵심 범위에 맞춰 **JWT 발급과 검증 흐름**만 구현했습니다.

**흐름**

```text
1. JavaFX 또는 클라이언트에서 /api/login 요청
2. 서버가 playerName을 기반으로 JWT 발급
3. 클라이언트가 토큰 저장
4. 보호 API 요청 시 Authorization 헤더에 Bearer 토큰 포함
5. JwtAuthFilter가 토큰 검증
6. 검증 성공 시 API 응답 반환
```

**의도**

- 인증의 전체 구조를 과하게 확장하지 않음
- JWT의 핵심인 발급 → 전달 → 검증 과정을 직접 구현
- JavaFX REST 연동에서 실제 토큰 사용 흐름을 보여줌

---

### 4. JavaFX GUI + 랭킹 팝업

기존 터미널 중심 프로그램에 JavaFX GUI를 추가했습니다.  
`GameFXApp`이 콘솔 게임 시작 시 함께 실행되고, `GameFXWindow`에서 랭킹 보기 버튼을 클릭하면 REST API 서버에 요청하여 랭킹 데이터를 화면에 표시합니다.

**동작 흐름**

```text
./gradlew run 실행
   ↓
GameFXApp + 콘솔 게임 동시 시작
   ↓
랭킹 보기 버튼 클릭
   ↓
/api/login 요청 → JWT 토큰 수신
   ↓
/api/leaderboard 요청
   ↓
랭킹 팝업에 결과 표시
```

**구현 포인트**

- `HttpClient`로 REST API 호출 (`LeaderboardClient`)
- JSON 응답 파싱
- JavaFX 팝업/테이블에 랭킹 표시
- 멀티플레이 로비 (`MultiplayerLobbyController`) / 전투 UI (`MultiplayerBattleController`) 포함
- 9강 JavaFX, 11강 REST API, 12강 JWT 내용 연결

---

### 5. JUnit5 + AssertJ 테스트

핵심 로직을 단위 테스트로 검증했습니다.  
테스트 코드는 AAA 패턴을 따르고, `@DisplayName`으로 테스트 의도를 한글로 표현했습니다.

| 테스트 대상 | 검증 내용 |
|------------|----------|
| `BattleManagerTest` | Strategy에 따라 공격 결과가 달라지는지, reset/takeDamage 통계 검증 |
| `BattleAnalyzerTest` | 전투 기록에 따라 올바른 칭호가 부여되는지, Stream 분석 결과 검증 |
| `InventoryTest` | 아이템 추가/제거/조회 동작, findFirst Optional 검증 |
| `SaveServiceTest` | 플레이어 기록이 저장/조회되는지, H2 인메모리 DB로 격리 테스트 |
| `DamageCalculatorTest` | TDD 방식으로 데미지 계산 로직 검증 (Red → Green → Refactor) |

### 테스트 예시

```java
@Test
@DisplayName("근접 공격은 설정한 범위 안의 데미지를 반환한다")
void melee_damage_is_within_range() {
    // Arrange
    BattleManager manager = new BattleManager(AttackStrategies.MELEE);

    // Act
    int damage = manager.attack(5, 15, "주먹");

    // Assert
    assertThat(damage).isBetween(5, 15);
}
```

**테스트에서 보여주고 싶은 점**

- 테스트는 단순 확인용 코드가 아니라 설계 도구가 될 수 있음
- Strategy, Service, Analyzer처럼 독립된 구조일수록 테스트하기 쉬움
- 기존 하드코딩 구조를 분리한 이유를 테스트 가능성으로 설명 가능

---

### 6. 멀티플레이어 실험 기능

`ServerSocket`을 사용하여 1:1 P2P 전투 대전을 실험했습니다.  
전체 RPG를 네트워크 게임으로 바꾸는 것이 아니라, 전투 한 판만 호스트와 게스트가 소켓으로 주고받는 작은 범위로 구현했습니다.

**구조**

```text
Host (BattleServer — 포트 9999 대기)
   ↕  TCP Socket
Guest (BattleClient — 호스트 IP로 연결)
```

**구현 범위**

- 호스트: `BattleServer`가 포트 9999에서 연결 대기
- 게스트: `BattleClient`가 호스트 IP로 직접 소켓 연결
- `BattleMessage`로 ATTACK/DEFEND/HP/RESULT 메시지 포맷 관리
- `MultiplayerBattle`이 턴 교환 및 승패 판정 처리
- JavaFX UI(`MultiplayerLobbyController`, `MultiplayerBattleController`)에서 조작

**적용 개념**

- Socket / ServerSocket
- Thread (백그라운드 소켓 I/O)
- `BufferedReader.readLine()` 블로킹 대기
- P2P 메시지 프로토콜

---

## API 명세

### POST `/api/login`

플레이어 이름으로 간단 로그인하고 JWT를 발급합니다.

**Request**

```json
{
  "playerName": "gayeon"
}
```

**Response**

```json
{
  "token": "JWT_TOKEN_VALUE"
}
```

---

### GET `/api/stats/{playerName}`

특정 플레이어의 최근 저장 기록을 조회합니다.

**Header**

```http
Authorization: Bearer JWT_TOKEN_VALUE
```

**Response**

```json
{
  "playerName": "gayeon",
  "level": 3,
  "hp": 80,
  "maxHp": 100,
  "gold": 200,
  "xp": 1500,
  "savedAt": "2026-06-18T10:00:00"
}
```

---

### GET `/api/leaderboard`

레벨/경험치 기준 상위 10위 랭킹을 조회합니다.

**Response**

```json
{
  "rankings": [
    {
      "rank": 1,
      "playerName": "gayeon",
      "level": 10,
      "totalKills": 42,
      "xp": 9800
    },
    {
      "rank": 2,
      "playerName": "player2",
      "level": 7,
      "totalKills": 28,
      "xp": 5300
    }
  ]
}
```

---

## 실행 화면


### JavaFX 메인 화면

![JavaFX 메인 화면](screenshots/javafx_main.png)

### JavaFX 랭킹 팝업

![JavaFX 랭킹 팝업](screenshots/javafx_leaderboard_popup.png)

### REST API 응답

![REST API 응답](screenshots/restapi1.png)
![REST API 응답](screenshots/restapi2.png)
![REST API 응답](screenshots/restapi2_1.png)
![REST API 응답](screenshots/restapi3.png)

### 테스트

![테스트 코드](screenshots/test.png)

---

## 본인이 구현한 부분

원본 프로젝트의 기존 구조를 기반으로 아래 기능을 직접 설계하고 구현했습니다.

### 1차 구조 개선

- `inventory/` — `Item`, `Inventory<T>`, `PotionItem`, `FirstAidItem`
- `enemy/` — `EnemyFactory`, `EnemyRegistry`
- `battle/` — `AttackStrategy`, `AttackStrategies`, `BattleManager`, `BattleRecord`, `PlayerTitle`, `BattleAnalyzer`
- `quest/` — `GameEvent`, `GameObserver`, `Quest`, `KillQuest`, `CriticalQuest`, `QuestManager`
- `util/` — `AutoSaveTask`, `GameLogger`
- `main/Game.java` — 개선된 시스템 연동, 한글화, getter 추가

### 업그레이드 기능

- `db/` — JPA 기반 저장 Entity (`SaveSlot`, `BattleLog`, `RankingEntry`, `QuestLog`), `JpaManager`, `SaveService`
- `api/` — 로그인/플레이어 기록/랭킹 REST Controller, JWT 발급(`JwtUtil`) 및 검증 필터(`JwtAuthFilter`), `LeaderboardClient`
- `main/` — `GameFXApp`, `GameFXWindow` (랭킹 팝업), `MultiplayerLobbyController`, `MultiplayerBattleController`
- `multiplayer/` — `BattleServer`, `BattleClient`, `BattleMessage`, `MultiplayerBattle` (P2P 소켓 전투)
- `battle/DamageCalculator` — TDD로 구현한 데미지 계산기
- 테스트 — `BattleManagerTest`, `BattleAnalyzerTest`, `InventoryTest`, `SaveServiceTest`, `DamageCalculatorTest`



## 트러블슈팅 / 구현 중 고민한 점

### 1. 기존 콘솔 게임과 새 구조의 연결

원본 프로젝트는 전역 static 변수와 직접 호출이 많아 새 구조를 바로 적용하기 어려웠습니다.  
따라서 기존 코드를 모두 갈아엎기보다, `BattleManager`, `EnemyRegistry`, `QuestManager` 같은 중간 계층을 추가하여 점진적으로 연결했습니다.

### 2. GUI와 콘솔 게임의 역할 분리

JavaFX로 전체 게임을 다시 만들면 범위가 너무 커질 수 있다고 판단했습니다.  
따라서 GUI는 핵심 데모인 **랭킹 조회와 REST 연동**에 집중하고, 기존 게임 로직은 콘솔 실행을 유지했습니다.

### 3. JWT 범위 조절

OAuth2, RBAC까지 구현하면 프로젝트 범위가 커지기 때문에, 이번 과제에서는 `/api/login`에서 토큰을 발급하고 보호 API에서 검증하는 최소 흐름만 구현했습니다.

### 4. 테스트 가능한 구조 만들기

처음에는 게임 로직이 `Game.java`에 몰려 있어 테스트하기 어려웠습니다.  
Strategy, Service, Analyzer처럼 역할을 분리하면서 테스트 코드 작성이 쉬워졌고, 테스트가 구조 개선의 이유를 설명해주는 근거가 되었습니다.

### 5. JpaTest와 SaveServiceTest 충돌 여부

두 테스트 클래스가 모두 `JpaManager` 싱글톤과 H2 인메모리 DB를 사용합니다.  
`JpaManager.close()`가 `instance = null`을 설정하고 `create-drop` 설정이 EMF 생성 시마다 테이블을 재생성하기 때문에, 실행 순서에 관계없이 충돌 없이 동작합니다.

---

## AI 활용 여부 및 활용 범위

본 프로젝트는 Claude를 활용한 바이브코딩 방식으로 진행했습니다.

**설계 단계**  
수업에서 배운 개념을 어떤 기능에 적용할지 Claude와 함께 후보를 정리했습니다.  
다만 단순히 제안을 그대로 사용하지 않고, 기존 코드와 충돌하지 않는지, 발표에서 설명 가능한 범위인지 직접 판단했습니다.

**구현 단계**  
Claude가 제안한 코드 초안을 IntelliJ에서 직접 실행하고, 컴파일 오류와 런타임 오류를 수정했습니다.  
특히 REST API, JavaFX 연동, JWT 검증 흐름은 실제 실행 결과를 확인하면서 프로젝트 구조에 맞게 수정했습니다.

**테스트 단계**  
JUnit5 테스트 초안을 만들고, 직접 실행하면서 실패하는 테스트를 기준으로 코드를 보완했습니다.  
TDD 방식으로 작은 기능을 추가하면서 테스트가 설계 도구가 될 수 있다는 점을 확인했습니다.

**문서화 단계**  
README와 발표 자료는 Claude 초안을 바탕으로 프로젝트 실제 구조, 구현 범위, 본인이 설명할 수 있는 내용 중심으로 수정했습니다.

---


*고급 자바 프로그래밍 | 2026년 1학기*
