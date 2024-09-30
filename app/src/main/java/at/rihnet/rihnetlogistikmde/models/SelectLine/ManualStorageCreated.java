package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

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
