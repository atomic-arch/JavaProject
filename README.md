# JavaFX Class Project

This starter project uses Java 21 and a local copy of the JavaFX 21 SDK. It does
not require Maven or Gradle.

## Windows

Install Java 21 first, then open Command Prompt or PowerShell in this project folder.

Set up the project:

```bat
scripts\windows\setup-windows.bat
```

The wizard checks Java, downloads the Windows JavaFX SDK and SQLite JDBC driver,
builds the project, runs the database test, and can launch the app.

Run the app:

```bat
scripts\windows\run.bat
```

Build manually:

```bat
scripts\windows\build.bat
```

Run the database test:

```bat
scripts\windows\test-database.bat
```

Create a Windows installer:

```bat
scripts\windows\package-exe.bat
```

Manual dependency downloads are also available:

```bat
scripts\windows\download-javafx-windows.bat
scripts\windows\download-sqlite-jdbc.bat
```

Important: JavaFX contains operating-system-specific files. If this project was copied from
Linux and `lib\javafx-sdk-21.0.8` already exists, but the Windows script says it found a Linux
JavaFX SDK, delete `lib\javafx-sdk-21.0.8` and run `scripts\windows\download-javafx-windows.bat` again.

## Linux

Install Java 21 first, then download the project dependencies once:

```bash
./scripts/linux/download-javafx.sh
./scripts/linux/download-sqlite-jdbc.sh
```

Run the app:

```bash
./scripts/linux/run.sh
```

Build manually:

```bash
./scripts/linux/build.sh
```

Run the database test:

```bash
./scripts/linux/test-database.sh
```

## Application Notes

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

On Windows, the terminal command `scripts\windows\run.bat` is the safest way to run the project.
The Code Runner play button may still try to use the Linux shell script depending on the
VS Code terminal configuration.

If JavaFX imports remain underlined after opening the project, run
`Java: Clean Java Language Server Workspace` from the command palette once.

## Publish A Windows Download

The repository includes a GitHub Actions workflow that builds a Windows `.exe`
installer and uploads it to a GitHub Release when a version tag is pushed.

Create and push a release tag:

```bash
git tag v1.0.1
git push origin v1.0.1
```

After the workflow finishes, the installer is available on the latest release:

```text
https://github.com/atomic-arch/JavaProject/releases/latest
```

The `docs/` folder contains a simple GitHub Pages download page that links to
that latest release. In GitHub, enable Pages from the `main` branch and choose
`/docs` as the source folder.

The JavaFX SDK and SQLite JDBC driver are stored in `lib/`. Compiled classes are
stored in `build/`. The generated SQLite database is stored in `data/clinic.db`.
