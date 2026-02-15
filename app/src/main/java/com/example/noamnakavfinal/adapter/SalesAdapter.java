package com.example.noamnakavfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.model.Sale;
import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SaleViewHolder> {
    private List<Sale> salesList;

    public SalesAdapter(List<Sale> salesList) {
        this.salesList = salesList;
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Sale sale = salesList.get(position);
        holder.text1.setText(sale.getCar().getBrand() + " " + sale.getCar().getModel());
        holder.text2.setText("תאריך: " + sale.getDate() + " | מחיר: ₪" + sale.getPrice());
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}