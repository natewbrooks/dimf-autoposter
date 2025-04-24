from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db

router = APIRouter()

@router.post("/")
def create_post_distribution(post_id: int, platform_id: int, db: Session = Depends(get_db)):
    db.execute(text("""
        INSERT INTO PostDistributions (PostID, PlatformID)
        VALUES (:post, :plat)
    """), {"post": post_id, "plat": platform_id})
    db.commit()
    return {"status": "Post assigned to platform"}

@router.delete("/")
def delete_post_distribution(post_id: int, platform_id: int, db: Session = Depends(get_db)):
    db.execute(text("""
        DELETE FROM PostDistributions
        WHERE PostID = :post AND PlatformID = :plat
    """), {"post": post_id, "plat": platform_id})
    db.commit()
    return {"status": "Post unassigned from platform"}