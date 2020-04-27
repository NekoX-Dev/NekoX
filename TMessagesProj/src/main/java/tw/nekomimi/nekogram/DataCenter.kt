package tw.nekomimi.nekogram

import com.v2ray.ang.util.Utils
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import org.telegram.tgnet.AbstractSerializedData
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.SerializedData
import tw.nekomimi.nekogram.utils.AlertUtil
import tw.nekomimi.nekogram.utils.receive
import java.io.File

object DataCenter {

    val ConnectionsManager.tgnetFile by receive<ConnectionsManager, File> {

        var config = ApplicationLoader.getFilesDirFixed()
        if (currentAccount != 0) {
            config = File(config, "account$currentAccount")
            config.mkdirs()
        }

        return@receive File(config,"tgnet.dat")

    }

    val ConnectionsManager.tgnetFileNew by receive<ConnectionsManager, File> { File(tgnetFile.parentFile!!, "${tgnetFile.name}.new") }

    @JvmStatic
    fun applyOfficalDataCanter(account: Int) {

        MessagesController.getMainSettings(account).edit().remove("layer").remove("custom_dc").apply()

        ConnectionsManager.getInstance(account).apply {

            tgnetFile.delete()
            tgnetFileNew.delete()

        }

        if (ConnectionsManager.native_isTestBackend(account) != 0) {

            ConnectionsManager.getInstance(account).switchBackend()

        }

        ConnectionsManager.native_cleanUp(account, true)

        AlertUtil.showToast("Restart required.")

    }

    @JvmStatic
    fun applyTestDataCenter(account: Int) {

        MessagesController.getMainSettings(account).edit().remove("layer").remove("custom_dc").apply()

        ConnectionsManager.getInstance(account).apply {

            tgnetFile.delete()
            tgnetFileNew.delete()

        }

        if (ConnectionsManager.native_isTestBackend(account) == 0) {

            ConnectionsManager.getInstance(account).switchBackend()

        }

        AlertUtil.showToast("Restart required.")

    }

    @JvmStatic
    fun applyCustomDataCenter(account: Int, ipv4Address: String = "", ipv6Address: String = "", port: Int, layer: Int) {

        MessagesController.getMainSettings(account).edit().putInt("layer", layer).putBoolean("custom_dc", true).apply()

        if (ConnectionsManager.native_isTestBackend(account) != 0) {

            ConnectionsManager.getInstance(account).switchBackend()

        }

        ConnectionsManager.getInstance(account).apply {

            tgnetFile.delete()
            tgnetFileNew.delete()

        }

        val buffer = SerializedData()

        val time = (System.currentTimeMillis() / 1000).toInt()

        buffer.writeInt32(5) // configVersion
        buffer.writeBool(false) // testBackend
        buffer.writeBool(true) // clientBlocked
        buffer.writeString("en") // lastInitSystemLangcode
        buffer.writeBool(true) // currentDatacenter != nullptr

        buffer.writeInt32(2) // currentDatacenterId
        buffer.writeInt32(0) // timeDifference
        buffer.writeInt32(time) // lastDcUpdateTime
        buffer.writeInt64(0L) // pushSessionId
        buffer.writeBool(false) // registeredForInternalPush
        buffer.writeInt32(time) // getCurrentTime()
        buffer.writeInt32(0) // sessions.size

        buffer.writeInt32(5) // datacenters.size

        repeat(5) {

            buffer.writeDataCenter(it + 1, ipv4Address, ipv4Address, port)

        }

        ConnectionsManager.getInstance(account).tgnetFileNew.apply {

            createNewFile()
            writeBytes(buffer.toByteArray())

        }

        AlertUtil.showToast("Restart required.")

    }

    fun AbstractSerializedData.writeDataCenter(id: Int, ipv4Address: String, ipv6Address: String, port: Int) {

        writeInt32(13) // configVersion
        writeInt32(id) // datacenterId
        writeInt32(13) // lastInitVersion
        writeInt32(13) //lastInitMediaVersion

        writeDataCenterAddress(ipv4Address, port)
        writeDataCenterAddress(ipv6Address, port)
        writeDataCenterAddress("", port)
        writeDataCenterAddress("", port)


    }

    fun AbstractSerializedData.writeDataCenterAddress(address: String, port: Int) {

        writeInt32(if (address.isBlank()) 0 else 1) // array->size()

        if (address.isNotBlank()) {

            writeString(address) // address
            writeInt32(port) // port
            writeInt32(if (Utils.isIpv6Address(address)) 1 else 0) // flags
            writeString("") // writeString

        }


    }

}