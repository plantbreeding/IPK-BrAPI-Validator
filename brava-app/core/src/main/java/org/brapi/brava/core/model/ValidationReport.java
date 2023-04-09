package org.brapi.brava.core.model;

import lombok.*;

import java.util.Date;
import java.util.Map;

/**
 * A complete summary of a validation run, containing the URL that was validated
 * a short report, a mini report, the date the resource was validated and the complete report
 * details as a json string
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReport {
    @NonNull
    private String reportId ;
    private String resourceId ;
    @NonNull
    private String resourceUrl ;
    @NonNull
    private String reportJson ;
    @NonNull
    private Map<String, Map<String, Object>> shortReport ;
    @NonNull
    private MiniReport miniReport ;
    @NonNull
    private Date date;
}
