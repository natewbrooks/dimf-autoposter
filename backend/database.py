import os
from dotenv import load_dotenv
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# Load environment variables
load_dotenv()

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
                connect_args={"connect_timeout": 10}  # 10 seconds timeout
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
    """Initialize database tables if they don't exist
    Skips if database connection failed
    """
    global engine
    
    # Skip if no engine (database connection failed)
    if engine is None:
        print("[DATABASE] Skipping database initialization - database unavailable")
        return False
        
    try:
        # Check if tables exist
        with engine.connect() as conn:
            try:
                # Check for Posts table existence
                result = conn.execute(text("SHOW TABLES LIKE 'Posts'"))
                tables_exist = result.rowcount > 0
                
                if not tables_exist:
                    print("[DATABASE] Expected tables missing — wiping all tables.")
                    
                    # Drop existing tables in reverse order of dependency
                    conn.execute(text("DROP TABLE IF EXISTS PostDistributions"))
                    conn.execute(text("DROP TABLE IF EXISTS PostImages"))
                    conn.execute(text("DROP TABLE IF EXISTS Posts"))
                    conn.execute(text("DROP TABLE IF EXISTS Images"))
                    conn.execute(text("DROP TABLE IF EXISTS SocialMediaPlatforms"))
                    conn.execute(text("DROP TABLE IF EXISTS Users"))
                    print("[DATABASE] All tables dropped.")
                    
                    # Create tables in correct order
                    # Users table
                    conn.execute(text("""
                    CREATE TABLE Users (
                        UserID INT AUTO_INCREMENT PRIMARY KEY,
                        Username VARCHAR(100) UNIQUE NOT NULL,
                        Email VARCHAR(100) NULL,
                        Password VARCHAR(255) NOT NULL
                    )
                    """))
                    
                    # Social Media Platform Table
                    conn.execute(text("""
                    CREATE TABLE SocialMediaPlatforms (
                        PlatformID INT AUTO_INCREMENT PRIMARY KEY,
                        Name VARCHAR(100) NOT NULL,
                        APIAccessStatus BOOLEAN NOT NULL DEFAULT FALSE,
                        PlatformURL TEXT,
                        IconURL TEXT
                    )
                    """))
                    
                    # Image Table
                    conn.execute(text("""
                    CREATE TABLE Images (
                        ImageID INT AUTO_INCREMENT PRIMARY KEY,
                        URL TEXT NOT NULL,
                        Source VARCHAR(255)
                    )
                    """))
                    
                    # Posts Table
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
                    
                    # PostImages Table
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
                    
                    # PostDistributions Table
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
                    ('LinkedIn', FALSE, 'https://www.linkedin.com/sharing/share-offsite/?url=', '/images/linkedin.png'),
                    ('Instagram', FALSE, 'https://www.instagram.com/', '/images/instagram.png'),
                    ('Facebook', FALSE, 'https://www.facebook.com/sharer/sharer.php?u=', '/images/facebook.png'),
                    ('X', FALSE, 'https://twitter.com/intent/tweet?url=', '/images/x.png')
                    """))
                    
                    conn.commit()
                    print("[DATABASE] All tables created successfully.")
                return True
            except Exception as e:
                conn.rollback()
                print(f"[DATABASE] Failed to initialize: {str(e)}")
                return False
    except Exception as e:
        print(f"[DATABASE] Error during initialization: {str(e)}")
        return False