package com.hong.aicodehelper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class DocumentLoaderService {

    private static final String DOCS_PATTERN = "classpath:docs/*";
    private static final int CHUNK_SIZE = 500;
    private static final int MIN_CHUNK_SIZE_CHARS = 100;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 10;

    public List<Document> loadAndSplitDocuments() {
        List<Document> allChunks = new ArrayList<>();

        Resource[] resources = discoverResources();
        if (resources == null || resources.length == 0) {
            log.warn("No resources found matching pattern: {}", DOCS_PATTERN);
            return Collections.emptyList();
        }

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;

            log.info("Loading document: {}", filename);
            try {
                List<Document> documents = readResource(resource);
                if (!documents.isEmpty()) {
                    List<Document> chunks = splitDocuments(documents);
                    chunks.forEach(chunk -> chunk.getMetadata().put("source", filename));
                    allChunks.addAll(chunks);
                    log.info("  -> Split into {} chunks", chunks.size());
                }
            } catch (IOException e) {
                log.error("Failed to read document: {}", filename, e);
            }
        }

        log.info("Total document chunks: {}", allChunks.size());
        return allChunks;
    }

    private Resource[] discoverResources() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            return resolver.getResources(DOCS_PATTERN);
        } catch (IOException e) {
            log.error("Failed to scan for documents", e);
            return null;
        }
    }

    private List<Document> readResource(Resource resource) throws IOException {
        log.debug("Reading resource: {}", resource.getFilename());
        TextReader textReader = new TextReader(resource);
        return textReader.get();
    }

    private List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(CHUNK_SIZE)
                .withMinChunkSizeChars(MIN_CHUNK_SIZE_CHARS)
                .withMinChunkLengthToEmbed(MIN_CHUNK_LENGTH_TO_EMBED)
                .withKeepSeparator(true)
                .build();
        return splitter.apply(documents);
    }
}
