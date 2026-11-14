import os
from PIL import Image

APP_DIR = os.path.dirname(os.path.abspath(__file__))
RES_DIR = os.path.join(APP_DIR, "res")
MASTER_ICON = os.path.join(APP_DIR, "ic_launcher_master.png")

master = Image.open(MASTER_ICON).convert("RGBA")

# Densities
densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

for folder, size in densities.items():
    dir_path = os.path.join(RES_DIR, folder)
    os.makedirs(dir_path, exist_ok=True)
    resized = master.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(os.path.join(dir_path, "ic_launcher.png"), "PNG")
    resized.save(os.path.join(dir_path, "ic_launcher_round.png"), "PNG")
    print(f"Generated {folder}/ic_launcher.png ({size}x{size})")

# Drawable standard
drawable_dir = os.path.join(RES_DIR, "drawable")
os.makedirs(drawable_dir, exist_ok=True)
master.save(os.path.join(drawable_dir, "ic_launcher.png"), "PNG")

# Adaptive foreground: 432x432 with 288x288 center glyph
fg_im = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
glyph = master.resize((288, 288), Image.Resampling.LANCZOS)
fg_im.paste(glyph, ((432 - 288) // 2, (432 - 288) // 2), glyph)
fg_im.save(os.path.join(drawable_dir, "ic_launcher_foreground.png"), "PNG")
print("Generated drawable/ic_launcher_foreground.png (432x432)")

# Colors.xml
values_dir = os.path.join(RES_DIR, "values")
os.makedirs(values_dir, exist_ok=True)
with open(os.path.join(values_dir, "colors.xml"), "w", encoding="utf-8") as f:
    f.write('''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#0B0E17</color>
</resources>
''')
print("Generated values/colors.xml")

# Adaptive mipmap
anydpi_dir = os.path.join(RES_DIR, "mipmap-anydpi-v26")
os.makedirs(anydpi_dir, exist_ok=True)
xml_content = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
'''
with open(os.path.join(anydpi_dir, "ic_launcher.xml"), "w", encoding="utf-8") as f:
    f.write(xml_content)
with open(os.path.join(anydpi_dir, "ic_launcher_round.xml"), "w", encoding="utf-8") as f:
    f.write(xml_content)
print("Generated mipmap-anydpi-v26/ic_launcher.xml")
