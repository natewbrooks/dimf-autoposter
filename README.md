# Defense Intelligence Memorial Foundation (DIMF) Auto Poster

This project provides a Java Swing GUI and FastAPI backend for generating and managing memorial posts for veterans. It integrates AI-generated content, optional platform selection, and image association, backed by a MySQL database. Local development uses Docker for database support, while production connects to a remote MySQL server at `triton2.towson.edu`.

---

## Prerequisites

### 1. Python Environment

- Python 3.10+

### 2. Java Development Environment

- Eclipse, NetBeans, or any IDE with Maven support

### 3. Docker (Optional for local development DB)

- [Install Docker](https://www.docker.com/products/docker-desktop)

---

## Local Development Setup

### 1. Start MySQL Database with Docker (Optional)

You can use Docker for local MySQL if you're not connected to the production DB.

```bash
docker-compose up --build
```

---

### 2. Run the Python FastAPI Server

Navigate to the backend directory and install dependencies:

```bash
cd backend
pip install -r requirements.txt
```

Start the development server:

```bash
uvicorn main:app --reload
```

The API will be available at [http://localhost:8000](http://localhost:8000)

API documentation is available at [http://localhost:8000/docs](http://localhost:8000/docs)

---

### 3. Run the Java Swing GUI

1. Open the project in a Java IDE like Eclipse or NetBeans.
2. Ensure Maven dependencies are resolved.
3. Run `Main.java`.

By default, a test user with username `user` and password `pass` is preloaded and autofilled in the login form. Simply press Enter to proceed.

---

## Features

- Create memorial posts by entering a veteran's name and date of death.
- Triggers Google Search API and DeepSeekAI (via HuggingFace) to generate meaningful content.
- Select social media platforms to associate (no actual posting due to lack of live API tokens).
- Upload images via URL (publicly hosted or cloud drive links).
- Save post data to the backend database with full relational mapping.
- View, edit, and delete existing posts from the sidebar.
- Export all data to Excel with one click.

---

## Database Schema

```sql
CREATE TABLE Users (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(100) UNIQUE NOT NULL,
    Email VARCHAR(100),
    Password VARCHAR(255) NOT NULL
);

CREATE TABLE SocialMediaPlatforms (
    PlatformID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    APIAccessStatus BOOLEAN NOT NULL DEFAULT FALSE,
    PlatformURL TEXT,
    IconURL TEXT
);

CREATE TABLE Images (
    ImageID SERIAL PRIMARY KEY,
    URL TEXT NOT NULL,
    Source VARCHAR(255)
);

CREATE TABLE Posts (
    PostID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    DateOfDeath DATE NOT NULL,
    Content TEXT,
    CreatedBy INT REFERENCES Users(UserID) ON DELETE SET NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE PostStatus (
    StatusID SERIAL PRIMARY KEY,
    UserID INT NOT NULL REFERENCES Users(UserID) ON DELETE CASCADE,
    PlatformID INT NOT NULL REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE,
    PostID INT NOT NULL REFERENCES Posts(PostID) ON DELETE CASCADE,
    TimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CurrentStatus VARCHAR(100) NOT NULL
);

CREATE TABLE PostImages (
    PostID INT NOT NULL REFERENCES Posts(PostID) ON DELETE CASCADE,
    ImageID INT NOT NULL REFERENCES Images(ImageID) ON DELETE CASCADE,
    PRIMARY KEY (PostID, ImageID)
);

CREATE TABLE PostDistributions (
    PostID INT NOT NULL REFERENCES Posts(PostID) ON DELETE CASCADE,
    PlatformID INT NOT NULL REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE,
    PRIMARY KEY (PostID, PlatformID)
);
```
