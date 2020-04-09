package tw.nekomimi.nekogram.utils

import android.annotation.SuppressLint
import android.view.View
import org.telegram.ui.ActionBar.ActionBarMenuItem
import org.telegram.ui.ActionBar.Theme

@SuppressLint("ViewConstructor")
class PopupBuilder @JvmOverloads constructor(anchor: View, dialog: Boolean = false) : ActionBarMenuItem(anchor.context, null, Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, -0x4c4c4d) {

    init {

        setAnchor(anchor)

        isShowOnTop = dialog

        isVerticalScrollBarEnabled = true

    }

    fun setItems(items: Array<CharSequence>, listener: (Int,CharSequence) -> Unit) {

        removeAllSubItems()

        items.forEachIndexed { i, v ->

            addSubItem(i, v)

        }

        setDelegate {

            listener(it,items[it])

        }

    }

    fun show() {

        toggleSubMenu()

    }

}