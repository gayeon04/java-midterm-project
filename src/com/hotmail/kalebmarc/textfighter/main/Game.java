package com.hotmail.kalebmarc.textfighter.main;

import com.hotmail.kalebmarc.textfighter.battle.AttackStrategies;
import com.hotmail.kalebmarc.textfighter.battle.AttackStrategy;
import com.hotmail.kalebmarc.textfighter.battle.BattleAnalyzer;
import com.hotmail.kalebmarc.textfighter.battle.BattleManager;
import com.hotmail.kalebmarc.textfighter.battle.BattleRecord;
import com.hotmail.kalebmarc.textfighter.enemy.EnemyRegistry;
import com.hotmail.kalebmarc.textfighter.item.*;
import com.hotmail.kalebmarc.textfighter.player.*;
import com.hotmail.kalebmarc.textfighter.quest.CriticalQuest;
import com.hotmail.kalebmarc.textfighter.quest.GameEvent;
import com.hotmail.kalebmarc.textfighter.quest.KillQuest;
import com.hotmail.kalebmarc.textfighter.quest.QuestManager;
import com.hotmail.kalebmarc.textfighter.db.JpaManager;
import com.hotmail.kalebmarc.textfighter.db.SaveService;
import com.hotmail.kalebmarc.textfighter.util.AutoSaveTask;
import com.hotmail.kalebmarc.textfighter.util.GameLogger;

import time.GameClock;

import javax.swing.*;
import java.util.Scanner;

import static com.hotmail.kalebmarc.textfighter.player.Health.getStr;
import static com.hotmail.kalebmarc.textfighter.player.Health.upgrade;
import static com.hotmail.kalebmarc.textfighter.player.Settings.menu;
import static com.hotmail.kalebmarc.textfighter.player.Settings.setDif;
import static java.util.Arrays.asList;

/**
 * [개선 후] Game.java - 모든 시스템 연동
 *
 * 1. EnemyRegistry (Factory Pattern)
 *    기존: public static Enemy zombie; goblin; ... 하드코딩 10개 선언
 *    개선: EnemyRegistry.initDefault() 한 줄로 모든 적 등록
 *
 * 2. BattleManager (Strategy Pattern)
 *    기존: if (weapon.equals("Sniper")) { if (fightPath<=30) ... } else { ... }
 *    개선: updateBattleStrategy()로 무기별 전략 자동 교체
 *
 * 3. BattleRecord + BattleAnalyzer (Stream/Optional)
 *    기존: 전투 통계 없음
 *    개선: 게임 종료 시 칭호/총평 자동 분석 출력
 *
 * 4. QuestManager (Observer Pattern)
 *    기존: 퀘스트 시스템 없음
 *    개선: notify() 한 줄로 퀘스트 자동 처리
 *
 * 5. AutoSaveTask (멀티스레드)
 *    기존: 수동 저장만 존재 (case 10)
 *    개선: 별도 스레드에서 30초마다 자동저장
 *
 * 6. GameLogger (Singleton)
 *    기존: 로그 시스템 없음
 *    개선: 전투/저장/에러 로그 통합 관리
 */
public class Game {

	private final static Scanner SCAN = new Scanner(System.in);
	private static boolean gameStarted = false;

	// 기존 Enemy static 변수 유지 (Settings.java 호환성)
	// EnemyRegistry.initDefault()와 병행 사용
	public static Enemy darkElf;
	public static Enemy ninja;
	public static Enemy giantSpider;
	public static Enemy zombie;
	public static Enemy goblin;
	public static Enemy ghost;
	public static Enemy barbarian;
	public static Enemy giantAnt;
	public static Enemy evilUnicorn;
	public static Enemy ogre;

	public static boolean hadGameStarted() { return gameStarted; }

	// ── 기존 Enemy static 변수 제거 → EnemyRegistry로 대체 ──────────
	// 기존: public static Enemy zombie; goblin; ghost; ninja; ...
	// 개선: EnemyRegistry.initDefault() 로 한 번에 등록 (Factory Pattern)

