package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentPositionCreated {
    @JsonProperty("DocumentKey")
    private String documentKey;

    @JsonProperty("DocumentKind")
    private String documentKind;

    @JsonProperty("DocumentNumber")
    private String documentNumber;

    @JsonProperty("PositionIdentifier")
    private String positionIdentifier;

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public String getDocumentKind() {
        return documentKind;
    }

    public void setDocumentKind(String documentKind) {
        this.documentKind = documentKind;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPositionIdentifier() {
        return positionIdentifier;
    }

    public void setPositionIdentifier(String positionIdentifier) {
        this.positionIdentifier = positionIdentifier;
    }
}
