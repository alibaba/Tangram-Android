package com.tmall.wireless.tangram3.util;

import android.content.Context;
import android.view.View;

public interface IDarkMode {
    boolean isDarkMode(Context context);

    <VIEW extends View> void setForceDarkAllowed(VIEW view, boolean allow);

}
