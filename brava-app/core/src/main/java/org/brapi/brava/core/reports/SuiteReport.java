package org.brapi.brava.core.reports;

import lombok.*;
import org.brapi.brava.core.model.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains multiple TestCollectionReports and the base url to be used (endpoint url) in those collections.
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class SuiteReport implements Report {

    @NonNull
    private String id;
    @NonNull
    private Resource resource;

    @Setter(AccessLevel.NONE)
    private List<CollectionReport> collections = new ArrayList<>();

    public void addCollectionReport(CollectionReport tc) {
        this.collections.add(tc);
    }
}
