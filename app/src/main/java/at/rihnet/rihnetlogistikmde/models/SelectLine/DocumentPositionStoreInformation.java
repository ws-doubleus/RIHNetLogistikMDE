package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class DocumentPositionStoreInformation {
    @JsonProperty("Quantity")
    private double Quantity;
    @JsonProperty("ArticleNumber")
    private String ArticleNumber;
    @JsonProperty("Warehouse")
    private String Warehouse;
    @JsonProperty("StoragePlaceIdentifier")
    private int StoragePlaceIdentifier;
    @JsonProperty("SerialNumber")
    private String SerialNumber;
    @JsonProperty("ExpirationDate")
    private Date ExpirationDate;
    @JsonProperty("TargetWarehouse")
    private String TargetWarehouse;
    @JsonProperty("TargetStoragePlaceIdentifier")
    private int TargetStoragePlaceIdentifier;

    public double getQuantity() {
        return Quantity;
    }

    public void setQuantity(double quantity) {
        this.Quantity = quantity;
    }

    public String getArticleNumber() {
        return ArticleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.ArticleNumber = articleNumber;
    }

    public String getWarehouse() {
        return Warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.Warehouse = warehouse;
    }

    public int getStoragePlaceIdentifier() {
        return StoragePlaceIdentifier;
    }

    public void setStoragePlaceIdentifier(int storagePlaceIdentifier) {
        this.StoragePlaceIdentifier = storagePlaceIdentifier;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.SerialNumber = serialNumber;
    }

    public Date getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.ExpirationDate = expirationDate;
    }

    public String getTargetWarehouse() {
        return TargetWarehouse;
    }

    public void setTargetWarehouse(String targetWarehouse) {
        this.TargetWarehouse = targetWarehouse;
    }

    public int getTargetStoragePlaceIdentifier() {
        return TargetStoragePlaceIdentifier;
    }

    public void setTargetStoragePlaceIdentifier(int targetStoragePlaceIdentifier) {
        this.TargetStoragePlaceIdentifier = targetStoragePlaceIdentifier;
    }
}
