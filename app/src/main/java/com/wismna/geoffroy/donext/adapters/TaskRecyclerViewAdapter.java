package com.wismna.geoffroy.donext.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;

import org.joda.time.LocalDate;

import java.util.List;


/**
 * {@link RecyclerView.Adapter} that can display a {@link Task}.
 */
public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.SimpleViewHolder> {

    private List<Task> mValues;
    private int viewType;
    private boolean mIsToday;

    public TaskRecyclerViewAdapter(List<Task> items, int viewType, boolean isToday) {
        mValues = items;
        mIsToday = isToday;
        this.viewType = viewType;
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType)
        {
            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_task_detailed, parent, false);
                return new DetailedViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_task_simple, parent, false);
                return new SimpleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final SimpleViewHolder holder, int position) {
        // Set basic information
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(holder.mItem.getId()));
        holder.mCycleView.setText(String.valueOf(holder.mItem.getCycle()));
        holder.mTitleView.setText(holder.mItem.getName());
        // Set optional description
        if (holder instanceof DetailedViewHolder)
            ((DetailedViewHolder)holder).mDescriptionView.setText(holder.mItem.getDescription());
        // Set task rendering
        holder.mTitleView.setTypeface(Typeface.DEFAULT);
        holder.mTitleView.setTextColor(Color.BLACK);

        // Additional information will not be displayed in Today view
        if (mIsToday) return;
        // Set alarm if past due date
        LocalDate dueDate = holder.mItem.getDueDate();
        if (holder.mItem.getDone() == 1)
            holder.mIconView.setImageResource(R.drawable.ic_check_ligth);
        else if (holder.mItem.getDeleted() == 1)
            holder.mIconView.setImageResource(R.drawable.ic_close_light);
        else if(dueDate != null && dueDate.isBefore(LocalDate.now()))
            holder.mIconView.setImageResource(R.drawable.ic_access_alarm);
        int priority = holder.mItem.getPriority();

        // Set priority
        switch (priority)
        {
            case 0:
                holder.mTitleView.setTextColor(Color.LTGRAY);
                break;
            case 2:
                holder.mTitleView.setTypeface(holder.mTitleView.getTypeface(), Typeface.BOLD);
                break;
            default:
                // No special styles to apply
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

    public void setItems(List<Task> tasks) {
        this.mValues = tasks;
        notifyDataSetChanged();
    }

    public Task getItem(int position) {
        return mValues.get(position);
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final ImageView mIconView;
        final TextView mCycleView;
        final TextView mTitleView;
        Task mItem;

        SimpleViewHolder(View view) {
            super(view);
            mView = view;

            mIdView = view.findViewById(R.id.task_id);
            mIconView = view.findViewById(R.id.task_icon);
            mCycleView = view.findViewById(R.id.task_cycle);
            mTitleView = view.findViewById(R.id.task_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }

    private class DetailedViewHolder extends SimpleViewHolder {
        private final TextView mDescriptionView;

        private DetailedViewHolder(View view) {
            super(view);
            mDescriptionView = view.findViewById(R.id.task_description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
