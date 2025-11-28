package kg.taskflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kg.taskflow.dto.ai.*;
import kg.taskflow.service.AiGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Generator", description = "AI-powered form and table generation")
public class AiGeneratorController {

    private final AiGeneratorService aiGeneratorService;

    @PostMapping("/generate-form")
    @Operation(summary = "Generate form schema from description",
            description = "Uses AI to generate a form schema based on natural language description")
    public ResponseEntity<GeneratedFormSchema> generateForm(
            @Valid @RequestBody GenerateFormRequest request) {
        return ResponseEntity.ok(aiGeneratorService.generateForm(request));
    }

    @PostMapping("/generate-table")
    @Operation(summary = "Generate table schema from description",
            description = "Uses AI to generate a table schema based on natural language description")
    public ResponseEntity<GeneratedTableSchema> generateTable(
            @Valid @RequestBody GenerateTableRequest request) {
        return ResponseEntity.ok(aiGeneratorService.generateTable(request));
    }
}
