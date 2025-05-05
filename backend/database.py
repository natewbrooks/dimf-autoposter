import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# Get DATABASE_URL from environment
DATABASE_URL = os.getenv("DATABASE_URL")

# Global engine and session variables - will be None if database connection fails
engine = None
session = None

def test_connection():
    """Test database connection and print result"""
    global engine
    
    try:
        # Only try to create engine if DATABASE_URL is set
        if not DATABASE_URL:
            print("[DATABASE] DATABASE_URL not set in environment variables")
            return False
            
        # Create engine if it doesn't exist yet
        if engine is None:
            engine = create_engine(
                DATABASE_URL,
                pool_size=20,
                pool_recycle=3600,
                pool_pre_ping=True,
                connect_args={"connect_timeout": 15}  # 15 second timeout
            )
            
        # Test connection
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        print("[DATABASE] Successfully connected to the database.")
        
        # Create session factory if connection was successful
        global session
        if session is None:
            session = sessionmaker(engine)
            
        return True
    except Exception as e:
        print("[DATABASE] Connection failed:", str(e))
        engine = None  # Reset engine on failure
        session = None  # Reset session on failure
        return False

def get_db():
    """Get database session - returns None if database is not available"""
    global session
    
    # If no session factory, yield None (database unavailable)
    if session is None:
        yield None
        return
        
    # Otherwise create and yield a session
    db = session()
    try:
        yield db
    finally:
        db.close()

def run_init_sql():
    """Ensure all required database tables exist; if any are missing, wipe and recreate them."""
    global engine

    required_tables = {
        'Users',
        'SocialMediaPlatforms',
        'Images',
        'Posts',
        'PostImages',
        'PostDistributions'
    }

    if engine is None:
        print("[DATABASE] Skipping database initialization - database unavailable")
        return False

    try:
        with engine.connect() as conn:
            try:
                # Get list of existing tables
                result = conn.execute(text("SHOW TABLES"))
                existing_tables = {row[0] for row in result.fetchall()}

                # Check if all required tables are present
                missing_tables = required_tables - existing_tables

                if missing_tables:
                    print(f"[DATABASE] Missing tables detected: {', '.join(missing_tables)} — wiping and recreating all tables.")

                    # Drop all relevant tables in reverse dependency order
                    conn.execute(text("DROP TABLE IF EXISTS PostDistributions"))
                    conn.execute(text("DROP TABLE IF EXISTS PostImages"))
                    conn.execute(text("DROP TABLE IF EXISTS Posts"))
                    conn.execute(text("DROP TABLE IF EXISTS Images"))
                    conn.execute(text("DROP TABLE IF EXISTS SocialMediaPlatforms"))
                    conn.execute(text("DROP TABLE IF EXISTS Users"))
                    print("[DATABASE] All tables dropped.")

                    # Create Users table
                    conn.execute(text("""
                    CREATE TABLE Users (
                        UserID INT AUTO_INCREMENT PRIMARY KEY,
                        Username VARCHAR(100) UNIQUE NOT NULL,
                        Email VARCHAR(100) NULL,
                        Password VARCHAR(255) NOT NULL
                    )
                    """))

                    # Create SocialMediaPlatforms table
                    conn.execute(text("""
                    CREATE TABLE SocialMediaPlatforms (
                        PlatformID INT AUTO_INCREMENT PRIMARY KEY,
                        Name VARCHAR(100) NOT NULL,
                        APIAccessStatus BOOLEAN NOT NULL DEFAULT FALSE,
                        PlatformURL TEXT,
                        IconURL TEXT
                    )
                    """))

                    # Create Images table
                    conn.execute(text("""
                    CREATE TABLE Images (
                        ImageID INT AUTO_INCREMENT PRIMARY KEY,
                        URL TEXT NOT NULL,
                        Source VARCHAR(255)
                    )
                    """))

                    # Create Posts table
                    conn.execute(text("""
                    CREATE TABLE Posts (
                        PostID INT AUTO_INCREMENT PRIMARY KEY,
                        Name VARCHAR(100) NOT NULL,
                        DateOfDeath DATE NOT NULL,
                        Content TEXT,
                        CreatedBy INT,
                        CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_posts_users FOREIGN KEY (CreatedBy)
                        REFERENCES Users(UserID) ON DELETE SET NULL
                    )
                    """))

                    # Create PostImages table
                    conn.execute(text("""
                    CREATE TABLE PostImages (
                        PostID INT NOT NULL,
                        ImageID INT NOT NULL,
                        PRIMARY KEY (PostID, ImageID),
                        CONSTRAINT fk_postimages_posts FOREIGN KEY (PostID)
                        REFERENCES Posts(PostID) ON DELETE CASCADE,
                        CONSTRAINT fk_postimages_images FOREIGN KEY (ImageID)
                        REFERENCES Images(ImageID) ON DELETE CASCADE
                    )
                    """))

                    # Create PostDistributions table
                    conn.execute(text("""
                    CREATE TABLE PostDistributions (
                        PostID INT NOT NULL,
                        PlatformID INT NOT NULL,
                        PRIMARY KEY (PostID, PlatformID),
                        CONSTRAINT fk_postdist_posts FOREIGN KEY (PostID)
                        REFERENCES Posts(PostID) ON DELETE CASCADE,
                        CONSTRAINT fk_postdist_platforms FOREIGN KEY (PlatformID)
                        REFERENCES SocialMediaPlatforms(PlatformID) ON DELETE CASCADE
                    )
                    """))

                    # Insert initial platforms
                    conn.execute(text("""
                    INSERT INTO SocialMediaPlatforms (Name, APIAccessStatus, PlatformURL, IconURL) VALUES
                    ('LinkedIn', FALSE, 'https://www.linkedin.com/sharing/share-offsite/?url=', '/resources/images/linkedin.png'),
                    ('Instagram', FALSE, 'https://www.instagram.com/', '/resources/images/instagram.png'),
                    ('Facebook', FALSE, 'https://www.facebook.com/sharer/sharer.php?u=', '/resources/images/facebook.png'),
                    ('X', FALSE, 'https://twitter.com/intent/tweet?url=', '/resources/images/x.png')
                    """))

                    conn.commit()
                    print("[DATABASE] All tables created successfully.")
                else:
                    print("[DATABASE] All required tables already exist.")

                return True

            except Exception as e:
                conn.rollback()
                print(f"[DATABASE] Failed to initialize: {str(e)}")
                return False

    except Exception as e:
        print(f"[DATABASE] Error during initialization: {str(e)}")
        return False
