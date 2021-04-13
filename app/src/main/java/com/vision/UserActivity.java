package com.vision;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import java.util.Locale;

public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    FusedLocationProviderClient fusedLocationProviderClient;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.logout3).setOnClickListener(this);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userId=user.getUid();

        //get the user name for searching the particular node
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("search");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                username =snapshot.child(userId).getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //monitor the change in the particular user
        mDatabase=FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(Integer.parseInt(snapshot.child(username).child("Accident").getValue().toString())==1){

                    String token =snapshot.child("Srinivasan").child("userToken").getValue().toString();
                    Log.d("Aadi","token is"+token);

                    //gps location
                    if(ActivityCompat.checkSelfPermission(UserActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                Location location = task.getResult();
                                double latitude =location.getLatitude();
                                double longitude = location.getLongitude();
                                String loc = "Accident latitude is: "+latitude+" and the longitude is: "+longitude;
                                FcmNotificationsSender notificationsSender=new FcmNotificationsSender("/topics/all","Location",loc,getApplicationContext(),UserActivity.this);
                                notificationsSender.SendNotifications();

                            }
                        });

                    }
                    else{
                        ActivityCompat.requestPermissions(UserActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
                    }

                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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