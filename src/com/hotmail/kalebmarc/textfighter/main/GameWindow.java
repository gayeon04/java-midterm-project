package com.hotmail.kalebmarc.textfighter.main;

import com.hotmail.kalebmarc.textfighter.item.Armour;
import com.hotmail.kalebmarc.textfighter.player.*;
import time.GameClock;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameWindow extends JFrame {

    static final BlockingQueue<String> INPUT_QUEUE = new LinkedBlockingQueue<>();

    private JTextPane textPane;
    private StyledDocument doc;
    private JTextField inputField;

    private JLabel playerNameLabel, playerHpLabel, levelLabel, coinsLabel, weaponLabel, armourLabel;
    private JProgressBar playerHpBar;
    private JLabel enemyNameLabel, enemyHpLabel;
    private JProgressBar enemyHpBar;
    private JLabel killsLabel, timeLabel;

    public GameWindow() {
        super("텍스트 파이터");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(12, 12, 20));
        setLayout(new BorderLayout(4, 4));

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(10, 10, 15));
        textPane.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        doc = textPane.getStyledDocument();

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));
        scroll.getViewport().setBackground(new Color(10, 10, 15));

        inputField = new JTextField();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, buildStatusPanel());
        split.setDividerLocation(690);
        split.setDividerSize(4);
        split.setBackground(new Color(12, 12, 20));
        split.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));

        add(split, BorderLayout.CENTER);
        add(buildInputPanel(), BorderLayout.SOUTH);

        new Timer(400, e -> refreshStatus()).start();

        setVisible(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    // ── 상태 패널 ────────────────────────────────────────────────────

    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(16, 16, 28));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        panel.add(makeTitle("⚔  텍스트 파이터"));
        panel.add(vgap(10));
        panel.add(makeDivider());
        panel.add(vgap(12));

        panel.add(makeHeader("플레이어"));
        panel.add(vgap(6));
        playerNameLabel = makeLabel("이름:    -");
        playerHpLabel   = makeLabel("체력:    -");
        playerHpBar     = makeHpBar(new Color(50, 200, 80));
        levelLabel      = makeLabel("레벨:    -");
        coinsLabel      = makeLabel("코인:    -");
        weaponLabel     = makeLabel("무기:    -");
        armourLabel     = makeLabel("방어구:  -");
        panel.add(playerNameLabel);
        panel.add(vgap(4));
        panel.add(playerHpLabel);
        panel.add(vgap(3));
        panel.add(playerHpBar);
        panel.add(vgap(6));
        panel.add(levelLabel);
        panel.add(coinsLabel);
        panel.add(weaponLabel);
        panel.add(armourLabel);

        panel.add(vgap(14));
        panel.add(makeDivider());
        panel.add(vgap(12));

        panel.add(makeHeader("현재 적"));
        panel.add(vgap(6));
        enemyNameLabel = makeLabel("이름:    -");
        enemyHpLabel   = makeLabel("체력:    -");
        enemyHpBar     = makeHpBar(new Color(210, 70, 70));
        panel.add(enemyNameLabel);
        panel.add(vgap(4));
        panel.add(enemyHpLabel);
        panel.add(vgap(3));
        panel.add(enemyHpBar);

        panel.add(vgap(14));
        panel.add(makeDivider());
        panel.add(vgap(12));

        panel.add(makeHeader("통계"));
        panel.add(vgap(6));
        killsLabel = makeLabel("처치:    -");
        timeLabel  = makeLabel("시간:    -");
        panel.add(killsLabel);
        panel.add(timeLabel);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // ── 입력 패널 ────────────────────────────────────────────────────

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBackground(new Color(18, 18, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel prompt = new JLabel("  입력 › ");
        prompt.setForeground(new Color(120, 200, 120));
        prompt.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        inputField.setBackground(new Color(25, 25, 40));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(new Color(120, 200, 120));
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 110)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JButton btn = new JButton("확인");
        btn.setBackground(new Color(50, 80, 160));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));

        ActionListener submit = e -> {
            String text = inputField.getText();
            INPUT_QUEUE.offer(text); // 빈 문자열도 허용 (pause용)
            inputField.setText("");
        };
        inputField.addActionListener(submit);
        btn.addActionListener(submit);

        panel.add(prompt, BorderLayout.WEST);
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(btn, BorderLayout.EAST);
        return panel;
    }

    // ── 상태 새로고침 ─────────────────────────────────────────────────

    private void refreshStatus() {
        try {
            int hp = Health.get(), maxHp = Health.getOutOf();
            playerNameLabel.setText("이름:    " + User.name());
            playerHpLabel.setText("체력:    " + hp + " / " + maxHp);
            playerHpBar.setMaximum(maxHp > 0 ? maxHp : 1);
            playerHpBar.setValue(Math.max(0, hp));
            double ratio = maxHp > 0 ? (double) hp / maxHp : 1.0;
            playerHpBar.setForeground(
                ratio > 0.6 ? new Color(50, 200, 80) :
                ratio > 0.3 ? new Color(220, 180, 0) :
                              new Color(210, 50, 50)
            );
            levelLabel.setText("레벨:    " + Xp.getLevel() + "  (" + Xp.getFull() + ")");
            coinsLabel.setText("코인:    " + Coins.get());
            weaponLabel.setText("무기:    " + Weapon.get().getName());
            armourLabel.setText("방어구:  " + Armour.getEquipped().toString());
        } catch (Exception ignored) {}

        try {
            Enemy enemy = Enemy.get();
            if (enemy != null) {
                int eHp = enemy.getHealth(), eMax = enemy.getHealthMax();
                enemyNameLabel.setText("이름:    " + enemy.getName());
                enemyHpLabel.setText("체력:    " + eHp + " / " + eMax);
                enemyHpBar.setMaximum(eMax > 0 ? eMax : 1);
                enemyHpBar.setValue(Math.max(0, eHp));
            }
        } catch (Exception ignored) {}

        try {
            killsLabel.setText("처치:    " + Stats.kills + "  (최고: " + Stats.highScore + ")");
            timeLabel.setText(GameClock.getGameDate() + "  " + GameClock.getGameTime());
        } catch (Exception ignored) {}
    }

    // ── 출력 메서드 ──────────────────────────────────────────────────

    public void append(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, color);
                doc.insertString(doc.getLength(), text, attrs);
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    public void appendLine(String text) {
        append(text + "\n", detectColor(text));
    }

    public void appendLine(String text, Color color) {
        append(text + "\n", color);
    }

    public void appendRaw(String text) {
        append(text, new Color(220, 220, 220));
    }

    public void addSeparator() {
        append("\n──────────────────────────────────────────────────────────\n",
               new Color(45, 45, 65));
    }

    // 텍스트 내용에 따라 색상 자동 결정
    private Color detectColor(String text) {
        if (text == null) return new Color(220, 220, 220);
        String t = text.trim();
        if (t.contains("처치") || t.contains("쓰러뜨"))        return new Color(255, 215, 60);
        if (t.contains("크리티컬"))                            return new Color(255, 140, 0);
        if (t.contains("데미지") || t.contains("공격했"))      return new Color(255, 110, 110);
        if (t.contains("반격") || t.contains("공격받"))        return new Color(255, 90, 90);
        if (t.contains("빗나") || t.contains("실패"))          return new Color(130, 130, 130);
        if (t.contains("회복") || t.contains("힐") || t.contains("포션")) return new Color(80, 220, 130);
        if (t.contains("저장") || t.contains("자동저장"))      return new Color(100, 160, 220);
        if (t.contains("퀘스트") || t.contains("완료"))        return new Color(180, 130, 255);
        if (t.startsWith("===") || t.startsWith("---"))        return new Color(100, 100, 140);
        if (t.startsWith("  ["))                               return new Color(160, 160, 200);
        if (t.matches("^\\s*\\d+\\).*"))                       return new Color(160, 200, 255);
        return new Color(220, 220, 220);
    }

    // ── 빌더 헬퍼 ───────────────────────────────────────────────────

    private JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(200, 175, 100));
        l.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel makeHeader(String text) {
        JLabel l = new JLabel("▶  " + text);
        l.setForeground(new Color(155, 135, 75));
        l.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(200, 200, 210));
        l.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JProgressBar makeHpBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(100);
        bar.setForeground(color);
        bar.setBackground(new Color(35, 35, 50));
        bar.setBorderPainted(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        bar.setPreferredSize(new Dimension(0, 10));
        bar.setAlignmentX(LEFT_ALIGNMENT);
        return bar;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(55, 55, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }
}
