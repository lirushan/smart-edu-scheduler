package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.SysTrainingPlan;
import com.smartedu.mapper.SysTrainingPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教务培养方案服务
 */
@Service
@RequiredArgsConstructor
public class AcademicTrainingPlanService {

    private final SysTrainingPlanMapper trainingPlanMapper;

    /**
     * 分页查询培养方案
     */
    public Page<SysTrainingPlan> listPlans(int page, int size, String major) {
        Page<SysTrainingPlan> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysTrainingPlan> wrapper = new LambdaQueryWrapper<SysTrainingPlan>()
                .orderByDesc(SysTrainingPlan::getCreateTime);
        if (major != null && !major.isBlank()) {
            wrapper.like(SysTrainingPlan::getMajor, major);
        }
        return trainingPlanMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 创建培养方案
     */
    @Transactional
    public SysTrainingPlan createPlan(SysTrainingPlan plan) {
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        trainingPlanMapper.insert(plan);
        return plan;
    }

    /**
     * 更新培养方案
     */
    @Transactional
    public SysTrainingPlan updatePlan(Long id, SysTrainingPlan plan) {
        SysTrainingPlan existing = trainingPlanMapper.selectById(id);
        if (existing == null) throw new BusinessException(BizError.NOT_FOUND);
        if (plan.getMajor() != null) existing.setMajor(plan.getMajor());
        if (plan.getGrade() != null) existing.setGrade(plan.getGrade());
        if (plan.getTotalCredits() != null) existing.setTotalCredits(plan.getTotalCredits());
        if (plan.getRequiredCredits() != null) existing.setRequiredCredits(plan.getRequiredCredits());
        if (plan.getElectiveCredits() != null) existing.setElectiveCredits(plan.getElectiveCredits());
        if (plan.getDescription() != null) existing.setDescription(plan.getDescription());
        if (plan.getStatus() != null) existing.setStatus(plan.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        trainingPlanMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除培养方案
     */
    @Transactional
    public void deletePlan(Long id) {
        SysTrainingPlan plan = trainingPlanMapper.selectById(id);
        if (plan == null) throw new BusinessException(BizError.NOT_FOUND);
        trainingPlanMapper.deleteById(id);
    }

    /**
     * 获取单个方案详情
     */
    public SysTrainingPlan getDetail(Long id) {
        SysTrainingPlan plan = trainingPlanMapper.selectById(id);
        if (plan == null) throw new BusinessException(BizError.NOT_FOUND);
        return plan;
    }
}
