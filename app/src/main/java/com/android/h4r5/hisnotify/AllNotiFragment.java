package com.android.h4r5.hisnotify;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.text.DateFormat.SHORT;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllNotiFragment extends Fragment {

    View view;
    FirebaseDatabase database;
    DatabaseReference  myRef;
    String strTemp;

    SharedPreferences nData;
    SharedPreferences AppPackages;

    LinearLayout nLayout;
    String key;
    Fragment fragment;

    private NotificationReceiver nReceiver;
    FragmentManager fragmentManager;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<NotificationObj> nList;
    private RecyclerAdapter recyclerAdapter;

    private EditText editText;
    private ImageView filterBtn;

    private MyPopUp myPopUp;
    private long dsCounter =0;
    private long dsCounted =0;
    private List<String> myFList;
    private String changerFilter = "";






    public AllNotiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_all_noti, container, false);


        nData = this.getActivity().getSharedPreferences("nData",Context.MODE_PRIVATE);
        AppPackages = this.getActivity().getSharedPreferences("AppPackages",Context.MODE_PRIVATE);

        //nLayout = view.findViewById(R.id.AllLayout);
        //nLayout.removeAllViews();

        nList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.AllLayout);
        recyclerView.removeAllViews();


        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        myPopUp = new MyPopUp(view.getContext());
        myFList = new ArrayList <String>();


        ////////////////////////////////



        editText = view.findViewById(R.id.search_bar2);



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
        fragment = fragmentManager.findFragmentByTag("allNotiFragment");

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
                                    nBody(dataSnapshot);
                                    recyclerAdapter =new RecyclerAdapter(nList);
                                    recyclerView.setAdapter(recyclerAdapter);

                                    if(dsCounter == dsCounted)
                                    {
                                        Log.e("This is jhfsjhfhhsj",String.valueOf(myFList));
                                        myPopUp.filterBtnPopUp(myFList);
                                        dsCounter = 0;
                                    }
                                }

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

        filterBtn = view.findViewById(R.id.filterBtn2);
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
                    case "All" :
                        changerFilter = "";
                        filterRV(changerFilter);
                        break;
                    case "Today":
                        changerFilter = DateFormat.getDateInstance(SHORT).format(new Date());
                        filterRV(changerFilter);
                        break;
                    default:
                        changerFilter = myPopUp.checker;
                        filterRV(changerFilter);
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

    /////////////Notification Body Creater
    public void nBody(DataSnapshot dataSnapshot)
    {
        ArrayList  a;
        a = (ArrayList) dataSnapshot.getValue();
        ////
        if (a!=null)
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




    //////////////////////
    class NotificationReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {


            strTemp = intent.getStringExtra("AppNameForAllF");

            Log.e("This", strTemp+"*****************");

            if(!strTemp.isEmpty())
            {
                DatabaseReference myRef3 = database.getReference(nData.getString("UserID","")).child("All Notification").child(strTemp);

                myRef3.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(isAdded())
                        {
                            strTemp="";
                            nBody(dataSnapshot);
                            filterRV(changerFilter);
                        }

                        //nLayout.addView(NotificationBody,0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }



        }
    }

}
