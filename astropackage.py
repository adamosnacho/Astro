import os
import shutil
import random
import sys

if len(sys.argv) > 1 and sys.argv[1] == "zip":
    plats = input("to zip platforms -> ")
    ver = input("version -> ")

    if "win" in plats:
        shutil.make_archive(f"dist/Astro {ver} - win", 'zip', f"Astro {ver} - win")
    if "mac" in plats:
        shutil.make_archive(f"dist/Astro {ver} - mac", 'zip', f"Astro {ver} - mac")
    if "lin" in plats:
        shutil.make_archive(f"dist/Astro {ver} - lin", 'zip', f"Astro {ver} - lin")
    exit()

# Print the current working directory
print(os.getcwd())

# Gather inputs
jar = input("astro jar -> ")
ver = input("version -> ")
plat = input("platform win/mac/lin -> ").lower()

# Create the output folder
folder = f"Astro {ver} - {plat}"
try:
    shutil.rmtree(folder)  # Remove the folder if it exists
except FileNotFoundError:
    pass

os.mkdir(folder)

# Move the jar file into the folder
shutil.copy(jar, os.path.join(folder, f"{folder}.jar"))

# Copy required directories
shutil.copytree("rec", os.path.join(folder, "rec"))
shutil.copytree("lib", os.path.join(folder, "lib"))

# Create a world info file with a randError: Unable to access jarfile Astro dev 1.1.jarom seed
with open(os.path.join(folder, "worldInfo.s"), "w") as f:
    f.write(f'world=world\nseed={random.randrange(0, 100000)}')

# Handle platform-specific files
if plat == "win":
    # Create the Windows batch script
    with open(os.path.join(folder, "run-astro.bat"), "w") as f:
        f.write(f'cd /d "%~dp0"\n')
        f.write(f'java -Djava.library.path=lib\\natives-windows -jar "Astro {ver} - {plat}.jar"')
elif plat == "lin":
    # Create the Linux shell script
    with open(os.path.join(folder, "run-astro.sh"), "w") as f:
        f.write('#!/bin/bash\n')
        f.write('cd "$(dirname "$0")"\n')
        f.write(f'java -Djava.library.path=lib/natives-linux -jar "Astro {ver} - {plat}.jar"\n')
elif plat == "mac":
    # Create the macOS shell script
    with open(os.path.join(folder, "run-astro.sh"), "w") as f:
        f.write('#!/bin/bash\n')
        f.write('cd "$(dirname "$0")"\n')
        f.write(f'java -Djava.library.path=lib/natives-mac -jar "Astro {ver} - {plat}.jar"\n')
else:
    print("Unsupported platform. Please choose win, mac, or lin.")
