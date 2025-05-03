import os
import sys

# Determine if running as executable or script
if getattr(sys, 'frozen', False):
    # Get the executable directory
    base_dir = os.path.dirname(sys.executable)
    # Look for .env file in the executable directory
    env_file = os.path.join(base_dir, '.env')
    if os.path.exists(env_file):
        print(f"[ENV] Loading .env file from: {env_file}")
        try:
            from dotenv import load_dotenv
            load_dotenv(env_file)
        except ImportError:
            print("[ENV] dotenv module not found, reading .env file manually")
            # Manual parsing of .env file
            with open(env_file, 'r') as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith('#'):
                        key, value = line.split('=', 1)
                        os.environ[key.strip()] = value.strip().strip('"\'')
    else:
        print(f"[ENV] .env file not found at {env_file}")
else:
    # Running as script
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except ImportError:
        print("[ENV] dotenv module not found")

import uvicorn

if __name__ == "__main__":
    # Print the database URL (masked for security)
    db_url = os.environ.get("DATABASE_URL", "Not set")
    if db_url != "Not set":
        # Simple masking of password component
        if "@" in db_url:
            parts = db_url.split("@")
            masked_url = parts[0].split(":")[0] + ":******@" + parts[1]
            print(f"[ENV] Database URL: {masked_url}")
        else:
            print("[ENV] Database URL is set (no @ symbol)")
    else:
        print("[ENV] DATABASE_URL environment variable is not set")
    
    # Start the uvicorn server
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)