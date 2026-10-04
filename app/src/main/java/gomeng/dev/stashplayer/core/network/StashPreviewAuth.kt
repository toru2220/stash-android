package gomeng.dev.stashplayer.core.network

import okio.ByteString.Companion.encodeUtf8

fun spritePreviewCacheKey(url: String, requestHeaders: Map<String, String>): String =
    "$url|${requestHeaders.toSortedMap()}".encodeUtf8().sha256().hex()

fun StashServerProfile.spritePreviewHeadersFor(frame: StashSpriteFrame): Map<String, String> {
    return authHeadersFor(frame.url)
}
