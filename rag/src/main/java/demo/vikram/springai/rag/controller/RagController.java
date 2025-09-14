package demo.vikram.springai.rag.controller;

import demo.vikram.springai.rag.model.ApplicationDomain;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder chatClientBuilder,
                         VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        this.vectorStore = vectorStore;
    }

    @PostMapping("/rag")
    public String ragProcess(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return chatClient.prompt()
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .user(userInputRequest.input())
                .call()
                .content();
    }
}
