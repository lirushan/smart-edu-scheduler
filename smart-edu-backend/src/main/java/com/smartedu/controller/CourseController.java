package com.smartedu.controller;

import com.smartedu.common.PageResult;
import com.smartedu.common.Result;
import com.smartedu.model.vo.CourseOfferingVO;
import com.smartedu.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程控制器
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/offerings")
    public Result<PageResult<CourseOfferingVO>> listOfferings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(courseService.searchOfferings(page, size, keyword, category));
    }

    @GetMapping("/offerings/{id}")
    public Result<CourseOfferingVO> getOffering(@PathVariable Long id) {
        return Result.ok(courseService.getOfferingDetail(id));
    }
}
