package demo.vikram.springai.capabilities.resources;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpMeta;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@Component
public class StaticResourceProvider {

    @Value("classpath:/static-files")
    private Resource resource;

    @McpResource(
            name = "Company Resources",
            description = "Contains all of the companies static files",
            uri = "company://resources"
    )
    public McpSchema.ReadResourceResult loadAllCompanyResources(final McpMeta mcpMeta) throws IOException {

        List<McpSchema.ResourceContents> resourceContentList = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(resource.getFile().toPath())) {
            walk.forEach(path -> {
                File currentDirectory = new File(path.toUri());
                File[] files = currentDirectory.listFiles();
                if (files != null) {
                    Arrays.stream(files).forEach(currentFile -> {
                        String mimeType;
                        try {
                            mimeType = Files.probeContentType(Path.of(currentFile.getPath()));
                        } catch (IOException e) {
                            mimeType = MediaType.APPLICATION_JSON_VALUE;
                        }

                        if (mimeType.equals(MediaType.APPLICATION_PDF_VALUE)) {
                            McpSchema.BlobResourceContents resource;
                            try {
                                resource = new McpSchema.BlobResourceContents("file://" + currentFile.getName(),
                                        mimeType,
                                        Base64.getEncoder().encodeToString(Files.readAllBytes(currentFile.toPath())));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            resourceContentList.add(resource);
                        } else {
                            McpSchema.TextResourceContents resource;
                            try {
                                resource = new McpSchema.TextResourceContents("file://" + currentFile.getName(),
                                        mimeType,
                                        Files.readString(currentFile.toPath()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            resourceContentList.add(resource);
                        }
                    });
                }
            });
        }
        return new McpSchema.ReadResourceResult(resourceContentList, null);
    }
}
