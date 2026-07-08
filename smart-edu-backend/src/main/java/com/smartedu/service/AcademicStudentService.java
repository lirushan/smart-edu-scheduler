package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.PageResult;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 教务学生导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicStudentService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 查询学生档案，承接新生导入后的数据核对。
     */
    public PageResult<SysUser> listStudents(Integer page, Integer size, String keyword, String major, String grade) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserType, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getDepartment, keyword)
                    .or().like(SysUser::getMajor, keyword));
        }
        if (StringUtils.hasText(major)) {
            wrapper.like(SysUser::getMajor, major);
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(SysUser::getGrade, grade);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> pageResult = userMapper.selectPage(Page.of(page, size), wrapper);
        pageResult.getRecords().forEach(user -> user.setPassword(null));
        return PageResult.of(pageResult.getTotal(), pageResult.getCurrent(),
                pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 预览上传的CSV/Excel文件，返回待导入学生列表
     */
    public List<Map<String, String>> previewImport(MultipartFile file) throws Exception {
        List<Map<String, String>> students = new ArrayList<>();
        String filename = file.getOriginalFilename();
        if (filename == null) throw new BusinessException(BizError.BAD_REQUEST.getCode(), "文件名不能为空");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new BusinessException(BizError.BAD_REQUEST.getCode(), "文件为空");
            String[] headers = parseCSVLine(headerLine);
            // 寻找列索引
            int nameIdx = findColumn(headers, "姓名", "name");
            int studentNoIdx = findColumn(headers, "学号", "studentNo", "username");
            int majorIdx = findColumn(headers, "专业", "major");
            int gradeIdx = findColumn(headers, "年级", "grade");

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                String[] values = parseCSVLine(line);
                Map<String, String> student = new LinkedHashMap<>();
                student.put("row", String.valueOf(rowNum));
                student.put("name", nameIdx >= 0 && nameIdx < values.length ? values[nameIdx].trim() : "");
                student.put("studentNo", studentNoIdx >= 0 && studentNoIdx < values.length ? values[studentNoIdx].trim() : "");
                student.put("major", majorIdx >= 0 && majorIdx < values.length ? values[majorIdx].trim() : "");
                student.put("grade", gradeIdx >= 0 && gradeIdx < values.length ? values[gradeIdx].trim() : "");
                students.add(student);
            }
        }
        return students;
    }

    /**
     * 批量导入学生
     */
    @Transactional
    public Map<String, Object> importStudents(List<Map<String, String>> students) {
        int success = 0;
        int fail = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, String> student : students) {
            try {
                String name = student.get("name");
                String studentNo = student.get("studentNo");
                String major = student.get("major");
                String grade = student.get("grade");

                if (name == null || name.isBlank()) {
                    fail++;
                    errors.add("第" + student.get("row") + "行：姓名不能为空");
                    continue;
                }
                if (studentNo == null || studentNo.isBlank()) {
                    fail++;
                    errors.add("第" + student.get("row") + "行：学号不能为空");
                    continue;
                }

                // 检查是否已存在
                Long existingCount = userMapper.selectCount(
                        new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, studentNo));
                if (existingCount > 0) {
                    fail++;
                    errors.add("第" + student.get("row") + "行：学号 " + studentNo + " 已存在");
                    continue;
                }

                SysUser user = SysUser.builder()
                        .username(studentNo)
                        .password(passwordEncoder.encode("password123"))
                        .realName(name)
                        .userType(1) // 学生
                        .major(major != null ? major : "")
                        .grade(grade != null ? grade : "")
                        .status(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();

                userMapper.insert(user);
                success++;
            } catch (Exception e) {
                fail++;
                errors.add("第" + student.get("row") + "行：" + e.getMessage());
                log.error("导入学生失败: row={}, error={}", student.get("row"), e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("total", students.size());
        result.put("errors", errors);
        return result;
    }

    private int findColumn(String[] headers, String... candidates) {
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim();
            for (String c : candidates) {
                if (h.equalsIgnoreCase(c) || h.contains(c)) return i;
            }
        }
        return -1;
    }

    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString());
        return values.toArray(new String[0]);
    }
}
