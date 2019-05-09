package com.angcyo.hover.item.decoration

import android.app.Activity
import android.graphics.*
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/08
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class HoverItemDecoration : RecyclerView.ItemDecoration() {
    internal var recyclerView: RecyclerView? = null
    internal var hoverCallback: HoverCallback? = null
    internal var isDownInHoverItem = false
    internal var windowContent: ViewGroup? = null

    internal val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val itemTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                isDownInHoverItem = overDecorationRect.contains(event.x.toInt(), event.y.toInt())
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                isDownInHoverItem = false
            }

            if (isDownInHoverItem) {
                onTouchEvent(recyclerView, event)
            }

            return isDownInHoverItem
        }

        override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
            if (isDownInHoverItem) {
                overViewHolder?.apply {
                    //一定要调用dispatchTouchEvent, 否则ViewGroup里面的子View, 不会响应touchEvent
                    itemView.dispatchTouchEvent(event)
                    if (itemView is ViewGroup) {
                        if ((itemView as ViewGroup).onInterceptTouchEvent(event)) {
                            itemView.onTouchEvent(event)
                        }
                    } else {
                        itemView.onTouchEvent(event)
                    }
                }
            }
        }
    }

    private val attachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(view: View?) {
            removeHoverView()
        }

        override fun onViewAttachedToWindow(view: View?) {

        }
    }

    /**
     * 调用此方法, 安装悬浮分割线
     * */
    fun attachToRecyclerView(recyclerView: RecyclerView?, init: HoverCallback.() -> Unit) {
        hoverCallback = HoverCallback()
        hoverCallback?.init()

        if (this.recyclerView !== recyclerView) {
            if (this.recyclerView != null) {
                this.destroyCallbacks()
            }

            this.recyclerView = recyclerView
            if (recyclerView != null) {
                this.setupCallbacks()
            }

            (recyclerView?.context as? Activity)?.apply {
                windowContent = window.findViewById(Window.ID_ANDROID_CONTENT)
            }
        }
    }

    private fun setupCallbacks() {
        this.recyclerView?.apply {
            addItemDecoration(this@HoverItemDecoration)
            addOnItemTouchListener(itemTouchListener)
            addOnAttachStateChangeListener(attachStateChangeListener)
        }
    }

    /**
     * 从Activity移除悬浮view
     * */
    private fun removeHoverView() {
        overViewHolder?.itemView?.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    /**
     *  添加悬浮view 到 Activity, 目的是为了 系统接管 悬浮View的touch事件以及drawable的state
     * */
    private fun addHoverView(view: View) {
        if (view.parent == null) {
            windowContent?.addView(
                view, 0,
                FrameLayout.LayoutParams(overDecorationRect.width(), overDecorationRect.height())
            )
        }
    }

    private fun destroyCallbacks() {
        this.recyclerView?.apply {
            removeItemDecoration(this@HoverItemDecoration)
            removeOnItemTouchListener(itemTouchListener)
            removeOnAttachStateChangeListener(attachStateChangeListener)
        }
        removeHoverView()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        checkOverDecoration(parent)

        overViewHolder?.let {
            if (!overDecorationRect.isEmpty) {
                addHoverView(it.itemView)

                if (it.itemView.parent != null) {
                    hoverCallback?.drawOverDecoration?.invoke(canvas, paint, it, overDecorationRect)
                }
            }
        }
    }

    private fun childViewHolder(parent: RecyclerView, childIndex: Int): RecyclerView.ViewHolder? {
        if (parent.childCount > childIndex) {
            return parent.findContainingViewHolder(parent.getChildAt(childIndex))
        }
        return null
    }

    /**当前悬浮的分割线, 如果有.*/
    internal var overViewHolder: RecyclerView.ViewHolder? = null

    /**当前悬浮分割线的坐标.*/
    internal val overDecorationRect = Rect()
    /**下一个悬浮分割线的坐标.*/
    internal val nextDecorationRect = Rect()

    private var tempRect = Rect()
    /**
     * 核心方法, 用来实时监测界面上 需要浮动的 分割线.
     * */
    internal fun checkOverDecoration(parent: RecyclerView) {
        childViewHolder(parent, 0)?.let { viewHolder ->
            var firstChildAdapterPosition = viewHolder.adapterPosition

            if (firstChildAdapterPosition != RecyclerView.NO_POSITION) {

                parent.adapter?.let { adapter ->
                    hoverCallback?.let { callback ->

                        var firstChildHaveOver = callback.haveOverDecoration.invoke(firstChildAdapterPosition)

                        if (!firstChildHaveOver) {
                            //第一个child没有分割线, 查找之前最近有分割线的position
                            val findOverPrePosition = findOverPrePosition(firstChildAdapterPosition)
                            if (findOverPrePosition != RecyclerView.NO_POSITION) {
                                //找到了最近的分割线
                                firstChildHaveOver = true

                                firstChildAdapterPosition = findOverPrePosition
                            }
                        }

                        if (firstChildHaveOver) {

                            val overStartPosition = findOverStartPosition(adapter, firstChildAdapterPosition)

                            if (overStartPosition == RecyclerView.NO_POSITION) {
                                clearOverDecoration()
                                return
                            }

                            //创建第一个位置的child 需要分割线
                            val firstViewHolder =
                                callback.createDecorationOverView.invoke(
                                    parent,
                                    adapter,
                                    overStartPosition
                                )

                            val overView = firstViewHolder.itemView
                            tempRect.set(overView.left, overView.top, overView.right, overView.bottom)

                            val nextViewHolder = childViewHolder(parent, findGridNextChildIndex())
                            if (nextViewHolder != null) {
                                //紧挨着的下一个child也有分割线, 监测是否需要上推

                                if (callback.haveOverDecoration.invoke(nextViewHolder.adapterPosition) &&
                                    !callback.isOverDecorationSame.invoke(
                                        adapter,
                                        firstChildAdapterPosition,
                                        nextViewHolder.adapterPosition
                                    )
                                ) {
                                    //不同的分割线, 实现上推效果
                                    if (nextViewHolder.itemView.top < overDecorationRect.height()) {
                                        tempRect.offsetTo(
                                            0,
                                            nextViewHolder.itemView.top - overDecorationRect.height()
                                        )
                                    }
                                }
                            }

                            if (overStartPosition == firstChildAdapterPosition && viewHolder.itemView.top == 0) {
                                //第一个child, 正好是 分割线的开始位置
                                clearOverDecoration()
                            } else {
                                if (overDecorationRect != tempRect) {
                                    clearOverDecoration()

                                    overViewHolder = firstViewHolder
                                    overDecorationRect.set(tempRect)
                                }
                            }
                        } else {
                            //当前位置不需要分割线
                            clearOverDecoration()
                        }
                    }
                }
            }
        }
    }

    /**
     * 查找GridLayoutManager中, 下一个具有全屏样式的child索引
     * */
    internal fun findGridNextChildIndex(): Int {
        var result = 1
        recyclerView?.layoutManager?.apply {
            if (this is GridLayoutManager) {

                for (i in 1 until recyclerView!!.childCount) {
                    childViewHolder(recyclerView!!, i)?.let {
                        if (it.adapterPosition != RecyclerView.NO_POSITION) {
                            if (spanSizeLookup?.getSpanSize(it.adapterPosition) == this.spanCount) {
                                result = i

                                return result
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    fun clearOverDecoration() {
        overDecorationRect.clear()
        nextDecorationRect.clear()
        removeHoverView()
        overViewHolder = null
    }

    /**
     * 查找指定位置类型相同的分割线, 最开始的adapterPosition
     * */
    internal fun findOverStartPosition(
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        adapterPosition: Int
    ): Int {
        var result = adapterPosition
        for (i in adapterPosition - 1 downTo 0) {
            if (i == 0) {
                result = i
                break
            } else if (!hoverCallback!!.isOverDecorationSame(adapter, adapterPosition, i)) {
                result = i + 1
                break
            }
        }

        if (result == 0) {
            hoverCallback?.let {
                if (!it.haveOverDecoration.invoke(result)) {
                    result = RecyclerView.NO_POSITION
                }
            }
        }

        return result
    }

    /**
     * 查找指定位置 没有分割线时, 最前出现分割线的adapterPosition
     * */
    internal fun findOverPrePosition(adapterPosition: Int): Int {
        var result = RecyclerView.NO_POSITION
        for (i in adapterPosition - 1 downTo 0) {
            if (hoverCallback!!.haveOverDecoration.invoke(i)) {
                result = i
                break
            }
        }
        return result
    }

    class HoverCallback {

        /**
         * 当前的 位置 是否有 悬浮分割线
         * */
        var haveOverDecoration: (adapterPosition: Int) -> Boolean =
            { adapterPosition -> decorationOverLayoutType.invoke(adapterPosition) > 0 }

        /**
         * 根据 位置, 返回对应分割线的布局类型, 小于0, 不绘制
         *
         * @see RecyclerView.Adapter.getItemViewType
         * */
        var decorationOverLayoutType: (adapterPosition: Int) -> Int =
            { _ -> -1 }

        /**
         * 判断2个分割线是否相同, 不同的分割线, 才会悬停, 相同的分割线只会绘制一条.
         * */
        var isOverDecorationSame: (
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            nowAdapterPosition: Int, nextAdapterPosition: Int
        ) -> Boolean =
            { _, _, _ ->
                false
            }

        /**
         * 创建 分割线 视图
         * */
        var createDecorationOverView: (
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            overAdapterPosition: Int
        ) -> RecyclerView.ViewHolder = { recyclerView, adapter, overAdapterPosition ->

            //拿到分割线对应的itemType
            val layoutType = decorationOverLayoutType.invoke(overAdapterPosition)

            //复用adapter的机制, 创建View
            val holder = adapter.createViewHolder(recyclerView, layoutType)

            //注意这里的position
            adapter.bindViewHolder(holder, overAdapterPosition)

            //测量view
            holder.itemView.apply {
                val params = layoutParams

                val widthSize: Int
                val widthMode: Int
                when (params.width) {
                    -1 -> {
                        widthSize = recyclerView.measuredWidth
                        widthMode = View.MeasureSpec.EXACTLY
                    }
                    else -> {
                        widthSize = recyclerView.measuredWidth
                        widthMode = View.MeasureSpec.AT_MOST
                    }
                }

                val heightSize: Int
                val heightMode: Int
                when (params.height) {
                    -1 -> {
                        heightSize = recyclerView.measuredWidth
                        heightMode = View.MeasureSpec.EXACTLY
                    }
                    else -> {
                        heightSize = recyclerView.measuredWidth
                        heightMode = View.MeasureSpec.AT_MOST
                    }
                }

                //标准方法1
                measure(
                    View.MeasureSpec.makeMeasureSpec(widthSize, widthMode),
                    View.MeasureSpec.makeMeasureSpec(heightSize, heightMode)
                )
                //标准方法2
                layout(0, 0, measuredWidth, measuredHeight)

                //标准方法3
                //draw(canvas)
            }

            holder
        }

        /**
         * 绘制分割线
         * */
        var drawOverDecoration: (
            canvas: Canvas,
            paint: Paint,
            viewHolder: RecyclerView.ViewHolder,
            overRect: Rect
        ) -> Unit =
            { canvas, paint, viewHolder, overRect ->

                canvas.save()
                canvas.translate(overRect.left.toFloat(), overRect.top.toFloat())

                viewHolder.itemView.draw(canvas)

                if (overRect.top == 0) {
                    //分割线完全显示的情况下, 才绘制阴影
                    val shadowTop = overRect.bottom.toFloat()
                    val shadowHeight = 10

                    paint.shader = LinearGradient(
                        0f, shadowTop, 0f,
                        shadowTop + shadowHeight,
                        intArrayOf(
                            Color.parseColor("#40000000"),
                            Color.TRANSPARENT /*Color.parseColor("#40000000")*/
                        ),
                        null, Shader.TileMode.CLAMP
                    )

                    //绘制阴影
                    canvas.drawRect(
                        overRect.left.toFloat(),
                        shadowTop,
                        overRect.right.toFloat(),
                        shadowTop + shadowHeight,
                        paint
                    )
                }

                canvas.restore()
            }
    }
}

fun Rect.clear() {
    set(0, 0, 0, 0)
}