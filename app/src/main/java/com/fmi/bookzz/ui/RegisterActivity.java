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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    TextView registerTV;
    EditText usernameET;
    EditText firstNameET;
    EditText lastNameET;
    EditText passwordET;
    Button registerB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerTV = findViewById(R.id.registerLabel);
        usernameET = findViewById(R.id.registerUsernameET);
        firstNameET = findViewById(R.id.registerFirstNameET);
        lastNameET = findViewById(R.id.registerLastNameET);
        passwordET = findViewById(R.id.registerPasswordET);
        registerB = findViewById(R.id.registerB);



    }
    public void register(View view){

        if(view.getId() == R.id.registerB){
            if(usernameET.getText().length() == 0
                    || firstNameET.getText().length() == 0
                    || lastNameET.getText().length() == 0
                    || passwordET.getText().length() == 0){

                Toast.makeText(this, "Please enter all fields and make sure both passwords match",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();

            user.setfName(firstNameET.getText().toString());
            user.setlName(lastNameET.getText().toString());
            user.setUsername(usernameET.getText().toString());
            user.setPassword(passwordET.getText().toString());

            new RegisterAsyncTask(user).execute();


        }
    }

    private class RegisterAsyncTask extends AsyncTask<Void, Void, Void> {

        User user;
        boolean isSuccessful;
        ProgressDialog dialog;

        String error;

        RegisterAsyncTask(User user){
            this.user = user;
            dialog = new ProgressDialog(RegisterActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Registering process is going...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String urlString = String.format("%s:%s/user/%s/%s/%s/%s",//fName/lName/username/password
                    RequestHelper.ADDRESS, RequestHelper.PORT, user.getfName(),
                    user.getlName(), user.getUsername(),user.getPassword());

            HttpURLConnection urlConnection = null;

            try{
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                String result = reader.readLine();

                if(result != null){
                    JSONObject jsonOb = new JSONObject(result);

                    if(!jsonOb.getString("username").equals("")){
                        isSuccessful = true;
                    }else{
                        error = "error";
                    }

                }else{
                    error = "There was no response!";
                }

            }catch (IOException | JSONException e) {
                error = e.getMessage();
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            dialog.hide();

            if(isSuccessful){
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

                intent.putExtra("newUser", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }else{
                Toast.makeText(RegisterActivity.this,
                        "Register was not successful " + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}