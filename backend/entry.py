import os
import sys

# Determine if running as executable or script
if getattr(sys, 'frozen', False):
    # When running as executable (PyInstaller package)
    base_dir = os.path.dirname(sys.executable)
    
    # Try multiple possible locations for .env file
    possible_env_locations = [
        os.path.join(base_dir, '.env'),                   # Same directory as executable
        os.path.join(os.path.dirname(base_dir), '.env'),  # Parent directory of executable
        os.path.abspath('.env'),                          # Current working directory
    ]
    
    env_loaded = False
    for env_path in possible_env_locations:
        if os.path.exists(env_path):
            print(f"[ENV] Found .env file at: {env_path}")
            try:
                from dotenv import load_dotenv
                load_dotenv(env_path)
                env_loaded = True
                break
            except ImportError:
                print("[ENV] dotenv module not found, reading .env file manually")
                # Manual parsing of .env file
                with open(env_path, 'r') as f:
                    for line in f:
                        line = line.strip()
                        if line and not line.startswith('#'):
                            try:
                                key, value = line.split('=', 1)
                                os.environ[key.strip()] = value.strip().strip('"\'')
                            except ValueError:
                                print(f"[ENV] Skipping invalid line in .env file: {line}")
                env_loaded = True
                break
    
    if not env_loaded:
        print("[ENV] WARNING: No .env file found in any of the following locations:")
        for loc in possible_env_locations:
            print(f"  - {loc}")
else:
    # Running as script
    try:
        from dotenv import load_dotenv
        load_dotenv()
        print("[ENV] Loaded .env file in development mode")
    except ImportError:
        print("[ENV] dotenv module not found in development mode")

# Manually override DATABASE_URL for testing if needed
# Uncomment the following line to use a local SQLite database for testing
# os.environ["DATABASE_URL"] = "sqlite:///test.db"

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
            print(f"[ENV] Database URL is set: {db_url}")
    else:
        print("[ENV] DATABASE_URL environment variable is not set")
    
    # Start the uvicorn server
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)