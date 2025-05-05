import os
import sys

def load_embedded_env():
    """
    Load .env file from inside PyInstaller bundle if present.
    """
    try:
        from dotenv import load_dotenv
    except ImportError:
        print("[ENV] python-dotenv not installed, skipping .env load")
        return

    if getattr(sys, 'frozen', False):
        # Running as PyInstaller exe
        bundle_dir = sys._MEIPASS
    else:
        # Running as script
        bundle_dir = os.path.abspath(os.path.dirname(__file__))

    env_path = os.path.join(bundle_dir, 'config', '.env')

    if os.path.exists(env_path):
        print(f"[ENV] Loading embedded .env from: {env_path}")
        load_dotenv(env_path)
    else:
        print(f"[ENV] WARNING: .env not found at: {env_path}")

load_embedded_env()

# Print masked DB URL
db_url = os.environ.get("DATABASE_URL", "Not set")
if db_url != "Not set" and "@" in db_url:
    user_info, host_info = db_url.split("@")
    user, _ = user_info.split(":", 1)
    masked_url = f"{user}:****@{host_info}"
    print(f"[ENV] DATABASE_URL: {masked_url}")
else:
    print(f"[ENV] DATABASE_URL: {db_url}")


# Run server
import uvicorn
if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)
