package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class ManualStorage {
    @JsonProperty("Date")
    private Date Date;
    @JsonProperty("WarehouseNumber")
    private String WarehouseNumber;
    @JsonProperty("BusinessPartnerType")
    private String BusinessPartnerType;
    @JsonProperty("BusinessPartner")
    private BusinessPartnerDetails BusinessPartner;
    @JsonProperty("WarehouseLocationNumber")
    private String WarehouseLocationNumber;

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        this.Date = date;
    }

    public String getWarehouseNumber() {
        return WarehouseNumber;
    }

    public void setWarehouseNumber(String warehouseNumber) {
        this.WarehouseNumber = warehouseNumber;
    }

    public String getBusinessPartnerType() {
        return BusinessPartnerType;
    }

    public void setBusinessPartnerType(String businessPartnerType) {
        this.BusinessPartnerType = businessPartnerType;
    }

    public BusinessPartnerDetails getBusinessPartner() {
        return BusinessPartner;
    }

    public void setBusinessPartner(BusinessPartnerDetails businessPartner) {
        this.BusinessPartner = businessPartner;
    }

    public String getWarehouseLocationNumber() {
        return WarehouseLocationNumber;
    }

    public void setWarehouseLocationNumber(String warehouseLocationNumber) {
        this.WarehouseLocationNumber = warehouseLocationNumber;
    }
}
