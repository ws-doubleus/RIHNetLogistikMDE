package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentDetailBusinessPartner {
    @JsonProperty("ReferenceAddressNumber")
    private String referenceAddressNumber;

    @JsonProperty("Address")
    private DocumentDetailAddress address;

    public String getReferenceAddressNumber() {
        return referenceAddressNumber;
    }

    public void setReferenceAddressNumber(String referenceAddressNumber) {
        this.referenceAddressNumber = referenceAddressNumber;
    }

    public DocumentDetailAddress getAddress() {
        return address;
    }

    public void setAddress(DocumentDetailAddress address) {
        this.address = address;
    }
}
