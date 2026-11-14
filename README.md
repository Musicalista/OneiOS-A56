# OneiOS for Samsung Galaxy A56 5G (SM-A566E / a56x)

[![Device: Galaxy A56 5G](https://img.shields.io/badge/Device-Samsung%20Galaxy%20A56%205G-blue.svg)](https://github.com/Musicalista/OneiOS-A56)
[![SoC: Exynos 1580](https://img.shields.io/badge/SoC-Exynos%201580%20(4nm)-orange.svg)](https://github.com/Musicalista/OneiOS-A56)
[![GPU: AMD Xclipse 540](https://img.shields.io/badge/GPU-Samsung%20Xclipse%20540%20(RDNA3)-red.svg)](https://github.com/Musicalista/OneiOS-A56)
[![Knox: 0x0 Safe](https://img.shields.io/badge/Knox-0x0%20Intact-brightgreen.svg)](https://github.com/Musicalista/OneiOS-A56)

**OneiOS** brings an authentic iOS 27 userland (Darwin Kernel 25.5.0, XNU, `launchd`, `backboardd`, and `SpringBoard`) to the **Samsung Galaxy A56 5G (`SM-A566E`)** running alongside Samsung One UI 7.

Unlike traditional ROMs or bare-metal replacements, **OneiOS does not flash partitions or wipe user data**. The iOS userland runs hosted on top of One UI's Linux kernel via a specialized userland XNU host (`xnuhost`), keeping your **Knox warranty bit at 0x0 intact**.

---

## Highlights & Features on Galaxy A56 5G

- **Zero Knox Tripping:** Runs in userland with `ro.boot.warranty_bit: 0` and `verifiedbootstate: green`. Samsung Pay/Wallet and Knox Secure Folder remain untouched.
- **Exynos 1580 (4nm) CPU Boost:** Automatically pins all 8 cores to peak factory clock speeds during sessions:
  - 1x Cortex-X4 @ **2.91 GHz** (Prime)
  - 3x Cortex-A720 @ **2.60 GHz** (Performance)
  - 4x Cortex-A520 @ **1.95 GHz** (Efficiency)
- **AMD RDNA3 (Xclipse 540) GPU Validation:** Validated Direct Rendering Manager (DRM) `amdgpu` driver support over `/dev/dri/renderD128` with PRIME `dma-buf` memory import.
- **One UI / iOS Hybrid Manager APK:** Precompiled management app with Dynamic Island status indicator, hardware spec detection, and built-in terminal log.
- **Failsafe Return Chord:** Hold **Volume Up + Volume Down for 3 seconds** at any time to instantly restore One UI in place.

---

## Quick Start & Installation

### Option A: Install via APK + Dirtyfrag (Rootless Softmod)
1. Download [app/OneiOS-A56.apk](app/OneiOS-A56.apk) and install it on your Galaxy A56 5G.
2. Obtain root privilege using the **Dirtyfrag** softmod tool or **KernelSU** (`u:r:ksu:s0`).
3. Deploy the runtime and image files to `/data/local/tmp/oneios/`:
   ```bash
   adb push runtime/ /data/local/tmp/oneios/runtime/
   adb push image/ /data/local/tmp/oneios/image/
   adb push scripts/phone/oneios_switch_a56.sh /data/local/tmp/oneios/oneios_switch.sh
   ```
4. Verify system integrity:
   ```bash
   adb shell su -c "/data/local/tmp/oneios/oneios_switch.sh --check"
   # Must return: PREFLIGHT OK
   ```
5. Open the **OneiOS Manager** app on your phone and tap **INICIAR SESSÃO IOS**.

---

## GPU Acceleration & Diagnostic Tool

The Galaxy A56 uses an AMD RDNA3-derived GPU (Samsung Xclipse 540) rather than Qualcomm Adreno. We provide a standalone hardware test tool to verify DRM PRIME buffer sharing:

```bash
# Push and execute the native GPU test tool
adb push tools/test_a56_gpu /data/local/tmp/test_a56_gpu
adb shell su -c "chmod 0755 /data/local/tmp/test_a56_gpu && /data/local/tmp/test_a56_gpu"
```

**Expected output:**
```
=== SAMSUNG GALAXY A56 5G (EXYNOS 1580 / SGPU) GPU TEST ===

[1] Opening DRM render node: /dev/dri/renderD128 ...
[+] Successfully opened /dev/dri/renderD128 (fd=3)
[+] DRM Driver: 'amdgpu' (version 3.49.0, date: '20150101')
[+] Description: 'AMD GPU'
[+] DRM_CAP_PRIME support: 0x3 (import=YES, export=YES)

[2] Testing memory buffer creation via /dev/udmabuf...
[+] Opened /dev/udmabuf (fd=4)
[+] Successfully allocated 1MB udmabuf dma-buf (fd=6)
[3] Importing dma-buf into /dev/dri/renderD128 via PRIME_FD_TO_HANDLE...
[+] SUCCESS: dma-buf imported into SGPU! GEM Handle: 1

=== TEST COMPLETED ===
```

And test the native xnuhost AMDGPU hardware integration:
```sh
adb shell su -c "/data/local/tmp/oneios/runtime/bin/xnuhost --selftest-xclipse"
```
**Expected output:**
```
PASS VirtIO response pool shares guest RAM and clears recycled ranges
PASS mapped imported guest memory into Xclipse GPU VA
PASS submitted native AMDGPU command buffer, sequence 1
PASS native AMDGPU fence completed
```

---

## Repository Structure

```
├── app/
│   ├── OneiOS-A56.apk          # Precompiled, signed manager APK
│   └── src/Manager.java         # Source code of the One UI / iOS hybrid app
├── docs/
│   ├── a56-guide.md             # Detailed hardware & porting guide
│   ├── architecture.md          # OneiOS architectural documentation
│   └── gpu.md                   # GPU / DRM native context specification
├── profiles/
│   └── ios-27.0.1-a56.toml      # Galaxy A56 configuration profile
├── scripts/
│   └── phone/
│       └── oneios_switch_a56.sh # Production switcher with CPU lock and failsafes
└── tools/
    ├── test_a56_gpu             # Native ARM64 GPU diagnostic test binary
    └── test_a56_gpu.c           # Source code for GPU PRIME test
```

---

## Safety & Disclaimers

- **Knox Safety:** OneiOS does not touch `/dev/block/by-name/boot` or any critical partitions. It does not trip Knox (`warranty_bit: 0`).
- **Data Protection:** Your Android `/data` partition remains untouched.
- **Disclaimer:** OneiOS is an independent research project. It is **not affiliated with or endorsed by Apple Inc. or Samsung Electronics**.

---

## License

Derived from the Apple Public Source License 2.0 ([LICENSE.APSL](LICENSE.APSL)) and MIT ([LICENSE](LICENSE)).
