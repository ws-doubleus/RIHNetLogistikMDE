package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class DocumentPositionStoreInformation {
    @JsonProperty("Quantity")
    private double quantity;

    @JsonProperty("ArticleNumber")
    private String articleNumber;

    @JsonProperty("Warehouse")
    private String warehouse;

    @JsonProperty("StoragePlaceIdentifier")
    private int storagePlaceIdentifier;

    @JsonProperty("SerialNumber")
    private String serialNumber;

    @JsonProperty("ExpirationDate")
    private Date expirationDate;

    @JsonProperty("TargetWarehouse")
    private String targetWarehouse;

    @JsonProperty("TargetStoragePlaceIdentifier")
    private int targetStoragePlaceIdentifier;

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public int getStoragePlaceIdentifier() {
        return storagePlaceIdentifier;
    }

    public void setStoragePlaceIdentifier(int storagePlaceIdentifier) {
        this.storagePlaceIdentifier = storagePlaceIdentifier;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getTargetWarehouse() {
        return targetWarehouse;
    }

    public void setTargetWarehouse(String targetWarehouse) {
        this.targetWarehouse = targetWarehouse;
    }

    public int getTargetStoragePlaceIdentifier() {
        return targetStoragePlaceIdentifier;
    }

    public void setTargetStoragePlaceIdentifier(int targetStoragePlaceIdentifier) {
        this.targetStoragePlaceIdentifier = targetStoragePlaceIdentifier;
    }
}
