package com.fmi.bookzz.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fmi.bookzz.R;
import com.fmi.bookzz.entity.User;
import com.fmi.bookzz.helper.RequestHelper;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    TextView loginTV;
    EditText usernameET;
    EditText passwordET;
    Button loginB;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginTV = findViewById(R.id.loginLabel);
        usernameET = findViewById(R.id.loginUsernameET);
        passwordET = findViewById(R.id.loginPasswordET);
        loginB = findViewById(R.id.loginB);

    }

    public void login(String username, String password){
        //User user = dbHelper.login(username, password);

        new LoginAsyncTask(username, password).execute();
    }

    public void onLoginClick(View view){
        if(usernameET.getText().length() == 0 || passwordET.getText().length() == 0){
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        login(username, password);
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Void> {

        boolean isSuccessful;
        String token;
        String errorMessage;
        String username;
        String password;

        ProgressDialog dialog;

        LoginAsyncTask(String username, String password){
            this.username = username;
            this.password = password;
            dialog = new ProgressDialog(LoginActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Loging in...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String urlString = String.format("%s:%s/login/%s/%s",
                    RequestHelper.ADDRESS, RequestHelper.PORT, username, password);

            HttpURLConnection urlConnection = null;

            try{
                URL url = new URL(urlString);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                String result = reader.readLine();

                if(result != null){

                    JSONObject resultOb = new JSONObject(result);
                    token = resultOb.getString("token");
                    if(!resultOb.getString("username").equals("")){
                        User user = new User();
                        user.setUsername(resultOb.getString("username"));
                        user.setProfilePicture(resultOb.getString("profilePicture"));
                        user.setfName(resultOb.getString("fName"));
                        user.setlName(resultOb.getString("lName"));
                        RequestHelper.currentUser = user;
                        RequestHelper.token = token;
                        isSuccessful = true;
                    }else{
                        errorMessage = resultOb.getString("Message");
                    }
                }

            } catch (Exception e) {
                errorMessage = e.getMessage();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            dialog.hide();

            if(isSuccessful){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}