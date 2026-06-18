package com.hotmail.kalebmarc.textfighter.main;

import com.hotmail.kalebmarc.textfighter.multiplayer.MultiplayerBattle;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MultiplayerBattleController {

    private final Stage owner;
    private final MultiplayerBattle battle;

    private ProgressBar myHpBar, enemyHpBar;
    private Label       myHpLbl, enemyHpLbl;
    private TextArea    log;
    private Button      attackBtn, defendBtn;
    private Stage       stage;

    public MultiplayerBattleController(Stage owner, MultiplayerBattle battle) {
        this.owner  = owner;
        this.battle = battle;
    }

    public void show() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("⚔ 멀티 전투");
        stage.setOnCloseRequest(e -> battle.close());

        // ── HP 패널 ───────────────────────────────────────────────────
        myHpBar    = hpBar("#40D040");
        enemyHpBar = hpBar("#D04040");
        myHpLbl    = lbl("내 HP: 100 / 100",   "#DCDCE8", 13, true);
        enemyHpLbl = lbl("상대 HP: 100 / 100", "#DCDCE8", 13, true);

        VBox myCard    = hpCard("내가",   myHpBar,    myHpLbl,    "#1A3A1A");
        VBox enemyCard = hpCard("상대방", enemyHpBar, enemyHpLbl, "#3A1A1A");
        HBox.setHgrow(myCard,    Priority.ALWAYS);
        HBox.setHgrow(enemyCard, Priority.ALWAYS);

        HBox hpRow = new HBox(12, myCard, enemyCard);
        hpRow.setPadding(new Insets(12));

        // ── 전투 로그 ─────────────────────────────────────────────────
        log = new TextArea();
        log.setEditable(false);
        log.setWrapText(true);
        log.setStyle(
            "-fx-control-inner-background: #080810; -fx-text-fill: #DCDCE8;" +
            "-fx-font-family: 'Consolas', '맑은 고딕', monospace; -fx-font-size: 12px;"
        );
        VBox.setVgrow(log, Priority.ALWAYS);

        // ── 버튼 ──────────────────────────────────────────────────────
        attackBtn = actionBtn("⚔  공격");
        defendBtn = actionBtn("🛡  방어");
        attackBtn.setOnAction(e -> doAction(true));
        defendBtn.setOnAction(e -> doAction(false));
        HBox btnRow = new HBox(12, attackBtn, defendBtn);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(8));

        VBox root = new VBox(hpRow, log, btnRow);
        root.setStyle("-fx-background-color: #0D0D1A;");

        stage.setScene(new Scene(root, 480, 520));
        stage.show();

        appendLog("전투 시작! " + (battle.isMyTurn() ? "내 턴입니다." : "상대 턴입니다."));
        updateButtons();

        // 게스트(선공 아님)는 첫 상대 행동을 먼저 수신
        if (!battle.isMyTurn()) {
            receiveOpponentAction();
        }
    }

    // ── 내 행동 처리 ──────────────────────────────────────────────────

    /**
     * 공격/방어 버튼 클릭 시 호출.
     * 소켓 I/O는 별도 스레드에서 실행하고, UI 갱신은 Platform.runLater()로 처리.
     *
     * 공격 후 수신 흐름:
     *   1) HP:n (상대 ack) → enemyHp 갱신, myTurn 여전히 false
     *   2) ATTACK:m 또는 DEFEND:m (상대 행동) → 처리 후 myTurn=true
     *
     * 방어 후 수신 흐름:
     *   1) ATTACK:m 또는 DEFEND:m (상대 행동) 직접 수신 → myTurn=true
     */
    private void doAction(boolean isAttack) {
        attackBtn.setDisable(true);
        defendBtn.setDisable(true);

        new Thread(() -> {
            try {
                if (isAttack) {
                    battle.attack();
                    appendLog("⚔ 공격! (데미지 전송)");
                } else {
                    battle.defend();
                    appendLog("🛡 방어! (체력 회복)");
                }

                // 첫 번째 수신: 공격 시 HP ack, 방어 시 상대 행동
                if (!isAttack) appendLog("[대기] 상대 행동 대기 중...");
                String r1 = battle.processTurn();
                Platform.runLater(this::updateHpBars);
                if (r1.startsWith("RESULT")) { handleResult(r1); return; }

                // 공격 후 HP ack 받았으면(myTurn=false) 상대 행동 추가 수신
                if (!battle.isMyTurn()) {
                    appendLog("[대기] 상대 행동 대기 중...");
                    String r2 = battle.processTurn();
                    Platform.runLater(this::updateHpBars);
                    if (r2.startsWith("RESULT")) { handleResult(r2); return; }
                }

                appendLog("내 HP: " + battle.getMyHp() + "  |  상대 HP: " + battle.getEnemyHp());
                Platform.runLater(this::updateButtons);

            } catch (Exception ex) {
                appendLog("[오류] " + ex.getMessage());
            }
        }).start();
    }

    // ── 상대 행동 대기 (게스트 첫 수신 / 재귀 없이 1회용) ─────────────

    private void receiveOpponentAction() {
        new Thread(() -> {
            try {
                appendLog("[대기] 상대 행동 대기 중...");
                String r = battle.processTurn();
                Platform.runLater(this::updateHpBars);
                if (r.startsWith("RESULT")) { handleResult(r); return; }

                appendLog("상대 행동 완료 — 내 HP: " + battle.getMyHp());
                Platform.runLater(this::updateButtons);
            } catch (Exception ex) {
                appendLog("[오류] " + ex.getMessage());
            }
        }).start();
    }

    // ── 전투 종료 처리 ────────────────────────────────────────────────

    private void handleResult(String response) {
        boolean win = response.contains("WIN");
        appendLog(win ? "\n★  승리! ★" : "\n✗  패배...");
        Platform.runLater(() -> {
            Alert alert = new Alert(win ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.initOwner(stage);
            alert.setTitle(win ? "승리!" : "패배");
            alert.setHeaderText(null);
            alert.setContentText(win
                ? "상대를 쓰러뜨렸습니다! 승리!"
                : "쓰러졌습니다... 다음 기회에!");
            alert.showAndWait();
            battle.close();
            stage.close();
        });
    }

    // ── UI 업데이트 (FX 스레드에서 호출) ─────────────────────────────

    private void updateHpBars() {
        int myHp    = battle.getMyHp();
        int enemyHp = battle.getEnemyHp();
        myHpBar   .setProgress(Math.max(0, myHp    / 100.0));
        enemyHpBar.setProgress(Math.max(0, enemyHp / 100.0));
        myHpLbl   .setText("내 HP: "   + Math.max(0, myHp)    + " / 100");
        enemyHpLbl.setText("상대 HP: " + Math.max(0, enemyHp) + " / 100");
    }

    private void updateButtons() {
        boolean myTurn = battle.isMyTurn();
        attackBtn.setDisable(!myTurn);
        defendBtn.setDisable(!myTurn);
    }

    private void appendLog(String text) {
        Platform.runLater(() -> {
            log.appendText(text + "\n");
            log.setScrollTop(Double.MAX_VALUE);
        });
    }

    // ── UI 빌더 헬퍼 ─────────────────────────────────────────────────

    private VBox hpCard(String name, ProgressBar bar, Label hpLbl, String bgColor) {
        Label title = lbl(name, "#C8B040", 13, true);
        VBox box = new VBox(4, title, bar, hpLbl);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: " + bgColor
                     + "; -fx-background-radius: 6;"
                     + " -fx-border-color: #2A2A4A; -fx-border-radius: 6;");
        return box;
    }

    private ProgressBar hpBar(String color) {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(14);
        bar.setStyle("-fx-accent: " + color + "; -fx-background-color: #1A1A30;");
        return bar;
    }

    private Label lbl(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;"
                   + (bold ? "-fx-font-weight: bold;" : ""));
        return l;
    }

    private Button actionBtn(String text) {
        Button btn = new Button(text);
        String base  = "-fx-background-color: #1E2A5A; -fx-text-fill: #C8C8E8;" +
                       "-fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 4;";
        String hover = "-fx-background-color: #2A3A7A; -fx-text-fill: #FFFFFF;" +
                       "-fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 4;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited (e -> btn.setStyle(base));
        btn.setPadding(new Insets(10, 28, 10, 28));
        return btn;
    }
}
