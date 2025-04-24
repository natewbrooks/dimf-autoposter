import os
from dotenv import load_dotenv
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from huggingface_hub import InferenceClient

load_dotenv()

class GoogleSearchResult(BaseModel):
    q: str
    summary: str

router = APIRouter()

@router.post("/")
def hf_ai_query(data: GoogleSearchResult):
    try:
        client = InferenceClient(
            provider="fireworks-ai",
            api_key=os.getenv("HUGGING_FACE_API_KEY")
        )

        example_post = (
            "On April 3rd, 2008, SGT Nicholas A. Robertson, died from wounds sustained during combat in the Zahn Khan District "
            "of Afghanistan the day before while serving as a member of Special Operations Team Alpha. Nick, as he was known to "
            "family and friends, was an accomplished soldier who used his advanced linguistic and cryptologic skills to exploit enemy "
            "communications and protect forces on the front lines. May we never forget his commitment to service and sacrifice and honor "
            "his legacy of success and dedication. RIP SGT Robertson. #tyfys #AmericanHero #DIMFRemembers\n\n"
        )

        messages = [
            {
                "role": "user",
                "content": (
                    "Create a respectful and heartfelt memorial for a fallen veteran using the following data. "
                    "Do not include markdown punctuation. Add hashtags. This is an example post.\n\n"
                    + example_post +
                    "Veteran: " + data.q +
                    " \n\nExtracted Snippets:\n\n" + data.summary
                )
            }
        ]

        full_response = ""
        stream = client.chat.completions.create(
            model="deepseek-ai/DeepSeek-V3-0324",
            messages=messages,
            temperature=0.5,
            max_tokens=2048,
            top_p=0.7,
            stream=True
        )

        for chunk in stream:
            content = chunk.choices[0].delta.content
            if content:
                full_response += content

        return {"response": full_response.strip()}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error during AI response: {str(e)}")
