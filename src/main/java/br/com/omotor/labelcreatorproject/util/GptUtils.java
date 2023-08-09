package br.com.omotor.labelcreatorproject.util;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class GptUtils {

    private final static String API_KEY = "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";
    public static List<String> translateWords(List<String> words) {
        OpenAiService service = new OpenAiService(API_KEY);
        StringBuilder prompt = new StringBuilder("Realize traduções para o inglês e mantenha a formatação original:\n");
        for (String word : words) {
            prompt.append(word.trim()).append(", ");
        }
        prompt.deleteCharAt(prompt.lastIndexOf(","));
        CompletionRequest request = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(prompt.toString())
                .maxTokens(500)
                .build();
        String response = service.createCompletion(request).getChoices().get(0).getText().replace("\n", "").trim();
        return List.of(response.split(", "));
    }
}
