package in.harmeetsingh.sprintaivectorpipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

record GameTitle(String title){
    public String getNormalizedTitle() {
        return title.toLowerCase().replace(" ", "_");
    }
}

@Component
public class GameRulesLoaderApplication {

    private static final Logger LOG = LoggerFactory.getLogger(GameRulesLoaderApplication.class);

    @Value("classpath:/prompt-templates/name-of-the-game.st")
    Resource nameOfTheGameTemplateResource;

    @Bean
    Function<Flux<List<Document>>, Flux<List<Document>>> titleDeterminer(ChatClient.Builder chatBuilder) {
        var chatClient = chatBuilder.defaultOptions(ChatOptions.builder().model("gpt-4o-mini")).build();

        return documentListFlux -> documentListFlux
                .map(documents -> {
                    var firstDocument = documents.getFirst();

                    var gameTitle = chatClient.prompt()
                            .user(userSpec -> userSpec
                                    .text(nameOfTheGameTemplateResource)
                                    .param("document", firstDocument.getText()))
                            .call()
                            .entity(GameTitle.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);

                    if (Objects.requireNonNull(gameTitle).title().equals("UNKNOWN")) {
                        LOG.warn("Unable to determine the name of a game; "+ "not adding to the vector store");
                        documents = Collections.emptyList();
                        return documents;
                    }

                    LOG.info("Determined game title to be {}", gameTitle.title());
                    documents = documents.stream().peek(document -> {
                        LOG.info("The game name ...... {}", gameTitle.getNormalizedTitle());
                        document.getMetadata()
                                .put("gameTitle", gameTitle.getNormalizedTitle());
                    }).toList();

                    return documents;
                });
    }
}
