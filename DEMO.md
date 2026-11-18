# Standalone two-client demo

The project has three independently running services:

| Service | Address | Purpose |
| --- | --- | --- |
| User client | `http://127.0.0.1:8080/#/login` | Mobile user workflow |
| Doctor client | `http://127.0.0.1:8081/#/doctor/dashboard` | Doctor workspace |
| Backend API | `http://127.0.0.1:8088/tijiancms` | Shared Spring Boot API |

## Start

Double-click `Start-Demo.cmd` from File Explorer. It starts all three services in the background and opens the user and doctor clients in two independent Edge or Chrome app windows.

The user client is opened at phone size, while the doctor client is opened as a desktop window. They use different origins and therefore keep their browser storage and sessions separate.

To start the services without opening browser windows:

```powershell
.\Start-Demo.ps1 -NoBrowser
```

To rebuild the backend before starting:

```powershell
.\Start-Demo.ps1 -RebuildBackend
```

## Stop

Double-click `Stop-Demo.cmd`. It stops only the processes recorded by the launcher. Logs are written to `.demo-runtime`.

## Requirements

- MySQL is running and contains the `health_check_system` database.
- Java and Node.js/npm are available in `PATH`.
- Frontend dependencies are installed in both frontend directories.
