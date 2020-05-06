package tw.nekomimi.nekogram.parts

import android.app.Activity
import android.content.IntentSender.SendIntentException
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONObject
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.SettingsActivity
import tw.nekomimi.nekogram.BottomBuilder
import tw.nekomimi.nekogram.ExternalGcm
import tw.nekomimi.nekogram.NekoXConfig
import tw.nekomimi.nekogram.utils.*
import java.util.*

@JvmOverloads
fun Activity.checkUpdate(force: Boolean = false) {

    val progress = AlertUtil.showProgress(this)

    progress.show()

    UIUtil.runOnIoDispatcher {

        if (ExternalGcm.checkPlayServices() && !force) {

            progress.uUpdate(LocaleController.getString("Checking", R.string.Checking) + " (Play Store)")

            val manager = AppUpdateManagerFactory.create(this)

            manager.registerListener(InstallStateUpdatedListener {

                if (it.installStatus() == InstallStatus.DOWNLOADED) {

                    val builder = BottomBuilder(this)

                    builder.addTitle(LocaleController.getString("UpdateDownloaded", R.string.UpdateDownloaded), false)

                    builder.addItem(LocaleController.getString("UpdateUpdate", R.string.UpdateUpdate), R.drawable.baseline_system_update_24, false) {

                        manager.completeUpdate()

                    }

                    builder.addItem(LocaleController.getString("UpdateLater", R.string.UpdateLater), R.drawable.baseline_watch_later_24, false, null)

                    builder.show()

                }

            })

            manager.appUpdateInfo.addOnSuccessListener {

                progress.dismiss()

                if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.availableVersionCode() > BuildConfig.VERSION_CODE) {

                    try {

                        manager.startUpdateFlowForResult(it, AppUpdateType.FLEXIBLE, this, 114514)

                    } catch (ignored: SendIntentException) { }

                } else {

                    AlertUtil.showToast(LocaleController.getString("NoUpdate", R.string.NoUpdate))

                }

            }.addOnFailureListener {

                progress.uDismiss()

                AlertUtil.showToast(it.message ?: it.javaClass.simpleName)

            }

            return@runOnIoDispatcher

        }

        progress.uUpdate(LocaleController.getString("Checking", R.string.Checking) + " (Repo)")

        val ex = LinkedList<Throwable>()

        UpdateUtil.updateUrls.forEach { url ->

            runCatching {

                val updateInfo = JSONObject(HttpUtil.get("$url/update.json"))

                val code = updateInfo.getInt("versionCode")

                progress.uDismiss()

                if (code > BuildConfig.VERSION_CODE || force) UIUtil.runOnUIThread {

                    val builder = BottomBuilder(this)

                    builder.addTitle(LocaleController.getString("UpdateAvailable", R.string.UpdateAvailable), updateInfo.getString("version"))

                    builder.addItem(LocaleController.getString("UpdateUpdate", R.string.UpdateUpdate), R.drawable.baseline_system_update_24, false) {

                        UpdateUtil.doUpdate(this, code, updateInfo.getString("defaultApkName"))

                        builder.dismiss()

                        NekoXConfig.preferences.edit().remove("ignored_update_at").remove("ignore_update_at").apply()

                    }

                    builder.addItem(LocaleController.getString("UpdateLater", R.string.UpdateLater), R.drawable.baseline_watch_later_24, false) {

                        builder.dismiss()

                        NekoXConfig.preferences.edit().putLong("ignored_update_at", System.currentTimeMillis()).apply()

                    }

                    builder.addItem(LocaleController.getString("Ignore", R.string.Ignore), R.drawable.baseline_block_24, true) {

                        builder.dismiss()

                        NekoXConfig.preferences.edit().putInt("ignore_update", code).apply()

                    }

                    builder.show()

                } else {

                    AlertUtil.showToast(LocaleController.getString("NoUpdate", R.string.NoUpdate))

                }

                return@runOnIoDispatcher

            }.onFailure {

                ex.add(it)

            }

        }

        progress.uDismiss()

        AlertUtil.showToast(ex.map { it.message ?: it.javaClass.simpleName }.joinToString("\n"))

    }

}