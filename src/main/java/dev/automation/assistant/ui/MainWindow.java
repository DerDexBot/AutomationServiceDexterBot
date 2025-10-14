package dev.automation.assistant.ui;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.POINT;
import dev.automation.assistant.service.AutomationService;
import dev.automation.assistant.service.WindowMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Das {@code MainWindow} stellt die grafische Benutzeroberfläche (GUI) für den
 * Automation Assistant bereit.
 * <p>
 * Über dieses Fenster kann der Benutzer:
 * <ul>
 *     <li>den Fenstertitel eines Zielprogramms eingeben und registrieren,</li>
 *     <li>eine Bildschirmposition (z. B. einen Button) auswählen,</li>
 *     <li>die gespeicherte Position testen (Mausklick ausführen),</li>
 *     <li>und die automatische Fensterüberwachung starten.</li>
 * </ul>
 * </p>
 *
 * <h2>Architektur:</h2>
 * <ul>
 *     <li>GUI-Komponenten: Java Swing</li>
 *     <li>Layout: {@link GridBagLayout} für saubere Ausrichtung</li>
 *     <li>Services:
 *         <ul>
 *             <li>{@link WindowMonitorService} – überwacht Fenster</li>
 *             <li>{@link AutomationService} – führt definierte Aktionen aus</li>
 *         </ul>
 *     </li>
 *     <li>System-Integration: {@link User32} (JNA) für Windows-API-Aufrufe</li>
 * </ul>
 *
 * <h2>Logging:</h2>
 * Alle Benutzeraktionen, Systemmeldungen und Fehler werden mit SLF4J / Logback
 * in der Konsole und in einer Logdatei protokolliert.
 *
 * @author <PRIVATE_PERSON>
 * @version 1.0
 * @since 2025-10-14
 */
public class MainWindow extends JFrame {

    /** Logger für Status- und Fehlerausgaben */
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    /** Eingabefeld für den Fenstertitel, der überwacht werden soll */
    private final JTextField windowTitleField;

    /** Statusanzeige im unteren Bereich des Fensters */
    private final JLabel statusLabel;

    /** Gespeicherter Fenstertitel, auf den sich die Überwachung bezieht */
    private String targetWindow;

    /** Vom Benutzer gespeicherte Mausposition (z. B. Button-Koordinaten) */
    private Point savedButtonPosition;

    /** Service für die Fensterüberwachung */
    private final WindowMonitorService monitorService;

    /** Service für automatisierte Aktionen (Fenster schließen, Klicks etc.) */
    private final AutomationService automationService;

    /**
     * Erstellt und initialisiert die Benutzeroberfläche.
     * <p>
     * Das Fenster ist nicht skalierbar und nutzt ein modernes, einfaches Layout.
     * </p>
     */
    public MainWindow() {
        super("Automation Assistant");

        this.monitorService = new WindowMonitorService();
        this.automationService = new AutomationService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        // Hauptlayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titel
        JLabel titleLabel = new JLabel("Automation Assistant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Beschriftung Fenstertitel
        JLabel windowLabel = new JLabel("Fenstertitel:");
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(windowLabel, gbc);

        // Eingabefeld
        windowTitleField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(windowTitleField, gbc);

        // Buttons rechtsbündig anordnen
        gbc.weightx = 0;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;

        JButton registerButton = new JButton("Fenster registrieren");
        registerButton.addActionListener(e -> onRegisterManual());
        gbc.gridy++;
        mainPanel.add(registerButton, gbc);

        JButton selectButton = new JButton("Button auswählen");
        selectButton.addActionListener(e -> onSelectButtonPosition());
        gbc.gridy++;
        mainPanel.add(selectButton, gbc);

        JButton clickButton = new JButton("Gespeicherten Button klicken");
        clickButton.addActionListener(e -> onClickSavedButton());
        gbc.gridy++;
        mainPanel.add(clickButton, gbc);

        JButton startButton = new JButton("Bot starten");
        startButton.addActionListener(e -> onStartBot());
        gbc.gridy++;
        mainPanel.add(startButton, gbc);

        // Statusanzeige
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

    /**
     * Registriert manuell ein Fenster anhand des eingegebenen Titels.
     * <p>
     * Wenn kein Text eingegeben wurde, wird der Benutzer gewarnt und der Vorgang abgebrochen.
     * </p>
     */
    private void onRegisterManual() {
        String input = windowTitleField.getText().trim();
        if (input.isEmpty()) {
            statusLabel.setText("Bitte Fenstertitel eingeben.");
            log.warn("Keine Fenstereingabe – Registrierung abgebrochen");
            return;
        }
        targetWindow = input;
        statusLabel.setText("Fenster registriert: " + targetWindow);
        log.info("Fenster manuell registriert: '{}'", targetWindow);
    }

    /**
     * Speichert die Bildschirmposition des Buttons, auf den später automatisch geklickt werden soll.
     * <p>
     * Nach Klick auf den Button hat der Benutzer zwei Sekunden Zeit, den Mauszeiger
     * auf das gewünschte UI-Element zu bewegen.
     * Anschließend wird die aktuelle Cursorposition gespeichert.
     * </p>
     */
    private void onSelectButtonPosition() {
        if (targetWindow == null || targetWindow.isBlank()) {
            statusLabel.setText("Bitte zuerst Fenstertitel registrieren.");
            log.warn("Button-Auswahl abgebrochen – kein Fenster registriert");
            return;
        }

        statusLabel.setText("Bewege Maus auf gewünschten Button (2s) …");
        log.info("Starte Button-Positionsauswahl …");

        new Thread(() -> {
            try {
                Thread.sleep(2000);
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

    /**
     * Führt einen simulierten Mausklick an der gespeicherten Position aus.
     * <p>
     * Wird verwendet, um den gespeicherten Punkt manuell zu testen oder
     * den Klick durch den Bot zu reproduzieren.
     * </p>
     */
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

    /**
     * Startet die automatische Fensterüberwachung und -interaktion.
     * <p>
     * Der {@link WindowMonitorService} überwacht das registrierte Fenster
     * und ruft den {@link AutomationService} auf, sobald es erkannt wird.
     * </p>
     */
    private void onStartBot() {
        if (targetWindow == null || targetWindow.isBlank()) {
            statusLabel.setText("Kein Fenster registriert.");
            log.warn("Botstart abgebrochen – kein Fenster registriert");
            return;
        }

        statusLabel.setText("Überwachung aktiv für: " + targetWindow);
        log.info("Starte Bot-Überwachung für '{}'", targetWindow);
        monitorService.startMonitoring(targetWindow, automationService);
    }
}
