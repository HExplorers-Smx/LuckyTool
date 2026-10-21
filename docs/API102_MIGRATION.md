# LuckyTool API 102 migration (YukiHook 1.5.0 stable)

- Based on the upstream LuckyTool main branch forked by HExplorers-Smx.
- Updates YukiHook BOM from 1.5.0-beta.8 to 1.5.0 stable; keeps libxposed 102.0.0, module entry, and all original Hook implementation files.
- Adds com.oplus.themestore to the declared scope; replaces obsolete Xposed 82 manifest metadata with generated META-INF/xposed files.
- Makes a clean clone buildable without the author's private release keystore. Debug signing is different from upstream release signing and **cannot normally upgrade an installed official APK in place**.
- Prevents Gradle configuration from changing version.properties automatically.
- CI triggers on the migration branch and checks the built APK for generated API 102 module metadata.
- This PR is a test migration. Compile result and runtime compatibility with Android / ColorOS still require actual CI and device verification. Do not merge as a proven stable release before testing.

- CI now compiles against Android SDK **37.2** (all three modules), required by YukiHook 1.5.0 and latest AndroidX AAR metadata. This does **not** raise targetSdk (28) or minSdk (30). Use `platforms;android-37.2` rather than the nonexistent `platforms;android-37` SDK-manager package.

- YukiHook 1.5 moved KavaRef hook utilities into `yukihook-extension-kavaref`. Run `python3 tools/migrate_yukihook_kavaref_imports.py` once after checkout; the CI workflow runs it before compilation. This is an intermediate automated source migration; generated imports will be committed to the full source after confirming they compile.
