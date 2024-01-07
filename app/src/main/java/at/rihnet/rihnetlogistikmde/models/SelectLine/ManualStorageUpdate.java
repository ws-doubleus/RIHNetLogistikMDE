package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ManualStorageUpdate {
    @JsonProperty("CustomFields")
    private CustomFields1 CustomFields;

    public CustomFields1 getCustomFields() {
        return CustomFields;
    }

    public void setCustomFields(CustomFields1 customFields) {
        CustomFields = customFields;
    }
}
