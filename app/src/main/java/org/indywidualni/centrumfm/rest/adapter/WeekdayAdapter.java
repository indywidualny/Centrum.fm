package org.indywidualni.centrumfm.rest.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.util.PrettyLength;
import org.indywidualni.centrumfm.util.database.AsyncWrapper;
import org.indywidualni.centrumfm.util.database.DataSource;

import java.util.Calendar;
import java.util.List;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.ViewHolder> {

    private final List<Schedule.Event> mDataset;
    private final int day;

    // provide a suitable constructor
    public WeekdayAdapter(List<Schedule.Event> mDataset, int day) {
        this.mDataset = mDataset;
        this.day = day;
    }

    // create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_schedule,
                viewGroup, false);

        return new ViewHolder(v, new ViewClicks());
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // get element from a dataset at this position and replace the contents of the view
        viewHolder.getStartTime().setText(mDataset.get(position).getStartDate()
                .replaceFirst(".{3}$", ""));
        viewHolder.getBelowTitle().setText(PrettyLength.get(mDataset.get(position)
                .getEventLength() / 3600));
        viewHolder.getTitle().setText(mDataset.get(position).getName());

        // select item which is on air now
        String[] time = mDataset.get(position).getStartDate().split(":");
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        start.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        start.set(Calendar.SECOND, Integer.parseInt(time[2]));
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.SECOND, mDataset.get(position).getEventLength());

        if ((day == (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1))
                && Calendar.getInstance().after(start) && Calendar.getInstance().before(end))
            viewHolder.getLinearLayout().setSelected(true);
        else
            viewHolder.getLinearLayout().setSelected(false);

        if (mDataset.get(position).isFavourite())
            viewHolder.getFavourite().setImageResource(R.drawable.ic_favorite_black_24dp);
        else
            viewHolder.getFavourite().setImageResource(R.drawable.ic_favorite_border_black_24dp);
    }

    // return the size of a dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView startTime;
        private final TextView title;
        private final TextView belowTitle;
        private final ImageView favourite;
        private final LinearLayout linearLayout;
        public final IViewHolderClicks mListener;

        public ViewHolder(View v, IViewHolderClicks listener) {
            super(v);
            mListener = listener;

            startTime = (TextView) v.findViewById(R.id.startTime);
            title = (TextView) v.findViewById(R.id.title);
            belowTitle = (TextView) v.findViewById(R.id.belowTitle);
            linearLayout = (LinearLayout) v.findViewById(R.id.element);
            favourite = (ImageView) v.findViewById(R.id.favourite);

            favourite.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView)
                mListener.onFavourite((ImageView) v, getAdapterPosition());
        }

        public TextView getStartTime() {
            return startTime;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getBelowTitle() {
            return belowTitle;
        }

        public LinearLayout getLinearLayout() {
            return linearLayout;
        }

        public ImageView getFavourite() {
            return favourite;
        }

        public interface IViewHolderClicks {
            void onFavourite(ImageView caller, int position);
        }

    }

    private class ViewClicks implements ViewHolder.IViewHolderClicks {
        public void onFavourite(ImageView caller, int position) {
            ImageView favourite = (ImageView) caller.findViewById(R.id.favourite);
            if (DataSource.getInstance().isEventFavourite(mDataset.get(position).getId())) {
                Log.v("onFavourite", "Item " + mDataset.get(position).getId() + " removed");
                mDataset.get(position).setFavourite(false);
                AsyncWrapper.removeFavourite(mDataset.get(position));
                favourite.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            } else {
                Log.v("onFavourite", "Item " + mDataset.get(position).getId() + " inserted");
                mDataset.get(position).setFavourite(true);
                AsyncWrapper.insertFavourite(mDataset.get(position));
                favourite.setImageResource(R.drawable.ic_favorite_black_24dp);
            }
        }
    }

}
