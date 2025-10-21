package dev.automation.assistant.ui;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.POINT;
import dev.automation.assistant.service.AutomationService;
import dev.automation.assistant.service.WindowMonitorService;
import dev.automation.assistant.util.WindowsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Hauptfenster des Automation Assistant.
 * <p>
 * Diese Swing-Oberflaeche erlaubt es, Fenster zu registrieren, Buttons auszuwaehlen
 * und automatisierte Interaktionen zu starten. Neu kann der Benutzer auch direkt
 * das Ziel-Fenster per Mauszeiger auswaehlen, um gleichnamige Fenster (z. B. Noah 4)
 * eindeutig zu identifizieren.
 * </p>
 */
public class MainWindow extends JFrame {


    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    /** Fenster, für das eine automatische Buttonaktion definiert wurde */
    private String autoActionWindowTitle = null;

    /** Gespeicherte Position des Buttons für automatische Aktionen */
    private Point autoActionButtonPosition = null;

    private HWND autoActionWindowHandle = null;

    private HWND targetHwnd;
    /** Handle des zuletzt bestätigten Dialogfensters */
    private HWND confirmedDialogHandle = null;
    private final JTextField windowTitleField;
    private final JLabel statusLabel;

    private String targetWindow;
    private Point savedButtonPosition;

    private final WindowMonitorService monitorService;
    private final AutomationService automationService;

    public MainWindow() {
        super("Automation Assistant");

        this.monitorService = new WindowMonitorService();
        this.automationService = new AutomationService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titelzeile
        JLabel titleLabel = new JLabel("Automation Assistant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Fenstertitel
        JLabel windowLabel = new JLabel("Fenstertitel:");
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(windowLabel, gbc);

        windowTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(windowTitleField, gbc);

        // Buttons (rechtsbündig)
        gbc.weightx = 0;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        JButton selectWindowButton = new JButton("Fenster auswählen");
        selectWindowButton.addActionListener(e -> onSelectWindow());
        gbc.gridy++;
        mainPanel.add(selectWindowButton, gbc);

        JButton selectButton = new JButton("Button auswählen");
        selectButton.addActionListener(e -> onSelectButtonPosition());
        gbc.gridy++;
        mainPanel.add(selectButton, gbc);

        /**
         * outdated *Placeholder*
         */
//        JButton clickButton = new JButton("Gespeicherten Button klicken");
//        clickButton.addActionListener(e -> onClickSavedButton());
//        gbc.gridy++;
//        mainPanel.add(clickButton, gbc);

        JButton startButton = new JButton("Bot starten");
        startButton.addActionListener(e -> onStartBot());
        gbc.gridy++;
        mainPanel.add(startButton, gbc);

        // Statuslabel
        statusLabel = new JLabel("Bereit");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);
        setVisible(true);
        log.info("UI erfolgreich initialisiert");
    }



    /** Fenster interaktiv per Maus auswählen */
    private void onSelectWindow() {
        statusLabel.setText("Bewege Maus auf gewünschtes Fenster (5s) …");
        log.info("Starte interaktive Fensterauswahl …");

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                HWND hwnd = WindowsUtils.getWindowUnderCursor();
                if (hwnd != null) {
                    String title = WindowsUtils.getWindowTitle(hwnd);
                    SwingUtilities.invokeLater(() -> {
                        targetHwnd = hwnd;                  // << handle speichern
                        targetWindow = title;
                        windowTitleField.setText(title);
                        statusLabel.setText("Fenster erkannt: " + title);
                        log.info("Fenster ausgewählt: '{}' ({})", title, hwnd);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Kein Fenster erkannt.");
                        log.warn("Fenster-Auswahl fehlgeschlagen");
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Fenster-Auswahl-Thread unterbrochen", e);
            }
        }, "Window-Select").start();
    }


    /** Button-Position (x,y) speichern */
    private void onSelectButtonPosition() {
        if (targetWindow == null || targetWindow.isBlank()) {
            statusLabel.setText("Bitte zuerst ein Fenster registrieren.");
            log.warn("Button-Auswahl abgebrochen – kein Fenster registriert");
            return;
        }

        statusLabel.setText("Bewege Maus auf gewünschten Button (8s) …");
        log.info("Starte Button-Positionsauswahl …");

        new Thread(() -> {
            try {
                Thread.sleep(8000);
                POINT p = new POINT();
                User32.INSTANCE.GetCursorPos(p);
                savedButtonPosition = new Point(p.x, p.y);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Position gespeichert: " + p.x + ", " + p.y);
                    log.info("Button-Position gespeichert bei {},{}", p.x, p.y);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Button-Auswahl-Thread unterbrochen", e);
            }
        }, "Button-Select").start();
    }

