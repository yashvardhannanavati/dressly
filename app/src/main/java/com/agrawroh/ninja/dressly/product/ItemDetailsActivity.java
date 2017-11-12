package com.agrawroh.ninja.dressly.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawroh.ninja.dressly.R;
import com.agrawroh.ninja.dressly.fragments.ViewPagerActivity;
import com.agrawroh.ninja.dressly.model.Item;
import com.agrawroh.ninja.dressly.notification.NotificationCountSetClass;
import com.agrawroh.ninja.dressly.options.CartListActivity;
import com.agrawroh.ninja.dressly.startup.MainActivity;
import com.agrawroh.ninja.dressly.utility.ImageUrlUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemDetailsActivity extends AppCompatActivity {
    private int imagePosition;
    private String stringImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        SimpleDraweeView mImageView = findViewById(R.id.image1);
        TextView txtViewName = findViewById(R.id.list_item_details_name);
        TextView txtViewPrice = findViewById(R.id.list_item_details_price);
        TextView txtViewLocation = findViewById(R.id.list_item_details_location);
        TextView txtViewDetails = findViewById(R.id.list_item_details_details);
        TextView txtViewRatings = findViewById(R.id.text_ratings);
        TextView textViewAddToCart = findViewById(R.id.text_action_bottom1);
        TextView textViewBuyNow = findViewById(R.id.text_action_bottom2);

        //Getting image uri from previous screen
        if (getIntent() != null) {
            stringImageUri = getIntent().getStringExtra("STRING_IMAGE_URI");
            imagePosition = getIntent().getIntExtra("STRING_IMAGE_POS", 0);

            txtViewName.setText(getIntent().getExtras().getString("STRING_IMAGE_NAM"));
            txtViewPrice.setText(getIntent().getExtras().getString("STRING_IMAGE_PRC"));
            txtViewLocation.setText(getIntent().getExtras().getString("STRING_IMAGE_LOC"));
            txtViewDetails.setText(getIntent().getExtras().getString("STRING_IMAGE_DET"));
            txtViewRatings.setText(getIntent().getExtras().getString("STRING_IMAGE_RAT"));
        }
        Uri uri = Uri.parse(stringImageUri);
        mImageView.setImageURI(uri);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemDetailsActivity.this, ViewPagerActivity.class);
                intent.putExtra("position", imagePosition);
                startActivity(intent);

            }
        });

        textViewAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item it = new Item();
                it.setItemName(getIntent().getExtras().getString("STRING_IMAGE_NAM"));
                it.setItemDescription(getIntent().getExtras().getString("STRING_IMAGE_DEC"));
                it.setItemDetails(getIntent().getExtras().getString("STRING_IMAGE_DET"));
                it.setItemPrice(getIntent().getExtras().getString("STRING_IMAGE_PRC"));
                it.setItemRating(getIntent().getExtras().getString("STRING_IMAGE_RAT"));
                it.setItemLocation(getIntent().getExtras().getString("STRING_IMAGE_LOC"));
                it.setItemSize(getIntent().getExtras().getString("STRING_IMAGE_SIZ"));
                it.setItemImageURI(new ArrayList<>(Arrays.asList(getIntent().getExtras().getString("STRING_IMAGE_URI"))));

                FirebaseDatabase.getInstance()
                        .getReference().child("cart")
                        .child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")))
                        .push().setValue(it);
                ++MainActivity.notificationCountCart;
                NotificationCountSetClass.setNotifyCount(MainActivity.notificationCountCart);
                Toast.makeText(ItemDetailsActivity.this, "Item Added To Cart!", Toast.LENGTH_SHORT).show();
            }
        });

        textViewBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
                imageUrlUtils.addCartListImageUri(stringImageUri);
                MainActivity.notificationCountCart++;
                NotificationCountSetClass.setNotifyCount(MainActivity.notificationCountCart);
                startActivity(new Intent(ItemDetailsActivity.this, CartListActivity.class));

            }
        });
    }
}
