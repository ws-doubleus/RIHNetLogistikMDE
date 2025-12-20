package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PredecessorDocumentData {
    @JsonProperty("Positions")
    private List<DocumentPositionStoreInformationUpdate> positions;

    public List<DocumentPositionStoreInformationUpdate> getPositions() {
        return positions;
    }

    public void setPositions(List<DocumentPositionStoreInformationUpdate> positions) {
        this.positions = positions;
    }
}
