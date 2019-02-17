package com.android.h4r5.hisnotify;

public class NotificationObj {

    private String objAppName;
    private String objTime;
    private String objDate;
    private String objTitle;
    private String objText;
    private String objBigText;
    private String objPackageName;


    public NotificationObj( String AppName,String Time,String Date,String Title,String Text,String BigText,String PackageName)
    {
        objAppName = AppName;
        objTime = Time;
        objDate = Date;
        objTitle = Title;
        objText = Text;
        objBigText = BigText;
        objPackageName = PackageName;
    }

    public String getObjAppName() {
        return objAppName;
    }

    public String getObjTime() {
        return objTime;
    }

    public String getObjDate() {
        return objDate;
    }

    public String getObjTitle() {
        return objTitle;
    }

    public String getObjText() {
        return objText;
    }

    public String getObjBigText() {
        return objBigText;
    }

    public String getObjPackageName() {
        return objPackageName;
    }
}
