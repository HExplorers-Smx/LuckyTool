from pathlib import Path
import zipfile
import tomllib

root=Path(__file__).resolve().parents[1]
versions=tomllib.loads((root/'gradle/libs.versions.toml').read_text())
assert versions['versions']['yuki']=='1.5.0'
assert versions['libraries']['libxposed-api']['version']=='102.0.0'
entry=(root/'app/src/main/java/com/hexplorers/splashguard/hook/HookEntry.kt').read_text()
assert 'targetApiVersion = 102' in entry
assert 'staticScope = false' in entry
assert 'ShakeSensorHook(rules)' in entry
apks=list((root/'app/build/outputs/apk/debug').glob('*.apk'))
assert len(apks)==1,apks
with zipfile.ZipFile(apks[0]) as z:
    required={'META-INF/xposed/java_init.list','META-INF/xposed/module.prop','META-INF/xposed/scope.list'}
    assert required.issubset(z.namelist()),required-set(z.namelist())
    assert z.read('META-INF/xposed/java_init.list').strip()
print('PASS: YukiHook 1.5.0 / libxposed API 102 metadata,',apks[0])
assert (root/'app/src/main/java/com/hexplorers/splashguard/CrashRecovery.kt').is_file()
assert (root/'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml').is_file()
