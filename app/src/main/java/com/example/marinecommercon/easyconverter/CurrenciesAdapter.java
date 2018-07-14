package com.example.marinecommercon.easyconverter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CurrenciesAdapter extends RecyclerView.Adapter<CurrenciesAdapter.ViewHolder> {

    private View view;
    private Activity activity;
    List<String> currencies;
    CurrenciesDialogListener listener;
    private int highlightedPosition = 0;

    public CurrenciesAdapter(Activity activity, List<String> currencies) {
        this.activity = activity;
        this.currencies = currencies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_currency, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.name.setText(currencies.get(i));
        if (highlightedPosition == i) {
            viewHolder.name.setTextColor(activity.getResources().getColor((R.color.colorAccent)));
        }
        else {
            viewHolder.name.setTextColor(activity.getResources().getColor((R.color.dark_gray)));
        }
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    public void highlightItem(int index) {
        highlightedPosition = index;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;

        ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            name = v.findViewById(R.id.name);
        }

        @Override
        public void onClick(View view) {
            String codeName = currencies.get(getAdapterPosition());
            listener.didSelect(codeName);
        }
    }
}
