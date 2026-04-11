package com.grain.system.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.system.dto.FarmerCreateDTO;
import com.grain.system.module.system.vo.FarmerVO;

public interface FarmerService {
    IPage<FarmerVO> getFarmerPage(int page, int size, String keyword, Integer auditStatus);
    FarmerVO getFarmerById(Integer id);
    void createFarmer(FarmerCreateDTO dto, Integer operatorId);
    void updateFarmer(Integer id, FarmerCreateDTO dto, Integer operatorId);
    void updateAuditStatus(Integer id, Integer auditStatus, Integer operatorId);
    void deleteFarmer(Integer id);
}
