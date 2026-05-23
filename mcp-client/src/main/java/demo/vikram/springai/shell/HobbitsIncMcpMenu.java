package demo.vikram.springai.shell;

import demo.vikram.springai.capabilities.DynamicResourceCapability;
import demo.vikram.springai.capabilities.PromptCapability;
import demo.vikram.springai.capabilities.StaticResourceCapability;
import demo.vikram.springai.capabilities.ToolCapability;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class HobbitsIncMcpMenu extends AbstractShellComponent {

    private final DynamicResourceCapability dynamicResourceCapability;
    private final StaticResourceCapability staticResourceCapability;

    private final PromptCapability promptCapability;

    private final ToolCapability toolCapability;

    @ShellMethod(key = "chatbot", value = "Provide ability to chat with the Application")
    public String getInteractiveChatBot() {

        StringInput component = new StringInput(getTerminal(), "Enter message: ", "");
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());

        StringInput.StringInputContext context = component.run(StringInput.StringInputContext.empty());
        String chat = toolCapability.chat(context.getResultValue());
        System.out.println("\n- :" + chat);

        return "\n";
    }

    @ShellMethod(key = "prompts", value = "Provides available MCP Server Prompts")
    public String getServerPrompts() throws IOException {

        List<PromptCapability.ServerPrompt> serverPrompts = promptCapability.loadServerPrompts();

        List<SelectorItem<PromptCapability.ServerPrompt>> items = serverPrompts.stream()
                .map(serverPrompt -> SelectorItem.of(
                        String.format("%s - (%s)", serverPrompt.name(), serverPrompt.description()), serverPrompt))
                .toList();

        SingleItemSelector<PromptCapability.ServerPrompt, SelectorItem<PromptCapability.ServerPrompt>> component =
                new SingleItemSelector<>(getTerminal(), items, "Select one:", null);

        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());

        SingleItemSelector.SingleItemSelectorContext<PromptCapability.ServerPrompt, SelectorItem<PromptCapability.ServerPrompt>> context =
                component.run(SingleItemSelector.SingleItemSelectorContext.empty());

        PromptCapability.ServerPrompt serverPrompt = context.getResultItem()
                .map(item -> item.getItem())
                .orElse(null);

        if (serverPrompt.promptArguments() == null || serverPrompt.promptArguments().isEmpty()) {
            return promptCapability.readPrompt(serverPrompt.name(), null);
        } else {
            serverPrompt.promptArguments()
                    .forEach(promptArgument -> {

                        List<String> parameterValues = promptCapability.getParameterValues(serverPrompt.name(), promptArgument.name());
                        Map<String, String> paramterDisplayValueMap = parameterValues.stream()
                                .collect(Collectors.toMap(key -> key.toUpperCase(),
                                        key -> key));
                        String parameterValue = selectFromMenu(paramterDisplayValueMap);

                        String promptResponse = promptCapability.readPrompt(serverPrompt.name(), Map.of(promptArgument.name(), parameterValue));

                        System.out.println("> Response : " + promptResponse);
                    });
        }
        return "\n";
    }

    @ShellMethod(key = "resources", value = "Provides available MCP Server Resources")
    public String getServerResources(@ShellOption(help = "Show resources that require user interaction") boolean dynamic) throws IOException {

        if (dynamic) {
            loadDynamicResources();
        } else {
            //NOTE: Currently this provides base64 encoded pdf, to decode copy content and go to: https://base64.guru/converter/decode/file
            loadStaticResources();
        }

        return "\n";
    }

    private void loadStaticResources() {

        List<DynamicResourceCapability.ResourceTemplate> resourceTemplates = staticResourceCapability.loadStaticResources();
        Map<String, String> paramterResourceDisplayValueMap = resourceTemplates.stream()
                .collect(Collectors.toMap(key -> String.format("%s - (%s)", key.name(), key.description()),
                        key -> key.uri()));
        String resourceTemplate = selectFromMenu(paramterResourceDisplayValueMap);

        String content = staticResourceCapability.loadResourceContent(resourceTemplate);
        System.out.println("> Response : " + content);
    }

    private void loadDynamicResources() {
        List<DynamicResourceCapability.ResourceTemplate> resourceTemplates = dynamicResourceCapability.loadDynamicResources();
        Map<String, String> paramterResourceDisplayValueMap = resourceTemplates.stream()
                .collect(Collectors.toMap(key -> String.format("%s - (%s)", key.name(), key.description()),
                        key -> key.uri()));
        String resourceTemplate = selectFromMenu(paramterResourceDisplayValueMap);

        List<String> variableNames = dynamicResourceCapability.loadDynamicUriParameters(resourceTemplate);
        variableNames.forEach(variableName -> {

            List<String> parameterValues = dynamicResourceCapability.loadDynamicParameterValues(resourceTemplate, variableName);

            Map<String, String> paramterDisplayValueMap = parameterValues.stream()
                    .collect(Collectors.toMap(String::toUpperCase, Function.identity()));
            String parameterValue = selectFromMenu(paramterDisplayValueMap);

            String content = dynamicResourceCapability.loadResourceContent(resourceTemplate, variableName, parameterValue);
            System.out.println("> Response : " + content);
        });
    }

    private String selectFromMenu(Map<String, String> options) {
        // Convert raw string options into Spring Shell SelectorItems
        List<SelectorItem<String>> selectorItems = options.keySet().stream()
                .map(key -> SelectorItem.of(key, options.get(key)))
                .collect(Collectors.toList());

        // Configure the interactive terminal UI component
        SingleItemSelector<String, SelectorItem<String>> selector =
                new SingleItemSelector<>(getTerminal(), selectorItems, "Select one: ", null);

        selector.setResourceLoader(getResourceLoader());
        selector.setTemplateExecutor(getTemplateExecutor());

        // FIX: Pass the mandated empty context into the run method execution
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context =
                selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        // Extract out what item string line the user committed via Enter
        return context.getResultItem()
                .map(item -> item.getItem())
                .orElse("No selection made");
    }
}