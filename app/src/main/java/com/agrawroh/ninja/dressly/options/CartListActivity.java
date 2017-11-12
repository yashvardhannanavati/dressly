package com.agrawroh.ninja.dressly.options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawroh.ninja.dressly.R;
import com.agrawroh.ninja.dressly.model.Item;
import com.agrawroh.ninja.dressly.product.ItemDetailsActivity;
import com.agrawroh.ninja.dressly.startup.MainActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartListActivity extends AppCompatActivity {
    private static Context mContext;
    private PaymentsClient mPaymentsClient;
    private static TextView payment, amount;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);
        mContext = CartListActivity.this;

        //Payment API
        payment = findViewById(R.id.text_action_bottom2);
        amount = findViewById(R.id.text_action_bottom1);

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPayment(view);
            }
        });

        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);
        checkIsReadyToPay();

        final RecyclerView recyclerView = findViewById(R.id.recyclerview);
        FirebaseDatabase.getInstance()
                .getReference().child("cart").child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Item> items = new ArrayList<>();
                        totalCartValue = 0;
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Item item = postSnapshot.getValue(Item.class);
                            items.add(item);
                            totalCartValue += Long.parseLong(item.getItemPrice().replace("$", ""));
                            totalCartValue += Long.parseLong(item.getItemRating().replace("$", ""));
                        }
                        updatePrice();
                        setCartLayout(items.size());
                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        recyclerView.setAdapter(new CartListActivity.SimpleStringRecyclerViewAdapter(recyclerView, items));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void updatePrice() {
        amount.setText("$" + String.valueOf(totalCartValue));
    }

    private void checkIsReadyToPay() {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(mPaymentsClient).addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            setPwgAvailable(result);
                        } catch (ApiException exception) {
                            // Process error
                            Log.w("isReadyToPay failed", exception);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status.getStatusCode());
                        break;
                }

                // Re-enables the Pay with Google button.
                payment.setClickable(true);
                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        //
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest.
        if (token != null) {
            String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show();

            // Use token.getToken() to get the token string.
            Log.d("PaymentData", "PaymentMethodToken received");
        }
    }

    private void handleError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletERROR_CODE_* 
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(View view) {
        // Disables the button to prevent multiple clicks.
        payment.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
//        String price = PaymentsUtil.microsToString(Long.parseLong(amount.getText().toString()));
        String price = PaymentsUtil.microsToString(totalCartValue);
        TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = mPaymentsClient.loadPaymentData(request);

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    private void setPwgAvailable(boolean available) {
        // If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
        // notify the user that Pay with Google is not available.
        // Please adjust to fit in with your current user flow. You are not required to explicitly
        // let the user know if isReadyToPay returns false.
        if (!available) {
            Toast.makeText(getApplicationContext(), "Unfortunately Google Pay is not available for this...", Toast.LENGTH_SHORT).show();
        }
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder> {

        private List<Item> cartItems;
        private RecyclerView mRecyclerView;

        static class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final SimpleDraweeView mImageView;
            final TextView cartItemName;
            final TextView cartItemDesc;
            final TextView cartItemPrice;
            final TextView cartItemLoc;
            final TextView cartItemSize;
            final LinearLayout mLayoutItem, mLayoutRemove;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = view.findViewById(R.id.image_cartlist);
                cartItemName = view.findViewById(R.id.cart_item_name);
                cartItemDesc = view.findViewById(R.id.cart_item_desc);
                cartItemPrice = view.findViewById(R.id.cart_item_price);
                cartItemLoc = view.findViewById(R.id.cart_item_loc);
                cartItemSize = view.findViewById(R.id.cart_item_size);
                mLayoutItem = view.findViewById(R.id.layout_item_desc);
                mLayoutRemove = view.findViewById(R.id.layout_action1);
            }
        }

        public SimpleStringRecyclerViewAdapter(RecyclerView recyclerView, List<Item> cartItems) {
            this.cartItems = cartItems;
            this.mRecyclerView = recyclerView;
        }

        @Override
        public CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cartlist_item, parent, false);
            return new CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onViewRecycled(CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder holder) {
            if (holder.mImageView.getController() != null) {
                holder.mImageView.getController().onDetach();
            }
            if (holder.mImageView.getTopLevelDrawable() != null) {
                holder.mImageView.getTopLevelDrawable().setCallback(null);
//                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
            }
        }

        @Override
        public void onBindViewHolder(final CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder holder, final int position) {
            final Uri uri = Uri.parse(cartItems.get(position).getItemImageURI().get(0));
            holder.mImageView.setImageURI(uri);
            holder.cartItemName.setText(cartItems.get(position).getItemName());
            holder.cartItemDesc.setText(cartItems.get(position).getItemDescription());
            long price = Long.parseLong(cartItems.get(position).getItemPrice().replace("$", ""));
            price += Long.parseLong(cartItems.get(position).getItemRating().replace("$", ""));
            holder.cartItemPrice.setText("$" + String.valueOf(price));
            holder.cartItemLoc.setText(cartItems.get(position).getItemLocation());
            holder.cartItemSize.setText("Size: " + cartItems.get(position).getItemSize());
            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                    intent.putExtra("STRING_IMAGE_NAM", cartItems.get(position).getItemName());
                    intent.putExtra("STRING_IMAGE_DEC", cartItems.get(position).getItemDescription());
                    intent.putExtra("STRING_IMAGE_DET", cartItems.get(position).getItemDetails());
                    intent.putExtra("STRING_IMAGE_PRC", cartItems.get(position).getItemPrice());
                    intent.putExtra("STRING_IMAGE_RAT", cartItems.get(position).getItemRating());
                    intent.putExtra("STRING_IMAGE_URI", cartItems.get(position).getItemImageURI().get(0));
                    intent.putExtra("STRING_IMAGE_LOC", cartItems.get(position).getItemLocation());
                    intent.putExtra("STRING_IMAGE_SIZ", cartItems.get(position).getItemSize());
                    intent.putExtra("STRING_IMAGE_POS", position);
                    mContext.startActivity(intent);

                }
            });

            //Set click action
            holder.mLayoutRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance()
                            .getReference().child("cart")
                            .child(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "|")))
                            .orderByChild("itemId").equalTo(cartItems.get(position).getItemId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        return;
                                    }
                                    for (DataSnapshot postDataSnapshot : dataSnapshot.getChildren()) {
                                        totalCartValue -= Long.parseLong(postDataSnapshot.getValue(Item.class).getItemPrice().replace("$", ""));
                                        totalCartValue -= Long.parseLong(postDataSnapshot.getValue(Item.class).getItemRating().replace("$", ""));
                                        postDataSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    cartItems.remove(position);
                    notifyDataSetChanged();
                    updatePrice();
                    --MainActivity.notificationCountCart;
                }
            });
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }
    }

    private static long totalCartValue = 0;

    /**
     * Set Cart Layout
     *
     * @param itemsCount
     */
    protected void setCartLayout(final int itemsCount) {
        LinearLayout layoutCartItems = findViewById(R.id.layout_items);
        LinearLayout layoutCartPayments = findViewById(R.id.layout_payment);
        LinearLayout layoutCartNoItems = findViewById(R.id.layout_cart_empty);

        if (itemsCount > 0) {
            layoutCartNoItems.setVisibility(View.GONE);
            layoutCartItems.setVisibility(View.VISIBLE);
            layoutCartPayments.setVisibility(View.VISIBLE);
        } else {
            layoutCartNoItems.setVisibility(View.VISIBLE);
            layoutCartItems.setVisibility(View.GONE);
            layoutCartPayments.setVisibility(View.GONE);

            Button bStartShopping = findViewById(R.id.bAddNew);
            bStartShopping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

}


