package com.aladdin.wobbrik

import android.content.Context
import com.aladdin.wobbrik.engine.CosmeticDefaultsStore
import org.json.JSONObject
import java.security.MessageDigest

internal class AndroidCosmeticDefaultsStore(context: Context) : CosmeticDefaultsStore {
    private val preferences = context.getSharedPreferences("cosmetic-defaults-v1", Context.MODE_PRIVATE)
    private fun key(uid: String, attempt: String? = null): String = MessageDigest.getInstance("SHA-256")
        .digest("${uid.length}:$uid:${attempt?.let { "attempt:$it" } ?: "default"}".toByteArray()).joinToString("") { "%02x".format(it) }
    private fun load(key: String): Map<String, String>? = preferences.getString(key, null)?.let { text ->
        JSONObject(text).let { json -> json.keys().asSequence().associateWith { json.getString(it) } }
    }
    private fun save(key: String, parts: Map<String, String>) {
        check(preferences.edit().putString(key, JSONObject(parts).toString()).commit()) { "Appearance could not be saved" }
    }
    override fun read(uid: String) = runCatching { load(key(uid)) }.getOrNull().orEmpty()
    override fun write(uid: String, parts: Map<String, String>) = save(key(uid), parts)
    override fun attempt(uid: String, attemptId: String) = load(key(uid, attemptId))
    override fun capture(uid: String, attemptId: String, parts: Map<String, String>) = save(key(uid, attemptId), parts)
}
