package com.example.sipora.rizalmhs.Register;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sipora.R;

public class OpeningAdapter extends RecyclerView.Adapter<OpeningAdapter.ViewHolder> {

    private final int[] layouts = {
            R.layout.activity_opening01,
            R.layout.activity_opening02,
            R.layout.activity_opening03
    };

    private final OpeningActivity activity;

    public OpeningAdapter(OpeningActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(layouts[viewType], parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View itemView = holder.itemView;

        // 🔹 Tombol "Create an account" — pindah ke RegisterActivity
        View btnCreate = itemView.findViewById(R.id.btn_create_account);
        View btnCreateAlt = itemView.findViewById(R.id.btn_create_account);

        View btnToUse = (btnCreate != null) ? btnCreate : btnCreateAlt;
        if (btnToUse != null) {
            btnToUse.setOnClickListener(v -> {
                Intent intent = new Intent(activity, RegisterActivity.class);
                activity.startActivity(intent);
            });
        }

        // 🔹 Teks "Log in" — pindah ke LoginActivity
        View tvLogin = itemView.findViewById(R.id.tv_login);
        View tvLoginAlt = itemView.findViewById(R.id.tvLogin); // jika ID berbeda

        View tvToUse = (tvLogin != null) ? tvLogin : tvLoginAlt;
        if (tvToUse != null) {
            tvToUse.setOnClickListener(v -> {
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
