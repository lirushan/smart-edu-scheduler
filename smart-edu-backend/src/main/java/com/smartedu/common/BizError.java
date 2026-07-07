package com.smartedu.common;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 业务错误码枚举
 */
@Getter
@AllArgsConstructor
public enum BizError {

    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 选课业务 (10001-10099)
    COURSE_FULL(10001, "课程名额已满"),
    ENROLL_TIME_NOT_OPEN(10002, "选课时间未到或已过"),
    PREREQUISITE_NOT_MET(10003, "先修课程未完成"),
    CREDIT_EXCEEDED(10004, "学分超限"),
    ENROLL_COUNT_EXCEEDED(10011, "选课门数超限"),
    TIME_CONFLICT(10005, "上课时间冲突"),
    DUPLICATE_ENROLL(10006, "请勿重复选课"),
    STUDENT_DISABLED(10007, "账号已限制选课"),
    ENROLL_NOT_FOUND(10008, "选课记录不存在"),
    ALREADY_DROPPED(10009, "该课程已退选"),
    COURSE_NOT_FOUND(10010, "课程不存在"),

    // 考试业务 (10100-10199)
    EXAM_NOT_STARTED(10100, "考试未开始"),
    EXAM_ENDED(10101, "考试已结束"),
    ALREADY_SUBMITTED(10102, "已交卷，不可重复提交"),
    EXAM_NOT_FOUND(10103, "考试不存在"),
    CHEAT_DETECTED(10104, "检测到异常行为"),

    // 成绩业务 (10200-10299)
    SCORE_NOT_EDITABLE(10200, "成绩状态不允许修改"),
    SCORE_ALREADY_PUBLISHED(10201, "成绩已发布，不可修改"),

    // 用户业务 (10300-10399)
    USER_NOT_FOUND(10300, "用户不存在"),
    PASSWORD_ERROR(10301, "密码错误"),
    ACCOUNT_LOCKED(10302, "账号已锁定"),
    ACCOUNT_DISABLED(10303, "账号已禁用"),
    ;

    private final int code;
    private final String message;
}
