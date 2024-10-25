package com.mobile2app.eventtracker.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mobile2app.eventtracker.NotificationsManager;
import com.mobile2app.eventtracker.R;
import com.mobile2app.eventtracker.databinding.FragmentNotificationBinding;

public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;
    Button testPush;
    MaterialSwitch toggle;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        testPush = root.findViewById(R.id.TestPush);
        toggle = root.findViewById(R.id.toggle_allow_push_notifications);
        toggle.setActivated(NotificationsManager.canPUSH(getContext()));

        testPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationsManager instance = NotificationsManager.getInstance();
                instance.sendPush();
            }
        });

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    NotificationsManager.saveUserPermPUSH(toggle.isChecked());
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}