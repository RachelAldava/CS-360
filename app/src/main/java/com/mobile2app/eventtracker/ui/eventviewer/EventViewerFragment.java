package com.mobile2app.eventtracker.ui.eventviewer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobile2app.eventtracker.DatabaseHandler;
import com.mobile2app.eventtracker.R;
import com.mobile2app.eventtracker.User;
import com.mobile2app.eventtracker.databinding.FragmentEventviewerBinding;
import com.mobile2app.eventtracker.databinding.FragmentLoginBinding;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A fragment representing a list of Items.
 */
public class EventViewerFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private FragmentEventviewerBinding binding;

    public EventViewerFragment() {
    }

    public static EventViewerFragment newInstance(int columnCount) {
        EventViewerFragment fragment = new EventViewerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEventviewerBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.list;
        FloatingActionButton fab = binding.addNew;
        View root = binding.getRoot();
        Context context = root.getContext();
        EventViewerAdapter adapter;

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        adapter = new EventViewerAdapter(getContext());
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (User.userLoggedIn()) adapter.addEntry(null);
                else Toast.makeText(view.getContext(), "Please log in first", Toast.LENGTH_LONG).show();
            }
        });

        return root;
    }
}