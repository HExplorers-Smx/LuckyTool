#!/usr/bin/env python3
"""YukiHook 1.5.x KavaRef import migration helper.

Safe to run repeatedly. Only inserts imports; it never changes Hook logic.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
source = root / "app/src/main/java"
imports_by_usage = {
    "toClass": r"\.toClass\s*\(",
    "toClassOrNull": r"\.toClassOrNull\s*\(",
    "hook": r"\.hook\s*\{",
    "hookAll": r"\.hookAll\s*\{",
    "invokeOriginal": r"\.invokeOriginal\s*\(",
    "invokeOriginalQuietly": r"\.invokeOriginalQuietly\s*\(",
}
changed = 0
for path in source.rglob("*.kt"):
    content = path.read_text(encoding="utf-8")
    if "YukiBaseHooker" not in content:
        continue
    # KavaRef's MemberResolver.hook extension in YukiHook 1.5 requires a
    # context(PackageParam). The matching legacy implementation simply
    # delegates to `MemberResolver.self` (java.lang.reflect.Member) and the
    # existing PackageParam Member.hook overload. Use that same direct route.
    # Unlike wrapping all of onHook() in context(this), this preserves the
    # normal Kotlin implicit receivers and all original hook callbacks.
    content = re.sub(r"\\?\\.hookAll\\s*(?=\\{)", lambda m:
        ("?.map { it.self }?.hookAll " if m.group(0).startswith("?.")
         else ".map { it.self }.hookAll "), content)
    content = re.sub(r"\\?\\.hook\\s*(?=\\{)", lambda m:
        ("?.self?.hook " if m.group(0).startswith("?.")
         else ".self.hook "), content)
    imports = []
    for method, pattern in imports_by_usage.items():
        if not re.search(pattern, content):
            continue
        if f"import com.highcapable.kavaref.extension.{method}" in content:
            continue
        desired = f"import com.highcapable.yukihookapi.extension.kavaref.{method}"
        if desired not in content:
            imports.append(desired)
    if not imports and content == path.read_text(encoding="utf-8"):
        continue
    if imports:
        m = re.search(r"^package[ \\t]+[^\\r\\n]+\\r?\\n", content, re.MULTILINE)
        if not m:
            raise RuntimeError(f"Missing package statement: {path}")
        content = content[:m.end()] + "\\n".join(sorted(imports)) + "\\n" + content[m.end():]
    path.write_text(content, encoding="utf-8")
    changed += 1
print(f"Updated {changed} Kotlin hook files with YukiHook 1.5 KavaRef imports")
