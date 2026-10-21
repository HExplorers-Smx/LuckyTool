package com.hexplorers.splashguard.hook

/** Pure text rules: reject login/privacy/payment pages before scanning. */
internal object SkipMatcher {
    private val candidatePattern = Regex(
        "^(?:跳过(?:广告)?(?:[0-9]{1,2}(?:s|秒)?)?|[0-9]{1,2}(?:s|秒)?跳过|skip(?:ad)?(?:[0-9]{1,2}s?)?)$",
        RegexOption.IGNORE_CASE
    )
    private val countdownPattern = Regex("[0-9]{1,2}(?:s|秒)?", RegexOption.IGNORE_CASE)
    private val bannedActivityName = Regex(
        "(?:login|signin|password|payment|checkout|confirm|permission|privacy|consent|verify|auth|captcha|biometric)",
        RegexOption.IGNORE_CASE
    )

    fun isPotentialAdSkip(text: CharSequence): Boolean {
        val compact = text.toString().replace(Regex("[\\s　:：·|]+"), "")
        return compact.length in 2..18 && candidatePattern.matches(compact)
    }

    fun isAdCountDown(text: String): Boolean = countdownPattern.containsMatchIn(text)
    fun isSensitiveActivity(className: String): Boolean = bannedActivityName.containsMatchIn(className)
    fun isSplashActivity(className: String): Boolean =
        className.contains("splash", true) || className.contains("advert", true) ||
            className.contains("startad", true)
}
