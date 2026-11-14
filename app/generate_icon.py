import math
from PIL import Image, ImageDraw, ImageFilter

def create_oneios_icon(size=512):
    # Create high-res 1024x1024 for supersampling
    W = 1024
    H = 1024
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)

    # 1. Dark Obsidian & Deep Sapphire Radial Background
    # Create squircle rounded rectangle mask
    radius = int(W * 0.22) # 22% corner radius (iOS standard superellipse)
    
    # We will draw background directly
    bg = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg)
    
    # Gradient background
    for y in range(H):
        ratio = y / H
        # Deep space dark: from #0B0E17 to #161C2C
        r = int(11 + (22 - 11) * ratio)
        g = int(14 + (28 - 14) * ratio)
        b = int(23 + (44 - 23) * ratio)
        bg_draw.line([(0, y), (W, y)], fill=(r, g, b, 255))

    # Mask with rounded rect
    mask = Image.new("L", (W, H), 0)
    mask_draw = ImageDraw.Draw(mask)
    margin = 40
    rect = [margin, margin, W - margin, H - margin]
    mask_draw.rounded_rectangle(rect, radius=radius, fill=255)

    # Apply mask to background
    squircle = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    squircle.paste(bg, (0, 0), mask)

    # 2. Add subtle inner glow & border on the squircle
    border = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    border_draw = ImageDraw.Draw(border)
    # Electric Cyan/Blue rim
    border_draw.rounded_rectangle(rect, radius=radius, outline=(0, 176, 255, 180), width=6)
    squircle = Image.alpha_composite(squircle, border)

    # 3. Ambient inner radial glow from center
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    cx, cy = W // 2, H // 2
    for rad in range(350, 0, -10):
        alpha = int(45 * (1.0 - rad / 350.0))
        # Cyan-blue glow
        glow_draw.ellipse([cx - rad, cy - rad, cx + rad, cy + rad], fill=(0, 176, 255, alpha))
    glow = glow.filter(ImageFilter.GaussianBlur(15))
    squircle = Image.alpha_composite(squircle, glow)

    # 4. Composite the OneOS / iOS Emblem
    # Let's load oneos_boot_icon.png and composite it with crisp styling
    boot_icon_path = r"C:\Users\Usuario\OneiOS-A56\app\oneos_boot_icon.png"
    try:
        boot_im = Image.open(boot_icon_path).convert("RGBA")
        # Resize to fit nicely inside center (e.g. 560x560)
        boot_size = 580
        boot_im = boot_im.resize((boot_size, boot_size), Image.Resampling.LANCZOS)
        
        # Colorize boot_im to vibrant Apple white / electric blue highlight
        # Add a subtle drop shadow to the boot icon
        shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        shadow.paste((0, 0, 0, 120), ((W - boot_size) // 2, (H - boot_size) // 2 + 12), boot_im.split()[-1])
        shadow = shadow.filter(ImageFilter.GaussianBlur(12))
        squircle = Image.alpha_composite(squircle, shadow)

        # Tint boot icon with gradient or pure crisp white/cyan
        tinted = Image.new("RGBA", (boot_size, boot_size), (255, 255, 255, 0))
        t_draw = ImageDraw.Draw(tinted)
        for y in range(boot_size):
            r_ratio = y / boot_size
            # Top pure white #FFFFFF to bottom cyan-tinted #E1F5FE
            r = int(255 - 20 * r_ratio)
            g = int(255 - 5 * r_ratio)
            b = 255
            t_draw.line([(0, y), (boot_size, y)], fill=(r, g, b, 255))
        
        # Mask with boot_im alpha
        boot_alpha = boot_im.split()[-1]
        boot_final = Image.new("RGBA", (boot_size, boot_size), (0, 0, 0, 0))
        boot_final.paste(tinted, (0, 0), boot_alpha)

        squircle.paste(boot_final, ((W - boot_size) // 2, (H - boot_size) // 2), boot_final)
    except Exception as e:
        print("Note on boot icon:", e)

    # 5. Add "iOS 16" subtle bottom badge/pill inside the icon
    badge_w = 260
    badge_h = 64
    bx1 = (W - badge_w) // 2
    by1 = H - margin - 130
    bx2 = bx1 + badge_w
    by2 = by1 + badge_h
    
    badge_im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(badge_im)
    b_draw.rounded_rectangle([bx1, by1, bx2, by2], radius=32, fill=(0, 230, 118, 220)) # Cupertino/OneUI vibrant green
    squircle = Image.alpha_composite(squircle, badge_im)

    # Downscale with high quality Lanczos to target size
    final_icon = squircle.resize((size, size), Image.Resampling.LANCZOS)
    return final_icon

if __name__ == "__main__":
    icon = create_oneios_icon(512)
    icon.save(r"C:\Users\Usuario\OneiOS-A56\app\ic_launcher_master.png", "PNG")
    print("Master icon generated successfully at 512x512!")
