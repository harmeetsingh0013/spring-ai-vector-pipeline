package in.harmeetsingh.sprintaivectorpipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
public class SprintAiVectorPipelineApplication {
    public static final Logger LOG = LoggerFactory.getLogger(SprintAiVectorPipelineApplication.class);

    @Bean
    Function<Flux<byte[]>, Flux<Document>> documentReader() {
        return resourceFlux -> resourceFlux
                .map(fileBytes -> new TikaDocumentReader(new ByteArrayResource(fileBytes))
                        .get()
                        .getFirst()).subscribeOn(Schedulers.boundedElastic());
    }

    @Bean
    Function<Flux<Document>, Flux<List<Document>>> splitter () {
        var splitter = TokenTextSplitter.builder().build();
        return documentFlux -> documentFlux
                .map(incoming -> splitter.apply(List.of(incoming)))
                .subscribeOn(Schedulers.boundedElastic());

    }

    @Bean
    Consumer<Flux<List<Document>>> vectorStoreConsumer(VectorStore vectorStore) {
        return documentFlux -> documentFlux
                .doOnNext(documents -> {
                    if (!documents.isEmpty()) {
                        var docCount = documents.size();
                        LOG.info("Writing {} documents to vector store.", docCount);
                        vectorStore.accept(documents);
                        LOG.info("{} documents have been written to vector store.", docCount);
                    }
                }).subscribe();
    }

    @Bean
    ApplicationRunner go(FunctionCatalog catalog) {
        Runnable composedFunction = catalog.lookup(null);
        return args -> {
            composedFunction.run();
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(SprintAiVectorPipelineApplication.class, args);
    }

}
