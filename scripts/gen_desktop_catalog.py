"""Generate desktop Catalog.java from ProductCatalogSeed.kt"""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SEED = ROOT / "app/src/main/java/com/middin/innovatie/app/data/local/ProductCatalogSeed.kt"
OUT = ROOT / "desktop/src/main/java/com/middin/innovatie/desktop/Catalog.java"


def java_string_literal(value: str) -> str:
    if '"""' in value:
        escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
        return f"\"{escaped}\""
    if "\n" in value or '"' in value:
        return '"""' + value + '"""'
    return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"'


def extract_entry_blocks(text: str) -> list[str]:
    blocks: list[str] = []
    i = 0
    while True:
        start = text.find("Entry(", i)
        if start < 0:
            break
        depth = 0
        j = start
        while j < len(text):
            ch = text[j]
            if ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0:
                    blocks.append(text[start : j + 1])
                    i = j + 1
                    break
            j += 1
        else:
            break
    return blocks


def parse_entry(block: str) -> tuple[str, str] | None:
    nm = re.search(r'name = "([^"]+)"', block)
    if not nm:
        return None
    dm = re.search(r"description = (.*)\)\s*$", block, re.DOTALL)
    if not dm:
        return None
    desc = "".join(re.findall(r'"([^"]*)"', dm.group(1)))
    return nm.group(1), desc


text = SEED.read_text(encoding="utf-8")
entries = [e for b in extract_entry_blocks(text) if (e := parse_entry(b)) is not None]

OUT.parent.mkdir(parents=True, exist_ok=True)
lines = [
    "package com.middin.innovatie.desktop;",
    "",
    "import java.util.List;",
    "",
    "public final class Catalog {",
    "    public record Product(String name, String description) {}",
    "",
    "    public static List<Product> all() {",
    "        return List.of(",
]
for idx, (name, desc) in enumerate(entries):
    comma = "," if idx < len(entries) - 1 else ""
    lines.append(
        f"            new Product({java_string_literal(name)}, {java_string_literal(desc)}){comma}"
    )
lines += ["        );", "    }", "", "    private Catalog() {}", "}"]
OUT.write_text("\n".join(lines), encoding="utf-8")
print(f"Wrote {len(entries)} products to {OUT}")
