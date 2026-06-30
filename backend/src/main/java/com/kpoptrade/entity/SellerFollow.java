package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("seller_follow")
public class SellerFollow {
    @TableId
    private Long id;
    private Long userId;
    private Long sellerId;
    private Date createdAt;
}
