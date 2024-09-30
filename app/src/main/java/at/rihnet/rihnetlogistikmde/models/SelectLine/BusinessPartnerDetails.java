package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BusinessPartnerDetails {
    @JsonProperty("Address")
    private DocumentDetailAddress address;

    public DocumentDetailAddress getAddress() {
        return address;
    }

    public void setAddress(DocumentDetailAddress address) {
        this.address = address;
    }
}
