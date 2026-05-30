package com.hotmail.kalebmarc.textfighter.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX 진입점.
 * Ui.getWindow()에서 별도 스레드로 Application.launch()를 호출해 시작함.
 * start()에서 GameFXWindow를 생성하고 Ui.WINDOW_READY를 카운트다운해 메인 스레드를 깨움.
 */
public class GameFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        primaryStage.hide(); // Application이 강제로 주는 primaryStage는 사용 안 함
        Ui.window = new GameFXWindow();
        Ui.WINDOW_READY.countDown();
    }

    @Override
    public void stop() {
        // Platform.exit() 호출 시 JVM도 종료
        System.exit(0);
    }
}
