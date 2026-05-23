package demo.vikram.springai.capabilities;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.DefaultMcpUriTemplateManager;
import io.modelcontextprotocol.util.McpUriTemplateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicResourceCapability {

    private final List<McpSyncClient> mcpSyncClients;

    public record ResourceTemplate(String name, String description, String uri) {
    }

    public List<ResourceTemplate> loadDynamicResources() {

        McpSyncClient mcpSyncClient = mcpSyncClients.getFirst();
        return mcpSyncClient
                .listResourceTemplates()
                .resourceTemplates()
                .stream()
                .map(resourceTemplate -> new ResourceTemplate(resourceTemplate.name(),
                        resourceTemplate.description(),
                        resourceTemplate.uriTemplate()))
                .toList();
    }

    public List<String> loadDynamicUriParameters(final String uri) {

        McpSyncClient mcpSyncClient = mcpSyncClients.getFirst();
        McpUriTemplateManager mcpUriTemplateManager = new DefaultMcpUriTemplateManager(uri);
        return mcpUriTemplateManager.getVariableNames();
    }

    public List<String> loadDynamicParameterValues(final String uri, final String parameterName) {
        McpSyncClient mcpSyncClient = mcpSyncClients.getFirst();
        McpSchema.CompleteRequest completeRequest = new McpSchema.CompleteRequest(
                new McpSchema.ResourceReference(uri),
                new McpSchema.CompleteRequest.CompleteArgument(parameterName, ""));

        McpSchema.CompleteResult completeResult = mcpSyncClient.completeCompletion(completeRequest);
        return new ArrayList<>(completeResult.completion().values());
    }

    public String loadResourceContent(final String uri, final String parameterName, final String parameterValue) {
        McpSyncClient mcpSyncClient = mcpSyncClients.getFirst();
        String uriTemplate = uri.replace(String.format("{%s}", parameterName), parameterValue);
        McpSchema.ReadResourceResult readResourceResult =
                mcpSyncClient.readResource(new McpSchema.ReadResourceRequest(uriTemplate));

        for (McpSchema.ResourceContents resourceContent : readResourceResult.contents()) {
            switch (resourceContent) {
                case McpSchema.BlobResourceContents blobResourceContents:
                    return blobResourceContents.blob();
                case McpSchema.TextResourceContents textResourceContents:
                    return textResourceContents.text();
                default:
                    return "Unsupported resource content";
            }
        }

        return null;
    }
}
