from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db

router = APIRouter()

@router.post("/")
def add_post_image(post_id: int, image_id: int, db: Session = Depends(get_db)):
    db.execute(text("INSERT INTO PostImages (PostID, ImageID) VALUES (:post, :img)"), {"post": post_id, "img": image_id})
    db.commit()
    return {"status": "Image linked to post"}

@router.delete("/")
def remove_post_image(post_id: int, image_id: int, db: Session = Depends(get_db)):
    db.execute(text("DELETE FROM PostImages WHERE PostID = :post AND ImageID = :img"), {"post": post_id, "img": image_id})
    db.commit()
    return {"status": "Image unlinked from post"}
