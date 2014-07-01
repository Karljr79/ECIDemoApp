package karlh.ecidemoapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;

import karlh.ecidemoapp.utils.CommonUtils;

import karlh.ecidemoapp.R;

public class SaleScreenActivity extends Activity {

    private String mLocationId, mCustomerId, mTabId;
    private Double mLoyaltyAmount, mTipAmount, mSubTotal, mLoyaltyDiscount, mLoyaltyModifier;
    private TextView txtCustID, txtLoyaltyMember, txtLoyaltyBalance, txtSubTotal, txtTipAmount, txtGrandTotal, txtLoyaltyDiscount;
    private Button btnApplyLoyalty, btnCharge;
    private boolean mIsLoyaltyMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_screen);
        mLoyaltyDiscount = 0.00;
        mLoyaltyModifier = 10.00;

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
                if(!mIsLoyaltyMember)
                {
                    CommonUtils.createToastMessage(SaleScreenActivity.this, "This client has no loyalty points to apply!");
                }
                else
                {
                    mLoyaltyDiscount = mLoyaltyAmount * mLoyaltyModifier;

                    txtLoyaltyDiscount.setText(roundNumberForDisplay(mLoyaltyDiscount), TextView.BufferType.NORMAL);

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
        Double total;

        total = (mSubTotal + mTipAmount) - mLoyaltyDiscount;

        txtGrandTotal.setText(roundNumberForDisplay(total), TextView.BufferType.NORMAL);
    }

    public String roundNumberForDisplay(Double number)
    {
        Double roundedNumber = (double)Math.round(number * 100) / 100;

        return roundedNumber.toString();
    }
}
