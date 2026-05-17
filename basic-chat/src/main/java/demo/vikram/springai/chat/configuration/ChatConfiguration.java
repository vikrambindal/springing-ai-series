package demo.vikram.springai.chat.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatConfiguration {

    @Bean
    public ChatClient chatClient(final ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    @Bean
    public ChatClientCustomizer optionsCustomizer() {
        return chatClientBuilder ->
                chatClientBuilder.defaultOptions(ChatOptions.builder()
                                .build())
                        .build();
    }

    @Bean
    public ChatClientCustomizer advisorCustomizer() {
        return chatClientBuilder ->
                chatClientBuilder.defaultAdvisors(
                                List.of(SimpleLoggerAdvisor.builder().build()))
                        .build();
    }

    @Bean
    public ChatClientCustomizer systemCustomizer() {
        return chatClientBuilder ->
                chatClientBuilder.defaultSystem(
                        """
                             You are a friendly telling Jokes system only.
                             For any other request that is not around asking for jokes, simply respond with
                             "I am sorry, but my capacity is only limited to telling jokes. Would you like a joke instead ?".
                             Do not attempt or even try to respond with anything but jokes.
                             """
                ).build();
    }
}
