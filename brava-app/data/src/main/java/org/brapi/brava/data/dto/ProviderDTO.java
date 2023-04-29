package org.brapi.brava.data.dto;

import lombok.*;

/**
 * A provider instance contains the information related to one resource provider
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class ProviderDTO {

    private String id;
    @NonNull
    private String name;
    private String email;
    private String description;
    private String logo;
}
