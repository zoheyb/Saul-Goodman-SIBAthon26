import java.awt.*;
import java.awt.geom.*;

public class DeskRenderer {

    private final GameEngine.GamePanel gp;

    public DeskRenderer(GameEngine.GamePanel gp) {
        this.gp = gp;
    }

    // ═══════════════════════════════════════════════════════════════
    //  DESK SEARCH — main hidden object gameplay scene
    // ═══════════════════════════════════════════════════════════════
    public void renderDeskSearch(Graphics2D g2) {
        double time = System.currentTimeMillis() / 1000.0;

        // ── Room background with gradient ────────────────────────
        GradientPaint room = new GradientPaint(0, 0,
                new Color(30, 25, 40), 0, gp.H, new Color(50, 40, 55));
        g2.setPaint(room);
        g2.fillRect(0, 0, gp.W, gp.H);

        // ── Wall ─────────────────────────────────────────────────
        g2.setColor(new Color(60, 50, 65));
        g2.fillRect(0, 0, gp.W, 140);
        GradientPaint shadow = new GradientPaint(0, 130,
                new Color(0, 0, 0, 100), 0, 180, new Color(0, 0, 0, 0));
        g2.setPaint(shadow);
        g2.fillRect(0, 130, gp.W, 50);

        // ── Desk surface ─────────────────────────────────────────
        drawDesk(g2, time);

        // ── Draw all desk objects ────────────────────────────────
        drawMonitor(g2, time);
        drawKeyboard(g2);
        drawLaptop(g2, time);
        drawCoffeeMug(g2, time);
        drawScatteredPapers(g2);
        drawDecoyNotes(g2, time);
        if (gp.stickyNoteVisible) drawStickyNote(g2, time);
        drawUSBDrive(g2);
        drawSmartphone(g2, time);
        drawPenHolder(g2);
        drawMouseDevice(g2);
        drawDeskDrawer(g2);

        // ── URGENCY EFFECT — red pulse when time is low ──────────
        if (gp.remainingSeconds <= 20 && gp.remainingSeconds > 0) {
            float urgency = (float)(0.1 + 0.1 * Math.sin(time * 6));
            g2.setColor(new Color(255, 0, 0, (int)(urgency * 150)));
            g2.fillRect(0, 0, gp.W, gp.H);
        }

        // ── Penalty flash overlay ────────────────────────────────
        if (gp.penaltyFlashAlpha > 0) {
            g2.setColor(new Color(255, 50, 50, gp.penaltyFlashAlpha));
            g2.fillRect(0, 0, gp.W, gp.H);
        }

        // ── Highlight found items with check marks ───────────────
        for (GameEngine.ClickableItem item : gp.deskItems) {
            if (item.found && !item.name.equals("Laptop")) {
                g2.setColor(new Color(0, 255, 120, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(item.bounds.x - 3, item.bounds.y - 3,
                        item.bounds.width + 6, item.bounds.height + 6, 8, 8);
                g2.setFont(gp.LABEL_FONT);
                g2.setColor(gp.NEON_GREEN);
                g2.drawString("✓", item.bounds.x + item.bounds.width - 5,
                        item.bounds.y + 5);
                g2.setStroke(new BasicStroke(1));
            }
        }

        // ── Hover highlight ──────────────────────────────────────
        if (!gp.hoverName.isEmpty()) {
            for (GameEngine.ClickableItem item : gp.deskItems) {
                if (item.name.equals(gp.hoverName) && !item.found) {
                    float hPulse = (float)(0.4 + 0.3 * Math.sin(time * 6));
                    g2.setColor(new Color(0, 230, 255, (int)(hPulse * 100)));
                    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
                    g2.drawRoundRect(item.bounds.x - 4, item.bounds.y - 4,
                            item.bounds.width + 8, item.bounds.height + 8, 8, 8);
                    g2.setStroke(new BasicStroke(1));
                    g2.setFont(gp.LABEL_FONT);
                    g2.setColor(gp.NEON_CYAN);
                    g2.drawString("🔍 " + item.name, gp.mouseX + 15, gp.mouseY - 10);
                }
            }
        }

        // ── HUD (calls ScreenRenderer helper) ────────────────────
        drawHUD(g2, time);

        // ── Feedback message ─────────────────────────────────────
        if (!gp.feedbackMsg.isEmpty()
                && System.currentTimeMillis() - gp.feedbackTime < 3000) {
            drawFeedbackPopup(g2, time);
        }

        // ── CRT scan lines ───────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 15));
        for (int y = 0; y < gp.H; y += 3) {
            g2.drawLine(0, y, gp.W, y);
        }
    }

    // ── Draw the wooden desk ────────────────────────────────────
    private void drawDesk(Graphics2D g2, double time) {
        GradientPaint deskGrad = new GradientPaint(0, 150,
                gp.DESK_TOP, 0, 550, gp.DESK_BROWN);
        g2.setPaint(deskGrad);
        g2.fillRoundRect(30, 150, gp.W - 60, 430, 8, 8);

        g2.setColor(new Color(gp.WOOD_GRAIN.getRed(), gp.WOOD_GRAIN.getGreen(),
                gp.WOOD_GRAIN.getBlue(), 40));
        for (int i = 0; i < 20; i++) {
            int y = 160 + i * 22;
            g2.drawLine(40, y, gp.W - 70, y);
        }

        g2.setColor(new Color(80, 50, 25));
        g2.fillRect(30, 575, gp.W - 60, 15);

        g2.setColor(new Color(70, 45, 20));
        g2.fillRect(60, 590, 20, 110);
        g2.fillRect(gp.W - 110, 590, 20, 110);

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRect(40, 700, gp.W - 80, 10);
    }

    // ── Draw desktop monitor ────────────────────────────────────
    private void drawMonitor(Graphics2D g2, double time) {
        g2.setColor(new Color(50, 50, 55));
        g2.fillRect(505, 135, 100, 15);
        g2.fillRect(540, 70, 30, 70);

        g2.setColor(new Color(35, 35, 40));
        g2.fillRoundRect(380, 15, 350, 130, 8, 8);

        GradientPaint screenGrad = new GradientPaint(390, 25,
                new Color(20, 30, 60), 390, 135, new Color(30, 45, 80));
        g2.setPaint(screenGrad);
        g2.fillRoundRect(390, 25, 330, 110, 4, 4);

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(gp.NEON_GREEN);
        String[] lines = {"C:\\> scanning network...",
                "192.168.1.1 ... OK", "192.168.1.42 ... ALERT"};
        for (int i = 0; i < lines.length; i++) {
            float flicker = (float)(0.7 + 0.3 * Math.sin(time * 5 + i));
            g2.setColor(new Color(0, (int)(255 * flicker), (int)(120 * flicker)));
            g2.drawString(lines[i], 405, 50 + i * 15);
        }

        if ((int)(time * 2) % 2 == 0) {
            g2.setColor(gp.NEON_GREEN);
            g2.fillRect(405, 95, 8, 12);
        }

        g2.setColor(new Color(255, 255, 255, 8));
        g2.fillRect(390, 25, 165, 110);
    }

    // ── Draw keyboard ───────────────────────────────────────────
    private void drawKeyboard(Graphics2D g2) {
        g2.setColor(new Color(45, 45, 50));
        g2.fillRoundRect(420, 170, 180, 60, 6, 6);

        g2.setColor(gp.KEY_COLOR);
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 12; col++) {
                g2.fillRoundRect(428 + col * 14, 177 + row * 14,
                        11, 11, 2, 2);
            }
        }
        g2.fillRoundRect(460, 233, 80, 11, 3, 3);
    }

