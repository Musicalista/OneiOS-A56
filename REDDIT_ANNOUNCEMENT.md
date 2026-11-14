# [RELEASE] OneiOS on Samsung Galaxy A56 5G (SM-A566E) — Authentic iOS 27 Userland with Zero Knox Tripping (0x0) & AMD Xclipse 540 GPU Support

**Repository:** [https://github.com/Musicalista/OneiOS-A56](https://github.com/Musicalista/OneiOS-A56)

---

Hey everyone,

After days of reverse engineering, kernel bridging, and hardware testing, we are excited to release **OneiOS for the Samsung Galaxy A56 5G (`SM-A566E` / Exynos 1580 / Samsung Xclipse 540 AMD RDNA3)**.

### What is OneiOS?
Unlike traditional ROM ports, GSI builds, or native dual-boot solutions that replace the boot partition, **OneiOS does NOT flash partitions, does NOT require unlocking the bootloader, and does NOT trip Knox (Knox remains 0x0 intact!)**.

OneiOS executes a full, source-built Apple Darwin/XNU kernel as a hosted process (`xnuhost`) directly on top of One UI's Linux kernel. iOS system calls and Mach traps are intercepted and handled in real time, running genuine iOS 27 userland (`launchd`, `backboardd`, `SpringBoard`, `Spotlight`, `MobileCal`, and system daemons).

---

### What Works on the Galaxy A56 5G
- **Zero Knox Tripping:** Operates with `verifiedbootstate: green` and `warranty_bit: 0`. Samsung Pay/Wallet and Secure Folder remain fully functional when returning to Android.
- **Full CPU Frequency Pinning:** Automatically unleashes the 4nm Exynos 1580 cluster:
  - 1x Cortex-X4 @ **2.91 GHz** (Prime)
  - 3x Cortex-A720 @ **2.60 GHz** (Performance)
  - 4x Cortex-A520 @ **1.95 GHz** (Efficiency)
  - Real-time `-20` priority scheduling on `xnuhost`.
- **AMD Xclipse 540 GPU Direct Rendering:** The Exynos 1580 features an AMD RDNA3 GPU. We validated memory buffer sharing (`DRM_CAP_PRIME: 0x3`) via `/dev/dri/renderD128`, importing `udmabuf` dma-bufs directly into GEM handles.
- **One UI / iOS Hybrid Manager APK:** A redesigned Android management app featuring a Dynamic Island status indicator, hardware spec auto-detection, and traffic-light terminal for live logs.
- **Instant Failsafe Return:** Hold **Volume Up + Volume Down for 3 seconds** at any time to instantly restore Samsung One UI in place without rebooting.

---

### Installation via APK + Dirtyfrag / KernelSU

1. **Install the APK:** Download `OneiOS-A56.apk` from the GitHub repository and install it on your device.
2. **Root Access:** Softmod root via **Dirtyfrag** or **KernelSU** (keeping Knox 0x0).
3. **Deploy Runtime & Image:**
   ```bash
   adb push runtime/ /data/local/tmp/oneios/runtime/
   adb push image/ /data/local/tmp/oneios/image/
   adb push scripts/phone/oneios_switch_a56.sh /data/local/tmp/oneios/oneios_switch.sh
   ```
4. **Preflight Check:**
   ```bash
   adb shell su -c "/data/local/tmp/oneios/oneios_switch.sh --check"
   # Output: PREFLIGHT OK
   ```
5. **Launch:** Tap **INICIAR SESSÃO IOS** in the app.

---

### Source Code & GPU Test Tool
All sources, including the native ARM64 GPU validation tool (`test_a56_gpu.c`), the switch scripts, and the Manager APK source, are fully open source on GitHub:

🔗 **GitHub:** [https://github.com/Musicalista/OneiOS-A56](https://github.com/Musicalista/OneiOS-A56)

Feel free to test on your own A56, report issues, and contribute!
