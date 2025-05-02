from fastapi import APIRouter, Depends, Body, Request
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db
from pydantic import BaseModel

router = APIRouter()

class ImageCreate(BaseModel):
    url: str
    source: str = ''

@router.get("/")
def get_images(db: Session = Depends(get_db)):
    return db.execute(text("SELECT * FROM Images")).mappings().all()

@router.post("/")
async def create_image(request: Request, db: Session = Depends(get_db)):
    try:
        # Get the raw request body
        body = await request.json()
        print("Received raw request body:", body)
        
        # Extract the URL and source from the request body
        url = body.get("url", "")
        source = body.get("source", "")
        
        print(f"Extracted URL: {url}")
        print(f"Extracted source: {source}")
        
        if not url:
            return JSONResponse(
                status_code=400,
                content={"error": "URL is required"}
            )
        
        # Insert the record
        db.execute(
            text("INSERT INTO Images (URL, Source) VALUES (:url, :source)"), 
            {"url": url, "source": source}
        )
        
        # Get the last inserted ID
        result = db.execute(text("SELECT LAST_INSERT_ID()"))
        image_id = result.scalar_one()
        
        db.commit()
        
        return {"status": "Image added", "image_id": image_id}
    except Exception as e:
        print(f"Error in create_image: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"error": str(e), "details": "Error processing image upload"}
        )

@router.delete("/{image_id}")
def delete_image(image_id: int, db: Session = Depends(get_db)):
    db.execute(text("DELETE FROM Images WHERE ImageID = :id"), {"id": image_id})
    db.commit()
    return {"status": "Image deleted"}

@router.get("/find")
def find_image_by_url(url: str, db: Session = Depends(get_db)):
    try:
        result = db.execute(text("""
            SELECT ImageID FROM Images WHERE URL = :url
        """), {"url": url}).first()
        
        if result:
            return {"status": "Image found", "image_id": result[0]}
        else:
            return JSONResponse(
                status_code=404,
                content={"status": "Image not found"}
            )
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"error": str(e)}
        )