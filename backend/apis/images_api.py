from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db

router = APIRouter()

@router.get("/")
def get_images(db: Session = Depends(get_db)):
    return db.execute(text("SELECT * FROM Images")).mappings().all()

@router.post("/")
def create_image(url: str, source: str = '', db: Session = Depends(get_db)):
    db.execute(text("INSERT INTO Images (URL, Source) VALUES (:url, :source)"), {"url": url, "source": source})
    db.commit()
    return {"status": "Image added"}

@router.delete("/{image_id}")
def delete_image(image_id: int, db: Session = Depends(get_db)):
    db.execute(text("DELETE FROM Images WHERE ImageID = :id"), {"id": image_id})
    db.commit()
    return {"status": "Image deleted"}