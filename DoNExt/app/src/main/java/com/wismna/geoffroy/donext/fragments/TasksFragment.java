package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wismna.geoffroy.donext.ItemTouchHelpers.TaskTouchHelper;
import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.activities.MainActivity;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.listeners.RecyclerItemClickListener;
import com.wismna.geoffroy.donext.widgets.NoScrollingLayoutManager;

/**
 * A fragment representing a list of Items.
 */
public class TasksFragment extends Fragment {
    private static final String TASK_LIST_ID = "task_list_id";
    private long taskListId = -1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TasksFragment() {
    }

    public static TasksFragment newInstance(long taskListId) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putLong(TASK_LIST_ID, taskListId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            taskListId = getArguments().getLong(TASK_LIST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        final Context context = view.getContext();

        // Set the Recycler view
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.task_list_view);
        recyclerView.setLayoutManager(new NoScrollingLayoutManager(context));

        TaskDataAccess taskDataAccess = new TaskDataAccess(view.getContext());
        taskDataAccess.open();

        // Set total cycles
        TextView totalCyclesView = (TextView) view.findViewById(R.id.total_task_cycles);
        totalCyclesView.setText(String.valueOf(taskDataAccess.getTotalCycles(taskListId) + " cycles"));

        // Set total count
        TextView totalTasksView = (TextView) view.findViewById(R.id.total_task_count);
        totalTasksView.setText(String.valueOf(taskDataAccess.getTaskCount(taskListId) + " tasks"));

        // Set RecyclerView Adapter
        final TaskRecyclerViewAdapter taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(taskDataAccess.getAllTasks(taskListId));
        recyclerView.setAdapter(taskRecyclerViewAdapter);

        taskDataAccess.close();

        // Set ItemTouch helper in RecyclerView to handle swipe move on elements
        ItemTouchHelper.Callback callback = new TaskTouchHelper(
                taskRecyclerViewAdapter, taskDataAccess, getFragmentManager(), recyclerView);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        // Implements touch listener to add click detection
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Bundle args = new Bundle();
                        args.putInt("position", position);

                        // Set current tab value to new task dialog
                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        args.putInt("list", viewPager.getCurrentItem());

                        FragmentManager manager = getFragmentManager();
                        TaskDialogFragment taskDialogFragment = TaskDialogFragment.newInstance(/*taskRecyclerViewAdapter, recyclerView,*/
                                taskRecyclerViewAdapter.getItem(position),
                                ((MainActivity.SectionsPagerAdapter)viewPager.getAdapter()).getAllItems());

                        taskDialogFragment.setArguments(args);
                        taskDialogFragment.show(manager, "Edit task");
                    }
                })
        );
        return view;
    }
}
