from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db
from pydantic import BaseModel
from typing import List, Optional

router = APIRouter()

class PostCreate(BaseModel):
    name: str
    date_of_death: str
    content: str = ''
    images: List[str] = []  # Accept list of image URLs from frontend
    platforms: List[int] = []
    created_by: Optional[int] = None  # User ID of the creator
    creator_username: Optional[str] = None  # Username of the creator

class PlatformSelection(BaseModel):
    platform_ids: List[int]

@router.get("/")
def get_posts(db: Session = Depends(get_db)):
    # Get posts with creator information
    posts = db.execute(text("""
        SELECT p.*, u.Username as CreatorUsername 
        FROM Posts p
        LEFT JOIN Users u ON p.CreatedBy = u.UserID
    """)).mappings().all()
    return posts

@router.post("/")
def create_post(post: PostCreate, db: Session = Depends(get_db)):
    try:
        # Create the post with creator information
        result = db.execute(text("""
            INSERT INTO Posts (Name, DateOfDeath, Content, CreatedBy)
            VALUES (:name, :dod, :content, :created_by)
        """), {
            "name": post.name,
            "dod": post.date_of_death,
            "content": post.content,
            "created_by": post.created_by,
        })

        # Get the post ID immediately after insert, before any other inserts
        post_id = result.lastrowid

        
        if not post_id:
            # Fallback method tries to find the post by name
            result = db.execute(text("""
                SELECT PostID FROM Posts 
                WHERE Name = :name 
                ORDER BY PostID DESC LIMIT 1
            """), {"name": post.name}).first()
            
            post_id = result[0] if result else None
            
        if not post_id:
            raise Exception("Failed to retrieve inserted post ID")

        # Insert Images + Link to Post
        replace_post_images(db, post_id, post.images)
        
        # Insert Platform associations
        if post.platforms:
            for platform_id in post.platforms:
                db.execute(text("""
                    INSERT INTO PostDistributions (PostID, PlatformID)
                    VALUES (:post_id, :platform_id)
                """), {
                    "post_id": post_id,
                    "platform_id": platform_id
                })

        db.commit()

        return {"status": "Post created", "post_id": post_id}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

# Update an existing post
@router.put("/{post_id}")
def update_post(post_id: int, post: PostCreate, db: Session = Depends(get_db)):
    try:
        # Update the post with creator information
        db.execute(text("""
            UPDATE Posts
            SET Name = :name,
                DateOfDeath = :dod,
                Content = :content,
                CreatedBy = :created_by
            WHERE PostID = :post_id
        """), {
            "name": post.name,
            "dod": post.date_of_death,
            "content": post.content,
            "created_by": post.created_by,  # Add the creator ID
            "post_id": post_id
        })

        # Replace images
        replace_post_images(db, post_id, post.images)
        
        # Replace platform associations
        if post.platforms:
            # Delete existing platform associations
            db.execute(text("""
                DELETE FROM PostDistributions
                WHERE PostID = :post_id
            """), {
                "post_id": post_id
            })
            
            # Add new platform associations
            for platform_id in post.platforms:
                db.execute(text("""
                    INSERT INTO PostDistributions (PostID, PlatformID)
                    VALUES (:post_id, :platform_id)
                """), {
                    "post_id": post_id,
                    "platform_id": platform_id
                })

        db.commit()

        return {"status": "Post updated", "post_id": post_id}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/{post_id}")
def get_post(post_id: int, db: Session = Depends(get_db)):
    try:
        # Get post with creator username
        query = """
            SELECT p.*, u.Username as CreatorUsername
            FROM Posts p
            LEFT JOIN Users u ON p.CreatedBy = u.UserID
            WHERE p.PostID = :post_id
        """
        post = db.execute(text(query), {"post_id": post_id}).mappings().first()
        
        if not post:
            raise HTTPException(status_code=404, detail="Post not found")
            
        # Get images for the post
        images_query = """
            SELECT i.ImageID, i.URL, i.Source
            FROM Images i
            JOIN PostImages pi ON i.ImageID = pi.ImageID
            WHERE pi.PostID = :post_id
        """
        images = db.execute(text(images_query), {"post_id": post_id}).mappings().all()
        
        # Get platforms for the post
        platforms_query = """
            SELECT p.PlatformID, p.Name, p.PlatformURL, p.IconURL
            FROM SocialMediaPlatforms p
            JOIN PostDistributions d ON p.PlatformID = d.PlatformID
            WHERE d.PostID = :post_id
        """
        platforms = db.execute(text(platforms_query), {"post_id": post_id}).mappings().all()
        
        # Combine post with images and platforms
        result = dict(post)
        result["images"] = list(images)
        result["platforms"] = list(platforms)
        
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/{post_id}")
def delete_post(post_id: int, db: Session = Depends(get_db)):
    # First, get all image IDs associated with this post
    image_ids = db.execute(
        text("SELECT ImageID FROM PostImages WHERE PostID = :id"),
        {"id": post_id}
    ).fetchall()
    
    # Delete the associations in PostImages table
    db.execute(
        text("DELETE FROM PostImages WHERE PostID = :id"),
        {"id": post_id}
    )
    
    # Delete platform associations
    db.execute(
        text("DELETE FROM PostDistributions WHERE PostID = :id"),
        {"id": post_id}
    )
    
    # Delete the actual images from Images table
    for image_id in image_ids:
        db.execute(
            text("DELETE FROM Images WHERE ImageID = :id"),
            {"id": image_id[0]}  # fetchall returns tuples, so we need to extract the ID
        )
    
    # Finally delete the post
    db.execute(
        text("DELETE FROM Posts WHERE PostID = :id"),
        {"id": post_id}
    )
    
    db.commit()
    return {"status": "Post and associated images deleted"}

