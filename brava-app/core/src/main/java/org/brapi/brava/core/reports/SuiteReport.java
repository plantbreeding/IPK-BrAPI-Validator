package org.brapi.brava.core.reports;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.brava.core.model.MiniReport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private String url;

    @Setter(AccessLevel.NONE)
    private List<CollectionReport> collections = new ArrayList<>();

    public void addCollectionReport(CollectionReport tc) {
        this.collections.add(tc);
    }

    @JsonIgnore
    public Map<String, Map<String, Object>> getShortReport() {
        Map<String, Map<String, Object>> shortReport = new LinkedHashMap<>();

        List<FolderReport> folderReports = this.collections.stream().flatMap(collectionReport -> collectionReport.getFolderReports().stream()).collect(Collectors.toList());

        folderReports.forEach(folderReport -> {
            shortReport.put(folderReport.getName(), folderReport.getTestsShort()) ;
        });

        return shortReport;
    }

    @JsonIgnore
    public MiniReport getMiniReport() {
        return new MiniReport(getShortReport()) ;
    }
}
