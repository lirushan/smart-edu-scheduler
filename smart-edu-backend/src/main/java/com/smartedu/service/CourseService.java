package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.PageResult;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.CrsOffering;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.CrsCourseMapper;
import com.smartedu.mapper.CrsOfferingMapper;
import com.smartedu.mapper.SysUserMapper;
import com.smartedu.model.vo.CourseOfferingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 课程服务：课程广场分页搜索 + 课程审核
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CrsOfferingMapper crsOfferingMapper;
    private final CrsCourseMapper crsCourseMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 课程广场 — 分页搜索（仅已审核通过的）
     */
    public PageResult<CourseOfferingVO> searchOfferings(int page, int size, String keyword, String category) {
        Page<CrsOffering> pageParam = Page.of(page, size);
        IPage<CrsOffering> result = crsOfferingMapper.searchOfferings(pageParam, keyword, category);

        List<CourseOfferingVO> vos = result.getRecords().stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), vos);
    }

    /**
     * 课程详情
     */
    public CourseOfferingVO getOfferingDetail(Long id) {
        CrsOffering offering = crsOfferingMapper.selectDetailById(id);
        if (offering == null) throw new BusinessException(BizError.COURSE_NOT_FOUND);
        return toVO(offering);
    }

    /**
     * 待审核课程列表（教务看本部门，管理员看全部）
     */
    public PageResult<CourseOfferingVO> listPendingApprovals(int page, int size, SysUser currentUser) {
        LambdaQueryWrapper<CrsOffering> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrsOffering::getStatus, 0);

        if (currentUser.getUserType() == 3) {
            // 教务：只看本部门的教师提交的课程
            List<SysUser> deptTeachers = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getUserType, 2)
                            .eq(SysUser::getDepartment, currentUser.getDepartment()));
            List<Long> teacherIds = deptTeachers.stream().map(SysUser::getId).toList();
            if (teacherIds.isEmpty()) {
                return PageResult.of(0, page, size, List.of());
            }
            wrapper.in(CrsOffering::getTeacherId, teacherIds);
        }

        wrapper.orderByAsc(CrsOffering::getCreateTime);
        Page<CrsOffering> pageParam = Page.of(page, size);
        Page<CrsOffering> result = crsOfferingMapper.selectPage(pageParam, wrapper);

        List<CourseOfferingVO> vos = result.getRecords().stream().map(o -> {
            CrsOffering detail = crsOfferingMapper.selectDetailById(o.getId());
            return detail != null ? toVO(detail) : toVO(o);
        }).toList();

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), vos);
    }

    /**
     * 审核通过
     */
    public void approve(Long id, String comment) {
        CrsOffering offering = crsOfferingMapper.selectById(id);
        if (offering == null) throw new BusinessException(BizError.COURSE_NOT_FOUND);
        offering.setStatus(1);
        offering.setAuditComment(comment);
        crsOfferingMapper.updateById(offering);
    }

    /**
     * 审核驳回
     */
    public void reject(Long id, String comment) {
        CrsOffering offering = crsOfferingMapper.selectById(id);
        if (offering == null) throw new BusinessException(BizError.COURSE_NOT_FOUND);
        offering.setStatus(2);
        offering.setAuditComment(comment);
        crsOfferingMapper.updateById(offering);
    }

    private CourseOfferingVO toVO(CrsOffering o) {
        return CourseOfferingVO.builder()
                .id(o.getId())
                .courseId(o.getCourseId())
                .courseName(o.getCourseName())
                .teacherId(o.getTeacherId())
                .teacherName(o.getTeacherName())
                .credit(o.getCredit())
                .semester(o.getSemester())
                .weekday(o.getWeekday())
                .periodStart(o.getPeriodStart())
                .periodEnd(o.getPeriodEnd())
                .location(o.getLocation())
                .capacity(o.getCapacity())
                .enrolledCount(o.getEnrolledCount())
                .status(o.getStatus())
                .auditComment(o.getAuditComment())
                .build();
    }
}
