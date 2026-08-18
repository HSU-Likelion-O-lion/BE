package com.likelion.olion.global.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class PromptTemplateLoader {
    private static final String PROMPT_ROOT = "prompts/";

    public String load(String feature, String version, Map<String, String> variables) {
        validatePathPart(feature, "기능");
        validatePathPart(version, "버전");
        String system = read(PROMPT_ROOT + feature + "/" + version + "/system.md");
        String user = read(PROMPT_ROOT + feature + "/" + version + "/user.md");
        return applyVariables(system + "\n\n" + user, variables);
    }

    private String read(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("프롬프트 파일을 읽을 수 없습니다: " + path, exception);
        }
    }

    private String applyVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            result = result.replace("{{" + variable.getKey() + "}}", variable.getValue());
        }
        return result;
    }

    private void validatePathPart(String value, String label) {
        if (value == null || !value.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(label + " 값이 올바르지 않습니다.");
        }
    }
}
