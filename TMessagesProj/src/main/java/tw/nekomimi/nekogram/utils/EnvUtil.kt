package tw.nekomimi.nekogram.utils

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object EnvUtil {

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    val rootDirectories by lazy {

        val mStorageManager = ApplicationLoader.applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val getPath = StorageManager::class.java.getDeclaredMethod("getPath")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            mStorageManager.storageVolumes.map { File(getPath.invoke(it) as String) }

        } else {

            (mStorageManager.javaClass.getMethod("getVolumeList").invoke(mStorageManager) as Array<StorageVolume>).map {

                File(getPath.invoke(it) as String)

            }

        }

    }

    @JvmStatic
    fun doTest() {

        FileLog.d("rootDirectories: ${rootDirectories.size}")

        rootDirectories.forEach { FileLog.d(it.path) }

    }

}