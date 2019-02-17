package com.android.h4r5.hisnotify;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MyPopUp {

    public Dialog myDialog;
    private Context myContext;
    public String checker = "";



    public MyPopUp(Context context) {
        myContext = context;
        myDialog = new Dialog(context);
    }

    public void showPopUp()
    {
        myDialog.show();
    }

    public void getAllNotyPopUp(String dAppName, String dTime, String dDate,String dTitle, String dText, String dBigText, String dPackageName)
    {

        final String app_name =  dAppName;
        final String app_date =  dDate;
        final String app_time =  dTime;
        final String app_title =  dTitle;
        final String app_pck_name =  dPackageName;


        final String app_noty_data =  dPackageName+"\n\n"+dAppName+"\n\n"+dTime+"\n\n"+dDate+"\n\n"+ dTitle+"\n\n"+dText+"\n\n"+dBigText;

        myDialog.setContentView(R.layout.show_notification_data);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //////////////////


        LinearLayout blockBtn = myDialog.findViewById(R.id.data_Block);
        LinearLayout deleteBtn = myDialog.findViewById(R.id.data_Delete);
        LinearLayout downloadBtn = myDialog.findViewById(R.id.data_Download);

        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                blockNoty(app_pck_name);

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                daleteNoty();

            }
        });

        final String data = dAppName;

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                downloadNoty(app_name,app_date,app_time,app_title,app_noty_data);

            }
        });


        TextView appname = myDialog.findViewById(R.id.data_nAppName);
        TextView time = myDialog.findViewById(R.id.data_nTime);
        TextView date = myDialog.findViewById(R.id.data_nDate);
        TextView title = myDialog.findViewById(R.id.data_title);
        TextView text = myDialog.findViewById(R.id.data_nText);
        TextView bigtext = myDialog.findViewById(R.id.data_nBigText);
        ImageView appIcon = myDialog.findViewById(R.id.appIconData);


        if(dTitle.equals(" ") && dText.equals(" ") && dBigText.equals(" "))
        {
            dTitle = "No Data";
            text.setVisibility(View.GONE);
            bigtext.setVisibility(View.GONE);
        }
        else
        {
            if(dTitle.equals(" "))
            {
                title.setVisibility(View.GONE);
            }
            if(dText.equals(" "))
            {
                text.setVisibility(View.GONE);
            }
            if(dBigText.equals(" "))
            {
                bigtext.setVisibility(View.GONE);
            }
            if(dText.equals(dBigText))
            {
                bigtext.setVisibility(View.GONE);
            }
        }


        appname.setText(dAppName);
        time.setText(dTime);
        date.setText(dDate);
        title.setText(dTitle);
        text.setText(dText);
        bigtext.setText(dBigText);

        Drawable mIcon ;
        try {
            mIcon = myContext.getPackageManager().getApplicationIcon(dPackageName);
            appIcon.setImageDrawable(mIcon);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }





    }

    public void filterBtnPopUp(List<String> list)
    {

        myDialog.setContentView(R.layout.filter_btn);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //LinearLayout linearLayout = myDialog.findViewById(R.id.temply);
        Button bAll = myDialog.findViewById(R.id.fBtn_All);
        bAll.setTag("All");
        Button bToday = myDialog.findViewById(R.id.fBtn_Todgy);
        bToday.setTag("Today");



        GridLayout gridLayout = myDialog.findViewById(R.id.fLy);


        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = (Drawable) myContext.getResources().getDrawable(R.drawable.bg_round_corner,null);
        }else {
            drawable = ResourcesCompat.getDrawable(myContext.getResources(),R.drawable.bg_round_corner,null);

        }


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checker = (String) view.getTag();
                myDialog.cancel();

            }
        };

        bAll.setOnClickListener(onClickListener);
        bToday.setOnClickListener(onClickListener);

        Log.e("This is List",String.valueOf(list));

        for (String i : list)
        {
            Button btn = new Button(myContext);
            btn.setBackground(drawable);
            btn.setTag(i);
            btn.setText(i);
            btn.setTextColor(ResourcesCompat.getColor(myContext.getResources(),R.color.colorPrimary,null));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f));
            params.width = 0;
            params.setMargins(10,10,10,10);
            btn.setLayoutParams(params);
            btn.setOnClickListener(onClickListener);
            gridLayout.addView(btn);
            //Log.e("This is Extras",i+" Created , Child count: "+ gridLayout.getChildCount());
        }


    }


    private void blockNoty(String packageName)
    {
        Intent intent = new Intent();


        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE",packageName);
        }
        else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            //mIcon = myContext.getPackageManager().getApplicationIcon(dPackageName);
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            int uid = 0;
            try {
                uid = myContext.getPackageManager().getApplicationInfo(packageName,0).uid;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            intent.putExtra("app_package",packageName);
            intent.putExtra("app_uid",uid);
        }
        else
        {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:"+packageName));
        }

        myContext.startActivity(intent);
    }

    private void daleteNoty()
    {

    }

    private void downloadNoty(String an, String dt, String tme, String ttl, String data)
    {
        dt = dt.replaceAll("/","-");
        tme = tme.replace(':','-');
        int a =ttl.hashCode();
        String fileName = an+"-"+dt+"-"+tme+"-"+a;

        File filePath = new File(Environment.getExternalStorageDirectory(),"HisNotify/"+an);
        if(!filePath.exists())
        {
            filePath.mkdir();
        }



        try {
            File myFile = new File(filePath,fileName+".txt");

            myFile.createNewFile();
            FileWriter writer = new FileWriter(myFile);
            writer.append(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
