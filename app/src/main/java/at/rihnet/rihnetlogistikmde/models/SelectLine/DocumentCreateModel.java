package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;



public class DocumentCreateModel {
    @JsonProperty("KindFlag")
    private String kindFlag;

    @JsonProperty("Date")
    private Date date;

    @JsonProperty("BusinessPartner")
    private DocumentDetailBusinessPartner businessPartner;

    @JsonProperty("WarehouseNumber")
    private String warehouseNumber;

    @JsonProperty("DeliveryDocumentNumber")
    private String deliveryDocumentNumber;

    public String getKindFlag() {
        return kindFlag;
    }

    public void setKindFlag(String kindFlag) {
        this.kindFlag = kindFlag;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DocumentDetailBusinessPartner getBusinessPartner() {
        return businessPartner;
    }

    public void setBusinessPartner(DocumentDetailBusinessPartner businessPartner) {
        this.businessPartner = businessPartner;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public String getDeliveryDocumentNumber() {
        return deliveryDocumentNumber;
    }

    public void setDeliveryDocumentNumber(String deliveryDocumentNumber) {
        this.deliveryDocumentNumber = deliveryDocumentNumber;
    }
}
