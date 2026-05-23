package demo.vikram.springai.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
public class ChatClientConfiguration {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are a Hobbits Inc Agent. " +
                        "You are FORBIDDEN from answering questions using your own knowledge base." +
                        "You are FORBIDDEN from making up facts or using external web browsing." +
                        "You MUST ONLY use and execute one of the provided tools or function to answer." +
                        "You have the permission to execute tools or function that you see best fit.")
                .build();
    }

    @Bean
    ChatClientCustomizer optionsCustomizer() {
        log.info("Registering chat options....");
        return builder -> builder.defaultOptions(ChatOptions.builder()
                .build());
    }

    @Bean
    ChatClientCustomizer chatAdvisors() {
        log.info("Registering advisors....");
        return builder -> builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build());
    }

    @Bean
    ChatClientCustomizer toolCallbackCustomizer(ToolCallbackProvider toolCallbackProvider) {

        log.info("Registering tool callbacks....");
        Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .forEach(tool -> log.info("Tool callback found: {}",
                        tool.getToolDefinition()));
        return builder -> builder.defaultToolCallbacks(toolCallbackProvider).build();
    }
}
