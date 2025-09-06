package demo.vikram.springai.controller;

import demo.vikram.springai.model.ApplicationDomain;
import demo.vikram.springai.service.ProductMethodBasedService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ProductChatController {

    private final ChatClient chatClient;
    private final ProductMethodBasedService productMethodBasedService;

    private static final String SYSTEM_PROMPT = """
                        You are a product inventory system that is capable of providing the following:
                        1. Ability for user to retrieve list of all the products in the inventory
                        2. Ability for user to search for list of products based on a given category
                        If you are not able to provide the result for any of the above simply let the user know by saying
                        "Unable to find information". If the user asks for anything else, then
                        let them know their question is outside the scope of your operation and you cannot answer it.
                        """;

    public ProductChatController(ChatClient.Builder chatClientBuilder,
                                 ProductMethodBasedService productMethodBasedService) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        this.productMethodBasedService = productMethodBasedService;
    }

    @PostMapping("/method")
    public String productMethodSearch(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userInputRequest.input())
                .tools(productMethodBasedService)
                .call()
                .content();
    }

    @PostMapping("/function")
    public String productFunctionSearch(@RequestBody ApplicationDomain.UserInputRequest userInputRequest) {

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userInputRequest.input())
                .toolNames("productSearchByCategory", "productListSupplier")
                .call()
                .content();
    }
}
