package com.android.h4r5.hisnotify;


import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OnlyAppNotificationFragment extends Fragment{

    String AppName;
    TinyDB DataContainerFile;
    SharedPreferences NotificationCounter; // TODO: 25/01/2019 takes [AppName = int count]
    SharedPreferences nData;
    SharedPreferences AppPackages;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FragmentManager fragmentManager;
    Fragment fragment;
    private String key;
    private List<String> myFList;
    private long dsCounter =0;
    private long dsCounted =0;

    private NotificationReceiver nReceiver;
    View view;

    //LinearLayout nLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private List<NotificationObj> nList;
    private List<NotificationObj> servList;

    private RecyclerAdapter recyclerAdapter;

    private EditText editText;
    private ImageView filterBtn;
    private MyPopUp myPopUp;











    public OnlyAppNotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_only_app_notification, container, false);
        /////////////
        NotificationCounter = this.getActivity().getSharedPreferences("NotificationCounter",Context.MODE_PRIVATE);
        nData = this.getActivity().getSharedPreferences("nData",Context.MODE_PRIVATE);
        AppPackages = this.getActivity().getSharedPreferences("AppPackages",Context.MODE_PRIVATE);

        DataContainerFile = new TinyDB(view.getContext());

        //nLayout = view.findViewById(R.id.nLayout);


        nList = new ArrayList<>();
        myFList = new ArrayList <String>();
        servList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.nLayout);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        myPopUp = new MyPopUp(view.getContext());

        if(isAdded())
        {
            nBodyCreater();
        }

        recyclerAdapter =new RecyclerAdapter(nList);
        recyclerView.setAdapter(recyclerAdapter);
///////////////////////////////////////////////////////



////////////////////////////////



        editText = view.findViewById(R.id.search_bar);



        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText.setCursorVisible(true);

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                filterRV(editable.toString().toLowerCase());
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {



                if(i == EditorInfo.IME_ACTION_SEARCH )
                {

                    editText.setCursorVisible(false);
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
                    }
                    return true;
                }
                return false;
            }
        });

//////////////////////


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(nData.getString("UserID","")).child("Sorted Notification");
        fragmentManager = getFragmentManager();
        fragment = fragmentManager.findFragmentByTag("onlyAppNotificationFragment");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                dsCounted  = dataSnapshot.getChildrenCount()-1;
                for(DataSnapshot ds2 : dataSnapshot.getChildren())
                {
                    if(!ds2.getKey().equals("0"))
                    {
                        //nLayout.removeAllViews();
                        key= (String) ds2.getValue();
                        DatabaseReference myRef2 = database.getReference(nData.getString("UserID","")).child("All Notification").child(key);

                        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(isAdded())
                                {
                                    dsCounter = dsCounter+1;
                                    datePicker(dataSnapshot);
                                    nAllBody(dataSnapshot);

                                    Log.e("This is dsCounter", String.valueOf(dsCounter));
                                    //Log.e("This is Value", String.valueOf(dataSnapshot.getValue()));
                                    if(dsCounter == dsCounted)
                                    {
                                        Log.e("This is jhfsjhfhhsj",String.valueOf(myFList));
                                        myPopUp.filterBtnPopUp(myFList);
                                        dsCounter = 0;
                                    }



                                }

                                //Log.e("This", String.valueOf(myFList));
                                //nLayout.addView(NotificationBody);

                            }



                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }

            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });


        filterBtn = view.findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"this is filter btn", Toast.LENGTH_SHORT).show();
                myPopUp.showPopUp();

            }
        });

        myPopUp.myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

                switch (myPopUp.checker) {
                    case "All":
                        recyclerAdapter = new RecyclerAdapter(servList);
                        recyclerView.setAdapter(recyclerAdapter);
                        break;
                    case "Today":
                        recyclerAdapter = new RecyclerAdapter(nList);
                        recyclerView.setAdapter(recyclerAdapter);
                        break;
                    default:
                        recyclerAdapter = new RecyclerAdapter(servList);
                        recyclerView.setAdapter(recyclerAdapter);
                        filterRV_for_PopUp(myPopUp.checker);
                        break;
                }
                Toast.makeText(view.getContext(),myPopUp.checker,Toast.LENGTH_SHORT).show();
            }
        });


        ///////////////////////

        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.h4r5.hisnotify");
        view.getContext().registerReceiver(nReceiver,filter);






        return view;
    }






    @Override
    public void onDestroyView() {

        Context context = view.getContext();
        context.unregisterReceiver(nReceiver);
        super.onDestroyView();
    }


    /////Filter Recycler view

    public  void filterRV(String tData)
    {
        List <NotificationObj> searchDataList = new ArrayList<>();

        for(NotificationObj nOBJ : nList)
        {
            if(nOBJ.getObjAppName().toLowerCase().contains(tData) || nOBJ.getObjTime().toLowerCase().contains(tData) || nOBJ.getObjDate().toLowerCase().contains(tData) || nOBJ.getObjTitle().toLowerCase().contains(tData) ||nOBJ.getObjText().toLowerCase().contains(tData) ||nOBJ.getObjBigText().toLowerCase().contains(tData) ||nOBJ.getObjPackageName().toLowerCase().contains(tData))
            {
                searchDataList.add(nOBJ);
            }

            recyclerAdapter.updateList(searchDataList);

        }


    }

    /////Filter Recycler view

    public  void filterRV_for_PopUp(String tData)
    {
        List <NotificationObj> searchDataList = new ArrayList<>();

        for(NotificationObj nOBJ : servList)
        {
            if(nOBJ.getObjAppName().toLowerCase().contains(tData) || nOBJ.getObjTime().toLowerCase().contains(tData) || nOBJ.getObjDate().toLowerCase().contains(tData) || nOBJ.getObjTitle().toLowerCase().contains(tData) ||nOBJ.getObjText().toLowerCase().contains(tData) ||nOBJ.getObjBigText().toLowerCase().contains(tData) ||nOBJ.getObjPackageName().toLowerCase().contains(tData))
            {
                searchDataList.add(nOBJ);
            }

            recyclerAdapter.updateList(searchDataList);

        }


    }


    //////////DatePicker

    public void datePicker(DataSnapshot dataSnapshot)
    {
        ArrayList  a;
        a = (ArrayList) dataSnapshot.getValue();
        if(a!=null)
        {
            String nDate = (String) a.get(2);
            if(!myFList.contains(nDate))
            {
                myFList.add(nDate);
            }

        }

    }

