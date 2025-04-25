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
    db.execute(
        text("DELETE FROM PostImages WHERE PostID = :post AND ImageID = :img"), 
        {"post": link.post_id, "img": link.image_id}
    )
    db.commit()
    return {"status": "Image unlinked from post"}