import java.awt.*;
import java.awt.geom.*;


public class ScreenRenderer {

    private final GameEngine.GamePanel gp;

    public ScreenRenderer(GameEngine.GamePanel gp) {
        this.gp = gp;
    }

    // ═══════════════════════════════════════════════════════════════
    //  INTRO DIALOGUE
    // ═══════════════════════════════════════════════════════════════
    public void renderIntroDialogue(Graphics2D g2) {
        double time = System.currentTimeMillis() / 1000.0;

        GradientPaint gp2 = new GradientPaint(0, 0,
                new Color(15, 15, 30), 0, gp.H, new Color(25, 20, 40));
        g2.setPaint(gp2);
        g2.fillRect(0, 0, gp.W, gp.H);

        drawFloatingParticles(g2, time, 30);

        float titleY = (float)(100 + Math.sin(time * 1.5) * 10);
        g2.setFont(gp.TITLE_FONT);
        g2.setColor(new Color(0, 230, 255, 30));
        for (int i = 0; i < 3; i++)
            drawCentered(g2, "CYBER DETECTIVE", (int)(titleY + i));
        g2.setColor(gp.NEON_CYAN);
        drawCentered(g2, "CYBER DETECTIVE", (int) titleY);

        g2.setFont(gp.HUD_FONT);
        g2.setColor(gp.CYBER_RED);
        drawCentered(g2, "▓▓ LEVEL 1: THE MESSY DESK ▓▓",
                (int)(titleY + 50));

        drawNeonPanel(g2, gp.W / 2 - 350, 250, 700, 300, gp.DIM_CYAN);

        g2.setColor(gp.NEON_CYAN);
        g2.fillOval(gp.W / 2 - 330, 270, 50, 50);
        g2.setColor(gp.BG_DARK);
        g2.setFont(gp.BIG_FONT);
        g2.drawString("D", gp.W / 2 - 318, 305);

        g2.setFont(gp.DIALOGUE_FONT);
        for (int i = 0; i <= Math.min(gp.dialoguePhase, gp.introDialogues.length - 1); i++) {
            float lineY = (float)(290 + i * 35 + Math.sin(time * 2 + i * 0.3) * 2);
            String line = gp.introDialogues[i];
            if (i == gp.dialoguePhase) {
                long elapsed = System.currentTimeMillis() - gp.stateStartTime;
                int chars = (int)(elapsed / 30);
                line = line.substring(0, Math.min(chars, line.length()));
            }
            g2.setColor(i == gp.dialoguePhase ? Color.WHITE : new Color(150, 160, 180));
            g2.drawString(line, gp.W / 2 - 310, (int) lineY);
        }

        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 4));
        g2.setFont(gp.HUD_FONT);
        g2.setColor(new Color(0, (int)(230 * pulse), (int)(255 * pulse)));
        drawCentered(g2, gp.dialoguePhase < gp.introDialogues.length - 1
                        ? ">>> PRESS ENTER TO CONTINUE <<<"
                        : ">>> PRESS ENTER TO BEGIN INVESTIGATION <<<",
                (int)(600 + Math.sin(time * 2) * 5));

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(gp.DIM_CYAN);
        g2.drawString("Cyber Detective v1.0 // Java 17 // No External Assets", 15, gp.H - 15);
    }

    // ═══════════════════════════════════════════════════════════════
    //  LAPTOP LOGIN SCREEN
    // ═══════════════════════════════════════════════════════════════
    public void renderLaptopLogin(Graphics2D g2) {
        double time = System.currentTimeMillis() / 1000.0;

        g2.setColor(new Color(10, 10, 20));
        g2.fillRect(0, 0, gp.W, gp.H);
        drawFloatingParticles(g2, time, 20);

        float panelY = (float)(150 + Math.sin(time) * 5);
        drawNeonPanel(g2, gp.W / 2 - 300, (int) panelY, 600, 350,
                gp.hasPassword ? gp.NEON_GREEN : gp.DIM_CYAN);

        g2.setFont(gp.BIG_FONT);
        g2.setColor(gp.NEON_CYAN);
        drawCentered(g2, "LAPTOP LOGIN", (int)(panelY + 50));

        g2.setColor(gp.hasPassword ? gp.NEON_GREEN : gp.CYBER_RED);
        int lx = gp.W / 2, ly = (int)(panelY + 100);
        g2.fillRoundRect(lx - 20, ly, 40, 30, 6, 6);
        g2.setStroke(new BasicStroke(4));
        g2.drawArc(lx - 14, ly - 18, 28, 28, 0, 180);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(30, 35, 50));
        g2.fillRoundRect(gp.W / 2 - 150, (int)(panelY + 160), 300, 45, 8, 8);
        g2.setColor(gp.DIM_CYAN);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(gp.W / 2 - 150, (int)(panelY + 160), 300, 45, 8, 8);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(gp.LAPTOP_FONT);
        if (gp.passwordInput.isEmpty()) {
            g2.setColor(new Color(100, 110, 130));
            g2.drawString("Type password here...",
                    gp.W / 2 - 130, (int)(panelY + 190));
        } else {
            g2.setColor(Color.WHITE);
            g2.drawString(gp.passwordInput,
                    gp.W / 2 - 130, (int)(panelY + 190));
        }

        if ((int)(time * 2) % 2 == 0) {
            int cursorX = gp.W / 2 - 130
                    + g2.getFontMetrics().stringWidth(gp.passwordInput);
            g2.setColor(gp.NEON_CYAN);
            g2.fillRect(cursorX + 2, (int)(panelY + 172), 2, 22);
        }

        if (gp.passwordWrong) {
            g2.setFont(gp.HUD_FONT);
            g2.setColor(gp.CYBER_RED);
            drawCentered(g2, String.format(
                    "✗ WRONG PASSWORD! %d attempt(s) left", gp.passwordAttemptsLeft),
                    (int)(panelY + 235));
        }

        boolean critical = gp.remainingSeconds <= 15;
        Color tc = critical ? gp.CYBER_RED : gp.AMBER;
        float tp = critical ? (float)(0.6 + 0.4 * Math.sin(time * 8)) : 1f;
        g2.setFont(gp.HUD_FONT);
        g2.setColor(new Color(tc.getRed(), tc.getGreen(), tc.getBlue(),
                (int)(255 * tp)));
        drawCentered(g2, String.format("Time Left: %02d:%02d",
                gp.remainingSeconds / 60, gp.remainingSeconds % 60),
                (int)(panelY + 265));

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(new Color(100, 110, 130));
        drawCentered(g2, "Type the password and press ENTER | Press ESC to go back",
                (int)(panelY + 295));

        g2.setFont(gp.HUD_FONT);
        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 4));
        g2.setColor(new Color(0, (int)(200 * pulse), (int)(230 * pulse)));
        drawCentered(g2, "⌨ ENTER PASSWORD TO UNLOCK",
                (int)(panelY + 330));
    }

    // ═══════════════════════════════════════════════════════════════
    //  LEVEL COMPLETE
    // ═══════════════════════════════════════════════════════════════
    public void renderLevelComplete(Graphics2D g2) {
        double time = System.currentTimeMillis() / 1000.0;

        GradientPaint gp2 = new GradientPaint(0, 0,
                new Color(10, 20, 15), 0, gp.H, new Color(15, 30, 20));
        g2.setPaint(gp2);
        g2.fillRect(0, 0, gp.W, gp.H);
        drawFloatingParticles(g2, time, 40);

        float panelY = (float)(120 + Math.sin(time) * 8);
        drawNeonPanel(g2, gp.W / 2 - 300, (int) panelY, 600, 400, gp.NEON_GREEN);

        g2.setColor(gp.AMBER);
        g2.setFont(new Font("Consolas", Font.BOLD, 64));
        drawCentered(g2, "★", (int)(panelY + 70));

        g2.setFont(gp.TITLE_FONT);
        g2.setColor(gp.NEON_GREEN);
        drawCentered(g2, "LEVEL COMPLETE!", (int)(panelY + 130));

        g2.setFont(gp.HUD_FONT);
        g2.setColor(Color.WHITE);
        String[] stats = {
                String.format("Time: %02d:%02d", gp.elapsedSeconds / 60, gp.elapsedSeconds % 60),
                String.format("Objects Found: %d/%d", gp.objectsFound, gp.totalObjects),
                "",
                "Evidence secured from the laptop.",
                "The suspect used 'admin123' as their password.",
                "Critical lesson: NEVER write passwords on sticky notes!"
        };
        for (int i = 0; i < stats.length; i++) {
            float lineY = (float)(panelY + 170 + i * 28
                    + Math.sin(time * 2 + i * 0.3) * 2);
            g2.setColor(i < 2 ? gp.NEON_CYAN : new Color(180, 200, 210));
            drawCentered(g2, stats[i], (int) lineY);
        }

        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 3.5));
        g2.setFont(gp.HUD_FONT);
        g2.setColor(new Color(0, (int)(255 * pulse), (int)(120 * pulse)));
        drawCentered(g2, ">>> PRESS ENTER FOR LEVEL SELECT <<<",
                (int)(panelY + 370));
    }

    // ═══════════════════════════════════════════════════════════════
    //  MISSION FAILED SCREEN
    // ═══════════════════════════════════════════════════════════════
    public void renderMissionFailed(Graphics2D g2) {
        double time = System.currentTimeMillis() / 1000.0;

        GradientPaint gp2 = new GradientPaint(0, 0,
                new Color(30, 5, 5), 0, gp.H, new Color(50, 10, 10));
        g2.setPaint(gp2);
        g2.fillRect(0, 0, gp.W, gp.H);
        drawFloatingParticles(g2, time, 15);

        for (int i = 0; i < 8; i++) {
            int gy = gp.rng.nextInt(gp.H);
            g2.setColor(new Color(255, 0, 0, 30 + gp.rng.nextInt(40)));
            g2.fillRect(0, gy, gp.W, 2 + gp.rng.nextInt(4));
        }

        float panelY = (float)(150 + Math.sin(time * 0.8) * 5);
        drawNeonPanel(g2, gp.W / 2 - 300, (int) panelY, 600, 380, gp.CYBER_RED);

        g2.setFont(new Font("Consolas", Font.BOLD, 64));
        g2.setColor(gp.CYBER_RED);
        drawCentered(g2, "✗", (int)(panelY + 70));

        g2.setFont(gp.TITLE_FONT);
        float pulse = (float)(0.6 + 0.4 * Math.sin(time * 4));
        g2.setColor(new Color(255, (int)(50 * pulse), (int)(70 * pulse)));
        drawCentered(g2, "MISSION FAILED", (int)(panelY + 130));

        g2.setFont(gp.DIALOGUE_FONT);
        g2.setColor(Color.WHITE);
        drawCentered(g2, gp.failReason, (int)(panelY + 180));

        g2.setFont(gp.HUD_FONT);
        g2.setColor(new Color(200, 180, 180));
        String[] stats = {
                String.format("Objects Found: %d/%d", gp.objectsFound, gp.totalObjects),
                String.format("Password Found: %s", gp.hasPassword ? "YES" : "NO"),
                String.format("Time Elapsed: %02d:%02d", gp.elapsedSeconds / 60, gp.elapsedSeconds % 60),
                "",
                "Lesson: Speed AND accuracy matter in cybersecurity!"
        };
        for (int i = 0; i < stats.length; i++) {
            drawCentered(g2, stats[i], (int)(panelY + 220 + i * 25));
        }

        g2.setFont(gp.HUD_FONT);
        pulse = (float)(0.5 + 0.5 * Math.sin(time * 3));
        g2.setColor(new Color((int)(255 * pulse), 0, 0));
        drawCentered(g2, ">>> PRESS R TO RETRY  |  ESC TO QUIT <<<",
                (int)(panelY + 360));
    }

    // ═══════════════════════════════════════════════════════════════
    //  LEVEL SELECT (City Map)
    // ═══════════════════════════════════════════════════════════════
    public void renderLevelSelect(Graphics2D g2) {
        double time = System.currentTimeMillis() / 1000.0;

        GradientPaint mapBg = new GradientPaint(0, 0,
                new Color(35, 40, 55), gp.W, gp.H, new Color(25, 30, 45));
        g2.setPaint(mapBg);
        g2.fillRect(0, 0, gp.W, gp.H);

        g2.setFont(gp.TITLE_FONT);
        g2.setColor(gp.NEON_CYAN);
        drawCentered(g2, "CITY MAP — SELECT MISSION", 55);

        g2.setColor(new Color(60, 65, 80));
        g2.setStroke(new BasicStroke(2));
        for (int i = 80; i < gp.W; i += 120) g2.drawLine(i, 80, i, gp.H - 40);
        for (int i = 80; i < gp.H; i += 100) g2.drawLine(40, i, gp.W - 40, i);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(45, 50, 65));
        for (int i = 0; i < 15; i++) {
            int bx = 100 + (i % 5) * 200;
            int by = 100 + (i / 5) * 180;
            int bw = 60 + (i * 17) % 60;
            int bh = 40 + (i * 13) % 50;
            g2.fillRect(bx, by, bw, bh);
            g2.setColor(new Color(255, 220, 100, 40));
            for (int wy = 0; wy < bh - 10; wy += 12) {
                for (int wx = 0; wx < bw - 10; wx += 14) {
                    g2.fillRect(bx + 4 + wx, by + 4 + wy, 6, 6);
                }
            }
            g2.setColor(new Color(45, 50, 65));
        }

        for (int i = 0; i < gp.levelPins.length; i++) {
            int px = gp.levelPins[i][0];
            float py = (float)(gp.levelPins[i][1] + Math.sin(time * 2 + i) * 6);
            boolean unlocked = (i == 0);

            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillOval(px - 12, (int)(py + 22), 24, 8);

            Color pinColor = unlocked ? gp.NEON_GREEN
                    : (i == 1 ? gp.AMBER : new Color(80, 80, 90));
            g2.setColor(pinColor);
            g2.fillOval(px - 15, (int)(py - 15), 30, 30);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(px - 5, (int)(py - 5), 10, 10);

            g2.setColor(pinColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(px, (int)(py + 15), px, (int)(py + 22));
            g2.setStroke(new BasicStroke(1));

            g2.setFont(gp.LABEL_FONT);
            g2.setColor(unlocked ? gp.NEON_GREEN : new Color(120, 120, 140));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(gp.levelNames[i],
                    px - fm.stringWidth(gp.levelNames[i]) / 2, (int)(py + 42));

            g2.setFont(gp.SMALL_FONT);
            String status = unlocked ? "COMPLETE ★"
                    : (i == 1 ? "UNLOCKED" : "LOCKED 🔒");
            g2.setColor(unlocked ? gp.NEON_GREEN
                    : (i == 1 ? gp.AMBER : new Color(80, 80, 90)));
            fm = g2.getFontMetrics();
            g2.drawString(status,
                    px - fm.stringWidth(status) / 2, (int)(py + 56));
        }

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(gp.DIM_CYAN);
        drawCentered(g2, "More levels coming soon... | Press ESC to exit", gp.H - 20);
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILITY METHODS (static so DeskRenderer can use them too)
    // ═══════════════════════════════════════════════════════════════

    
    public static void drawNeonPanel(Graphics2D g2, int x, int y,
                                     int w, int h, Color border) {
        g2.setColor(GameEngine.GamePanel.PANEL_BG);
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(new Color(border.getRed(), border.getGreen(),
                border.getBlue(), 30));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x - 2, y - 2, w + 4, h + 4, 14, 14);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, w, h, 12, 12);
        g2.setStroke(new BasicStroke(1));
    }

    
    private void drawCentered(Graphics2D g2, String s, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, (gp.W - fm.stringWidth(s)) / 2, y);
    }


    public static void drawCenteredStatic(Graphics2D g2, String s, int y, int width) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, (width - fm.stringWidth(s)) / 2, y);
    }

   
    private void drawFloatingParticles(Graphics2D g2, double time, int count) {
        for (int i = 0; i < count; i++) {
            float x = (float)((i * 37 + Math.sin(time + i * 0.7) * 50) % gp.W);
            float y = (float)((i * 23 + time * 20) % gp.H);
            float wobble = (float)(Math.sin(time * 2 + i) * 10);
            int alpha = (int)(30 + 20 * Math.sin(time * 3 + i));
            g2.setColor(new Color(0, 230, 255, Math.max(10, alpha)));
            g2.fill(new Ellipse2D.Float(x + wobble, gp.H - y, 2 + i % 3, 2 + i % 3));
        }
    }
}