    // ── Draw the laptop ─────────────────────────────────────────
    private void drawLaptop(Graphics2D g2, double time) {
        g2.setColor(new Color(55, 55, 65));
        g2.fillRoundRect(430, 200, 250, 160, 8, 8);

        GradientPaint laptopScreen = new GradientPaint(440, 208,
                gp.SCREEN_BLUE, 440, 340, new Color(20, 30, 55));
        g2.setPaint(laptopScreen);
        g2.fillRoundRect(440, 208, 230, 120, 4, 4);

        g2.setColor(gp.hasPassword ? gp.NEON_GREEN : gp.CYBER_RED);
        int lockX = 545, lockY = 248;
        g2.fillRoundRect(lockX - 12, lockY, 24, 20, 4, 4);
        g2.setStroke(new BasicStroke(3));
        g2.drawArc(lockX - 8, lockY - 12, 16, 16, 0, 180);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(gp.hasPassword ? gp.NEON_GREEN : Color.WHITE);
        String lockText = gp.hasPassword ? "CLICK TO UNLOCK" : "LOCKED — Find Password";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(lockText, 555 - fm.stringWidth(lockText) / 2, 290);

        float glow = (float)(0.03 + 0.02 * Math.sin(time * 3));
        g2.setColor(new Color(gp.hasPassword ? 0 : 255,
                gp.hasPassword ? 255 : 40,
                gp.hasPassword ? 120 : 70, (int)(glow * 255)));
        g2.fillRoundRect(440, 208, 230, 120, 4, 4);

        g2.setColor(new Color(50, 50, 60));
        g2.fillRoundRect(440, 335, 230, 25, 4, 4);
        g2.setColor(new Color(60, 60, 70));
        for (int i = 0; i < 18; i++) {
            g2.fillRect(445 + i * 12, 338, 9, 8);
        }
        g2.setColor(new Color(55, 55, 65));
        g2.fillRoundRect(520, 349, 60, 8, 3, 3);
    }

