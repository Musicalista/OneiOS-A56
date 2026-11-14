import os
import sys
import time
import subprocess
import hashlib

ADB = r"C:\Program Files (x86)\platform-tools\adb.exe"
SRC = r"C:\Users\Usuario\hakios-xnu-kernel\userland\system.apfs"
DST = "/data/local/tmp/oneios/image/os.img"
CHUNK_TMP_LOCAL = r"C:\Users\Usuario\chunk.tmp"
CHUNK_TMP_REMOTE = "/data/local/tmp/chunk.tmp"
CHUNK_SIZE = 100 * 1024 * 1024 # 100 MB

def run_adb(args):
    cmd = [ADB] + args
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"ADB failed: {cmd}\nstderr: {res.stderr}\nstdout: {res.stdout}")
    return res.stdout.strip()

def main():
    if not os.path.exists(SRC):
        print(f"Error: {SRC} does not exist")
        sys.exit(1)

    total_size = os.path.getsize(SRC)
    print(f"Source: {SRC} ({total_size} bytes)")

    # Ensure remote directory exists and remove any old os.img or chunk.tmp
    run_adb(["shell", f"mkdir -p /data/local/tmp/oneios/image; rm -f {DST} {CHUNK_TMP_REMOTE}"])

    start_time = time.time()
    with open(SRC, "rb") as f:
        chunk_idx = 0
        total_chunks = (total_size + CHUNK_SIZE - 1) // CHUNK_SIZE
        while True:
            data = f.read(CHUNK_SIZE)
            if not data:
                break
            
            chunk_idx += 1
            t0 = time.time()
            with open(CHUNK_TMP_LOCAL, "wb") as cf:
                cf.write(data)
            
            # Push chunk
            run_adb(["push", CHUNK_TMP_LOCAL, CHUNK_TMP_REMOTE])
            
            # Append on remote
            if chunk_idx == 1:
                run_adb(["shell", f"mv {CHUNK_TMP_REMOTE} {DST}"])
            else:
                run_adb(["shell", f"cat {CHUNK_TMP_REMOTE} >> {DST} && rm -f {CHUNK_TMP_REMOTE}"])
            
            elapsed = time.time() - t0
            total_elapsed = time.time() - start_time
            rate = len(data) / (1024 * 1024 * elapsed) if elapsed > 0 else 0
            print(f"[{chunk_idx}/{total_chunks}] {len(data)} bytes ({rate:.1f} MB/s) | total: {total_elapsed:.1f}s")

    # Clean local chunk
    if os.path.exists(CHUNK_TMP_LOCAL):
        os.remove(CHUNK_TMP_LOCAL)

    # Check size
    remote_size = run_adb(["shell", f"stat -c %s {DST}"])
    print(f"Remote size: {remote_size} (expected {total_size})")
    if int(remote_size) != total_size:
        print("ERROR: Remote size mismatch!")
        sys.exit(1)

    print("Verifying SHA256...")
    remote_sha = run_adb(["shell", f"sha256sum {DST}"]).split()[0]
    print(f"Remote SHA256: {remote_sha}")
    expected_sha = "016a3c67afe917a82e87de4f6c8df88358e1cfae73427ec8354fd0ccace1e97d"
    if remote_sha.lower() == expected_sha.lower():
        print("SUCCESS: SHA256 matched perfectly!")
    else:
        print(f"ERROR: SHA256 mismatch!\nExpected: {expected_sha}\nGot:      {remote_sha}")
        sys.exit(1)

if __name__ == "__main__":
    main()
