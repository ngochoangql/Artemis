package com.example.artemis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<JSONObject> friendList;

    public FriendAdapter(List<JSONObject> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject friend = friendList.get(position);
        try {
            String friendName = friend.getString("friend_name");
            String friendUUID = friend.getString("friend_uuid");
            holder.friendNameTextView.setText(friendName);
            holder.friendUUIDTextView.setText(friendUUID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView friendNameTextView,friendUUIDTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friendName);
            friendUUIDTextView = itemView.findViewById(R.id.friendUUID);
        }
    }
}
