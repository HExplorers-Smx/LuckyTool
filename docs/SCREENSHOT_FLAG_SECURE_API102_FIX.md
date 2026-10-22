# LuckyTool FLAG_SECURE API 102 fix

## Root cause
In MainHook.kt the outer event gates used the inverse of ModulePrefs.disable_flag_secure, while DisableFlagSecure.java internally expected the *opposite* state. Thus both the on/off states failed to install hooks.

## Changes
- Enable only when disable_flag_secure=true, default OFF, in both MainHook and DisableFlagSecure.
- Fail closed when RemotePreferences are unavailable.
- Avoid installing hooks while disabled on hot reload.
- Bump versionCode to 21833, retain user-facing versionName 1.35.

## Verification
In LSPosed enable system, com.oplus.screenshot and where applicable com.android.systemui; enable the LuckyTool screenshot restriction removal switch, then reboot (system_server is not restarted by a force-close). Test a regular FLAG_SECURE app. DRM and hardware-protected video may still appear black. Observe Xposed logs for LuckyTool-DisableFlagSecure hook errors. This change has no claimed device runtime verification.
