package com.example.springairag.rag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IngestController {

	private final VectorStore vectorStore;

	private final TokenTextSplitter textSplitter = TokenTextSplitter.builder().build();

	public IngestController(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	public record TextIngestRequest(String content, Map<String, Object> metadata) {
	}

	@PostMapping("/api/documents/text")
	public ResponseEntity<Void> ingestText(@RequestBody TextIngestRequest request) {
		Document document = new Document(request.content(), request.metadata() == null ? Map.of() : request.metadata());
		vectorStore.add(textSplitter.split(List.of(document)));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/documents/markdown")
	public ResponseEntity<Void> ingestMarkdown(@RequestParam("file") MultipartFile file) throws IOException {
		Resource resource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename();
			}
		};
		MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
			.withAdditionalMetadata("filename", file.getOriginalFilename())
			.build();
		List<Document> documents = new MarkdownDocumentReader(resource, config).get();
		vectorStore.add(textSplitter.split(documents));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/api/documents/pdf")
	public ResponseEntity<Void> ingestPdf(@RequestParam("file") MultipartFile file) throws IOException {
		Resource resource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename();
			}
		};
		List<Document> documents = new PagePdfDocumentReader(resource).get();
		vectorStore.add(textSplitter.split(documents));
		return ResponseEntity.ok().build();
	}

}
