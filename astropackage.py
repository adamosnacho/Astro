import os, shutil, random

print(os.getcwd())

jar = input("astro jar -> ")
ver = input("version -> ")
plat = input("platform win/mac/lin -> ")

folder = "Astro " + ver
try:
    os.removedirs(folder)
except:pass

os.mkdir(folder)

shutil.move(jar, folder + "/" + folder + ".jar")
shutil.copytree("rec", folder + "/rec")
shutil.copytree("lib", folder + "/lib")

with open(folder + "/worldInfo.s", "w") as f:
    f.write('world=world\nseed=' + str(random.randrange(0, 100000)))

if plat == "win":
    shutil.copytree("lib/natives-windows", folder + "/natives-windows")

    with open(folder + "/run-astro.bat", "w") as f:
        f.write('cd /d "%~dp0"\njava -Djava.library.path=natives-windows -jar "Astro ' + ver + '.jar"')