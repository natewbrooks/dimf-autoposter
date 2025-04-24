from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db

router = APIRouter()

@router.get("/")
def get_platforms(db: Session = Depends(get_db)):
    return db.execute(text("SELECT * FROM SocialMediaPlatforms")).mappings().all()

@router.post("/")
def create_platform(name: str, api_access_status: str, icon_url: str, db: Session = Depends(get_db)):
    db.execute(text("""
        INSERT INTO SocialMediaPlatforms (Name, APIAccessStatus, IconURL) 
        VALUES (:name, :status, :icon_url)
    """), {"name": name, "status": api_access_status, "icon_url": icon_url})
    db.commit()
    return {"status": "Platform created"}

@router.put("/{platform_id}")
def update_platform(platform_id: int, name: str, api_access_status: str, icon_url: str, db: Session = Depends(get_db)):
    db.execute(text("""
        UPDATE SocialMediaPlatforms 
        SET Name = :name, APIAccessStatus = :status, IconURL = :icon_url 
        WHERE PlatformID = :id
    """), {"id": platform_id, "name": name, "status": api_access_status, "icon_url": icon_url})
    db.commit()
    return {"status": "Platform updated"}

@router.delete("/{platform_id}")
def delete_platform(platform_id: int, db: Session = Depends(get_db)):
    db.execute(text("DELETE FROM SocialMediaPlatforms WHERE PlatformID = :id"), {"id": platform_id})
    db.commit()
    return {"status": "Platform deleted"}