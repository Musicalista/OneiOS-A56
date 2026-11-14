# [RELEASE] OneiOS on Samsung Galaxy A56 5G (SM-A566E) — Authentic iOS 16.5 Userland with Zero Knox Tripping (0x0) & AMD Xclipse 540 GPU Support

**Repository:** [https://github.com/Musicalista/OneiOS-A56](https://github.com/Musicalista/OneiOS-A56)

---

Hey everyone,

After days of reverse engineering, kernel bridging, and hardware testing, we are excited to release **OneiOS for the Samsung Galaxy A56 5G (`SM-A566E` / Exynos 1580 / Samsung Xclipse 540 AMD RDNA3)**.

### What is OneiOS?
Unlike traditional ROM ports, GSI builds, or native dual-boot solutions that replace the boot partition, **OneiOS does NOT flash partitions, does NOT require unlocking the bootloader, and does NOT trip Knox (Knox remains 0x0 intact!)**.

OneiOS executes a full, source-built Apple Darwin/XNU kernel as a hosted process (`xnuhost`) directly on top of One UI's Linux kernel. iOS system calls and Mach traps are intercepted and handled in real time, running genuine iOS 16.5 userland (`launchd`, `backboardd`, `SpringBoard`, `Spotlight`, `MobileCal`, and system daemons).

---

### What Works on the Galaxy A56 5G
- **Zero Knox Tripping:** Operates with `verifiedbootstate: green` and `warranty_bit: 0`. Samsung Pay/Wallet and Secure Folder remain fully functional when returning to Android.
- **Ultra-Optimized iOS 16.5 (20F66):** 2.4 GB root APFS footprint with zero zRAM thrashing, maintaining 60–120 FPS on the Super AMOLED display.
- **Full CPU Frequency Pinning:** Automatically unleashes the 4nm Exynos 1580 cluster:
  - 1x Cortex-X4 @ **2.91 GHz** (Prime)
  - 3x Cortex-A720 @ **2.60 GHz** (Performance)
  - 4x Cortex-A520 @ **1.95 GHz** (Efficiency)
  - Real-time `-20` priority scheduling on `xnuhost`.
- **AMD Xclipse 540 GPU Direct Rendering:** The Exynos 1580 features an AMD RDNA3 GPU. We validated memory buffer sharing (`DRM_CAP_PRIME: 0x3`) via `/dev/dri/renderD128`, importing `udmabuf` dma-bufs directly into GEM handles.
- **One UI / iOS Hybrid Manager APK:** A redesigned Android management app featuring a Dynamic Island status indicator, hardware spec auto-detection, direct in-app GitHub image downloading/importing, and traffic-light terminal for live logs.
- **Instant Failsafe Return:** Hold **Volume Up + Volume Down for 3 seconds** at any time to instantly restore Samsung One UI in place without rebooting.

---

### In-App Installation (No PC Needed)

1. **Install the APK:** Download `OneiOS-A56.apk` from the GitHub Releases and install on your phone.
2. **Root Access:** Grant root via **KernelSU** or **Dirtyfrag** (Knox 0x0 preserved).
3. **Download Image:** Inside the app, tap **📥 BAIXAR / IMPORTAR IMAGEM IOS 16.5** -> **Baixar direto do GitHub Releases**. The app downloads, unpacks, and configures everything automatically!
4. **Launch:** Tap **▶ INICIAR SESSÃO IOS**.

---

### Source Code & GPU Test Tool
All sources, including the native ARM64 GPU validation tool (`test_a56_gpu.c`), the switch scripts, and the Manager APK source, are fully open source on GitHub:

🔗 **GitHub:** [https://github.com/Musicalista/OneiOS-A56](https://github.com/Musicalista/OneiOS-A56)

Feel free to test on your own A56, report issues, and contribute!
