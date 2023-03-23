package org.brapi.brava.core.reports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.brapi.brava.core.model.Resource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains multiple Collection Reports and the Resource to be used (endpoint url) in those collections.
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class SuiteReport implements Report {

    @NonNull
    private Resource resource;

    @Setter(AccessLevel.NONE)
    private List<CollectionReport> collections = new ArrayList<>();

    public void addCollectionReport(CollectionReport tc) {
        this.collections.add(tc);
    }

    public Map<String, Map<String, Object>> getShortReport() {
        Map<String, Map<String, Object>> shortReport = new LinkedHashMap<>();

        List<FolderReport> folderReports = this.collections.stream().flatMap(collectionReport -> collectionReport.getFolderReports().stream()).collect(Collectors.toList());

        folderReports.forEach(folderReport -> {
            shortReport.put(folderReport.getName(), folderReport.getTestsShort()) ;
        });

        return shortReport;
    }

    public MiniReport getMiniReport() {
        return new MiniReport(getShortReport()) ;
    }
}
