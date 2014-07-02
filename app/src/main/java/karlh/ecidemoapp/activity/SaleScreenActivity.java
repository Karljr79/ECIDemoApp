package karlh.ecidemoapp.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

import com.paypal.core.rest.OAuthTokenCredential;

import karlh.ecidemoapp.utils.HttpClient;

import karlh.ecidemoapp.utils.CommonUtils;
import karlh.ecidemoapp.R;

public class SaleScreenActivity extends Activity {

    private static final String LOG = "SALESCREEN";
    private String mLocationId, mCustomerId, mTabId, mInvoiceID;
    private Double mLoyaltyAmount, mTipAmount, mSubTotal, mLoyaltyDiscount, mLoyaltyRemainder;
    private TextView txtCustID, txtLoyaltyMember, txtLoyaltyBalance, txtSubTotal, txtTipAmount, txtGrandTotal, txtLoyaltyDiscount;
    private Button btnApplyLoyalty, btnCreateInvoice, btnSale;
    private boolean mIsLoyaltyMember;
    private getAccessTokenClass gat;
    private createInvoiceClass cr;

    private static final String mClientID = "AdjgOBAs-DM-nU3wsoKzFZvq2W8Vc_-RT0aCOeKEvHAJWnGk66giE6rKDZiN";
    private static final String mSecret = "ECEt3xDGDlZ8zixnT5PD9jwIirSjKh0lmRZx1ocMfmB4PZ19WDxBhFHHUF2w";
    private String mAccessToken;
    private String mURLInvoice = "https://www.paypal.com/webapps/hereapi/merchant/v1/invoices";
    private String mURLPayment = "https://www.paypal.com/webapps/hereapi/merchant/v1/pay";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_screen);
        mLoyaltyDiscount = 0.00;

        gat = new getAccessTokenClass();
        gat.execute();

        //grab data from intent and store it
        mLocationId = getIntent().getStringExtra("locationId");
        mCustomerId = getIntent().getStringExtra("customerId");
        mTabId = getIntent().getStringExtra("tabId");
        mLoyaltyAmount = getIntent().getDoubleExtra("loyaltyAmount", 0.00);
        mTipAmount = getIntent().getDoubleExtra("tipAmount", 0.00);
        mSubTotal = getRandomNumber().doubleValue();

        //connect text fields
        txtCustID = (TextView) findViewById(R.id.txtCustName);
        txtLoyaltyBalance = (TextView) findViewById(R.id.txtLoyaltyBalance);
        txtLoyaltyMember = (TextView) findViewById(R.id.txtLoyaltyMember);
        txtSubTotal = (TextView) findViewById(R.id.txtSubTotal);
        txtTipAmount = (TextView) findViewById(R.id.txtTipAmount);
        txtGrandTotal = (TextView) findViewById(R.id.txtGrandTotal);
        txtLoyaltyDiscount = (TextView) findViewById(R.id.txtLoyaltyDiscount);

        //connect buttons
        btnApplyLoyalty = (Button) findViewById(R.id.btnApplyLoyalty);
        btnCreateInvoice = (Button) findViewById(R.id.btnCreateInvoice);
        btnSale = (Button) findViewById(R.id.btnSale);
        btnSale.setEnabled(false);


        //handle initial population of data
        txtCustID.setText(mCustomerId, TextView.BufferType.NORMAL);
        txtLoyaltyBalance.setText(mLoyaltyAmount.toString(), TextView.BufferType.NORMAL);

        //does the customer have a balance to spend?
        if(mLoyaltyAmount > 0.00)
        {
            txtLoyaltyMember.setText("Yes", TextView.BufferType.NORMAL);
            mIsLoyaltyMember = true;
        }
        else
        {
            txtLoyaltyMember.setText("No", TextView.BufferType.NORMAL);
            mIsLoyaltyMember = false;
        }

        //get a random number for the invoice
        txtSubTotal.setText(roundNumberForDisplay(mSubTotal), TextView.BufferType.NORMAL);
        txtLoyaltyDiscount.setText("0.00", TextView.BufferType.NORMAL);
        txtTipAmount.setText(mTipAmount.toString(), TextView.BufferType.NORMAL);

        //update the total display
        updateTotal();

        //setup apply loyalty button listener
        btnApplyLoyalty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if not a loyalty member
                if(!mIsLoyaltyMember || mLoyaltyAmount == 0)
                {
                    CommonUtils.createToastMessage(SaleScreenActivity.this, "This client is not a member or has no loyalty points to apply!");
                }
                else
                {
                    mLoyaltyDiscount = mLoyaltyAmount;

                    //is the discount greater than the actual bill?
                    if (mLoyaltyAmount >= getTotal(false) - 1.00)
                    {
                        mLoyaltyRemainder = mLoyaltyDiscount - getTotal(false);
                        mLoyaltyAmount = mLoyaltyRemainder;
                        txtLoyaltyBalance.setText(roundNumberForDisplay(mLoyaltyRemainder), TextView.BufferType.NORMAL);
                        txtLoyaltyDiscount.setText(roundNumberForDisplay(mLoyaltyDiscount), TextView.BufferType.NORMAL);
                    }
                    else
                    {
                        txtLoyaltyDiscount.setText(roundNumberForDisplay(mLoyaltyDiscount), TextView.BufferType.NORMAL);
                    }

                    updateTotal();
                }

            }
        });

        btnCreateInvoice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cr = new createInvoiceClass();
                cr.execute();
            }
        });

        btnSale.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                JSONObject payload = constructPaymentPayload(mTabId, mInvoiceID);

                try {
                    Log.i(LOG, payload.toString(2));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sale_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Float getRandomNumber()
    {
        float minX = 3.0f;
        float maxX = 5.0f;

        Random rand = new Random();

        float finalX = rand.nextFloat() * (maxX - minX) + minX;

        return finalX;
    }

    public void updateTotal()
    {
        txtGrandTotal.setText(roundNumberForDisplay(getTotal(false)), TextView.BufferType.NORMAL);
        txtLoyaltyBalance.setText(roundNumberForDisplay(mLoyaltyAmount), TextView.BufferType.NORMAL);
    }

    public Double getTotal(boolean onlySubTotal)
    {
        Double total;

        if (onlySubTotal)
        {
            total = (mSubTotal + mTipAmount) - mLoyaltyDiscount;
        }
        else
        {
            total = mSubTotal - mLoyaltyDiscount;
        }


        return total;
    }

    public String roundNumberForDisplay(Double number)
    {
        Double roundedNumber = (double)Math.round(number * 100) / 100;

        return roundedNumber.toString();
    }

    public JSONObject constructInvoicePayload(String tip, String total)
    {
        JSONObject objectParent = new JSONObject();

        try
        {
            //objectParent = new JSONObject("{\"paymentType\":\"tab\",\"tabId\":\"" + tabId + "\",\"invoice\":{\"currencyCode\":\"CAD\",\"paymentTerms\":\"DueOnReceipt\",\"merchantInfo\":{\"businessName\":\"ECI Test Merchant\",\"address\":{\"country\":\"US\",\"state\":\"CA\",\"line1\":\"Address Not Specified\",\"city\":\"BostonSan Francisco\",\"postalCode\":\"94109\"}},\"items\":[{\"unitPrice\":\"" + total + "\",\"name\":\"Default Item\",\"quantity\":\"1\"}],\"gratuityAmount\":\"" + tip + ",\"merchantEmail\":\"karljr79@gmail.com\"}}");
            objectParent = new JSONObject("{\"merchantEmail\":\"cowright@paypal.com\",\"merchantInfo\":{\"businessName\":\"Cory ECI Test App\",\"address\":{\"line1\":\"2141 N. 1st Street\",\"city\":\"San Jose\",\"state\":\"CA\",\"postalCode\":\"95131\",\"country\":\"US\"},\"phoneNumber\":\"\"},\"currencyCode\":\"USD\",\"items\":[{\"name\":\"Test Item\",\"quantity\":\"1\",\"unitPrice\":\"" + total + "\"}],\"gratuityAmount\":\"" + tip + "\",\"invoiceDate\":\"2014-07-02T19:28:04+01:00\",\"paymentTerms\":\"DueOnReceipt\"})");
        }
        catch (Exception ex)
        {
            Log.e(LOG, "Error creating JSON Invoice!!!!!!!");
        }

        try
        {
            String message = objectParent.toString();

            Log.i("JSON Invoice Output", message);
        }
        catch(Exception e)
        {
            Log.e(LOG, "Error converting JSON to String!!!!!!!");
        }

        return objectParent;
    }

    public JSONObject constructPaymentPayload(String tabID, String invoice)
    {
        JSONObject objectParent = new JSONObject();

        try
        {
            objectParent = new JSONObject();
            objectParent.put("paymentType","tab");
            objectParent.put("invoiceId", invoice);
            objectParent.put("tabId", tabID);
        }
        catch (Exception ex)
        {
            Log.e(LOG, "Error creating JSON Payload!!!!!!!");
        }

        try
        {
            String message = objectParent.toString();

            Log.i("JSON Invoice Output", message);
        }
        catch(Exception e)
        {
            Log.e(LOG, "Error converting JSON to String!!!!!!!");
        }

        return objectParent;
    }

    private class getAccessTokenClass extends AsyncTask<String, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(String... urls)
        {
            try{
                Map<String, String> sdkconfig = new HashMap<String, String>();
                sdkconfig.put("mode", "live");

                try {
                    mAccessToken = new OAuthTokenCredential(mClientID, mSecret, sdkconfig).getAccessToken();
                    Log.i("Access Token is: ", mAccessToken);
                }
                catch (Exception e){
                    Log.i("Issue with PayPal Auth", "Problems!!!!");
                    e.printStackTrace();
                }
            }
            catch (Exception e){
                Log.i("ACCESS", "Did not work!!!!!!!!!!!!!!");
            }
            return null;
        }

    }

    private class createInvoiceClass extends AsyncTask<String, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(String... urls)
        {
            try{
                JSONObject jo = constructInvoicePayload(roundNumberForDisplay(mTipAmount), roundNumberForDisplay(getTotal(true)));

                JSONObject jsonRec = HttpClient.SendHttpPost(mURLInvoice, jo, mAccessToken);

                Log.i("RECEIVED", jsonRec.toString(2));

                if (jsonRec.getString("status").equals("200") || jsonRec.getString("status").equals("201"))
                {
                    mInvoiceID = jsonRec.getString("invoiceID");
                    CommonUtils.createToastMessage(SaleScreenActivity.this, "Invoice created!!!" + mInvoiceID);

                    Log.i(LOG, "Invoice ID: " + mInvoiceID);

                    btnSale.setEnabled(true);
                }
                else
                {
                    CommonUtils.createToastMessage(SaleScreenActivity.this, "Error" + jsonRec.getString("status"));
                }
            }
            catch (Exception e){
                Log.e(LOG, "Invoice Creation Did not Work!!!!!!!!!!!!!!");
            }
            return null;
        }
    }

    private class makePaymentClass extends AsyncTask<String, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(String... urls)
        {
            try{

                if (mTabId != null && mInvoiceID != null)
                {
                    JSONObject jo = constructPaymentPayload(mTabId, mInvoiceID);

                    JSONObject jsonRec = HttpClient.SendHttpPost(mURLPayment, jo, mAccessToken);

                    Log.i("RECEIVED PAYMENT RESPONSE", jsonRec.toString(2));


                }
                else
                {
                    CommonUtils.createToastMessage(SaleScreenActivity.this, "There is no Invoice ID");
                    return null;
                }

            }
            catch (Exception e){
                Log.e("Payment", "Did not work!!!!!!!!!!!!!!");
            }
            return null;
        }
    }

}