    // ── Draw coffee mug ─────────────────────────────────────────
    private void drawCoffeeMug(Graphics2D g2, double time) {
        g2.setColor(gp.MUG_COLOR);
        g2.fillRoundRect(160, 350, 55, 65, 6, 6);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(gp.MUG_COLOR);
        g2.drawArc(210, 365, 20, 30, -90, 180);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(gp.COFFEE_BROWN);
        g2.fillOval(164, 354, 47, 12);
        g2.setColor(new Color(200, 200, 200, 60));
        for (int i = 0; i < 3; i++) {
            float sx = (float)(175 + i * 12 + Math.sin(time * 3 + i) * 5);
            float sy = (float)(345 - i * 5 + Math.sin(time * 2 + i) * 3);
            g2.fill(new Ellipse2D.Float(sx, sy - 8, 4, 8));
        }
        g2.setColor(new Color(80, 50, 30, 40));
        g2.drawOval(158, 410, 60, 10);
    }

    // ── Draw scattered papers ───────────────────────────────────
    private void drawScatteredPapers(Graphics2D g2) {
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(Math.toRadians(-5), 440, 430);
        g2r.setColor(gp.PAPER_WHITE);
        g2r.fillRect(395, 405, 120, 55);
        g2r.setColor(new Color(100, 100, 110));
        g2r.setFont(new Font("Consolas", Font.PLAIN, 8));
        g2r.drawString("COMPANY PASSWORD POLICY", 400, 420);
        g2r.drawString("Rule 1: Never write passwords", 400, 432);
        g2r.drawString("Rule 2: Use 2FA always", 400, 444);
        g2r.drawString("Rule 3: Report suspicious email", 400, 456);
        g2r.dispose();

        g2r = (Graphics2D) g2.create();
        g2r.rotate(Math.toRadians(8), 470, 440);
        g2r.setColor(new Color(230, 225, 210));
        g2r.fillRect(430, 418, 110, 50);
        g2r.setColor(new Color(120, 120, 130));
        g2r.setFont(new Font("Consolas", Font.PLAIN, 8));
        g2r.drawString("Q4 Security Audit Report", 435, 432);
        g2r.drawString("STATUS: ██ FAILED ██", 435, 444);
        g2r.drawString("3 critical vulnerabilities", 435, 456);
        g2r.dispose();
    }

