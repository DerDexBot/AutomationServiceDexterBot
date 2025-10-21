package dev.automation.assistant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility-Klasse fuer Thread-bezogene Funktionen.
 * Beinhaltet sichere Sleep-Methoden mit Logging.
 */
public final class ThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    private ThreadUtils() {
        // utility class
    }

    /**
     * Fuehrt einen sicheren Sleep-Vorgang durch.
     *
     * @param millis Dauer in Millisekunden
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread wurde waehrend Sleep unterbrochen ({} ms)", millis);
        }
    }

    /**
     * Convenience-Methode fuer 2 Sekunden Wartezeit.
     */
    public static void sleepShort() {
        sleep(2000);
    }

    /**
     * Convenience-Methode fuer laengere Wartezeit (5 s).
     */
    public static void sleepLong() {
        sleep(5000);
    }
}
