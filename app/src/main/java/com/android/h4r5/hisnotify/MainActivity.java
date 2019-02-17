package com.android.h4r5.hisnotify;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;


    String userONinfo;
    String userID;
    int tAppID =0;
    int nCounter=0;
    Intent rIntent;
    String loginTime;
    String tA,tB;

    SharedPreferences NotificationCounter;
    SharedPreferences NotificationAppID;
    SharedPreferences.Editor editNCounter;
    SharedPreferences.Editor editNAppID;

    SharedPreferences nData;
    SharedPreferences.Editor nDataEditor;

    //SharedPreferences sortNDATA;
    //SharedPreferences.Editor editorN_DATA;


    public static FragmentManager fragmentManager;



    @Override
    protected void onStart() {

        super.onStart();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //database = FirebaseDatabase.getInstance();
        NotificationCounter = getSharedPreferences("NotificationCounter",Context.MODE_PRIVATE);

        NotificationAppID = getSharedPreferences("NotificationAppID",Context.MODE_PRIVATE);

        //sortNDATA = getSharedPreferences("sortNDATA",Context.MODE_PRIVATE);

        editNCounter = NotificationCounter.edit();
        editNAppID = NotificationAppID.edit();
        //editorN_DATA = sortNDATA.edit();




        Intent intent1 = getIntent();



        rIntent = new Intent(MainActivity.this,NotificationListener.class);

        loginTime =intent1.getStringExtra("loginTime");

        if(loginTime.equals("just"))
        {
            if(NotificationListener.checkService.equals("connected"))
            {
                Intent harsh = new Intent("com.android.h4r5.hisnotify");
                harsh.putExtra("Stopper","Stop");

                sendBroadcast(harsh);




            }
            else if(NotificationListener.checkService.equals("disconnected"))
            {

            }
            else if(NotificationListener.checkService.equals(""))
            {

            }
            //
        }

        userONinfo = intent1.getStringExtra("UserInfo");
        userID = intent1.getStringExtra("UserID");

        nData = getSharedPreferences("nData",Context.MODE_PRIVATE);

        myRef =database.getReference();
        nDataEditor = nData.edit();

        nDataEditor.putString("O&N",userONinfo);
        nDataEditor.putString("UserID",userID);
        if(userONinfo.equals("NEW"))
        {
            nDataEditor.putInt("tAppID",tAppID);
            nDataEditor.putInt("nCount",nCounter);
            nDataEditor.apply();

            myRef = database.getReference().child(userID).child("Total App IDs");
            myRef.setValue(String.valueOf(tAppID));

            myRef = database.getReference().child(userID).child("Notification Count");
            myRef.setValue(String.valueOf(nCounter));
        }
        else if (userONinfo.equals("OLD"))
        {
            Log.e("This","Inside Old");

            ////////////////////////


            ////////////////////////////

            myRef = database.getReference().child(userID).child("Total App IDs");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    tAppID =  Integer.parseInt(dataSnapshot.getValue(String.class));
                    Log.e("This","Inside Old "+tAppID);
                    nDataEditor.putInt("tAppID",tAppID);
                    nDataEditor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            myRef = database.getReference().child(userID).child("Notification Count");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    nCounter = Integer.parseInt(dataSnapshot.getValue(String.class));
                    nDataEditor.putInt("nCount",nCounter);
                    nDataEditor.apply();
                    Log.e("This","Inside Old "+nCounter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.e("This","Inside Old "+nCounter+" "+tAppID);
        }


        ////////////////////////////////////////

        if(userONinfo.equals("OLD") && loginTime.equals("just"))
        {

            if(NotificationAppID!=null && NotificationCounter!=null  )
            {
                editNAppID.clear().apply();
                editNCounter.clear().apply();
               // editorN_DATA.clear().apply();
            }


            //////for ID
            myRef = database.getReference().child(userID).child("Notification ID");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot DS : dataSnapshot.getChildren())
                    {
                        editNAppID.putInt(DS.getKey(),DS.getValue(Integer.class));
                        editNAppID.apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //////for N Counts
            myRef = database.getReference().child(userID).child("Notification Counter");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot DS : dataSnapshot.getChildren())
                    {
                        editNCounter.putInt(DS.getKey(),DS.getValue(Integer.class));
                        editNCounter.apply();

                        nDataEditor.putInt(DS.getKey(),DS.getValue(Integer.class)+1);
                        nDataEditor.apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }


        /////////////////////////////////////////////


        Toast.makeText(getApplicationContext(),userONinfo,Toast.LENGTH_SHORT).show();

        /////////////////////////////////////////////

        fragmentManager = getSupportFragmentManager();

        LinearLayout homeBtn = findViewById(R.id.HomeBtn);
        LinearLayout AllNotyBtn = findViewById(R.id.AllNoty);
        LinearLayout ToolBar_profileBtn = findViewById(R.id.toolProfile);
        ImageView shareBtn = findViewById(R.id.shareBtn);
        ImageView acProfileBtn = findViewById(R.id.ac_profile);




        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeFragment homeFragment =new HomeFragment();
                fragmentManager.beginTransaction().replace(R.id.FragmentContainer,homeFragment,"homeFragment").commit();
            }
        });

        AllNotyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllNotiFragment allNotiFragment = new AllNotiFragment();
                fragmentManager.beginTransaction().replace(R.id.FragmentContainer,allNotiFragment,"allNotiFragment").commit();
            }
        });

        ToolBar_profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profile profile = new Profile();
                fragmentManager.beginTransaction().replace(R.id.FragmentContainer,profile,"profile").commit();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Share btn",Toast.LENGTH_SHORT).show();
            }
        });
        acProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profile profile = new Profile();
                fragmentManager.beginTransaction().replace(R.id.FragmentContainer,profile,"profile").commit();
            }
        });




        ///////////Fregment

        if(findViewById(R.id.FragmentContainer)!=null)
        {
            if(savedInstanceState!=null)
            {
                return;
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HomeFragment homeFragment = new HomeFragment();
            fragmentTransaction.add(R.id.FragmentContainer,homeFragment,"homeFragment");
            fragmentTransaction.commit();

        }


    }

    @Override
    public void onBackPressed() {

        Fragment frg = fragmentManager.findFragmentByTag("homeFragment");



        if(frg!=null)
        {
            super.onBackPressed();
        }
        else
        {
            HomeFragment homeFragment = new HomeFragment();
            fragmentManager.beginTransaction().replace(R.id.FragmentContainer,homeFragment,"homeFragment").commit();
        }



    }



    //////////////ArrToStr

    public String ArrToStr(List <String> a)
    {
        StringBuilder sB =new StringBuilder();
        for(String i : a)
        {
            sB.append(i);
            sB.append(",");
        }

        return sB.toString();
    }


}
