package com.example.loginregister.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.loginregister.R;
import com.example.loginregister.helpers.RequestHandler;
import com.example.loginregister.helpers.SharedPrefManager;
import com.example.loginregister.helpers.URLs;
import com.example.loginregister.model.SPDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;


public class CreateAccount extends AppCompatActivity {
    private TextInputEditText sptextInputEditTextName;
    private TextInputEditText sptextInputEditTextEmail;
    private TextInputEditText sptextInputEditTextPhoneNumber;
    private TextInputEditText sptextInputEditTextPassword;
    private TextInputEditText sptextInputEditTextConfirmPassword;
    private AppCompatButton buttonRegister;
    ProgressDialog dialog;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        setTitle("Create an account");

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, SP_MainScreen.class));
            return;
        }

        sptextInputEditTextName = (TextInputEditText) findViewById(R.id.SP_FullName);
        sptextInputEditTextEmail = (TextInputEditText) findViewById(R.id.SP_UserEmail);
        sptextInputEditTextPhoneNumber = (TextInputEditText) findViewById(R.id.SP_Number2);
        sptextInputEditTextPassword = (TextInputEditText) findViewById(R.id.SP_textInputEditTextPassword);
        sptextInputEditTextConfirmPassword = (TextInputEditText) findViewById(R.id.SP_textInputEditTextConfirmPassword);
        buttonRegister = (AppCompatButton)findViewById(R.id.SP_appCompatButtonRegister) ;

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();

            }
        });

    }

    private void registerUser() {

        final String name = sptextInputEditTextName.getText().toString().trim();
        final String email = sptextInputEditTextEmail.getText().toString().trim();
        final String password = sptextInputEditTextPassword.getText().toString().trim();
        final String phone = sptextInputEditTextPhoneNumber.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            sptextInputEditTextName.setError("Please enter username");
            sptextInputEditTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            sptextInputEditTextPhoneNumber.setError("Please enter a phone number");
            sptextInputEditTextPhoneNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            sptextInputEditTextEmail.setError("Please enter email");
            sptextInputEditTextEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            sptextInputEditTextEmail.setError("Enter a valid email");
            //sptextInputEditTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            sptextInputEditTextPassword.setError("Enter a password");
            sptextInputEditTextPassword.requestFocus();
            return;
        }

        class RegisterUser extends AsyncTask<Void, Void, String> {



            @Override
            protected String doInBackground(Void... voids) {
                //creating request handler object
//                RequestHandler requestHandler = new RequestHandler();
//
//                //creating request parameters
//                HashMap<String, String> params = new HashMap<>();
//                params.put("username", name);
//                params.put("email", email);
//                params.put("password", password);
//                params.put("phone", phone);
//
//                //returing the response
        //        return requestHandler.sendPostRequest(URLs.URL_REGISTER, params);

                String result="";
                try {
                    URL url = new URL(URLs.URL_REGISTER);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder();
                    builder.appendQueryParameter("username", name);
                    builder.appendQueryParameter("email", email);
                    builder.appendQueryParameter("password", password);
                    builder.appendQueryParameter("phone", phone);
                    String query = builder.build().getEncodedQuery();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();

                    int response = conn.getResponseCode();
                    if (response == HttpURLConnection.HTTP_OK) {
                        InputStream bis = new BufferedInputStream(conn.getInputStream());
//                        JSONParsr jsonParser = new JSONParser();
//                        JSONObject jsonObject = (JSONObject)jsonParser.parse(
//                                new InputStreamReader(inputStream, "UTF-8"));
                        BufferedReader streamReader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
                        StringBuilder responseStrBuilder = new StringBuilder();

                        String inputStr;
                        while ((inputStr = streamReader.readLine()) != null)
                            responseStrBuilder.append(inputStr);

                        JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

                        result = jsonObject.toString();
                    }
                    conn.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return result;
            }


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(CreateAccount.this);
                dialog.setMessage("Registering..");
                dialog.show();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //hiding the progressbar after completion
                dialog.dismiss();

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(s);

                    //if no error in response
                    if (!obj.getBoolean("error")) {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                        //getting the user from the response
                        JSONObject userJson = obj.getJSONObject("user");

                        //creating a new user object
                        SPDetails spDetails = new SPDetails(
                                userJson.getInt("id"),
                                userJson.getString("username"),
                                userJson.getString("email"),
                                userJson.getString("phone")
                        );

                        Log.i("user", Integer.toString(userJson.getInt("id")));
                        Log.i("user1", userJson.getString("username"));
                        Log.i("user2",userJson.getString("email"));
                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(spDetails);

                        //starting the profile activity
                        finish();
                        startActivity(new Intent(getApplicationContext(), OtpVerification.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        //executing the async task
        RegisterUser ru = new RegisterUser();
        try {
            ru.execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}