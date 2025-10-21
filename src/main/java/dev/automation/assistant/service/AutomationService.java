package dev.automation.assistant.service;

import com.sun.jna.platform.win32.WinDef;
import dev.automation.assistant.util.WindowsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der {@code AutomationService} kapselt die Aktionen, die automatisch ausgeführt werden sollen,
 * sobald ein bestimmtes Fenster erkannt wird.
 * <p>
 * Aktuell beschränkt sich die Automatisierung darauf, ein Fenster mit einem bestimmten Titel
 * zu schliessen. Die Funktionalität kann jedoch leicht erweitert werden, z. B. um Klicks auf
 * gespeicherte Positionen oder das Senden von Tastatureingaben.
 * </p>
 *
 * <h2>Verwendung:</h2>
 * Diese Klasse wird typischerweise vom {@link dev.automation.assistant.service.WindowMonitorService}
 * aufgerufen, wenn ein Ziel-Fenster detektiert wurde.
 *
 * <pre>
 * AutomationService automationService = new AutomationService();
 * automationService.handleWindow("Fehler");
 * </pre>
 *
 * <h2>Logging:</h2>
 * Alle Aktionen werden über {@link org.slf4j.Logger} protokolliert (Logback-Integration).
 *
 * @author <PRIVATE_PERSON>
 * @version 1.0
 * @since 2025-10-14
 */
public class AutomationService {

    /** Logger-Instanz für Status- und Fehlerausgaben */
    private static final Logger log = LoggerFactory.getLogger(AutomationService.class);

    /**
     * Führt die definierte Automationsaktion für ein bestimmtes Fenster aus.
     * <p>
     * Standardverhalten: Schließt das übergebene Fenster über {@link WindowsUtils#closeWindow(String)}.
     * </p>
     *
     * @param title Der exakte Fenstertitel, der geschlossen werden soll (Gross-/Kleinschreibung relevant).
     */
    public void handleWindow(String title) {
        log.info("Automatische Aktion: Fenster '{}' wird geschlossen.", title);
        WindowsUtils.closeWindow(title);
    }
//    public void handleWindowByHandle(WinDef.HWND hwnd, String title) {
//        log.info("Automatische Aktion: Fenster '{}' wird gezielt geschlossen.", title);
//        WindowsUtils.closeWindow(hwnd, title);
//    }

}
