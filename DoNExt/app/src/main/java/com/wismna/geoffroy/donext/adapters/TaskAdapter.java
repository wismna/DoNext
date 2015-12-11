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
import com.wismna.geoffroy.donext.fragments.TasksFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Task} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private final List<Task> mValues;
    private final OnListFragmentInteractionListener mListener;

    public TaskAdapter(List<Task> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(String.valueOf(holder.mItem.getId()));
        holder.mCycleView.setText(String.valueOf(holder.mItem.getCycle()));
        holder.mTitleView.setText(holder.mItem.getName());
        holder.mDescriptionView.setText(holder.mItem.getDescription());
        int priority = holder.mItem.getPriority();

        switch (priority)
        {
            case 0:
                holder.mTitleView.setTextColor(Color.LTGRAY);
                break;
            case 2:
                holder.mTitleView.setTypeface(holder.mTitleView.getTypeface(), Typeface.BOLD);
                break;
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public void add(Task item, int position) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        //int position = mValues.indexOf(item);
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void update(Task item, int position)
    {
        mValues.set(position, item);
        notifyItemChanged(position);
    }

    public Task getItem(int position) {
        return mValues.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mCycleView;
        public final TextView mTitleView;
        public final TextView mDescriptionView;
        public Task mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            mIdView = (TextView) view.findViewById(R.id.task_id);
            mCycleView = (TextView) view.findViewById(R.id.task_cycle);
            mTitleView = (TextView) view.findViewById(R.id.task_name);
            mDescriptionView = (TextView) view.findViewById(R.id.task_description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
