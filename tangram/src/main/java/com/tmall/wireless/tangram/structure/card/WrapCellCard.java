package com.tmall.wireless.tangram.structure.card;

import android.support.annotation.NonNull;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Style;
import org.json.JSONObject;

/**
 * Created by longerian on 17/4/10.
 *
 * @author longerian
 * @date 2017/04/10
 */

public class WrapCellCard extends GridCard {

    public WrapCellCard() {
        super(1);
    }

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        maxChildren = 1;
        id = data.optString(KEY_ID, id == null ? "" : id);
        this.type = TangramBuilder.TYPE_SINGLE_COLUMN;
        this.stringType = TangramBuilder.TYPE_CONTAINER_1C_FLOW;
        createCell(resolver, data, true);
        //do not need parse style, leave style empty
        this.extras.remove(KEY_STYLE);
        style = new Style();
    }
}