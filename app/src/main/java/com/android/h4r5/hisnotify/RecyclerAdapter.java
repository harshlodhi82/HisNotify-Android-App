package com.android.h4r5.hisnotify;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.myViewHolder>
{
    private List <NotificationObj> myList;


    public RecyclerAdapter(List<NotificationObj> list)
    {
        myList = list;

    }

    public void updateList(List<NotificationObj> list){
        myList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_body,parent,false);



        myViewHolder myViewHolder = new myViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, final int position) {

        String nAppName = myList.get(position).getObjAppName();
        String nTime = myList.get(position).getObjTime();
        String nDate = myList.get(position).getObjDate();
        String nTitle = myList.get(position).getObjTitle();
        String nText = myList.get(position).getObjText();
        String nBigText = myList.get(position).getObjBigText();
        String nPackageName = myList.get(position).getObjPackageName();


        //////////////
        final MyPopUp myPopUp;
        myPopUp = new MyPopUp(holder.mView.getContext());
        myPopUp.getAllNotyPopUp(nAppName,nTime,nDate,nTitle,nText,nBigText,nPackageName);
        ////////////

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myPopUp.showPopUp();
            }
        });


        if(nTitle.equals(" ") && nText.equals(" ") && nBigText.equals(" "))
        {
            nTitle = "No Data";
            holder.nTextTV.setVisibility(View.GONE);
            holder.nBigTetxTV.setVisibility(View.GONE);
        }
        else
         {
             if(nTitle.equals(" "))
             {
                 holder.nTitleTV.setVisibility(View.GONE);
             }
             if(nText.equals(" "))
             {
                 holder.nTextTV.setVisibility(View.GONE);
             }
             if(nBigText.equals(" "))
             {
                 holder.nBigTetxTV.setVisibility(View.GONE);
             }
             if(nText.equals(nBigText))
             {
                 holder.nBigTetxTV.setVisibility(View.GONE);
             }
         }

        Drawable mIcon ;
        try {
            mIcon = holder.mView.getContext().getPackageManager().getApplicationIcon(nPackageName);
            holder.nAppIconIV.setImageDrawable(mIcon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        holder.nTitleTV.setText(nTitle);
        holder.nTextTV.setText(nText);
        holder.nAppNameTV.setText(nAppName);
        holder.nTimeTV.setText(nTime);
        holder.nDateTV.setText(nDate);
        holder.nBigTetxTV.setText(nBigText);

    }

    @Override
    public int getItemCount() {
        return myList.size();
    }


    public static class myViewHolder extends RecyclerView.ViewHolder
    {

        TextView nTitleTV;
        TextView nTextTV;
        TextView nAppNameTV;
        TextView nTimeTV ;
        TextView nDateTV;
        TextView nBigTetxTV ;
        ImageView nAppIconIV;
        View mView;
        LinearLayout linearLayout;

        public myViewHolder(View itemView) {
            super(itemView);


            mView = itemView;
            linearLayout = itemView.findViewById(R.id.nRootLY);
            nTitleTV = (TextView) itemView.findViewById(R.id.Title);
            nTextTV = (TextView) itemView.findViewById(R.id.Text);
            nAppNameTV = (TextView) itemView.findViewById(R.id.nAppName);
            nTimeTV = (TextView) itemView.findViewById(R.id.nTime);
            nDateTV = (TextView) itemView.findViewById(R.id.nDate);
            nBigTetxTV = (TextView) itemView.findViewById(R.id.BigText);
            nAppIconIV = itemView.findViewById(R.id.nAppIcon);
        }
    }


}
