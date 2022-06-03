package com.sixsimplex.treasurehunt.Phantom1.utils;

import android.content.Context;
import android.view.View;

public class ProgressBarUtilities {

    public static void showProgressBar(Context context, HorizontalDotProgressBar horizontalDotProgressBar){
      if(horizontalDotProgressBar != null){
          ((View) horizontalDotProgressBar.getParent()).setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
          horizontalDotProgressBar.setVisibility(View.VISIBLE);
      }
    }

    public static void hideProgressBar(HorizontalDotProgressBar horizontalDotProgressBar){
        if(horizontalDotProgressBar !=null){
            horizontalDotProgressBar.clearAnimation();
            horizontalDotProgressBar.setVisibility(View.GONE);
        }
    }


}
