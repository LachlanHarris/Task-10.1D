package com.example.a101d;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.a101d.util.PaymentUtil;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import java.util.Locale;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



public class FoodItem<view> extends AppCompatActivity {

    private Button googlePayButton;
    private PaymentsClient paymentsClient;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item);
        googlePayButton = findViewById(R.id.GPayButton);

        paymentsClient = PaymentUtil.createPaymentsClient(this);

        int ID = getIntent().getIntExtra("FoodID", 99999999);
        TextView IDView = findViewById(R.id.textView2);
        IDView.setText(String.valueOf(ID));



        googlePayButton.setOnClickListener(
                new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        requestPayment(view);
                    }
                });




        possiblyShowGooglePayButton();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void possiblyShowGooglePayButton() {

        final Optional<JSONObject> isReadyToPayJson = PaymentUtil.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            setGooglePayAvailable(task.getResult());
                        } else {
                            Log.w("isReadyToPay failed", task.getException());
                        }
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        //handlePaymentSuccess(paymentData);
                        break;

                    case Activity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        //handleError(status.getStatusCode());
                        break;
                }

                // Re-enables the Google Pay payment button.
                googlePayButton.setClickable(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void requestPayment(View view) {

        // Disables the button to prevent multiple clicks.
        googlePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user
        //double garmentPrice = selectedGarment.getDouble("price");
        //long garmentPriceCents = Math.round(garmentPrice * PaymentsUtil.CENTS_IN_A_UNIT.longValue());
        long priceCents = 1000;
        Optional<JSONObject> paymentDataRequestJson = PaymentUtil.getPaymentDataRequest(priceCents);
        if (!paymentDataRequestJson.isPresent()) {
            return;
        }
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    this, LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "GOOGLE PAY UNAVAILABLE", Toast.LENGTH_LONG).show();
        }
    }
}