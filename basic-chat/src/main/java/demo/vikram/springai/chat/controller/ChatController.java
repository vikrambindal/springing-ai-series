package demo.vikram.springai.chat.controller;

import demo.vikram.springai.chat.model.ApplicationDomain;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @PostMapping("/generic")
    public String genericChat(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return chatClient.prompt(userInputRequest.input())
                .call()
                .content();
    }
}
