package appbackend.wobbrik.billing

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class BillingSession(val uid: String, val epoch: String)
interface Sessions {
    fun current(): BillingSession?
    suspend fun idToken(session: BillingSession): String
}
enum class ProductKind { DURABLE, CONSUMABLE }
data class CatalogProduct(val id: String, val kind: ProductKind)
data class BillingCatalog(val appId: String, val packageName: String, val products: List<CatalogProduct>)
data class BillingSnapshot(val entitlements: Set<String>, val purchasedCoins: Long)
data class PurchaseEvent(val product: String, val token: String, val accountId: String?, val purchased: Boolean)
data class QueuedPurchase(val uid: String, val product: String, val token: String)
interface BillingBackend {
    suspend fun catalog(session: BillingSession): BillingCatalog
    suspend fun identity(session: BillingSession): String
    suspend fun verify(session: BillingSession, purchase: QueuedPurchase)
    suspend fun snapshot(session: BillingSession): BillingSnapshot
}
interface PurchaseJournal {
    fun identity(uid: String): String?
    fun bind(uid: String, accountId: String)
    fun queue(purchase: QueuedPurchase)
    fun pending(uid: String): List<QueuedPurchase>
    fun remove(purchase: QueuedPurchase)
    fun snapshot(uid: String): BillingSnapshot
    fun replaceSnapshot(uid: String, value: BillingSnapshot)
    fun clear(uid: String)
}
data class CheckoutIdentity(val session: BillingSession, val product: String, val accountId: String)
class AccountChanged : IllegalStateException("Billing account changed")
class PurchaseDenied : IllegalStateException("Purchase is not eligible for this account")

/** No client acknowledgement, local grant, coin credit or cross-UID transfer. */
class BillingCoordinator(
    private val sessions: Sessions,
    private val backend: BillingBackend,
    private val journal: PurchaseJournal,
    private val packageName: String,
) {
    private val mutex = Mutex()
    private fun snapshot(): BillingSession = sessions.current() ?: throw PurchaseDenied()
    private fun stillCurrent(session: BillingSession) {
        if (sessions.current() != session) throw AccountChanged()
    }
    fun activeSnapshot(): BillingSnapshot {
        val session = sessions.current() ?: return BillingSnapshot(emptySet(), 0)
        val state = journal.snapshot(session.uid)
        return if (sessions.current() == session) state else BillingSnapshot(emptySet(), 0)
    }
    fun activeEntitlements(): Set<String> = activeSnapshot().entitlements
    suspend fun prepare(product: String): CheckoutIdentity = mutex.withLock {
        val session = snapshot()
        val catalog = backend.catalog(session)
        stillCurrent(session)
        if (catalog.appId != "wobbrik" || catalog.packageName != packageName || catalog.products.none { it.id == product }) throw PurchaseDenied()
        val identity = backend.identity(session)
        stillCurrent(session)
        if (!identity.matches(Regex("[a-f0-9]{64}"))) throw PurchaseDenied()
        journal.bind(session.uid, identity)
        CheckoutIdentity(session, product, identity)
    }
    /** Call immediately before Play's launchBillingFlow on the main thread. */
    fun checkLaunch(checkout: CheckoutIdentity) = stillCurrent(checkout.session)
    /** Persist first; a process death after this point can retry the same token. */
    suspend fun accept(event: PurchaseEvent) = mutex.withLock {
        if (!event.purchased) return@withLock
        val session = snapshot()
        val identity = journal.identity(session.uid) ?: throw PurchaseDenied()
        if (event.accountId != identity || event.token.isBlank() || event.product.isBlank()) throw PurchaseDenied()
        journal.queue(QueuedPurchase(session.uid, event.product, event.token))
        drain(session)
    }
    /** Restore/retry requires an online identity; this never reassigns an old purchase. */
    suspend fun refresh() = mutex.withLock {
        val session = snapshot()
        val identity = backend.identity(session)
        stillCurrent(session)
        if (!identity.matches(Regex("[a-f0-9]{64}"))) throw PurchaseDenied()
        journal.bind(session.uid, identity)
        drain(session)
    }
    private suspend fun drain(session: BillingSession) {
        var deniedPurchase = false
        for (purchase in journal.pending(session.uid)) {
            stillCurrent(session)
            try {
                backend.verify(session, purchase)
            } catch (denied: PurchaseDenied) {
                stillCurrent(session)
                journal.remove(purchase)
                deniedPurchase = true
                continue
            }
            stillCurrent(session)
            journal.remove(purchase)
        }
        val state = backend.snapshot(session)
        require(state.purchasedCoins >= 0) { "Invalid wallet snapshot" }
        stillCurrent(session)
        journal.replaceSnapshot(session.uid, state)
        if (deniedPurchase) throw PurchaseDenied()
    }
    /** Must be called only after the account's server deletion barrier is confirmed. */
    fun clearDeletedAccount(uid: String) = journal.clear(uid)
}
