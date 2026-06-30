package com.kpoptrade.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompleteOrderDto {
    private String orderNo;
    /** 为空则确认整单全部明细 */
    private List<Long> orderItemIds;
}
