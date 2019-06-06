# HoverItemDecoration
 Kotlin--›超轻量RecyclerView悬停效果(带touch点击事件)

# 特性
1. 支持仅绘制分割线 不带`Touch事件` 不带`Drawable状态`控制. 也就是没有任何交互. 
2. 支持仅带`Touch事件` 不带`Drawable状态`控制.
3. 极致体验 带`Touch事件` 带`Drawable状态`控制. 堪称完美!

# 使用方式

额...

```
/**极致体验, 想哪悬停, 就哪悬停*/
itemIsHover = true
```

封装的好 使用起来就是一个开关.

具体, 还是看代码吧;


---

```
HoverItemDecoration().attachToRecyclerView(recyclerView)
```

```
HoverItemDecoration().attachToRecyclerView(recyclerView) {
    enableTouchEvent = true
    enableDrawableState = true
}
```
```
HoverItemDecoration().attachToRecyclerView(recyclerView) {
    enableTouchEvent = false
    enableDrawableState = false
}
```

**更多配置**

参考[HoverCallback]类:

```
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
```
