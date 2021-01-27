package com.moon.nugasam

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.moon.nugasam.data.User

class MeerkatAdapter internal constructor(val activity: MainActivityV2) : ListAdapter<User, MeerkatAdapter.ItemViewHolder>(COMPARATOR) {

    private lateinit var myName: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.d("MeerkatAdapter", "onCreateViewHolder viewType:$viewType")
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(
            inflater.inflate(
                R.layout.row_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.d("MeerkatAdapter", "onBindViewHOlder position:$position")
        val model = getItem(position)
        holder.view.setBackgroundResource(R.color.white)
        holder.title.text = model.name
        holder.nuga.text = model.nuga.toString()
        holder.point.setOnClickListener {
            if (model.point != null && !activity.isInActionMode) {
                holder.point.background = null
                holder.point.text = model.point.toString()
                holder.setIsRecyclable(false)
            }
        }
        var maxPoint = 0
        var index = 0

        for (m: User in currentList) {
            if (m.point != null && m.point > maxPoint) {
                maxPoint = m.point
                index = currentList.indexOf(m)
            }
        }
        if (index == position) {
            holder.icon.visibility = View.VISIBLE
        } else {
            holder.icon.visibility = View.GONE
        }
        Glide.with(activity).load(model.imageUrl).apply(RequestOptions.circleCropTransform())
            .into(holder.thumbnail)
        if ((activity as MainActivityV2).isInActionMode) {
            if (activity.selectionList.contains(currentList[position])) {
                holder.view.setBackgroundResource(R.color.grey_200)
            }
        }
    }

    inner class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener,
        View.OnLongClickListener {
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val title: TextView = itemView.findViewById(R.id.title)
        val point: TextView = itemView.findViewById(R.id.point)
        val nuga: TextView = itemView.findViewById(R.id.nuga)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val view: View = itemView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            if ((activity as MainActivityV2).isInActionMode) {
                val user = activity.userInfo[adapterPosition]
                Log.d(
                    "MeerkatAdapter",
                    "onClick view:" + view + ", selectUserName: " + user.name + ", myName: " + myName
                )
                if ((user.name == myName) || (user.fullName == (myName))) {
                    Toast.makeText(activity, "본인은 선택할 수 없습니다.", Toast.LENGTH_LONG).show()
                    return
                }
                activity.prepareSelection(adapterPosition)
                notifyItemChanged(adapterPosition)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val user = (activity as MainActivityV2).userInfo[adapterPosition]
            Log.d("MeerkatAdapter", "onLongClick user:$user")
            if ((user.name == myName) || (user.fullName == (myName))) {
                Toast.makeText(activity, "본인은 선택할 수 없습니다.", Toast.LENGTH_LONG).show()
                return false
            }
            if (activity.isInActionMode) {
                Toast.makeText(activity, "Click으로 선택해주세요.", Toast.LENGTH_LONG).show()
                return true
            }

            activity.prepareToolbar(adapterPosition)
            return true
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    init {
        val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        myName = pref.getString("name", "")
    }
    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(old: User, new: User): Boolean =
                old.name == new.name

            override fun areContentsTheSame(old: User, new: User): Boolean =
                old == new
        }
    }
}