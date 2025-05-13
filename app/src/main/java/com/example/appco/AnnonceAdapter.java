package com.example.appco;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private static final String TAG = "AnnonceAdapter";
    private List<Annonce> annonces;
    private Context context;

    public AnnonceAdapter(List<Annonce> annonces) {
        this.annonces = annonces;
    }

    @NonNull
    @Override
    public AnnonceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_annonce, parent, false);
        return new AnnonceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnonceViewHolder holder, int position) {
        Annonce annonce = annonces.get(position);
        holder.titre.setText(annonce.getTitre());
        holder.description.setText(annonce.getDescription());
        holder.ratingBar.setRating(annonce.getRating());

        // Display owner name if available
        if (holder.ownerName != null) {
            String ownerNameText = annonce.getOwnerName();
            if (ownerNameText != null && !ownerNameText.isEmpty()) {
                holder.ownerName.setText("Par: " + ownerNameText);
                holder.ownerName.setVisibility(View.VISIBLE);
                Log.d(TAG, "Owner name displayed: " + ownerNameText);
            } else {
                holder.ownerName.setVisibility(View.GONE);
                Log.d(TAG, "Owner name not available for annonce: " + annonce.getId());
            }
        }

        // Load image based on what's available
        if (annonce.getImageUrl() != null && !annonce.getImageUrl().isEmpty()) {
            // Use Glide to load image from URL
            Glide.with(context)
                    .load(annonce.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.image);
        } else if (annonce.getImageBase64() != null && !annonce.getImageBase64().isEmpty()) {
            // Decode Base64 image
            try {
                byte[] decodedString = Base64.decode(annonce.getImageBase64(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.image.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                // Use default image on error
                Log.e(TAG, "Error decoding base64 image: " + e.getMessage());
                holder.image.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            // Use default image if no image available
            holder.image.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnnonceDetailsActivity.class);
            intent.putExtra("annonceId", annonce.getId());
            // Pass owner information too if needed in details
            intent.putExtra("ownerName", annonce.getOwnerName());
            intent.putExtra("ownerEmail", annonce.getOwnerEmail());
            intent.putExtra("ownerId", annonce.getOwnerId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return annonces.size();
    }

    public void updateAnnonces(List<Annonce> newAnnonces) {
        this.annonces = newAnnonces;
        notifyDataSetChanged();
    }

    static class AnnonceViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView titre, description, ownerName;
        RatingBar ratingBar;

        public AnnonceViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageAnnonce);
            titre = itemView.findViewById(R.id.titreAnnonce);
            description = itemView.findViewById(R.id.descriptionAnnonce);
            ratingBar = itemView.findViewById(R.id.ratingAnnonce);
            // Find ownerName TextView if it exists in your layout
            ownerName = itemView.findViewById(R.id.ownerNameText);
        }
    }
}