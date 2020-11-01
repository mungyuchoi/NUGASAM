package com.moon.nugasam

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.moon.nugasam.data.User
import java.util.*

class MyAdapter internal constructor(private val mActivity: Activity, myDataset: ArrayList<User>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    private val myName: String

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener,
        OnLongClickListener {
        var mImageView: ImageView = v.findViewById(R.id.thumbnail)
        var mTitleView: TextView = v.findViewById(R.id.title)
        var mPointView: TextView = v.findViewById(R.id.point)
        var mNugaView: TextView = v.findViewById(R.id.nuga)
        var mIconView: ImageView = v.findViewById(R.id.icon)
        var mView: View = v
        override fun onLongClick(view: View): Boolean {
            val user = (mActivity as MainActivity).getUser(adapterPosition)
            if ((user.name == myName) || (user.fullName == (myName))) {
                Toast.makeText(mActivity, "본인은 선택할 수 없습니다.", Toast.LENGTH_LONG).show()
                return false
            }
            if (mActivity.isInActionMode) {
                Toast.makeText(mActivity, "Click으로 선택해주세요.", Toast.LENGTH_LONG).show()
                return true
            }

            mActivity.prepareToolbar(adapterPosition)
            return true
        }

        override fun onClick(view: View) {
            if ((mActivity as MainActivity).isInActionMode) {
                val user = mActivity.getUser(adapterPosition)
                Log.d(
                    "MyAdapter",
                    "onCiick view:" + view + ", selectUserName: " + user.name + ", myName: " + myName
                )
                if ((user.name == myName) || (user.fullName == (myName))) {
                    Toast.makeText(mActivity, "본인은 선택할 수 없습니다.", Toast.LENGTH_LONG).show()
                    return
                }
                mActivity.prepareSelection(adapterPosition)
                notifyItemChanged(adapterPosition)
            }
        }

        init {
            v.setOnLongClickListener(this)
            v.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mDataset[position]
        holder.mView.setBackgroundResource(R.color.white)
        holder.mTitleView.text = model.name
        holder.mNugaView.text = model.nuga.toString()
        holder.mPointView.setOnClickListener(View.OnClickListener {
            if (model.point != null) {
                holder.mPointView.background = null
                holder.mPointView.text = model.point.toString()
                holder.setIsRecyclable(false)
            }
        })
        var maxPoint = 0
        var index = 0
        for (m: User in mDataset) {
            if (m.point != null && m.point > maxPoint) {
                maxPoint = m.point
                index = mDataset.indexOf(m)
            }
        }
        if (index == position) {
            holder.mIconView.visibility = View.VISIBLE
        } else {
            holder.mIconView.visibility = View.GONE
        }
        Glide.with(mActivity).load(model.imageUrl).apply(RequestOptions.circleCropTransform())
            .into(holder.mImageView)
        if ((mActivity as MainActivity).isInActionMode) {
            if (mActivity.selectionList.contains(mDataset[position])) {
                holder.mView.setBackgroundResource(R.color.grey_200)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemCount(): Int {
        return mDataset.size
    }


    fun addAllData(list: ArrayList<User>) {
        mDataset.clear()
        for (model: User in list) {
            mDataset.add(model)
        }
        notifyDataSetChanged()
    }

    companion object {
        private lateinit var mDataset: ArrayList<User>
    }

    init {
        mDataset = myDataset
        val pref = mActivity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        myName = pref.getString("name", "")
    }
}