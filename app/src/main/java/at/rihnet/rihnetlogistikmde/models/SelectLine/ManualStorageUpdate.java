package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ManualStorageUpdate {
    @JsonProperty("CustomFields")
    private CustomFields1 customFields;

    public CustomFields1 getCustomFields() {
        return customFields;
    }

    public void setCustomFields(CustomFields1 customFields) {
        this.customFields = customFields;
    }
}
