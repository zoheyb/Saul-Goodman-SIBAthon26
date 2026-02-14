import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;


    public class Firewall extends JFrame {

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                Firewall frame = new Firewall();
                frame.setVisible(true);
            });
        }

        public Firewall() {
            super("Cyber Detective — Enhanced Edition");
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
            LEVEL_SELECT,      // City map for next levels
            // NEW LEVEL 2 STATES
            LEVEL2_INTRO,      // Level 2 introduction
            LEVEL2_PHISHING,   // Level 2 main gameplay - email analysis
            LEVEL2_QUIZ,       // Level 2 quiz section
            LEVEL2_COMPLETE    // Level 2 completion screen
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
        //  PHISHING ELEMENT — clickable suspicious parts in emails
        // ═══════════════════════════════════════════════════════════════
        static class PhishingElement {
            String type; // "link", "sender", "urgency", "grammar"
            Rectangle bounds;
            boolean identified;
            String explanation;

            PhishingElement(String type, Rectangle bounds, String explanation) {
                this.type = type;
                this.bounds = bounds;
                this.identified = false;
                this.explanation = explanation;
            }
        }

        // ═══════════════════════════════════════════════════════════════
        //  MAIN GAME PANEL
        // ═══════════════════════════════════════════════════════════════
        static class GamePanel extends JPanel implements ActionListener, MouseListener, KeyListener {

            // ── Dimensions ─────────────────────────────────────────────
            private static final int W = 1100;
            private static final int H = 750;

            // ── Color Palette — Modern Cyber Theme ────────────────────
            private static final Color BG_DARK      = new Color(12, 12, 24);
            private static final Color DESK_BROWN   = new Color(90, 58, 28);
            private static final Color DESK_TOP     = new Color(130, 85, 40);
            private static final Color WOOD_GRAIN   = new Color(110, 70, 32);
            private static final Color NEON_CYAN    = new Color(80, 200, 255);
            private static final Color NEON_GREEN   = new Color(50, 230, 130);
            private static final Color CYBER_RED    = new Color(255, 70, 90);
            private static final Color AMBER        = new Color(255, 200, 60);
            private static final Color STICKY_YELLOW= new Color(255, 240, 100);
            private static final Color PANEL_BG     = new Color(16, 20, 36, 235);
            private static final Color SCREEN_BLUE  = new Color(25, 35, 70);
            private static final Color SCREEN_GLOW  = new Color(50, 70, 130);
            private static final Color PAPER_WHITE  = new Color(245, 240, 228);
            private static final Color DIM_CYAN     = new Color(40, 130, 160);
            private static final Color COFFEE_BROWN = new Color(70, 40, 15);
            private static final Color MUG_COLOR    = new Color(210, 210, 220);
            private static final Color KEY_COLOR    = new Color(55, 55, 65);
            // Level 2 specific colors
            private static final Color EMAIL_BG     = new Color(250, 250, 245);
            private static final Color PHISHING_RED = new Color(255, 100, 100);
            private static final Color SAFE_GREEN   = new Color(100, 255, 100);
            private static final Color WARNING_ORANGE = new Color(255, 170, 40);
            // Extra accent colors
            private static final Color PURPLE_GLOW  = new Color(140, 80, 255);
            private static final Color SOFT_WHITE   = new Color(220, 225, 240);

            // ── Fonts ──────────────────────────────────────────────────
            private static final Font TITLE_FONT    = new Font("Segoe UI", Font.BOLD, 44);
            private static final Font HUD_FONT      = new Font("Segoe UI", Font.BOLD, 15);
            private static final Font DIALOGUE_FONT = new Font("Segoe UI", Font.PLAIN, 16);
            private static final Font LABEL_FONT    = new Font("Segoe UI", Font.BOLD, 13);
            private static final Font STICKY_FONT   = new Font("Comic Sans MS", Font.BOLD, 14);
            private static final Font LAPTOP_FONT   = new Font("Consolas", Font.PLAIN, 18);
            private static final Font BIG_FONT      = new Font("Segoe UI", Font.BOLD, 36);
            private static final Font SMALL_FONT    = new Font("Segoe UI", Font.PLAIN, 11);
            private static final Font HANDWRITING   = new Font("Segoe Script", Font.PLAIN, 15);
            // Level 2 fonts
            private static final Font EMAIL_FONT    = new Font("Segoe UI", Font.PLAIN, 14);
            private static final Font EMAIL_HEADER  = new Font("Segoe UI", Font.BOLD, 16);

            // ── State ──────────────────────────────────────────────────
            private GameState state = GameState.INTRO_DIALOGUE;
            private boolean hasPassword = false;
            private boolean stickyNoteFound = false;
            private String passwordInput = "";
            private boolean passwordWrong = false;
            private int dialoguePhase = 0;
            private long stateStartTime;
            private int objectsFound = 0;
            private int totalObjects = 9;  // total clickable items on desk

            // ── COUNTDOWN TIMER (90 seconds to complete the mission) ───
            private static final int MISSION_TIME_LIMIT = 90; // seconds
            private int remainingSeconds = MISSION_TIME_LIMIT;
            private String failReason = "";

            // ── PASSWORD ATTEMPTS (only 3 tries!) ─────────────────────
            private int passwordAttemptsLeft = 3;

            // ── Sticky note only appears after finding 3+ objects ─────
            private boolean stickyNoteVisible = false;
            private static final int ITEMS_BEFORE_STICKY = 3;

            // ── Time penalty tracking ──────────────────────────────────
            private int penaltyFlashAlpha = 0;

            // ── NEW: Wrong clicks counter ──────────────────────────────
            private int wrongClicks = 0;

            // ── NEW: Hint system ───────────────────────────────────────
            private int hintsUsed = 0;
            private static final int MAX_HINTS = 3;
            private String currentHint = "";
            private long hintTime = 0;

            // ── NEW: Score & Ranking ───────────────────────────────────
            private int finalScore = 0;
            private String rankGrade = "";

            // ── NEW: Object magnifier ──────────────────────────────────
            private ClickableItem magnifiedItem = null;
            private long magnifyStartTime = 0;

            // ── NEW: Tutorial system ───────────────────────────────────
            private boolean showTutorial = true;
            private int tutorialStep = 0;

            // ── Dialogue Lines ─────────────────────────────────────────
            private final String[] introDialogues = {
                    "DISPATCH: \"Detective, we have a CODE RED situation.\"",
                    "\"A suspect's computer contains critical evidence.\"",
                    "\"The desk is a mess, but the password is hidden somewhere.\"",
                    "\"You have 90 SECONDS. Search every object carefully.\"",
                    "\"Warning: Wrong clicks waste time. Wrong passwords are limited.\"",
                    "\"Find the password, unlock the laptop. The clock is TICKING!\""
            };

            // ── LEVEL 2 DATA ───────────────────────────────────────────
            private final String[] level2Intro = {
                    "DISPATCH: \"Great work on Level 1, Detective!\"",
                    "\"Now we have a new threat: A PHISHING CAMPAIGN.\"",
                    "\"Employees are receiving suspicious emails.\"",
                    "\"Your task: Analyze 5 emails and identify which are PHISHING attempts.\"",
                    "\"You have 60 SECONDS. Each wrong answer costs time!\"",
                    "\"Click on each email to examine it, then classify it. Good luck!\""
            };

            // Email objects for Level 2
            static class Email {
                String sender;
                String subject;
                String content;
                boolean isPhishing;
                boolean analyzed;
                boolean classified;
                String classification; // "phishing" or "safe"
                Rectangle bounds;

                Email(String sender, String subject, String content, boolean isPhishing) {
                    this.sender = sender;
                    this.subject = subject;
                    this.content = content;
                    this.isPhishing = isPhishing;
                    this.analyzed = false;
                    this.classified = false;
                    this.classification = "";
                }
            }

            private List<Email> emails = new ArrayList<>();
            private Email selectedEmail = null;
            private int emailsAnalyzed = 0;
            private int emailsClassified = 0;
            private int correctClassifications = 0;
            private String level2Feedback = "";
            private long level2FeedbackTime = 0;
            private boolean showEmailDetail = false;
            private static final int LEVEL2_TIME_LIMIT = 60; // seconds
            private int level2RemainingSeconds = LEVEL2_TIME_LIMIT;
            private long level2StartTime;

            // NEW: Phishing element identification
            private List<PhishingElement> currentEmailElements = new ArrayList<>();
            private int elementsIdentified = 0;
            private int totalPhishingElements = 0;
            private int level2FinalScore = 0;
            private String level2RankGrade = "";

            // Quiz section for Level 2 (after emails)
            private int quizPhase = 0;
            private final String[][] quizQuestions = {
                    {"What's the safest action for a suspicious email?",
                            "A) Click the link to check",
                            "B) Delete it immediately",
                            "C) Forward to IT department",
                            "D) Reply and ask who sent it",
                            "C",
                            "IT departments have tools to analyze suspicious emails safely.",
                            "Clicking links or replying can expose you to malware or confirm",
                            "your email to attackers. Deleting removes evidence IT needs."},
                    {"Which is a sign of a phishing email?",
                            "A) Personalized greeting with your name",
                            "B) Urgent request to verify account",
                            "C) From a known colleague",
                            "D) Proper grammar and spelling",
                            "B",
                            "Phishers create urgency so you act without thinking.",
                            "Phrases like 'IMMEDIATELY' or 'your account will be closed'",
                            "are classic pressure tactics. Always verify through official channels."},
                    {"What should you check in an email sender address?",
                            "A) It looks familiar",
                            "B) The display name only",
                            "C) Full email domain for misspellings",
                            "D) It's from a popular service",
                            "C",
                            "Attackers use look-alike domains like 'paypa1.com' or 'g00gle.com'.",
                            "Display names can be faked easily. Always inspect the FULL email",
                            "domain character by character. One wrong letter = phishing!"},
                    {"If you clicked a phishing link, what should you do FIRST?",
                            "A) Panic and do nothing",
                            "B) Disconnect from network",
                            "C) Keep browsing",
                            "D) Shut down computer",
                            "B",
                            "Disconnecting stops malware from spreading or sending your data.",
                            "Then change passwords from a DIFFERENT device and report to IT.",
                            "Shutting down may lose forensic evidence needed for investigation."}
            };
            private int currentQuestion = 0;
            private String quizInput = "";
            private int quizScore = 0;
            private boolean quizFeedback = false;
            private boolean quizFeedbackCorrect = false;
            private String[] quizExplanation = {"", "", ""}; // 3-line explanation

            // ── Clickable Items on the Desk ────────────────────────────
            private final List<ClickableItem> deskItems = new ArrayList<>();

            // ── Hover tracking ─────────────────────────────────────────
            private String hoverName = "";
            private int mouseX = 0, mouseY = 0;
            private String feedbackMsg = "";
            private long feedbackTime = 0;

            // ── Floating animation ─────────────────────────────────────
            private final Random rng = new Random();

            // ── Level select pins ──────────────────────────────────────
            private final int[][] levelPins = {
                    {250, 300}, {450, 200}, {650, 350}, {350, 450}, {750, 250}
            };
            private final String[] levelNames = {
                    "The Messy Desk ★", "The Phishing Lab", "Server Room",
                    "CEO's Office", "The Dark Web Cafe"
            };
            private final boolean[] levelsUnlocked = {true, true, false, false, false};
            private final boolean[] levelsCompleted = {false, false, false, false, false};

            // ── Timer ──────────────────────────────────────────────────
            private long missionStartTime;
            private int elapsedSeconds;
            private final javax.swing.Timer gameTimer;

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
                        // Check hover over items
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
                        } else if (state == GameState.LEVEL2_PHISHING) {
                            // Check hover over emails
                            for (int i = 0; i < emails.size(); i++) {
                                Email email = emails.get(i);
                                if (email.bounds != null && email.bounds.contains(mouseX, mouseY) && !email.classified) {
                                    hoverName = "Email " + (i + 1);
                                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                    return;
                                }
                            }
                            // Check hover over phishing elements
                            for (PhishingElement elem : currentEmailElements) {
                                if (elem.bounds.contains(mouseX, mouseY) && !elem.identified) {
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
                initLevel2Emails();

                // 60 FPS game loop
                gameTimer = new javax.swing.Timer(16, this);
                gameTimer.start();
            }

 // ═══════════════════════════════════════════════════════════
            //  SOUND SYSTEM
            // ═══════════════════════════════════════════════════════════
            private void playSound(String soundType) {
                new Thread(() -> {
                    try {
                        if (soundType.equals("click")) {
                            Toolkit.getDefaultToolkit().beep();
                        } else if (soundType.equals("correct")) {
                            // Play ascending beep
                            for (int i = 0; i < 3; i++) {
                                Toolkit.getDefaultToolkit().beep();
                                Thread.sleep(50);
                            }
                        } else if (soundType.equals("wrong")) {
                            // Play descending tone
                            Toolkit.getDefaultToolkit().beep();
                            Thread.sleep(100);
                            Toolkit.getDefaultToolkit().beep();
                        } else if (soundType.equals("hint")) {
                            Toolkit.getDefaultToolkit().beep();
                            Thread.sleep(80);
                            Toolkit.getDefaultToolkit().beep();
                        }
                    } catch (Exception e) {}
                }).start();
            }

            // ═══════════════════════════════════════════════════════════
            //  HINT SYSTEM
            // ═══════════════════════════════════════════════════════════
            private void useHint() {
                if (hintsUsed >= MAX_HINTS) {
                    feedbackMsg = "No hints remaining!";
                    feedbackTime = System.currentTimeMillis();
                    playSound("wrong");
                    return;
                }

                // Deduct time
                missionStartTime -= 5000; // -5 seconds penalty
                hintsUsed++;
                hintTime = System.currentTimeMillis();
                playSound("hint");

                // Progressive hints
                if (hintsUsed == 1) {
                    currentHint = "💡 Look for yellow paper on the desk";
                } else if (hintsUsed == 2) {
                    currentHint = "💡 The password is on a sticky note - find 3+ objects first!";
                } else {
                    currentHint = "💡 Check the RIGHT side of the desk after finding items";
                }

                feedbackMsg = String.format("Hint used! -5 seconds. (%d/%d remaining)",
                        MAX_HINTS - hintsUsed, MAX_HINTS);
                feedbackTime = System.currentTimeMillis();
            }

            // ═══════════════════════════════════════════════════════════
            //  SCORE CALCULATION
            // ═══════════════════════════════════════════════════════════
            private void calculateScore() {
                int timeBonus = remainingSeconds * 10; // 10 points per second
                int accuracyBonus = (totalObjects - wrongClicks) * 50; // 50 points per correct click
                int speedBonus = 0;

                if (elapsedSeconds < 45) speedBonus = 500; // Under 45s
                else if (elapsedSeconds < 60) speedBonus = 300; // Under 60s
                else if (elapsedSeconds < 75) speedBonus = 100; // Under 75s

                int hintPenalty = hintsUsed * 100;
                int wrongPasswordPenalty = (3 - passwordAttemptsLeft) * 200;

                finalScore = Math.max(0, timeBonus + accuracyBonus + speedBonus - hintPenalty - wrongPasswordPenalty);

                // Calculate rank
                if (finalScore >= 1500 && elapsedSeconds < 45 && wrongClicks == 0) {
                    rankGrade = "S"; // Perfect
                } else if (finalScore >= 1200) {
                    rankGrade = "A"; // Excellent
                } else if (finalScore >= 900) {
                    rankGrade = "B"; // Good
                } else if (finalScore >= 600) {
                    rankGrade = "C"; // Average
                } else {
                    rankGrade = "D"; // Pass
                }
            }

            private void calculateLevel2Score() {
                int baseScore = correctClassifications * 200;
                int timeBonus = level2RemainingSeconds * 5;
                int quizBonus = quizScore * 100;
                int thoroughnessBonus = (elementsIdentified == totalPhishingElements && totalPhishingElements > 0) ? 500 : 0;

                level2FinalScore = Math.max(0, baseScore + timeBonus + quizBonus + thoroughnessBonus);

                // Calculate rank
                int totalPossible = emails.size();
                float accuracy = (float)correctClassifications / totalPossible;

                if (level2FinalScore >= 2000 && accuracy == 1.0 && quizScore == quizQuestions.length) {
                    level2RankGrade = "S"; // Perfect
                } else if (level2FinalScore >= 1500) {
                    level2RankGrade = "A"; // Excellent
                } else if (level2FinalScore >= 1000) {
                    level2RankGrade = "B"; // Good
                } else if (level2FinalScore >= 600) {
                    level2RankGrade = "C"; // Average
                } else {
                    level2RankGrade = "D"; // Pass
                }
            }

            /** Initialize all clickable objects on the desk */
            private void initDeskItems() {
                deskItems.clear();
                stickyNoteVisible = false;

                // ── The items are placed relative to the desk area ─────
                // STICKY NOTE (the REAL password!) — HIDDEN until 3+ items found
                deskItems.add(new ClickableItem("Sticky Note",
                        780, 370, 90, 75,
                        "A yellow sticky note! It reads: 'Password: admin123'",
                        STICKY_YELLOW));

                // DECOY STICKY NOTE 1 — wrong password!
                deskItems.add(new ClickableItem("Red Sticky Note",
                        100, 200, 75, 60,
                        "A red sticky note reads: 'WiFi: office2024'. Not what you need!",
                        CYBER_RED));

                // DECOY STICKY NOTE 2 — wrong password!
                deskItems.add(new ClickableItem("Blue Sticky Note",
                        900, 475, 70, 55,
                        "A blue note: 'login: guest / pass: welcome1'. Hmm, not for this laptop...",
                        NEON_CYAN));

                // COFFEE MUG — red herring
                deskItems.add(new ClickableItem("Coffee Mug",
                        160, 340, 65, 80,
                        "A half-empty coffee mug. Still warm... Someone was here recently.",
                        MUG_COLOR));

                // SCATTERED PAPERS — clue hint
                deskItems.add(new ClickableItem("Scattered Papers",
                        400, 410, 140, 60,
                        "Password Policy: 'DO NOT write passwords down!' — Someone didn't listen...",
                        PAPER_WHITE));

                // USB DRIVE — suspicious
                deskItems.add(new ClickableItem("USB Drive",
                        620, 490, 50, 20,
                        "A suspicious unmarked USB drive. Never plug unknown USBs! [-5 sec penalty]",
                        CYBER_RED));

                // PHONE — info
                deskItems.add(new ClickableItem("Smartphone",
                        280, 440, 55, 90,
                        "2FA notifications DISABLED. The suspect clearly ignores security!",
                        new Color(40, 40, 50)));

                // DRAWER HANDLE — new item
                deskItems.add(new ClickableItem("Desk Drawer",
                        60, 520, 110, 40,
                        "A locked drawer. You hear something rattle inside. Key not found.",
                        DESK_BROWN));

                // LAPTOP — main interaction (always last for z-order)
                deskItems.add(new ClickableItem("Laptop",
                        430, 180, 250, 180,
                        "", // handled separately
                        SCREEN_GLOW));
            }

            /** Initialize Level 2 phishing emails */
            private void initLevel2Emails() {
                emails.clear();

                // Email 1 - Phishing
                emails.add(new Email(
                        "security@paypa1.com",
                        "URGENT: Your account has been suspended!",
                        "Dear valued customer,\n\nYour PayPal account has been suspended due to suspicious activity.\nClick here to verify your account immediately: http://paypa1-verify.com\n\nFailure to verify within 24 hours will result in permanent closure.",
                        true
                ));

                // Email 2 - Safe
                emails.add(new Email(
                        "newsletter@techweekly.com",
                        "Your Tech Weekly Digest",
                        "Hello subscriber,\n\nHere are this week's top tech stories:\n- AI breakthrough announced\n- New smartphone releases\n- Cybersecurity tips\n\nTo unsubscribe, click here.\n\nThanks,\nTech Weekly Team",
                        false
                ));

                // Email 3 - Phishing
                emails.add(new Email(
                        "it-support@company.com",
                        "Password Expiry Notice",
                        "Your password will expire in 24 hours.\nPlease keep your current password.\n\nTo reset your password, visit: http://company-security.com/reset\n\nIT Support",
                        true
                ));

                // Email 4 - Phishing
                emails.add(new Email(
                        "hr@companysystems.net",
                        "Important: Update your direct deposit",
                        "Dear Employee,\n\nOur payroll system has been upgraded.\nPlease update your direct deposit information immediately to ensure timely payment.\n\nLogin here: http://companysystems-payroll.com\n\nHR Department",
                        true
                ));

                // Email 5 - Safe
                emails.add(new Email(
                        "no-reply@slack.com",
                        "New messages in #general",
                        "Hi there,\n\nYou have 3 new messages in the #general channel:\n- @alice: Team meeting at 2pm\n- @bob: Project update\n- @carol: Lunch plans?\n\nView conversation: https://slack.com/messages\n\nThanks,\nSlack Team",
                        false
                ));

                // Set bounds for emails (grid layout)
                int startX = 150;
                int startY = 200;
                int width = 160;
                int height = 100;
                int spacing = 20;

                for (int i = 0; i < emails.size(); i++) {
                    int col = i % 3;
                    int row = i / 3;
                    int x = startX + col * (width + spacing);
                    int y = startY + row * (height + spacing);
                    emails.get(i).bounds = new Rectangle(x, y, width, height);
                }
            }

            private void analyzeEmailElements(Email email) {
                currentEmailElements.clear();

                if (!email.isPhishing) return;

                // Check sender domain (in email detail view coordinates)
                if (email.sender.contains("paypa1") || email.sender.contains("1")) {
                    currentEmailElements.add(new PhishingElement(
                            "sender",
                            new Rectangle(340, 285, 200, 20),
                            "Misspelled: 'paypa1.com' uses '1' not 'l'"
                    ));
                }

                if (email.content.contains("http://")) {
                    currentEmailElements.add(new PhishingElement(
                            "link",
                            new Rectangle(340, 380, 150, 20),
                            "Unencrypted HTTP link (not HTTPS)"
                    ));
                }

                if (email.content.contains("URGENT") || email.content.contains("24 hours") || email.content.contains("immediately")) {
                    currentEmailElements.add(new PhishingElement(
                            "urgency",
                            new Rectangle(340, 340, 200, 20),
                            "Creates false urgency pressure"
                    ));
                }

                totalPhishingElements = currentEmailElements.size();
            }

            // ═══════════════════════════════════════════════════════════
