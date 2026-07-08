package com.smartedu.controller;

import com.smartedu.common.PageResult;
import com.smartedu.common.Result;
import com.smartedu.entity.SysUser;
import com.smartedu.service.AcademicStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 教务学生管理控制器（新生导入）
 */
@RestController
@RequestMapping("/api/v1/academic/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicStudentController {

    private final AcademicStudentService academicStudentService;

    /**
     * 学生档案列表
     */
    @GetMapping
    public Result<PageResult<SysUser>> listStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String grade) {
        return Result.ok(academicStudentService.listStudents(page, size, keyword, major, grade));
    }

    /**
     * 预览上传文件
     */
    @PostMapping("/preview")
    public Result<List<Map<String, String>>> preview(@RequestParam("file") MultipartFile file) {
        try {
            return Result.ok(academicStudentService.previewImport(file));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 确认导入
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importStudents(@RequestBody List<Map<String, String>> students) {
        return Result.ok(academicStudentService.importStudents(students));
    }
}
