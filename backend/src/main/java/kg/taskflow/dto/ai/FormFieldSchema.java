package kg.taskflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldSchema {

    private String name;
    private String label;
    private String type; // text, email, number, select, textarea, date, checkbox
    private boolean required;
    private String placeholder;
    private String defaultValue;
    private List<SelectOption> options; // for select type
    private ValidationRule validation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectOption {
        private String value;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRule {
        private Integer minLength;
        private Integer maxLength;
        private Integer min;
        private Integer max;
        private String pattern;
        private String message;
    }
}
