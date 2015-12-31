package com.wismna.geoffroy.donext.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wismna.geoffroy.donext.ItemTouchHelpers.TaskListTouchHelper;
import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;

import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TaskList}.
 */

// TODO: implement inline edit
public class TaskListRecyclerViewAdapter extends RecyclerView.Adapter<TaskListRecyclerViewAdapter.ViewHolder>
    implements TaskListTouchHelper.TaskListTouchHelperAdapter {

    private final List<TaskList> mValues;
    private Context mContext;

    public TaskListRecyclerViewAdapter(List<TaskList> items, Context context) {
        mValues = items;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tasklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mTaskCountView.setText(String.valueOf(mValues.get(position).getTaskCount()));
        holder.mTaskNameView.setText(mValues.get(position).getName());

        // Handle inline name change
        holder.mTaskNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    EditText editText = (EditText) v;
                    holder.mItem.setName(editText.getText().toString());

                    TaskListDataAccess taskListDataAccess = new TaskListDataAccess(mContext);
                    taskListDataAccess.open();

                    update(holder.mItem, position);
                    taskListDataAccess.updateName(holder.mItem.getId(), holder.mItem.getName());

                    taskListDataAccess.close();
                }
            }
        });

        // Handle click on delete button
        // TODO: find a way to call TaskListsFragment.toggleVisibleCreateNewTaskListLayout
        holder.mTaskDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskListDataAccess taskListDataAccess = new TaskListDataAccess(mContext);
                taskListDataAccess.open();

                taskListDataAccess.deleteTaskList(holder.mItem.getId());
                remove(position);

                taskListDataAccess.close();
            }
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

    public void update(TaskList item, int position) {
        mValues.set(position, item);
        notifyItemChanged(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        TaskListDataAccess taskListDataAccess = new TaskListDataAccess(mContext);
        taskListDataAccess.open();
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

        taskListDataAccess.updateOrder(fromTaskId, toPosition);
        taskListDataAccess.updateOrder(toTaskId, fromPosition);
        // Update the adapter on the fly
        notifyItemMoved(fromPosition, toPosition);

        taskListDataAccess.close();
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTaskCountView;
        public final TextView mTaskNameView;
        public final Button mTaskDeleteButton;
        public TaskList mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTaskCountView = (TextView) view.findViewById(R.id.task_list_count);
            mTaskNameView = (TextView) view.findViewById(R.id.task_list_name);
            mTaskDeleteButton = (Button) view.findViewById(R.id.task_list_delete);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTaskNameView.getText() + "'";
        }
    }
}
