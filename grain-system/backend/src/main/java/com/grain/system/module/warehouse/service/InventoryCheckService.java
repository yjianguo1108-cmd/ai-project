package com.grain.system.module.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.warehouse.dto.CheckAuditDTO;
import com.grain.system.module.warehouse.dto.CheckDetailUpdateDTO;
import com.grain.system.module.warehouse.dto.CheckPlanCreateDTO;
import com.grain.system.module.warehouse.dto.OutboundCreateDTO;
import com.grain.system.module.warehouse.entity.InventoryCheckDetail;
import com.grain.system.module.warehouse.entity.InventoryCheckPlan;
import com.grain.system.module.warehouse.entity.OutboundOrder;

import java.util.List;

public interface InventoryCheckService {

    IPage<InventoryCheckPlan> getPlanPage(int page, int size, Integer status, Integer checkType);

    InventoryCheckPlan getPlanById(Integer id);

    void createPlan(CheckPlanCreateDTO dto, Integer operatorId);

    void publishPlan(Integer id, Integer operatorId);

    List<InventoryCheckDetail> getDetailList(Integer planId);

    void updateDetail(Integer id, CheckDetailUpdateDTO dto, Integer operatorId);

    void submitPlan(Integer id, Integer operatorId);

    void auditPlan(Integer id, CheckAuditDTO dto, Integer operatorId);

    void handleDiff(Integer id, String handleType, String adjustReason, Integer operatorId);

    String getReportUrl(Integer id);

    IPage<OutboundOrder> getOutboundPage(int page, int size, Integer grainId, Integer positionId, String dateFrom, String dateTo, Integer status);

    OutboundOrder getOutboundById(Integer id);

    void createOutbound(OutboundCreateDTO dto, Integer operatorId);

    void confirmOutbound(Integer id, Integer operatorId);

    void voidOutbound(Integer id, String reason, Integer operatorId);
}
