package demo.vikram.springai.chat.controller;

import demo.vikram.springai.chat.model.ApplicationDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @PostMapping
    public Joke genericChat(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        String joke = chatClient.prompt(userInputRequest.input())
                .call()
                .content();

        return new Joke(joke);
    }

    record Joke(String joke) {}
}
