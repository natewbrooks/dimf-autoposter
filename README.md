# Defense Intelligence Memorial Foundation (DIMF) Auto Poster

A Java Swing GUI application for managing memorial posts and social media automation, backed by a PostgreSQL database.  
This project uses Docker to host the PostgreSQL database. The Java GUI application runs directly on your local system and connects to the database over `localhost`.

---

## Prerequisites

### 1. Install Docker Desktop

- Download from: https://www.docker.com/products/docker-desktop/
- Enable "Use the WSL 2 based engine" during installation (on Windows)
- Ensure Docker Desktop is running

---

## Running the Project

### 1. Start the PostgreSQL database container

From the project root directory, run:

```bash
docker-compose up --build
```

This command:

- Builds and starts the PostgreSQL database container
- Makes the database available on `localhost:5432`

---

### 2. Run the Java Swing GUI Locally

Open a terminal in the project directory and run:

```bash
javac -cp "postgresql.jar" Main.java
java -cp ".;postgresql.jar" Main
```

> On Windows, use `;` instead of `:` in the classpath.

Make sure `postgresql.jar` is downloaded and present in the working directory. It should be in the git repo.

---

## Troubleshooting

If the Java application cannot connect to the database, ensure:

- Docker is running
- The `db` container is healthy (`docker ps` to check)
- Your Java app is using the correct JDBC URL:
  ```
  jdbc:postgresql://localhost:5432/dimf
  ```
