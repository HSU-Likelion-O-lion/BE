package com.likelion.olion.global.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateLoaderTest {
    @Test
    void loadsVersionedPromptAndReplacesVariables() {
        String prompt = new PromptTemplateLoader().load(
                "essay", "v1", Map.of("reflections", "ID=1\n내용=오늘의 사유"));

        assertThat(prompt).contains("독자의 사유");
        assertThat(prompt).contains("ID=1\n내용=오늘의 사유");
        assertThat(prompt).doesNotContain("{{reflections}}");
    }
}
