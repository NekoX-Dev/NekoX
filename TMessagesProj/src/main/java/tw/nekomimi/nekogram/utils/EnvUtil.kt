package tw.nekomimi.nekogram.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import org.telegram.messenger.*
import org.telegram.messenger.browser.Browser
import org.telegram.ui.ActionBar.AlertDialog
import tw.nekomimi.nekogram.NekoConfig
import java.io.File
import java.io.FileInputStream
import java.util.*

object EnvUtil {

    @JvmField
    val isCompatibilityMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()

    @JvmStatic
    fun mkUri(mediaId: Number, isVideo: Boolean): Uri {
        return if (isVideo) {
            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId.toLong())
        } else {
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId.toLong())
        }
    }

    @JvmStatic
    fun mkCopy(mediaId: Number, isVideo: Boolean): String {
        return MediaController.copyFileToCache(mkUri(mediaId, isVideo), "file")
    }

    @JvmStatic
    fun openInput(mediaId: Number, isVideo: Boolean): FileInputStream {
        return ApplicationLoader.applicationContext.contentResolver.openInputStream(mkUri(mediaId, isVideo)) as FileInputStream
    }

    @Suppress("DEPRECATION")
    @JvmStatic
    fun getThumbnail(mediaId: Number, isVideo: Boolean, opts: BitmapFactory.Options): Bitmap {
        return if (isVideo) {
            MediaStore.Video.Thumbnails.getThumbnail(ApplicationLoader.applicationContext.contentResolver, mediaId.toLong(), MediaStore.Video.Thumbnails.MINI_KIND, opts)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(ApplicationLoader.applicationContext.contentResolver, mediaId.toLong(), MediaStore.Images.Thumbnails.MINI_KIND, opts)
        }
    }

    @JvmStatic
    fun postCheckComMode(ctx: Context) {

        if (isCompatibilityMode) {
            val builder = AlertDialog.Builder(ctx)

            builder.setTitle(LocaleController.getString("COMWarn", R.string.COMWarn))
            builder.setMessage(LocaleController.getString("COMInfo", R.string.COMInfo))

            builder.setNeutralButton(LocaleController.getString("COMOpen", R.string.COMOpen)) { _, _ ->
                Browser.openUrl(ctx, "https://github.com/NekoX-Dev/NekoX/wiki/About-Compatibility-Mode")
            }
            builder.setPositiveButton(LocaleController.getString("Ok", R.string.OK), null)

            builder.show()

        }
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    val rootDirectories by lazy {

        val mStorageManager = ApplicationLoader.applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        (mStorageManager.javaClass.getMethod("getVolumePaths").invoke(mStorageManager) as Array<String>).map { File(it) }

    }

    @JvmStatic
    val availableDirectories
        get() = LinkedList<File>().apply {

            add(File(ApplicationLoader.getDataDirFixed(), "files/media"))
            add(File(ApplicationLoader.getDataDirFixed(), "cache/media"))

            rootDirectories.forEach {

                add(File(it, "Android/data/" + ApplicationLoader.applicationContext.packageName + "/files"))
                add(File(it, "Android/data/" + ApplicationLoader.applicationContext.packageName + "/cache"))

            }

            add(Environment.getExternalStoragePublicDirectory("NekoX"))

        }.map { it.path }.toTypedArray()

    @JvmStatic
    fun getTelegramPath(): File {

        if (NekoConfig.cachePath == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // https://github.com/NekoX-Dev/NekoX/issues/284
                NekoConfig.setCachePath(File(ApplicationLoader.getDataDirFixed(), "cache/media").path)
            } else {
                NekoConfig.setCachePath(ApplicationLoader.applicationContext.getExternalFilesDir(null)!!.path)
            }

        }

        var telegramPath = File(NekoConfig.cachePath)

        if (telegramPath.isDirectory || telegramPath.mkdirs()) {

            return telegramPath

        }

        telegramPath = ApplicationLoader.applicationContext.getExternalFilesDir(null)
                ?: File(ApplicationLoader.getDataDirFixed(), "cache/files")

        if (telegramPath.isDirectory || telegramPath.mkdirs()) {

            return telegramPath

        }

        telegramPath = File(ApplicationLoader.getDataDirFixed(), "cache/files")

        if (!telegramPath.isDirectory) telegramPath.mkdirs();

        return telegramPath;

    }

    @JvmStatic
    fun doTest() {

        FileLog.d("rootDirectories: ${rootDirectories.size}")

        rootDirectories.forEach { FileLog.d(it.path) }

    }

}