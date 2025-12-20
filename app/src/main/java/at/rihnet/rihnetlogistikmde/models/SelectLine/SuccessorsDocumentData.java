package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SuccessorsDocumentData {
    @JsonProperty("DocumentKindDestination")
    private String documentKindDestination;

    @JsonProperty("Positions")
    private List<DocumentPositionStoreInformationUpdate> positions;

    public String getDocumentKindDestination() {
        return documentKindDestination;
    }

    public void setDocumentKindDestination(String documentKindDestination) {
        this.documentKindDestination = documentKindDestination;
    }

    public List<DocumentPositionStoreInformationUpdate> getPositions() {
        return positions;
    }

    public void setPositions(List<DocumentPositionStoreInformationUpdate> positions) {
        this.positions = positions;
    }
}
