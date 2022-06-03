package com.sixsimplex.treasurehunt.revelocore.util.log;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceInfo {

    private static String className = "DeviceInfo";

    public static String getDeviceInfo(Context context) {

        String info = "";
        info += "device name:" + getDeviceName() + " .. ";
        info += "manufacturer:" + getDeviceInfo(context, "DEVICE_MANUFACTURE") + " .. ";
        info += "isTablet?:" + isTablet(context) + " .. ";
        info += "android version:" + getDeviceInfo(context, "DEVICE_VERSION");
        return info;
    }

    private static String getDeviceInfo(Context activity, String infoType) {
        try {
            switch (infoType) {
                case "DEVICE_LANGUAGE":
                    return Locale.getDefault().getDisplayLanguage();

                case "DEVICE_TIME_ZONE":
                    return TimeZone.getDefault().getID();//(false, TimeZone.SHORT);

                case "DEVICE_LOCAL_COUNTRY_CODE":
                    return activity.getResources().getConfiguration().locale.getCountry();

                case "DEVICE_CURRENT_YEAR":
                    return "" + (Calendar.getInstance().get(Calendar.YEAR));

                case "DEVICE_CURRENT_DATE_TIME":
                    Calendar calendarTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    long time = (calendarTime.getTimeInMillis() / 1000);
                    return String.valueOf(time);
                //                    return DateFormat.getDateTimeInstance().format(new Date());

                case "DEVICE_CURRENT_DATE_TIME_ZERO_GMT":
                    Calendar calendarTime_zero = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"), Locale.getDefault());
                    return String.valueOf((calendarTime_zero.getTimeInMillis() / 1000));
                //                    DateFormat df = DateFormat.getDateTimeInstance();
                //                    df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                //                    return df.format(new Date());

                case "DEVICE_HARDWARE_MODEL":
                    return getDeviceName();

                case "DEVICE_NUMBER_OF_PROCESSORS":
                    return Runtime.getRuntime().availableProcessors() + "";

                case "DEVICE_LOCALE":
                    return Locale.getDefault().getISO3Country();

                case "DEVICE_IP_ADDRESS_IPV4":
                    return "not available";
//                    return getIPAddress(true);

                case "DEVICE_IP_ADDRESS_IPV6":
                    return "not available";
//                    return getIPAddress(false);

                case "DEVICE_MAC_ADDRESS":
                    String mac = getMACAddress("wlan0");
                    if (TextUtils.isEmpty(mac)) {
                        mac = getMACAddress("eth0");
                    }
                    if (TextUtils.isEmpty(mac)) {
                        mac = "DU:MM:YA:DD:RE:SS";
                    }
                    return mac;

                case "DEVICE_TOTAL_MEMORY":
                    return String.valueOf(getTotalMemory(activity));

                case "DEVICE_FREE_MEMORY":
                    return String.valueOf(getFreeMemory(activity));

                case "DEVICE_USED_MEMORY":
                    long freeMem = getTotalMemory(activity) - getFreeMemory(activity);
                    return String.valueOf(freeMem);

                case "DEVICE_TOTAL_CPU_USAGE":
                    int[] cpu = getCpuUsageStatistic();
                    if (cpu != null) {
                        int total = cpu[0] + cpu[1] + cpu[2] + cpu[3];
                        return String.valueOf(total);
                    }
                    return "";

                case "DEVICE_TOTAL_CPU_USAGE_SYSTEM":
                    int[] cpu_sys = getCpuUsageStatistic();
                    if (cpu_sys != null) {
                        int total = cpu_sys[1];
                        return String.valueOf(total);
                    }
                    return "";

                case "DEVICE_TOTAL_CPU_USAGE_USER":
                    int[] cpu_usage = getCpuUsageStatistic();
                    if (cpu_usage != null) {
                        int total = cpu_usage[0];
                        return String.valueOf(total);
                    }
                    return "";

                case "DEVICE_MANUFACTURE":
                    return Build.MANUFACTURER;

                case "DEVICE_SYSTEM_VERSION":
                    return String.valueOf(getDeviceName());

                case "DEVICE_VERSION":
                    return String.valueOf(Build.VERSION.SDK_INT);

                case "DEVICE_IN_INCH":
                    return getDeviceInch(activity);

                case "DEVICE_TOTAL_CPU_IDLE":
                    int[] cpu_idle = getCpuUsageStatistic();
                    if (cpu_idle != null) {
                        int total = cpu_idle[2];
                        return String.valueOf(total);
                    }
                    return "";

                case "DEVICE_NETWORK_TYPE":
                    return getNetworkType(activity);

                case "DEVICE_NETWORK":
                    return checkNetworkStatus(activity);

                case "DEVICE_TYPE":
                    if (isTablet(activity)) {
                        if (getDeviceMoreThan5Inch(activity)) {
                            return "Tablet";
                        } else
                            return "Mobile";
                    } else {
                        return "Mobile";
                    }

                case "DEVICE_SYSTEM_NAME":
                    return "Android OS";

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return " error ";
    }

    @SuppressLint("NewApi")
    private static long getTotalMemory(Context activity) {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo);
            }

            return memoryInfo.totalMem / 1048576L; // in megabyte (mb)
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static long getFreeMemory(Context activity) {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo);
            }

            return memoryInfo.availMem / 1048576L; // in megabyte (mb)
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static String getDeviceName() {
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                return capitalize(model);
            } else {
                return capitalize(manufacturer) + " " + model;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error fetching name";
        }
    }

    private static String capitalize(String s) {
        try {
            if (s == null || s.length() == 0) {
                return "";
            }
            char first = s.charAt(0);
            if (Character.isUpperCase(first)) {
                return s;
            } else {
                return Character.toUpperCase(first) + s.substring(1);
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    @SuppressLint("NewApi")
    private static String getMACAddress(String interfaceName) {
        try {

            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
            return "";
        } // for now eat exceptions
        return "";

    }

    private static int[] getCpuUsageStatistic() {
        try {
            String tempString = executeTop();

            tempString = tempString.replaceAll(",", "");
            tempString = tempString.replaceAll("User", "");
            tempString = tempString.replaceAll("System", "");
            tempString = tempString.replaceAll("IOW", "");
            tempString = tempString.replaceAll("IRQ", "");
            tempString = tempString.replaceAll("%", "");

            for (int i = 0; i < 10; i++) {
                tempString = tempString.replaceAll(" {2}", " ");
            }

            tempString = tempString.trim();
            String[] myString = tempString.split(" ");
            int[] cpuUsageAsInt = new int[myString.length];

            for (int i = 0; i < myString.length; i++) {
                myString[i] = myString[i].trim();
                cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
            }
            return cpuUsageAsInt;

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "executeTop", "error in getting cpu statics");
            return null;
        }
    }

    private static String executeTop() {
        Process p = null;
        BufferedReader in = null;
        String returnString = null;
        try {
            p = Runtime.getRuntime().exec("top -n 1");
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (returnString == null || returnString.contentEquals("")) {
                returnString = in.readLine();
            }
        } catch (IOException e) {
            ReveloLogger.error(className, "executeTop", "error in getting first line of top");
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (p != null) {
                    p.destroy();
                }
            } catch (IOException e) {
                ReveloLogger.error(className, "executeTop", "error in closing and destroying top process");
                e.printStackTrace();
            }
        }
        return returnString;
    }

    private static String getNetworkType(final Context activity) {

        final ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        // check for wifi
        if (connMgr != null) {
            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isAvailable()) {
                return "Wifi";
            }
        }

        // check for mobile data
        if (connMgr != null) {
            final android.net.NetworkInfo mobileData = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileData.isAvailable()) {
                return getDataType(activity);
            }
        }

        //if no network found
        return "noNetwork";
    }

    private static String checkNetworkStatus(final Context activity) {

        // Get connect manager
        final ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        // check for wifi
        if (connMgr != null) {
            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isAvailable()) {
                return "Wifi";
            }
        }

        // check for mobile data
        if (connMgr != null) {
            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobile.isAvailable()) {
                return getDataType(activity);
            }
        }

        //if no network found
        return "0";
    }

    private static boolean isTablet(Context context) {
        try {
            return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean getDeviceMoreThan5Inch(Context activity) {
        try {
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();

            float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
            float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;

            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);

            return diagonalInches >= 7;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getDeviceInch(Context activity) {
        try {
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();

            float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
            float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
            return String.valueOf(diagonalInches);
        } catch (Exception e) {
            return "-1";
        }
    }

    private static String getDataType(Context activity) {

        String type = "Mobile Data";
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

        if (tm != null) {
            switch (tm.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    type = "Mobile Data 3G";
                    // for 3g HSDPA network type will be return as
                    // per testing(real) in device with 3g enable
                    // data and speed will also matters to decide 3g network type
                    break;

                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    type = "Mobile Data 4G";// No specification for the 4g but from wiki I found(HSPAP used in 4g)
                    break;

                case TelephonyManager.NETWORK_TYPE_GPRS:
                    type = "Mobile Data GPRS";
                    break;

                case TelephonyManager.NETWORK_TYPE_EDGE:
                    type = "Mobile Data EDGE 2G";
                    break;

            }
        }

        return type;
    }
}
