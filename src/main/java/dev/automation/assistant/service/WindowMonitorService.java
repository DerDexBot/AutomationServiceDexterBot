package dev.automation.assistant.service;

import com.sun.jna.platform.win32.WinDef.HWND;
import dev.automation.assistant.util.WindowsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Ueberwacht Fenster und meldet neu erscheinende Fenster mit gleichem Titel.
 */
public class WindowMonitorService {

    private static final Logger log = LoggerFactory.getLogger(WindowMonitorService.class);
    private volatile boolean active = false;
    private HWND mainWindowHandle;

    /**
     * Startet die Fensterueberwachung.
     *
     * @param targetHandle Das Hauptfenster
     * @param windowTitle Der Fenstertitel
     * @param onNewWindow Callback, wenn ein neues Fenster erkannt wird
     */
    public void startMonitoring(HWND targetHandle, String windowTitle, Consumer<HWND> onNewWindow) {
        if (active) {
            log.warn("Ueberwachung laeuft bereits.");
            return;
        }

        active = true;
        mainWindowHandle = targetHandle;

        Thread monitorThread = new Thread(() -> {
            log.info("Starte Ueberwachung fuer Fenster '{}' ({})", windowTitle, targetHandle);
            HWND lastDetected = null;

            while (active) {
                try {
                    HWND found = WindowsUtils.findWindowByTitle(windowTitle);

                    if (found != null && !found.equals(mainWindowHandle) && !found.equals(lastDetected)) {
                        log.info("Neues Unterfenster erkannt: {}", found);
                        onNewWindow.accept(found);
                        lastDetected = found;
                    }

                    Thread.sleep(5000);
                } catch (Exception ex) {
                    log.error("Fehler beim Ueberwachen der Fenster", ex);
                }
            }
        }, "Window-Monitor");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

//    public void stopMonitoring() {
//        active = false;
//        log.info("Ueberwachung gestoppt.");
//    }
}