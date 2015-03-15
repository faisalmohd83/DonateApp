package com.example.fmtestapp;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class ImageProcessingActivity extends Activity {

    private static final String TAG = ImageProcessingActivity.class.getSimpleName();

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
    // note that these credentials will differ between live & sandbox
    // environments.
    private static final String CONFIG_CLIENT_ID = "credential from developer.paypal.com";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int IMAGE_PICKER_REQUEST = 3;

    private final int RESPONSE_OK = 200;
    private final String OCR_API_KEY = "QdgJmSnWjK";
    private final String ISO_639_1_CODE_EN = "en";

    private Spinner typeSpinner;
    private EditText amountText;
    private EditText dateText;

    private String mPayType = null;
    private String fileName = null;
    private String amountString = null;
    private String dateString = null;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT).clientId(CONFIG_CLIENT_ID)
            .merchantName("Hipster Store")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_bill_layout);

        // spinner
        typeSpinner = (Spinner) findViewById(R.id.spinnerutiltype);
        typeSpinner.setOnItemSelectedListener(typeSelectedListener);
        // editText
        // Amount
        amountText = (EditText) findViewById(R.id.inputamount);
        amountText.addTextChangedListener(watcher);
        // Date
        dateText = (EditText) findViewById(R.id.inputdate);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

        }
    }

    TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            amountString = amountText.getText().toString();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }
    };

    OnItemSelectedListener typeSelectedListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //
            mPayType = typeSpinner.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            mPayType = "Water";
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_fetch:
            fetchPitcureContent();
            return true;
        case R.id.action_proceed:
            checkout();
            return true;
        case R.id.action_reset:
            resetUI();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal(amountString), "SEK", mPayType, paymentIntent);
    }

    private void checkout() {
        if (TextUtils.isEmpty(amountString) || TextUtils.isEmpty(dateString)) {
            Toast.makeText(ImageProcessingActivity.this, "Please scan bill to fetch data",
                    Toast.LENGTH_SHORT).show();
        } else {
            /*
             * PAYMENT_INTENT_SALE will cause the payment to complete
             * immediately. Change PAYMENT_INTENT_SALE to -
             * PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture
             * funds later. - PAYMENT_INTENT_ORDER to create a payment for
             * authorization and capture later via calls from your server.
             * 
             * Also, to include additional payment details and an item list, see
             * getStuffToBuy() below.
             */
            PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

            /*
             * See getStuffToBuy(..) for examples of some available payment
             * options.
             */
            Intent intent = new Intent(ImageProcessingActivity.this, PaymentActivity.class);

            // send the same configuration for restart resiliency
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
            startActivityForResult(intent, REQUEST_CODE_PAYMENT);
        }
    }

    private void resetUI() {
        typeSpinner.setSelection(0, true);
        mPayType = null;
        amountText.setText(null);
        dateText.setText(null);
    }

    private void fetchPitcureContent() {
        // Starting image picker activity
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                IMAGE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         * TODO: send 'confirm' (and possibly
                         * confirm.getPayment() to your server for verification
                         * or consent completion. See
                         * https://developer.paypal.com
                         * /webapps/developer/docs/integration
                         * /mobile/verify-mobile-payment/ for more details.
                         * 
                         * For sample mobile backend interactions, see
                         * https://github
                         * .com/paypal/rest-api-sdk-python/tree/master
                         * /samples/mobile_backend
                         */

                        Toast.makeText(getApplicationContext(), "Payment Succesful",
                                Toast.LENGTH_LONG).show();
                        // reset UI
                        resetUI();

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK) {
            fileName = getRealPathFromURI(data.getData());
            Log.d(TAG, "Selected (en) :" + getStringNameFromRealPath(fileName));
            // UI validation
            if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(OCR_API_KEY)
                    && !TextUtils.isEmpty(ISO_639_1_CODE_EN)) {
                new processImageTask().execute();
            } else {
                Toast.makeText(ImageProcessingActivity.this, "All data are required.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class processImageTask extends AsyncTask<Void, Void, Void> {
        // local vars to AysncTask
        String processedText = null;
        ProgressDialog progressDialog = null;
        OCRServiceAPI apiClient = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ImageProcessingActivity.this, "Please wait ...",
                    "Processing Image, Converting to text.", true, false);
            apiClient = new OCRServiceAPI(OCR_API_KEY);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //
            apiClient.convertToText(ISO_639_1_CODE_EN, fileName);
            // Showing response dialog
            processedText = apiClient.getResponseText();

            if (apiClient.getResponseCode() == RESPONSE_OK) {
                try {
                    // fetching amount
                    Pattern amountPattern = Pattern.compile("TOTAL (.+)");
                    Matcher matcher = amountPattern.matcher(processedText);
                    while (matcher.find()) {
                        Log.d(TAG, "Faisal " + matcher.group());
                        String temp[] = matcher.group().split("\\s");
                        amountString = temp[temp.length - 1].trim();
                        Log.d(TAG, "Amount: " + amountString);
                    }
                    // fetching date
                    // Pattern.compile("([0-9]{2})/([0-9]{2})/([0-9]{4})");
                    Pattern datePattern = Pattern.compile("Due Date: (.+)");
                    Matcher matcher2 = datePattern.matcher(processedText);
                    while (matcher2.find()) {
                        dateString = matcher2.group().split(":")[1].trim();
                        Log.d(TAG, "Date: " + dateString);
                    }
                } catch (IllegalStateException e) {
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // stop progress bar
            progressDialog.dismiss();
            if (apiClient.getResponseCode() == RESPONSE_OK) {
                // set amount
                if (!TextUtils.isEmpty(amountString))
                    amountText.setText(amountString);
                // set date
                if (!TextUtils.isEmpty(dateString))
                    dateText.setText(dateString);
            } else {
                Toast.makeText(ImageProcessingActivity.this,
                        "Bad reseponse from read OCR. You may try again.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /*
     * Returns image real path.
     */
    private String getRealPathFromURI(final Uri contentUri) {
        final String[] proj = {
            MediaStore.Images.Media.DATA };
        final Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    /*
     * Cuts selected file name from real path to show in screen.
     */
    private String getStringNameFromRealPath(final String bucketName) {
        return bucketName.lastIndexOf('/') > 0 ? bucketName
                .substring(bucketName.lastIndexOf('/') + 1) : bucketName;
    }
}
