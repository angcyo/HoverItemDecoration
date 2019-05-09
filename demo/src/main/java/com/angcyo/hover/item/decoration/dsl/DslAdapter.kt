package com.angcyo.hover.item.decoration.dsl

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.angcyo.hover.item.decoration.RBaseViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/07
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslAdapter(var context: Context) : RecyclerView.Adapter<RBaseViewHolder>() {

    val mAllDatas = mutableListOf<DslAdapterItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RBaseViewHolder {
        var itemView: View? = null
        var viewHolder: RBaseViewHolder? = null
        var itemLayoutId = -1
        if (itemView == null) {
            itemLayoutId = getItemLayoutId(viewType)
            try {
                itemView = LayoutInflater.from(context).inflate(itemLayoutId, parent, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        viewHolder = RBaseViewHolder(itemView!!)
        return viewHolder
    }

    override fun onBindViewHolder(p0: RBaseViewHolder, p1: Int) {
        onBindView(p0, p1, getItemData(p1))
    }

    override fun getItemViewType(position: Int): Int {
        return getItemType(position)
    }

    /**
     * 在最后的位置插入数据
     */
    fun addLastItem(bean: DslAdapterItem) {
        val startPosition = mAllDatas.size
        mAllDatas.add(bean)
        notifyItemInserted(startPosition)
        notifyItemRangeChanged(startPosition, itemCount)
    }

    fun getItemData(position: Int): DslAdapterItem {
        return mAllDatas[position]
    }

    override fun getItemCount(): Int {
        return mAllDatas.size
    }

    fun getItemLayoutId(viewType: Int): Int {
        return viewType
    }

    fun getItemType(position: Int): Int {
        return getItemData(position).itemLayoutId
    }

    fun onBindView(holder: RBaseViewHolder, position: Int, bean: DslAdapterItem?) {
        bean?.let {
            it.itemBind.invoke(holder, position, it)
        }
    }

}