package com.tmall.wireless.tangram.op;

import java.util.List;

import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;

/**
 * Created by longerian on 2018/3/26.
 *
 * @author longerian
 * @date 2018/03/26
 */

public class LoadGroupOp extends TangramOp2<Card, List<BaseCell>> {
    public LoadGroupOp(Card arg1, List<BaseCell> arg2) {
        super(OP_TYPE.LOAD_GROUP, arg1, arg2);
    }
}
