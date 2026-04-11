package com.grain.system.module.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.grain.system.module.purchase.dto.WeighingRecordCreateDTO;
import com.grain.system.module.purchase.vo.WeighingRecordVO;

public interface WeighingRecordService {

    IPage<WeighingRecordVO> getWeighingPage(int page, int size, String keyword, Integer status, Integer reserveId);

    WeighingRecordVO getWeighingById(Integer id);

    void createWeighing(WeighingRecordCreateDTO dto, Integer operatorId);

    void updateWeighing(Integer id, WeighingRecordCreateDTO dto, Integer operatorId);

    void auditWeighing(Integer id, Integer operatorId);

    void voidWeighing(Integer id, String reason, Integer operatorId);

    void deleteWeighing(Integer id);
}