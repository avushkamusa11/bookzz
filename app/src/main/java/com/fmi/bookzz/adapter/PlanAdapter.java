package com.fmi.bookzz.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.Plan;
import com.fmi.bookzz.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class PlanAdapter extends
    RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

        Context context;
        private List<Plan> plans;
        private MainActivity activity;
        public PlanAdapter(Activity activity, Context context){
            this.activity = (MainActivity) activity;
            this.context=context;
            plans = new ArrayList<>();
        }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_list_row,parent,false);
       return new PlanViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
            Plan plan = plans.get(position);
            holder.bind(plan);
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }
    public void changePlans(List<Plan> currentPlans) {

            if(plans.size() != 0){
                plans.clear();
            }
            plans.addAll(currentPlans);

        notifyDataSetChanged();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder{

            private TextView planBookTitleTV;
            private TextView planStartTimeTV;

            public PlanViewHolder(@NonNull View itemView, Context context) {
                super(itemView);

                planBookTitleTV = itemView.findViewById(R.id.planBookTitleTV);
                planStartTimeTV = itemView.findViewById(R.id.planStartTimeTV);
            }
            public void bind(Plan plan){
                planStartTimeTV.setText(plan.getStartTime());
                planBookTitleTV.setText(plan.getBookTitle());
            }
        }


    }
