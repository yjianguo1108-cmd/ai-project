package com.grain.system.module.warehouse.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckAuditDTO {

    private String result;

    private List<DiffDetailDTO> diffDetails;

    @Data
    public static class DiffDetailDTO {
        private Integer detailId;
        private String handleType;
        private String adjustReason;
    }
}
