package com.hotmail.kalebmarc.textfighter.main;

import com.hotmail.kalebmarc.textfighter.item.Armour;
import com.hotmail.kalebmarc.textfighter.item.FirstAid;
import com.hotmail.kalebmarc.textfighter.item.InstaHealth;
import com.hotmail.kalebmarc.textfighter.player.*;
import com.hotmail.kalebmarc.textfighter.quest.KillQuest;
import com.hotmail.kalebmarc.textfighter.quest.Quest;
import com.hotmail.kalebmarc.textfighter.quest.QuestManager;
import time.GameClock;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class GameFXWindow {

    static final BlockingQueue<String> INPUT_QUEUE = new LinkedBlockingQueue<>();

    // ── 상단 HP바 / 스탯 ────────────────────────────────────────────
    private ProgressBar playerHpBar, enemyHpBar;
    private Label playerNameLbl, playerHpLbl;
    private Label enemyNameLbl, enemyHpLbl;
    private Label coinsLbl, levelLbl, weaponLbl, armourLbl, timeLbl, killsLbl;

    // ── 중앙 좌: 전투 로그 ──────────────────────────────────────────
    private TextArea battleLog;

    // ── 중앙 우: 퀘스트 / 인벤토리 ─────────────────────────────────
    private VBox questContent;
    private VBox inventoryContent;

    private Stage stage;

    // 메인 게임 메뉴 버튼 라벨
    private static final String[] BTN_LABELS = {
        "[1]  전투",      "[2]  집으로",   "[3]  마을로",
        "[4]  응급키트",  "[5]  포션",
        "[6]  음식",      "[7]  인스타힐", "[8]  POWER",
        "[9]  도망",      "[0]  종료"
    };

    // ── 생성자 (FX Application Thread에서 호출) ─────────────────────

    public GameFXWindow() {
        stage = new Stage();
        stage.setTitle("텍스트 파이터");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle(BG("0D0D1A") + font("맑은 고딕"));
        root.setTop(buildTop());
        root.setCenter(buildCenter());
        root.setBottom(buildBottom());

        Scene scene = new Scene(root, 1200, 780);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DIGIT1: case NUMPAD1: INPUT_QUEUE.offer("1");  break;
                case DIGIT2: case NUMPAD2: INPUT_QUEUE.offer("2");  break;
                case DIGIT3: case NUMPAD3: INPUT_QUEUE.offer("3");  break;
                case DIGIT4: case NUMPAD4: INPUT_QUEUE.offer("4");  break;
                case DIGIT5: case NUMPAD5: INPUT_QUEUE.offer("5");  break;
                case DIGIT6: case NUMPAD6: INPUT_QUEUE.offer("6");  break;
                case DIGIT7: case NUMPAD7: INPUT_QUEUE.offer("7");  break;
                case DIGIT8: case NUMPAD8: INPUT_QUEUE.offer("8");  break;
                case DIGIT9: case NUMPAD9: INPUT_QUEUE.offer("9");  break;
                case DIGIT0: case NUMPAD0: INPUT_QUEUE.offer("10"); break;
                default: break;
            }
        });

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(400), e -> refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // ── 상단 패널: 3열 (플레이어 HP / 적 HP / 스탯) ─────────────────

    private HBox buildTop() {
        // 플레이어 HP
        playerNameLbl = lbl("플레이어", GOLD, 13, true);
        playerHpBar   = hpBar(GREEN);
        playerHpLbl   = lbl("HP: -/-", TEXT, 12, false);
        VBox playerPane = card(playerNameLbl, playerHpBar, playerHpLbl);

        // 적 HP
        enemyNameLbl = lbl("적", RED, 13, true);
        enemyHpBar   = hpBar(RED);
        enemyHpLbl   = lbl("HP: -/-", TEXT, 12, false);
        VBox enemyPane = card(enemyNameLbl, enemyHpBar, enemyHpLbl);

        // 스탯
        coinsLbl  = lbl("💰  코인:    -",    TEXT,    12, false);
        levelLbl  = lbl("⭐  레벨:    -",    TEXT,    12, false);
        weaponLbl = lbl("🗡  무기:    -",    TEXT,    12, false);
        armourLbl = lbl("🛡  방어구:  -",    TEXT,    12, false);
        timeLbl   = lbl("🕐  시간:    -",    SUBTEXT, 11, false);
        killsLbl  = lbl("💀  처치:    -",    SUBTEXT, 11, false);
        VBox statsPane = card(coinsLbl, levelLbl, weaponLbl, armourLbl, timeLbl, killsLbl);

        HBox.setHgrow(playerPane, Priority.ALWAYS);
        HBox.setHgrow(enemyPane,  Priority.ALWAYS);
        HBox.setHgrow(statsPane,  Priority.ALWAYS);

        HBox top = new HBox(8, playerPane, enemyPane, statsPane);
        top.setPadding(new Insets(8, 8, 4, 8));
        top.setStyle(BG("0D0D1A"));
        return top;
    }

    // ── 중앙 패널: 좌(전투 로그) + 우(퀘스트/인벤토리) ─────────────

    private SplitPane buildCenter() {
        // 좌: 전투 로그
        Label logTitle = lbl("⚔  전투 로그", GOLD, 13, true);
        battleLog = new TextArea();
        battleLog.setEditable(false);
        battleLog.setWrapText(true);
        battleLog.setStyle(
            BG("080810") +
            "-fx-control-inner-background: #080810;" +
            "-fx-text-fill: #DCDCE8;" +
            "-fx-font-family: 'Consolas', '맑은 고딕', monospace;" +
            "-fx-font-size: 13px;"
        );
        VBox.setVgrow(battleLog, Priority.ALWAYS);
        VBox logPane = new VBox(6, logTitle, battleLog);
        logPane.setPadding(new Insets(8));
        logPane.setStyle(BG("0D0D1A"));

        // 우: 퀘스트
        Label questTitle = lbl("📋  퀘스트", GOLD, 13, true);
        questContent = new VBox(5);
        questContent.setPadding(new Insets(4));
        ScrollPane questScroll = new ScrollPane(questContent);
        questScroll.setFitToWidth(true);
        questScroll.setMaxHeight(230);
        questScroll.setStyle(BG("14142A") + "-fx-background: #14142A;");
        VBox questPane = new VBox(4, questTitle, questScroll);
        questPane.setPadding(new Insets(8));
        questPane.setStyle(BG("14142A") + border("2A2A4A") + radius(6));

        // 우: 인벤토리
        Label invTitle = lbl("🎒  인벤토리", GOLD, 13, true);
        inventoryContent = new VBox(5);
        inventoryContent.setPadding(new Insets(4));
        VBox invPane = new VBox(4, invTitle, inventoryContent);
        invPane.setPadding(new Insets(8));
        invPane.setStyle(BG("14142A") + border("2A2A4A") + radius(6));

        VBox rightPane = new VBox(8, questPane, invPane);
        rightPane.setPadding(new Insets(8));
        rightPane.setStyle(BG("0D0D1A"));
        rightPane.setMinWidth(240);

        SplitPane split = new SplitPane(logPane, rightPane);
        split.setDividerPositions(0.66);
        split.setStyle(BG("0D0D1A"));
        return split;
    }

    // ── 하단 패널: 버튼 10개 + 입력 필드 ────────────────────────────

    private VBox buildBottom() {
        HBox row1 = btnRow(), row2 = btnRow();

        for (int i = 0; i < 10; i++) {
            final String val = (i < 9) ? String.valueOf(i + 1) : "10";
            Button btn = actionBtn(BTN_LABELS[i]);
            btn.setOnAction(e -> INPUT_QUEUE.offer(val));
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
            (i < 5 ? row1 : row2).getChildren().add(btn);
        }

        // 텍스트 입력 (서브 메뉴용)
        Label prompt = lbl("입력:", SUBTEXT, 13, false);
        TextField field = new TextField();
        field.setStyle(
            BG("14142A") + "-fx-text-fill: #DCDCE8;" +
            "-fx-border-color: #2A2A4A; -fx-font-size: 13px;" +
            "-fx-prompt-text-fill: #505070;"
        );
        field.setPromptText("숫자 또는 텍스트 입력...");
        HBox.setHgrow(field, Priority.ALWAYS);
        Button ok = actionBtn("확인");
        Runnable submit = () -> {
            INPUT_QUEUE.offer(field.getText());
            field.setText("");
        };
        field.setOnAction(e -> submit.run());
        ok.setOnAction(e -> submit.run());

        HBox inputRow = new HBox(6, prompt, field, ok);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setPadding(new Insets(4, 8, 4, 8));

        VBox bottom = new VBox(6, row1, row2, inputRow);
        bottom.setPadding(new Insets(6, 8, 8, 8));
        bottom.setStyle(BG("111120") + "-fx-border-color: #2A2A4A; -fx-border-width: 1 0 0 0;");
        return bottom;
    }

    // ── 주기적 상태 갱신 (400ms) ─────────────────────────────────────

    private void refresh() {
        refreshTop();
        refreshQuests();
        refreshInventory();
    }

    private void refreshTop() {
        try {
            int hp = Health.get(), max = Health.getOutOf();
            playerNameLbl.setText("플레이어: " + User.name());
            playerHpLbl.setText("HP:  " + hp + " / " + max);
            playerHpBar.setProgress(max > 0 ? (double) hp / max : 1.0);
            double r = max > 0 ? (double) hp / max : 1.0;
            playerHpBar.setStyle("-fx-accent: " + (r > 0.6 ? GREEN : r > 0.3 ? YELLOW : RED) + "; -fx-background-color: #1A1A30;");
        } catch (Exception ignored) {}

        try {
            Enemy e = Enemy.get();
            if (e != null) {
                int eHp = e.getHealth(), eMax = e.getHealthMax();
                enemyNameLbl.setText("적:  " + e.getName());
                enemyHpLbl.setText("HP:  " + Math.max(0, eHp) + " / " + eMax);
                enemyHpBar.setProgress(eMax > 0 ? Math.max(0, (double) eHp / eMax) : 0);
            }
        } catch (Exception ignored) {}

        try {
            coinsLbl .setText("💰  코인:    " + Coins.get());
            levelLbl .setText("⭐  레벨:    " + Xp.getLevel() + "  (" + Xp.getFull() + ")");
            weaponLbl.setText("🗡  무기:    " + Weapon.get().getName());
            armourLbl.setText("🛡  방어구:  " + Armour.getEquipped().toString());
            timeLbl  .setText("🕐  " + GameClock.getGameDate() + "  " + GameClock.getGameTime());
            killsLbl .setText("💀  처치:    " + Stats.kills + "  (최고: " + Stats.highScore + ")");
        } catch (Exception ignored) {}
    }

    private void refreshQuests() {
        questContent.getChildren().clear();
        try {
            QuestManager qm = QuestManager.getInstance();

            for (Object obs : qm.getActiveQuests()) {
                if (!(obs instanceof Quest)) continue;
                Quest q = (Quest) obs;
                VBox card = new VBox(3);
                card.setStyle(BG("1A1A30") + "-fx-padding: 6; -fx-background-radius: 4;");
                card.getChildren().add(lbl("⏳  " + q.getTitle(), "#D0C040", 12, true));
                card.getChildren().add(lbl(q.getDescription(), SUBTEXT, 11, false));

                if (obs instanceof KillQuest) {
                    KillQuest kq = (KillQuest) obs;
                    int cur = kq.getCurrentKills(), req = kq.getRequiredKills();
                    ProgressBar pb = new ProgressBar((double) cur / req);
                    pb.setMaxWidth(Double.MAX_VALUE);
                    pb.setPrefHeight(8);
                    pb.setStyle("-fx-accent: #4080D0; -fx-background-color: #0A0A20;");
                    card.getChildren().addAll(pb, lbl(cur + " / " + req, "#7090C0", 11, false));
                }
                questContent.getChildren().add(card);
            }

            for (Object obs : qm.getCompletedQuests()) {
                if (!(obs instanceof Quest)) continue;
                questContent.getChildren().add(lbl("✅  " + ((Quest) obs).getTitle(), "#40C040", 12, false));
            }

            if (questContent.getChildren().isEmpty())
                questContent.getChildren().add(lbl("(진행 중인 퀘스트 없음)", "#505070", 12, false));

        } catch (Exception ignored) {}
    }

    private void refreshInventory() {
        inventoryContent.getChildren().clear();
        try { invRow("🩺  응급 키트",  FirstAid.get());         } catch (Exception ignored) {}
        try { invRow("🧪  생존 포션",  Potion.get("survival")); } catch (Exception ignored) {}
        try { invRow("💊  회복 포션",  Potion.get("recovery")); } catch (Exception ignored) {}
        try { invRow("⚡  인스타 힐",  InstaHealth.get());      } catch (Exception ignored) {}
    }

    private void invRow(String name, int qty) {
        String col = qty > 0 ? TEXT : "#404060";
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label n = lbl(name, col, 12, false);
        Label q = lbl("×  " + qty, qty > 0 ? "#A0D0A0" : "#404060", 12, true);
        HBox.setHgrow(n, Priority.ALWAYS);
        row.getChildren().addAll(n, q);
        inventoryContent.getChildren().add(row);
    }

    // ── 공개 출력 API (Ui.java에서 호출) ────────────────────────────

    public void appendLog(String text) {
        Platform.runLater(() -> {
            battleLog.appendText(text + "\n");
            battleLog.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void appendRaw(String text) {
        Platform.runLater(() -> {
            battleLog.appendText(text);
            battleLog.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void clearLog() {
        Platform.runLater(() -> battleLog.clear());
    }

    public void showAlert(String body, String title, Alert.AlertType type) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.initOwner(stage);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(body);
            alert.showAndWait();
            latch.countDown();
        });
        try { latch.await(); } catch (InterruptedException ignored) {}
    }

    public int showConfirm(String body, String title) {
        int[] result = {1};
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(stage);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(body);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> r = alert.showAndWait();
            result[0] = (r.isPresent() && r.get() == ButtonType.YES) ? 0 : 1;
            latch.countDown();
        });
        try { latch.await(); } catch (InterruptedException ignored) {}
        return result[0];
    }

    // ── UI 빌더 헬퍼 ─────────────────────────────────────────────────

    private Label lbl(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;" + (bold ? "-fx-font-weight: bold;" : ""));
        return l;
    }

    private VBox card(javafx.scene.Node... nodes) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8, 10, 8, 10));
        box.setStyle(BG("14142A") + border("2A2A4A") + radius(6));
        box.getChildren().addAll(nodes);
        return box;
    }

    private ProgressBar hpBar(String color) {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(14);
        bar.setStyle("-fx-accent: " + color + "; -fx-background-color: #1A1A30;");
        return bar;
    }

    private Button actionBtn(String text) {
        Button btn = new Button(text);
        String base  = BG("1E2A5A") + "-fx-text-fill: #C8C8E8; -fx-font-size: 12px; -fx-cursor: hand;" + radius(4) + font("맑은 고딕");
        String hover = BG("2A3A7A") + "-fx-text-fill: #FFFFFF;  -fx-font-size: 12px; -fx-cursor: hand;" + radius(4) + font("맑은 고딕");
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited (e -> btn.setStyle(base));
        btn.setPadding(new Insets(8, 6, 8, 6));
        return btn;
    }

    private HBox btnRow() {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // ── CSS 헬퍼 ─────────────────────────────────────────────────────
    private static final String TEXT    = "#DCDCE8";
    private static final String SUBTEXT = "#9090B8";
    private static final String GOLD    = "#C8B040";
    private static final String GREEN   = "#40D040";
    private static final String YELLOW  = "#D0C040";
    private static final String RED     = "#D04040";

    private static String BG(String hex)     { return "-fx-background-color: #" + hex + ";"; }
    private static String border(String hex) { return "-fx-border-color: #" + hex + ";"; }
    private static String radius(int r)      { return "-fx-background-radius: " + r + "; -fx-border-radius: " + r + ";"; }
    private static String font(String name)  { return "-fx-font-family: '" + name + "';"; }
}
