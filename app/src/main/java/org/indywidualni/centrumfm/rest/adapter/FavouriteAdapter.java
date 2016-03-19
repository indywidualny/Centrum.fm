package org.indywidualni.centrumfm.rest.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.util.PrettyLength;
import org.indywidualni.centrumfm.util.database.AsyncWrapper;

import java.util.ArrayList;
import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // The items to display in your RecyclerView
    private List<Object> items;

    private final int EVENT = 0, LABEL = 1;

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavouriteAdapter(List<Object> items) {
        this.items = items;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Schedule.Event)
            return EVENT;
        return LABEL;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        if (viewType == EVENT) {
            View v1 = inflater.inflate(R.layout.list_item_schedule, viewGroup, false);
            return new WeekdayAdapter.ViewHolder(v1, new ViewClicks());
        }

        View v = inflater.inflate(R.layout.list_item_label, viewGroup, false);
        return new LabelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case EVENT:
                WeekdayAdapter.ViewHolder vh1 = (WeekdayAdapter.ViewHolder) viewHolder;
                configureViewHolder1(vh1, position);
                break;
            case LABEL:
                LabelViewHolder vh2 = (LabelViewHolder) viewHolder;
                configureViewHolder2(vh2, position);
                break;
        }
    }

    private class ViewClicks implements WeekdayAdapter.ViewHolder.IViewHolderClicks {
        public void onFavourite(ImageView caller, int position) {
            // remove from a database
            AsyncWrapper.removeFavourite((Schedule.Event) items.get(position));

            int id = ((Schedule.Event) items.get(position)).getId();

            // remove an unfavourited item from all the labels
            List<Object> toRemove = new ArrayList<>();
            for (Object o : items) {
                if (o instanceof Schedule.Event && ((Schedule.Event) o).getId() == id)
                    toRemove.add(o);
            }
            items.removeAll(toRemove);

            // remove all the labels without items
            toRemove = new ArrayList<>();
            for (int i = 0; i < items.size() - 1; ++i) {
                if (items.get(i) instanceof String && items.get(i + 1) instanceof String)
                    toRemove.add(items.get(i));
            }
            if (items.get(items.size() - 1) instanceof String)
                toRemove.add(items.get(items.size() - 1));
            items.removeAll(toRemove);

            // notify the adapter and delete an event from a database
            notifyDataSetChanged();
        }
    }

    private void configureViewHolder1(WeekdayAdapter.ViewHolder vh1, int position) {
        Schedule.Event event = (Schedule.Event) items.get(position);
        vh1.getStartTime().setText(event.getStartDate().replaceFirst(".{3}$", ""));
        vh1.getTitle().setText(event.getName());
        vh1.getBelowTitle().setText(PrettyLength.get(event.getEventLength() / 3600));
        vh1.getFavourite().setImageResource(R.drawable.ic_favorite_black_24dp);
    }

    private void configureViewHolder2(LabelViewHolder vh2, int position) {
        vh2.getLabel().setText((String) items.get(position));
    }

    public static class LabelViewHolder extends RecyclerView.ViewHolder {
        
        private TextView label;

        public TextView getLabel() {
            return label;
        }

        public LabelViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.label);
        }
        
    }

}