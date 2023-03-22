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

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getShortReport() throws JsonProcessingException, IOException {
        LinkedHashMap<String, LinkedHashMap<String, Object>> shortReport = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
        ObjectMapper mapper = new ObjectMapper();

        List<FolderReport> folderReports = this.collections.stream().flatMap(collectionReport -> collectionReport.getFolderReports().stream()).collect(Collectors.toList());

        folderReports.forEach(folderReport -> {
            shortReport.put(folderReport.getName(), folderReport.getTestsShort()) ;
        });

        return shortReport;
    }

    public MiniReport getMiniReport() throws JsonProcessingException, IOException {
        MiniReport miniReport = new MiniReport();
        List<Integer> time = new ArrayList<Integer>();
        List<String> totalTests = new ArrayList<String>();
        List<String> passedTests = new ArrayList<String>();
        List<String> warningTests = new ArrayList<String>();
        List<String> failedTests = new ArrayList<String>();
        LinkedHashMap<String, LinkedHashMap<String, Object>> shortReport = this.getShortReport();
        for (Map.Entry<String, LinkedHashMap<String, Object>> folder : shortReport.entrySet()) {
            for(Map.Entry<String, Object> tests : folder.getValue().entrySet()) {
                if (tests.getValue().getClass().equals(LinkedHashMap.class)) {
                    LinkedHashMap<String, Object> test = (LinkedHashMap<String, Object>) tests.getValue();
                    if (!tests.getKey().equals("/calls")) {
                        time.add((int) test.get("responseTime"));
                    }
                    totalTests.add(tests.getKey());
                    List<String> testStatus = (List<String>) test.get("testStatus");
                    if (testStatus.isEmpty()) {
                        passedTests.add(tests.getKey());
                    } else {
                        boolean failed = false;
                        // Count as failed only if it fails for the following reasons.
                        for (String reason : testStatus) {
                            if (reason.equals("wrong status code") ||
                                    reason.equals("wrong contentType") ||
                                    reason.equals("can't connect")) {
                                failed = true;
                            }
                        }
                        if (failed) {
                            failedTests.add(tests.getKey());
                        } else {
                            warningTests.add(tests.getKey());
                        }


                    }
                }
            }
        }
        double median;
        if (time.isEmpty()) {
            median = 0;
        } else {
            median = calculateMedian(time);
        }
        miniReport.setTime(median);
        miniReport.setPassedTests(passedTests);
        miniReport.setTotalTests(totalTests);
        miniReport.setFailedTests(failedTests);
        miniReport.setWarningTests(warningTests);
        return miniReport;
    }


    private double calculateMedian(List<Integer> l) {
        Collections.sort(l);
        double median;
        if (l.size() % 2 == 0) {
            median = (l.get(l.size()/2) + l.get(l.size()/2 - 1))/2;
        } else {
            median = l.get(l.size()/2);
        }
        return median;
    }
}
