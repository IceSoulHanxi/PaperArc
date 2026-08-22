#!/usr/bin/env python3
"""Scan dev/paperarc/mixin/common for *Mixin.java files and regenerate
the "mixins" array in paperarc-common.mixins.json deterministically."""
import json, os, sys

ROOT = os.path.join(os.path.dirname(__file__), "..")
PKG_DIR = os.path.normpath(os.path.join(ROOT, "common/src/main/java/dev/paperarc/mixin/common"))
JSON_PATH = os.path.normpath(os.path.join(ROOT, "common/src/main/resources/paperarc-common.mixins.json"))

entries = []
for dirpath, _, files in os.walk(PKG_DIR):
    for f in sorted(files):
        if f.endswith("Mixin.java"):
            rel = os.path.relpath(dirpath, PKG_DIR)
            name = f[:-5]
            entries.append(name if rel == "." else f"{rel.replace(os.sep, '.')}.{name}")

# keep original order for existing entries, append new ones sorted
with open(JSON_PATH) as fh:
    cfg = json.load(fh)
existing = [e for e in cfg["mixins"] if e in entries]
new = sorted(set(entries) - set(existing))
cfg["mixins"] = existing + new
with open(JSON_PATH, "w") as fh:
    json.dump(cfg, fh, indent=2)
    fh.write("\n")
print(f"registered {len(existing)} existing + {len(new)} new = {len(cfg['mixins'])} mixins")
for n in new:
    print("  +", n)
