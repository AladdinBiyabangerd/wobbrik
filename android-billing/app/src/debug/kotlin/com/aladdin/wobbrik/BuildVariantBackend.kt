package com.aladdin.wobbrik

/** Real builds never provide a local identity or backend. */
internal object BuildVariantBackend {
    val localUid: String? = null
    fun transport(context: android.content.Context): appbackend.wobbrik.game.GameplayTransport? = null
}
