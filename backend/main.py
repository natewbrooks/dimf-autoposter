from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from database import get_db, test_connection
from fastapi import HTTPException

from apis import (
    auth_api,
    images_api,
    platforms_api,
    google_api,
    ai_api,
    export_excel_api,
)
from apis.posts import (
    posts_api,
    post_images_api,
    post_status_api,
    post_distributions_api
)
from database import test_connection

app = FastAPI()


# CORS
origins = [
    "http://localhost",
    "http://localhost:3000",
    "http://localhost:5173",
    "http://127.0.0.1",
    "http://127.0.0.1:3000",
    "http://127.0.0.1:5173",
    "http://127.0.0.1:8000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
        
        
 # Create default user on startup if no user exist
@app.on_event("startup")
def create_default_user():
    test_connection()
    db = next(get_db())

    try:
        auth_api.create_user(
            username="user",
            password="pass",
            email=None,
            db=db
        )
        print("[DEFAULT USER] User created: username='user', password='pass'")
    except HTTPException as e:
        print(f"[DEFAULT USER EXISTS] {e.detail}")
    except Exception as e:
        print(f"Unexpected error: {e}")
        
# Base routers
app.include_router(
    platforms_api.router,
    prefix="/api/platforms",
    tags=["Social Media Platforms"],
)

app.include_router(
    images_api.router,
    prefix="/api/images",
    tags=["Images"],
)

app.include_router(
    auth_api.router,
    prefix="/api/auth",
    tags=["Auth"],
)

app.include_router(
    export_excel_api.router,
    prefix="/api/export/excel",
    tags=["Export Excel"],
)

# Google Search API
app.include_router(
    google_api.router,
    prefix="/api/google",
    tags=["SerpAPI Implementation"],
)

app.include_router(
    ai_api.router,
    prefix="/api/ai",
    tags=["DeepSeek AI via HuggingFace API"],
)


# Post-related routers
app.include_router(
    posts_api.router,
    prefix="/api/posts",
    tags=["Posts"],
)

app.include_router(
    post_status_api.router,
    prefix="/api/posts/statuses",
    tags=["Post Statuses"],
)

app.include_router(
    post_images_api.router,
    prefix="/api/posts/images",
    tags=["Post Images"],
)

app.include_router(
    post_distributions_api.router,
    prefix="/api/posts/distributions",
    tags=["Post Distributions"],
)