////////////Notification Body Creator
    public void nBodyCreater()
    {
        if(getArguments() != null)
        {
            AppName = getArguments().getString("AppName");
            int totalNoti = NotificationCounter.getInt(AppName,0);


            int i;
            for(i = nData.getInt(AppName,0);i<=totalNoti;i++)
            {
                String KeyNoti = AppName+i;
                List a= DataContainerFile.getListString(KeyNoti);

                ////
                if(!a.isEmpty())
                {
                    String nAppName = (String) a.get(0);
                    String nTime = (String) a.get(1);
                    String nDate = (String) a.get(2);
                    String nTitle = (String) a.get(3);
                    String nText = (String) a.get(4);
                    String nBigText = (String) a.get(5);
                    String nPackageName = AppPackages.getString(nAppName,"");

                    NotificationObj notificationObj = new NotificationObj(nAppName,nTime,nDate,nTitle,nText,nBigText,nPackageName);
                    nList.add(0,notificationObj);


                }


            }

        }

    }



///////////////All Notification Body Creator
    public void nAllBody(DataSnapshot dataSnapshot)
    {

        if(getArguments() != null)
        {
            String serAppName = getArguments().getString("AppName");
            ArrayList  a;
            a = (ArrayList) dataSnapshot.getValue();
            ////
            if (a!=null && a.contains(serAppName))
            {
                String nAppName = (String) a.get(0);
                String nTime = (String) a.get(1);
                String nDate = (String) a.get(2);
                String nTitle = (String) a.get(3);
                String nText = (String) a.get(4);
                String nBigText = (String) a.get(5);
                String nPackageName = AppPackages.getString(nAppName,"");

                NotificationObj notificationObj = new NotificationObj(nAppName,nTime,nDate,nTitle,nText,nBigText,nPackageName);
                servList.add(0,notificationObj);
            }

        }

    }
    
//////////////////////
    class NotificationReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {


            recyclerView.removeAllViews();
            nList.clear();
            nBodyCreater();
            recyclerAdapter.notifyDataSetChanged();


        }
    }


}
