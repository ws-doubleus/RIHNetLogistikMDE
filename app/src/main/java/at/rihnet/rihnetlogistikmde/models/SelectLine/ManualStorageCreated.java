package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManualStorageCreated {
    @JsonProperty("ManualStorageNumber")
    private String manualStorageNumber;

    public String getManualStorageNumber() {
        return manualStorageNumber;
    }

    public void setManualStorageNumber(String manualStorageNumber) {
        this.manualStorageNumber = manualStorageNumber;
    }
}