    // ── Draw DECOY sticky notes ─────────────────────────────────
    private void drawDecoyNotes(Graphics2D g2, double time) {
        float f1 = (float)(Math.sin(time * 1.2) * 2);
        g2.setColor(new Color(255, 120, 120));
        g2.fillRect(100, (int)(200 + f1), 72, 55);
        g2.setColor(new Color(200, 80, 80));
        g2.fillPolygon(new int[]{172, 158, 172},
                new int[]{(int)(200+f1), (int)(200+f1), (int)(214+f1)}, 3);
        g2.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2.setColor(new Color(80, 0, 0));
        g2.drawString("WiFi Pass:", 106, (int)(218 + f1));
        g2.drawString("office2024", 106, (int)(232 + f1));

        float f2 = (float)(Math.sin(time * 1.8 + 1) * 2);
        g2.setColor(new Color(140, 200, 255));
        g2.fillRect(900, (int)(475 + f2), 68, 50);
        g2.setColor(new Color(100, 160, 220));
        g2.fillPolygon(new int[]{968, 955, 968},
                new int[]{(int)(475+f2), (int)(475+f2), (int)(488+f2)}, 3);
        g2.setFont(new Font("Consolas", Font.PLAIN, 8));
        g2.setColor(new Color(0, 0, 100));
        g2.drawString("guest /", 905, (int)(492 + f2));
        g2.drawString("welcome1", 905, (int)(504 + f2));
    }

    // ── Draw desk drawer ────────────────────────────────────────
    private void drawDeskDrawer(Graphics2D g2) {
        g2.setColor(new Color(90, 58, 30));
        g2.fillRoundRect(60, 520, 110, 40, 4, 4);
        g2.setColor(new Color(70, 45, 20));
        g2.drawRoundRect(60, 520, 110, 40, 4, 4);
        g2.setColor(new Color(160, 140, 100));
        g2.fillRoundRect(100, 536, 30, 8, 3, 3);
        g2.setColor(new Color(40, 30, 15));
        g2.fillOval(140, 537, 6, 6);
    }

    // ── Draw the hidden sticky note ─────────────────────────────
    private void drawStickyNote(Graphics2D g2, double time) {
        float noteFloat = (float)(Math.sin(time * 1.5) * 2);
        long timeSinceVisible = System.currentTimeMillis() - gp.feedbackTime;
        float slideOffset = Math.max(0, 150 - timeSinceVisible * 0.3f);
        int noteX = (int)(780 + slideOffset);

        if (!gp.stickyNoteFound) {
            float glow = (float)(0.5 + 0.5 * Math.sin(time * 5));
            g2.setColor(new Color(255, 255, 0, (int)(glow * 120)));
            g2.fillOval(noteX + 70, (int)(360 + noteFloat), 25, 25);
            g2.setFont(new Font("Consolas", Font.BOLD, 9));
            g2.setColor(gp.CYBER_RED);
            g2.drawString("NEW", noteX + 73, (int)(377 + noteFloat));
        }

        g2.setColor(gp.STICKY_YELLOW);
        g2.fillRect(noteX, (int)(370 + noteFloat), 88, 70);

        g2.setColor(new Color(230, 210, 70));
        int[] xCorner = {noteX+88, noteX+70, noteX+88};
        int[] yCorner = {(int)(370 + noteFloat), (int)(370 + noteFloat),
                (int)(388 + noteFloat)};
        g2.fillPolygon(xCorner, yCorner, 3);

        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRect(noteX+3, (int)(443 + noteFloat), 88, 4);

        Font noteFont = gp.HANDWRITING.deriveFont(Font.PLAIN, 13f);
        g2.setFont(noteFont);
        g2.setColor(new Color(30, 30, 180));
        g2.drawString("Password:", noteX+8, (int)(393 + noteFloat));
        g2.setFont(noteFont.deriveFont(Font.BOLD, 15f));
        g2.setColor(new Color(200, 30, 30));
        g2.drawString("admin123", noteX+8, (int)(418 + noteFloat));

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(new Color(30, 30, 180));
        g2.drawString(":)", noteX+60, (int)(435 + noteFloat));

        if (gp.stickyNoteFound) {
            float glow = (float)(0.3 + 0.2 * Math.sin(time * 5));
            g2.setColor(new Color(0, 255, 120, (int)(glow * 200)));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(noteX-3, (int)(367 + noteFloat), 94, 76, 4, 4);
            g2.setStroke(new BasicStroke(1));
        }
    }

