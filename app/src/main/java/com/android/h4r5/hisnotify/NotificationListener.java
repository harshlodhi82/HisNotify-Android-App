package com.android.h4r5.hisnotify;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import static java.text.DateFormat.SHORT;

public class NotificationListener extends NotificationListenerService {

    Bundle extraBudle ;
    SharedPreferences NotificationAppID; // TODO: 25/01/2019 takes [AppName = int ID]
    SharedPreferences NotificationCounter; // TODO: 25/01/2019 takes [AppName = int count]
    SharedPreferences sortedNotificationID;
    SharedPreferences nData;
    SharedPreferences AppPackages;

    NLServiceReceiver nReceiver;


    public static String checkService = "";


    ////
    FirebaseAuth mAuth;
    FirebaseUser currentUser ;
    String UserID;


    ///////////TINy DB
    TinyDB DataContainerFile;  // TODO: 25/01/2019 [AppName : 0 , Time : 1, Date : 2, Title : 3, Text : 4, BigText : 5]
    ArrayList<String> NotyData; // TODO: 25/01/2019 AppName + Notification_Counter
    int nCounter;
    String strForAllNotifyFrag = "";

////////////////////Firebase DataBase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef =database.getReference();

    int appID;


    /////


    int notyCounter;

    Intent dataSender;
    String ongoingCreator;
    String NewOld="";
    String NewOld_forNotOngoig="";
    boolean wasRemoved;

    List<String> AppIDs;
    ///
    private String TAG = this.getClass().getSimpleName();

    /////HashMAp




    @Override
    public void onCreate() {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        if(currentUser!=null)
        {
            strForAllNotifyFrag = "";

            ///////////Data COnatainer File
            DataContainerFile = new TinyDB(this);
            DataContainerFile.putInt("nCounter",0);

            //Log.i(TAG, "***************************"+DataContainerFile.getInt("CLickCount") );

            dataSender = new Intent("com.android.h4r5.hisnotify");


            NotificationCounter = getSharedPreferences("NotificationCounter",Context.MODE_PRIVATE);

            NotificationAppID = getSharedPreferences("NotificationAppID",Context.MODE_PRIVATE);

            sortedNotificationID = getSharedPreferences("sortedNotificationID",Context.MODE_PRIVATE);

            AppPackages = getSharedPreferences("AppPackages",Context.MODE_PRIVATE);


            nData = getSharedPreferences("nData",Context.MODE_PRIVATE);
            UserID = nData.getString("UserID","");
            appID = nData.getInt("tAppID",0);
            notyCounter = nData.getInt("nCount",0);

        }


        nReceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.h4r5.hisnotify");
        registerReceiver(nReceiver,filter);

        super.onCreate();
    }



