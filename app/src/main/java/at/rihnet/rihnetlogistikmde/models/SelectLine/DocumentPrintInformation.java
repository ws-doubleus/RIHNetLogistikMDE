package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentPrintInformation {
    @JsonProperty("NumberOfCopies")
    private int numberOfCopies;

    @JsonProperty("MasterName")
    private String masterName;

    @JsonProperty("PrintTarget")
    private PrintTarget printTarget;

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public PrintTarget getPrintTarget() {
        return printTarget;
    }

    public void setPrintTarget(PrintTarget printTarget) {
        this.printTarget = printTarget;
    }
}
