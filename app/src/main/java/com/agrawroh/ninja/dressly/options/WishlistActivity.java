package com.agrawroh.ninja.dressly.options;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agrawroh.ninja.dressly.R;
import com.agrawroh.ninja.dressly.model.Item;
import com.agrawroh.ninja.dressly.product.ItemDetailsActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recylerview_list);
        mContext = WishlistActivity.this;

        final RecyclerView recyclerView = findViewById(R.id.recyclerview);
        FirebaseDatabase.getInstance()
                .getReference().child("wishlist").child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Item> items = new ArrayList<>();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            items.add(postSnapshot.getValue(Item.class));
                        }
                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(recyclerView, items));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder> {

        private List<Item> wishlistItems;
        private RecyclerView mRecyclerView;

        static class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final SimpleDraweeView mImageView;
            final TextView wishlistName;
            final TextView wishlistDesc;
            final TextView wishlistPrice;
            final LinearLayout mLayoutItem;
            final ImageView mImageViewWishlist;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = view.findViewById(R.id.image_wishlist);
                wishlistName = view.findViewById(R.id.wishlist_name);
                wishlistDesc = view.findViewById(R.id.wishlist_desc);
                wishlistPrice = view.findViewById(R.id.wishlist_price);
                mLayoutItem = view.findViewById(R.id.layout_item_desc);
                mImageViewWishlist = view.findViewById(R.id.ic_wishlist);
            }
        }

        public SimpleStringRecyclerViewAdapter(RecyclerView recyclerView, List<Item> wishlistItems) {
            this.wishlistItems = wishlistItems;
            this.mRecyclerView = recyclerView;
        }

        @Override
        public WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wishlist_item, parent, false);
            return new WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (holder.mImageView.getController() != null) {
                holder.mImageView.getController().onDetach();
            }
            if (holder.mImageView.getTopLevelDrawable() != null) {
                holder.mImageView.getTopLevelDrawable().setCallback(null);
//                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
            }
        }

        @Override
        public void onBindViewHolder(final WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder holder, final int position) {
            final Uri uri = Uri.parse(wishlistItems.get(position).getItemImageURI().get(0));
            holder.mImageView.setImageURI(uri);
            holder.wishlistName.setText(wishlistItems.get(position).getItemName());
            holder.wishlistDesc.setText(wishlistItems.get(position).getItemDescription());
            holder.wishlistPrice.setText(wishlistItems.get(position).getItemPrice());
            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                    intent.putExtra("STRING_IMAGE_NAM", wishlistItems.get(position).getItemName());
                    intent.putExtra("STRING_IMAGE_DEC", wishlistItems.get(position).getItemDescription());
                    intent.putExtra("STRING_IMAGE_DET", wishlistItems.get(position).getItemDetails());
                    intent.putExtra("STRING_IMAGE_PRC", wishlistItems.get(position).getItemPrice());
                    intent.putExtra("STRING_IMAGE_RAT", wishlistItems.get(position).getItemRating());
                    intent.putExtra("STRING_IMAGE_LOC", wishlistItems.get(position).getItemLocation());
                    intent.putExtra("STRING_IMAGE_SIZ", wishlistItems.get(position).getItemSize());
                    intent.putExtra("STRING_IMAGE_URI", wishlistItems.get(position).getItemImageURI().get(0));
                    intent.putExtra("STRING_IMAGE_POS", position);
                    mContext.startActivity(intent);

                }
            });

            /* Set Click Action For WishList */
            holder.mImageViewWishlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance()
                            .getReference().child("wishlist")
                            .child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")))
                            .orderByChild("itemId").equalTo(wishlistItems.get(position).getItemId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        return;
                                    }
                                    for (DataSnapshot postDataSnapshot : dataSnapshot.getChildren()) {
                                        postDataSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    wishlistItems.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return wishlistItems.size();
        }
    }
}
