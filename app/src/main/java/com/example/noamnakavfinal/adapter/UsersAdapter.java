package com.example.noamnakavfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.model.User;

import java.util.List;

    public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

        private List<User> users;

        public UsersAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_alluserpage, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvName.setText(user.getFname() + " " + user.getLname());
            holder.tvEmail.setText(user.getEmail());
            holder.tvPhone.setText(user.getPhone());
            holder.tvAdmin.setText(user.isAdmin() ? "Admin" : "User");
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static class UserViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvEmail, tvPhone, tvAdmin;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.etFname);
                tvEmail = itemView.findViewById(R.id.etemail);
                tvPhone = itemView.findViewById(R.id.etphone);
                tvAdmin = itemView.findViewById(R.id.tvAdmin);
            }
        }
    }

