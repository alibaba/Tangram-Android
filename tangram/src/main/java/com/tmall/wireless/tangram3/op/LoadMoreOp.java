package com.tmall.wireless.tangram3.op;

import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.structure.BaseCell;

import java.util.List;

/**
 * Created by longerian on 2018/3/26.
 *
 * @author longerian
 * @date 2018/03/26
 */

public class LoadMoreOp extends TangramOp3<Card, List<BaseCell>, Boolean> {

    public LoadMoreOp(Card arg1, List<BaseCell> arg2, Boolean arg3) {
        super(arg1, arg2, arg3);
    }

}
