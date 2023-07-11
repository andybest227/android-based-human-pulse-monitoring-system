package com.example.heartbeatratemonitor;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    //Declaration EditTexts
    EditText editTextUserName;
    EditText editTextEmail;
    EditText editTextPassword;

    //Declaration TextInputLayout
    TextInputLayout textInputLayoutUserName;
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;

    //Declaration Button
    Button buttonRegister;

    //Declaration SQLiteHelper
    SqliteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sqliteHelper = new SqliteHelper(this);
        initTextViewLogin();
        initViews();
        buttonRegister.setOnClickListener(view -> {
            if (validate()) {
                String UserName = editTextUserName.getText().toString();
                String Email = editTextEmail.getText().toString();
                String Password = editTextPassword.getText().toString();

                //Check in the database is there any user associated with this email?
                if (!sqliteHelper.isEmailExists(Email)) {
                    //Email does not exist now add new user to database
                    sqliteHelper.addUser(new User(null, UserName, Email, Password));
                    Snackbar.make(buttonRegister, "User created successfully! Please Login", Snackbar.LENGTH_LONG).show();
                    new Handler().postDelayed(this::finish, Snackbar.LENGTH_LONG);
                } else {
                    //Email exists with input provided so how error user already exists
                    Snackbar.make(buttonRegister, "User already exists with same email", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
    //this method used to set Login TextView click event
    private void initTextViewLogin(){
        TextView textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(view -> finish());
    }
    //this method is used to connect XML views to its Objects
    private void initViews(){
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextUserName = findViewById(R.id.editTextUserName);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutUserName = findViewById(R.id.textInputLayoutUserName);
        buttonRegister = findViewById(R.id.buttonRegister);
    }
    //This method is used to validate input given by user
    public boolean validate(){
        boolean valid;

        //Get values from Edittext fields
        String UserName = editTextUserName.getText().toString();
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();

        //Handling validation for userName field
        if (UserName.isEmpty()){
            textInputLayoutUserName.setError("Please enter valid username!");
        }else{
            if(UserName.length()>5){
                textInputLayoutUserName.setError(null);
            }else{
                textInputLayoutUserName.setError("Username is too short!");
            }
        }
        //Handling validation  for email field
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            textInputLayoutEmail.setError("Please enter valid email!");
        }else{
            textInputLayoutEmail.setError(null);
        }
        //Handling validation for Password field
        if (Password.isEmpty()){
            valid = false;
            textInputLayoutPassword.setError("Please enter valid password!");
        }else{
            if (Password.length() >5){
                valid = true;
                textInputLayoutPassword.setError(null);
            }else{
                valid = false;
                textInputLayoutPassword.setError("Password is too short!");
            }
        }
        return valid;
    }
}