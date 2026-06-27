# JavaFX Class Project

This starter project uses Java 21 and a local copy of the JavaFX 21 SDK. It does
not require Maven or Gradle.

## Setup On Linux

Download JavaFX once:

```bash
./scripts/download-javafx.sh
./scripts/download-sqlite-jdbc.sh
```

## Setup On Windows

Install Java 21 first, then open Command Prompt or PowerShell in this project folder.

Run the setup wizard:

```bat
scripts\setup-windows.bat
```

The wizard checks Java, downloads the Windows JavaFX SDK and SQLite JDBC driver,
builds the project, runs the database test, and can launch the app.

Manual setup is also available:

```bat
scripts\download-javafx-windows.bat
scripts\download-sqlite-jdbc.bat
```

Important: JavaFX contains operating-system-specific files. If this project was copied from
Linux and `lib\javafx-sdk-21.0.8` already exists, but the Windows script says it found a Linux
JavaFX SDK, delete `lib\javafx-sdk-21.0.8` and run `scripts\download-javafx-windows.bat` again.

## Run On Linux

```bash
./scripts/run.sh
```

## Run On Windows

```bat
scripts\run.bat
```

The application stores its data in `data/clinic.db`. The first launch creates the
database and inserts the demo accounts and appointments.

The interface intentionally uses the JavaFX panes, controls, event handlers, and
`setStyle` method covered in the course sessions.

### Demo Accounts

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `admin123` |
| Client | `client` | `client123` |

## Run From VS Code

Open the project folder in VS Code, install the Java extension if needed, then use:

- the `Run` link above `main`;
- `Run and Debug` with the `Run JavaFX Application` configuration.

On Windows, the terminal command `scripts\run.bat` is the safest way to run the project.
The Code Runner play button may still try to use the Linux shell script depending on the
VS Code terminal configuration.

If JavaFX imports remain underlined after opening the project, run
`Java: Clean Java Language Server Workspace` from the command palette once.

## Build

Linux:

```bash
./scripts/build.sh
```

Windows:

```bat
scripts\build.bat
```

## Test Database

Run the SQLite persistence test against a temporary database:

Linux:

```bash
./scripts/test-database.sh
```

Windows:

```bat
scripts\test-database.bat
```

The JavaFX SDK and SQLite JDBC driver are stored in `lib/`. Compiled classes are
stored in `build/`. The generated SQLite database is stored in `data/clinic.db`.