	// Weapons (기존 유지)
	public static Weapon fists;
	public static Weapon baseballBat;
	public static Weapon knife;
	public static Weapon pipe;
	public static Weapon pistol;
	public static Weapon smg;
	public static Weapon shotgun;
	public static Weapon rifle;
	public static Weapon sniper;
	public static Weapon chainsaw;

	// Armours (기존 유지)
	public static Armour none     = new Armour("None",     0,   0,  1);
	public static Armour basic    = new Armour("Basic",    400, 15, 5);
	public static Armour advanced = new Armour("Advanced", 750, 30, 7);

	// Food (기존 유지)
	public static Food apple       = new Food("사과",       "평범한 사과.",                    StatusEffect.type.HEALTH, Food.type.FRUIT,      5);
	public static Food orange      = new Food("오렌지",     "사과 같지만 오렌지색.",            StatusEffect.type.HEALTH, Food.type.FRUIT,      5);
	public static Food dragonfruit = new Food("용과",       "아쉽게도 진짜 용은 아님.",         StatusEffect.type.HEALTH, Food.type.FRUIT,      10);
	public static Food meat        = new Food("고기 덩어리", "아마도 상하지 않았을 것.",         StatusEffect.type.HEALTH, Food.type.MEAT_OTHER, 15);
	public static Food mushroom    = new Food("버섯",       "좋은 종류의 버섯!",               StatusEffect.type.HEALTH, Food.type.OTHER,      5);
	public static Food fish        = new Food("물고기",     "강과 호수에서 잡은 신선한 생선.", StatusEffect.type.HEALTH, Food.type.MEAT_FISH,  15);

	// 신규 시스템
	private static BattleManager battleManager;
	private static BattleRecord  battleRecord;
	private static AutoSaveTask  autoSave;
	private static SaveService   saveService;
	private static final GameLogger    logger       = GameLogger.getInstance();
	private static final QuestManager  questManager = QuestManager.getInstance();

