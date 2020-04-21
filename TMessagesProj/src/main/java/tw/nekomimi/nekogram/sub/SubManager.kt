package tw.nekomimi.nekogram.sub

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import tw.nekomimi.nekogram.database.mkDatabase
import tw.nekomimi.nekogram.utils.ProxyUtil
import java.util.*

object SubManager {

    val database by lazy { mkDatabase("proxy_sub") }

    @JvmStatic
    val subList by lazy { database.getRepository("proxy_sub", SubInfo::class.java).apply {

        find().toList().forEach {

            if (it.internal) remove(it)

        }

        update(object : SubInfo() {

            init {

                name = LocaleController.getString("NekoXProxy", R.string.NekoXProxy)

                _id = 1L
                internal = true

            }

            override fun displayName() = LocaleController.getString("PublicPrefix",R.string.PublicPrefix)

            override fun reloadProxies(): List<SharedConfig.ProxyInfo>? {

                return ProxyUtil.reloadProxyList()

            }

        },true)

    } }

}