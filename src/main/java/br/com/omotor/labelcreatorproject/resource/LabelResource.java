package br.com.omotor.labelcreatorproject.resource;

import br.com.omotor.labelcreatorproject.model.dto.*;
import br.com.omotor.labelcreatorproject.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/label")
@CrossOrigin("*")
@RequiredArgsConstructor
public class LabelResource {

    private final LabelService service;

    @PostMapping
    public ResponseEntity<ReturnMessage> createLabel(@RequestBody @Valid Quotes quotesList) {
        return service.createLabel(quotesList);
    }

    @GetMapping
    public ResponseEntity<List<SystemTranslateDto>> findAllLabels() {
        return service.findAllLabels();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReturnMessage> deleteLabel(@PathVariable Long id) {
        return service.deleteLabel(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOneLabel(@PathVariable Long id) {
        return service.findOneLabel(id);
    }

    @PutMapping
    public ResponseEntity<ReturnMessage> editLabel(@RequestBody SystemTranslateDto labelDto) {
        return service.editLabel(labelDto);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchLabel(@RequestParam String value) {
        return service.searchLabel(value);
    }

    @PostMapping("/replace/{projectId}")
    public ResponseEntity<?> replaceLabel(@RequestBody Html html, @PathVariable Long projectId){
        return service.replaceLabel(html, projectId);
    }

    @PostMapping("/translate/{projectId}")
    public ResponseEntity<Html> translateLabel(@RequestBody Html html, @PathVariable Long projectId){
        return service.htmlTranslator(html, projectId);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<SystemTranslateDto>> searchLabelProject(@PathVariable Long id){
        return service.searchLabelProject(id);
    }

    @GetMapping("/sql/{id}")
    public ResponseEntity<ReturnMessage> generateSql(@PathVariable(value = "id") Long projectId, @RequestParam Integer systemLocaleId){
        return service.generateSql(projectId, systemLocaleId);
    }
}