	public void start() {

		// Factory Pattern: 적 등록, settings.java에서 적 등록
		//EnemyRegistry.initDefault();

		// Strategy Pattern: 기본 전략(근접)으로 시작
		battleManager = new BattleManager(AttackStrategies.MELEE);
		battleRecord  = new BattleRecord(User.name());

		// 멀티스레드: 30초마다 JPA 자동저장
		saveService = new SaveService();
		autoSave = new AutoSaveTask(() -> {
			saveService.saveGame("AUTO");
			logger.save("JPA 자동저장 완료");
		}, 30);

		// Observer Pattern: 퀘스트 등록
		questManager.subscribe(new KillQuest("좀비 사냥꾼",  "Zombie", 3,  50));
		questManager.subscribe(new KillQuest("몬스터 헌터",   null,     5, 100));
		questManager.subscribe(new CriticalQuest("크리티컬 마스터", 2, 80));

		logger.info("게임 시작");

		GameUtils.showPopup(Constants.HEADER,
				Constants.SUB_HEADER,
				asList("저장 파일에서 게임을 불러오시겠습니까?"),
				asList("메인으로 돌아가기", "예", "아니오")
		);

		int choice = Ui.getValidInt();

		switch (choice) {
			case 1:
				autoSave.stop();
				return;
			case 2:
				if (Saves.load()) {
					gameStarted = true;
					GameClock.startTimeClock();
					autoSave.start();
					break;
				} else {
					autoSave.stop();
					return;
				}
			default:
				String difficultyLevel = getDifficulty();
				if (difficultyLevel.equals("Exit")) {
					autoSave.stop();
					return;
				} else {
					setDif(difficultyLevel, true, false);
					Health.set(100, 100);
					User.promptNameSelection();
					Enemy.encounterNew();
					Saves.save(true);
					gameStarted = true;
					GameClock.startTimeClock();
					autoSave.start();
					battleRecord = new BattleRecord(User.name());
					break;
				}
		}

		while (true) {

			if (Stats.kills > Stats.highScore) Stats.highScore = Stats.kills;
			Achievements.check();

			// Strategy Pattern: 무기 변경 시 전략 자동 교체
			updateBattleStrategy();

			Ui.cls();

			Ui.println("============================================================");
			Ui.println("  텍스트 파이터 " + Version.getFull());
			Ui.println("============================================================");
			if (Cheats.enabled()) Ui.println("  [!] 치트 활성화됨");
			Ui.print(Settings.godModeMsg());
			Ui.println("  [ 점수 ]");
			Ui.println("  레벨: " + Xp.getLevel() + "   " + Xp.getFull());
			Ui.println("  연속 처치: " + Stats.kills + "   최고 기록: " + Stats.highScore);
			Ui.println("------------------------------------------------------------");
			Ui.println("  [ 플레이어: " + User.name() + " ]");
			Ui.println("  체력:      " + getStr());
			Ui.println("  코인:      " + Coins.get());
			Ui.println("  응급 키트: " + FirstAid.get() + "개");
			Ui.println("  포션:  생존 " + Potion.get("survival") + "개 / 회복 " + Potion.get("recovery") + "개");
			Ui.println("  방어구:    " + Armour.getEquipped().toString());
			Ui.println("  무기:      " + Weapon.get().getName());
			GameClock.updateGameTime();
			Ui.println("------------------------------------------------------------");
			Ui.println("  [ 시간 ]");
			Ui.println("  날짜: " + GameClock.getGameDate() + "   시각: " + GameClock.getGameTime());
			Weapon.displayAmmo();
			Ui.println("------------------------------------------------------------");
			Ui.println("  [ 적 정보 ]");
			Ui.println("  이름:      " + Enemy.get().getName());
			Ui.println("  체력:      " + Enemy.get().getHeathStr());
			Ui.println("  응급 키트: " + Enemy.get().getFirstAidKit() + "개");
			Ui.println("============================================================");
			Ui.println("  1) 전투           2) 집으로        3) 마을로");
			Ui.println("  4) 응급 치료 키트  5) 포션 사용    6) 음식 먹기");
			Ui.println("  7) 인스타 힐      8) POWER        9) 도망치기");
			Ui.println(" 10) 게임 종료 (자동 저장)");
			Ui.println("============================================================");

			switch (Ui.getValidInt()) {

				case 1:
					// [핵심 변경] Strategy Pattern 적용
					// 기존: if (weapon.equals("Sniper")) { if (fightPath<=30) ... } else { ... }
					// 개선: battleManager가 무기에 맞는 전략으로 데미지 계산
					int damage = battleManager.attack(
							Weapon.get().getDamageMin(),
							Weapon.get().getDamageMax(),
							Weapon.get().getName()
					);

					if (damage > 0) {
						boolean killed = Enemy.get().takeDamage(damage);
						logger.battle(Weapon.get().getName() + " → " + damage + " 데미지");

						// 크리티컬 여부에 따라 ATTACK/CRITICAL 중 하나만 기록 (중복 집계 방지)
						boolean isCritical = damage >= Weapon.get().getDamageMax() * 1.8;
						if (isCritical) {
							battleRecord.record(BattleRecord.EventType.CRITICAL, damage, Weapon.get().getName());
							questManager.notify(GameEvent.CRITICAL_HIT, damage);
						} else {
							battleRecord.record(BattleRecord.EventType.ATTACK, damage, Weapon.get().getName());
						}

						Ui.println("------------------------------------------------------------");
						Ui.println("  " + Enemy.get().getName() + "을(를) 공격했습니다!");
						Ui.println("  " + Weapon.get().getName() + "으로 " + damage + " 데미지!");
						Ui.println("------------------------------------------------------------");
						Ui.println("  내 체력: " + getStr());
						Ui.println("  적 체력: " + Enemy.get().getHeathStr());
						Ui.println("------------------------------------------------------------");

						if (killed) {
							// Observer Pattern: 처치 이벤트 발행 → 퀘스트 자동 처리
							questManager.notify(GameEvent.ENEMY_KILLED, Enemy.get().getName());
							battleRecord.record(BattleRecord.EventType.KILL, 1, Enemy.get().getName());
							logger.battle(Enemy.get().getName() + " 처치!");
						}
						else {
							// 적이 살아있으면 반격
							Enemy.get().dealDamage();
						}

					} else {
						// 빗나감 - 적이 반격
						battleRecord.record(BattleRecord.EventType.MISS, 0, Weapon.get().getName());
						int enemyDmg = Enemy.get().getDamageMin()
								+ new java.util.Random().nextInt(
								Math.max(1, Enemy.get().getDamageMax() - Enemy.get().getDamageMin())
						);
						Health.takeDamage(enemyDmg);
						battleRecord.record(BattleRecord.EventType.HIT, enemyDmg, Enemy.get().getName());
						logger.battle("빗나감! " + Enemy.get().getName() + " 반격 → " + enemyDmg + " 피해");
						Ui.println("------------------------------------------------------------");
						Ui.println("  공격이 빗나갔습니다!");
						Ui.println("  " + Enemy.get().getName() + "의 반격 → " + enemyDmg + " 피해!");
						Ui.println("------------------------------------------------------------");
						Ui.println("  내 체력: " + getStr());
						Ui.println("  적 체력: " + Enemy.get().getHeathStr());
						Ui.println("------------------------------------------------------------");
					}
					Ui.pause();
					break;

				case 2:
					home();
					break;

				case 3:
					town();
					break;

				case 4:
					FirstAid.use();
					battleRecord.record(BattleRecord.EventType.POTION_USED, 20, "응급 치료 키트");
					break;

				case 5:
					Ui.cls();
					Ui.println("어떤 포션을 사용하겠습니까?");
					Ui.println("1) 생존 포션");
					Ui.println("2) 회복 포션");
					Ui.println("3) 뒤로");
					switch (Ui.getValidInt()) {
						case 1:
							Potion.use("survival");
							battleRecord.record(BattleRecord.EventType.POTION_USED, 0, "생존 포션");
							break;
						case 2:
							Potion.use("recovery");
							battleRecord.record(BattleRecord.EventType.POTION_USED, 0, "회복 포션");
							break;
					}
					break;

				case 6:
					Food.list();
					break;

				case 7:
					InstaHealth.use();
					break;

				case 8:
					Power.use();
					break;

				case 9:
					// Observer Pattern: 도망 이벤트 발행
					questManager.notify(GameEvent.RAN_AWAY, null);
					battleRecord.record(BattleRecord.EventType.RAN_AWAY, 0, "도망");
					logger.info("전투 도망");
					Ui.cls();
					Ui.popup("전투에서 도망쳤습니다.", "도망", JOptionPane.INFORMATION_MESSAGE);
					Enemy.encounterNew();
					break;

				case 10:
					Stats.timesQuit++;
					autoSave.stop();
					saveService.saveRanking();
					JpaManager.getInstance().close();
					printBattleReport();
					questManager.printStatus();
					logger.info("게임 종료");
					Ui.pause();
					return;

				case 0:
					Cheats.cheatGateway();
					break;

				case 99:
					Debug.menu();
					break;

				default:
					break;

			} // switch
		} // while
	}

