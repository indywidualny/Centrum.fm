package org.indywidualni.centrumfm.rest.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.activity.NewsableActivity;
import org.indywidualni.centrumfm.rest.model.Channel;
import org.indywidualni.centrumfm.util.ui.CustomLinkMovementMethod;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private static PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
    private static Context mContext;
    private List<Channel.Item> mDataset;
    private boolean shouldHide;

    private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
            MyApplication.getContextOfApplication());

    // provide a suitable constructor
    public NewsAdapter(List<Channel.Item> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_news,
                viewGroup, false);

        return new ViewHolder(v, new ViewHolder.ViewHolderClicks() {
            public void onExpand(TextView caller, int position) {
                // don't continue when all the news should be expanded
                if (preferences.getBoolean("show_all_news", false))
                    return;

                final RelativeLayout expandable = (RelativeLayout) v.findViewById(R.id.expandable);
                if (!expandable.isShown()) {
                    expandable.setVisibility(View.VISIBLE);
                    expandable.setAlpha(0.0f);
                    expandable.animate().alpha(1.0f);
                    mDataset.get(position).setExpanded(true);
                } else {
                    expandable.animate().alpha(0.0f);
                    shouldHide = true;
                    mDataset.get(position).setExpanded(false);
                }
                expandable.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (shouldHide) {
                            expandable.setVisibility(View.GONE);
                            shouldHide = false;
                        }
                    }
                });
            }

            public void onPlay(TextView caller, int position) {
                if (mContext instanceof NewsableActivity) {
                    ((NewsableActivity) mContext).playEnclosure(mDataset.get(position)
                            .getEnclosureUrl());
                }
            }

            public void onMore(TextView caller, int position) {
                if (mContext instanceof NewsableActivity) {
                    ((NewsableActivity) mContext).openCustomTab(mDataset.get(position).getLink());
                }
            }

            public void onShare(TextView caller, int position) {
                if (mContext instanceof NewsableActivity) {
                    ((NewsableActivity) mContext).shareTextUrl(mDataset.get(position).getLink(),
                            mDataset.get(position).getTitle());
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // get element from a dataset at this position and replace the contents of the view
        viewHolder.getTitle().setText(mDataset.get(position).getTitle());
        viewHolder.getTimeAgo().setText(prettyTime.format(mDataset.get(position).getDate()));
        viewHolder.getCategory().setText(mDataset.get(position).getCategory());

        viewHolder.getDescription().setText(Html.fromHtml(mDataset.get(position).getDescription()
                .replace("<strong>", "").replace("</strong>", "")
                .replaceAll("(.)(p)( ).*?(=)(\".*?\")(>)", "").replace("<p>", "")
                .replace("</p>", "<br /><br />").replaceFirst("&#187;.*", "")
                .replaceFirst("Czytaj dalej.*", "").trim()));

        // disable play button when there is nothing to play
        if (mDataset.get(position).getEnclosureUrl() == null) {
            viewHolder.getPlay().setEnabled(false);
            viewHolder.getPlay().setAlpha(0.4f);
        } else {
            viewHolder.getPlay().setEnabled(true);
            viewHolder.getPlay().setAlpha(1);
        }

        // don't continue when all the news should be expanded
        if (preferences.getBoolean("show_all_news", false)) {
            viewHolder.getExpandable().setVisibility(View.VISIBLE);
            return;
        }

        // expand items (the first one and recently expanded ones)
        if (position == 0 || mDataset.get(position).isExpanded())
            viewHolder.getExpandable().setVisibility(View.VISIBLE);
        else
            viewHolder.getExpandable().setVisibility(View.GONE);
    }

    // return the size of a dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView description;
        private final TextView timeAgo;
        private final TextView category;
        private final TextView play;
        private final TextView readMore;
        private final TextView share;
        private final RelativeLayout expandable;
        public ViewHolderClicks mListener;

        public ViewHolder(View v, ViewHolderClicks listener) {
            super(v);
            mListener = listener;

            title = (TextView) v.findViewById(R.id.title);
            description = (TextView) v.findViewById(R.id.description);
            timeAgo = (TextView) v.findViewById(R.id.timeAgo);
            category = (TextView) v.findViewById(R.id.category);
            play = (TextView) v.findViewById(R.id.itemPlay);
            readMore = (TextView) v.findViewById(R.id.itemReadMore);
            share = (TextView) v.findViewById(R.id.itemShare);
            expandable = (RelativeLayout) v.findViewById(R.id.expandable);

            description.setMovementMethod(CustomLinkMovementMethod.getInstance(mContext));

            title.setOnClickListener(this);
            play.setOnClickListener(this);
            readMore.setOnClickListener(this);
            share.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.title:
                    mListener.onExpand((TextView) v, getAdapterPosition());
                    break;
                case R.id.itemPlay:
                    mListener.onPlay((TextView) v, getAdapterPosition());
                    break;
                case R.id.itemReadMore:
                    mListener.onMore((TextView) v, getAdapterPosition());
                    break;
                case R.id.itemShare:
                    mListener.onShare((TextView) v, getAdapterPosition());
                    break;
            }
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getDescription() {
            return description;
        }

        public TextView getTimeAgo() {
            return timeAgo;
        }

        public TextView getCategory() {
            return category;
        }

        public TextView getPlay() {
            return play;
        }

        public RelativeLayout getExpandable() {
            return expandable;
        }

        public interface ViewHolderClicks {
            void onExpand(TextView caller, int position);
            void onPlay(TextView caller, int position);
            void onMore(TextView caller, int position);
            void onShare(TextView caller, int position);
        }
    }

}