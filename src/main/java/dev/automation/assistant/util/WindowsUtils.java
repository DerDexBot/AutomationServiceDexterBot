package dev.automation.assistant.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinDef.LPARAM;

/**
 * Dienstklasse fuer Windows-spezifische Aktionen mit JNA.
 * Unterstuetzt das Finden, Schliessen und Interagieren mit Fenstern und Controls.
 */
public final class WindowsUtils {

    private static final Logger log = LoggerFactory.getLogger(WindowsUtils.class);
    private static final int BM_CLICK = 0x00F5; // Button Click Message

    private WindowsUtils() {}

    /**
     * Prueft, ob ein Fenster mit gegebenem Titel aktuell offen ist.
     */
    public static boolean isWindowOpen(String title) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, title);
        boolean open = hwnd != null;
        log.debug("Fensterpruefung fuer '{}': {}", title, open ? "offen" : "nicht gefunden");
        return open;
    }

    /**
     * Schliesst ein Fenster anhand seines Titels.
     */
    public static void closeWindow(String title) {
        HWND hwnd = User32.INSTANCE.FindWindow(null, title);
        if (hwnd != null) {
            log.info("Sende WM_CLOSE an Fenster '{}'", title);
            User32.INSTANCE.PostMessage(hwnd, 0x0010, null, null);
        } else {
            log.warn("Fenster '{}' nicht gefunden – nichts zu schliessen", title);
        }
    }


    /**
     * Erkennt das Fenster unter dem Mauszeiger (interaktive Auswahl).
     * Der Benutzer kann das Ziel-Fenster einfach mit der Maus anvisieren.
     */
    public static HWND getWindowUnderCursor() {
        POINT point = new POINT();
        User32.INSTANCE.GetCursorPos(point);

        HWND hwnd = User32Extra.INSTANCE.WindowFromPoint(point);
        HWND target = hwnd;

        if (hwnd == null) {
            log.warn("Kein Control unter Cursor erkannt – versuche Fallback-Erkennung...");
        } else {
            // Hochlaufen zum Hauptfenster
            HWND parent;
            while ((parent = User32.INSTANCE.GetParent(hwnd)) != null) {
                hwnd = parent;
            }
            target = hwnd;

            // Prüfen, ob ein Fenstertitel existiert
            char[] buffer = new char[512];
            User32.INSTANCE.GetWindowText(target, buffer, 512);
            String title = Native.toString(buffer);

            if (title != null && !title.isBlank()) {
                log.info("Fenster unter Cursor erkannt: '{}' ({})", title, target);
                return target;
            } else {
                log.debug("WindowFromPoint hat kein sichtbares Fenster gefunden. Verwende Fallback...");
            }
        }

        //Fallback: mit EnumWindows prüfen
        final HWND[] result = new HWND[1];
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            WinDef.RECT rect = new WinDef.RECT();
            User32.INSTANCE.GetWindowRect(hWnd, rect);

            if (point.x >= rect.left && point.x <= rect.right && point.y >= rect.top && point.y <= rect.bottom) {
                result[0] = hWnd;
                return false; // stop enumeration
            }
            return true; // continue
        }, Pointer.NULL);

        if (result[0] != null) {
            char[] titleBuf = new char[512];
            User32.INSTANCE.GetWindowText(result[0], titleBuf, 512);
            String fallbackTitle = Native.toString(titleBuf);
            log.info("Fenster erkannt (Fallback): '{}' ({})", fallbackTitle, result[0]);
            return result[0];
        }

        log.warn("Kein Fenster unter Cursor erkannt – auch Fallback erfolglos");
        return null;
    }

    /**
     * Erkennt das Steuerelement (Button etc.) unter dem Mauszeiger.
     */
//    public static HWND getControlUnderCursor() {
//        POINT point = new POINT();
//        User32.INSTANCE.GetCursorPos(point);
//        HWND hwnd = User32Extra.INSTANCE.WindowFromPoint(point);
//        if (hwnd != null) {
//            char[] buffer = new char[256];
//            User32.INSTANCE.GetClassName(hwnd, buffer, 256);
//            String className = Native.toString(buffer);
//            log.info("Control unter Cursor: HWND={} Klasse='{}'", hwnd, className);
//            return hwnd;
//        }
//        log.warn("Kein Control unter Cursor erkannt");
//        return null;
//    }

    public static void sendVirtualClick(HWND hwnd) {
        if (hwnd == null) {
            log.warn("Kein gültiges Fensterhandle für virtuellen Klick.");
            return;
        }

        final int BM_CLICK = 0x00F5; // Button-Click Message
        User32.INSTANCE.SendMessage(hwnd, BM_CLICK, new WPARAM(0), new LPARAM(0));

        log.info("Virtueller Klick an Fensterhandle {} gesendet.", hwnd);
    }

    /**
     * Simuliert einen Klick auf ein Control (z. B. Button).
     */
//    public static void clickControl(HWND controlHandle) {
//        if (controlHandle == null) {
//            log.warn("Kein Control-Handle angegeben – Klick abgebrochen");
//            return;
//        }
//        log.info("Sende BM_CLICK an Control {}", controlHandle);
//        User32.INSTANCE.SendMessage(controlHandle, BM_CLICK, new WPARAM(0), new LPARAM(0));
//    }

    /**
     * Hilfsfunktion – gibt den Fenstertitel eines Handles zurueck.
     */
    public static String getWindowTitle(HWND hwnd) {
        if (hwnd == null) return null;
        char[] buffer = new char[512];
        User32.INSTANCE.GetWindowText(hwnd, buffer, 512);
        return Native.toString(buffer);
    }

    /**
     * Erweiterte User32-Schnittstelle fuer WindowFromPoint().
     * Diese Funktion ist in JNA nicht direkt in User32 enthalten und wird hier nachgeladen.
     */
    private interface User32Extra extends com.sun.jna.Library {
        User32Extra INSTANCE = Native.load("user32", User32Extra.class);
        HWND WindowFromPoint(POINT point);
    }
//    public static boolean isWindowStillValid(HWND hwnd) {
//        return hwnd != null && User32.INSTANCE.IsWindow(hwnd);
//    }
    public static void closeWindow(HWND hwnd, String title) {
        if (hwnd != null && User32.INSTANCE.IsWindow(hwnd)) {
            log.info("Sende WM_CLOSE an Fenster '{}' ({})", title, hwnd);
            User32.INSTANCE.PostMessage(hwnd, 0x0010, null, null);
        } else {
            log.warn("Fenster '{}' ist nicht mehr gültig oder nicht vorhanden", title);
        }

    }
    public static HWND findWindowByTitle(String title) {
        return User32.INSTANCE.FindWindow(null, title);
    }

}
