package com.kpoptrade.dto;

import lombok.Data;

@Data
public class GoodsAuditDto {
    private Long goodsId;
    /** true=通过上架, false=拒绝 */
    private Boolean approved;
    private String reason;
}
