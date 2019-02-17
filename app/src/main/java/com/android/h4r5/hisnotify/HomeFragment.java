package com.android.h4r5.hisnotify;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    int tempD=0;
    private Button btn;
    private android.support.v7.widget.GridLayout ly;

    //private String TAG = this.getClass().getSimpleName();

    private NotificationReceiver nReceiver;

    SharedPreferences NotificationCounter;
    SharedPreferences NotificationAppID;
    SharedPreferences sortedNotificationID;
    SharedPreferences nData;
    SharedPreferences AppPackages;

    List<String> allAps;
    View view;
    View.OnClickListener testListner;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_home, container, false);

        ///////////////Strat from here


        NotificationCounter = this.getActivity().getSharedPreferences("NotificationCounter",Context.MODE_PRIVATE);
        NotificationAppID = this.getActivity().getSharedPreferences("NotificationAppID",Context.MODE_PRIVATE);
        sortedNotificationID = this.getActivity().getSharedPreferences("sortedNotificationID",Context.MODE_PRIVATE);
        nData = this.getActivity().getSharedPreferences("nData",Context.MODE_PRIVATE);
        AppPackages = this.getActivity().getSharedPreferences("AppPackages",Context.MODE_PRIVATE);
        /////////Strt from here


        //new ly
        ly = view.findViewById(R.id.ly2);
        ly.removeAllViews();

        if(sortedNotificationID.contains("AppIDs"))
        {
            String tempStr = sortedNotificationID.getString("AppIDs","");
            allAps = new ArrayList<>(Arrays.asList(tempStr.split(",")));
        }




        testListner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i;
                for(i=1;i<Integer.parseInt(allAps.get(0));i++)
                {
                    //Log.e("THis is harsh",allAps.get(i)+" "+view.getId());
                    //if(view.getId()==NotificationAppID.getInt(allAps.get(i),0))
                    if(view.getId()==NotificationAppID.getInt(allAps.get(i),0))
                    {
                        //////do something here


                        ///////////Fregment

                        Bundle args = new Bundle();
                        args.putString("AppName",allAps.get(i));
                        OnlyAppNotificationFragment onlyAppNotificationFragment= new OnlyAppNotificationFragment();
                        onlyAppNotificationFragment.setArguments(args);
                        MainActivity.fragmentManager.beginTransaction().replace(R.id.FragmentContainer,onlyAppNotificationFragment,"onlyAppNotificationFragment").commit();

                    }
                }
            }
        };









        if(sortedNotificationID.contains("AppIDs"))
        {

            //Log.i(TAG, " your here");

            String tempStr5 = sortedNotificationID.getString("AppIDs","");
            List<String> AppIDs = new ArrayList<>(Arrays.asList(tempStr5.split(",")));

            int i;
            for(i=1;i<AppIDs.size();i++)
            {
//                btn = new Button(this.getActivity());
//                int id = NotificationAppID.getInt(AppIDs.get(i),0);
//                btn.setId(id);
//                String NumberOfNoty = Integer.toString(NotificationCounter.getInt(AppIDs.get(i),0)-(nData.getInt(AppIDs.get(i),0)-1))+" "+AppIDs.get(i);
//                btn.setText(NumberOfNoty);
//                ly.addView(btn,0);
//                btn.setOnClickListener(testListner);

                appBtnCreator(AppIDs.get(i));

            }

        }

        ////////////////////////

        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.h4r5.hisnotify");
        view.getContext().registerReceiver(nReceiver,filter);

        //Log.i(TAG, " *************OnViewCreated");


        ///////
        return view;
    }


    @Override
    public void onDestroyView() {

        Context context = view.getContext();
        context.unregisterReceiver(nReceiver);
        super.onDestroyView();
    }



    //////////////App Creater//////////////

    public void appBtnCreator(String mAppName2)
    {
        View appBtnBody = getLayoutInflater().inflate(R.layout.app_btn_ly,null);

        android.support.v7.widget.GridLayout.LayoutParams params = new android.support.v7.widget.GridLayout.LayoutParams(android.support.v7.widget.GridLayout.spec(android.support.v7.widget.GridLayout.UNDEFINED,1f), android.support.v7.widget.GridLayout.spec(android.support.v7.widget.GridLayout.UNDEFINED,1f));
        appBtnBody.findViewById(R.id.rootLY).setLayoutParams(params);

        TextView AppName = (TextView) appBtnBody.findViewById(R.id.appNameID);
        TextView nCounter = (TextView) appBtnBody.findViewById(R.id.notyCounter);
        ImageView mAppIcon = appBtnBody.findViewById(R.id.appIconID);


        Drawable mIcon ;
        try {
            mIcon = view.getContext().getPackageManager().getApplicationIcon(AppPackages.getString(mAppName2,""));
            mAppIcon.setImageDrawable(mIcon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String nCounterData = Integer.toString(NotificationCounter.getInt(mAppName2,0)-(nData.getInt(mAppName2,0)-1));
        int id = NotificationAppID.getInt(mAppName2,0);

        appBtnBody.setId(id);
        appBtnBody.setOnClickListener(testListner);

        AppName.setText(mAppName2);
        nCounter.setText(nCounterData);


        ly.addView(appBtnBody,0);
    }


    /////////////////////

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String tempStr = sortedNotificationID.getString("AppIDs","");
            allAps = new ArrayList<>(Arrays.asList(tempStr.split(",")));


            String checkListnerConnection = intent.getStringExtra("ListnerConnected");

            if(checkListnerConnection.equals("yes"))
            {
                int i;
                ly.removeAllViews();
                for(i=1;i<allAps.size();i++)
                {
//                    btn = new Button(context);
//                    int id = NotificationAppID.getInt(allAps.get(i),0);
//                    btn.setId(id);
//                    String NumberOfNoty = Integer.toString(NotificationCounter.getInt(allAps.get(i),0)-(nData.getInt(allAps.get(i),0)-1))+" "+allAps.get(i);
//                    btn.setText(NumberOfNoty);
//                    btn.setOnClickListener(testListner);
//                    ly.addView(btn,0);

                    appBtnCreator(allAps.get(i));


                }

//                HomeFragment homeFragment =new HomeFragment();
//                MainActivity.fragmentManager.beginTransaction().replace(R.id.FragmentContainer,homeFragment,"homeFragment").commit();
            }
            else
            {
                String mAppName = allAps.get(allAps.size()-1);
                Log.e("This", mAppName + "****");
                String temp = "New";
                if(ly.getChildCount()>0)
                {
                    int j;
                    for(j=0;j<allAps.size()-2;j++)
                    {
                        //Log.e("This", String.valueOf(j));
                        if(NotificationAppID.getInt(mAppName,0)==ly.getChildAt(j).getId())
                        {
                            temp = "Old";
                        }
                    }
                }



                if(temp.equals("New"))
                {
                    // Log.e("This", mAppName + "Created");
//                    btn = new Button(context);
//                    btn.setId(NotificationAppID.getInt(mAppName,0));
//                    String NumberOfNoty = Integer.toString(NotificationCounter.getInt(mAppName,0)-(nData.getInt(mAppName,0)-1))+" "+mAppName;
//                    btn.setText(NumberOfNoty);
//                    btn.setOnClickListener(testListner);
//                    ly.addView(btn,0);

                    appBtnCreator(mAppName);
                }
                else if(temp.equals("Old"))
                {
//                    btn = view.findViewById(NotificationAppID.getInt(mAppName,0));
//
//
//                    ly.removeView(btn);
//
//                    String NumberOfNoty = Integer.toString(NotificationCounter.getInt(mAppName,0)-(nData.getInt(mAppName,0)-1))+" "+mAppName;
//                    btn.setText(NumberOfNoty);
//                    ly.addView(btn,0);


                    View appBtnBody2 = view.findViewById(NotificationAppID.getInt(mAppName,0));
                    ly.removeView(appBtnBody2);


                    TextView AppName = (TextView) appBtnBody2.findViewById(R.id.appNameID);
                    TextView nCounter = (TextView) appBtnBody2.findViewById(R.id.notyCounter);
                    ImageView mAppIcon = appBtnBody2.findViewById(R.id.appIconID);

                    Drawable mIcon ;
                    try {
                        mIcon = view.getContext().getPackageManager().getApplicationIcon(AppPackages.getString(mAppName,""));
                        mAppIcon.setImageDrawable(mIcon);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    String nCounterData = Integer.toString(NotificationCounter.getInt(mAppName,0)-(nData.getInt(mAppName,0)-1));


                    AppName.setText(mAppName);
                    nCounter.setText(nCounterData);
                    appBtnBody2.setOnClickListener(testListner);

                    ly.addView(appBtnBody2,0);


                }
            }





           // Log.i(TAG, allAps+" ************");

        }
    }

}
