import os
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    print("Warning: dotenv module not found, using environment variables as-is")

import uvicorn

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)