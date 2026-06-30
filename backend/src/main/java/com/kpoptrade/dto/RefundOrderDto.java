package com.kpoptrade.dto;

import lombok.Data;

import java.util.List;

@Data
public class RefundOrderDto {
    private String orderNo;
    private String reason;
    /** 为空则整单全额退款；否则仅退指定明细 */
    private List<Long> orderItemIds;
}
