package com.tmall.wireless.tangram.support;

import android.view.View;
import com.tmall.wireless.tangram.structure.BaseCell;

/**
 * Created by longerian on 2018/3/5.
 *
 * @author longerian
 * @date 2018/03/05
 */

public class TangramRxEvent {

    private View mView;

    private BaseCell mCell;

    private int mEventType;

    public TangramRxEvent(View view, BaseCell cell, int eventType) {
        mView = view;
        mCell = cell;
        mEventType = eventType;
    }

    public void update(View view, BaseCell cell, int eventType) {
        mView = view;
        mCell = cell;
        mEventType = eventType;
    }

    public View getView() {
        return mView;
    }

    public BaseCell getCell() {
        return mCell;
    }

    public int getEventType() {
        return mEventType;
    }
}
