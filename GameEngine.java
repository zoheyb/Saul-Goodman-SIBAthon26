import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║   PERSON 1: GameEngine.java                                 ║
 * ║   Main class — JFrame, GameState, ClickableItem,            ║
 * ║   Game Loop, Input Handling, State Machine                   ║
 * ║                                                              ║
 * ║   Depends on: DeskRenderer.java, ScreenRenderer.java        ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class GameEngine extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameEngine frame = new GameEngine();
            frame.setVisible(true);
        });
    }

    public GameEngine() {
        super("Cyber Detective — Level 1: The Messy Desk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    // ═══════════════════════════════════════════════════════════════
    //  GAME STATES
    // ═══════════════════════════════════════════════════════════════
    enum GameState {
        INTRO_DIALOGUE,    // Detective briefing at start
        DESK_SEARCH,       // Hidden object — find the sticky note
        LAPTOP_LOGIN,      // Enter password screen
        LEVEL_COMPLETE,    // Success screen
        MISSION_FAILED,    // Time ran out or too many wrong passwords
        LEVEL_SELECT       // City map for next levels
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLICKABLE ITEM — represents a hidden object on the desk
    // ═══════════════════════════════════════════════════════════════
    static class ClickableItem {
        String name;
        Rectangle bounds;
        String dialogue;
        boolean found;
        Color highlightColor;

        ClickableItem(String name, int x, int y, int w, int h,
                      String dialogue, Color highlight) {
            this.name = name;
            this.bounds = new Rectangle(x, y, w, h);
            this.dialogue = dialogue;
            this.found = false;
            this.highlightColor = highlight;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  MAIN GAME PANEL — game loop + input + rendering dispatch
    // ═══════════════════════════════════════════════════════════════
    static class GamePanel extends JPanel implements ActionListener, MouseListener, KeyListener {

        // ── Dimensions ─────────────────────────────────────────────
        static final int W = 1100;
        static final int H = 750;

        // ── Color Palette — Dark Detective Theme ───────────────────
        static final Color BG_DARK      = new Color(18, 18, 28);
        static final Color DESK_BROWN   = new Color(101, 67, 33);
        static final Color DESK_TOP     = new Color(139, 90, 43);
        static final Color WOOD_GRAIN   = new Color(120, 75, 35);
        static final Color NEON_CYAN    = new Color(0, 230, 255);
        static final Color NEON_GREEN   = new Color(0, 255, 120);
        static final Color CYBER_RED    = new Color(255, 50, 70);
        static final Color AMBER        = new Color(255, 190, 0);
        static final Color STICKY_YELLOW= new Color(255, 240, 100);
        static final Color PANEL_BG     = new Color(20, 25, 40, 230);
        static final Color SCREEN_BLUE  = new Color(30, 40, 80);
        static final Color SCREEN_GLOW  = new Color(60, 80, 140);
        static final Color PAPER_WHITE  = new Color(240, 235, 220);
        static final Color DIM_CYAN     = new Color(0, 120, 130);
        static final Color COFFEE_BROWN = new Color(80, 50, 20);
        static final Color MUG_COLOR    = new Color(200, 200, 210);
        static final Color KEY_COLOR    = new Color(60, 60, 70);

        // ── Fonts ──────────────────────────────────────────────────
        static final Font TITLE_FONT    = new Font("Consolas", Font.BOLD, 42);
        static final Font HUD_FONT      = new Font("Consolas", Font.BOLD, 16);
        static final Font DIALOGUE_FONT = new Font("Consolas", Font.PLAIN, 16);
        static final Font LABEL_FONT    = new Font("Consolas", Font.BOLD, 14);
        static final Font STICKY_FONT   = new Font("Comic Sans MS", Font.BOLD, 14);
        static final Font LAPTOP_FONT   = new Font("Consolas", Font.PLAIN, 18);
        static final Font BIG_FONT      = new Font("Consolas", Font.BOLD, 36);
        static final Font SMALL_FONT    = new Font("Consolas", Font.PLAIN, 12);
        static final Font HANDWRITING   = new Font("Segoe Script", Font.PLAIN, 15);

        // ── State ──────────────────────────────────────────────────
        GameState state = GameState.INTRO_DIALOGUE;
        boolean hasPassword = false;
        boolean stickyNoteFound = false;
        String passwordInput = "";
        boolean passwordWrong = false;
        int dialoguePhase = 0;
        long stateStartTime;
        int objectsFound = 0;
        int totalObjects = 9;

        // ── COUNTDOWN TIMER (90 seconds) ───────────────────────────
        static final int MISSION_TIME_LIMIT = 90;
        int remainingSeconds = MISSION_TIME_LIMIT;
        String failReason = "";

        // ── PASSWORD ATTEMPTS (3 tries) ────────────────────────────
        int passwordAttemptsLeft = 3;

        // ── Sticky note visibility ─────────────────────────────────
        boolean stickyNoteVisible = false;
        static final int ITEMS_BEFORE_STICKY = 3;

        // ── Time penalty tracking ──────────────────────────────────
        int penaltyFlashAlpha = 0;

        // ── Dialogue Lines ─────────────────────────────────────────
        final String[] introDialogues = {
                "DISPATCH: \"Detective, we have a CODE RED situation.\"",
                "\"A suspect's computer contains critical evidence.\"",
                "\"The desk is a mess, but the password is hidden somewhere.\"",
                "\"You have 90 SECONDS. Search every object carefully.\"",
                "\"Warning: Wrong clicks waste time. Wrong passwords are limited.\"",
                "\"Find the password, unlock the laptop. The clock is TICKING!\""
        };

        // ── Clickable Items on the Desk ────────────────────────────
        final List<ClickableItem> deskItems = new ArrayList<>();

        // ── Hover tracking ─────────────────────────────────────────
        String hoverName = "";
        int mouseX = 0, mouseY = 0;
        String feedbackMsg = "";
        long feedbackTime = 0;

        // ── Random ─────────────────────────────────────────────────
        final Random rng = new Random();

        // ── Level select pins ──────────────────────────────────────
        final int[][] levelPins = {
                {250, 300}, {450, 200}, {650, 350}, {350, 450}, {750, 250}
        };
        final String[] levelNames = {
                "The Messy Desk ★", "The Phishing Lab", "Server Room",
                "CEO's Office", "The Dark Web Cafe"
        };

        // ── Timer ──────────────────────────────────────────────────
        long missionStartTime;
        int elapsedSeconds;
        final javax.swing.Timer gameTimer;

        // ── Sub-renderers (Person 2 & Person 3) ────────────────────
        final DeskRenderer deskRenderer;
        final ScreenRenderer screenRenderer;

        // ═══════════════════════════════════════════════════════════
        //  CONSTRUCTOR
        // ═══════════════════════════════════════════════════════════
        GamePanel() {
            setPreferredSize(new Dimension(W, H));
            setBackground(BG_DARK);
            setFocusable(true);
            addMouseListener(this);
            addKeyListener(this);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    hoverName = "";
                    if (state == GameState.DESK_SEARCH) {
                        for (ClickableItem item : deskItems) {
                            if (item.bounds.contains(mouseX, mouseY) && !item.found) {
                                hoverName = item.name;
                                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                return;
                            }
                        }
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            });

            stateStartTime = System.currentTimeMillis();
            initDeskItems();

            // Create renderers
            deskRenderer = new DeskRenderer(this);
            screenRenderer = new ScreenRenderer(this);

            // 60 FPS game loop
            gameTimer = new javax.swing.Timer(16, this);
            gameTimer.start();
        }

        /** Initialize all clickable objects on the desk */
        void initDeskItems() {
            deskItems.clear();
            stickyNoteVisible = false;

            deskItems.add(new ClickableItem("Sticky Note",
                    780, 370, 90, 75,
                    "A yellow sticky note! It reads: 'Password: admin123'",
                    STICKY_YELLOW));

            deskItems.add(new ClickableItem("Red Sticky Note",
                    100, 200, 75, 60,
                    "A red sticky note reads: 'WiFi: office2024'. Not what you need!",
                    CYBER_RED));

            deskItems.add(new ClickableItem("Blue Sticky Note",
                    900, 475, 70, 55,
                    "A blue note: 'login: guest / pass: welcome1'. Hmm, not for this laptop...",
                    NEON_CYAN));

            deskItems.add(new ClickableItem("Coffee Mug",
                    160, 340, 65, 80,
                    "A half-empty coffee mug. Still warm... Someone was here recently.",
                    MUG_COLOR));

            deskItems.add(new ClickableItem("Scattered Papers",
                    400, 410, 140, 60,
                    "Password Policy: 'DO NOT write passwords down!' — Someone didn't listen...",
                    PAPER_WHITE));

            deskItems.add(new ClickableItem("USB Drive",
                    620, 490, 50, 20,
                    "A suspicious unmarked USB drive. Never plug unknown USBs! [-5 sec penalty]",
                    CYBER_RED));

            deskItems.add(new ClickableItem("Smartphone",
                    280, 440, 55, 90,
                    "2FA notifications DISABLED. The suspect clearly ignores security!",
                    new Color(40, 40, 50)));

            deskItems.add(new ClickableItem("Desk Drawer",
                    60, 520, 110, 40,
                    "A locked drawer. You hear something rattle inside. Key not found.",
                    DESK_BROWN));

            deskItems.add(new ClickableItem("Laptop",
                    430, 180, 250, 180,
                    "",
                    SCREEN_GLOW));
        }

        // ═══════════════════════════════════════════════════════════
        //  GAME LOOP (60 FPS)
        // ═══════════════════════════════════════════════════════════
        @Override
        public void actionPerformed(ActionEvent e) {
            if (state == GameState.DESK_SEARCH || state == GameState.LAPTOP_LOGIN) {
                long now = System.currentTimeMillis();
                elapsedSeconds = (int)((now - missionStartTime) / 1000);
                remainingSeconds = MISSION_TIME_LIMIT - elapsedSeconds;

                if (remainingSeconds <= 0) {
                    remainingSeconds = 0;
                    failReason = "TIME'S UP! You failed to unlock the laptop in time.";
                    state = GameState.MISSION_FAILED;
                }

                if (objectsFound >= ITEMS_BEFORE_STICKY && !stickyNoteVisible) {
                    stickyNoteVisible = true;
                    feedbackMsg = "Something appeared on the desk! A sticky note just fell out!";
                    feedbackTime = System.currentTimeMillis();
                }

                if (penaltyFlashAlpha > 0) penaltyFlashAlpha -= 3;
            }
            repaint();
        }

        // ═══════════════════════════════════════════════════════════
        //  RENDERING — dispatches to Person 2 & Person 3
        // ═══════════════════════════════════════════════════════════
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            g2.setColor(BG_DARK);
            g2.fillRect(0, 0, W, H);

            switch (state) {
                case INTRO_DIALOGUE -> screenRenderer.renderIntroDialogue(g2);
                case DESK_SEARCH    -> deskRenderer.renderDeskSearch(g2);
                case LAPTOP_LOGIN   -> screenRenderer.renderLaptopLogin(g2);
                case LEVEL_COMPLETE -> screenRenderer.renderLevelComplete(g2);
                case MISSION_FAILED -> screenRenderer.renderMissionFailed(g2);
                case LEVEL_SELECT   -> screenRenderer.renderLevelSelect(g2);
            }
        }

        // ═══════════════════════════════════════════════════════════
        //  INPUT HANDLING
        // ═══════════════════════════════════════════════════════════
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX(), y = e.getY();

            if (state == GameState.DESK_SEARCH) {
                for (ClickableItem item : deskItems) {
                    if (item.name.equals("Sticky Note") && !stickyNoteVisible) continue;

                    if (item.bounds.contains(x, y)) {
                        if (item.name.equals("Laptop")) {
                            state = GameState.LAPTOP_LOGIN;
                            passwordInput = "";
                            passwordWrong = false;
                            return;
                        }
                        if (!item.found) {
                            item.found = true;
                            objectsFound++;
                            feedbackMsg = item.dialogue;
                            feedbackTime = System.currentTimeMillis();

                            if (item.name.equals("Sticky Note")) {
                                stickyNoteFound = true;
                                hasPassword = true;
                            }
                        } else {
                            feedbackMsg = "Already investigated: " + item.name;
                            feedbackTime = System.currentTimeMillis();
                        }
                        return;
                    }
                }
                // Clicked empty area — TIME PENALTY
                missionStartTime -= 3000;
                penaltyFlashAlpha = 80;
                feedbackMsg = "Nothing here! -3 seconds penalty. Click carefully!";
                feedbackTime = System.currentTimeMillis();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            switch (state) {
                case INTRO_DIALOGUE -> {
                    if (key == KeyEvent.VK_ENTER) {
                        if (dialoguePhase < introDialogues.length - 1) {
                            dialoguePhase++;
                            stateStartTime = System.currentTimeMillis();
                        } else {
                            state = GameState.DESK_SEARCH;
                            missionStartTime = System.currentTimeMillis();
                        }
                    }
                }
                case DESK_SEARCH -> {
                    // No key actions in search mode
                }
                case LAPTOP_LOGIN -> {
                    if (key == KeyEvent.VK_ESCAPE) {
                        state = GameState.DESK_SEARCH;
                    } else if (key == KeyEvent.VK_ENTER) {
                        if (passwordInput.equalsIgnoreCase("admin123")) {
                            state = GameState.LEVEL_COMPLETE;
                        } else {
                            passwordAttemptsLeft--;
                            passwordWrong = true;
                            passwordInput = "";
                            if (passwordAttemptsLeft <= 0) {
                                failReason = "Too many wrong passwords! The laptop locked permanently.";
                                state = GameState.MISSION_FAILED;
                            }
                        }
                    } else if (key == KeyEvent.VK_BACK_SPACE) {
                        if (!passwordInput.isEmpty()) {
                            passwordInput = passwordInput.substring(0,
                                    passwordInput.length() - 1);
                        }
                        passwordWrong = false;
                    }
                }
                case LEVEL_COMPLETE -> {
                    if (key == KeyEvent.VK_ENTER) {
                        state = GameState.LEVEL_SELECT;
                    }
                }
                case MISSION_FAILED -> {
                    if (key == KeyEvent.VK_R) {
                        state = GameState.INTRO_DIALOGUE;
                        dialoguePhase = 0;
                        stateStartTime = System.currentTimeMillis();
                        hasPassword = false;
                        stickyNoteFound = false;
                        stickyNoteVisible = false;
                        passwordInput = "";
                        passwordWrong = false;
                        passwordAttemptsLeft = 3;
                        objectsFound = 0;
                        remainingSeconds = MISSION_TIME_LIMIT;
                        feedbackMsg = "";
                        initDeskItems();
                    } else if (key == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
                case LEVEL_SELECT -> {
                    if (key == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (state == GameState.LAPTOP_LOGIN) {
                char c = e.getKeyChar();
                if (Character.isLetterOrDigit(c) && passwordInput.length() < 20) {
                    passwordInput += c;
                    passwordWrong = false;
                }
            }
        }

        @Override public void keyReleased(KeyEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}
