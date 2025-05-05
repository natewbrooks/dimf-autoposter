from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db

router = APIRouter()

@router.get("/")
def get_statuses(db: Session = Depends(get_db)):
    return db.execute(text("SELECT * FROM Status")).mappings().all()

@router.post("/")
def create_status(user_id: int, platform_id: int, post_id: int, current_status: str, db: Session = Depends(get_db)):
    db.execute(text("INSERT INTO Status (UserID, PlatformID, PostID, CurrentStatus) VALUES (:uid, :pid, :postid, :status)"), {"uid": user_id, "pid": platform_id, "postid": post_id, "status": current_status})
    db.commit()
    return {"status": "Status entry added"}

# NOT USED RIGHT NOW