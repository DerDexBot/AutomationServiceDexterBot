package dev.automation.assistant.service;

import dev.automation.assistant.util.WindowsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der {@code WindowMonitorService} überwacht zyklisch das Vorhandensein eines bestimmten
 * Fensters anhand seines Titels und führt – falls erkannt – eine definierte
 * Automationsaktion aus.
 *
 * <p>
 * Diese Klasse läuft in einem separaten Thread, um das Haupt-UI nicht zu blockieren.
 * Wird das Ziel-Fenster erkannt, ruft sie den
 * {@link AutomationService} auf, um die festgelegte Reaktion (z. B. Schließen oder Klick)
 * auszuführen.
 * </p>
 *
 * <h2>Funktionsweise:</h2>
 * <ul>
 *   <li>Über {@link #startMonitoring(String, AutomationService)} wird ein Hintergrund-Thread gestartet.</li>
 *   <li>Dieser prüft alle 2 Sekunden, ob das Fenster geöffnet ist (via {@link WindowsUtils#isWindowOpen(String)}).</li>
 *   <li>Wird es gefunden, erfolgt der Aufruf von {@link AutomationService#handleWindow(String)}.</li>
 *   <li>Die Überwachung kann über {@link #stopMonitoring()} beendet werden.</li>
 * </ul>
 *
 * <h2>Thread-Sicherheit:</h2>
 * Das Flag {@code active} ist {@code volatile}, um konsistente Zustände zwischen
 * dem Haupt-Thread und dem Überwachungs-Thread sicherzustellen.
 *
 * <h2>Beispiel:</h2>
 * <pre>
 * WindowMonitorService monitor = new WindowMonitorService();
 * monitor.startMonitoring("Fehler", new AutomationService());
 * </pre>
 *
 * <h2>Logging:</h2>
 * Ereignisse werden über SLF4J/Logback protokolliert, um den Überwachungsstatus nachzuvollziehen.
 *
 * @author <PRIVATE_PERSON>
 * @version 1.0
 * @since 2025-10-14
 */
public class WindowMonitorService {

    /** Logger für Statusmeldungen und Fehlerprotokolle */
    private static final Logger log = LoggerFactory.getLogger(WindowMonitorService.class);

    /** Steuerungsflag für aktiven Überwachungs-Thread */
    private volatile boolean active = false;

    /**
     * Startet die Überwachung eines bestimmten Fensters.
     * <p>
     * Wird bereits überwacht, wird ein erneuter Startversuch ignoriert.
     * </p>
     *
     * @param windowTitle Der exakte Fenstertitel, nach dem gesucht werden soll.
     * @param automationService Die Service-Instanz, die beim Erkennen des Fensters ausgeführt wird.
     */
    public void startMonitoring(String windowTitle, AutomationService automationService) {
        if (active) {
            log.warn("Überwachung läuft bereits.");
            return;
        }

        active = true;

        Thread monitorThread = new Thread(() -> {
            log.info("Starte Überwachung für Fenster '{}'", windowTitle);
            while (active) {
                try {
                    if (WindowsUtils.isWindowOpen(windowTitle)) {
                        log.info("Fenster erkannt: {}", windowTitle);
                        automationService.handleWindow(windowTitle);
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Überwachungs-Thread wurde unterbrochen.");
                } catch (Exception ex) {
                    log.error("Fehler beim Überwachen des Fensters", ex);
                }
            }
        }, "Window-Monitor");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * Stoppt die laufende Fensterüberwachung.
     * <p>
     * Beendet den Hintergrund-Thread durch Setzen des Kontroll-Flags.
     * </p>
     */
    public void stopMonitoring() {
        active = false;
        log.info("Überwachung gestoppt.");
    }
}
