package dev.automation.assistant.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dienstprogrammklasse für native Windows-Interaktionen über JNA (Java Native Access).
 * <p>
 * Diese Klasse bietet Methoden zur direkten Kommunikation mit der Windows-API (user32.dll),
 * z. B. um Fenster zu finden, zu schliessen oder Buttons (Controls) per Nachricht anzusteuern.
 * </p>
 *
 * <h2>Verwendete Windows-API-Funktionen:</h2>
 * <ul>
 *     <li>{@code FindWindow} – Sucht ein Fenster anhand des Titels</li>
 *     <li>{@code PostMessage} – Sendet Nachrichten an Fenster (z. B. WM_CLOSE)</li>
 *     <li>{@code SendMessage} – Sendet Nachrichten an Controls (z. B. BM_CLICK)</li>
 *     <li>{@code WindowFromPoint} – Ermittelt das Fenster/Control unter dem Mauszeiger</li>
 * </ul>
 *
 * <h2>Typische Anwendungsfälle:</h2>
 * <ul>
 *     <li>Überprüfung, ob ein Fenster mit bestimmtem Titel geöffnet ist</li>
 *     <li>Automatisches Schliessen von Fenstern oder interagieren von Buttons</li>
 *     <li>Simulieren eines Klicks auf ein Control (z. B. Button)</li>
 * </ul>
 *
 * <h2>Technische Hinweise:</h2>
 * <ul>
 *     <li>Funktioniert nur auf Windows-Betriebssystemen</li>
 *     <li>Verwendet JNA (Java Native Access) zur Laufzeitbindung von user32.dll</li>
 *     <li>Alle Methoden sind statisch und Thread-sicher nutzbar</li>
 * </ul>
 *
 * @author <PRIVATE_PERSON>
 * @version 1.0
 * @since 2025-10-14
 */
public final class WindowsUtils {

    /** Logger für Debug- und Fehlerausgaben */
    private static final Logger log = LoggerFactory.getLogger(WindowsUtils.class);

    /** Windows-Meldung zum Simulieren eines Button-Klicks (BM_CLICK) */
    private static final int BM_CLICK = 0x00F5;

    /** Privater Konstruktor, um Instanziierung zu verhindern (Utility-Klasse) */
    private WindowsUtils() {}

    /**
     * Prüft, ob ein Fenster mit dem angegebenen Titel aktuell geöffnet ist.
     *
     * @param title Der exakte Fenstertitel (Gross-/Kleinschreibung beachten)
     * @return {@code true}, wenn ein Fenster mit diesem Titel existiert, sonst {@code false}
     */
    public static boolean isWindowOpen(String title) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, title);
        boolean open = hwnd != null;
        log.debug("Fensterprüfung für '{}': {}", title, open ? "offen" : "nicht gefunden");
        return open;
    }

    /**
     * Schliesst ein Fenster anhand seines Titels.
     * <p>
     * Sendet die Windows-Nachricht {@code WM_CLOSE} (0x0010) an das entsprechende Fenster.
     * Wenn kein Fenster mit dem angegebenen Titel gefunden wird, wird eine Warnung geloggt.
     * </p>
     *
     * @param title Der Fenstertitel, der geschlossen werden soll
     */
    public static void closeWindow(String title) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, title);
        if (hwnd != null) {
            log.info("Sende WM_CLOSE an Fenster '{}'", title);
            User32.INSTANCE.PostMessage(hwnd, 0x0010, null, null); // WM_CLOSE
        } else {
            log.warn("Fenster '{}' nicht gefunden – nichts zu schliessen", title);
        }
    }

    /**
     * Ermittelt das Fensterhandle (HWND) anhand eines Fenstertitels.
     *
     * @param title Fenstertitel, nach dem gesucht werden soll
     * @return Das Fensterhandle ({@link HWND}) oder {@code null}, falls nicht gefunden
     */
    public static HWND getWindowHandle(String title) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, title);
        if (hwnd == null) {
            log.warn("Kein Fenster mit Titel '{}' gefunden.", title);
        } else {
            log.info("Fensterhandle für '{}' erkannt: {}", title, hwnd);
        }
        return hwnd;
    }

    /**
     * Ermittelt das Control (z. B. Button) unter dem aktuellen Mauszeiger.
     * <p>
     * Diese Methode verwendet {@code WindowFromPoint}, um das Fensterhandle (HWND)
     * unter der aktuellen Cursorposition zurückzugeben.
     * </p>
     *
     * @return Das Handle des Controls unter dem Cursor oder {@code null}, falls kein Element erkannt wurde
     */
    public static HWND getControlUnderCursor() {
        POINT point = new POINT();
        User32.INSTANCE.GetCursorPos(point);
        HWND hwnd = WindowFromPoint(point);
        if (hwnd != null) {
            char[] buffer = new char[256];
            User32.INSTANCE.GetClassName(hwnd, buffer, 256);
            String className = Native.toString(buffer);
            log.info("Control unter Cursor: HWND={} Klasse='{}'", hwnd, className);
            return hwnd;
        }
        log.warn("Kein Control unter Cursor erkannt");
        return null;
    }

    /**
     * Führt einen simulierten Klick auf ein angegebenes Windows-Control aus.
     * <p>
     * Sendet die Nachricht {@code BM_CLICK} (0x00F5) an das Control, wodurch ein
     * Button-Klick ausgelöst wird.
     * </p>
     *
     * @param controlHandle Das Handle des Controls, das angeklickt werden soll
     */
    public static void clickControl(HWND controlHandle) {
        if (controlHandle == null) {
            log.warn("Kein Control-Handle angegeben – Klick abgebrochen");
            return;
        }
        log.info("Sende BM_CLICK an Control {}", controlHandle);
        User32.INSTANCE.SendMessage(controlHandle, BM_CLICK, new WPARAM(0), new LPARAM(0));
    }

    /**
     * Native Wrapper-Methode für {@code WindowFromPoint}, um das Fensterhandle unter
     * einer gegebenen Bildschirmkoordinate zu bestimmen.
     *
     * @param point Die Koordinaten (x/y) auf dem Bildschirm
     * @return Das {@link HWND}-Handle des Fensters oder Controls an dieser Position
     */
    private static HWND WindowFromPoint(POINT point) {
        return (HWND) Native.load("user32", User32Extra.class).WindowFromPoint(point);
    }

    /**
     * Zusätzliche native Schnittstelle für den Aufruf der Funktion {@code WindowFromPoint}
     * aus der Windows-API (user32.dll).
     */
    private interface User32Extra extends com.sun.jna.Library {
        /**
         * Ruft das Fensterhandle (HWND) an einer bestimmten Bildschirmposition ab.
         *
         * @param point Bildschirmkoordinate, deren zugehöriges Fenster ermittelt werden soll
         * @return Das Fensterhandle unter dem gegebenen Punkt
         */
        HWND WindowFromPoint(POINT point);
    }
}
