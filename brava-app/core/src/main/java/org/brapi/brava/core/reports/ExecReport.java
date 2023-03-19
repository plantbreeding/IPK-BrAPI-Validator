package org.brapi.brava.core.reports;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Report from one "exec" command.
 */
@Setter
@Getter
@RequiredArgsConstructor
@NoArgsConstructor
public class ExecReport implements Report {
    @NonNull
    private String name;
    private boolean passed;

    private String type;
    private String schema;
    @Setter(AccessLevel.NONE)
    private List<String> message = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private List<JsonNode> error = new ArrayList<JsonNode>();

    public void addMessage(String message) {
        this.message.add(message);
    }

    public List<JsonNode> getError() {
        return error.subList(0, Math.min(error.size(), 10));
    }

    public void addError(JsonNode error) {
        this.error.add(error);
    }
}
