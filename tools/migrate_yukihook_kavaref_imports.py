#!/usr/bin/env python3
"""Migrate legacy LuckyTool KavaRef calls to YukiHook 1.5.

The modern KavaRef MemberResolver.hook extension needs a context parameter.
Using resolver.self.hook instead delegates to the same YukiHook native
Member.hook overload without shadowing the original onHook() receiver.
This script is idempotent and keeps Hook callback bodies unchanged.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
source_dir = root / "app/src/main/java"
imports_by_usage = {
    "toClass": r"\.toClass\s*\(",
    "toClassOrNull": r"\.toClassOrNull\s*\(",
    "invokeOriginal": r"\.invokeOriginal\s*\(",
    "invokeOriginalQuietly": r"\.invokeOriginalQuietly\s*\(",
}
changed = 0

for path in source_dir.rglob("*.kt"):
    original = path.read_text(encoding="utf-8")
    if "YukiBaseHooker" not in original:
        continue
    content = original
    content = re.sub(
        r"(?<!\.self)(?<!\.self\?)(?P<nullable>\?)?\.hookAll\s*(?=\{)",
        lambda m: "?.map { it.self }?.hookAll " if m.group("nullable")
        else ".map { it.self }.hookAll ",
        content,
    )
    content = re.sub(
        r"(?<!\.self)(?<!\.self\?)(?P<nullable>\?)?\.hook\s*(?=\{)",
        lambda m: "?.self?.hook " if m.group("nullable")
        else ".self.hook ",
        content,
    )
    imports = []
    for name, pattern in imports_by_usage.items():
        if not re.search(pattern, content):
            continue
        if f"import com.highcapable.kavaref.extension.{name}" in content:
            continue
        wanted = f"import com.highcapable.yukihookapi.extension.kavaref.{name}"
        if wanted not in content:
            imports.append(wanted)
    if imports:
        m = re.search(r"^package[ \t]+[^\r\n]+\r?\n", content, re.MULTILINE)
        if not m:
            raise RuntimeError(f"Missing package statement: {path}")
        content = content[:m.end()] + "\n".join(sorted(imports)) + "\n" + content[m.end():]
    if content != original:
        path.write_text(content, encoding="utf-8")
        changed += 1

print(f"Migrated {changed} Kotlin hook files for YukiHook 1.5")
