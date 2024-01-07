package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class BusinessPartnerDetails {
    @JsonProperty("Address")
    private DocumentDetailAddress Address;

    public DocumentDetailAddress getAddress() {
        return Address;
    }

    public void setAddress(DocumentDetailAddress address) {
        Address = address;
    }
}
