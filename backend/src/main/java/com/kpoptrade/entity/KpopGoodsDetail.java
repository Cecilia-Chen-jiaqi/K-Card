package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("kpop_goods_detail")
public class KpopGoodsDetail {
    @TableId
    private Long id;
    private Long goodsId;
    private String cardBundle;
    private String exchangeInfo;
    private java.util.Date reserveDeadline;
    private String extraInfo;
}
