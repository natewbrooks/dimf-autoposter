-- User Table
CREATE TABLE Users (
    UserID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Role VARCHAR(50) NOT NULL
);

-- Social Media Platform Table
CREATE TABLE SocialMediaPlatforms (
    PlatformID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    APIAccessStatus VARCHAR(50) NOT NULL
);

-- Memorial Post Table
CREATE TABLE MemorialPosts (
    PostID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    DateOfDeath DATE NOT NULL,
    Content TEXT,
    Description TEXT,
    AIContent TEXT
);

-- Image Table
CREATE TABLE Images (
    ImageID SERIAL PRIMARY KEY,
    URL TEXT NOT NULL,
    Source VARCHAR(255)
);

-- Status Table (associates Users, Platforms, and Posts)
CREATE TABLE Status (
    StatusID SERIAL PRIMARY KEY,
    UserID INT NOT NULL REFERENCES Users(UserID) ON DELETE CASCADE,
    PlatformID INT NOT NULL REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE,
    PostID INT NOT NULL REFERENCES MemorialPosts(PostID) ON DELETE CASCADE,
    TimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CurrentStatus VARCHAR(100) NOT NULL
);

-- Image Association Table (MemorialPost contains Images)
CREATE TABLE PostImages (
    PostID INT NOT NULL REFERENCES MemorialPosts(PostID) ON DELETE CASCADE,
    ImageID INT NOT NULL REFERENCES Images(ImageID) ON DELETE CASCADE,
    PRIMARY KEY (PostID, ImageID)
);

-- Posting Table (MemorialPost posted to one or more platforms)
CREATE TABLE Postings (
    PostID INT NOT NULL REFERENCES MemorialPosts(PostID) ON DELETE CASCADE,
    PlatformID INT NOT NULL REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE,
    PRIMARY KEY (PostID, PlatformID)
);
