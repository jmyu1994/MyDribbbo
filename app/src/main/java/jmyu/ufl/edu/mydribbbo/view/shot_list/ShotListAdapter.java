package jmyu.ufl.edu.mydribbbo.view.shot_list;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.List;

import jmyu.ufl.edu.mydribbbo.R;
import jmyu.ufl.edu.mydribbbo.model.Shot;
import jmyu.ufl.edu.mydribbbo.utils.ModelUtils;
import jmyu.ufl.edu.mydribbbo.view.shot_detail.ShotActivity;
import jmyu.ufl.edu.mydribbbo.view.shot_detail.ShotFragment;

/**
 * Created by jmyu on 6/27/18.
 */

class ShotListAdapter extends RecyclerView.Adapter {

    private List<Shot> data;
    private LoadMoreListener loadMoreListener;
    private boolean showLoading;

    private static final int VIEW_TYPE_SHOT = 1;
    private static final int VIEW_TYPE_LOADING = 2;


    public ShotListAdapter(List<Shot> shots, LoadMoreListener loadMoreListener) {
        this.data = shots;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SHOT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_list_item, parent, false);
            return new ShotViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_list_loading, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_SHOT) {
            Shot shot = data.get(position);
            ShotViewHolder shotViewHolder = (ShotViewHolder) holder;
            shotViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            shotViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            shotViewHolder.viewCount.setText(String.valueOf(shot.views_count));
            shotViewHolder.image.setImageResource(R.drawable.shot_placeholder);
            //System.out.println(shot.getImageUrl());

            Picasso.with(holder.itemView.getContext())
                    .load(shot.getImageUrl())
                    .placeholder(R.drawable.shot_placeholder)
                    .into(shotViewHolder.image);

            shotViewHolder.cover.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, ShotActivity.class);
                intent.putExtra(ShotFragment.KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>(){}));
                intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);
                context.startActivity(intent);
            });
        } else if (viewType == VIEW_TYPE_LOADING) {
            loadMoreListener.loadMore();
        }

    }

    @Override
    public int getItemCount() {
        return showLoading ? data.size() + 1 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size() ? VIEW_TYPE_SHOT : VIEW_TYPE_LOADING;
    }

    public void append(List<Shot> shots) {
        data.addAll(shots);
        notifyDataSetChanged();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setShowLoading(boolean showLoading) {
        if (showLoading != this.showLoading){
            this.showLoading = showLoading;
            notifyDataSetChanged();
        }
    }

    public interface LoadMoreListener {
        public void loadMore();
    }
}
