package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cart")
public class Cart {
    @TableId
    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer quantity;
    private Integer selected;
}