    /////////////////////Get Active Posted Notification////////////////////////
    @Override
    public void onListenerConnected() {


        ////////temp
        checkService = "connected";


        //Log.i(TAG, "**************Listener is connected");


        if(currentUser!=null)
        {

            nData = getSharedPreferences("nData",Context.MODE_PRIVATE);
            SharedPreferences.Editor nDataEditor = nData.edit();
            UserID = nData.getString("UserID","");
            appID = nData.getInt("tAppID",0);
            notyCounter = nData.getInt("nCount",0);



            //SharedPreferences.Editor editorN_DATA = sortNDATA.edit();
            SharedPreferences.Editor editorNotificationCounter1 = NotificationCounter.edit();
            SharedPreferences.Editor editorNotificationAppID1 = NotificationAppID.edit();
            SharedPreferences.Editor editorSorter1 = sortedNotificationID.edit();
            SharedPreferences.Editor editorPackage = AppPackages.edit();


            List<String> NotiID = new ArrayList<>();


            if(sortedNotificationID.contains("AppIDs"))
            {
                AppIDs = new ArrayList<>();
                String tempStr = sortedNotificationID.getString("AppIDs","");
                String tempNotiID = sortedNotificationID.getString("NotiID","");
                AppIDs.addAll(Arrays.asList(tempStr.split(",")));
                NotiID.addAll(Arrays.asList(tempNotiID.split(",")));

            }
            else
            {
                AppIDs = new ArrayList<>();
                AppIDs.add(0,"0");
                editorSorter1.putString("AppIDs", ArrToStr(AppIDs));
                editorSorter1.apply();

                NotiID.add(0,"0");
                editorSorter1.putString("NotiID", ArrToStr(NotiID));
                editorSorter1.apply();

                // Log.i(TAG, "***New AppID is Created******"+AppIDs+" "+AppIDs.size());
            }
            //////////////////////////////////
            StatusBarNotification [] activeNoti = getActiveNotifications();

            int i;
            for(i=0;i<activeNoti.length;i++)
            {

                strForAllNotifyFrag = "";
                StatusBarNotification ActSBN = activeNoti[i];

                //////////////////////////////////////////////
                //HashMap<String,String> dataContainer = new HashMap<>();

                String appName;
                String packageName = ActSBN.getPackageName();
                PackageManager packageManager= getApplicationContext().getPackageManager();
                try {
                    appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                } catch (PackageManager.NameNotFoundException e) {
                    appName="Removed App";
                    e.printStackTrace();
                }


                /////////////////////////////////////////////////


//                Log.e("This",eachApp_NCounter+"*******************");


                //////////////////////////////////////////////////
                String HoursMinutes = DateFormat.getTimeInstance(SHORT).format(new Date());
                String date = DateFormat.getDateInstance(SHORT).format(new Date());
                extraBudle = ActSBN.getNotification().extras;

                String NotificationTitle = " ";
                String NotificationText= " ";
                String NotificationBigText = " ";

                CharSequence TextChar = extraBudle.getCharSequence("android.text");
                CharSequence TitleChar = extraBudle.getCharSequence("android.title");
                CharSequence BigTextChar = extraBudle.getCharSequence("android.bigText");
                if(TextChar!=null)
                {
                    NotificationText = TextChar.toString();
                }
                if(TitleChar!=null)
                {
                    NotificationTitle = TitleChar.toString();
                }
                if(BigTextChar!=null)
                {
                    NotificationBigText = BigTextChar.toString();
                }




                if(ActSBN.isOngoing())
                {

                    if(!NotificationAppID.contains(appName))
                    {
                        appID=appID+1;



                        editorPackage.putString(appName,ActSBN.getPackageName()).apply();
                        editorNotificationAppID1.putInt(appName, appID);
                        editorNotificationAppID1.apply();
                        editorNotificationCounter1.putInt(appName, 1);
                        editorNotificationCounter1.apply();

                        nDataEditor.putInt(appName,NotificationCounter.getInt(appName,0)).apply();



                        AppIDs.add(1,appName);
                        NotiID.add(1,String.valueOf(ActSBN.getId()));


                        ///appID
                        myRef = database.getReference(UserID).child("Notification ID").child(appName);
                        myRef.setValue(appID);
                        ///app Noty Counter
                        myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                        myRef.setValue(1);

                        NotyData = new ArrayList<>();
                        NotyData.add(appName);
                        NotyData.add(HoursMinutes);
                        NotyData.add(date);
                        NotyData.add(NotificationTitle);
                        NotyData.add(NotificationText);
                        NotyData.add(NotificationBigText);
                        nCounter = DataContainerFile.getInt("nCounter")+1;
                        DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                        DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                        myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                        myRef.setValue(NotyData);

                        notyCounter = notyCounter+1;
                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                        myRef.setValue(String.valueOf(notyCounter));

                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                        myRef.setValue(appName+NotificationCounter.getInt(appName,0));


                        strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);
                        //myRef.push(NotyData);
                        ///
                    }
                    else if(!NotiID.contains(String.valueOf(ActSBN.getId())))
                    {
                        AppIDs.remove(appName);
                        AppIDs.add(1,appName);


                        NotiID.remove(String.valueOf(ActSBN.getId()));
                        NotiID.add(1,String.valueOf(ActSBN.getId()));

                        myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                        myRef.setValue(NotificationCounter.getInt(appName,1)+1);

                        editorNotificationCounter1.putInt(appName, NotificationCounter.getInt(appName,1)+1);
                        editorNotificationCounter1.apply();

                        ///app Noty Counter

                        NotyData = new ArrayList<>();
                        NotyData.add(appName);
                        NotyData.add(HoursMinutes);
                        NotyData.add(date);
                        NotyData.add(NotificationTitle);
                        NotyData.add(NotificationText);
                        NotyData.add(NotificationBigText);
                        nCounter = DataContainerFile.getInt("nCounter")+1;
                        DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                        DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                        myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                        myRef.setValue(NotyData);

                        notyCounter = notyCounter+1;
                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                        myRef.setValue(String.valueOf(notyCounter));

                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                        myRef.setValue(appName+NotificationCounter.getInt(appName,0));

                        strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);

                        /////////////
                    }


                }
                else
                {

                    if(!NotificationAppID.contains(appName))
                    {
                        appID=appID+1;



                        editorPackage.putString(appName,ActSBN.getPackageName()).apply();
                        editorNotificationCounter1.putInt(appName, 1);
                        editorNotificationCounter1.apply();
                        editorNotificationAppID1.putInt(appName, appID);
                        editorNotificationAppID1.apply();

                        nDataEditor.putInt(appName,NotificationCounter.getInt(appName,0)).apply();

                        AppIDs.add(1,appName);
                        NotiID.add(1,String.valueOf(ActSBN.getId()));

                        /////

                        ///appID
                        myRef = database.getReference(UserID).child("Notification ID").child(appName);
                        myRef.setValue(appID);
                        ///app Noty Counter
                        myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                        myRef.setValue(1);

                        NotyData = new ArrayList<>();
                        NotyData.add(appName);
                        NotyData.add(HoursMinutes);
                        NotyData.add(date);
                        NotyData.add(NotificationTitle);
                        NotyData.add(NotificationText);
                        NotyData.add(NotificationBigText);
                        nCounter = DataContainerFile.getInt("nCounter")+1;
                        DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                        DataContainerFile.putListString(appName+String.valueOf(NotificationCounter.getInt(appName,0)),NotyData);
                        myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                        myRef.setValue(NotyData);

                        notyCounter = notyCounter+1;
                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                        myRef.setValue(String.valueOf(notyCounter));

                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                        myRef.setValue(appName+NotificationCounter.getInt(appName,0));


                        strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);
                        ///
                    }
                    else if(!NotiID.contains(String.valueOf(ActSBN.getId())))
                    {
                        AppIDs.remove(appName);
                        AppIDs.add(1,appName);


                        NotiID.remove(String.valueOf(ActSBN.getId()));
                        NotiID.add(1,String.valueOf(ActSBN.getId()));

                        Log.e("This","****************"+NotificationCounter.getInt(appName,1));

                        myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                        myRef.setValue(NotificationCounter.getInt(appName,1)+1);

                        editorNotificationCounter1.putInt(appName, NotificationCounter.getInt(appName,1)+1);
                        editorNotificationCounter1.apply();


                        ///
                        ///app Noty Counter


                        NotyData = new ArrayList<>();
                        NotyData.add(appName);
                        NotyData.add(HoursMinutes);
                        NotyData.add(date);
                        NotyData.add(NotificationTitle);
                        NotyData.add(NotificationText);
                        NotyData.add(NotificationBigText);
                        nCounter = DataContainerFile.getInt("nCounter")+1;
                        DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                        DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                        myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                        myRef.setValue(NotyData);
                        notyCounter = notyCounter+1;
                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                        myRef.setValue(String.valueOf(notyCounter));

                        myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                        myRef.setValue(appName+NotificationCounter.getInt(appName,0));

                        strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);
                        ///
                    }

                }

                AppIDs.remove(0);
                AppIDs.add(0,String.valueOf(AppIDs.size()+1));
                editorSorter1.putString("AppIDs", ArrToStr(AppIDs));
                editorSorter1.apply();


                NotiID.remove(0);
                NotiID.add(0,String.valueOf(NotiID.size()+1));
                editorSorter1.putString("NotiID",ArrToStr(NotiID));
                editorSorter1.apply();

                editorNotificationAppID1.putInt("appID", appID);
                editorNotificationAppID1.apply();

                //SharedPreferences.Editor nDataEditor = nData.edit();
                nDataEditor.putInt("tAppID",appID);
                nDataEditor.putInt("nCount",notyCounter);
                nDataEditor.apply();

                myRef = database.getReference(UserID).child("Total App IDs");
                myRef.setValue(String.valueOf(appID));
                myRef = database.getReference(UserID).child("Notification Count");
                myRef.setValue(String.valueOf(notyCounter));



                dataSender.putExtra("ListnerConnected","yes");

                Log.e("This",  appName + " sent");



                //////////
                //String saveToDCstr = HashToStr(dataContainer);
