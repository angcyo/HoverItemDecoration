package com.angcyo.hover.item.decoration

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.angcyo.hover.item.decoration.dsl.DslAdapter
import com.angcyo.hover.item.decoration.dsl.DslAdapterItem
import com.angcyo.hover.item.decoration.dsl.DslRecyclerScroll

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/07
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

public fun RecyclerView.dslAdapter(init: DslAdapter.() -> Unit) {
    val dslAdapter = DslAdapter(context)
    dslAdapter.init()
    adapter = dslAdapter
}

public fun RecyclerView.dslAdapter(spanCount: Int = 1, init: DslAdapter.() -> Unit) {
    val dslAdapter = DslAdapter(context)
    dslAdapter.init()

    layoutManager = GridLayoutManager(context, spanCount).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return dslAdapter.getItemData(position).itemSpanCount
            }
        }
    }
    adapter = dslAdapter
}

public fun DslAdapter.renderItem(init: DslAdapterItem.() -> Unit) {
    val adapterItem = DslAdapterItem()
    adapterItem.init()
    addLastItem(adapterItem)
}

public fun RecyclerView.onScroll(init: DslRecyclerScroll.() -> Unit) {
    val dslRecyclerView = DslRecyclerScroll()
    dslRecyclerView.init()
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager
            if (layoutManager is LinearLayoutManager) {
                dslRecyclerView.firstItemAdapterPosition = layoutManager.findFirstVisibleItemPosition()
                dslRecyclerView.firstItemCompletelyVisibleAdapterPosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
            } else {

            }

            dslRecyclerView.onRecyclerScrolled.invoke(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            dslRecyclerView.onRecyclerScrollStateChanged.invoke(recyclerView, newState)
        }
    })
}

public fun RecyclerView.clearItemDecoration() {
    for (i in itemDecorationCount - 1 downTo 0) {
        removeItemDecorationAt(i)
    }
}