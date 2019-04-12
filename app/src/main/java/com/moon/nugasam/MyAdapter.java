package com.moon.nugasam;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.moon.nugasam.data.User;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private Activity mActivity;
    private static ArrayList<User> mDataset;
    private String myName;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView mImageView;
        TextView mTitleView;
        TextView mNugaView;
        View mView;

        ViewHolder(View v) {
            super(v);
            mTitleView = v.findViewById(R.id.title);
            mNugaView = v.findViewById(R.id.nuga);
            mImageView = v.findViewById(R.id.thumbnail);
            mView = v;
            v.setOnLongClickListener(this);
            v.setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            User user = ((MainActivity) mActivity).getUser(getAdapterPosition());
            if (user.name.equals(myName) || user.fullName.equals((myName))) {
                Toast.makeText(mActivity, "본인은 선택할 수 없습니다.", Toast.LENGTH_LONG).show();
                return false;
            }
            ((MainActivity) mActivity).prepareToolbar(getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            if (((MainActivity) mActivity).isInActionMode()) {
                User user = ((MainActivity) mActivity).getUser(getAdapterPosition());
                Log.d("MQ!", "onCiick view:" + view + ", selectUserName: " + user.name + ", myName: " + myName);
                if (user.name.equals(myName) || user.fullName.equals((myName))) {
                    Toast.makeText(mActivity, "본인은 선택할 수 없습니다.", Toast.LENGTH_LONG).show();
                    return;
                }

                ((MainActivity) mActivity).prepareSelection(getAdapterPosition());
                notifyItemChanged(getAdapterPosition());
            }
        }
    }

    MyAdapter(Activity activity, ArrayList<User> myDataset) {
        this.mActivity = activity;
        this.mDataset = myDataset;

        SharedPreferences pref = mActivity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE);
        myName = pref.getString("name", "");
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User model = mDataset.get(position);
        holder.mView.setBackgroundResource(R.color.white);
        holder.mTitleView.setText(model.name);
        holder.mNugaView.setText(model.nuga.toString());
        Glide.with(mActivity).load(model.imageUrl).apply(RequestOptions.circleCropTransform()).into(holder.mImageView);

        if (((MainActivity) mActivity).isInActionMode()) {
            if (((MainActivity) mActivity).getSelectionList().contains(mDataset.get(position))) {
                holder.mView.setBackgroundResource(R.color.grey_200);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addAllData(ArrayList<User> list) {
        mDataset.clear();
        for (User model : list) {
            mDataset.add(model);
        }
        notifyDataSetChanged();
    }
}