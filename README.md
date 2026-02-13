# SymptomTracker

Android-App zum Erfassen und Speichern von Herz-Symptom-Einträgen.

## Funktionen

- **Startseite**: Zeigt die letzten Einträge und einen FAB-Button zum Erstellen neuer Einträge
- **Neuer Eintrag**: Formular mit:
  - Schweregrad (1–10, Slider)
  - Art des Schmerzes (Stechend, Dumpf, Pochend, Stichartig + Textfeld für Sonstige)
  - Datum & Uhrzeit (mit „Jetzt“-Button)
  - Eingenommene Medikation
  - Auslöser
  - Notiz

- **Persistente Speicherung**: Alle Einträge werden dauerhaft in einer Room-Datenbank gespeichert und bleiben nach App-Neustart erhalten.

## Projekt einrichten

1. **Android Studio** (Ladybug oder neuer) installieren
2. **File → Open** und den Ordner `SymptomTracker` auswählen
3. Android Studio synchronisiert das Projekt und lädt Abhängigkeiten
4. Bei Bedarf: **File → Sync Project with Gradle Files**
5. Projekt mit **Build → Make Project** bauen
6. App auf Gerät oder Emulator mit **Run** (grünes Dreieck) starten

## Technologie-Stack

- Kotlin
- Jetpack Compose (UI)
- Room (Datenbank)
- Material Design 3
