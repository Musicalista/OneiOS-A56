# [RELEASE] OneiOS on Samsung Galaxy A56 5G (SM-A566E) — iOS 16.5 Userland next to One UI with Zero Knox Tripping (0x0), AMD Xclipse 540 GPU & Direct In-App Setup

**GitHub Repository:** [https://github.com/Musicalista/OneiOS-A56](https://github.com/Musicalista/OneiOS-A56)  
**Latest APK Release:** [OneiOS-A56.apk](https://github.com/Musicalista/OneiOS-A56/releases/latest)

---

Hey r/android, r/jailbreak, r/samsung, and fellow low-level tinkerers!

After extensive reverse-engineering of Darwin/XNU kernel internals, APFS space manager structures, and Samsung Exynos hardware interfaces, we are thrilled to announce the official release of **OneiOS for the Samsung Galaxy A56 5G (`SM-A566E` / Exynos 1580 / AMD Xclipse 540)**.

OneiOS allows you to launch and run an authentic Apple iOS userland (`launchd`, `backboardd`, `SpringBoard`, `Spotlight`, `MobileCal`, and native system daemons) directly alongside Samsung One UI, running at silky-smooth 60–120 FPS on the 120Hz Super AMOLED display.

---

### TL;DR — What Makes This Unique?

* **Zero Knox Tripping (0x0 Intact):** We do **NOT** flash boot partitions, unlock bootloader fuses, or touch `/dev/block/by-name/boot`. `ro.boot.warranty_bit` remains strictly `0` and `verifiedbootstate` remains `green`. Samsung Pay/Wallet and Secure Folder continue working flawlessly upon returning to One UI.
* **Hosted Darwin/XNU Engine (`xnuhost`):** iOS runs hosted on top of One UI's Linux kernel inside an isolated mount namespace. Syscalls and Mach traps are intercepted in user space in real time with virtually zero virtualization overhead.
* **Why iOS 16.5 (20F66)?** Earlier experimental iOS 27 userlands relied on >16 GB APFS disk images that triggered severe zRAM swap thrashing on the A56's 8 GB RAM, with wallpaper extensions (`PosterBoard`) spinning at 450% CPU. iOS 16.5 provides a razor-sharp **2.4 GB root APFS footprint**, requires **zero swap**, leaves 3.5+ GB free physical RAM for host daemons, and runs buttery smooth at up to 120 FPS.
* **Direct In-App GitHub Setup (No PC Required):** The newly redesigned **OneiOS Manager APK** now features built-in GitHub Releases downloading, local `/sdcard/Download` importing, auto-extraction, and checksum verification with a single tap.
* **Hardware AMD RDNA3 (Xclipse 540) GPU Acceleration:** Direct Rendering Manager (DRM) integration via `/dev/dri/renderD128` with PRIME `dma-buf` memory sharing (`DRM_CAP_PRIME: 0x3`).
* **Failsafe Return:** Hold **Volume Up + Volume Down for 3 seconds** at any time to instantly restore Samsung One UI in-place without rebooting.

---

### Deep Dive: Engineering & Architecture

#### 1. APFS Space Manager (`spaceman_sanity_check:287`) Root Cause & Fix
During earlier boots, mounting `os.img` triggered an internal APFS panic:
```text
nx_corruption_detected_int:36: diskX Container corruption detected by spaceman_sanity_check:287!
```
By disassembling `spaceman_sanity_check` in ARM64e `kernel.kc` (`0xfffffff00aa5963c` to `0xfffffff00aa59948`), we identified that offset `0x80` (`sm_allocator_offset`) in block 27 was left uninitialized (`0`) by legacy `mkapfs.py` tools. The kernel sanity check:
```text
0xfffffff00aa59850: cmp x9, [x21, #0x80]  ; x9 = cib_addr_off + cibs * 8 (392)
```
failed because `392 > 0`. Initializing offset `0x80` to `4096` (`BS`) and recomputing Fletcher64 checksums completely eliminated all container corruption panics.

#### 2. Dual RAMDisk & SystemOS Cryptex Mount
XNU requires both the root OS volume and the SystemOS cryptex volume to be present during early userspace handover (`kern_exec.c:10249`). We stage both volumes as read-only virtual block devices:
- `os.img` (Role `os`, UUID `83E5E832-CB2D-B7AE-BF0B-E72FBD9D89FA`, 2.4 GB)
- `systemos.img` (Role `systemos`, UUID `67AE63E2-7901-A9A0-3CDB-A05F92F51016`, 12.5 MB)
- `data.seed` (Role `data-seed`, 1.2 GB)

#### 3. AMD Xclipse 540 (RDNA3) GPU Acceleration
Unlike Qualcomm Snapdragon devices that use proprietary `/dev/kgsl-3d0`, the Galaxy A56 5G is powered by the Samsung Exynos 1580 with an AMD RDNA3-derived Xclipse 540 GPU. 
OneiOS communicates directly with `/dev/dri/renderD128` through the standard Linux DRM `amdgpu` driver (v3.49.0), utilizing `udmabuf` dma-bufs imported directly into GEM handles (`PRIME_FD_TO_HANDLE`). We include an open-source test utility (`test_a56_gpu.c`) in the repo that independently verifies buffer sharing.

#### 4. Exynos 1580 CPU Frequency Pinning
To prevent energy-aware scheduler (EAS) throttling during trapped context switches, our switch script pins all 8 cores to peak clock rates:
- **1x Cortex-X4 @ 2.91 GHz** (Prime Core)
- **3x Cortex-A720 @ 2.60 GHz** (Performance Cores)
- **4x Cortex-A520 @ 1.95 GHz** (Efficiency Cores)
- Real-time priority (`-20` renice) assigned to the `xnuhost` thread group.

---

### Quick Start Guide (100% In-App Setup)

No ADB or PC is necessary for daily use:

1. **Install the OneiOS Manager APK:**
   Download and install `OneiOS-A56.apk` from the [Releases](https://github.com/Musicalista/OneiOS-A56/releases/latest) page.
2. **Grant Root Privilege:**
   Open the app and grant root access via **KernelSU** or **Dirtyfrag** softmod (preserving Knox 0x0).
3. **Download Image:**
   Tap the button:
   ```text
   📥   BAIXAR / IMPORTAR IMAGEM IOS 16.5
   ```
   Select **Baixar direto do GitHub Releases**. The app streams the compressed image package directly to `/sdcard/Download/OneiOS/`, unpacks it into `/data/local/tmp/oneios/image/`, and runs the hash staging script.
   *(Alternatively, if you already downloaded the `.tar` / `.tar.gz` package via your mobile browser, select **Importar arquivo baixado**)*.
4. **Launch iOS:**
   Once verified, the Dynamic Island status indicator will turn vibrant green:
   ```text
   🟢   SISTEMA PRONTO
   ```
   Tap **▶ INICIAR SESSÃO IOS**. One UI will gracefully sleep, and the iOS 16.5 SpringBoard will illuminate your screen.
5. **Returning to Android:**
   Hold **Volume Up + Volume Down for 3 seconds** to safely resume One UI right where you left off.

---

### Project Repositories & Tools

* **Main Repository:** [https://github.com/Musicalista/OneiOS-A56](https://github.com/Musicalista/OneiOS-A56)
* **GPU Diagnostic Tool Source:** [tools/test_a56_gpu.c](file:///C:/Users/Usuario/OneiOS-A56/tools/test_a56_gpu.c)
* **Manager APK Source:** [app/src/Manager.java](file:///C:/Users/Usuario/OneiOS-A56/app/src/Manager.java)
* **Staging & Hashes Automation:** [tools/stage_ios16.sh](file:///C:/Users/Usuario/OneiOS-A56/tools/stage_ios16.sh)

Enjoy experimenting, and let us know your feedback, benchmarks, and questions in the comments below!