	/**
	 * 무기에 따라 BattleManager 전략 교체 (Strategy Pattern 핵심)
	 *
	 * 기존: case 1 안에 if/else 하드코딩
	 * 개선: 무기 이름 → 전략 객체 동적 교체
	 */
	private void updateBattleStrategy() {
		AttackStrategy strategy;
		switch (Weapon.get().getName()) {
			case "Sniper":   strategy = AttackStrategies.SNIPER;   break;
			case "Shotgun":  strategy = AttackStrategies.SHOTGUN;  break;
			default:         strategy = AttackStrategies.MELEE;    break;
		}
		battleManager.setStrategy(strategy);
	}

	/**
	 * 게임 종료 시 BattleAnalyzer로 전투 분석 리포트 출력 (Stream/Optional 활용)
	 */
	private void printBattleReport() {
		if (battleRecord.getTotalTurns() > 0) {
			BattleAnalyzer analyzer = new BattleAnalyzer();
			analyzer.printReport(analyzer.analyze(battleRecord));
		}
	}

	private static void town() {
		while (true) {
			Ui.cls();
			Ui.println("============================================================");
			Ui.println("  마을에 오신 것을 환영합니다!");
			Ui.println("============================================================");
			Ui.println("  [ 점수 ]");
			Ui.println("  연속 처치: " + Stats.kills + "   최고 기록: " + Stats.highScore);
			Ui.println("------------------------------------------------------------");
			Ui.println("  [ 플레이어 정보 ]");
			Ui.println("  체력:      " + getStr());
			Ui.println("  코인:      " + Coins.get());
			Ui.println("  응급 키트: " + FirstAid.get() + "개");
			Ui.println("  포션:  생존 " + Potion.get("survival") + "개 / 회복 " + Potion.get("recovery") + "개");
			Ui.println("  무기:      " + Weapon.get().getName());
			Ui.println("============================================================");
			Ui.println("  1) 카지노          2) 집");
			Ui.println("  3) 은행            4) 상점");
			Ui.println("  5) 체력 업그레이드  6) 뒤로");
			Ui.println("============================================================");

			switch (Ui.getValidInt()) {
				case 1: Casino.menu(); break;
				case 2: home();        break;
				case 3: Bank.menu();   break;
				case 4: Shop.menu();   break;
				case 5: upgrade();     break;
				case 6: return;
				default: break;
			}
		}
	}