    /** Klick auf gespeicherte Position simulieren */
    private void onClickSavedButton() {
        if (savedButtonPosition == null) {
            statusLabel.setText("Keine Position gespeichert.");
            log.warn("Klick abgebrochen – keine gespeicherte Position vorhanden");
            return;
        }

        try {
            Robot robot = new Robot();
            robot.mouseMove(savedButtonPosition.x, savedButtonPosition.y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            statusLabel.setText("Klick ausgeführt bei: " + savedButtonPosition.x + ", " + savedButtonPosition.y);
            log.info("Simulierter Klick ausgeführt bei {},{}", savedButtonPosition.x, savedButtonPosition.y);
        } catch (Exception e) {
            log.error("Fehler beim Simulieren des Mausklicks", e);
            statusLabel.setText("Klick fehlgeschlagen");
        }
    }

    /** Startet die Fensterüberwachung */
    private void onStartBot() {
        if (targetWindow == null || targetWindow.isBlank()) {
            statusLabel.setText("Kein Fenster registriert.");
            log.warn("Botstart abgebrochen – kein Fenster registriert");
            return;
        }

        statusLabel.setText("Überwachung aktiv für: " + targetWindow);
        log.info("Starte Bot-Überwachung für '{}'", targetWindow);
        monitorService.startMonitoring(targetHwnd, targetWindow, this::onNewWindowDetected);
    }
    /**
     * Wird aufgerufen, wenn ein neues Fenster erkannt wurde.
     */
    private void onNewWindowDetected(HWND hwnd) {
        String title = WindowsUtils.getWindowTitle(hwnd);
        log.info("Fenster erkannt: '{}'", title);

        // Prüfen, ob für dieses Fenster bereits eine automatische Aktion existiert
        if (autoActionWindowTitle != null && autoActionWindowTitle.equals(title) && autoActionButtonPosition != null) {
            log.info("Automatische Aktion aktiv: Drücke gespeicherten Button für '{}'", title);
            performAutoClick(autoActionButtonPosition);
            return;
        }

        // Neues oder unbekanntes Fenster → Benutzer fragen
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Neues Fenster erkannt: '" + title + "'\nMöchtest du den Button registrieren, der beim nächsten Mal automatisch gedrückt werden soll?",
                    "Neues Fenster erkannt",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                statusLabel.setText("Bitte Maus über gewünschten Button bewegen (5s)...");
                log.info("Starte Button-Registrierung für '{}'", title);

                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        com.sun.jna.platform.win32.WinDef.POINT p = new com.sun.jna.platform.win32.WinDef.POINT();
                        User32.INSTANCE.GetCursorPos(p);
                        autoActionWindowTitle = title;
                        autoActionButtonPosition = new Point(p.x, p.y);

                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Button registriert für '" + title + "' bei (" + p.x + ", " + p.y + ")");
                            log.info("Button registriert: {} ({},{})", title, p.x, p.y);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                log.info("Fenster '{}' ignoriert.", title);
            }
        });
    }
    //TODO Mouse Perform click set in a Thread, because the robot use the User Mouse
    /**
     * Führt einen automatischen Klick aus, ohne die Benutzermaus zu bewegen.
     * Wenn möglich, wird BM_CLICK direkt an das Fensterhandle gesendet.
     * Falls kein gültiges Handle existiert, wird als Fallback die Robot-Maus verwendet.
     */
    private void performAutoClick(Point position) {
        new Thread(() -> {
            try {
                if (autoActionWindowHandle != null) {
                    log.info("Sende virtuellen Klick an Fensterhandle {}", autoActionWindowHandle);
                    WindowsUtils.sendVirtualClick(autoActionWindowHandle);
                    SwingUtilities.invokeLater(() ->
                            statusLabel.setText("Virtueller Klick an Fenster gesendet")
                    );
                } else {
                    log.warn("Kein gültiges Fensterhandle – verwende Robot als Fallback.");
                    Robot robot = new Robot();
                    robot.mouseMove(position.x, position.y);
                    Thread.sleep(200);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(200);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    SwingUtilities.invokeLater(() ->
                            statusLabel.setText("Fallback: Klick mit Robot bei " + position.x + ", " + position.y)
                    );
                }

            } catch (Exception e) {
                log.error("Fehler beim automatischen Klick", e);
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Klick fehlgeschlagen")
                );
            }
        }, "AutoClick-Thread").start();
    }


}
