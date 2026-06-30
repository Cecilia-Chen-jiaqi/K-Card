package com.kpoptrade.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartCheckoutBatchDto {
    private List<Long> cartIds;
}
