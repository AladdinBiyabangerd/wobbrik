package appbackend.wobbrik.billing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI

class BillingRetryLater : IOException("Billing service is temporarily unavailable")
class AppBackendClient(baseUrl: String, private val sessions: Sessions) : BillingBackend {
    private val base = URI(baseUrl).also {
        require(it.scheme == "https" && !it.host.isNullOrBlank() && it.userInfo == null && it.query == null && it.fragment == null)
        require(it.path.isNullOrEmpty() || it.path == "/") { "Use the backend origin URL" }
    }.toString().trimEnd('/')
    private suspend fun request(session: BillingSession, path: String, body: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        if (sessions.current() != session) throw AccountChanged()
        val bearer = sessions.idToken(session)
        if (sessions.current() != session) throw AccountChanged()
        val connection = URI("$base/v1/apps/wobbrik/$path").toURL().openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("Authorization", "Bearer $bearer")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val bytes = stream?.use { input ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (out.size() <= 65536) {
                    val n = input.read(buffer, 0, minOf(buffer.size, 65537 - out.size()))
                    if (n < 0) break
                    out.write(buffer, 0, n)
                }
                out.toByteArray()
             } ?: byteArrayOf()
            if (bytes.size > 65536) throw BillingRetryLater()
            val payload = if (bytes.isEmpty()) JSONObject() else JSONObject(String(bytes, Charsets.UTF_8))
            if (status !in 200..299) {
                if (status == 403 || status == 404 || (status == 409 && payload.optString("error") == "account_or_purchase_conflict")) throw PurchaseDenied()
                throw BillingRetryLater()
            }
            if (sessions.current() != session) throw AccountChanged()
            payload
        } finally { connection.disconnect() }
    }
    override suspend fun catalog(session: BillingSession): BillingCatalog {
        val response = request(session, "billing/catalog")
        val products = response.getJSONArray("products")
        val items = (0 until products.length()).map { index ->
            val p = products.getJSONObject(index)
            val kind = when (p.getString("kind")) {
                "durable" -> ProductKind.DURABLE
                "consumable" -> ProductKind.CONSUMABLE
                else -> throw PurchaseDenied()
            }
            CatalogProduct(p.getString("id"), kind)
        }
        return BillingCatalog(response.getString("app_id"), response.getString("android_package"), items)
    }

    override suspend fun identity(session: BillingSession): String = request(session, "billing/identity").getString("google_obfuscated_account_id")
    override suspend fun verify(session: BillingSession, purchase: QueuedPurchase) {
        if (purchase.uid != session.uid) throw PurchaseDenied()
        val response = request(session, "billing/google/verify", JSONObject().put("product", purchase.product).put("purchase_token", purchase.token))
        if (!response.optBoolean("delivery_confirmed", response.optBoolean("active"))) throw PurchaseDenied()
    }
    override suspend fun snapshot(session: BillingSession): BillingSnapshot {
        val response = request(session, "billing/state")
        val values = response.getJSONArray("entitlements")
        val coins = response.getLong("purchased_coins")
        require(coins >= 0) { "Invalid wallet snapshot" }
        return BillingSnapshot((0 until values.length()).map { values.getJSONObject(it).getString("id") }.toSet(), coins)
    }
}
