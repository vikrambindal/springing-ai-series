package demo.vikram.springai.capabilities.tool;

import demo.vikram.springai.model.FoodRecipe;
import demo.vikram.springai.model.TravelResponse;
import demo.vikram.springai.model.UserTravelChoice;
import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ToolProvider {

    @McpTool(
            name = "hobbitFoodRecipeAnalyzer",
            description = "Provides summary of hobbit food recipe"
    )
    public FoodRecipe hobbitFoodRecipeSampling(
            final McpSyncRequestContext context,
            @McpToolParam(description = "Name of the food") String food
    ) {

        if (context.sampleEnabled()) {

            McpSchema.CreateMessageRequest samplingMessageRequest = McpSchema.CreateMessageRequest.builder()
                    .systemPrompt("""
                                You are a hobbit food recipe analyzer. You are to suggest recipes for a given food."
                                "If you are not able to suggest a food recipe, simply respond with a friendly message"
                                "letting user know recipe currently not available.
                            """)
                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.ASSISTANT,
                            new McpSchema.TextContent("Please provide recipe for a %s".formatted(food)))))
                    .progressToken(null)
                    .maxTokens(1000)
                    .stopSequences(null)
                    .meta(null)
                    .metadata(null)
                    .temperature(null)
                    .modelPreferences(null)
                    .progressToken(null)
                    .build();
            McpSchema.CreateMessageResult messageResult = context.sample(samplingMessageRequest);
            McpSchema.TextContent content = (McpSchema.TextContent) messageResult.content();
            return new FoodRecipe(content.text());
        }

        return new FoodRecipe("Sorry, recipe currently not available. Please try again later.");
    }

    @McpTool(
            name = "hobbitTravelPlanner",
            description = "Provides direction to hobbits for travelling from one location to another"
    )
    public TravelResponse hobbitTravelPlannerElicitation(
            final McpSyncRequestContext context,
            @McpToolParam(description = "Source from where the hobbit wants to start travelling") String from,
            @McpToolParam(description = "Destination where the hobbit wants to travel to") String to
    ) {

        if (context.elicitEnabled()) {

            StructuredElicitResult<UserTravelChoice> elicit = context.elicit(spec ->
                    spec.message("please select hobbit options"), UserTravelChoice.class);

            if (elicit.action() == McpSchema.ElicitResult.Action.ACCEPT) {
                return switch (elicit.structuredContent().travelRoute()) {
                    case VIA_ISENGUARD -> new TravelResponse("""
                            1. Start from %s,
                            2. Take left to Isenguard.
                            3. March towards Gondor.
                            4. You will then arrive at your destination: %s
                            """.formatted(from, to));
                    case VIA_MARSHLANDS -> new TravelResponse("""
                            1. Start from %s,
                            2. You will meet Gollum
                            2. Follow him in marshlands, DO NOT look in water
                            3. Go up the stairs with Cirith ungol
                            4. You will then arrive at your destination: %s
                            """.formatted(from, to));
                };
            }
        }

        return new TravelResponse("Sorry, we failed to recognize your selected route. Please choose the route and accept");
    }

    @McpTool(
            name = "hobbitPartyPlanner",
            description = "Provides direction to hobbits for planning a party"
    )
    public String hobbitPartyPlanning(
            final McpSyncRequestContext exchange,
            @McpProgressToken Object token) throws InterruptedException {

        Object progressTracer = token == null ? "1" : token;
        exchange.progress(new McpSchema.ProgressNotification(progressTracer,
                10, 100d, "Analyzing party items...."));

        TimeUnit.SECONDS.sleep(10);

        exchange.progress(new McpSchema.ProgressNotification(progressTracer,
                50, 100d, "I have list of items, preparing party plan...."));

        TimeUnit.SECONDS.sleep(10);

        exchange.progress(new McpSchema.ProgressNotification(progressTracer,
                100, 100d, "I have list of items, preparing party plan...."));

        return """
                Okay, here is what I have planned.
                1. Arrange drinks.
                2. Hobbits love their music, so get a nice music with grooving beats.
                3. Bilbo loves to give speech, so get a mike and awesome speaker system
                4. Food, hobbits love food. Get burgers, pizza and a very large birthday cake
                5. What party is a party without firecrackers. Get a dragon fire cracker and keep it out of
                   arms way from Merry and Pippin.
                6. Have a dance floor for everyone to enjoy the evening.
                """;
    }
}
