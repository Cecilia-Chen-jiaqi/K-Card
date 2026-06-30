package com.kpoptrade.dto;

import com.kpoptrade.entity.Goods;
import lombok.Data;

@Data
public class AdminGoodsDto {
    private Goods goods;
    private String sellerUsername;
    private String sellerCampus;
}
