# Text Fighter — Claude 참조 문서

## 빌드 환경
- Gradle + Java 21 + JavaFX 21
- 진입점: `com.hotmail.kalebmarc.textfighter.main.Start`
- **비표준 소스 레이아웃**: Java 소스가 `src/main/java/`가 아니라 `src/`에 직접 있음
- 리소스: `src/main/resources/` (build.gradle에 명시)
- 테스트 파일 규칙: `**/*Test.java` → test sourceSet, 나머지 → main sourceSet

## 패키지 구조

### `main/`
| 클래스 | 역할 |
|---|---|
| `Game.java` | 메인 게임 루프 (while(true)). 모든 시스템 연동 |
| `Start.java` | 진입점. Game 인스턴스 생성 |
| `User.java` | 플레이어 이름만 (`name()`, `setName()`) |
| `Enemy.java` | 현재 적 (`Enemy.get()` static) |
| `Saves.java` | YAML 파일 저장/로드 (기존 방식, 삭제 안 함) |
| `Ui.java` | 콘솔 I/O 헬퍼 (`cls()`, `println()`, `pause()`) |
| `Constants.java` | 앱 상수 |

### `player/` — **플레이어 데이터 분산 저장**
| 클래스 | 역할 | 주요 API |
|---|---|---|
| `Health.java` | HP 관리 | `get()`, `getOutOf()`, `set(hp, maxHp)` |
| `Xp.java` | 경험치·레벨 | `get()`, `getLevel()`, `setAll(xp, outOf, level)` |
| `Coins.java` | 골드 | `get()`, `set(amount, isAdd)` |
| `Stats.java` | 전투 통계 (public static 필드) | `totalKills`, `kills`, `highScore` |
| `Achievements.java` | 업적 플래그 | |
| `Potion.java` | 포션 수량 | |
| `Settings.java` | 난이도, 갓모드 | |

### `battle/`
| 클래스 | 역할 |
|---|---|
| `BattleRecord.java` | 전투 이벤트 DTO. `countEvents(EventType)`, `getTotalDamageDealt()` |
| `BattleManager.java` | 전략 실행자 (Strategy Pattern) |
| `AttackStrategies.java` | 전략 상수: MELEE, SNIPER, SHOTGUN |
| `BattleAnalyzer.java` | Stream 기반 전투 분석 |

### `quest/`
| 클래스 | 역할 |
|---|---|
| `QuestManager.java` | Singleton Observer 주체. `notify(GameEvent, data)` |
| `Quest.java` | 추상 Observer. `rewardCoins` (XP 아님!) |
| `KillQuest.java` / `CriticalQuest.java` | 구체 퀘스트 |
| `GameEvent.java` | 이벤트 enum: ENEMY_KILLED, CRITICAL_HIT, RAN_AWAY |

### `util/`
| 클래스 | 역할 |
|---|---|
| `AutoSaveTask.java` | ScheduledExecutorService 래퍼. `new AutoSaveTask(Runnable, intervalSec)` |
| `GameLogger.java` | Double-checked locking Singleton. `info/warn/error/battle/save(msg)` |

### `db/` (JPA — 2026-05 추가)
| 클래스 | 역할 |
|---|---|
| `JpaManager.java` | Singleton EMF. `createEntityManager()`, `close()` |
| `SaveService.java` | 트랜잭션 관리. 모든 쓰기/읽기 메서드 |
| `entity/SaveSlot.java` | 플레이어 스냅샷 엔티티 |
| `entity/BattleLog.java` | 전투 결과 엔티티 |
| `entity/RankingEntry.java` | 랭킹 엔티티 |
| `entity/QuestLog.java` | 퀘스트 완료 엔티티 |

## 정적 필드 위치 (가장 중요 — User.java에 hp/level/gold 없음!)

| 데이터 | 클래스 | API |
|---|---|---|
| 플레이어 이름 | `User` | `User.name()` |
| 현재 HP | `Health` | `Health.get()` |
| 최대 HP | `Health` | `Health.getOutOf()` |
| 레벨 | `Xp` | `Xp.getLevel()` |
| 경험치 | `Xp` | `Xp.get()` (int) |
| 골드 | `Coins` | `Coins.get()` |
| 총 처치 수 | `Stats` | `Stats.totalKills` |
| 현재 연속 처치 | `Stats` | `Stats.kills` |

## 디자인 패턴 적용 위치

| 패턴 | 위치 |
|---|---|
| Singleton | `GameLogger`, `QuestManager`, `JpaManager` |
| Observer | `QuestManager` (Subject) + `Quest` 서브클래스 (Observer) |
| Strategy | `BattleManager` + `AttackStrategy` 인터페이스 |
| Factory | `EnemyRegistry` |

## JPA 설정 요점

- **DB**: H2 파일 DB → `./saves/textfighter.mv.db` 생성
- **persistence-unit 이름**: `textfighter-pu`
- **Hibernate 버전**: 6.4.4 → `jakarta.persistence.*` 사용 (`javax.*` 아님)
- **`persistence.xml` 위치**: `src/main/resources/META-INF/persistence.xml`
- `BattleLog.totalKills` = `record.countEvents(EventType.KILL)` (직접 필드 없음)
- `Quest.rewardCoins` 는 코인 보상 (XP 아님)

## Game.java 주요 흐름

```
start()
  ├─ saveService = new SaveService()
  ├─ autoSave = new AutoSaveTask(() -> saveService.saveGame("AUTO"), 30)
  ├─ 로드/신규 선택 → autoSave.start()
  └─ while(true) 게임루프
       ├─ case 1: 전투 (BattleManager.attack → BattleRecord.record → QuestManager.notify)
       ├─ case 10: 종료
       │    ├─ autoSave.stop()
       │    ├─ saveService.saveRanking()
       │    ├─ JpaManager.getInstance().close()
       │    └─ printBattleReport() → return
       └─ case 0: 치트, case 99: 디버그
```
