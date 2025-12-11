
lines = []
with open('app/src/main/java/com/example/fadesleeptimer/MainActivity.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Keep lines 1-626 (indices 0-625)
# Skip lines 627-820 (indices 626-819)
# Keep lines 821-End (indices 820+)

# Adjusting for 0-based index:
# Line 626 is index 625.
# Line 627 is index 626.
# Line 820 is index 819.
# Line 821 is index 820.

part1 = lines[:626]
part2 = lines[820:]

with open('app/src/main/java/com/example/fadesleeptimer/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.writelines(part1 + part2)

print(f"Wrote {len(part1) + len(part2)} lines.")
