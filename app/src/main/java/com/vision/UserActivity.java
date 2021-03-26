package com.vision;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
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
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        mAuth = FirebaseAuth.getInstance();







        findViewById(R.id.logout3).setOnClickListener(this);
        //button for push notification
        findViewById(R.id.Notificationbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getting the data of the respective hospital and police station
                Log.d("Aadi","inside onclick");




                mDatabase=FirebaseDatabase.getInstance().getReference().child("users");
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Log.d("Aadi","inside dataSnapshot");

                        String token =snapshot.child("police1").child("userToken").getValue().toString();

                        Log.d("Aadi","token is"+token);

                        FcmNotificationsSender notificationsSender=new FcmNotificationsSender("/topics/all","TITLE","BODY",getApplicationContext(),UserActivity.this);

                        notificationsSender.SendNotifications();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }









    private void Logout() {
        Log.i("Srini","insidelogout");
        mAuth.signOut();
        finish();
        startActivity(new Intent(UserActivity.this, SignInActivity.class));
    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.logout3:
            {
                Log.i("Srini","inside the onclick listener");
                Logout();
            }
        }

    }
}