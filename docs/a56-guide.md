# OneiOS on Samsung Galaxy A56 5G (SM-A566E / a56x)

Complete technical documentation for running iOS 27 userland on top of the Samsung Galaxy A56 5G (Exynos 1580 / AMD RDNA3).

---

## 1. Hardware Architecture

| Component | Galaxy A56 5G (`SM-A566E`) | Notes |
| :--- | :--- | :--- |
| **SoC** | Samsung Exynos 1580 (`s5e8855` / `erd8855`) | 4nm EUV FinFET |
| **CPU Cluster** | 1x Cortex-X4 @ 2.91 GHz (policy7)<br>3x Cortex-A720 @ 2.60 GHz (policy4)<br>4x Cortex-A520 @ 1.95 GHz (policy0) | 8 vCPUs mapped to XNU |
| **GPU** | Samsung Xclipse 540 (AMD RDNA3) | Exposed via `/dev/dri/card0` and `/dev/dri/renderD128` |
| **Display** | 1080×2340 @ 120Hz Super AMOLED | Scaled at `@3x` (360×780 points) |
| **Touchscreen** | `/dev/input/event4` (`sec_touchscreen`) | Event driver via `oneios-hid` |
| **Knox State** | 0x0 (`verifiedbootstate: green`, `warranty_bit: 0`) | **Zero Knox tripping** |

---

## 2. Key Differences from Snapdragon (S24 Ultra)

1. **GPU Driver Path:**
   - **Snapdragon:** Uses Qualcomm proprietary `/dev/kgsl-3d0` node and Droid-VM `drm2kgsl` with Turnip.
   - **Exynos 1580 (A56):** Uses Linux standard Direct Rendering Manager (DRM) `amdgpu` driver (v3.49.0) over `/dev/dri/renderD128`.
   - PRIME dma-buf buffer sharing is supported (`DRM_CAP_PRIME: 0x3` - import & export).
   - Test tool: `tools/test_a56_gpu` validates udmabuf import into GEM handles.
   - Built-in bridge: `xnuhost` compiled with `XH_AMDGPU=1` dynamically targets `/dev/dri/renderD128` via `ONEIOS_AMDGPU_RENDER_NODE`.

2. **CPU Clock Pinning:**
   - In One UI, the Energy-Aware Scheduler (EAS) throttles clocks when traps occur.
   - `scripts/phone/oneios_switch_a56.sh` automatically forces `performance` governor and locks all 8 cores at their peak frequencies during the iOS session:
     - Policy 0: 1.95 GHz
     - Policy 4: 2.60 GHz
     - Policy 7: 2.91 GHz
   - Sets `renice -n -20` on `xnuhost` for real-time priority.

3. **Disabled Telemetry Daemons:**
   - In modern iOS 27 userland, daemons such as `com.apple.analyticsd`, `rapportd`, and `parsecd` consume >300% CPU waiting for Apple cloud services.
   - These are suppressed in `profiles/ios-27.0.1-a56.toml` via `boot-contract.json` / `disabled.plist`.

---

## 3. Installation & Usage

### Prerequisites
1. **Samsung Galaxy A56 5G (`SM-A566E`)** running One UI 7.
2. Root access via **Dirtyfrag** (rootless softmod) or **KernelSU** (without tripping Knox 0x0).
3. The **OneiOS-A56 Manager APK** installed.

### Step 1: Install APK
Download and install `app/OneiOS-A56.apk`. Grant root permissions when prompted.

### Step 2: Deploy Image & Runtime
Place the iOS 27 runtime and image in `/data/local/tmp/oneios`:
```sh
adb push runtime/ /data/local/tmp/oneios/runtime/
adb push image/ /data/local/tmp/oneios/image/
adb push scripts/phone/oneios_switch_a56.sh /data/local/tmp/oneios/oneios_switch.sh
```

### Step 3: Run Preflight Check
```sh
su -c '/data/local/tmp/oneios/oneios_switch.sh --check'
# Output must be: PREFLIGHT OK
```

### Step 4: Start Session
Open the OneiOS Manager app on your Galaxy A56 and tap **INICIAR SESSÃO IOS**.

### Safety & Returning to One UI
To exit the iOS session and immediately return to Samsung One UI:
* **Hold Volume Up + Volume Down together for 3 seconds.**
* Or hold **Power + Volume Down** for 7 seconds to force reboot.
