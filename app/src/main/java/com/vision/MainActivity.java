package com.vision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText editTextEmail, editTextPassword,editTextName,editTextAge,editTextVehicleRegNo;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        FirebaseMessaging.getInstance().subscribeToTopic("all");
        editTextEmail= findViewById(R.id.editTextEmail);
        editTextPassword= findViewById(R.id.editTextPassword);
        editTextName=findViewById(R.id.edittextname);
        editTextAge=findViewById(R.id.edittextage);
        editTextVehicleRegNo=findViewById(R.id.editTextVehicleRegNo);



        mAuth = FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.textviewlogin).setOnClickListener(this);

  }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser != null) {
            updateUI();
        }
    }

    private void updateUI() {
        startActivity(new Intent(MainActivity.this, UserActivity.class));
    }

    private void registerUser(){
        final String email = editTextEmail.getText().toString().trim();
        final String password=editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String age = editTextAge.getText().toString().trim();
        final String vehicleRegNo = editTextVehicleRegNo.getText().toString().trim();
        if(email.isEmpty())
        {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty())
        {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        //check password length
        if (password.length() < 6) {
            editTextPassword.setError("Minimum length of password should be 6");
            editTextPassword.requestFocus();
            return;
        }
        // String name =editTextEmail.getText().toString().trim();
        //mDatabase.push().setValue(name);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    finish();

                    Log.d("Aaditya","firebase authentication is over!...going towards token creation");

                    //token creation

                    // fcm settings for perticular user

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (task.isSuccessful()) {

                                        final String token = Objects.requireNonNull(task.getResult()).getToken();
                                        Log.d("Aaditya","token "+ token);
                                        validateCredentials(email,password,name,age,vehicleRegNo,token);
                                    }

                                }
                            });
                    Log.i("part","validation is over!");
//                    String email = editTextEmail.getText().toString().trim();
//                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
//                    ref.child(user.getUid()).setValue(email);


                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });



    }

    private void validateCredentials(final String email, final String password,final String name, final String age,final String vehicleRegNo,String tokena) {

        final DatabaseReference root;
        root=FirebaseDatabase.getInstance().getReference();
        Log.i("part","entered inside validateCredentials");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userId=user.getUid();


        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!(dataSnapshot.child("users").child(name).exists()))
                {
                    Log.i("part","entered inside if not exists!");
                    HashMap<String,Object> userdataMap= new HashMap<>();
                    userdataMap.put("email",email);
                    userdataMap.put("password",password);
                    //userdataMap.put("name",name);
                    userdataMap.put("age",age);
                    userdataMap.put("vehicleRegNo",vehicleRegNo);
                    Log.d("Aaditya","this is inside the datasnapshot " + tokena);
                    userdataMap.put("userToken", tokena);
                    userdataMap.put("Status", 0);




                    root.child("users").child(name).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(MainActivity.this, "account is created", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                    }

                                    else
                                    {
                                        Toast.makeText(MainActivity.this, "network error: please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
                else
                {
                    Toast.makeText(MainActivity.this, "this email already  exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("part","inside on cancelled database error!");

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button2:
            {
                registerUser();
            }
            break;

            case R.id.textviewlogin:
                startActivity(new Intent(this, SignInActivity.class ));
                break;
        }
    }


}