//            editorDC.putString(appName,saveToDCstr);
//            editorDC.apply();

                //Log.i(TAG, dataContainer+"*********************************");

                //////////////////////////////////////////////
            }


            sendBroadcast(dataSender);



        }
        else
        {

            Toast.makeText(getApplicationContext(),"Please Login 1st",Toast.LENGTH_SHORT).show();

        }


    }


    @Override
    public void onListenerDisconnected() {

        checkService = "disconnected";
        Log.e("This ","Disconnected");
        super.onListenerDisconnected();
    }


    @Override
    public StatusBarNotification[] getActiveNotifications() {
        return super.getActiveNotifications();
    }

    ////////////////////////////////////////////////////////////////////
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        ///////////////////////-----------Main Notification Program----------//////////////////////////////
        Log.e("This ","Service is already running222222222222");
        if(currentUser!=null)
        {

            nData = getSharedPreferences("nData",Context.MODE_PRIVATE);
            SharedPreferences.Editor nDataEditor = nData.edit();

            UserID = nData.getString("UserID","");
            appID = nData.getInt("tAppID",0);
            notyCounter = nData.getInt("nCount",0);

            strForAllNotifyFrag = "";


            /////after Listner Connected
            dataSender.putExtra("ListnerConnected","no");

            ////file creator

            //SharedPreferences.Editor editorN_DATA = sortNDATA.edit();
            SharedPreferences.Editor editorNotificationCounter = NotificationCounter.edit();
            SharedPreferences.Editor editorNotificationAppID = NotificationAppID.edit();
            SharedPreferences.Editor editorSorter = sortedNotificationID.edit();
            SharedPreferences.Editor editorPackage = AppPackages.edit();

            //////////

            ///Date and Time
            String HoursMinutes = DateFormat.getTimeInstance(SHORT).format(new Date());
            String date = DateFormat.getDateInstance(SHORT).format(new Date());
            String Time = HoursMinutes+"\n"+date;


            extraBudle = sbn.getNotification().extras;

            String NotificationTitle = " ";
            String NotificationText=  " ";
            String NotificationBigText = " ";

            CharSequence TextChar = extraBudle.getCharSequence("android.text");
            CharSequence TitleChar = extraBudle.getCharSequence("android.title");
            CharSequence BigTextChar = extraBudle.getCharSequence("android.bigText");
            if(TextChar!=null)
            {
                NotificationText = TextChar.toString();
            }
            if(TitleChar!=null)
            {
                NotificationTitle = TitleChar.toString();
            }
            if(BigTextChar!=null)
            {
                NotificationBigText = BigTextChar.toString();
            }


            //Log.i(TAG, "************************"+extraBudle);

            dataSender.putExtra("NotificationText",NotificationText);

            dataSender.putExtra("NotificationTitle",NotificationTitle);
            String packageName = sbn.getPackageName();
            dataSender.putExtra("packageName",packageName);
            dataSender.putExtra("PostedTime",Time);
            ///////////////////////---------------------//////////////////////////////

            ////App Name
            String appName;
            PackageManager packageManager= getApplicationContext().getPackageManager();
            try {
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                appName="Removed App";
                e.printStackTrace();
            }

            dataSender.putExtra("appName",appName);





            if(sbn.isOngoing())
            {
                ongoingCreator = appName+"IsOngoing";
                dataSender.putExtra("checkOngoing",ongoingCreator);
                // setDataForOngoing(appName);
                if(!NotificationAppID.contains(appName))
                {
                    NewOld = "new";
                    appID=appID+1;

                    ///////


                    editorPackage.putString(appName,sbn.getPackageName()).apply();
                    editorNotificationAppID.putInt(appName, appID);
                    editorNotificationAppID.apply();
                    editorNotificationCounter.putInt(appName, 1);
                    editorNotificationCounter.apply();

                    nDataEditor.putInt(appName,NotificationCounter.getInt(appName,0)).apply();

                    AppIDs.add(appName);


                    wasRemoved=false;

                    ///

                    ///appID
                    myRef = database.getReference(UserID).child("Notification ID").child(appName);
                    myRef.setValue(appID);
                    ///app Noty Counter
                    myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                    myRef.setValue(1);

                    NotyData = new ArrayList<>();
                    NotyData.add(appName);
                    NotyData.add(HoursMinutes);
                    NotyData.add(date);
                    NotyData.add(NotificationTitle);
                    NotyData.add(NotificationText);
                    NotyData.add(NotificationBigText);
                    NotyData.add(NotificationText);
                    nCounter = DataContainerFile.getInt("nCounter")+1;
                    DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                    DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                    myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                    myRef.setValue(NotyData);


                    notyCounter = notyCounter+1;
                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                    myRef.setValue(String.valueOf(notyCounter));

                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                    myRef.setValue(appName+NotificationCounter.getInt(appName,0));


                    strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);
                    ////////
                }
                else if(wasRemoved)
                {
                    AppIDs.remove(appName);
                    AppIDs.add(appName);


                    myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                    myRef.setValue(NotificationCounter.getInt(appName,1)+1);

                    editorNotificationCounter.putInt(appName, NotificationCounter.getInt(appName,1)+1);
                    editorNotificationCounter.apply();




                    NewOld = "new";
                    //Log.i(TAG, "**************WasRemove ran");
                    wasRemoved=false;
                    ///

                    ///app Noty Counter


                    NotyData = new ArrayList<>();
                    NotyData.add(appName);
                    NotyData.add(HoursMinutes);
                    NotyData.add(date);
                    NotyData.add(NotificationTitle);
                    NotyData.add(NotificationText);
                    NotyData.add(NotificationBigText);
                    nCounter = DataContainerFile.getInt("nCounter")+1;
                    DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                    DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                    myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                    myRef.setValue(NotyData);


                    notyCounter = notyCounter+1;
                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                    myRef.setValue(String.valueOf(notyCounter));

                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                    myRef.setValue(appName+NotificationCounter.getInt(appName,0));


                    strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);

                    ///
                }
                else
                {

                    NewOld="old";
                    wasRemoved=false;
                }
                dataSender.putExtra("NewOld",NewOld);

            }
            else
            {
                ongoingCreator = appName+"IsNotOngoing";
                dataSender.putExtra("checkOngoing",ongoingCreator);
                //setDataForNotOngoing(appName);
                if(!NotificationAppID.contains(appName))
                {
                    appID=appID+1;
                    NewOld_forNotOngoig="new";



                    ///////




                    editorPackage.putString(appName,sbn.getPackageName()).apply();
                    editorNotificationCounter.putInt(appName, 1);
                    editorNotificationCounter.apply();

                    nDataEditor.putInt(appName,NotificationCounter.getInt(appName,0)).apply();

                    editorNotificationAppID.putInt(appName, appID);
                    editorNotificationAppID.apply();

                    AppIDs.add(appName);

                    //sortNDataList.remove(appName);
                    //sortNDataList.add(0,appName);
                    // Log.i(TAG, appName+" added to "+AppIDs+" "+AppIDs.size());
                    //ArrAppNameID.add(appName);


                    //Log.i(TAG, appName+" is not ongoing");
                    //Log.i(TAG, String.valueOf(NotificationAppID.getAll()));

                    ///
                    ///appID
                    myRef = database.getReference(UserID).child("Notification ID").child(appName);
                    myRef.setValue(appID);
                    ///app Noty Counter
                    myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                    myRef.setValue(1);

                    NotyData = new ArrayList<>();
                    NotyData.add(appName);
                    NotyData.add(HoursMinutes);
                    NotyData.add(date);
                    NotyData.add(NotificationTitle);
                    NotyData.add(NotificationText);
                    NotyData.add(NotificationBigText);
                    nCounter = DataContainerFile.getInt("nCounter")+1;
                    DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                    DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                    myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                    myRef.setValue(NotyData);


                    notyCounter = notyCounter+1;
                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                    myRef.setValue(String.valueOf(notyCounter));

                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                    myRef.setValue(appName+NotificationCounter.getInt(appName,0));

//                    editorS_DATA.putString(String.valueOf(0),String.valueOf(notyCounter));
//                    editorS_DATA.putString(String.valueOf(notyCounter),appName+NotificationCounter.getInt(appName,0));
//                    editorS_DATA.apply();

                    strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);
                    ///
                    ////////
                }
                else
                {
                    NewOld_forNotOngoig="old";
                    AppIDs.remove(appName);
                    AppIDs.add(appName);

                    ///sortNDataList.remove(appName);
                    //sortNDataList.add(0,appName);





                    myRef = database.getReference(UserID).child("Notification Counter").child(appName);
                    myRef.setValue(NotificationCounter.getInt(appName,1)+1);

                    editorNotificationCounter.putInt(appName, NotificationCounter.getInt(appName,1)+1);
                    editorNotificationCounter.apply();

                    //dataContainer.put("NotificationCounter",String.valueOf(Integer.parseInt(dataContainer.get("NotificationCounter"))+1));


                    //Log.i(TAG, "not new Noti "+appName+" "+String.valueOf(NotificationCounter.getAll())+"\n\n\n");
                    //apps_HashMap.put(appName, apps_HashMap.get(appName)+1);
                    ///

                    ///app Noty Counter


                    NotyData = new ArrayList<>();
                    NotyData.add(appName);
                    NotyData.add(HoursMinutes);
                    NotyData.add(date);
                    NotyData.add(NotificationTitle);
                    NotyData.add(NotificationText);
                    NotyData.add(NotificationBigText);
                    nCounter = DataContainerFile.getInt("nCounter")+1;
                    DataContainerFile.putInt("nCounter",DataContainerFile.getInt("nCounter")+1);
                    DataContainerFile.putListString(appName+NotificationCounter.getInt(appName,0),NotyData);
                    myRef = database.getReference(UserID).child("All Notification").child(appName+NotificationCounter.getInt(appName,0));
                    myRef.setValue(NotyData);


                    notyCounter = notyCounter+1;
                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(0));
                    myRef.setValue(String.valueOf(notyCounter));

                    myRef = database.getReference(UserID).child("Sorted Notification").child(String.valueOf(notyCounter));
                    myRef.setValue(appName+NotificationCounter.getInt(appName,0));

