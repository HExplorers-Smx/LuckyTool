package com.hexplorers.splashguard

import org.json.JSONArray
import org.json.JSONObject

/**
 * Safe, data-only import format. No scripts, selectors, reflection, HTTP, or
 * arbitrary click coordinates. Only a literal skip text + target package.
 */
data class SkipRule(
    val id: String, val name: String, val packageName: String,
    val text: String, val enabled: Boolean
)

object RuleCatalog {
    const val KEY = "rules_json"
    private val pkgPattern = Regex("^[a-zA-Z_][\\w]*(?:\\.[a-zA-Z_][\\w]*){1,10}$")
    private val idPattern = Regex("^[a-zA-Z0-9_-]{1,64}$")
    private val sensitive = Regex(
        "(登录|确认|同意|支付|付款|授权|下一步|允许|继续|验证码|密码|提交|login|pay|agree|allow|confirm)",
        RegexOption.IGNORE_CASE
    )

    fun parse(raw: String): List<SkipRule> {
        if (raw.isBlank()) return emptyList()
        require(raw.length <= 131072) { "规则内容过大（最大 128 KB）" }
        val root = JSONObject(raw)
        require(root.optInt("schema", -1) == 1) { "仅支持简净 schema=1 的规则" }
        val list = root.getJSONArray("rules")
        require(list.length() <= 64) { "规则数量不能超过 64 条" }
        val ids = HashSet<String>()
        return (0 until list.length()).map { idx ->
            val item = list.getJSONObject(idx)
            // Reject unsupported instructions instead of silently accepting
            // action scripts or unsafe rule capabilities.
            val allowed = setOf("id", "name", "packageName", "text", "enabled")
            val keys = item.keys().asSequence().toList()
            require(keys.all(allowed::contains)) { "规则存在不支持的字段" }
            val id = item.getString("id").trim()
            val name = item.optString("name", id).trim()
            val pkg = item.getString("packageName").trim()
            val text = item.getString("text").trim()
            require(idPattern.matches(id) && ids.add(id)) { "ID 重复或格式错误" }
            require(name.isNotEmpty() && name.length <= 40) { "规则名称无效" }
            require(pkgPattern.matches(pkg)) { "包名无效：$pkg" }
            require(text.length in 2..18 && !sensitive.containsMatchIn(text)) {
                "跳过文字过长或涉及敏感操作"
            }
            SkipRule(id, name, pkg, text, item.optBoolean("enabled", true))
        }
    }

    fun encode(rules: List<SkipRule>): String {
        val items = JSONArray()
        rules.forEach {
            items.put(JSONObject().put("id", it.id).put("name", it.name)
                .put("packageName", it.packageName).put("text", it.text)
                .put("enabled", it.enabled))
        }
        return JSONObject().put("schema", 1).put("rules", items).toString(2)
    }

    fun matches(rules: List<SkipRule>, packageName: String, shownText: String): Boolean =
        rules.any { it.enabled && it.packageName == packageName &&
            it.text.equals(shownText.trim(), ignoreCase = true) }

    const val EXAMPLE = """{
  "schema": 1,
  "rules": [
    {
      "id": "demo_skip",
      "name": "示例：只对指定应用生效",
      "packageName": "com.example.reader",
      "text": "跳过广告",
      "enabled": true
    }
  ]
}"""
}
