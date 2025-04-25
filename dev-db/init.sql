CREATE DATABASE IF NOT EXISTS nbrooks1db;
USE nbrooks1db;

-- User Table
CREATE TABLE Users (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(100) UNIQUE NOT NULL,
    Email VARCHAR(100) NULL,
    Password VARCHAR(255) NOT NULL
);

-- Social Media Platform Table
CREATE TABLE SocialMediaPlatforms (
    PlatformID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    APIAccessStatus BOOLEAN NOT NULL DEFAULT FALSE,
    PlatformURL TEXT,
    IconURL TEXT
);

-- Image Table
CREATE TABLE Images (
    ImageID SERIAL PRIMARY KEY,
    URL TEXT NOT NULL,
    Source VARCHAR(255)
);


-- Memorial Post Table
CREATE TABLE Posts (
    PostID SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    DateOfDeath DATE NOT NULL,
    Content TEXT,
    CreatedBy INT REFERENCES Users(UserID) ON DELETE SET NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- Description TEXT,
    -- AIContent TEXT
);

-- Status Table (associates Users, Platforms, and Posts)
-- CREATE TABLE PostStatus (
--     StatusID SERIAL PRIMARY KEY,
--     UserID INT NOT NULL REFERENCES Users(UserID) ON DELETE CASCADE,
--     PlatformID INT NOT NULL REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE,
--     PostID INT NOT NULL REFERENCES MemorialPosts(PostID) ON DELETE CASCADE,
--     TimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     CurrentStatus VARCHAR(100) NOT NULL
-- );

-- Image Association Table (MemorialPost contains Images)
CREATE TABLE PostImages (
    PostID INT NOT NULL REFERENCES MemorialPosts(PostID) ON DELETE CASCADE,
    ImageID INT NOT NULL REFERENCES Images(ImageID) ON DELETE CASCADE,
    PRIMARY KEY (PostID, ImageID)
);

CREATE TABLE PostDistributions (
    PostID INT NOT NULL REFERENCES MemorialPosts(PostID) ON DELETE CASCADE,
    PlatformID INT NOT NULL REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE,
    PRIMARY KEY (PostID, PlatformID)
);



INSERT INTO SocialMediaPlatforms (Name, APIAccessStatus, PlatformURL, IconURL) VALUES
('LinkedIn', FALSE, 'https://www.linkedin.com/sharing/share-offsite/?url=', '/images/linkedin.png'),
('Instagram', FALSE, 'https://www.instagram.com/', '/images/instagram.png'),
('Facebook', FALSE, 'https://www.facebook.com/sharer/sharer.php?u=', '/images/facebook.png'),
('X', FALSE, 'https://twitter.com/intent/tweet?url=', '/images/x.png');
