package com.grain.system.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grain.system.common.exception.BusinessException;
import com.grain.system.common.result.R;
import com.grain.system.common.util.SecurityUtil;
import com.grain.system.module.system.entity.Grain;
import com.grain.system.module.system.entity.StoragePosition;
import com.grain.system.module.system.entity.SystemParam;
import com.grain.system.module.system.mapper.GrainMapper;
import com.grain.system.module.system.mapper.StoragePositionMapper;
import com.grain.system.module.system.mapper.SystemParamMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "基础配置管理")
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final GrainMapper grainMapper;
    private final StoragePositionMapper positionMapper;
    private final SystemParamMapper paramMapper;

    // ===== 粮食种类 =====

    @Operation(summary = "查询粮食种类列表")
    @GetMapping("/grain-types")
    public R<List<Grain>> listGrains(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Grain> wrapper = new LambdaQueryWrapper<Grain>().orderByAsc(Grain::getGrainType, Grain::getGrainGrade);
        if (status != null) wrapper.eq(Grain::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Grain::getGrainType, keyword);
        return R.ok(grainMapper.selectList(wrapper));
    }

    @Operation(summary = "分页查询粮食种类")
    @GetMapping("/grain-types/page")
    public R<IPage<Grain>> pageGrains(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Grain> wrapper = new LambdaQueryWrapper<Grain>().orderByAsc(Grain::getGrainType);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Grain::getGrainType, keyword).or().like(Grain::getGrainGrade, keyword);
        if (status != null) wrapper.eq(Grain::getStatus, status);
        return R.ok(grainMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "新增粮食种类")
    @PostMapping("/grain-types")
    @PreAuthorize("hasAuthority('system:grain:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> createGrain(@RequestBody Grain grain) {
        grainMapper.insert(grain);
        return R.ok();
    }

    @Operation(summary = "编辑粮食种类")
    @PutMapping("/grain-types/{id}")
    @PreAuthorize("hasAuthority('system:grain:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> updateGrain(@PathVariable Integer id, @RequestBody Grain grain) {
        grain.setId(id);
        grainMapper.updateById(grain);
        return R.ok();
    }

    @Operation(summary = "删除粮食种类")
    @DeleteMapping("/grain-types/{id}")
    @PreAuthorize("hasAuthority('system:grain:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> deleteGrain(@PathVariable Integer id) {
        grainMapper.deleteById(id);
        return R.ok();
    }

    // ===== 储位管理 =====

    @Operation(summary = "查询储位列表")
    @GetMapping("/storage-positions")
    public R<List<StoragePosition>> listPositions(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<StoragePosition> wrapper = new LambdaQueryWrapper<StoragePosition>().orderByAsc(StoragePosition::getPositionCode);
        if (status != null) wrapper.eq(StoragePosition::getStatus, status);
        return R.ok(positionMapper.selectList(wrapper));
    }

    @Operation(summary = "分页查询储位")
    @GetMapping("/storage-positions/page")
    public R<IPage<StoragePosition>> pagePositions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<StoragePosition> wrapper = new LambdaQueryWrapper<StoragePosition>().orderByAsc(StoragePosition::getPositionCode);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(StoragePosition::getPositionCode, keyword).or().like(StoragePosition::getPositionName, keyword);
        if (status != null) wrapper.eq(StoragePosition::getStatus, status);
        return R.ok(positionMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "新增储位")
    @PostMapping("/storage-positions")
    @PreAuthorize("hasAuthority('system:position:create') or hasAuthority('ROLE_ADMIN')")
    public R<Void> createPosition(@RequestBody StoragePosition position) {
        long count = positionMapper.selectCount(
                new LambdaQueryWrapper<StoragePosition>().eq(StoragePosition::getPositionCode, position.getPositionCode()));
        if (count > 0) throw new BusinessException("储位编码已存在");
        positionMapper.insert(position);
        return R.ok();
    }

    @Operation(summary = "编辑储位")
    @PutMapping("/storage-positions/{id}")
    @PreAuthorize("hasAuthority('system:position:update') or hasAuthority('ROLE_ADMIN')")
    public R<Void> updatePosition(@PathVariable Integer id, @RequestBody StoragePosition position) {
        position.setId(id);
        positionMapper.updateById(position);
        return R.ok();
    }

    @Operation(summary = "删除储位")
    @DeleteMapping("/storage-positions/{id}")
    @PreAuthorize("hasAuthority('system:position:delete') or hasAuthority('ROLE_ADMIN')")
    public R<Void> deletePosition(@PathVariable Integer id) {
        StoragePosition pos = positionMapper.selectById(id);
        if (pos == null) throw new BusinessException("储位不存在");
        if (pos.getCurrentStock() != null && pos.getCurrentStock().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new BusinessException("该储位有库存，无法删除");
        }
        positionMapper.deleteById(id);
        return R.ok();
    }

    // ===== 系统参数 =====

    @Operation(summary = "查询系统参数列表")
    @GetMapping("/system-params")
    public R<List<SystemParam>> listParams(@RequestParam(required = false) String paramGroup) {
        LambdaQueryWrapper<SystemParam> wrapper = new LambdaQueryWrapper<SystemParam>().orderByAsc(SystemParam::getParamGroup, SystemParam::getId);
        if (paramGroup != null && !paramGroup.isEmpty()) wrapper.eq(SystemParam::getParamGroup, paramGroup);
        return R.ok(paramMapper.selectList(wrapper));
    }

    @Operation(summary = "更新系统参数")
    @PutMapping("/system-params/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public R<Void> updateParam(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        SystemParam param = paramMapper.selectById(id);
        if (param == null) throw new BusinessException("参数不存在");
        if (param.getIsEditable() != 1) throw new BusinessException("该参数不允许修改");
        param.setParamValue(body.get("paramValue"));
        param.setUpdateUserId(SecurityUtil.getCurrentUserId().intValue());
        param.setUpdateTime(LocalDateTime.now());
        paramMapper.updateById(param);
        return R.ok();
    }
}
