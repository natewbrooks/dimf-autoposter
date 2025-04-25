import os
from dotenv import load_dotenv
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from serpapi import GoogleSearch

load_dotenv()

router = APIRouter()

@router.get("/search")
def perform_google_search(q: str):
    try:
        params = {
            "q": q,
            "location": "United States",
            "hl": "en",
            "gl": "us",
            "google_domain": "google.com",
            "api_key": os.getenv("SERP_API_KEY")
        }

        search = GoogleSearch(params)
        results = search.get_dict()

        if "organic_results" not in results:
            raise HTTPException(status_code=404, detail="No results found.")

        aggregated_data = ""
        for result in results["organic_results"]:
            title = result.get("title", "")
            snippet = result.get("snippet", "")
            position = str(result.get("position", ""))
            aggregated_data += f"[{position}] PAGE TITLE: {title} PAGE SNIPPET: {snippet}\n"

        return {"q": q, "summary": aggregated_data}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error during search: {str(e)}")
    
    
@router.get("/images/")
def perform_google_images_search(q: str):
    try:
        params = {
            "q": q,
            "engine": "google_images",
            "hl": "en",
            "gl": "us",
            "google_domain": "google.com",
            "api_key": os.getenv("SERP_API_KEY")
        }

        search = GoogleSearch(params)
        results = search.get_dict()

        if "images_results" not in results:
            raise HTTPException(status_code=404, detail="No image results found.")

        # Get only the first 5 thumbnails
        thumbnails = []
        for result in results["images_results"][:5]:  # Limit to first 5 results
            thumbnails.append(result.get("thumbnail", ""))

        return {"q": q, "thumbnails": thumbnails}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error during image search: {str(e)}")