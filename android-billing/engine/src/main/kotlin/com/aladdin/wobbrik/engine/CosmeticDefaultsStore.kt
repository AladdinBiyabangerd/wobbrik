package com.aladdin.wobbrik.engine

/** Account preferences and an immutable per-attempt appearance; neither grants ownership. */
interface CosmeticDefaultsStore {
    fun read(uid: String): Map<String, String>
    fun write(uid: String, parts: Map<String, String>)
    fun attempt(uid: String, attemptId: String): Map<String, String>?
    fun capture(uid: String, attemptId: String, parts: Map<String, String>)
}
