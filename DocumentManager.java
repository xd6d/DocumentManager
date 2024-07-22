import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    Map<String, Document> documents = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        document.setId(Integer.toString(document.hashCode()));
        document.setCreated(Instant.now());
        documents.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> result = new LinkedList<>(documents.values());
        if (request.getTitlePrefixes() != null) {
            result = filterMany(result, request.getTitlePrefixes(), (d, prefix) -> d.getTitle().startsWith(prefix.toString()));
        }
        if (request.getContainsContents() != null) {
            result = filterMany(result, request.getContainsContents(), (d, content) -> d.getContent().contains(content.toString()));
        }
        if (request.getAuthorIds() != null) {
            result = filterMany(result, request.getAuthorIds(), (d, id) -> d.getAuthor().getId().equals(id.toString()));
        }
        if (request.getCreatedFrom() != null) {
            result.removeIf(document -> document.getCreated().isBefore(request.getCreatedFrom()));
        }
        if (request.getCreatedTo() != null) {
            result.removeIf(document -> document.getCreated().isAfter(request.getCreatedTo()));
        }
        return result;
    }

    private List<Document> filterMany(List<Document> input, List<String> samples, BiPredicate<Document, Object> filter) {
        List<Document> filtered = new LinkedList<>();
        for (String sample : samples) {
            input.stream()
                    .filter(d -> filter.test(d, sample))
                    .forEach(filtered::add);
        }
        return filtered;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}