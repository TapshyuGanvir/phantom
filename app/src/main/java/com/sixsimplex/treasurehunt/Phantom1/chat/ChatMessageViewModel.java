package com.sixsimplex.treasurehunt.Phantom1.chat;

import android.content.Context;
import android.widget.TextView;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ChatMessageViewModel implements Comparable {
    String message, time;
    TextView tv_message, tv_time;
    boolean isMessageSent = false;

    public ChatMessageViewModel(String message, boolean isMessageSent, String time) {
        this.message = message;
        this.isMessageSent = isMessageSent;
        this.time = time;
    }

    public static Date convertStringDateToDate(String dateString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss", Locale.ENGLISH);
            return formatter.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setViews(TextView tv_message, TextView tv_time, Context context) {
        this.tv_message = tv_message;
        this.tv_time = tv_time;
        try {
            tv_message.setText(this.message);
            if (isMessageSent) {
                tv_message.setBackgroundColor(context.getResources().getColor(R.color.color_blu1e));
            }
            else {
                tv_message.setBackgroundColor(context.getResources().getColor(R.color.color_green_white));
            }
            tv_time.setText(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMessageSent() {
        return isMessageSent;
    }

    public void setMessageSent(boolean messageSent) {
        isMessageSent = messageSent;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ChatMessageViewModel) {
            String oTime = ((ChatMessageViewModel) o).getTime();
            Date oDate = DatePickerMethods.convertStringDateToDate(oTime);
            Date thisDate = DatePickerMethods.convertStringDateToDate(this.time);
            if (oDate != null && thisDate != null) {
                return thisDate.compareTo(oDate);
            }

        }
        return 0;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public static Comparator<ChatMessageViewModel>  getComparator() {
        Comparator<ChatMessageViewModel> comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 != null && o2 != null && o1 instanceof ChatMessageViewModel && o2 instanceof ChatMessageViewModel) {

                    try {
                        String o1Time = ((ChatMessageViewModel) o1).getTime();
                        String o2Time = ((ChatMessageViewModel) o2).getTime();
                        Date o1Date = DatePickerMethods.convertStringDateToDate(o1Time);
                        Date o2Date = DatePickerMethods.convertStringDateToDate(o2Time);
                        if (o1Date != null && o2Date != null) {
                            return o1Date.compareTo(o2Date);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                return 0;
            }
        };
        return comparator;
    }
}
