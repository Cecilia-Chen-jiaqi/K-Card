package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("goods_favorite")
public class GoodsFavorite {
    @TableId
    private Long id;
    private Long userId;
    private Long goodsId;
    private Date createdAt;
}
