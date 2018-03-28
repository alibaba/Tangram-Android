package com.tmall.wireless.tangram.op;

/**
 * Created by longerian on 2018/3/26.
 *
 * @author longerian
 * @date 2018/03/26
 */

public class TangramOp3<V1, V2, V3> extends TangramOp2<V1, V2> {

    private final V3 arg3;

    public TangramOp3(V1 arg1, V2 arg2, V3 arg3) {
        super(arg1, arg2);
        this.arg3 = arg3;
    }

    public V3 getArg3() {
        return arg3;
    }
}
