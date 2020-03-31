package com.tmall.wireless.tangram3.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

public class DarkModeHelper {
    private static IDarkMode sMode = new DefaultDarkModeImpl();

    public static void setDarkModeImpl(@NonNull IDarkMode mode) {
        sMode = mode;
    }

    public static boolean isDarkMode(Context context) {
        return sMode.isDarkMode(context);
    }


    public static <VIEW extends View> void disableForceDark(VIEW view) {
        sMode.setForceDarkAllowed(view, false);
    }

    public static <VIEW extends View>  void enableForceDark(VIEW view) {
        sMode.setForceDarkAllowed(view, true);
    }

}
