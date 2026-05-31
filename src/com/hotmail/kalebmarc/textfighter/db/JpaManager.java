package com.hotmail.kalebmarc.textfighter.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.h2.tools.Server;

import java.sql.SQLException;

public class JpaManager {

    private static volatile JpaManager instance;
    private final EntityManagerFactory emf;
    private Server h2WebConsole;

    private JpaManager() {
        String pu = System.getProperty("textfighter.persistenceUnit", "textfighter-pu");
        emf = Persistence.createEntityManagerFactory(pu);
        if (!"textfighter-test-pu".equals(pu)) startWebConsole();
    }

    private void startWebConsole() {
        try {
            h2WebConsole = Server.createWebServer("-webPort", "8082", "-webAllowOthers").start();
            System.out.println("[H2 Console] http://localhost:8082  (게임 실행 중 DB 확인용)");
            System.out.println("[H2 Console] JDBC URL: jdbc:h2:./saves/textfighter;AUTO_SERVER=TRUE");
        } catch (SQLException e) {
            System.out.println("[H2 Console] 콘솔 시작 실패 (포트 충돌 가능): " + e.getMessage());
        }
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static JpaManager getInstance() {
        if (instance == null) {
            synchronized (JpaManager.class) {
                if (instance == null) instance = new JpaManager();
            }
        }
        return instance;
    }

    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void close() {
        if (emf != null && emf.isOpen()) emf.close();
        if (h2WebConsole != null) h2WebConsole.stop();
        instance = null; // 다음 게임 시작 또는 테스트 재실행 시 재초기화 허용
    }
}
