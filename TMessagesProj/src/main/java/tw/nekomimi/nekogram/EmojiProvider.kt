package tw.nekomimi.nekogram

import android.graphics.Typeface
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildConfig

object EmojiProvider {

    val type = BuildConfig.FLAVOR

    @JvmField
    val containsEmoji = !type.contains("NoEmoji")

    // default use blob
    @JvmField
    val isFont = !type.contains("Emoji")

    @JvmStatic
    val font by lazy {
        if (!isFont) throw IllegalStateException()
        val resName = when {
            !type.contains("Emoji") -> "blob_compat.ttf"
            else -> throw IllegalStateException()
        }
        Typeface.createFromAsset(ApplicationLoader.applicationContext.assets, "fonts/$resName");
    }


}