@router.get("/{post_id}/platforms/")
def get_post_platforms(post_id: int, db: Session = Depends(get_db)):
    try:
        query = """
            SELECT p.PlatformID, p.Name, p.PlatformURL, p.IconURL
            FROM SocialMediaPlatforms p
            JOIN PostDistributions d ON p.PlatformID = d.PlatformID
            WHERE d.PostID = :post_id
        """
        result = db.execute(text(query), {"post_id": post_id}).mappings().all()
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading platforms: {e}")

@router.put("/{post_id}/platforms/")
def update_post_platforms(post_id: int, platform_selection: PlatformSelection, db: Session = Depends(get_db)):
    try:
        # First, delete all existing platform associations for this post
        db.execute(
            text("DELETE FROM PostDistributions WHERE PostID = :post_id"),
            {"post_id": post_id}
        )
        
        # Then insert the new selections
        for platform_id in platform_selection.platform_ids:
            db.execute(
                text("INSERT INTO PostDistributions (PostID, PlatformID) VALUES (:post_id, :platform_id)"),
                {"post_id": post_id, "platform_id": platform_id}
            )
        
        db.commit()
        return {"status": "Post platforms updated successfully"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error updating platforms: {e}")

@router.post("/{post_id}/platforms/{platform_id}")
def add_platform_to_post(post_id: int, platform_id: int, db: Session = Depends(get_db)):
    try:
        # Check if association already exists
        existing = db.execute(
            text("SELECT COUNT(*) FROM PostDistributions WHERE PostID = :post_id AND PlatformID = :platform_id"),
            {"post_id": post_id, "platform_id": platform_id}
        ).scalar()
        
        if existing == 0:
            db.execute(
                text("INSERT INTO PostDistributions (PostID, PlatformID) VALUES (:post_id, :platform_id)"),
                {"post_id": post_id, "platform_id": platform_id}
            )
            db.commit()
        
        return {"status": "Platform added to post"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error adding platform: {e}")

@router.delete("/{post_id}/platforms/{platform_id}")
def remove_platform_from_post(post_id: int, platform_id: int, db: Session = Depends(get_db)):
    try:
        db.execute(
            text("DELETE FROM PostDistributions WHERE PostID = :post_id AND PlatformID = :platform_id"),
            {"post_id": post_id, "platform_id": platform_id}
        )
        db.commit()
        return {"status": "Platform removed from post"}
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error removing platform: {e}")
    
@router.get("/{post_id}/images/")
def get_post_images(post_id: int, db: Session = Depends(get_db)):
    try:
        query = """
            SELECT i.ImageID, i.URL, i.Source
            FROM Images i
            JOIN PostImages pi ON i.ImageID = pi.ImageID
            WHERE pi.PostID = :post_id
        """
        result = db.execute(text(query), {"post_id": post_id}).mappings().all()
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading images: {e}")
    
def replace_post_images(db: Session, post_id: int, image_urls: List[str]):
    try:
        # Delete all PostImages for this PostID
        db.execute(text("""
            DELETE FROM PostImages WHERE PostID = :post_id
        """), {"post_id": post_id})

        # Insert new images and link to post
        for url in image_urls:
            # Check if image already exists
            existing_image = db.execute(text("""
                SELECT ImageID FROM Images WHERE URL = :url
            """), {"url": url}).first()
            
            if existing_image:
                image_id = existing_image[0]
            else:
                # Insert into Images
                db.execute(text("""
                    INSERT INTO Images (URL, Source)
                    VALUES (:url, :source)
                """), {"url": url, "source": "Uploaded by user"})

                image_id = db.execute(text("SELECT LAST_INSERT_ID() as id")).first()[0]
            
            # Insert into PostImages
            db.execute(text("""
                INSERT INTO PostImages (PostID, ImageID)
                VALUES (:post_id, :image_id)
            """), {"post_id": post_id, "image_id": image_id})
    except Exception as e:
        print(f"Error in replace_post_images: {str(e)}")
        raise e