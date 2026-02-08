package com.example.noamnakavfinal.adapter;



import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.util.ImageUtil;

import java.util.List;

    public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.CarViewHolder> {

        public interface OnCarClickListener {
            void onClick(Car car);
        }

        Context context;
        List<Car> cars;
        OnCarClickListener listener;

        public CarsAdapter(Context context, List<Car> cars, OnCarClickListener listener) {
            this.context = context;
            this.cars = cars;
            this.listener = listener;
        }

        public void updateList(List<Car> newCars) {
            cars = newCars;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.car_item, parent, false);
            return new CarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
            Car car = cars.get(position);

            holder.title.setText(car.getBrand() + " " + car.getModel());
            holder.price.setText("₪ " + car.getPrice());
            holder.year.setText("שנה: " + car.getYear());

            if (car.getImage64() != null) {
                Bitmap bitmap = ImageUtil.convertFrom64base(car.getImage64());
                holder.img.setImageBitmap(bitmap);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(car));
        }

        @Override
        public int getItemCount() {
            return cars.size();
        }

        static class CarViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView title, price, year;

            public CarViewHolder(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.imgCar);
                title = itemView.findViewById(R.id.tvTitle);
                price = itemView.findViewById(R.id.tvPrice);
                year = itemView.findViewById(R.id.tvYear);
            }
        }
    }

