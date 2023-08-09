package br.com.omotor.labelcreatorproject.service;

import br.com.omotor.labelcreatorproject.feign.LabelClient;
import br.com.omotor.labelcreatorproject.model.*;
import br.com.omotor.labelcreatorproject.model.dto.*;
import br.com.omotor.labelcreatorproject.repository.ProjectRepository;
import br.com.omotor.labelcreatorproject.repository.SystemTranslateRepository;
import br.com.omotor.labelcreatorproject.util.HtmlUtils;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LabelService {

    @Autowired
    private SystemTranslateRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    LabelClient labelClient;

    @Transactional
    public ResponseEntity<ReturnMessage> createLabel(Quotes quotesList) {
        RestTemplate template = new RestTemplate();
        List<SystemTranslate> approvedLabels = new ArrayList<>();
        List<SystemTranslate> reprovedLabels = new ArrayList<>();
        Project project = projectRepository.findById(quotesList.getIdProject()).get();
        HashMap<String, String> labels = labelClient.fetchLabels(URI.create(project.getDevUrl()));
        quotesList.getQuotes().forEach(quote -> {
            quote = quote.trim();
            String url = "https://api.mymemory.translated.net/get?q=" + quote + "&langpair=pt|en";
            Matches matches = template.getForObject(url, Matches.class);
            assert matches != null;
            String translation = matches.getMatches().get(0).getTranslation();
            String labelNick = "label_" + translation.replace(" ", "_").toLowerCase();
            SystemTranslate systemTranslatePt = new SystemTranslate(labelNick, quote, 1, project);
            SystemTranslate systemTranslateEn = new SystemTranslate(labelNick, translation, 2, project);
            if (repository.existsByValueAndKeyLabelAndProjectId(systemTranslatePt.getValue(), systemTranslatePt.getKeyLabel(), systemTranslatePt.getProject().getId())) {
                reprovedLabels.add(systemTranslatePt);
            } else if (labels.containsKey(systemTranslatePt.getKeyLabel()) && labels.containsValue(systemTranslatePt.getValue())) {
                reprovedLabels.add(systemTranslatePt);
            } else {
                repository.save(systemTranslatePt);
                repository.save(systemTranslateEn);
                approvedLabels.add(systemTranslatePt);
            }
        });
        return ResponseEntity.status(200).body(new ReturnMessage("Labels cadastrada com sucesso!", new LabelResults(approvedLabels, reprovedLabels)));
    }

    public ResponseEntity<List<SystemTranslateDto>> findAllLabels() {
        return ResponseEntity.status(200).body(repository.findAll().stream().map(SystemTranslateDto::new).toList());
    }

    public ResponseEntity<ReturnMessage> deleteLabel(Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(404).body(new ReturnMessage("Label Não Existe no Sistema!", null));
        }
        repository.deleteById(id);
        return ResponseEntity.status(200).body(new ReturnMessage("Label Deletada com sucesso!", id));
    }

    public ResponseEntity<?> findOneLabel(Long id) {
        return ResponseEntity.status(200).body(repository.findById(id).get());
    }

    public ResponseEntity<ReturnMessage> editLabel(SystemTranslateDto labelDto) {
        if (!repository.existsById(labelDto.getId())) {
            return ResponseEntity.status(404).body(new ReturnMessage("Label Não Existe no Sistema!", null));
        }
        SystemTranslate label = repository.findById(labelDto.getId()).get();
        label.edit(labelDto);
        repository.save(label);

        return ResponseEntity.status(200).body(new ReturnMessage("Label Alterada com Sucesso!", label));
    }

    public ResponseEntity<?> searchLabel(String value) {
        return ResponseEntity.status(200).body(repository.findByValueContainingOrKeyLabelContaining(value, value));
    }

    public ResponseEntity<Html> replaceLabel(Html html, Long projectId) {
        List<SystemTranslate> labels = repository.findAllBySystemLocaleId(1L);
        String translatedHtml = html.getHtml();
        HashMap<String, String> devLabels = labelClient.fetchLabels(URI.create(projectRepository.findById(projectId).get().getDevUrl()));

        for (SystemTranslate label : labels) {
            String regex = "\\b" + Pattern.quote(label.getValue()) + "\\b"; // Expressão regular para correspondência de palavra completa
            String translatedLabel = "{{'" + label.getKeyLabel() + "' | translate}}";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(translatedHtml);

            translatedHtml = matcher.replaceAll(translatedLabel);
        }

        for (Map.Entry<String, String> entry : devLabels.entrySet()) {
            String regex = "\\b" + Pattern.quote(entry.getValue()) + "\\b"; // Expressão regular para correspondência de palavra completa
            String translatedLabel = "{{'" + entry.getKey() + "' | translate}}";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(translatedHtml);

            translatedHtml = matcher.replaceAll(translatedLabel);
        }

        html.setHtml(translatedHtml);
        return ResponseEntity.status(200).body(html);
    }


    public Set<String> extractTextWithJsoup(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getAllElements();
        Set<String> htmlTexts = new HashSet<>();

        elements.forEach(element -> {
            if (!element.tagName().equals("mat-icon")) {
                htmlTexts.add(element.ownText());
            }
        });
        return htmlTexts.stream().filter(element -> !element.contains("{") && !element.contains("}") && !element.isEmpty()).collect(Collectors.toSet());
    }

    public ResponseEntity<Html> htmlReplacer(Html html, Long projectId) {
        List<SystemTranslate> labels = repository.findAllBySystemLocaleId(1L);
        HashMap<String, String> devLabels = labelClient.fetchLabels(URI.create(projectRepository.findById(projectId).get().getDevUrl()));
//
//        Document document = new Document("");
//        document.append(html.getHtml());
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, true));
        Document document = parser.parseInput(html.getHtml(), "");


        for (SystemTranslate label : labels) {
            replaceTextWithTranslation(document, label.getValue(), label.getKeyLabel());
        }

        for (Map.Entry<String, String> label : devLabels.entrySet()) {
            replaceTextWithTranslation(document, label.getValue(), label.getKey());
        }

        Document doc = new Document("");
        doc.parser(parser).append(HtmlUtils.removeHtmlTags(document.outerHtml()));

        return ResponseEntity.status(200).body(new Html(doc.outerHtml()));
    }

    // Método para substituir texto por tradução mantendo os atributos
    private void replaceTextWithTranslation(Element element, String oldValue, String translationKey) {
        if (element.ownText().equals(oldValue) && !element.tagName().equals("mat-icon")) {
            element.text("{{'" + translationKey + "' | translate}}");
        }
        for (Element child : element.children()) {
            replaceTextWithTranslation(child, oldValue, translationKey);
        }
    }

    public ResponseEntity<Html> htmlTranslator(Html html, Long projectId) {
        this.createLabel(new Quotes(extractTextWithJsoup(html.getHtml()).stream().toList(), projectId));
        return this.htmlReplacer(html, projectId);
    }

    public ResponseEntity<List<SystemTranslateDto>> searchLabelProject(Long id) {
        return ResponseEntity.status(200).body(repository.findAllByProjectIdAndSystemLocaleId(id, 1).stream().map(SystemTranslateDto::new).toList());
    }

    public ResponseEntity<ReturnMessage> generateSql(Long projectId, Integer systemLocaleId) {
        Project project = projectRepository.findById(projectId).get();
        HashMap<String, String> labels = labelClient.fetchLabels(URI.create(project.getDevUrl()));

        List<SystemTranslate> labelList = repository.findAllByProjectIdAndSystemLocaleId(projectId, systemLocaleId);

        StringBuilder sqlCommand = new StringBuilder("INSERT INTO `" + project.getDataBaseName() + "`.`system_translate` (`created_at`, `key`, `value`, `system_locale_id`) VALUES \n");

        for (SystemTranslate label : labelList) {
            if (!labels.containsKey(label.getKeyLabel()) && !labels.containsValue(label.getValue())) {
                sqlCommand.append("(now(), ").append("'").append(label.getKeyLabel()).append("'").append(", ").append("'").append(label.getValue()).append("'").append(", '").append(label.getSystemLocaleId()).append("'),\n");
            }
        }
        sqlCommand.deleteCharAt(sqlCommand.lastIndexOf(","));
        sqlCommand.deleteCharAt(sqlCommand.lastIndexOf("\n"));
        sqlCommand.append(";");
        return ResponseEntity.status(200).body(new ReturnMessage("SQL Gerado com Sucesso!", sqlCommand));
    }

    public String removePunctuation(String input) {
        String regex = "[\\p{Punct}]";
        String result = input.replaceAll(regex, "");
        return result;
    }
}
