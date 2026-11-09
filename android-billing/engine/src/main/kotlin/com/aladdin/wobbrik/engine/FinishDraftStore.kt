package com.aladdin.wobbrik.engine

/** Local UI/terminal intent only. Never a wallet, grant, accepted snapshot or command journal. */
data class FinishDraft(val terminal: EngineCheckpoint, val parts: Map<String, String> = emptyMap())
interface FinishDraftStore {
    fun read(uid: String, attemptId: String): FinishDraft?
    /** Synchronous durable write before checkpoint dispatch or selection acknowledgement. */
    fun write(uid: String, attemptId: String, draft: FinishDraft)
}