	private static void home() {
		while (true) {
			Ui.cls();
			Ui.println("============================================================");
			Ui.println("  집에 오신 것을 환영합니다!");
			Ui.println("============================================================");
			Ui.println("  [ 점수 ]");
			Ui.println("  연속 처치: " + Stats.kills + "   최고 기록: " + Stats.highScore);
			Ui.println("------------------------------------------------------------");
			Ui.println("  [ 플레이어 정보 ]");
			Ui.println("  체력:      " + getStr());
			Ui.println("  코인:      " + Coins.get());
			Ui.println("  응급 키트: " + FirstAid.get() + "개");
			Ui.println("  포션:  생존 " + Potion.get("survival") + "개 / 회복 " + Potion.get("recovery") + "개");
			Ui.println("  무기:      " + Weapon.get().getName());
			Ui.println("============================================================");
			Ui.println("  1) 무기 장착         2) 방어구 장착");
			Ui.println("  3) 아이템 상자 보기   4) 업적");
			Ui.println("  5) 통계              6) 정보");
			Ui.println("  7) 설정              8) 도움말");
			Ui.println("  9) 크레딧           10) 뒤로");
			Ui.println("============================================================");

			switch (Ui.getValidInt()) {
				case 1:  Weapon.choose();                                   break;
				case 2:  Armour.choose();                                   break;
				case 3:  Chest.view();                                      break;
				case 4:  Achievements.view();                               break;
				case 5:  Stats.view();                                      break;
				case 6:  About.view(true); Achievements.viewedAbout = true; break;
				case 7:  menu();                                            break;
				case 8:  Help.view();                                       break;
				case 9:  Credits.view();                                    break;
				case 10: return;
				default: break;
			}
		}
	}

	private static String getDifficulty() {
		GameUtils.showPopup(Constants.HEADER,
				Constants.SUB_HEADER,
				asList("난이도를 선택하세요"),
				asList("나가기", "쉬움", "어려움")
		);
		int c = Ui.getValidInt();
		Ui.cls();
		if (c == 2) return "Easy";
		if (c == 3) return "Hard";
		return "Exit";
	}
}