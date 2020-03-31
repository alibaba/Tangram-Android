package com.tmall.wireless.tangram3.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;

public class DefaultDarkModeImpl implements IDarkMode {
    @Override
    public boolean isDarkMode(Context context) {

        if (Build.VERSION.SDK_INT < 29) {
            return false;
        }

        Configuration configuration = getConfiguration(context);
        if (configuration == null) {
            return false;
        }
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public <VIEW extends View> void setForceDarkAllowed(VIEW view, boolean allow) {
        if (Build.VERSION.SDK_INT < 29) {
            return;
        }

        view.setForceDarkAllowed(allow);

    }

    private static Configuration getConfiguration(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return null;
        } else {
            Resources resources = context.getResources();
            return resources == null ? null : resources.getConfiguration();
        }
    }
}
