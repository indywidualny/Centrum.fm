package org.indywidualni.centrumfm.rest.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private List<Song> mDataset;
    private ViewHolder.IViewHolderClicks viewHolderClicks;

    // provide a suitable constructor
    public SongsAdapter(ViewHolder.IViewHolderClicks viewHolderClicks, List<Song> dataset) {
        this.viewHolderClicks = viewHolderClicks != null ? viewHolderClicks : null;
        this.mDataset = new ArrayList<>(dataset);
    }

    // create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_song,
                viewGroup, false);

        return new ViewHolder(v, viewHolderClicks);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // get element from a dataset at this position and replace the contents of the view
        viewHolder.getId().setText(Integer.toString(position + 1));
        viewHolder.getTitle().setText(mDataset.get(position).getTitle());
        viewHolder.getArtist().setText(mDataset.get(position).getArtist());
        viewHolder.getDuration().setText(mDataset.get(position).getDuration());

        if (!TextUtils.isEmpty(mDataset.get(position).getPlayed()))
            viewHolder.getPlayed().setText(mDataset.get(position).getPlayed().replace("T", "  "));
        else if (!TextUtils.isEmpty(mDataset.get(position).getSum()))
            viewHolder.getPlayed().setText(MyApplication.getContextOfApplication()
                    .getString(R.string.played_n_times, mDataset.get(position).getSum()));
        else
            viewHolder.getPlayed().setVisibility(View.GONE);
    }

    // return the size of a dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // get current dataset
    public final List<Song> getDataset() {
        return mDataset;
    }

    // set new dataset
    public void setDataset(List<Song> dataset) {
        mDataset = new ArrayList<>(dataset);
        notifyDataSetChanged();
    }

    // search helpers
    public Song removeItem(int position) {
        final Song model = mDataset.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Song model) {
        mDataset.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Song model = mDataset.remove(fromPosition);
        mDataset.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    // search support
    public void animateTo(List<Song> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Song> newModels) {
        for (int i = mDataset.size() - 1; i >= 0; i--) {
            final Song model = mDataset.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Song> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Song model = newModels.get(i);
            if (!mDataset.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Song> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Song model = newModels.get(toPosition);
            final int fromPosition = mDataset.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private final LinearLayout linearLayout;
        private final TextView id;
        private final TextView title;
        private final TextView artist;
        private final TextView played;
        private final TextView duration;
        private IViewHolderClicks mListener;

        public ViewHolder(View v, IViewHolderClicks listener) {
            super(v);
            mListener = listener;

            linearLayout = (LinearLayout) v.findViewById(R.id.element);
            id = (TextView) v.findViewById(R.id.id);
            title = (TextView) v.findViewById(R.id.title);
            artist = (TextView) v.findViewById(R.id.artist);
            played = (TextView) v.findViewById(R.id.played);
            duration = (TextView) v.findViewById(R.id.duration);

            linearLayout.setOnClickListener(this);
            linearLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                if (v instanceof LinearLayout)
                    mListener.onContentClick((LinearLayout) v, getAdapterPosition());
            }
        }
        
        @Override
        public boolean onLongClick(View v) {
            return mListener != null && mListener.onContentLongClick(getAdapterPosition());
        }

        public TextView getId() {
            return id;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getArtist() {
            return artist;
        }

        public TextView getPlayed() {
            return played;
        }

        public TextView getDuration() {
            return duration;
        }

        public interface IViewHolderClicks {
            void onContentClick(LinearLayout caller, int position);
            boolean onContentLongClick(int position);
        }

    }

}