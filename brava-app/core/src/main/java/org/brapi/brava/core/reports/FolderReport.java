package org.brapi.brava.core.reports;

import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Homologous of config.Folder, groups Item reports
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class FolderReport implements Report  {

    private String name;

    @NonNull
    private String url;

    private String description;
    private int total;
    private int fails;

    @Setter(AccessLevel.NONE)
    private List<ItemReport> itemReports = new ArrayList<>();
    private LinkedHashMap<String, Object> testsShort = new LinkedHashMap<String, Object>();

    /**
     * @param itemReport Test report to be added to the folder
     */
    public void addItemReport(ItemReport itemReport) {
        this.itemReports.add(itemReport);
    }
}
