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

public class MultiplayerLobbyController {

    private final Stage owner;

    public MultiplayerLobbyController(Stage owner) {
        this.owner = owner;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("⚔ 멀티플레이 로비");

        Label title    = lbl("⚔  멀티플레이 전투", "#C8B040", 18, true);
        Label statusLbl = lbl("상태: 대기 중", "#9090B8", 12, false);

        Button hostBtn  = actionBtn("🏠 방 만들기  (호스트)");
        Button guestBtn = actionBtn("🔗 방 참가  (게스트)");
        Button backBtn  = actionBtn("← 뒤로");

        TextField ipField = new TextField("localhost");
        ipField.setPromptText("호스트 IP 입력...");
        ipField.setStyle(
            "-fx-background-color: #14142A; -fx-text-fill: #DCDCE8;" +
            "-fx-border-color: #2A2A4A; -fx-font-size: 13px; -fx-pref-width: 180px;"
        );

        hostBtn.setOnAction(e -> {
            statusLbl.setText("상태: 포트 9999 대기 중...");
            hostBtn.setDisable(true);
            guestBtn.setDisable(true);
            new Thread(() -> {
                try {
                    MultiplayerBattle battle = new MultiplayerBattle(true);
                    battle.startAsHost();
                    Platform.runLater(() -> {
                        stage.close();
                        new MultiplayerBattleController(owner, battle).show();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLbl.setText("오류: " + ex.getMessage());
                        hostBtn.setDisable(false);
                        guestBtn.setDisable(false);
                    });
                }
            }).start();
        });

        guestBtn.setOnAction(e -> {
            String host = ipField.getText().trim();
            statusLbl.setText("상태: " + host + " 연결 중...");
            hostBtn.setDisable(true);
            guestBtn.setDisable(true);
            new Thread(() -> {
                try {
                    MultiplayerBattle battle = new MultiplayerBattle(false);
                    battle.startAsClient(host);
                    Platform.runLater(() -> {
                        stage.close();
                        new MultiplayerBattleController(owner, battle).show();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLbl.setText("오류: " + ex.getMessage());
                        hostBtn.setDisable(false);
                        guestBtn.setDisable(false);
                    });
                }
            }).start();
        });

        backBtn.setOnAction(e -> stage.close());

        Label ipLbl = lbl("IP:", "#9090B8", 12, false);
        HBox ipRow  = new HBox(8, ipLbl, ipField);
        ipRow.setAlignment(Pos.CENTER);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2A2A4A;");

        VBox box = new VBox(14, title, hostBtn, sep, ipRow, guestBtn, statusLbl, backBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: #0D0D1A;");

        hostBtn .setMaxWidth(240);
        guestBtn.setMaxWidth(240);
        backBtn .setMaxWidth(240);

        stage.setScene(new Scene(box, 370, 390));
        stage.show();
    }

    // ── UI 헬퍼 ──────────────────────────────────────────────────────

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
        btn.setPadding(new Insets(10, 20, 10, 20));
        return btn;
    }
}
