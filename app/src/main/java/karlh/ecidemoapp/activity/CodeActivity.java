/*
ECI Demo App
Copyright Karl Hirschhorn, 2014
 */

package karlh.ecidemoapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import karlh.ecidemoapp.R;
import karlh.ecidemoapp.utils.CommonUtils;

public class CodeActivity extends Activity {

    //static variable
    private static final String LOG = "CODE SCREEN";

    private String mURL = "https://secure416.websitewelcome.com/~cdwright/tests/getrecord.php?";
    private String mCode, mLocationID, mCustomerID, mTabId, mLocale_x, mShouldApplyLoyalty;
    private Double mLoyaltyAmount, mTipAmount;

    //UI variables
    private Button mConfirmButton;
    private EditText txtCode;
    private ProgressDialog mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        txtCode = (EditText) findViewById(R.id.txtCode);

        //setup login button
        mConfirmButton = (Button) findViewById(R.id.btnSubmit);

        //setup spinner
        mSpinner = new ProgressDialog(CodeActivity.this);
        mSpinner.setIndeterminate(false);
        mSpinner.setCanceledOnTouchOutside(false);

        // setup register button's click listener
        mConfirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCode = CommonUtils.getString(txtCode);

                if (mCode.length() == 4)
                {
                    //validate the code
                    CheckCodeTask asyncTask = new CheckCodeTask(mCode);
                    asyncTask.execute();

                    hideKeyboard(v);

                    // Show the progress dialog
                    showProgressDialog();

                }
                else
                {
                    CommonUtils.createToastMessage(CodeActivity.this, "Please enter a 4 digit code");
                }
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.code, menu);
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

    /**
     * Method to show the progress dialog with a suitable message.
     */
    private void showProgressDialog() {
        mSpinner.setMessage("Checking Code...");
        mSpinner.show();
    }

    /**
     * Method to hide the progress dialog.
     */
    private void hideProgressDialog() {
        if (mSpinner.isShowing())
            mSpinner.dismiss();
    }

    private void hideKeyboard(View v)
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //all is good, head to the sale screen
    private void goSaleScreen()
    {
        //hide progress dialog
        hideProgressDialog();

        //pass the data from the JSON to the next activity
        Intent intent = new Intent(CodeActivity.this, SaleScreenActivity.class);
        intent.putExtra("locationId", mLocationID);
        intent.putExtra("customerId", mCustomerID);
        intent.putExtra("tabId", mTabId);
        intent.putExtra("locale_x", mLocale_x);
        intent.putExtra("loyaltyAmount", mLoyaltyAmount);
        intent.putExtra("applyLoyalty", mShouldApplyLoyalty);
        intent.putExtra("tipAmount", mTipAmount);
        intent.putExtra("code", mCode);

        startActivity(intent);

        Log.i(LOG, "Headed to Sales Screen..............");
    }


    //Async task to check the entered code
    private class CheckCodeTask extends AsyncTask<String, Void, ArrayList<String>>
    {
        String mCode = null;
        boolean mFailed = false;

        public CheckCodeTask(String code)
        {
            mCode = code;
        }

        private void setFailed()
        {
            mFailed = true;
        }

        @Override
        protected ArrayList<String> doInBackground(String... urls)
        {
            HttpClient httpclient = new DefaultHttpClient();
            mURL += "code="+mCode;

            HttpPost httppost = new HttpPost(mURL);
            String result = "";

            try
            {
                HttpResponse response = httpclient.execute(httppost);

                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();

                //convert response to string
                try
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    is.close();

                    result=sb.toString();

                    //is there a JSON payload?
                    if (!result.contains("null"))
                    {
                        try
                        {
                            //parse json data
                            JSONObject jobj = new JSONObject(result);
                            mLocationID = jobj.getString("locationId");
                            mCustomerID = jobj.getString("customerId");
                            mTabId = jobj.getString("tabId");
                            mLocale_x = jobj.getString("locale_x");
                            mLoyaltyAmount = jobj.getDouble("loyaltyAmount");
                            mTipAmount = jobj.getDouble("tipAmount");
                            mShouldApplyLoyalty = jobj.getString("applyLoyalty");

                            //head to the sale screen
                            goSaleScreen();
                        } catch (JSONException e)
                        {
                            Log.e(LOG, "Error parsing data " + e.toString());
                        }
                    }
                    else
                    {
                        //display toast
                        CommonUtils.createToastMessage(CodeActivity.this, "The code you entered is invalid, try again");


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //set text view back to none
                                txtCode.setText("");

                                //hide progress dialog
                                hideProgressDialog();
                            }
                        });


                    }
                }
                catch(Exception e){
                    Log.e(LOG, "Error converting result " + e.toString());
                }

            }
            catch(Exception e)
            {
                setFailed();
                Log.e(LOG, e.getMessage());
            }
            return null;
        }
    }
}
