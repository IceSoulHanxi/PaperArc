#!/usr/bin/env python3
"""Version CraftBukkit package references inside compiled mixin classes.

paperarc sources compile against the *deobf* spigot artifact where Craft*
classes live under org/bukkit/craftbukkit/v/. At runtime Arclight serves them
under org/bukkit/craftbukkit/<rev>/ (v1_21_R1 for 1.21.1) and does NOT remap
third-party mixin classes, so any raw v/ reference that reaches a resolved
class explodes with NoClassDefFoundError during bootstrap.

This tool rewrites every CONSTANT_Utf8 entry matching the unversioned prefix
to the versioned one across all .class files in a jar.
"""
import sys, zipfile, struct, io, shutil

SRC_PREFIX = b"org/bukkit/craftbukkit/v/"
DST_PREFIX_TMPL = b"org/bukkit/craftbukkit/%s/"

def rewrite_class(data: bytes, rev: str) -> bytes:
    if SRC_PREFIX not in data and b"org.bukkit.craftbukkit.v." not in data:
        return data
    dst_prefix = DST_PREFIX_TMPL % rev.encode()
    if len(data) < 10 or data[:4] != b"\xca\xfe\xba\xbe":
        return data
    buf = io.BytesIO(data)
    out = io.BytesIO()
    out.write(buf.read(8))  # magic+minor+major
    (cp_count,) = struct.unpack(">H", buf.read(2))
    out.write(struct.pack(">H", cp_count))
    i = 1
    changed = 0
    while i < cp_count:
        tag = buf.read(1)[0]
        out.write(bytes([tag]))
        if tag == 1:  # Utf8
            (ln,) = struct.unpack(">H", buf.read(2))
            payload = buf.read(ln)
            if SRC_PREFIX in payload:
                payload = payload.replace(SRC_PREFIX, dst_prefix)
                changed += 1
            if b"org.bukkit.craftbukkit.v." in payload:
                payload = payload.replace(b"org.bukkit.craftbukkit.v.", b"org.bukkit.craftbukkit.%s." % rev.encode())
                changed += 1
            out.write(struct.pack(">H", len(payload)))
            out.write(payload)
        elif tag in (3, 4):
            out.write(buf.read(4))
        elif tag in (5, 6):
            out.write(buf.read(8)); i += 1  # long/double take two slots
        elif tag in (7, 8, 16, 19, 20):
            out.write(buf.read(2))
        elif tag in (9, 10, 11, 12, 17, 18):
            out.write(buf.read(4))
        elif tag == 15:
            out.write(buf.read(3))
        else:
            raise ValueError(f"unknown cp tag {tag}")
        i += 1
    out.write(buf.read())
    return out.getvalue()

def main():
    jar_in = sys.argv[1]
    rev = sys.argv[2] if len(sys.argv) > 2 else "v1_21_R1"
    jar_out = sys.argv[3] if len(sys.argv) > 3 else None
    tmp = (jar_out or jar_in) + ".tmp"
    total = 0
    with zipfile.ZipFile(jar_in) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename.endswith(".class"):
                new = rewrite_class(data, rev)
                if new != data:
                    total += 1
                data = new
            zout.writestr(item, data)
    shutil.move(tmp, jar_out or jar_in)
    print(f"versioned {total} class files -> {rev}")

if __name__ == "__main__":
    main()
