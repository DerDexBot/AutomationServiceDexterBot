package dev.automation.assistant;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import dev.automation.assistant.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.net.URL;

/**
 * Einstiegspunkt der Anwendung "Automation Assistant".
 * <p>
 * Diese Klasse initialisiert die Anwendung, konfiguriert das Logging (Logback)
 * und startet anschliessend die grafische Benutzeroberfläche ({@link MainWindow}).
 * </p>
 *
 * <h2>Hauptfunktionen:</h2>
 * <ul>
 *     <li>Zeigt beim Start einen Dialog zur Auswahl der Logdatei an.</li>
 *     <li>Setzt den Pfad der Logdatei in der System-Property {@code log.file}.</li>
 *     <li>Lädt die Logback-Konfiguration neu, damit die Datei sofort aktiv ist.</li>
 *     <li>Startet danach das Swing-Hauptfenster.</li>
 * </ul>
 *
 * <h2>Logging:</h2>
 * Alle Systemmeldungen werden über SLF4J/Logback ausgegeben.
 * Der Benutzer kann selbst bestimmen, wo die Logdatei gespeichert wird
 * (Standard: {@code ./logs/automation-assistant.log}).
 *
 * <h2>Technische Details:</h2>
 * <ul>
 *     <li>GUI wird im Event Dispatch Thread gestartet (SwingUtilities.invokeLater).</li>
 *     <li>Logback wird nachträglich mit {@link JoranConfigurator} reinitialisiert.</li>
 * </ul>
 *
 * @author <PRIVATE_PERSON>
 * @version 1.0
 * @since 2025-10-14 19:13
 */
public class ApplicationLauncher {

    /** Logger für Status- und Initialisierungsmeldungen */
    private static final Logger log = LoggerFactory.getLogger(ApplicationLauncher.class);

    /**
     * Einstiegspunkt der Anwendung.
     * <p>
     * Öffnet beim Start einen Datei-Dialog für die Logdatei,
     * initialisiert das Logging-System und startet anschliessend
     * die Hauptoberfläche.
     * </p>
     *
     * @param args Kommandozeilenargumente (werden nicht verwendet)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                File logFile = chooseLogFile();

                if (logFile != null) {
                    // falls keine Endung -> .log hinzufügen
                    if (!logFile.getName().toLowerCase().endsWith(".log") &&
                            !logFile.getName().toLowerCase().endsWith(".txt")) {
                        logFile = new File(logFile.getAbsolutePath() + ".log");
                    }

                    // Ordner anlegen
                    logFile.getParentFile().mkdirs();
                    System.setProperty("log.file", logFile.getAbsolutePath());
                    reconfigureLogback();

                    log.info("Logdatei gesetzt auf: {}", logFile.getAbsolutePath());
                } else {
                    // Fallback, falls Benutzer Abbruch wählt
                    File defaultFile = new File("logs/automation-assistant.log");
                    defaultFile.getParentFile().mkdirs();
                    System.setProperty("log.file", defaultFile.getAbsolutePath());
                    reconfigureLogback();
                    log.info("Standard-Logdatei erstellt: {}", defaultFile.getAbsolutePath());
                }

                // Start des Hauptfensters
                new MainWindow();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Öffnet einen Datei-Dialog, über den der Benutzer eine Logdatei auswählen oder erstellen kann.
     * <p>
     * Unterstützt die Dateiformate <code>.log</code> und <code>.txt</code>.
     * Wird der Dialog abgebrochen, liefert die Methode {@code null}.
     * </p>
     *
     * @return Die ausgewählte Logdatei oder {@code null}, falls keine Datei gewählt wurde
     */
    private static File chooseLogFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Wähle oder erstelle die Log-Datei");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Dateifilter (.log und .txt)
        FileNameExtensionFilter filter =
                new FileNameExtensionFilter("Log-Dateien (*.log, *.txt)", "log", "txt");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File("automation-assistant.log"));

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /**
     * Lädt die Logback-Konfiguration nachträglich neu,
     * damit die geänderte Logdatei sofort aktiv wird.
     * <p>
     * Dazu wird die aktuelle {@link LoggerContext}-Instanz zurückgesetzt
     * und die Konfigurationsdatei {@code logback.xml} erneut eingelesen.
     * </p>
     */
    private static void reconfigureLogback() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset(); // alte Konfiguration entfernen

            URL configUrl = ApplicationLauncher.class.getClassLoader().getResource("logback.xml");
            if (configUrl != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                configurator.doConfigure(configUrl);
                StatusPrinter.printInCaseOfErrorsOrWarnings(context);
            }
        } catch (Exception ex) {
            System.err.println("Fehler beim Reinitialisieren von Logback: " + ex.getMessage());
        }
    }
}
