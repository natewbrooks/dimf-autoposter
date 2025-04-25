from fastapi import APIRouter, Depends, Body
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db
from pydantic import BaseModel

router = APIRouter()

class PostImageLink(BaseModel):
    post_id: int
    image_id: int

@router.post("/")
def add_post_image(link: PostImageLink, db: Session = Depends(get_db)):
    db.execute(
        text("INSERT INTO PostImages (PostID, ImageID) VALUES (:post, :img)"), 
        {"post": link.post_id, "img": link.image_id}
    )
    db.commit()
    return {"status": "Image linked to post"}

@router.delete("/")
def remove_post_image(link: PostImageLink, db: Session = Depends(get_db)):
    # First, get the image URL before deleting associations
    image_url = db.execute(
        text("SELECT URL FROM Images WHERE ImageID = :img"),
        {"img": link.image_id}
    ).fetchone()
    
    # Delete the association in PostImages table
    db.execute(
        text("DELETE FROM PostImages WHERE PostID = :post AND ImageID = :img"), 
        {"post": link.post_id, "img": link.image_id}
    )
    
    # Check if this image is associated with any other posts
    other_associations = db.execute(
        text("SELECT COUNT(*) FROM PostImages WHERE ImageID = :img"),
        {"img": link.image_id}
    ).scalar()
    
    # If no other posts use this image, delete the image file and record
    if other_associations == 0 and image_url:
        # Delete the physical file
        import os
        if image_url[0] and image_url[0].startswith('/'):
            # Assuming image_url is a local path like '/images/photo.jpg'
            image_path = os.path.join('static', image_url[0].lstrip('/'))
            try:
                if os.path.exists(image_path):
                    os.remove(image_path)
            except Exception as e:
                print(f"Error deleting image file {image_path}: {e}")
        
        # Delete the image record from database
        db.execute(
            text("DELETE FROM Images WHERE ImageID = :img"),
            {"img": link.image_id}
        )
    
    db.commit()
    return {"status": "Image unlinked from post and deleted if not used elsewhere"}