    // ── Draw USB drive ──────────────────────────────────────────
    private void drawUSBDrive(Graphics2D g2) {
        g2.setColor(gp.CYBER_RED);
        g2.fillRoundRect(620, 490, 45, 18, 4, 4);
        g2.setColor(new Color(180, 180, 190));
        g2.fillRect(660, 494, 12, 10);
        g2.setColor(new Color(255, 0, 0, 180));
        g2.fillOval(628, 496, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 7));
        g2.drawString("???", 636, 503);
    }

    // ── Draw smartphone ─────────────────────────────────────────
    private void drawSmartphone(Graphics2D g2, double time) {
        g2.setColor(new Color(30, 30, 35));
        g2.fillRoundRect(280, 440, 52, 88, 8, 8);
        g2.setColor(new Color(20, 25, 50));
        g2.fillRoundRect(284, 450, 44, 68, 4, 4);
        g2.setColor(gp.CYBER_RED);
        g2.fillRect(284, 450, 44, 12);
        g2.setFont(new Font("Consolas", Font.PLAIN, 7));
        g2.setColor(Color.WHITE);
        g2.drawString("2FA OFF!", 289, 459);
        float flicker = (float)(0.7 + 0.3 * Math.sin(time * 2));
        g2.setColor(new Color(150, 150, 200, (int)(flicker * 255)));
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("16:07", 290, 490);
        g2.setColor(new Color(50, 50, 55));
        g2.fillOval(298, 520, 16, 5);
    }

    // ── Draw pen holder ─────────────────────────────────────────
    private void drawPenHolder(Graphics2D g2) {
        g2.setColor(new Color(70, 70, 80));
        g2.fillRoundRect(900, 190, 40, 70, 6, 6);
        g2.setColor(new Color(60, 60, 70));
        g2.fillOval(900, 185, 40, 14);
        Color[] penColors = {gp.CYBER_RED, gp.NEON_CYAN, gp.AMBER,
                new Color(100, 200, 100)};
        for (int i = 0; i < penColors.length; i++) {
            g2.setColor(penColors[i]);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(908 + i * 8, 190, 905 + i * 10, 160 - i * 5);
            g2.setStroke(new BasicStroke(1));
        }
    }

    // ── Draw mouse device ───────────────────────────────────────
    private void drawMouseDevice(Graphics2D g2) {
        g2.setColor(new Color(60, 60, 65));
        g2.fillRoundRect(700, 250, 35, 55, 15, 15);
        g2.setColor(new Color(80, 80, 85));
        g2.fillRoundRect(713, 260, 8, 12, 3, 3);
        g2.setColor(new Color(50, 50, 55));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(717, 250, 720, 200);
        g2.setStroke(new BasicStroke(1));
    }

    // ── HUD overlay ─────────────────────────────────────────────
    private void drawHUD(Graphics2D g2, double time) {
        g2.setColor(new Color(10, 10, 20, 200));
        g2.fillRect(0, 0, gp.W, 55);
        g2.setColor(new Color(0, 230, 255, 80));
        g2.drawLine(0, 55, gp.W, 55);

        g2.setFont(gp.HUD_FONT);
        g2.setColor(gp.NEON_CYAN);
        g2.drawString("LEVEL 1: THE MESSY DESK", 20, 22);

        boolean critical = gp.remainingSeconds <= 15;
        boolean warning  = gp.remainingSeconds <= 30 && !critical;
        Color timerColor = critical ? gp.CYBER_RED : (warning ? gp.AMBER : gp.NEON_GREEN);
        float timerPulse = critical ? (float)(0.6 + 0.4 * Math.sin(time * 8)) : 1f;
        g2.setColor(new Color(timerColor.getRed(), timerColor.getGreen(),
                timerColor.getBlue(), (int)(255 * timerPulse)));
        g2.drawString(String.format("⏱ %02d:%02d",
                gp.remainingSeconds / 60, gp.remainingSeconds % 60), 320, 22);

        float timerFrac = (float) gp.remainingSeconds / gp.MISSION_TIME_LIMIT;
        int barX = 420, barW = 200;
        g2.setColor(new Color(30, 30, 50));
        g2.fillRoundRect(barX, 10, barW, 16, 6, 6);
        g2.setColor(timerColor);
        g2.fillRoundRect(barX, 10, (int)(barW * timerFrac), 16, 6, 6);
        g2.setColor(new Color(255,255,255,60));
        g2.drawRoundRect(barX, 10, barW, 16, 6, 6);

        g2.setColor(gp.NEON_GREEN);
        g2.drawString(String.format("Found: %d/%d", gp.objectsFound, gp.totalObjects), 650, 22);

        g2.setColor(gp.hasPassword ? gp.NEON_GREEN : gp.CYBER_RED);
        g2.drawString(gp.hasPassword ? "KEY ACQUIRED" : "FIND PASSWORD", 850, 22);

        g2.setFont(gp.SMALL_FONT);
        g2.setColor(gp.DIM_CYAN);
        g2.drawString(String.format("Password Attempts: %d/3 remaining",
                gp.passwordAttemptsLeft), 20, 44);
        if (!gp.stickyNoteVisible) {
            g2.setColor(gp.AMBER);
            g2.drawString(String.format("Find %d more objects to reveal a clue...",
                    gp.ITEMS_BEFORE_STICKY - gp.objectsFound), 300, 44);
        }

        g2.setColor(new Color(10, 10, 20, 180));
        g2.fillRect(0, gp.H - 40, gp.W, 40);
        g2.setColor(new Color(0, 230, 255, 80));
        g2.drawLine(0, gp.H - 40, gp.W, gp.H - 40);

        g2.setFont(gp.SMALL_FONT);
        float pulse = (float)(0.5 + 0.5 * Math.sin(time * 3));
        g2.setColor(new Color(0, 200, 230, (int)(pulse * 200 + 55)));
        String hint = critical ? "HURRY! Time is almost up!"
                : "Click objects to investigate • Beware of decoy notes • Wrong clicks cost time!";
        g2.drawString(hint, 20, gp.H - 15);
    }

    // ── Feedback popup ──────────────────────────────────────────
    private void drawFeedbackPopup(Graphics2D g2, double time) {
        long age = System.currentTimeMillis() - gp.feedbackTime;
        float alpha = Math.max(0, 1 - age / 3000f);
        float yOffset = (float)(age * 0.02);

        ScreenRenderer.drawNeonPanel(g2, gp.W / 2 - 280, (int)(600 - yOffset),
                560, 50, gp.DIM_CYAN);
        g2.setFont(gp.DIALOGUE_FONT);
        g2.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
        ScreenRenderer.drawCenteredStatic(g2, gp.feedbackMsg, (int)(630 - yOffset), gp.W);
    }
}

