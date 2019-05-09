package com.angcyo.hover.item.decoration.dsl

import android.support.v7.widget.RecyclerView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/08
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslRecyclerScroll {

    var firstItemAdapterPosition = RecyclerView.NO_POSITION

    var firstItemCompletelyVisibleAdapterPosition = RecyclerView.NO_POSITION

    /**
     * @see RecyclerView.OnScrollListener.onScrolled
     * */
    var onRecyclerScrolled: (recyclerView: RecyclerView, dx: Int, dy: Int) -> Unit = { _, _, _ -> }

    /**
     * @see RecyclerView.OnScrollListener.onScrollStateChanged
     * */
    var onRecyclerScrollStateChanged: (recyclerView: RecyclerView, newState: Int) -> Unit = { _, _ -> }

}