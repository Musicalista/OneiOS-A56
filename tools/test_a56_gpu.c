#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <linux/types.h>

// DRM IOCTL definitions
#define DRM_IOCTL_BASE 'd'
#define DRM_COMMAND_BASE 0xa0

struct drm_version {
    int version_major;
    int version_minor;
    int version_patchlevel;
    size_t name_len;
    char *name;
    size_t date_len;
    char *date;
    size_t desc_len;
    char *desc;
};

#define DRM_IOCTL_VERSION _IOWR(DRM_IOCTL_BASE, 0x00, struct drm_version)

struct drm_get_cap {
    __u64 capability;
    __u64 value;
};
#define DRM_CAP_PRIME 0x5
#define DRM_IOCTL_GET_CAP _IOWR(DRM_IOCTL_BASE, 0x0c, struct drm_get_cap)

struct drm_prime_handle {
    __u32 handle;
    __u32 flags;
    __s32 fd;
};
#define DRM_IOCTL_PRIME_HANDLE_TO_FD _IOWR(DRM_IOCTL_BASE, 0x2d, struct drm_prime_handle)
#define DRM_IOCTL_PRIME_FD_TO_HANDLE _IOWR(DRM_IOCTL_BASE, 0x2e, struct drm_prime_handle)

// udmabuf definitions
struct udmabuf_create {
    __u32 memfd;
    __u32 flags;
    __u64 offset;
    __u64 size;
};
#define UDMABUF_CREATE _IOW('u', 0x42, struct udmabuf_create)

int main(int argc, char **argv) {
    printf("=== SAMSUNG GALAXY A56 5G (EXYNOS 1580 / SGPU) GPU TEST ===\n\n");

    // 1. Check DRM nodes
    const char *node = "/dev/dri/renderD128";
    printf("[1] Opening DRM render node: %s ...\n", node);
    int fd = open(node, O_RDWR | O_CLOEXEC);
    if (fd < 0) {
        printf("[-] Failed to open %s: %s (errno %d)\n", node, strerror(errno), errno);
        return 1;
    }
    printf("[+] Successfully opened %s (fd=%d)\n", node, fd);

    // 2. Query DRM version
    char name[128] = {0};
    char date[128] = {0};
    char desc[256] = {0};
    struct drm_version ver;
    memset(&ver, 0, sizeof(ver));
    ver.name = name; ver.name_len = sizeof(name) - 1;
    ver.date = date; ver.date_len = sizeof(date) - 1;
    ver.desc = desc; ver.desc_len = sizeof(desc) - 1;

    if (ioctl(fd, DRM_IOCTL_VERSION, &ver) < 0) {
        printf("[-] DRM_IOCTL_VERSION failed: %s\n", strerror(errno));
    } else {
        printf("[+] DRM Driver: '%s' (version %d.%d.%d, date: '%s')\n",
               name, ver.version_major, ver.version_minor, ver.version_patchlevel, date);
        printf("[+] Description: '%s'\n", desc);
    }

    // 3. Query PRIME support
    struct drm_get_cap cap;
    memset(&cap, 0, sizeof(cap));
    cap.capability = DRM_CAP_PRIME;
    if (ioctl(fd, DRM_IOCTL_GET_CAP, &cap) < 0) {
        printf("[-] DRM_CAP_PRIME query failed: %s\n", strerror(errno));
    } else {
        printf("[+] DRM_CAP_PRIME support: 0x%llx (import=%s, export=%s)\n",
               (unsigned long long)cap.value,
               (cap.value & 0x1) ? "YES" : "NO",
               (cap.value & 0x2) ? "YES" : "NO");
    }

    // 4. Test udmabuf creation
    printf("\n[2] Testing memory buffer creation via /dev/udmabuf...\n");
    int ufd = open("/dev/udmabuf", O_RDWR | O_CLOEXEC);
    if (ufd < 0) {
        printf("[-] Failed to open /dev/udmabuf: %s\n", strerror(errno));
    } else {
        printf("[+] Opened /dev/udmabuf (fd=%d)\n", ufd);
        
        // Create a sealable memfd
        int mfd = memfd_create("test_gpu_buf", MFD_ALLOW_SEALING);
        if (mfd < 0) {
            printf("[-] memfd_create failed: %s\n", strerror(errno));
        } else {
            size_t buf_size = 1024 * 1024; // 1 MB
            if (ftruncate(mfd, buf_size) < 0) {
                printf("[-] ftruncate failed: %s\n", strerror(errno));
            } else {
                fcntl(mfd, F_ADD_SEALS, F_SEAL_SHRINK);
                
                struct udmabuf_create ucreate;
                memset(&ucreate, 0, sizeof(ucreate));
                ucreate.memfd = mfd;
                ucreate.flags = 0;
                ucreate.offset = 0;
                ucreate.size = buf_size;
                
                int dma_fd = ioctl(ufd, UDMABUF_CREATE, &ucreate);
                if (dma_fd < 0) {
                    printf("[-] UDMABUF_CREATE failed: %s\n", strerror(errno));
                } else {
                    printf("[+] Successfully allocated 1MB udmabuf dma-buf (fd=%d)\n", dma_fd);
                    
                    // 5. Test importing dma_fd into sgpu DRM
                    printf("[3] Importing dma-buf into %s via PRIME_FD_TO_HANDLE...\n", node);
                    struct drm_prime_handle prime;
                    memset(&prime, 0, sizeof(prime));
                    prime.fd = dma_fd;
                    prime.flags = 0;
                    
                    if (ioctl(fd, DRM_IOCTL_PRIME_FD_TO_HANDLE, &prime) < 0) {
                        printf("[-] PRIME_FD_TO_HANDLE failed: %s (errno %d)\n", strerror(errno), errno);
                    } else {
                        printf("[+] SUCCESS: dma-buf imported into SGPU! GEM Handle: %u\n", prime.handle);
                    }
                    close(dma_fd);
                }
            }
            close(mfd);
        }
        close(ufd);
    }

    close(fd);
    printf("\n=== TEST COMPLETED ===\n");
    return 0;
}
