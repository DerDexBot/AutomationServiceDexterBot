# Automation Service Dexter Bots

Ein leichtgewichtiges Java-Tool zur Automatisierung von Windows-Fenstern.  
Das Programm erkennt bestimmte Fenster, fuehrt definierte Aktionen aus und kann Fenster automatisch schliessen oder Buttons klicken.  
Es wurde entwickelt mit **Java Swing**, **Logback** und **JNA**.

---
## Version 2

Das Programm wurde überarbeitet, da es erstmals auf ein spezifisches Fesnter einstellbar war, was jedoch zu Komplikationen führte, wenn das Child-Window gleich hies.

Neue Anwendung von dem Update:

- Programm öffnen
- Fenster Scannen (Button -> Maus auf Fenster zeigen lassen) und warten bis das Fenster registriert ist (siehe Meldung in dem Client)
- Start Bot
- Dann warten bis das Child-Window aufgeht
- Auf Child-Window des Bots den Button registrieren (mit OK) -> Maus auf den gewünschten Button zeigen -> warten
- Erstmals manuell schliessen
- Danach geht alles vollautomatisch

## Erweiterungen TODO

- Maus von dem User-Mouse entkoppelt
- Button Pixel innerhalb des Fensters berechnen
- evtl wiederholte clicks bis das Child-Window closed ist

## Projektbeschreibung

**Automation Service Bots** dient zur Automatisierung von Desktop-Interaktionen auf Windows-Systemen.  
Das Tool erkennt geoeffnete Fenster anhand ihres Titels und fuehrt daraufhin Aktionen aus, wie:

- Fenster automatisch schliessen oder benutzerdefinierte Interaktionen
- Einen bestimmten Button klicken (z. B. *Merge* oder *Skip*)
- Mauspositionen speichern und automatisch klicken
- Logging in Datei und Konsole
- Dynamische erkennung, auch wenn Fenster oder Buttons nicht exakt in den gleichen Pixel erscheinen.

Das Projekt ist modular aufgebaut und fuer weitere Automatisierungsaufgaben erweiterbar.



## Features

| Kategorie | Beschreibung |
|------------|---------------|
| **Fenstererkennung** | Erkennt und ueberwacht geoeffnete Fenster anhand des Titels |
| **Benutzerdefinierte Interaktionen** | Fuehrt automatisch definierte Aktionen aus, z. B. Fenster schliessen, Buttons klicken oder andere Eingaben ausloesen |
| **Button-Automatisierung** | Klickt auf Buttons innerhalb eines Fensters (z. B. *Merge*, *OK*) |
| **Mauspositionsspeicher** | Speichert Mauspositionen fuer wiederkehrende Klicks |
| **Logging-System** | Logausgabe in Datei und Konsole mit Logback |
| **Benutzeroberflaeche** | Moderne Java Swing GUI mit Statusanzeige |
| **Plattform** | Entwickelt fuer Windows mit JNA-Integration |

---

## Installation

### 1. Voraussetzungen
- Java 17 oder hoeher
- Maven installiert (`mvn -v`)
- Windows Betriebssystem
- Java IDE

### 2. Projekt klonen
```bash
git clone https://github.com/DeinGitHubName/AutomationServiceDexterBots.git
cd AutomationServiceDexterBots

3. Build erstellen

mvn clean package

4. Starten

java -jar target/automation-assistant-1.0.0.jar

Beim Start kannst du den Speicherort der Logdatei waehlen.
Anschliessend startet automatisch die Benutzeroberflaeche.
Benutzeroberflaeche

Nach dem Start siehst du das Hauptfenster mit:

    Eingabefeld fuer Fenstertitel

    Buttons zum Registrieren, Speichern, Klicken und Starten

    Statusanzeige fuer aktuelle Aktionen

    Echtzeit-Logging in der Konsole

Funktionsweise

    Fenster registrieren

        Gib den Fenstertitel ein (z. B. DreamBot 3 Launcher)

        Das Tool merkt sich diesen Titel und ueberwacht das Fenster

    Button speichern

        Bewege die Maus auf den gewuenschten Button (z. B. Merge)

        Nach 2 Sekunden wird die Position automatisch gespeichert

    Bot starten

        Der Bot ueberwacht das registrierte Fenster

        Sobald es erscheint, wird automatisch die gespeicherte Aktion ausgefuehrt

Logging

Das Programm verwendet Logback fuer alle Logeintraege.
Du kannst beim Start die Logdatei selbst waehlen (.log oder .txt).

Beispielhafte Logausgabe:

2025-10-14 18:22:10 INFO  [main] ApplicationLauncher - Logdatei gesetzt auf: logs/automation-assistant.log
2025-10-14 18:22:12 INFO  [AWT-EventQueue-0] MainWindow - Fenster registriert: DreamBot 3 Launcher
2025-10-14 18:22:16 INFO  [Window-Monitor] WindowMonitorService - Fenster erkannt: DreamBot 3 Launcher
2025-10-14 18:22:16 INFO  [Window-Monitor] AutomationService - Fenster 'DreamBot 3 Launcher' wird geschlossen.

Tests

Das Projekt enthaelt Unit-Tests mit JUnit 5.
Tests ausfuehren

mvn test

Gepruefte Bereiche

    Funktionsfaehigkeit von AutomationService

    Stabilitaet der Fensterueberwachung

    Thread-Sicherheit

    UI-Initialisierung

## Erweiterungen

Die Doku beschreit das das Programm **leicht erweiterbar** ist für **komplexere Benutzerinteraktionen** (z. B. Textfelder, Auswahlmenüs, etc.).
Beispiel: Automatisches Ausloesen eines Buttons innerhalb eines Dialogfensters:

```java
HWND parent = User32.INSTANCE.FindWindow(null, "Dateien zusammenfuehren");
HWND button = User32.INSTANCE.FindWindowEx(parent, null, null, "Merge");
if (button != null) {
    User32.INSTANCE.SendMessage(button, 0x00F5, new WPARAM(0), new LPARAM(0)); // BM_CLICK
}

Projektstruktur

src/
 ├─ main/
 │   ├─ java/dev/automation/assistant/
 │   │   ├─ ApplicationLauncher.java        → Startpunkt, Logging-Setup, UI-Start
 │   │   ├─ service/
 │   │   │   ├─ AutomationService.java      → Fuehrt Aktionen aus (Fenster schliessen, Button klicken)
 │   │   │   └─ WindowMonitorService.java   → Ueberwacht Fensterstatus
 │   │   ├─ ui/
 │   │   │   └─ MainWindow.java             → Swing-Benutzeroberflaeche
 │   │   └─ util/
 │   │       └─ WindowsUtils.java           → JNA-basierte Windows-Funktionen
 │   └─ resources/
 │       └─ logback.xml                     → Logging-Konfiguration
 └─ test/
     └─ java/dev/automation/assistant/
         └─ AutomationAssistantTest.java    → JUnit-Tests fuer Kernfunktionen

Lizenz

Dieses Projekt ist frei verwendbar fuer Bildungs- und Demonstrationszwecke.
Alle Abhaengigkeiten (JNA, Logback, JUnit) sind Open Source.
Autor

DerDexBot
Projekt: Automation Service Dexter Bots
Entwickelt als praxisnahes Java-Projekt zur Desktop-Automatisierung.

Vorschau Life in dem Docs Ordner.





