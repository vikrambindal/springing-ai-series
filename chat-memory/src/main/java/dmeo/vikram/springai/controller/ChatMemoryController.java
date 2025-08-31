package dmeo.vikram.springai.controller;

import dmeo.vikram.springai.model.ApplicationDomain;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/chat")
public class ChatMemoryController {

    private final ChatClient genericChatClient;
    private final ChatClient inMemoryChatClient;
    private final ChatClient persistentChatClient;

    public ChatMemoryController(@Qualifier("genericChatClient") final ChatClient genericChatClient,
                                @Qualifier("inMemoryChatClient") final ChatClient inMemoryChatClient,
                                @Qualifier("persistentChatClient") final ChatClient persistentChatClient) {

        this.genericChatClient = genericChatClient;
        this.inMemoryChatClient = inMemoryChatClient;
        this.persistentChatClient = persistentChatClient;
    }

    @PostMapping("/generic")
    public String genericChat(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return genericChatClient.prompt(userInputRequest.input())
                .call()
                .content();
    }

    @PostMapping("/in-memory")
    public String inMemoryChat(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return inMemoryChatClient
                .prompt(userInputRequest.input())
                .call()
                .content();
    }

    @PostMapping("/db-memory")
    public String dbMemoryChat(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return persistentChatClient
                .prompt(userInputRequest.input())
                .call()
                .content();
    }

    @PostMapping("/db-user-memory")
    public String dbMemoryChat(@RequestBody ApplicationDomain.UserInputRequest userInputRequest,
                               @RequestHeader("user-id") String userId) {

        return persistentChatClient
                .prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
                .user(userInputRequest.input())
                .call()
                .content();
    }
}
