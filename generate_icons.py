import struct, zlib, os

def create_png(width, height, filepath):
    def chunk(chunk_type, data):
        c = chunk_type + data
        crc = struct.pack('>I', zlib.crc32(c) & 0xffffffff)
        return struct.pack('>I', len(data)) + c + crc

    header = b'\x89PNG\r\n\x1a\n'
    ihdr = chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0))

    raw = b''
    for y in range(height):
        raw += b'\x00'
        for x in range(width):
            cx, cy = width // 2, height // 2
            r = min(width, height) // 3
            dist = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if abs(dist - r) < 3:
                raw += b'\xff\xff\xff'
            elif abs(dist - r * 0.6) < 2:
                raw += b'\xff\xff\xff'
            elif abs(x - cx) < 4 and abs(y - cy) < r * 1.2:
                raw += b'\xff\xff\xff'
            else:
                raw += b'\x15\x65\xc0'

    idat = chunk(b'IDAT', zlib.compress(raw))
    iend = chunk(b'IEND', b'')

    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(header + ihdr + idat + iend)

base = r'C:\Users\yangyan\AcousticsCalculator\app\src\main\res'
for density, size in [('mdpi', 48), ('hdpi', 72), ('xhdpi', 96), ('xxhdpi', 144), ('xxxhdpi', 192)]:
    path = os.path.join(base, f'mipmap-{density}', 'ic_launcher.png')
    create_png(size, size, path)
    print(f'Created {path}')

print('Done!')
