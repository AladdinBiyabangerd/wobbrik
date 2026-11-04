package appbackend.wobbrik.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class StoreOffer(val token: String, val formattedPrice: String, val currency: String)
sealed interface BillingNotice {
    data object Updated : BillingNotice
    data object Pending : BillingNotice
    data object Canceled : BillingNotice
    data object WrongAccount : BillingNotice
    data object RetryLater : BillingNotice
}
/** One application-scoped instance. Call onForeground after login and on returning from Play. */
class PlayBillingBridge(context: Context, private val coordinator: BillingCoordinator) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connection = Mutex()
    private val events = MutableSharedFlow<BillingNotice>(extraBufferCapacity = 16)
    val notices = events.asSharedFlow()
    private val client = BillingClient.newBuilder(context.applicationContext)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .setListener { result, purchases -> scope.launch {
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { process(it) }
                BillingClient.BillingResponseCode.USER_CANCELED -> events.emit(BillingNotice.Canceled)
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> safely { restore() }
                else -> events.emit(BillingNotice.RetryLater)
            }
        }}.build()
    private suspend fun connected() = withContext(Dispatchers.Main.immediate) { connection.withLock {
        if (client.isReady) return@withLock
        withTimeout(10000) { suspendCancellableCoroutine<Unit> { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (!continuation.isActive) return
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) continuation.resume(Unit)
                    else continuation.resumeWithException(BillingRetryLater())
                }
                override fun onBillingServiceDisconnected() { }
            })
        }}
    }}
    private suspend fun product(id: String): ProductDetails {
        connected()
        return withContext(Dispatchers.Main.immediate) { withTimeout(10000) {
            suspendCancellableCoroutine { continuation ->
                val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(
                    QueryProductDetailsParams.Product.newBuilder().setProductId(id).setProductType(BillingClient.ProductType.INAPP).build()
                )).build()
                client.queryProductDetailsAsync(params) { result, products ->
                    if (!continuation.isActive) return@queryProductDetailsAsync
                    val detail = products.productDetailsList.singleOrNull { it.productId == id }
                    if (result.responseCode == BillingClient.BillingResponseCode.OK && detail != null) continuation.resume(detail)
                    else continuation.resumeWithException(BillingRetryLater())
                }
            }
        }}
    }
    private fun durableOffers(product: ProductDetails) = product.oneTimePurchaseOfferDetailsList.orEmpty()
        .filter { it.rentalDetails == null && it.preorderDetails == null }
    suspend fun offers(productId: String): List<StoreOffer> {
        val checkout = coordinator.prepare(productId)
        val details = product(productId)
        coordinator.checkLaunch(checkout)
        return durableOffers(details).mapNotNull { offer -> offer.offerToken?.let { StoreOffer(it, offer.formattedPrice, offer.priceCurrencyCode) } }
    }
    suspend fun buy(activity: Activity, productId: String, selectedOfferToken: String) {
        val checkout = coordinator.prepare(productId)
        val details = product(productId) // Never reuse ProductDetails from a previous checkout.
        if (durableOffers(details).none { it.offerToken == selectedOfferToken }) throw PurchaseDenied()
        withContext(Dispatchers.Main.immediate) {
            coordinator.checkLaunch(checkout)
            val product = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(selectedOfferToken).build()
            val result = client.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                .setObfuscatedAccountId(checkout.accountId).setProductDetailsParamsList(listOf(product)).build())
            if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) scope.launch { safely { restore() } }
            else if (result.responseCode != BillingClient.BillingResponseCode.OK) throw BillingRetryLater()
        }
    }
    fun onForeground() { scope.launch { safely { restore() } } }
    suspend fun restore() {
        coordinator.refresh()
        connected()
        val owned = withContext(Dispatchers.Main.immediate) { withTimeout(10000) {
            suspendCancellableCoroutine<List<Purchase>> { continuation ->
                val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
                client.queryPurchasesAsync(params) { result, purchases ->
                    if (!continuation.isActive) return@queryPurchasesAsync
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) continuation.resume(purchases)
                    else continuation.resumeWithException(BillingRetryLater())
                }
            }
        }}
        owned.forEach { process(it) }
        events.emit(BillingNotice.Updated)
    }
    private suspend fun process(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) { events.emit(BillingNotice.Pending); return }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.products.size != 1 || purchase.quantity != 1) { events.emit(BillingNotice.WrongAccount); return }
        safely {
            coordinator.accept(PurchaseEvent(purchase.products.single(), purchase.purchaseToken,
                purchase.accountIdentifiers?.obfuscatedAccountId, purchased = true))
            events.emit(BillingNotice.Updated)
        }
    }
    private suspend fun safely(block: suspend () -> Unit) {
        try { block() }
        catch (_: TimeoutCancellationException) { events.emit(BillingNotice.RetryLater) }
        catch (e: CancellationException) { throw e }
        catch (_: AccountChanged) { events.emit(BillingNotice.WrongAccount) }
        catch (_: PurchaseDenied) { events.emit(BillingNotice.WrongAccount) }
        catch (_: Exception) { events.emit(BillingNotice.RetryLater) }
    }
    override fun close() { scope.cancel(); client.endConnection() }
}
