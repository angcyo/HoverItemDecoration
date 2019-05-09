package com.angcyo.hover.item.decoration.dsl

import com.angcyo.hover.item.decoration.RBaseViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/07
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslAdapterItem {

    /**
     * 在 GridLayoutManager 中, 需要占多少个 span
     * */
    var itemSpanCount = 1

    /**布局的xml id, 必须设置.*/
    var itemLayoutId: Int = -1

    /**附加的数据*/
    var itemData: Any? = null

    /**界面绑定*/
    var itemBind: (itemHolder: RBaseViewHolder, itemPosition: Int, adapterItem: DslAdapterItem) -> Unit = { _, _, _ -> }
}
