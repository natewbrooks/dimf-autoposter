# Defense Intelligence Memorial Foundation (DIMF) Auto Poster

This project provides a Java Swing GUI and FastAPI backend for generating and managing memorial posts for veterans. It integrates AI-generated content, optional platform selection, and image association, backed by a MySQL database.

> The application connects to the **MySQL server at `triton.towson.edu`**, using the user **`nbrooks1`** and its associated password to authenticate. It connects specifically to the **`nbrooks1db`** database.
>
> If the SQL tables defined by this project are **not detected on startup**, the application will **automatically wipe the entire `nbrooks1db` database and reinitialize all tables**.

## How to Run the Production Application

1. Open the `build/` folder.

2. Run:

   ```
   backend.exe
   ```

   This starts the backend service and connects it to the MySQL database. Watch the terminal to ensure it shows a **successful connection**.

3. Once the backend is running and connected, open:

   ```
   frontend.jar
   ```

   This launches the Java Swing GUI application.

4. Keep the **backend terminal window** running in the background while using the frontend application.

## Features

- Create memorial posts by entering a veteran’s name and date of death.
- Automatically generate meaningful memorial content using Google Search and DeepSeekAI.
- Select social media platforms to associate (no live posting).
- Upload and associate images via public URLs.
- Save post data with full relational mapping in the database.
- View, edit, and delete existing posts.
- Export all data to Excel with one click.
