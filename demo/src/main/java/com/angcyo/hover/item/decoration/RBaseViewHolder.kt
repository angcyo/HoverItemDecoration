package com.angcyo.hover.item.decoration

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

class RBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun <T : View> v(id: Int): T? = itemView.findViewById(id)

    fun rv(id: Int): RecyclerView? = itemView.findViewById(id)

    fun tv(id: Int): TextView? = itemView.findViewById(id)

    fun click(id: Int, listener: (View) -> Unit) {
        v<View>(id)?.setOnClickListener {
            listener.invoke(it)
        }
    }

    fun clickItem(listener: (View) -> Unit) {
        itemView.setOnClickListener {
            listener.invoke(it)
        }
    }
}