package demo.vikram.springai.capabilities;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StaticResourceCapability {

    private final List<McpSyncClient> mcpSyncClients;

    public List<DynamicResourceCapability.ResourceTemplate> loadStaticResources() {

        McpSyncClient mcpSyncClient = mcpSyncClients.getFirst();
        return mcpSyncClient.listResources()
                .resources()
                .stream()
                .map(resource -> new DynamicResourceCapability.ResourceTemplate(resource.name(),
                        resource.description(),
                        resource.uri()))
                .toList();
    }

    public String loadResourceContent(final String uri) {
        McpSyncClient mcpSyncClient = mcpSyncClients.getFirst();
        McpSchema.ReadResourceResult readResourceResult =
                mcpSyncClient.readResource(new McpSchema.ReadResourceRequest(uri));

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
