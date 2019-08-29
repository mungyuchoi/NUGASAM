package com.moon.nugasam;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
import com.kongzue.dialog.v2.SelectDialog;
import com.moon.nugasam.data.UndoData;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.moon.nugasam.data.User;
import org.jetbrains.annotations.NotNull;

public class UndoAdapter extends RecyclerView.Adapter<UndoAdapter.ViewHolder> {

    private Activity mActivity;
    private static ArrayList<UndoData> mDataset;

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        // date
        TextView mDateView;

        // me
        ImageView mImageView;
        TextView mTitleView;

        // who
        View mItem1;
        ImageView mImageView1;
        TextView mTitleView1;

        View mItem2;
        ImageView mImageView2;
        TextView mTitleView2;

        View mItem3;
        ImageView mImageView3;
        TextView mTitleView3;

        View mItem4;
        ImageView mImageView4;
        TextView mTitleView4;

        View mItem5;
        ImageView mImageView5;
        TextView mTitleView5;

        View mItem6;
        ImageView mImageView6;
        TextView mTitleView6;

        View mItem7;
        ImageView mImageView7;
        TextView mTitleView7;

        View mItem8;
        ImageView mImageView8;
        TextView mTitleView8;

        View mItem9;
        ImageView mImageView9;
        TextView mTitleView9;


        View mItem10;
        ImageView mImageView10;
        TextView mTitleView10;

