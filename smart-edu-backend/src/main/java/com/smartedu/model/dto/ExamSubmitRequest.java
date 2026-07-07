package com.smartedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 交卷请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmitRequest {

    private List<AnswerItem> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {
        private Long questionId;
        private String answer;
    }
}
