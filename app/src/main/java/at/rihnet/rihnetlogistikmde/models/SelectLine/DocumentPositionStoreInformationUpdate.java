package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentPositionStoreInformationUpdate implements Serializable {
    @JsonProperty("ArticleNumber")
    private String articleNumber;

    @JsonProperty("Quantity")
    private double quantity;

    @JsonProperty("Identifier")
    private String identifier;

    @JsonProperty("SerialNumber")
    private String serialNumber;

    @JsonProperty("StoragePlaceIdentifier")
    private int storagePlaceIdentifier;

    @JsonProperty("Warehouse")
    private String warehouse;

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getStoragePlaceIdentifier() {
        return storagePlaceIdentifier;
    }

    public void setStoragePlaceIdentifier(int storagePlaceIdentifier) {
        this.storagePlaceIdentifier = storagePlaceIdentifier;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
}
