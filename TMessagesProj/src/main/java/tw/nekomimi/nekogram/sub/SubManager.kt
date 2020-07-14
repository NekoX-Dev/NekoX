package tw.nekomimi.nekogram.sub

import org.dizitart.no2.objects.filters.ObjectFilters
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import tw.nekomimi.nekogram.database.mkDatabase

object SubManager {

    val database by lazy { mkDatabase("proxy_sub") }

    @JvmStatic val count get() = subList.find().totalCount()

    @JvmStatic
    val subList by lazy {

        database.getRepository("proxy_sub", SubInfo::class.java).apply {

            val public = find(ObjectFilters.eq("id", 1L)).firstOrDefault()

            update(SubInfo().apply {

                name = LocaleController.getString("NekoXProxy", R.string.NekoXProxy)
                enable = public?.enable ?: true

                urls = listOf(
                        "https://gitlab.com/NekohaSekai/nekox-proxy-list/-/raw/master/proxy_list",
                        "https://nekox-dev.github.io/ProxyList/proxy_list",
                        "https://gitee.com/nekoshizuku/AwesomeRepo/raw/master/proxy_list"
                )

                id = 1L
                internal = true

                proxies = public?.proxies ?: listOf()

            }, true)

        }

    }

}