package com.kpoptrade.dto;

import com.kpoptrade.entity.Goods;
import lombok.Data;

import java.util.Date;

@Data
public class GoodsDetailDto extends Goods {
    private String sellerUsername;
    private String sellerCampus;
    private Boolean favorited;
    private Boolean followingSeller;
    private String cardBundle;
    private String exchangeInfo;
    private Date reserveDeadline;
    private String extraInfo;
}
