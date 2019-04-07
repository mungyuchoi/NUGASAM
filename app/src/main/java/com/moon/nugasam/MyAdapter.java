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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private Activity mActivity;
    private static ArrayList<FirebasePost> mDataset;

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
            mImageView.setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            ((MainActivity) mActivity).prepareToolbar(getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            Log.d("MQ!", "onCiick view:" + view);
            if ( ((MainActivity) mActivity).isInActionMode()) {
                ((MainActivity) mActivity).prepareSelection(getAdapterPosition());
                notifyItemChanged(getAdapterPosition());
            }
        }
    }

    MyAdapter(Activity activity, ArrayList<FirebasePost> myDataset) {
        this.mActivity = activity;
        this.mDataset = myDataset;
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
        FirebasePost model = mDataset.get(position);
        holder.mView.setBackgroundResource(R.color.white);
        holder.mTitleView.setText(model.name);
        holder.mNugaView.setText(model.nuga.toString());
        Glide.with(mActivity).load(model.imageUrl).into(holder.mImageView);

        if ( ((MainActivity) mActivity).isInActionMode()) {
            if (((MainActivity) mActivity).getSelectionList().contains(mDataset.get(position))) {
                holder.mView.setBackgroundResource(R.color.grey_200);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static ArrayList<FirebasePost> getDataSet() {
        return mDataset;
    }

    public void removeData(ArrayList<FirebasePost> list) {
        for (FirebasePost model : list) {
            mDataset.remove(model);
        }
        notifyDataSetChanged();
    }

    public void changeDataItem(int position, FirebasePost model) {
        mDataset.set(position, model);
        notifyDataSetChanged();
    }
}