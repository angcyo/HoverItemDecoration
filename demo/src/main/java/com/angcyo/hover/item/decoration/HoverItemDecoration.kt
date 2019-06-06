package com.angcyo.hover.item.decoration

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.FrameLayout
import com.angcyo.hover.item.decoration.dsl.DslAdapter

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

    /**分割线追加在的容器*/
    internal var windowContent: ViewGroup? = null

    /**需要移除的分割线*/
    internal val removeViews = arrayListOf<View>()

    val cancelEvent = Runnable {
        overViewHolder?.apply {

            itemView.dispatchTouchEvent(
                MotionEvent.obtain(
                    nowTime(),
                    nowTime(),
                    MotionEvent.ACTION_UP,
                    0f,
                    0f,
                    0
                )
            )
        }
    }

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
                    if (hoverCallback?.enableDrawableState == true) {
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                            //有些时候, 可能收不到 up/cancel 事件, 延时发送cancel事件, 解决一些 界面drawable的bug
                            recyclerView.postDelayed(cancelEvent, 160L)
                        } else {
                            recyclerView.removeCallbacks(cancelEvent)
                        }

                        //一定要调用dispatchTouchEvent, 否则ViewGroup里面的子View, 不会响应touchEvent
                        itemView.dispatchTouchEvent(event)
                        if (itemView is ViewGroup) {
                            if ((itemView as ViewGroup).onInterceptTouchEvent(event)) {
                                itemView.onTouchEvent(event)
                            }
                        } else {
                            itemView.onTouchEvent(event)
                        }
                    } else {
                        //没有Drawable状态, 需要手动控制touch事件, 因为系统已经管理不了 itemView了
                        if (event.actionMasked == MotionEvent.ACTION_UP) {
                            itemView.performClick()
                        }
                    }
                }
            }
        }
    }

    private val attachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(view: View?) {
            removeAllHoverView()
        }

        override fun onViewAttachedToWindow(view: View?) {

        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //滚动状态结束
                removeAllHoverView()
            }
        }
    }

    /**
     * 调用此方法, 安装悬浮分割线
     * */
    fun attachToRecyclerView(recyclerView: RecyclerView?, init: HoverCallback.() -> Unit = {}) {
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

    /**卸载分割线*/
    fun detachedFromRecyclerView() {
        hoverCallback = null

        if (this.recyclerView != null) {
            this.destroyCallbacks()
        }
        isDownInHoverItem = false
        windowContent = null
        recyclerView = null
    }

    private fun setupCallbacks() {
        this.recyclerView?.apply {
            addItemDecoration(this@HoverItemDecoration)
            if (hoverCallback?.enableTouchEvent == true) {
                addOnItemTouchListener(itemTouchListener)
            }
            addOnAttachStateChangeListener(attachStateChangeListener)
            addOnScrollListener(scrollListener)
        }
    }

    private fun destroyCallbacks() {
        this.recyclerView?.apply {
            removeItemDecoration(this@HoverItemDecoration)
            removeOnItemTouchListener(itemTouchListener)
            removeOnAttachStateChangeListener(attachStateChangeListener)
            removeOnScrollListener(scrollListener)
        }
        removeAllHoverView()
    }

    private fun removeAllHoverView() {
        removeViews.forEach {
            (it.parent as? ViewGroup)?.removeView(it)
        }

        removeViews.clear()
    }

    /**
     * 从Activity移除悬浮view
     * */
    private fun removeHoverView() {
        overViewHolder?.itemView?.apply {
            dispatchTouchEvent(MotionEvent.obtain(nowTime(), nowTime(), MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
            //(parent as? ViewGroup)?.removeView(this)

            removeViews.add(this)
        }
    }

    /**
     *  添加悬浮view 到 Activity, 目的是为了 系统接管 悬浮View的touch事件以及drawable的state
     * */
    private fun addHoverView(view: View) {
        if (view.parent == null) {
            windowContent?.addView(
                view, 0,
                FrameLayout.LayoutParams(overDecorationRect.width(), overDecorationRect.height()).apply {
                    val viewRect = recyclerView?.getViewRect()

                    leftMargin = overDecorationRect.left + (viewRect?.left ?: 0)
                    topMargin = overDecorationRect.top + (viewRect?.top ?: 0)
                }
            )
        }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (state.isPreLayout || state.willRunSimpleAnimations()) {
            return
        }

        checkOverDecoration(parent)

        overViewHolder?.let {
            if (!overDecorationRect.isEmpty) {

                //L.d("...onDrawOverDecoration...")
                hoverCallback?.apply {
                    if (enableTouchEvent && enableDrawableState) {
                        addHoverView(it.itemView)
                    }

                    drawOverDecoration.invoke(canvas, paint, it, overDecorationRect)
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
    val overDecorationRect = Rect()
    /**下一个悬浮分割线的坐标.*/
    internal val nextDecorationRect = Rect()
    /**分割线的所在位置*/
    var overAdapterPosition = RecyclerView.NO_POSITION

    private var tempRect = Rect()
    /**
     * 核心方法, 用来实时监测界面上 需要浮动的 分割线.
     * */
    internal fun checkOverDecoration(parent: RecyclerView) {
        childViewHolder(parent, 0)?.let { viewHolder ->
            val firstChildAdapterPosition = viewHolder.adapterPosition

            if (firstChildAdapterPosition != RecyclerView.NO_POSITION) {

                parent.adapter?.let { adapter ->
                    hoverCallback?.let { callback ->

                        var firstChildHaveOver = callback.haveOverDecoration.invoke(adapter, firstChildAdapterPosition)

                        //第一个child, 需要分割线的 position 的位置
                        var firstChildHaveOverPosition = firstChildAdapterPosition

                        if (!firstChildHaveOver) {
                            //第一个child没有分割线, 查找之前最近有分割线的position
                            val findOverPrePosition = findOverPrePosition(adapter, firstChildAdapterPosition)
                            if (findOverPrePosition != RecyclerView.NO_POSITION) {
                                //找到了最近的分割线
                                firstChildHaveOver = true

                                firstChildHaveOverPosition = findOverPrePosition
                            }
                        }

                        if (firstChildHaveOver) {

                            val overStartPosition = findOverStartPosition(adapter, firstChildHaveOverPosition)

                            if (overStartPosition == RecyclerView.NO_POSITION) {
                                clearOverDecoration()
                                return
                            } else {
                                firstChildHaveOverPosition = overStartPosition
                            }

                            //创建第一个位置的child 需要分割线
                            val firstViewHolder =
                                callback.createDecorationOverView.invoke(
                                    parent,
                                    adapter,
                                    firstChildHaveOverPosition
                                )

                            val overView = firstViewHolder.itemView
                            tempRect.set(overView.left, overView.top, overView.right, overView.bottom)

                            val nextViewHolder = childViewHolder(parent, findGridNextChildIndex())
                            if (nextViewHolder != null) {
                                //紧挨着的下一个child也有分割线, 监测是否需要上推

                                if (callback.haveOverDecoration.invoke(adapter, nextViewHolder.adapterPosition) &&
                                    !callback.isOverDecorationSame.invoke(
                                        adapter,
                                        firstChildAdapterPosition,
                                        nextViewHolder.adapterPosition
                                    )
                                ) {
                                    //不同的分割线, 实现上推效果
                                    if (nextViewHolder.itemView.top < tempRect.height()) {
                                        tempRect.offsetTo(
                                            0,
                                            nextViewHolder.itemView.top - tempRect.height()
                                        )
                                    }
                                }
                            }

                            if (firstChildHaveOverPosition == firstChildAdapterPosition && viewHolder.itemView.top >= 0 /*考虑分割线*/) {
                                //第一个child, 正好是 分割线的开始位置
                                clearOverDecoration()
                            } else {
                                if (overAdapterPosition != firstChildHaveOverPosition) {
                                    clearOverDecoration()

                                    overViewHolder = firstViewHolder
                                    overDecorationRect.set(tempRect)

                                    overAdapterPosition = firstChildHaveOverPosition
                                } else if (overDecorationRect != tempRect) {
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
        overAdapterPosition = RecyclerView.NO_POSITION
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
                if (hoverCallback!!.isOverDecorationSame(adapter, adapterPosition, i)) {
                    result = i
                }
                break
            } else if (!hoverCallback!!.isOverDecorationSame(adapter, adapterPosition, i)) {
                result = i + 1
                break
            }
        }

        if (result == 0) {
            hoverCallback?.let {
                if (!it.haveOverDecoration.invoke(adapter, result)) {
                    result = RecyclerView.NO_POSITION
                }
            }
        }

        return result
    }

    /**
     * 查找指定位置 没有分割线时, 最前出现分割线的adapterPosition
     * */
    internal fun findOverPrePosition(adapter: RecyclerView.Adapter<*>, adapterPosition: Int): Int {
        var result = RecyclerView.NO_POSITION
        for (i in adapterPosition - 1 downTo 0) {
            if (hoverCallback!!.haveOverDecoration.invoke(adapter, i)) {
                result = i
                break
            }
        }
        return result
    }

    class HoverCallback {

        /**激活touch手势*/
        var enableTouchEvent = true

        /**激活drawable点击效果, 此功能需要先 激活 touch 手势*/
        var enableDrawableState = enableTouchEvent

        /**
         * 当前的 位置 是否有 悬浮分割线
         * */
        var haveOverDecoration: (adapter: RecyclerView.Adapter<*>, adapterPosition: Int) -> Boolean =
            { adapter, adapterPosition ->
                if (adapter is DslAdapter) {
                    adapter.getItemData(adapterPosition).itemIsHover
                } else {
                    decorationOverLayoutType.invoke(adapter, adapterPosition) > 0
                }
            }

        /**
         * 根据 位置, 返回对应分割线的布局类型, 小于0, 不绘制
         *
         * @see RecyclerView.Adapter.getItemViewType
         * */
        var decorationOverLayoutType: (adapter: RecyclerView.Adapter<*>, adapterPosition: Int) -> Int =
            { adapter, adapterPosition ->
                if (adapter is DslAdapter) {
                    adapter.getItemViewType(adapterPosition)
                } else {
                    -1
                }
            }

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
            val layoutType = decorationOverLayoutType.invoke(adapter, overAdapterPosition)

            //复用adapter的机制, 创建View
            val holder = adapter.createViewHolder(recyclerView, layoutType)

            //注意这里的position
            adapter.bindViewHolder(holder, overAdapterPosition)

            //测量view
            measureHoverView.invoke(recyclerView, holder.itemView)

            holder
        }

        /**自定义layout的分割线, 不使用 adapter中的xml*/
        val customDecorationOverView: (
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
            overAdapterPosition: Int
        ) -> RecyclerView.ViewHolder = { recyclerView, adapter, overAdapterPosition ->

            //拿到分割线对应的itemType
            val layoutType = decorationOverLayoutType.invoke(adapter, overAdapterPosition)

            val itemView = LayoutInflater.from(recyclerView.context).inflate(layoutType, recyclerView, false)

            val holder = RBaseViewHolder(itemView)

            //注意这里的position
            adapter.bindViewHolder(holder, overAdapterPosition)

            //测量view
            measureHoverView.invoke(recyclerView, holder.itemView)

            holder
        }

        /**
         * 测量 View, 确定宽高和绘制坐标
         * */
        var measureHoverView: (parent: RecyclerView, hoverView: View) -> Unit = { parent, hoverView ->
            hoverView.apply {
                val params = layoutParams

                val widthSize: Int
                val widthMode: Int
                when (params.width) {
                    -1 -> {
                        widthSize = parent.measuredWidth
                        widthMode = View.MeasureSpec.EXACTLY
                    }
                    else -> {
                        widthSize = parent.measuredWidth
                        widthMode = View.MeasureSpec.AT_MOST
                    }
                }

                val heightSize: Int
                val heightMode: Int
                when (params.height) {
                    -1 -> {
                        heightSize = parent.measuredWidth
                        heightMode = View.MeasureSpec.EXACTLY
                    }
                    else -> {
                        heightSize = parent.measuredWidth
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
        }

        /**
         * 绘制分割线, 请不要使用 foreground 属性.
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

                if (enableDrawShadow) {
                    drawOverShadowDecoration.invoke(canvas, paint, viewHolder, overRect)
                }

                canvas.restore()
            }

        /**是否激活阴影绘制*/
        var enableDrawShadow = true

        /**
         * 绘制分割线下面的阴影, 或者其他而外的信息
         * */
        var drawOverShadowDecoration: (
            canvas: Canvas,
            paint: Paint,
            viewHolder: RecyclerView.ViewHolder,
            overRect: Rect
        ) -> Unit =
            { canvas, paint, viewHolder, overRect ->

                if (overRect.top == 0) {
                    //分割线完全显示的情况下, 才绘制阴影
                    val shadowTop = overRect.bottom.toFloat()
                    val shadowHeight = 4 * dp

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
            }
    }
}

fun Rect.clear() {
    set(0, 0, 0, 0)
}

public inline fun <T> T.nowTime() = System.currentTimeMillis()


/**
 * 获取View, 相对于手机屏幕的矩形
 * */
public fun View.getViewRect(result: Rect = Rect()): Rect {
    var offsetX = 0
    var offsetY = 0

    //横屏, 并且显示了虚拟导航栏的时候. 需要左边偏移
    //只计算一次
    (context as? Activity)?.let {
        it.window.decorView.getGlobalVisibleRect(result)
        if (result.width() > result.height()) {
            //横屏了
            offsetX = -navBarHeight(it)
        }
    }

    return getViewRect(offsetX, offsetY, result)
}

/**
 * 获取View, 相对于手机屏幕的矩形, 带皮阿尼一
 * */
public fun View.getViewRect(offsetX: Int, offsetY: Int, result: Rect = Rect()): Rect {
    //可见位置的坐标, 超出屏幕的距离会被剃掉
    //getGlobalVisibleRect(r)
    val r2 = IntArray(2)
    //val r3 = IntArray(2)
    //相对于屏幕的坐标
    getLocationOnScreen(r2)
    //相对于窗口的坐标
    //getLocationInWindow(r3)

    val left = r2[0] + offsetX
    val top = r2[1] + offsetY

    result.set(left, top, left + measuredWidth, top + measuredHeight)
    return result
}


/**
 * 导航栏的高度(如果显示了)
 */
fun navBarHeight(context: Context): Int {
    var result = 0

    if (context is Activity) {
        val decorRect = Rect()
        val windowRect = Rect()

        context.window.decorView.getGlobalVisibleRect(decorRect)
        context.window.findViewById<View>(Window.ID_ANDROID_CONTENT).getGlobalVisibleRect(windowRect)

        if (decorRect.width() > decorRect.height()) {
            //横屏
            result = decorRect.width() - windowRect.width()
        } else {
            //竖屏
            result = decorRect.bottom - windowRect.bottom
        }
    }

    return result
}

public val <T> T.dp: Float by lazy {
    Resources.getSystem()?.displayMetrics?.density ?: 0f
}