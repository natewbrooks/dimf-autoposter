# -*- mode: python ; coding: utf-8 -*-
import os
from PyInstaller.utils.hooks import collect_all

# List all the local modules your app depends on
local_modules = [
    'main.py',
    'database.py',
    # Add any other .py files your app needs
]

# Environment files
env_files = [
    '.env',           # Main .env file
]

# Include all dependencies from your requirements.txt
packages = [
    'uvicorn',
    'fastapi',
    'starlette',
    'pydantic',
    'sqlalchemy',
    'anyio',
    'sniffio',
    'click',
    'h11',
    'idna',
    'openpyxl',
    'requests',
    'PyYAML',
    'charset_normalizer',
    'urllib3',
    'certifi',
    'greenlet',
    'pymysql',
    'cryptography'
]

# Modules that aren't structured as packages
modules = [
    'python-dotenv',
    'typing_extensions',
    'google_search_results',
    'bcrypt'
]

# Add all your local modules as data files
all_datas = [(module, '.') for module in local_modules]
# Add environment files
all_datas.extend([(env_file, '.') for env_file in env_files if os.path.exists(env_file)])

all_binaries = []
all_hiddenimports = [
    'apis',  # Include the apis package
    'apis.posts'  # Include the posts subpackage
]

# Handle importing all the Python files in the 'apis' directory
apis_dir = 'apis'
if os.path.exists(apis_dir) and os.path.isdir(apis_dir):
    # Include the apis directory and its __init__.py
    all_datas.append((apis_dir, apis_dir))
    
    # Check for posts subdirectory
    posts_dir = os.path.join(apis_dir, 'posts')
    if os.path.exists(posts_dir) and os.path.isdir(posts_dir):
        # Include the posts directory and its __init__.py
        all_datas.append((posts_dir, posts_dir))
        
        # Add all Python files in the posts directory
        posts_files = [os.path.join(posts_dir, f) for f in os.listdir(posts_dir) 
                    if f.endswith('.py') and os.path.isfile(os.path.join(posts_dir, f))]
        
        for posts_file in posts_files:
            # Add as data file with directory structure preserved
            all_datas.append((posts_file, os.path.dirname(posts_file)))
            
            # Add as hidden import
            module_path = posts_file.replace('\\', '.').replace('/', '.').replace('.py', '')
            all_hiddenimports.append(module_path)
    
    # Add all Python files directly in the apis directory
    api_files = [os.path.join(apis_dir, f) for f in os.listdir(apis_dir) 
                if f.endswith('.py') and os.path.isfile(os.path.join(apis_dir, f))]
    
    for api_file in api_files:
        # Add as data file with directory structure preserved
        all_datas.append((api_file, os.path.dirname(api_file)))
        
        # Add as hidden import
        module_path = api_file.replace('\\', '.').replace('/', '.').replace('.py', '')
        all_hiddenimports.append(module_path)

# Collect all package dependencies
for package in packages:
    try:
        datas, binaries, hiddenimports = collect_all(package)
        all_datas.extend(datas)
        all_binaries.extend(binaries)
        all_hiddenimports.extend(hiddenimports)
    except Exception as e:
        print(f"Warning: Error collecting {package}: {e}")

# Add the modules that aren't structured as packages
all_hiddenimports.extend(modules)
# Also add alternative underscore/hyphen versions
all_hiddenimports.extend([m.replace('-', '_') for m in modules])

# Add specific imports that might be missed
additional_imports = [
    'uvicorn.lifespan',
    'uvicorn.lifespan.on',
    'uvicorn.lifespan.off',
    'uvicorn.protocols',
    'uvicorn.protocols.http',
    'uvicorn.protocols.http.auto',
    'uvicorn.protocols.websockets',
    'uvicorn.protocols.websockets.auto',
    'uvicorn.logging',
    'uvicorn.loops',
    'uvicorn.loops.auto',
    'fastapi.middleware',
    'fastapi.middleware.cors',
    'fastapi.middleware.gzip',
    'fastapi.middleware.httpsredirect',
    'fastapi.middleware.trustedhost',
    'fastapi.middleware.wsgi',
    'starlette.middleware',
    'starlette.middleware.cors',
    'starlette.middleware.errors',
    'starlette.middleware.gzip',
    'starlette.middleware.sessions',
    'starlette.middleware.trustedhost',
    'starlette.middleware.wsgi',
    'dotenv',
    'pydantic_core',
    'database'
]

all_hiddenimports.extend(additional_imports)

a = Analysis(
    ['entry.py'],
    pathex=[],
    binaries=all_binaries,
    datas=all_datas,
    hiddenimports=all_hiddenimports,
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    noarchive=False,
)

pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='dimf-server',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=True,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)