package tw.nekomimi.nekogram.sub

import org.json.JSONObject
import org.telegram.messenger.ApplicationLoader
import tw.nekomimi.nekogram.utils.getValue
import tw.nekomimi.nekogram.utils.setValue
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object SubManager {

    @JvmField
    val subs = LinkedList<SubInfo>()
    init {


        subs.add(SubInfo().apply {

            name = "test"

            mirrors = LinkedList<SubInfo.Mirror>()

            mirrors.add(SubInfo.Mirror().apply {

                url = "https://test.address"

            })

        })
    }

    var loaded by AtomicBoolean()

    fun loadSubList() {

        if (loaded) return

        subs.clear()

        File(ApplicationLoader.getFilesDirFixed(), "nekox").listFiles()?.forEach {

            subs.add(readInfo(JSONObject(it.readText())))

        }

    }


    fun saveSubList() {

        subs.forEach {


        }

    }

    private fun writeInfo(document: JSONObject, info: SubInfo) {

        document.put("name", info.name)

    }

    private fun readInfo(document: JSONObject): SubInfo {

        val info = SubInfo()

        info.name = document.getString("name")
        info.mirrors = LinkedList()

        val mirrorsArray = document.getJSONArray("mirrors")

        for (index in 0 until mirrorsArray.length()) {

            val mirror = SubInfo.Mirror()

            mirrorsArray.get(index).also {

                if (it is String) {

                    mirror.url = it

                } else if (it is JSONObject) {

                    mirror.url = it.getString("url")
                    mirror.method = it.optString("method")
                    mirror.headers = hashMapOf()

                    val headersObj = it.optJSONObject("headers")

                    headersObj?.keys()?.forEach { key ->

                        mirror.headers[key] = headersObj.getString(key)

                    }

                }

            }

            info.mirrors.add(mirror)

        }

        info.proxies = LinkedList()

        document.getJSONArray("proxies").also {

            for (index in 0 until it.length()) info.proxies.add(it.getString(index))

        }

        return info

    }

}