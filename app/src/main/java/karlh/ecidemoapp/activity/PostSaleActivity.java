/*
ECI Demo App
Copyright Karl Hirschhorn, 2014
 */

package karlh.ecidemoapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import karlh.ecidemoapp.R;
import karlh.ecidemoapp.utils.CommonUtils;

public class PostSaleActivity extends Activity {

    //static variables
    private final String LOG = "POST SALE SCREEN";

    private String url = "https://secure416.websitewelcome.com/~cdwright/tests/getrecord.php?";
    private String mLoyaltyBalance, mTXNumber, mTotalPaid, mCode;

    //UI variables
    private TextView txtLoyaltyBalance, txtTXNumber, txtTotalPaid;
    private Button btnGoHome;

    //Async tasks variables
    private updateServerClass up;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_sale);

        //take care of parsing intent and connecting UI components
        handleNewIntent(getIntent());
        connectUI();

        //attempt to update the server with paid status and updated loyalty balance
        up = new updateServerClass();
        up.execute();

        updateDisplay();

        btnGoHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostSaleActivity.this, CodeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_sale, menu);
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

    public void handleNewIntent(Intent i)
    {
        //grab data from intent and store it
        mTXNumber = i.getStringExtra("TXNumber");
        mTotalPaid = i.getStringExtra("Amount");
        mLoyaltyBalance = i.getStringExtra("LoyaltyBalance");
        mCode = i.getStringExtra("Code");
    }

    public void connectUI()
    {
        //connect text fields
        txtLoyaltyBalance = (TextView) findViewById(R.id.txtNewBalance);
        txtTXNumber = (TextView) findViewById(R.id.txtComTX);
        txtTotalPaid = (TextView) findViewById(R.id.txtComTotal);
        btnGoHome = (Button) findViewById(R.id.btnReturnHome);
    }

    public void updateDisplay()
    {
        txtLoyaltyBalance.setText(mLoyaltyBalance, TextView.BufferType.NORMAL);
        txtTotalPaid.setText(mTotalPaid, TextView.BufferType.NORMAL);
        txtTXNumber.setText(mTXNumber, TextView.BufferType.NORMAL);

        Log.i(LOG, "Updated Post Sale Display");
    }

    private class updateServerClass extends AsyncTask<String, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(String... urls)
        {
            HttpClient httpclient = new DefaultHttpClient();
            url += "code=" + mCode + "&paid=y&loyalty=" + mLoyaltyBalance;

            HttpPost httppost = new HttpPost(url);
            String result = "";

            try
            {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

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

                    //parse json data
                    try
                    {
                        JSONObject jobj  = new JSONObject(result);

                        String status = jobj.getString("status");


                        if (status.equals("success"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CommonUtils.createToastMessage(PostSaleActivity.this, "Successfully updated Loyalty Database");
                                }
                            });

                        }

                    }
                    catch(JSONException e)
                    {
                        Log.e(LOG, "Error parsing data returned from database update " + e.toString());
                    }
                }
                catch(Exception e){
                    Log.e(LOG, "Error converting result " + e.toString());
                }

            }
            catch(Exception e)
            {
                Log.e(LOG, e.getMessage());
            }
            return null;
        }
    }

}
