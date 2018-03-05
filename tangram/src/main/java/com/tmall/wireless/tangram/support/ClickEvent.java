package com.tmall.wireless.tangram.support;

import android.view.View;
import com.tmall.wireless.tangram.structure.BaseCell;

/**
 * Created by longerian on 2018/3/5.
 *
 * @author longerian
 * @date 2018/03/05
 */

public class ClickEvent {

    public final View mView;

    public final BaseCell mCell;

    public final int eventType;

    public ClickEvent(View view, BaseCell cell, int eventType) {
        mView = view;
        mCell = cell;
        this.eventType = eventType;
    }
}
