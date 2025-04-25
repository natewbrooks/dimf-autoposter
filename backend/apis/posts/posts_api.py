from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db
from pydantic import BaseModel

router = APIRouter()

class PostCreate(BaseModel):
    name: str
    date_of_death: str
    content: str = ''
    created_by: int | None = None

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
    db.execute(text("DELETE FROM Posts WHERE PostID = :id"), {"id": post_id})
    db.commit()
    return {"status": "Post deleted"}


# @router.get("/{post_id}/platforms/")
# def get_post_platforms(post_id: int, db: Session = Depends(get_db)):
#     try:
#         query = """
#             SELECT p.PlatformID, p.Name, p.PlatformURL, p.IconURL
#             FROM SocialMediaPlatforms p
#             JOIN PostDistributions d ON p.PlatformID = d.PlatformID
#             WHERE d.PostID = :post_id
#         """
#         result = db.execute(text(query), {"post_id": post_id}).mappings().all()
#         return result
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"Error loading platforms: {e}")
    
    
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
