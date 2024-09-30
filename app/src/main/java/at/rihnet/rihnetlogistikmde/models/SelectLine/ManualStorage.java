package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ManualStorage {
    @JsonProperty("Date")
    private Date date;

    @JsonProperty("WarehouseNumber")
    private String warehouseNumber;

    @JsonProperty("BusinessPartnerType")
    private String businessPartnerType;

    @JsonProperty("BusinessPartner")
    private BusinessPartnerDetails businessPartner;

    @JsonProperty("WarehouseLocationNumber")
    private String warehouseLocationNumber;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getWarehouseNumber() {
        return warehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.warehouseNumber = warehouseNumber;
    }

    public String getBusinessPartnerType() {
        return businessPartnerType;
    }

    public void setBusinessPartnerType(String businessPartnerType) {
        this.businessPartnerType = businessPartnerType;
    }

    public BusinessPartnerDetails getBusinessPartner() {
        return businessPartner;
    }

    public void setBusinessPartner(BusinessPartnerDetails businessPartner) {
        this.businessPartner = businessPartner;
    }

    public String getWarehouseLocationNumber() {
        return warehouseLocationNumber;
    }

    public void setWarehouseLocationNumber(String warehouseLocationNumber) {
        this.warehouseLocationNumber = warehouseLocationNumber;
    }
}
