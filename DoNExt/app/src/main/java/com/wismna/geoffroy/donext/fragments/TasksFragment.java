package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.database.TaskDataAccess;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TasksFragment extends Fragment {

    private TaskDataAccess taskDataAccess;
    private static final String TASK_LIST_ID = "task_list_id";
    private long taskListId = -1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TasksFragment() {
    }

    @SuppressWarnings("unused")
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
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.task_list_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        taskDataAccess = new TaskDataAccess(view.getContext());
        taskDataAccess.open();

        // Set total cycles
        TextView totalCyclesView = (TextView) view.findViewById(R.id.total_task_cycles);
        totalCyclesView.setText(String.valueOf(taskDataAccess.getTotalCycles(taskListId)));

        // Set total count
        TextView totalTasksView = (TextView) view.findViewById(R.id.total_task_count);
        totalTasksView.setText(String.valueOf(taskDataAccess.getTaskCount(taskListId)));

        // Set RecyclerView Adapter
        TaskAdapter taskAdapter = new TaskAdapter(taskDataAccess.getAllTasks(taskListId), mListener);
        recyclerView.setAdapter(taskAdapter);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Task item);
    }
}