        ViewHolder(View v) {
            super(v);
            mDateView = v.findViewById(R.id.date);
            mImageView = v.findViewById(R.id.thumbnail);
            mTitleView = v.findViewById(R.id.title);

            mImageView1 = v.findViewById(R.id.thumbnail1);
            mTitleView1 = v.findViewById(R.id.title1);
            mImageView2 = v.findViewById(R.id.thumbnail2);
            mTitleView2 = v.findViewById(R.id.title2);
            mImageView3 = v.findViewById(R.id.thumbnail3);
            mTitleView3 = v.findViewById(R.id.title3);
            mImageView4 = v.findViewById(R.id.thumbnail4);
            mTitleView4 = v.findViewById(R.id.title4);
            mImageView5 = v.findViewById(R.id.thumbnail5);
            mTitleView5 = v.findViewById(R.id.title5);
            mImageView6 = v.findViewById(R.id.thumbnail6);
            mTitleView6 = v.findViewById(R.id.title6);
            mImageView7 = v.findViewById(R.id.thumbnail7);
            mTitleView7 = v.findViewById(R.id.title7);
            mImageView8 = v.findViewById(R.id.thumbnail8);
            mTitleView8 = v.findViewById(R.id.title8);
            mImageView9 = v.findViewById(R.id.thumbnail9);
            mTitleView9 = v.findViewById(R.id.title9);
            mImageView10 = v.findViewById(R.id.thumbnail10);
            mTitleView10 = v.findViewById(R.id.title10);

            mItem1 = v.findViewById(R.id.item1);
            mItem2 = v.findViewById(R.id.item2);
            mItem3 = v.findViewById(R.id.item3);
            mItem4 = v.findViewById(R.id.item4);
            mItem5 = v.findViewById(R.id.item5);
            mItem6 = v.findViewById(R.id.item6);
            mItem7 = v.findViewById(R.id.item7);
            mItem8 = v.findViewById(R.id.item8);
            mItem9 = v.findViewById(R.id.item9);
            mItem10 = v.findViewById(R.id.item10);
           // v.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            Log.d("MQ!", "onLongClick position: " + getAdapterPosition());
            SelectDialog dialog = SelectDialog.build(mActivity, "정말 취소하나요?", "", "네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    Query date = ref.child("history").orderByChild("date").equalTo(mDataset.get(getAdapterPosition()).date);
                    date.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UndoData undoData = mDataset.get(getAdapterPosition());
                            // who의 개수를 확인, me의 full name을 user로부터 가져와서 nuga값에 다가 who의 개수를 뺀다.
                            // 그리고 업데이트
                            int whosCount = undoData.who.size();


                            Map childUpdates = new HashMap<String, Object>();
                            User me = ((UndoActivity) mActivity).getUser(undoData.me.fullName);
                            me.nuga -= whosCount;
                            Log.d("MQ!", "me : " + undoData.me.fullName + ", whoCount: " + whosCount + ", me key: " + ((UndoActivity) mActivity).getKey(undoData.me));
                            childUpdates.put("/users/" + ((UndoActivity) mActivity).getKey(undoData.me), me);
                            for (int i = 0; i < whosCount; i++) {
                                User user = ((UndoActivity) mActivity).getUser(undoData.who.get(i).fullName);
                                user.nuga += 1;
                                Log.d("MQ!", "who: " + user.fullName + ", key : " + ((UndoActivity) mActivity).getKey(user));
                                childUpdates.put("/users/" + ((UndoActivity) mActivity).getKey(user), user);
                            }
                            ref.updateChildren(childUpdates);

                            // who에서는 full name의 user로부터 가져와서 nuga값 += 1을한다.
                            // who의 개수만큼

                            // 삭제
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                data.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }, "아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.setDialogStyle(1);
            dialog.showDialog();
            return true;
        }
    }

    UndoAdapter(Activity activity, ArrayList<UndoData> dataset) {
        this.mActivity = activity;
        this.mDataset = dataset;
    }

    @NonNull
    @Override
    public UndoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.undo_list_item, parent, false);
        return new UndoAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UndoAdapter.ViewHolder holder, int position) {
        if (mDataset.size() == 0) {
            return;
        }
        UndoData model = mDataset.get(position);
        holder.mDateView.setText(DateFormat.getInstance().format(Long.parseLong(model.date)));
        Glide.with(mActivity).load(model.me.imageUrl).apply(RequestOptions.circleCropTransform()).into(holder.mImageView);
        holder.mTitleView.setText(model.me.name);

        int count = model.who.size();
        int index = 0;
        while (index < count) {
            String imageUrl = model.who.get(index).imageUrl;
            String title = model.who.get(index).name;
            ImageView imageView = getImageView(holder, index);
            TextView textView = getTextView(holder, index);
            View item = getItemView(holder, index);
            item.setVisibility(View.VISIBLE);
            Glide.with(mActivity).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(imageView);
            textView.setText(title);
            index++;
        }
    }

    private View getItemView(ViewHolder holder, int index) {
        View item = null;
        switch (index) {
            case 0:
                item = holder.mItem1;
                break;
            case 1:
                item = holder.mItem2;
                break;
            case 2:
                item = holder.mItem3;
                break;
            case 3:
                item = holder.mItem4;
                break;
            case 4:
                item = holder.mItem5;
                break;
            case 5:
                item = holder.mItem6;
                break;
            case 6:
                item = holder.mItem7;
                break;
            case 7:
                item = holder.mItem8;
                break;
            case 8:
                item = holder.mItem9;
                break;
            case 9:
                item = holder.mItem10;
                break;
        }
        return item;
    }

    private TextView getTextView(ViewHolder holder, int index) {
        TextView textView = null;
        switch (index) {
            case 0:
                textView = holder.mTitleView1;
                break;
            case 1:
                textView = holder.mTitleView2;
                break;
            case 2:
                textView = holder.mTitleView3;
                break;
            case 3:
                textView = holder.mTitleView4;
                break;
            case 4:
                textView = holder.mTitleView5;
                break;
            case 5:
                textView = holder.mTitleView6;
                break;
            case 6:
                textView = holder.mTitleView7;
                break;
            case 7:
                textView = holder.mTitleView8;
                break;
            case 8:
                textView = holder.mTitleView9;
                break;
            case 9:
                textView = holder.mTitleView10;
                break;
        }
        return textView;
    }

    private ImageView getImageView(ViewHolder holder, int index) {
        ImageView imageView = null;
        switch (index) {
            case 0:
                imageView = holder.mImageView1;
                break;
            case 1:
                imageView = holder.mImageView2;
                break;
            case 2:
                imageView = holder.mImageView3;
                break;
            case 3:
                imageView = holder.mImageView4;
                break;
            case 4:
                imageView = holder.mImageView5;
                break;
            case 5:
                imageView = holder.mImageView6;
                break;
            case 6:
                imageView = holder.mImageView7;
                break;
            case 7:
                imageView = holder.mImageView8;
                break;
            case 8:
                imageView = holder.mImageView9;
                break;
            case 9:
                imageView = holder.mImageView10;
                break;
        }
        return imageView;

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addAllData(@NotNull ArrayList<UndoData> list) {
        mDataset.clear();
        for (UndoData model : list) {
            mDataset.add(model);
        }
        notifyDataSetChanged();
    }
}
