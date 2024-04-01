package com.example.artemis;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    public int DefaultPosition;

    public HourAdapter(int DefaultPosition){
        this.DefaultPosition = DefaultPosition;
    }
    // Phương thức setter để thiết lập vị trí mặc định

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.hour_item, parent, false);
        HourViewHolder hourViewHolder = new HourViewHolder(itemView);
        return hourViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {

        holder.bindHour(position%24);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // Số giờ từ 00 đến 23
    }

    public static class HourViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewHour;

        public HourViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHour = itemView.findViewById(R.id.textViewHour);
        }

        public void bindHour(int hour) {
            textViewHour.setText(String.format("%02d", hour)); // Định dạng số giờ thành chuỗi với hai chữ số

        }
    }
}
