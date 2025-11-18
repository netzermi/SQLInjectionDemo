# SQL-Injection-Demo

## Beschreibung
Dieses Projekt demonstriert die Sicherheitslücke **SQL-Injection** anhand eines einfachen Java-Programms mit einer MySQL-Datenbank. Es zeigt sowohl unsichere als auch sichere Methoden zur Interaktion mit der Datenbank und erklärt, wie SQL-Injection-Angriffe funktionieren und wie man sie verhindern kann.

## Voraussetzungen
- Java 8 oder höher
- Maven
- MySQL-Datenbank (DB: jdbcdemo, User: user, Passwort: 12345)
- JDBC-Treiber für MySQL

## Aufbau der Datenbank
Die Datenbank enthält eine Tabelle `users` mit folgenden Feldern:
- `id`: Primärschlüssel
- `username`: Benutzername
- `password`: Passwort (gehasht mit SHA2)
- `created_at`: Erstellungszeitpunkt

Beispieldaten werden automatisch eingefügt.

## Funktionen
1. **Unsichere Methoden**:
    - `getUserByUsernameUnsafe`: Anfällig für SQL-Injection.
    - `vulnerableLogin`: Unsicherer Login mit String-Konkatenation.

2. **Sichere Methoden**:
    - `getUserByUsernameSafe`: Verwendet `PreparedStatement`, um SQL-Injection zu verhindern.
    - `safeLogin`: Sicherer Login mit gebundenen Parametern.

3. **Demonstration**:
    - Abruf aller Benutzer.
    - SQL-Injection-Angriffe und deren Auswirkungen.
    - Sichere Abfragen als Gegenbeispiel.

## Ausführung
1. Datenbankverbindung konfigurieren (URL, Benutzer, Passwort in `SqlInjectionDemo` anpassen).
2. Projekt mit Maven bauen und ausführen:
   ```bash
   mvn compile exec:java -Dexec.mainClass="Main"
