package demo.vikram.springai.capabilities.resources;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpComplete;
import org.springaicommunity.mcp.annotation.McpMeta;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

@Component
public class DynamicResourceProvider {

    private static List<String> MIDDLE_EARTH_USERS = List.of("frodo", "samwise", "merry", "pippin");

    @Value("classpath:/profiles")
    private Resource hobbitProfileResource;

    @Value("classpath:/profiles/mugshots")
    private Resource hobbitMugshotsResource;

    @McpComplete(
            uri = "hobbit-profile://profile/{user}"
    )
    public List<String> searchForHobbitsWithProfiles(final McpSchema.CompleteRequest.CompleteArgument argument) {
        if ("user".equals(argument.name())) {
            return MIDDLE_EARTH_USERS.stream()
                    .filter(name -> name.toLowerCase().startsWith(argument.value().toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @McpComplete(
            uri = "hobbit-profile://mugshots/{user}"
    )
    public List<String> searchForHobbitsWithMugshots(final McpSchema.CompleteRequest.CompleteArgument argument) {
        if ("user".equals(argument.name())) {
            return MIDDLE_EARTH_USERS.stream()
                    .filter(name -> name.toLowerCase().startsWith(argument.value().toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @McpResource(
            name = "MiddleEarth Hobbit Profiles",
            uri = "hobbit-profile://profile/{user}",
            description = "Provides profiles for main hobbits from lord of the rings"
    )
    public McpSchema.ReadResourceResult hobbitProfiles(final McpSchema.ReadResourceRequest request,
                                                       final String user) throws IOException {


        File currentDirectory = new File(hobbitProfileResource.getURI());
        File[] files = currentDirectory.listFiles();

        for (File file : files) {
            if (file.getName().equals(user + ".txt")) {
                try {
                    byte[] fileContent = Files.readAllBytes(Path.of(file.getPath()));
                    return new McpSchema.ReadResourceResult(
                            List.of(new McpSchema.TextResourceContents(request.uri(),
                                    "text/plain",
                                    new String(fileContent, StandardCharsets.UTF_8))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new FileNotFoundException("No profile found for user : %s".formatted(user));
    }

    @McpResource(
            name = "MiddleEarth Hobbit Mugshots",
            uri = "hobbit-profile://mugshots/{user}",
            description = "Provides mugshots for main hobbits from lord of the rings"
    )
    public McpSchema.ReadResourceResult hobbitMugshots(final McpSchema.ReadResourceRequest request,
                                                       final String user,
                                                       final McpMeta mcpMeta) throws IOException {


        File currentDirectory = new File(hobbitMugshotsResource.getURI());
        File[] files = currentDirectory.listFiles();

        for (File file : files) {
            if (file.getName().equals(user + ".jpg")) {
                try {
                    return new McpSchema.ReadResourceResult(
                            List.of(new McpSchema.BlobResourceContents(request.uri(),
                                    Files.probeContentType(Path.of(file.getPath())),
                                    Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new FileNotFoundException("No profile found for user : %s".formatted(user));
    }
}
