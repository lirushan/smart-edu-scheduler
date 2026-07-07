package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartedu.common.BizError;
import com.smartedu.common.PageResult;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.ExamQuestion;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.ExamQuestionMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 题库服务：CRUD + 批量导入 + 审核
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final ExamQuestionMapper questionMapper;

    /**
     * 分页搜索试题
     */
    public PageResult<ExamQuestion> listQuestions(int page, int size, Integer questionType,
                                                   String keyword, Integer scope,
                                                   Integer auditStatus, SysUser currentUser) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();

        if (questionType != null) wrapper.eq(ExamQuestion::getQuestionType, questionType);
        if (scope != null) wrapper.eq(ExamQuestion::getScope, scope);
        if (auditStatus != null) wrapper.eq(ExamQuestion::getAuditStatus, auditStatus);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ExamQuestion::getContent, keyword)
                    .or().like(ExamQuestion::getKnowledgePoint, keyword));
        }

        // 教师只能看自己的，题库管理员可看全局
        if (currentUser.getUserType() == 2) {
            wrapper.and(w -> w.eq(ExamQuestion::getCreatedBy, currentUser.getId())
                    .or().eq(ExamQuestion::getScope, 1).eq(ExamQuestion::getAuditStatus, 1));
        }

        wrapper.orderByDesc(ExamQuestion::getCreateTime);
        Page<ExamQuestion> pageResult = questionMapper.selectPage(Page.of(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getCurrent(),
                pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 试题详情
     */
    public ExamQuestion getQuestion(Long id) {
        return questionMapper.selectById(id);
    }

    /**
     * 新增试题
     */
    public ExamQuestion createQuestion(ExamQuestion question, SysUser currentUser) {
        question.setCreatedBy(currentUser.getId());
        // 个人 scope=2 默认通过；设为公开 scope=1 触发审核
        if (question.getScope() == null) question.setScope(2);
        if (question.getScope() == 1) {
            question.setAuditStatus(0); // 待审
        } else {
            question.setAuditStatus(1); // 自动通过
        }
        if (question.getDifficulty() == null) question.setDifficulty(3);
        questionMapper.insert(question);
        return question;
    }

    /**
     * 编辑试题
     */
    public ExamQuestion updateQuestion(Long id, ExamQuestion question) {
        ExamQuestion existing = questionMapper.selectById(id);
        if (existing == null) throw new BusinessException(BizError.NOT_FOUND);

        if (question.getQuestionType() != null) existing.setQuestionType(question.getQuestionType());
        if (question.getContent() != null) existing.setContent(question.getContent());
        if (question.getOptions() != null) existing.setOptions(question.getOptions());
        if (question.getAnswer() != null) existing.setAnswer(question.getAnswer());
        if (question.getAnalysis() != null) existing.setAnalysis(question.getAnalysis());
        if (question.getDifficulty() != null) existing.setDifficulty(question.getDifficulty());
        if (question.getKnowledgePoint() != null) existing.setKnowledgePoint(question.getKnowledgePoint());
        if (question.getScope() != null) {
            existing.setScope(question.getScope());
            if (question.getScope() == 1) existing.setAuditStatus(0); // 变为公开需重新审核
        }

        questionMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除试题
     */
    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }

    /**
     * 待审核列表
     */
    public PageResult<ExamQuestion> listPendingAudit(int page, int size) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getAuditStatus, 0).orderByAsc(ExamQuestion::getCreateTime);
        Page<ExamQuestion> pageResult = questionMapper.selectPage(Page.of(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getCurrent(),
                pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 审核试题
     */
    public ExamQuestion auditQuestion(Long id, Integer auditStatus, String comment, SysUser auditor) {
        ExamQuestion question = questionMapper.selectById(id);
        if (question == null) throw new BusinessException(BizError.NOT_FOUND);

        question.setAuditStatus(auditStatus);
        question.setAuditorId(auditor.getId());
        question.setAuditTime(LocalDateTime.now());
        questionMapper.updateById(question);
        return question;
    }
}
