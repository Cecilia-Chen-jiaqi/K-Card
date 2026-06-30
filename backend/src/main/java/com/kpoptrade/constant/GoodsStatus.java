package com.kpoptrade.constant;

public final class GoodsStatus {
    /** 下架/卖家主动下架 */
    public static final int OFF = 0;
    /** 已上架（审核通过） */
    public static final int ON = 1;
    /** 待审核 */
    public static final int PENDING = 2;
    /** 审核拒绝 */
    public static final int REJECTED = 3;

    private GoodsStatus() {
    }
}
