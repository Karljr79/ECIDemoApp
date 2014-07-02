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

import com.paypal.core.rest.OAuthTokenCredential;

import karlh.ecidemoapp.utils.RestClient;

import karlh.ecidemoapp.utils.CommonUtils;
import karlh.ecidemoapp.R;

public class SaleScreenActivity extends Activity {

    private String mLocationId, mCustomerId, mTabId;
    private Double mLoyaltyAmount, mTipAmount, mSubTotal, mLoyaltyDiscount, mLoyaltyRemainder;
    private TextView txtCustID, txtLoyaltyMember, txtLoyaltyBalance, txtSubTotal, txtTipAmount, txtGrandTotal, txtLoyaltyDiscount;
    private Button btnApplyLoyalty, btnCharge;
    private boolean mIsLoyaltyMember;
    private getAccessTokenClass gat;
    private createInvoiceClass cr;

    private static final String mClientID = "AdjgOBAs-DM-nU3wsoKzFZvq2W8Vc_-RT0aCOeKEvHAJWnGk66giE6rKDZiN";
    private static final String mSecret = "ECEt3xDGDlZ8zixnT5PD9jwIirSjKh0lmRZx1ocMfmB4PZ19WDxBhFHHUF2w";
    private String mAccessToken;
    private String mURLInvoice = "https://www.paypal.com/webapps/hereapi/merchant/v1/invoices";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_screen);
        mLoyaltyDiscount = 0.00;

        gat = new getAccessTokenClass();
        gat.execute();

        cr = new createInvoiceClass();
        cr.execute();


        //grab data from intent
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
        btnCharge = (Button) findViewById(R.id.btnSettle);

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
                    if (mLoyaltyAmount >= getTotal() - 1.00)
                    {
                        mLoyaltyRemainder = mLoyaltyDiscount - getTotal();
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
        float minX = 25.0f;
        float maxX = 50.0f;

        Random rand = new Random();

        float finalX = rand.nextFloat() * (maxX - minX) + minX;

        return finalX;
    }

    public void updateTotal()
    {
        txtGrandTotal.setText(roundNumberForDisplay(getTotal()), TextView.BufferType.NORMAL);
        txtLoyaltyBalance.setText(roundNumberForDisplay(mLoyaltyAmount), TextView.BufferType.NORMAL);
    }

    public Double getTotal()
    {
        Double total;

        total = (mSubTotal + mTipAmount) - mLoyaltyDiscount;

        return total;
    }

    public String roundNumberForDisplay(Double number)
    {
        Double roundedNumber = (double)Math.round(number * 100) / 100;

        return roundedNumber.toString();
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
                Log.i("RESTCLIENT", "Did not work!!!!!!!!!!!!!!");
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


            }
            catch (Exception e){
                Log.e("Payment", "Did not work!!!!!!!!!!!!!!");
            }
            return null;
        }
    }


}
