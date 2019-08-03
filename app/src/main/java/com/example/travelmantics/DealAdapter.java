package com.example.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    ArrayList<HolidayDeal> deals;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    private ImageView rvImgIcon;

    public DealAdapter() {
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        deals = FirebaseUtil.holidayDeals;

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HolidayDeal hd = dataSnapshot.getValue(HolidayDeal.class);
                hd.setId(dataSnapshot.getKey());
                deals.add(hd);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.rv_row, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        HolidayDeal deal = deals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{

        TextView rvTvTitle;
        TextView rvTvPrice;
        TextView rvTvDescription;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            rvTvTitle = itemView.findViewById(R.id.rvTvTitle);
            rvTvPrice = itemView.findViewById(R.id.rvTvPrice);
            rvTvDescription = itemView.findViewById(R.id.rvTvDescription);
            rvImgIcon= itemView.findViewById(R.id.rvimgIcon);
            itemView.setOnClickListener(this);
        }

        public void bind(HolidayDeal deal) {
            rvTvTitle.setText(deal.getTitle());
            rvTvPrice.setText(deal.getPrice());
            rvTvDescription.setText(deal.getDescription());
            showImage(deal.getImageUrl());

        }

        @Override
        public void onClick(View v) {
           int position = getAdapterPosition();
           HolidayDeal selectedDeal = deals.get(position);
           Intent intent = new Intent(v.getContext(), AdminActivity.class);
           intent.putExtra("Deal", selectedDeal);
           v.getContext().startActivity(intent);
        }

        private void showImage(String url) {
            if(url != null && url.isEmpty()==false) {
                Picasso.get()
                        .load(url)
                        .resize(160, 160)
                        .centerCrop()
                        .into(rvImgIcon);
            }
        }
    }
}
