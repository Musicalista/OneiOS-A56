import os
import subprocess
import shutil
import zipfile

APP_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_DIR = os.path.join(APP_DIR, "src")
CLASSES_DIR = os.path.join(APP_DIR, "build", "classes")
DEX_DIR = os.path.join(APP_DIR, "build", "dex")
SDK_JAR = r"C:\Users\Usuario\AppData\Local\Android\Sdk\platforms\android-35\android.jar"
BUILD_TOOLS = r"C:\Users\Usuario\AppData\Local\Android\Sdk\build-tools\35.0.0"

AAPT = os.path.join(BUILD_TOOLS, "aapt.exe")
D8 = os.path.join(BUILD_TOOLS, "d8.bat")
ZIPALIGN = os.path.join(BUILD_TOOLS, "zipalign.exe")
APKSIGNER = os.path.join(BUILD_TOOLS, "apksigner.bat")
KEYSTORE = os.path.join(APP_DIR, "test.jks")
MANIFEST = os.path.join(APP_DIR, "AndroidManifest.xml")
OUT_APK = os.path.join(APP_DIR, "OneiOS-A56.apk")

if os.path.exists(CLASSES_DIR):
    shutil.rmtree(CLASSES_DIR)
os.makedirs(CLASSES_DIR, exist_ok=True)

if os.path.exists(DEX_DIR):
    shutil.rmtree(DEX_DIR)
os.makedirs(DEX_DIR, exist_ok=True)

print("[1] Compiling Manager.java with javac...")
java_src = os.path.join(SRC_DIR, "Manager.java")
cmd_javac = ["javac", "-cp", SDK_JAR, "-d", CLASSES_DIR, java_src]
res = subprocess.run(cmd_javac, capture_output=True, text=True)
if res.returncode != 0:
    print("JAVAC ERROR:\n", res.stderr)
    exit(1)
print("Javac compiled cleanly.")

print("[2] Running D8 to create classes.dex...")
class_files = []
for root, dirs, files in os.walk(CLASSES_DIR):
    for f in files:
        if f.endswith(".class"):
            class_files.append(os.path.join(root, f))
cmd_d8 = [D8, "--output", DEX_DIR] + class_files
res = subprocess.run(cmd_d8, capture_output=True, text=True, shell=True)
if res.returncode != 0:
    print("D8 ERROR:\n", res.stderr)
    exit(1)
print("D8 generated classes.dex.")

print("[3] Packaging with AAPT...")
unsigned_apk = os.path.join(APP_DIR, "build", "manager-unsigned.apk")
if os.path.exists(unsigned_apk):
    os.remove(unsigned_apk)
cmd_aapt = [AAPT, "package", "-f", "-M", MANIFEST, "-I", SDK_JAR, "-F", unsigned_apk]
res = subprocess.run(cmd_aapt, capture_output=True, text=True)
if res.returncode != 0:
    print("AAPT ERROR:\n", res.stderr)
    exit(1)
print("AAPT created base APK.")

print("[4] Adding classes.dex to APK...")
dex_file = os.path.join(DEX_DIR, "classes.dex")
with zipfile.ZipFile(unsigned_apk, "a") as z:
    z.write(dex_file, "classes.dex")

print("[5] Running Zipalign...")
aligned_apk = os.path.join(APP_DIR, "build", "manager-aligned.apk")
if os.path.exists(aligned_apk):
    os.remove(aligned_apk)
cmd_zipalign = [ZIPALIGN, "-p", "-f", "4", unsigned_apk, aligned_apk]
res = subprocess.run(cmd_zipalign, capture_output=True, text=True)
if res.returncode != 0:
    print("ZIPALIGN ERROR:\n", res.stderr)
    exit(1)
print("Zipalign finished.")

print("[6] Signing with apksigner...")
cmd_sign = [APKSIGNER, "sign", "--ks", KEYSTORE, "--ks-pass", "pass:android", "--key-pass", "pass:android", aligned_apk]
res = subprocess.run(cmd_sign, capture_output=True, text=True, shell=True)
if res.returncode != 0:
    print("APKSIGNER ERROR:\n", res.stderr)
    exit(1)
print("Signed APK successfully.")

shutil.copy2(aligned_apk, OUT_APK)
print(f"Copied final APK to {OUT_APK} ({os.path.getsize(OUT_APK)} bytes).")
