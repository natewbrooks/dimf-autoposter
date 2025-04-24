from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db
import bcrypt
from pydantic import BaseModel

class LoginRequest(BaseModel):
    username: str
    password: str

router = APIRouter()


# ---------- LOGIN ----------
@router.post("/login")
def login_user(data: LoginRequest, db: Session = Depends(get_db)):
    result = db.execute(
        text("SELECT * FROM Users WHERE Username = :username"),
        {"username": data.username}
    ).mappings().first()

    if not result:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    
    if not bcrypt.checkpw(data.password.encode('utf-8'), result["Password"].encode('utf-8')):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    
    return {"status": "Login successful", "user_id": result["UserID"], "username": result["Username"]}

# ---------- CRUD USERS ----------
@router.get("/users")
def get_users(db: Session = Depends(get_db)):
    return db.execute(text("SELECT UserID, Username, Email FROM Users")).mappings().all()

@router.post("/users")
def create_user(username: str, password: str, email: str = None, db: Session = Depends(get_db)):
    # Check if user with this username already exists
    existing_user = db.execute(text("SELECT * FROM Users WHERE Username = :username"), {"username": username}).mappings().first()
    if existing_user:
        raise HTTPException(status_code=400, detail="User with this username already exists")
    
    # If not, proceed with user creation
    password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
    db.execute(text("""
        INSERT INTO Users (Username, Email, Password)
        VALUES (:username, :email, :password)
    """), {"username": username, "email": email, "password": password})
    db.commit()
    return {"status": "User created"}

@router.put("/users/{user_id}")
def update_user(user_id: int, username: str, email: str = None, password: str = None, db: Session = Depends(get_db)):
    # Check if username is being changed and if the new username already exists
    current_user = db.execute(text("SELECT * FROM Users WHERE UserID = :id"), {"id": user_id}).mappings().first()
    if not current_user:
        raise HTTPException(status_code=404, detail="User not found")
    
    if current_user["Username"] != username:
        existing_user = db.execute(text("SELECT * FROM Users WHERE Username = :username"), {"username": username}).mappings().first()
        if existing_user:
            raise HTTPException(status_code=400, detail="Username already taken")
    
    # Proceed with update
    if password:
        password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
        db.execute(text("""
            UPDATE Users SET Username = :username, Email = :email, Password = :password
            WHERE UserID = :id
        """), {"id": user_id, "username": username, "email": email, "password": password})
    else:
        db.execute(text("""
            UPDATE Users SET Username = :username, Email = :email
            WHERE UserID = :id
        """), {"id": user_id, "username": username, "email": email})
    db.commit()
    return {"status": "User updated"}

@router.delete("/users/{user_id}")
def delete_user(user_id: int, db: Session = Depends(get_db)):
    db.execute(text("DELETE FROM Users WHERE UserID = :id"), {"id": user_id})
    db.commit()
    return {"status": "User deleted"}