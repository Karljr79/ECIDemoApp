package karlh.ecidemoapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.paypal.merchant.sdk.PayPalHereSDK;
import karlh.ecidemoapp.R;
import karlh.ecidemoapp.utils.CommonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;


public class LoginActivity extends Activity {

    private static final String LOG = "PayPalHere.LoginScreen";
    private String mUsername;
    private String mPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private TextView mEnv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //set the layout
        setContentView(R.layout.activity_login);

        //setup environment text view
        mEnv = (TextView) findViewById(R.id.txtEnv);

        //setup spinner
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        //setup login button
        mLoginButton = (Button) findViewById(R.id.btnLogin);
        // setup register button's click listener
        mLoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Grab username and password
                mUsername = CommonUtils.getString((EditText) findViewById(R.id.txtUsername));
                mPassword = CommonUtils.getString((EditText) findViewById(R.id.txtPassword));

                if(!isValidInput())
                {
                    // Sending back a toast message indicating invalid credentials.
                    CommonUtils.createToastMessage(LoginActivity.this,
                            CommonUtils.getStringFromId(LoginActivity.this, R.string.invalid_credentials));
                    return;
                }

                performOAuthLogin(v);

            }
        });


        // Initialize the PayPalHereSDK with the application context and the server env name.
        // This init is NECESSARY as the SDK needs the app context to init a few underlying objects.
        // The 2 options available are "Live" and "Sandbox"
        PayPalHereSDK.init(getApplicationContext(), PayPalHereSDK.Sandbox);

        // Using a default username and password.
        setUserCredentials();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
     * Method to move into the OAuth login activity.
     *
     * @param arg
     */
    private void performOAuthLogin(View arg) {
        hideKeyboard(arg);

        // Pass the username and password to the OAuth login activity for OAuth login.
        // Once the login is successful, we automatically check in the merchant in the OAuth activity.
        Intent intent = new Intent(LoginActivity.this, OAuthLoginActivity.class);
        intent.putExtra("username", mUsername);
        intent.putExtra("password", mPassword);
        //intent.putExtra("useLive", mUseLive);
        startActivity(intent);

    }

    private void updateConnectedEnvUI() {
        String connectedTo = PayPalHereSDK.getCurrentServer();
        mEnv.setText(CommonUtils.isNullOrEmpty(connectedTo) ? "" : connectedTo);
    }

    /**
     * Method to set the username and password to a default value.
     */
    //TODO: need to remove this before shipping the app to public
    private void setUserCredentials() {
        ((EditText) findViewById(R.id.txtUsername)).setText("test_user02@paypal.com");
        ((EditText) findViewById(R.id.txtPassword)).setText("11111111");
    }

    /**
     * Method to valid the input for null or empty.
     *
     * @return
     */
    private boolean isValidInput() {
        if (CommonUtils.isNullOrEmpty(mUsername) || CommonUtils.isNullOrEmpty(mPassword)) {
            return false;
        }
        return true;
    }

    /**
     * Method to hide the keyboard.
     *
     * @param v
     */
    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }
}
