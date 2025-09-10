package com.wismna.geoffroy.donext.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.helpers.TaskListTouchHelper;

import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TaskList}.
 */

public class TaskListRecyclerViewAdapter extends RecyclerView.Adapter<TaskListRecyclerViewAdapter.TaskViewHolder>
    implements TaskListTouchHelper.TaskListTouchHelperAdapter {

    public interface TaskListRecyclerViewAdapterListener {
        void onEditTextLoseFocus(TaskList taskList);
        void onClickDeleteButton(int position, long id);
        void onItemMove(long fromTaskId, long toTaskId, int fromPosition, int toPosition);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private final List<TaskList> mValues;
    private final TaskListRecyclerViewAdapterListener mListener;

    public TaskListRecyclerViewAdapter(List<TaskList> items,
                                       TaskListRecyclerViewAdapterListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tasklist, parent, false);
        return new TaskViewHolder(view);
    }

    // Remove the message about overriding performClick, which requires subclassing ImageView
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final TaskViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTaskCountView.setText(String.valueOf(mValues.get(position).getTaskCount()));
        holder.mTaskNameView.setText(mValues.get(position).getName());

        holder.handleView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mListener.onStartDrag(holder);
            }
            return false;
        });

        // Handle inline name change
        holder.mTaskNameView.setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            String name = editText.getText().toString();

            if (!hasFocus && !holder.mItem.getName().matches(name)) {
                holder.mItem.setName(name);

                update(holder.mItem, holder.getBindingAdapterPosition());
                mListener.onEditTextLoseFocus(holder.mItem);
            }
        });

        // Handle click on delete button
        holder.mTaskDeleteButton.setOnClickListener(v -> {
            // Disable the OnFocusChanged listener as it is now pointless and harmful
            holder.mTaskNameView.setOnFocusChangeListener(null);

            //remove(position);
            mListener.onClickDeleteButton(holder.getBindingAdapterPosition(), holder.mItem.getId());
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void add(TaskList item, int position) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    private void update(TaskList item, int position) {
        mValues.set(position, item);
        notifyItemChanged(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        long fromTaskId = mValues.get(fromPosition).getId();
        long toTaskId = mValues.get(toPosition).getId();

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {

                Collections.swap(mValues, i, i + 1);
            }
        }
        else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mValues, i, i - 1);
            }
        }
        mListener.onItemMove(fromTaskId, toTaskId, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView handleView;
        final TextView mTaskCountView;
        final TextView mTaskNameView;
        final Button mTaskDeleteButton;
        TaskList mItem;

        TaskViewHolder(View view) {
            super(view);
            mView = view;
            handleView = itemView.findViewById(R.id.handle);
            mTaskCountView = view.findViewById(R.id.task_list_count);
            mTaskNameView = view.findViewById(R.id.task_list_name);
            mTaskDeleteButton = view.findViewById(R.id.task_list_delete);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mTaskNameView.getText() + "'";
        }
    }
}
