/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agrawroh.ninja.dressly.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawroh.ninja.dressly.R;
import com.agrawroh.ninja.dressly.model.Item;
import com.agrawroh.ninja.dressly.product.ItemDetailsActivity;
import com.agrawroh.ninja.dressly.startup.MainActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ImageListFragment extends Fragment {

    public static final String STRING_IMAGE_URI = "ImageUri";
    public static final String STRING_IMAGE_POSITION = "ImagePosition";
    private static MainActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) inflater.inflate(R.layout.layout_recylerview_list, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(final RecyclerView recyclerView) {
      /*  if (ImageListFragment.this.getArguments().getInt("type") == 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        } else if (ImageListFragment.this.getArguments().getInt("type") == 2) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        } else {
            GridLayoutManager layoutManager = new GridLayoutManager(recyclerView.getContext(), 3);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        }*/

        FirebaseDatabase.getInstance()
                .getReference().child("items").child(String.valueOf(ImageListFragment.this.getArguments().getInt("type")))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Item> items = new ArrayList<>();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            items.add(postSnapshot.getValue(Item.class));
                        }
                        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(recyclerView, items));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private List<Item> mValues;
        private RecyclerView mRecyclerView;

        static class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final SimpleDraweeView mImageView;
            final TextView mNameView;
            final TextView mDescView;
            final TextView mPriceView;
            final LinearLayout mLayoutItem;
            final ImageView mImageViewWishlist;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = view.findViewById(R.id.image1);
                mNameView = view.findViewById(R.id.list_item_name);
                mDescView = view.findViewById(R.id.list_item_desc);
                mPriceView = view.findViewById(R.id.list_item_price);
                mLayoutItem = view.findViewById(R.id.layout_item);
                mImageViewWishlist = view.findViewById(R.id.ic_wishlist);
            }
        }

        SimpleStringRecyclerViewAdapter(RecyclerView recyclerView, List<Item> items) {
            mValues = items;
            mRecyclerView = recyclerView;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
           /* FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) holder.mImageView.getLayoutParams();
            if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
                layoutParams.height = 200;
            } else if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                layoutParams.height = 600;
            } else {
                layoutParams.height = 800;
            }*/
            final Uri uri = Uri.parse(mValues.get(position).getItemImageURI().get(0));
            holder.mImageView.setImageURI(uri);
            holder.mNameView.setText(mValues.get(position).getItemName());
            holder.mDescView.setText(mValues.get(position).getItemDescription());
            holder.mPriceView.setText(mValues.get(position).getItemPrice());
            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, ItemDetailsActivity.class);
                    intent.putExtra("STRING_IMAGE_NAM", mValues.get(position).getItemName());
                    intent.putExtra("STRING_IMAGE_DEC", mValues.get(position).getItemDescription());
                    intent.putExtra("STRING_IMAGE_DET", mValues.get(position).getItemDetails());
                    intent.putExtra("STRING_IMAGE_PRC", mValues.get(position).getItemPrice());
                    intent.putExtra("STRING_IMAGE_RAT", mValues.get(position).getItemRating());
                    intent.putExtra("STRING_IMAGE_LOC", mValues.get(position).getItemLocation());
                    intent.putExtra("STRING_IMAGE_SIZ", mValues.get(position).getItemSize());
                    intent.putExtra("STRING_IMAGE_URI", mValues.get(position).getItemImageURI().get(0));
                    intent.putExtra("STRING_IMAGE_POS", position);
                    mActivity.startActivity(intent);

                }
            });

            /* WishList Click Action */
            holder.mImageViewWishlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance()
                            .getReference().child("wishlist")
                            .child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")))
                            .push().setValue(mValues.get(position));
                    holder.mImageViewWishlist.setImageResource(R.drawable.ic_favorite_black_18dp);
                    notifyDataSetChanged();
                    Toast.makeText(mActivity, "Item Added To WishList!", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}
