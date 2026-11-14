#!/system/bin/sh
set -e

STAGE="/data/local/tmp/oneios"
IMG_DIR="$STAGE/image"

echo "=== Staging iOS 16.5 image metadata ==="

# 1. Write image.conf
cat << 'EOF' > "$STAGE/image.conf"
format=1
version=16.5
build=20F66
EOF

# 2. Generate image.sha256 and image.ids
cd "$IMG_DIR"

# Clean up any leftover temporary files
rm -f "$STAGE/image.sha256.new" "$STAGE/image.ids.new"

echo "Hashing files..."
find . ! -type d | sed 's|^\./||' | sort | while read -r name; do
    echo "Processing $name..."
    sum=$(sha256sum "$name" | cut -d' ' -f1)
    id=$(stat -c '%d:%i:%s:%Y' "$name")
    echo "$sum  $name" >> "$STAGE/image.sha256.new"
    echo "$id  $name" >> "$STAGE/image.ids.new"
done

# Normalize whitespace (two spaces between sum/id and name)
awk '{ print $1 "  " $2 }' "$STAGE/image.sha256.new" > "$STAGE/image.sha256"
awk '{ print $1 "  " $2 }' "$STAGE/image.ids.new" > "$STAGE/image.ids"
rm -f "$STAGE/image.sha256.new" "$STAGE/image.ids.new"

# 3. Fix permissions
chown -R 0:0 "$STAGE"
chmod -R u=rwX,go=rX "$STAGE"
chmod 755 "$STAGE/runtime/bin/xnuhost"
chmod 755 "$STAGE/oneios_switch.sh"

echo "=== Staging Complete ==="
cat "$STAGE/image.conf"
echo "--- image.sha256 ---"
cat "$STAGE/image.sha256"
echo "--- image.ids ---"
cat "$STAGE/image.ids"
