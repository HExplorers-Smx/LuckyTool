# KSP generates the libxposed module entry and META-INF/xposed files.
# Avoid stripping the module entry if release minification is later enabled.
-keep class com.hexplorers.splashguard.hook.HookEntry { *; }
