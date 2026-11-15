package appbackend.wobbrik.game

import appbackend.wobbrik.billing.BillingSession
import kotlinx.serialization.json.*

/** Public game identity only; never derived from Google account information. */
data class PublicProfile(val revision: Long, val displayName: String, val avatarId: String)
data class ProfileReplacement(val expectedRevision: Long, val displayName: String, val avatarId: String) {
    init { require(expectedRevision >= 0); PublicProfiles.identity(displayName, avatarId); require(displayName.codePointCount(0, displayName.length) <= PublicProfiles.MaxNameLength) }
    fun json(): String = buildJsonObject {
        put("expected_profile_revision", expectedRevision)
        put("display_name", displayName); put("avatar_id", avatarId)
    }.toString()
}

object PublicProfiles {
    const val MaxNameLength = 15
    val avatars = setOf("builder", "pilot", "architect")
    fun identity(name: String, avatar: String) {
        require(avatar in avatars)
        require(name.codePointCount(0, name.length) in 1..24 && name.isNotEmpty() && name.first() != ' ' && name.last() != ' ')
        name.codePoints().forEach { c ->
            require(c !in 0xD800..0xDFFF && !Character.isISOControl(c) &&
                Character.getType(c) != Character.FORMAT.toInt() &&
                (c == 32 || (!Character.isWhitespace(c) && !Character.isSpaceChar(c) && c != 0x85)))
        }
    }
    fun parse(body: String): PublicProfile {
        val r = Json.parseToJsonElement(body).jsonObject
        val revision = r.getValue("revision").jsonPrimitive.also { require(!it.isString) }.long
        val name = r.getValue("display_name").jsonPrimitive.also { require(it.isString) }.content
        val avatar = r.getValue("avatar_id").jsonPrimitive.also { require(it.isString) }.content
        require(revision >= 0); identity(name, avatar)
        return PublicProfile(revision, name, avatar)
    }
}

/** Separate from economic commands, receipts and the durable attempt journal. */
interface ProfileAccess {
    suspend fun profile(session: BillingSession): PublicProfile
    suspend fun replaceProfile(session: BillingSession, replacement: ProfileReplacement): PublicProfile
}
