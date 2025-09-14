package demo.vikram.springai.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IngestionService implements CommandLineRunner {

    @Value("classpath:/pdfs/bank.pdf")
    private Resource resource;

    @Autowired
    private VectorStore vectorStore;

    @Override
    public void run(String... args) throws Exception {

        log.info("Beginning to ETL custom document..");

        // Read
        final DocumentReader documentReader = new TikaDocumentReader(resource);
        List<Document> documentList = documentReader.read();
        log.info("Document read");

        // Chunk
        final DocumentTransformer documentTransformer = new TokenTextSplitter();
        List<Document> chunkedDocuments = documentTransformer.apply(documentList);
        log.info("Splitted document into {}", chunkedDocuments.size());

        // Write
        vectorStore.add(chunkedDocuments);
        log.info("Saved documents...");
    }
}
