from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db
from pydantic import BaseModel
from typing import List

router = APIRouter()

class PostCreate(BaseModel):
    name: str
    date_of_death: str
    content: str = ''
    created_by: int | None = None

class PlatformSelection(BaseModel):
    platform_ids: List[int]

@router.get("/")
def get_posts(db: Session = Depends(get_db)):
    return db.execute(text("SELECT * FROM Posts")).mappings().all()

@router.post("/")
def create_post(post: PostCreate, db: Session = Depends(get_db)):
    try:
        db.execute(text("""
            INSERT INTO Posts (Name, DateOfDeath, Content, CreatedBy)
            VALUES (:name, :dod, :content, :created_by)
        """), {
            "name": post.name,
            "dod": post.date_of_death,
            "content": post.content,
            "created_by": post.created_by
        })
        result = db.execute(text("SELECT LAST_INSERT_ID()"))
        post_id = result.scalar_one()
        db.commit()
        return {"status": "Post created", "post_id": post_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.put("/{post_id}")
def update_post(post_id: int, post: PostCreate, db: Session = Depends(get_db)):
    try:
        db.execute(text("""
            UPDATE Posts
            SET Name = :name,
                DateOfDeath = :dod,
                Content = :content,
                CreatedBy = :created_by
            WHERE PostID = :id
        """), {
            "id": post_id,
            "name": post.name,
            "dod": post.date_of_death,
            "content": post.content,
            "created_by": post.created_by
        })
        db.commit()
        return {"status": "Post updated"}
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