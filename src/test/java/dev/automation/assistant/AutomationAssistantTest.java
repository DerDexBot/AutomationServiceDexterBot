package dev.automation.assistant;

import dev.automation.assistant.service.AutomationService;
import dev.automation.assistant.service.WindowMonitorService;
import dev.automation.assistant.ui.MainWindow;
import dev.automation.assistant.util.WindowsUtils;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die Kernfunktionen des Projekts "Automation Assistant".
 * <p>
 * Diese Tests prüfen grundlegende Komponenten (Service, Monitoring, Utilities)
 * auf Funktionalität, Stabilität und Fehlerfreiheit.
 * </p>
 *
 * <h2>Testarten:</h2>
 * <ul>
 *     <li>Unit-Tests (z. B. {@link AutomationService})</li>
 *     <li>Integrationstests mit Systemaufrufen (z. B. {@link WindowsUtils})</li>
 *     <li>UI-Tests (Initialisierung des {@link MainWindow})</li>
 * </ul>
 *
 * <p>
 * Hinweis: Tests, die auf Windows-API zugreifen, funktionieren nur auf Windows-Systemen.
 * </p>
 *
 * @author <PRIVATE_PERSON>
 * @version 1.0
 * @since 2025-10-14
 */
public class AutomationAssistantTest {

    @BeforeAll
    static void setup() {
        System.out.println("Starte Tests für Automation Assistant ...");
    }

    @Test
    @DisplayName("AutomationService: HandleWindow ohne Fehler")
    void testAutomationServiceHandleWindow() {
        AutomationService service = new AutomationService();
        assertDoesNotThrow(() -> service.handleWindow("FakeWindowTitle"));
    }

//    @Test
//    @DisplayName("WindowMonitorService: Start/Stop ohne Exception")
//    void testWindowMonitorServiceStartStop() throws InterruptedException {
//        WindowMonitorService monitor = new WindowMonitorService();
//        AutomationService automation = new AutomationService();
//
//        monitor.startMonitoring("UnbekanntesFenster", automation);
//        Thread.sleep(2000);
//        monitor.stopMonitoring();
//
//        assertTrue(true); // Wenn keine Exception, gilt als erfolgreich
//    }

    @Test
    @DisplayName("WindowsUtils: Nicht existierendes Fenster korrekt erkannt")
    void testWindowsUtilsIsWindowOpen() {
        boolean open = WindowsUtils.isWindowOpen("FensterDasNichtExistiert");
        assertFalse(open, "Fenster sollte als nicht offen erkannt werden");
    }

    @Test
    @DisplayName("MainWindow: UI erfolgreich initialisiert")
    void testMainWindowInitialization() {
        MainWindow window = new MainWindow();
        assertNotNull(window, "UI-Fenster sollte erstellt werden");
        assertEquals("Automation Assistant", window.getTitle());
        window.dispose();
    }

    @AfterAll
    static void teardown() {
        System.out.println("Alle Tests abgeschlossen.");
    }
}
