package com.angcyo.hover.item.decoration

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.angcyo.hover.item.decoration.dsl.DslAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var baseViewHolder: RBaseViewHolder

    val itemDecoration = HoverItemDecoration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        baseViewHolder = RBaseViewHolder(window.decorView)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }


        gridLayoutTest()

        baseViewHolder.click(R.id.grid_button) {
            gridLayoutTest()
        }

        baseViewHolder.click(R.id.linear_button) {
            linearLayoutTest()
        }

        baseViewHolder.click(R.id.d1) {
            itemDecoration.let {
                it.detachedFromRecyclerView()
                it.attachToRecyclerView(baseViewHolder.rv(R.id.recycler_view)) {
                    enableTouchEvent = false
                    enableDrawableState = false
                }
            }
        }
        baseViewHolder.click(R.id.d2) {
            itemDecoration.let {
                it.detachedFromRecyclerView()
                it.attachToRecyclerView(baseViewHolder.rv(R.id.recycler_view)) {
                    enableTouchEvent = true
                    enableDrawableState = false
                }
            }
        }
        baseViewHolder.click(R.id.d3) {
            itemDecoration.let {
                it.detachedFromRecyclerView()
                it.attachToRecyclerView(baseViewHolder.rv(R.id.recycler_view)) {
                    enableTouchEvent = true
                    enableDrawableState = true
                }
            }
        }

        baseViewHolder.rv(R.id.recycler_view)?.apply {
            itemDecoration.let {
                it.detachedFromRecyclerView()
                it.attachToRecyclerView(this)
            }
        }
    }

    fun gridLayoutTest() {
        baseViewHolder.rv(R.id.recycler_view)?.apply {

            dslAdapter(4) {
                for (i in 0..2) {
                    renderImageItem(this, true)
                }

                for (i in 0..5) {
                    renderTextItem(this, 4)
                    for (i in 0..5) {
                        renderImageItem(this, true)
                    }
                }
            }
        }
    }

    fun linearLayoutTest() {
        baseViewHolder.rv(R.id.recycler_view)?.apply {
            layoutManager = LinearLayoutManager(applicationContext)

            dslAdapter {
                for (i in 0..2) {
                    renderImageItem(this)
                }

                for (i in 0..5) {
                    renderTextItem(this)

                    for (i in 0..1) {
                        renderImageItem(this)
                    }
                }
            }
        }
    }

    fun renderImageItem(dslAdapter: DslAdapter, grid: Boolean = false) {
        dslAdapter.renderItem {
            itemLayoutId = if (grid) R.layout.item_image_little else R.layout.item_image

            itemBind = { itemHolder, position, _ ->
                itemHolder.clickItem {
                    show("戳到人家[鼻孔]啦:$position")
                }
            }
        }
    }

    fun renderTextItem(dslAdapter: DslAdapter, spanCount: Int = 1) {
        dslAdapter.renderItem {
            /**极致体验, 想哪悬停, 就哪悬停*/
            itemIsHover = true
            itemSpanCount = spanCount

            itemLayoutId = R.layout.item_text

            itemBind = { itemHolder, itemPosition, _ ->
                itemHolder.tv(R.id.text_view)?.text = "位置$itemPosition"

                itemHolder.clickItem {
                    show("点击位置:$itemPosition")
                }

                itemHolder.click(R.id.check_box) {
                    show("CheckBox:$itemPosition")
                }
            }
        }
    }

    fun show(text: CharSequence) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()

        title = "${nowTime()} -> $text"
    }
}

