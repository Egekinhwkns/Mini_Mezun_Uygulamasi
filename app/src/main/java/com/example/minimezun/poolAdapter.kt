package com.example.minimezun

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class poolAdapter(val tmpList : MutableList<PoolModel>) : RecyclerView.Adapter<poolAdapter.Holder>() {

    class Holder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val pp = itemView.findViewById<ImageView>(R.id.ppPoolItem)
        val poolImg = itemView.findViewById<ImageView>(R.id.imagePoolItem)
        val poolText = itemView.findViewById<TextView>(R.id.textPoolItem)

        fun bindItems(item : PoolModel){

            poolText.text = item.poolText

            pp.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    item.pp,
                    0,
                    item.pp.size
                )
            )
            pp.clipToOutline = true

            poolImg.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    item.poolImg,
                    0,
                    item.poolImg.size
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pool_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return tmpList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindItems(tmpList.get(position))
    }
}