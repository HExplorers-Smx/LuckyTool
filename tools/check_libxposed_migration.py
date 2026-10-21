#!/usr/bin/env python3
"""Static check for the API 102/YukiHook 1.5.0 migration, not a device test."""
from pathlib import Path
import re
import tomllib
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
versions = tomllib.loads((root / 'gradle/libs.versions.toml').read_text())
assert versions['versions']['yukihook'] == '1.5.0'
assert 'extra["compileSdkVersion"] = 37' in (root / 'build.gradle.kts').read_text()
for gradle in ('app/build.gradle.kts', 'colorpicker/build.gradle.kts', 'hidden-api-stub/build.gradle.kts'):
    assert 'minorApiLevel = 2' in (root / gradle).read_text(), gradle
assert versions['libraries']['libxposed-api']['version'] == '102.0.0'
for name in ('yukihook-bom', 'yukihook-core', 'yukihook-compiler', 'yukihook-runtime-libxposed', 'yukihook-extension-kavaref'):
    assert name in versions['libraries'], name
assert 'yukihook-runtime-xposed82' not in versions['libraries']
app = (root / 'app/build.gradle.kts').read_text()
for name in ('implementation(libs.yukihook.runtime.libxposed)', 'ksp(libs.yukihook.compiler)', 'compileOnly(libs.libxposed.api)', 'implementation(libs.yukihook.extension.kavaref)'):
    assert name in app, name
hook = (root / 'app/src/main/java/com/luckyzyx/luckytool/hook/MainHook.kt').read_text()
assert '@YukiHookLibXposedEntry(' in hook
assert 'minApiVersion = 102' in hook and 'targetApiVersion = 102' in hook
assert 'registerFrameworkEvents {' in hook and 'loadSystem(HookAndroid)' in hook
m = re.search(r'\bscope\s*=\s*\[([^]]+)\]', hook, re.S)
assert m, 'No declared scope'
scope = set(re.findall(r'"([^"]+)"', m.group(1)))
targets = set(re.findall(r'\bloadApp\(\s*"([^"]+)"', hook))
assert not (targets - scope), f'Missing scope entries: {targets - scope}'
assert 'system' in scope
manifest = ET.parse(root / 'app/src/main/AndroidManifest.xml').getroot()
android = '{http://schemas.android.com/apk/res/android}'
legacy = {'xposedmodule','xposeddescription','xposedminversion'}
assert not [m for m in manifest.findall('.//meta-data') if m.get(android + 'name') in legacy]
print('PASS: source-level YukiHook 1.5.0 and libxposed API 102 checks; real build pending')
