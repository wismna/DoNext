package com.wismna.geoffroy.donext.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;

import java.util.List;


/**
 * {@link RecyclerView.Adapter} that can display a {@link Task}.
 */
public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.SimpleViewHolder> {

    private final List<Task> mValues;
    private int viewType;

    public TaskRecyclerViewAdapter(List<Task> items, int viewType) {
        mValues = items;
        this.viewType = viewType;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType)
        {
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_task_simple, parent, false);
                return new SimpleViewHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_task_detailed, parent, false);
                return new DetailedViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(holder.mItem.getId()));
        holder.mCycleView.setText(String.valueOf(holder.mItem.getCycle()));
        holder.mTitleView.setText(holder.mItem.getName());
        if (holder instanceof DetailedViewHolder)
            ((DetailedViewHolder)holder).mDescriptionView.setText(holder.mItem.getDescription());
        int priority = holder.mItem.getPriority();

        // Reset task rendering
        holder.mTitleView.setTypeface(Typeface.DEFAULT);
        holder.mTitleView.setTextColor(Color.BLACK);

        switch (priority)
        {
            case 0:
                holder.mTitleView.setTextColor(Color.LTGRAY);
                break;
            case 2:
                holder.mTitleView.setTypeface(holder.mTitleView.getTypeface(), Typeface.BOLD);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    public void add(Task item, int position) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void update(Task item, int position) {
        mValues.set(position, item);
        notifyItemChanged(position);
    }

    public int getCycleCount() {
        int count = 0;
        for (Task task: mValues) {
            count += task.getCycle();
        }
        return count;
    }

    public Task getItem(int position) {
        return mValues.get(position);
    }


    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mCycleView;
        public final TextView mTitleView;
        public Task mItem;

        public SimpleViewHolder(View view) {
            super(view);
            mView = view;

            mIdView = (TextView) view.findViewById(R.id.task_id);
            mCycleView = (TextView) view.findViewById(R.id.task_cycle);
            mTitleView = (TextView) view.findViewById(R.id.task_name);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    public class DetailedViewHolder extends SimpleViewHolder {
        public final TextView mDescriptionView;

        public DetailedViewHolder(View view) {
            super(view);
            mDescriptionView = (TextView) view.findViewById(R.id.task_description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
