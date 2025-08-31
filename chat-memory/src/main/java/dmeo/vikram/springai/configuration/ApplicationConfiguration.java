package dmeo.vikram.springai.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    public MessageWindowChatMemory inMemoryWindowChatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean("postgresChatMemoryRepository")
    public ChatMemoryRepository persistentChatMemoryRepository(final JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }

    @Bean("persistentMemoryWindowChatMemory")
    public MessageWindowChatMemory persistentMemoryWindowChatMemory(final ChatMemoryRepository postgresChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(postgresChatMemoryRepository)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor inMemoryChatAdvisor(final MessageWindowChatMemory inMemoryWindowChatMemory) {

        return MessageChatMemoryAdvisor.builder(inMemoryWindowChatMemory)
                .build();
    }

    @Bean("persistentMemoryChatAdvisor")
    public MessageChatMemoryAdvisor persistentMemoryChatAdvisor(final MessageWindowChatMemory persistentMemoryWindowChatMemory) {

        return MessageChatMemoryAdvisor.builder(persistentMemoryWindowChatMemory)
                .build();
    }

    @Bean("genericChatClient")
    @Primary
    public ChatClient genericChatClient(final ChatClient.Builder chatClientBuilder,
                                         final SimpleLoggerAdvisor simpleLoggerAdvisor) {
        return chatClientBuilder.defaultAdvisors(simpleLoggerAdvisor)
                .build();
    }

    @Bean("inMemoryChatClient")
    public ChatClient inMemoryChatClient(final ChatClient.Builder chatClientBuilder,
                                           final MessageChatMemoryAdvisor inMemoryChatAdvisor,
                                           final SimpleLoggerAdvisor simpleLoggerAdvisor) {
        return chatClientBuilder.defaultAdvisors(inMemoryChatAdvisor, simpleLoggerAdvisor)
                .build();
    }

    @Bean("persistentChatClient")
    public ChatClient persistentChatClient(final ChatClient.Builder chatClientBuilder,
                                           final MessageChatMemoryAdvisor persistentMemoryChatAdvisor,
                                           final SimpleLoggerAdvisor simpleLoggerAdvisor) {
        return chatClientBuilder.defaultAdvisors(persistentMemoryChatAdvisor, simpleLoggerAdvisor)
                .build();
    }
}
