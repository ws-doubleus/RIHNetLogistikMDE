package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ManualStorageCreated {
    @JsonProperty("ManualStorageNumber")
    private String ManualStorageNumber;

    public String getManualStorageNumber() {
        return ManualStorageNumber;
    }

    public void setManualStorageNumber(String manualStorageNumber) {
        this.ManualStorageNumber = manualStorageNumber;
    }
}
