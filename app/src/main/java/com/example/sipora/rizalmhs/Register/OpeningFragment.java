package com.example.sipora.rizalmhs.Register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OpeningFragment extends Fragment {
    private final int layoutRes;

    public OpeningFragment(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutRes, container, false);
    }
}
