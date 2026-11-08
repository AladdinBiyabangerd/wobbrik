package com.aladdin.wobbrik

import android.content.Context
import com.aladdin.wobbrik.engine.EngineCheckpointCodec
import com.aladdin.wobbrik.engine.FinishDraft
import com.aladdin.wobbrik.engine.FinishDraftStore
import org.json.JSONObject
import java.security.MessageDigest

/** App-private, account/attempt-scoped UI intent. AttemptJournal remains the sole wire outbox. */
internal class AndroidFinishDraftStore(context: Context) : FinishDraftStore {
    private val preferences = context.getSharedPreferences("finish-cosmetics-v1", Context.MODE_PRIVATE)
    private fun key(uid: String, attemptId: String): String = MessageDigest.getInstance("SHA-256")
        .digest("${uid.length}:$uid:$attemptId".toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    override fun read(uid: String, attemptId: String): FinishDraft? {
        val text = preferences.getString(key(uid, attemptId), null) ?: return null
        return runCatching {
            val json = JSONObject(text)
            require(json.getInt("version") == 1)
            val parts = json.getJSONObject("parts")
            FinishDraft(EngineCheckpointCodec.decode(json.getString("terminal")),
                parts.keys().asSequence().associateWith { parts.getString(it) })
        }.getOrNull()
    }

    override fun write(uid: String, attemptId: String, draft: FinishDraft) {
        val json = JSONObject().put("version", 1).put("terminal", EngineCheckpointCodec.encode(draft.terminal))
            .put("parts", JSONObject(draft.parts))
        check(preferences.edit().putString(key(uid, attemptId), json.toString()).commit()) {
            "Finish selection could not be persisted"
        }
    }
}
