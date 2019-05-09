package com.angcyo.hover.item.decoration

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val overPositionList = mutableListOf<Int>()

    lateinit var baseViewHolder: RBaseViewHolder

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

        baseViewHolder.rv(R.id.recycler_view)?.apply {
            HoverItemDecoration().attachToRecyclerView(this) {
                decorationOverLayoutType = {
                    R.layout.item_text
                }

                haveOverDecoration = {
                    overPositionList.contains(it)
                }
            }
        }
    }

    fun gridLayoutTest() {
        overPositionList.clear()

        baseViewHolder.rv(R.id.recycler_view)?.apply {

            dslAdapter(4) {
                for (i in 0..2) {
                    renderItem {
                        itemLayoutId = R.layout.item_image_little
                    }
                }

                for (i in 0..5) {
                    overPositionList.add(i * 7 + 3)

                    renderItem {
                        itemSpanCount = 4

                        itemLayoutId = R.layout.item_text

                        itemBind = { itemHolder, itemPosition, adapterItem ->
                            itemHolder.tv(R.id.text_view)?.text = "位置$itemPosition"

                            itemHolder.clickItem {
                                show("点击位置:$itemPosition")
                            }

                            itemHolder.click(R.id.check_box) {
                                show("CheckBox:$itemPosition")
                            }
                        }
                    }

                    for (i in 0..5) {
                        renderItem {
                            itemLayoutId = R.layout.item_image_little
                        }
                    }
                }
            }
        }
    }

    fun linearLayoutTest() {
        overPositionList.clear()

        baseViewHolder.rv(R.id.recycler_view)?.apply {
            layoutManager = LinearLayoutManager(applicationContext)

            dslAdapter {
                for (i in 0..2) {
                    renderItem {
                        itemLayoutId = R.layout.item_image
                    }
                }

                for (i in 0..5) {
                    overPositionList.add(i * 3 + 3)

                    renderItem {
                        itemLayoutId = R.layout.item_text

                        itemBind = { itemHolder, itemPosition, adapterItem ->
                            itemHolder.tv(R.id.text_view)?.text = "位置$itemPosition"

                            itemHolder.clickItem {
                                show("点击位置:$itemPosition")
                            }

                            itemHolder.click(R.id.check_box) {
                                show("CheckBox:$itemPosition")
                            }
                        }
                    }

                    for (i in 0..1) {
                        renderItem {
                            itemLayoutId = R.layout.item_image
                        }
                    }
                }
            }
        }
    }

    fun show(text: CharSequence) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}