//                    editorS_DATA.putString(String.valueOf(0),String.valueOf(notyCounter));
//                    editorS_DATA.putString(String.valueOf(notyCounter),appName+NotificationCounter.getInt(appName,0));
//                    editorS_DATA.apply();

                    strForAllNotifyFrag = appName+NotificationCounter.getInt(appName,0);

                    ///

                }

                dataSender.putExtra("NewOld_forNotOngoig",NewOld_forNotOngoig);
            }

            dataSender.putExtra("AppNameForAllF",strForAllNotifyFrag);

            AppIDs.remove(0);
            AppIDs.add(0,String.valueOf(AppIDs.size()+1));
            editorSorter.putString("AppIDs", ArrToStr(AppIDs));
            editorSorter.apply();




            editorNotificationAppID.putInt("appID", appID);
            editorNotificationAppID.apply();

//////////////////////
            //SharedPreferences.Editor nDataEditor = nData.edit();
            nDataEditor.putInt("tAppID",appID);
            nDataEditor.putInt("nCount",notyCounter);
            nDataEditor.apply();

            myRef = database.getReference(UserID).child("Total App IDs");
            myRef.setValue(String.valueOf(appID));
            myRef = database.getReference(UserID).child("Notification Count");
            myRef.setValue(String.valueOf(notyCounter));
            dataSender.putExtra("Test","noTest");
            sendBroadcast(dataSender);

            //////////

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


    /////////HashMap to STR
