package kg.taskflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kg.taskflow.config.GroqConfig;
import kg.taskflow.dto.ai.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGeneratorService {

    private final GroqConfig groqConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String FORM_SYSTEM_PROMPT = """
        You are a form generator AI. Based on the user's description, generate a JSON schema for a form.

        Response MUST be valid JSON with this exact structure:
        {
            "title": "Form title",
            "description": "Form description",
            "submitButtonText": "Submit button text",
            "fields": [
                {
                    "name": "fieldName",
                    "label": "Field Label",
                    "type": "text|email|number|select|textarea|date|checkbox|password|tel|url",
                    "required": true|false,
                    "placeholder": "Placeholder text",
                    "defaultValue": "",
                    "options": [{"value": "val", "label": "Label"}],
                    "validation": {
                        "minLength": null,
                        "maxLength": null,
                        "min": null,
                        "max": null,
                        "pattern": null,
                        "message": null
                    }
                }
            ]
        }

        Rules:
        - Use camelCase for field names
        - Add appropriate validation rules
        - Use Russian for labels if the prompt is in Russian
        - "options" array only for "select" type
        - Return ONLY valid JSON, no markdown, no explanation
        """;

    private static final String TABLE_SYSTEM_PROMPT = """
        You are a table generator AI. Based on the user's description, generate a JSON schema for a data table.

        Response MUST be valid JSON with this exact structure:
        {
            "title": "Table title",
            "description": "Table description",
            "columns": [
                {
                    "key": "columnKey",
                    "header": "Column Header",
                    "type": "text|number|date|boolean|badge|avatar|actions",
                    "sortable": true|false,
                    "filterable": true|false,
                    "width": "150px",
                    "align": "left|center|right",
                    "format": null
                }
            ],
            "pagination": true,
            "searchable": true,
            "actions": ["create", "edit", "delete", "export"]
        }

        Rules:
        - Use camelCase for column keys
        - Use Russian for headers if the prompt is in Russian
        - Add appropriate column types
        - Return ONLY valid JSON, no markdown, no explanation
        """;

    public GeneratedFormSchema generateForm(GenerateFormRequest request) {
        if (!groqConfig.isEnabled()) {
            log.warn("Groq API is not configured, returning default form");
            return getDefaultFormSchema(request.getPrompt());
        }

        String prompt = request.getPrompt();
        if (request.getEntityType() != null) {
            prompt += "\nEntity type: " + request.getEntityType();
        }

        String response = callGroqApi(FORM_SYSTEM_PROMPT, prompt);
        return parseFormResponse(response);
    }

    public GeneratedTableSchema generateTable(GenerateTableRequest request) {
        if (!groqConfig.isEnabled()) {
            log.warn("Groq API is not configured, returning default table");
            return getDefaultTableSchema(request.getPrompt());
        }

        String prompt = request.getPrompt();
        if (request.getEntityType() != null) {
            prompt += "\nEntity type: " + request.getEntityType();
        }

        String response = callGroqApi(TABLE_SYSTEM_PROMPT, prompt);
        return parseTableResponse(response);
    }

    private String callGroqApi(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqConfig.getModel());
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 2000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    groqConfig.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            if (response.getBody() != null) {
                return response.getBody()
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

            throw new RuntimeException("Empty response from Groq API");
        } catch (Exception e) {
            log.error("Error calling Groq API: {}", e.getMessage());
            throw new RuntimeException("Failed to generate schema: " + e.getMessage());
        }
    }

    private GeneratedFormSchema parseFormResponse(String response) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanJson = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            return objectMapper.readValue(cleanJson, GeneratedFormSchema.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse form response: {}", response);
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private GeneratedTableSchema parseTableResponse(String response) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanJson = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            return objectMapper.readValue(cleanJson, GeneratedTableSchema.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse table response: {}", response);
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private GeneratedFormSchema getDefaultFormSchema(String prompt) {
        return GeneratedFormSchema.builder()
                .title("Новая форма")
                .description("Сгенерировано на основе: " + prompt)
                .submitButtonText("Сохранить")
                .fields(List.of(
                        FormFieldSchema.builder()
                                .name("name")
                                .label("Название")
                                .type("text")
                                .required(true)
                                .placeholder("Введите название")
                                .build(),
                        FormFieldSchema.builder()
                                .name("description")
                                .label("Описание")
                                .type("textarea")
                                .required(false)
                                .placeholder("Введите описание")
                                .build()
                ))
                .build();
    }

    private GeneratedTableSchema getDefaultTableSchema(String prompt) {
        return GeneratedTableSchema.builder()
                .title("Новая таблица")
                .description("Сгенерировано на основе: " + prompt)
                .columns(List.of(
                        TableColumnSchema.builder()
                                .key("id")
                                .header("ID")
                                .type("text")
                                .sortable(true)
                                .build(),
                        TableColumnSchema.builder()
                                .key("name")
                                .header("Название")
                                .type("text")
                                .sortable(true)
                                .filterable(true)
                                .build(),
                        TableColumnSchema.builder()
                                .key("createdAt")
                                .header("Дата создания")
                                .type("date")
                                .sortable(true)
                                .build()
                ))
                .pagination(true)
                .searchable(true)
                .actions(List.of("create", "edit", "delete"))
                .build();
    }
}
