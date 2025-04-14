# Defense Intelligence Memorial Foundation (DIMF) Auto Poster

A Java Swing GUI application for managing memorial posts and social media automation, backed by a PostgreSQL database. This project runs fully inside Docker and supports **live reloading** of GUI code on file change (hot reload).

---

## Prerequisites

### 1. Install Docker Desktop

- Download from: https://www.docker.com/products/docker-desktop/
- Enable "Use the WSL 2 based engine" during installation
- Ensure Docker Desktop is running

---

### 2. Install VcXsrv (X Server for Windows GUI)

- One-time installation. Only install if using Windows OS.
- Download from: https://sourceforge.net/projects/vcxsrv/files/latest/download
- After installing, launch `XLaunch` and choose:
  1. **Multiple Windows**
  2. **Start no client**
  3. **Disable access control** (important)
  4. Finish and leave VcXsrv running in the background

This allows the Docker container to display GUI applications on your Windows desktop.

---

## Running the Project

After completing the steps above:

```bash
docker-compose up --build
```

This command:

- Builds and starts the PostgreSQL database container
- Starts the Java Swing application container
- Launches the GUI window titled **DIMF AutoPoster** on your Windows desktop

---

## Live Code Reloading

Once the app is running:

- Make changes to any `.java` files inside the `app/` directory
- Save your changes
- The container will automatically:
  - Detect the changes
  - Recompile the Java source files
  - Restart the GUI with the updated code

You do not need to rebuild or restart the container manually.

## Troubleshooting:

### 1. Allow Docker to access your Windows display

Open PowerShell or Command Prompt and run:

```powershell
setx DISPLAY host.docker.internal:0.0
```

This sets the `DISPLAY` environment variable to route GUI output from the container to your X server (VcXsrv).

---