//    public String HashToStr(HashMap<String,String> hashMap)
//    {
//        String convertedStr;
//        convertedStr = hashMap.toString();
//        return convertedStr;
//    }
//
//    /////////String to HashMap
//    public HashMap<String,String> SrtToHashmap(String str)
//    {
//        // TODO: 25/01/2019 ////////// Convert String to String[]
//        HashMap<String,String> hashMap = new HashMap<>();
//        ArrayList<String> tmp = new ArrayList<>();
//        StringBuilder sb = new StringBuilder(str);
//        sb.deleteCharAt(0);
//        sb.deleteCharAt(sb.length()-1);
//        String a = sb.toString();
//
//        tmp.addAll(Arrays.asList(a.split(", ")));
//        int i;
//        for(i=0;i<tmp.size();i++)
//        {
//
//            String [] tmpStr = tmp.get(i).split("=");
//            hashMap.put(tmpStr[0],tmpStr[1]);
//        }
//        //Log.i(TAG, hashMap.get("1")+"*********************************");
//        return hashMap;
//
//    }

    ////////////Data Set//////////////////////////////////////////////////



    @Override
    public void onDestroy() {
        super.onDestroy();
        checkService = "distroyed";
        unregisterReceiver(nReceiver);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if(sbn.isOngoing())
        {
            //NewOld = "new";
            wasRemoved=true;
            dataSender.putExtra("NewOld","removed");
            sendBroadcast(dataSender);



        }
        super.onNotificationRemoved(sbn);
    }


    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            String stopper= intent.getStringExtra("Stopper");
//            Log.e("This ","Hey harsh recieved bc");

            if(stopper!=null)
            {
                if(stopper.equals("Stop"))
                {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        requestUnbind();
                        tryReconnectService();

                    }
                }
            }


            ///////////do something here


        }
    }

    //////////reconnector
    public void tryReconnectService() {
        //It say to Notification Manager RE-BIND your service to listen notifications again inmediatelly!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ComponentName componentName = new ComponentName(getApplicationContext(), NotificationListener.class);
            requestRebind(componentName);
        }
    }


}
