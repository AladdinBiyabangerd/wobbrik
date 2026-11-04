package appbackend.wobbrik.billing

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Host must exclude wobbrik_billing.xml from both backup and device transfer. */
class SecurePurchaseJournal(context: Context) : PurchaseJournal {
    private val prefs = context.getSharedPreferences("wobbrik_billing", Context.MODE_PRIVATE)
    private val alias = "wobbrik.billing.v1"
    private val key: SecretKey by lazy {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }
    private fun name(uid: String) = MessageDigest.getInstance("SHA-256").digest(uid.toByteArray()).joinToString("") { "%02x".format(it) }
    private fun read(uid: String): JSONObject {
        val raw = prefs.getString(name(uid), null) ?: return JSONObject()
        val bytes = Base64.decode(raw, Base64.NO_WRAP)
        require(bytes.size > 12) { "Billing journal is unreadable" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        cipher.updateAAD(uid.toByteArray())
        return JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
    }
    private fun write(uid: String, value: JSONObject) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        cipher.updateAAD(uid.toByteArray())
        val encrypted = cipher.iv + cipher.doFinal(value.toString().toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(name(uid), Base64.encodeToString(encrypted, Base64.NO_WRAP)).commit()) { "Billing journal write failed" }
    }
    @Synchronized override fun identity(uid: String): String? = read(uid).optString("identity").takeIf { it.isNotEmpty() }
    @Synchronized override fun bind(uid: String, accountId: String) {
        val state = read(uid)
        val old = state.optString("identity")
        check(old.isEmpty() || old == accountId) { "Billing identity changed" }
        write(uid, state.put("identity", accountId))
    }
    @Synchronized override fun queue(purchase: QueuedPurchase) {
        val state = read(purchase.uid)
        val pending = state.optJSONArray("pending") ?: JSONArray()
        for (i in 0 until pending.length()) if (pending.getJSONObject(i).getString("token") == purchase.token) return
        check(pending.length() < 64) { "Too many unprocessed purchases" }
        pending.put(JSONObject().put("product", purchase.product).put("token", purchase.token))
        write(purchase.uid, state.put("pending", pending))
    }
    @Synchronized override fun pending(uid: String): List<QueuedPurchase> {
        val values = read(uid).optJSONArray("pending") ?: return emptyList()
        return (0 until values.length()).map { i -> values.getJSONObject(i).let { QueuedPurchase(uid, it.getString("product"), it.getString("token")) } }
    }
    @Synchronized override fun remove(purchase: QueuedPurchase) {
        val state = read(purchase.uid)
        val kept = pending(purchase.uid).filterNot { it.token == purchase.token }
        write(purchase.uid, state.put("pending", JSONArray(kept.map { JSONObject().put("product", it.product).put("token", it.token) })))
    }
    @Synchronized override fun snapshot(uid: String): BillingSnapshot {
        val state = read(uid)
        val values = state.optJSONArray("entitlements") ?: JSONArray()
        val coins = state.optLong("purchased_coins", 0)
        check(coins >= 0) { "Invalid cached wallet" }
        return BillingSnapshot((0 until values.length()).map { values.getString(it) }.toSet(), coins)
    }
    @Synchronized override fun replaceSnapshot(uid: String, value: BillingSnapshot) {
        check(value.purchasedCoins >= 0)
        val state = read(uid).put("entitlements", JSONArray(value.entitlements.sorted())).put("purchased_coins", value.purchasedCoins)
        write(uid, state)
    }
    @Synchronized override fun clear(uid: String) {
        check(prefs.edit().remove(name(uid)).commit()) { "Billing journal deletion failed" }
    }
}
