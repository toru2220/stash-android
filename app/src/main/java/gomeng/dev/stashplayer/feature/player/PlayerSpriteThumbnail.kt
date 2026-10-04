package gomeng.dev.stashplayer.feature.player

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSpriteFrame
import gomeng.dev.stashplayer.core.network.fitsSpriteSheet
import gomeng.dev.stashplayer.core.network.spritePreviewCacheKey
import gomeng.dev.stashplayer.core.network.spritePreviewHeadersFor
import kotlin.math.roundToInt

@Composable
internal fun PlayerSpriteThumbnail(
    frame: StashSpriteFrame,
    serverProfile: StashServerProfile?,
    imageLoader: ImageLoader,
) {
    val context = LocalContext.current
    // Key the request to the sheet, not the tile: scrubbing reuses Coil's decoded bitmap.
    val request = remember(context, frame.url, serverProfile) {
        val headers = serverProfile?.spritePreviewHeadersFor(frame).orEmpty()
        val cacheKey = spritePreviewCacheKey(frame.url, headers)
        ImageRequest.Builder(context)
            .data(frame.url)
            .size(Size.ORIGINAL)
            .allowHardware(false)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .apply { headers.forEach { (name, value) -> setHeader(name, value) } }
            .build()
    }
    val painter = rememberAsyncImagePainter(request, imageLoader)
    val bitmap = ((painter.state as? AsyncImagePainter.State.Success)?.result?.drawable as? BitmapDrawable)?.bitmap
    if (bitmap != null && frame.fitsSpriteSheet(bitmap.width, bitmap.height)) {
        val image = remember(bitmap) { bitmap.asImageBitmap() }
        Canvas(Modifier.size(width = 112.dp, height = 63.dp)) {
            val scale = minOf(size.width / frame.width, size.height / frame.height)
            val width = (frame.width * scale).roundToInt().coerceAtLeast(1)
            val height = (frame.height * scale).roundToInt().coerceAtLeast(1)
            drawImage(
                image = image,
                srcOffset = IntOffset(frame.x, frame.y),
                srcSize = IntSize(frame.width, frame.height),
                dstOffset = IntOffset(((size.width - width) / 2).roundToInt(), ((size.height - height) / 2).roundToInt()),
                dstSize = IntSize(width, height),
            )
        }
    }
    // Loading, invalid crops and failures leave the existing time/delta card intact.
}
