package com.hotmail.kalebmarc.textfighter.main;

import javafx.application.Application;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ui {
    public static boolean guiEnabled = true;

    // GameFXApp.start()에서 직접 세팅 (같은 패키지)
    static volatile GameFXWindow window = null;
    static final CountDownLatch  WINDOW_READY = new CountDownLatch(1);

    private static final AtomicBoolean FX_LAUNCHED = new AtomicBoolean(false);
    private static final Scanner       IN          = new Scanner(System.in);

    private Ui() {}

    // ── FX 창 초기화 (첫 출력 시 자동 실행) ─────────────────────────

    private static GameFXWindow getWindow() {
        if (window == null && guiEnabled) {
            if (FX_LAUNCHED.compareAndSet(false, true)) {
                // 별도 스레드에서 Application.launch() — 표준 JavaFX 진입점
                Thread launcher = new Thread(() -> {
                    try {
                        Application.launch(GameFXApp.class);
                    } catch (Exception e) {
                        // 실패 시 터미널 모드로 폴백
                        guiEnabled = false;
                        WINDOW_READY.countDown();
                    }
                }, "javafx-launcher");
                launcher.setDaemon(false);
                launcher.start();
            }
            // 창이 준비될 때까지 대기
            try { WINDOW_READY.await(); } catch (InterruptedException ignored) {}
            if (window == null) guiEnabled = false;
        }
        return window;
    }

    // ── 출력 ─────────────────────────────────────────────────────────

    public static <T> void print(T input) {
        String text = String.valueOf(input);
        if (guiEnabled) {
            GameFXWindow w = getWindow();
            if (w != null) w.appendRaw(text);
        } else {
            System.out.print(text);
        }
    }

    public static <T> void println(T input) {
        String text = String.valueOf(input);
        if (guiEnabled) {
            GameFXWindow w = getWindow();
            if (w != null) w.appendLog(text);
        } else {
            System.out.println(text);
        }
    }

    public static void print()   { if (!guiEnabled) System.out.print(""); }
    public static void println() { println(""); }

    // ── 화면 초기화 ───────────────────────────────────────────────────

    public static void cls() {
        if (guiEnabled) {
            GameFXWindow w = getWindow();
            if (w != null) w.clearLog();
        } else {
            try {
                if (System.getProperty("os.name").contains("Windows"))
                    new ProcessBuilder("cmd", "/c", "chcp 65001 > nul && cls").inheritIO().start().waitFor();
                else
                    Runtime.getRuntime().exec("clear");
            } catch (IOException | InterruptedException e) {
                for (int i = 0; i < 50; i++) System.out.println();
            }
        }
    }

    // ── 일시 정지 ─────────────────────────────────────────────────────

    public static void pause() {
        if (guiEnabled) {
            GameFXWindow w = getWindow();
            if (w != null) w.appendLog("  [ Enter 또는 버튼을 눌러 계속... ]");
            try { GameFXWindow.INPUT_QUEUE.take(); } catch (InterruptedException ignored) {}
        } else {
            try { IN.nextLine(); } catch (Exception ignored) {}
        }
    }

    // ── 메시지 ────────────────────────────────────────────────────────

    public static void msg(String msg) {
        if (msg == null || msg.isEmpty()) { cls(); pause(); return; }
        cls();
        println(msg);
        pause();
    }

    // ── 팝업 ──────────────────────────────────────────────────────────

    public static void popup(String body, String title, int msgType) {
        if (guiEnabled) {
            GameFXWindow w = getWindow();
            if (w != null) {
                Alert.AlertType type = msgType == 0 ? Alert.AlertType.ERROR
                                     : msgType == 2 ? Alert.AlertType.WARNING
                                     : Alert.AlertType.INFORMATION;
                w.showAlert(body, title, type);
            }
        } else {
            msg(body);
        }
    }

    public static int confirmPopup(String body, String title) {
        if (guiEnabled) {
            GameFXWindow w = getWindow();
            return (w != null) ? w.showConfirm(body, title) : 1;
        } else {
            cls();
            println(body);
            println("(Y/N)");
            Scanner in = new Scanner(System.in);
            while (!in.hasNextLine()) in.nextLine();
            String line = in.nextLine().toUpperCase().trim();
            cls();
            return (!line.isEmpty() && line.charAt(0) == 'Y') ? 0 : 1;
        }
    }

    // ── 입력 ──────────────────────────────────────────────────────────

    public static int getValidInt() {
        if (guiEnabled) {
            while (true) {
                try {
                    String s = GameFXWindow.INPUT_QUEUE.take().trim();
                    return Integer.parseInt(s);
                } catch (NumberFormatException ignored) {
                } catch (InterruptedException ignored) {}
            }
        } else {
            while (!IN.hasNextInt()) IN.nextLine();
            return IN.nextInt();
        }
    }

    public static int getValidInt(int min, int max) {
        int v = getValidInt();
        while (v < min || v > max) v = getValidInt();
        return v;
    }

    public static String getValidString() {
        if (guiEnabled) {
            while (true) {
                try {
                    String s = GameFXWindow.INPUT_QUEUE.take().trim();
                    if (!s.isEmpty()) return s;
                } catch (InterruptedException ignored) {}
            }
        } else {
            IN.reset();
            return IN.next();
        }
    }

    // ── 유틸리티 ──────────────────────────────────────────────────────

    public static boolean isDecimalNumber(String s) {
        return s != null && s.matches("\\d+\\.\\d+");
    }

    public static boolean isNumber(String s) {
        return s != null && !s.matches("\\D");
    }
}
