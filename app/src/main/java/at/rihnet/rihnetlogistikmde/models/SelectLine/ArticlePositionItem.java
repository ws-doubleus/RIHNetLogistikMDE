package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ArticlePositionItem {
    @JsonProperty("ArticleNumber")
    private String articleNumber;

    @JsonProperty("CalculatedQuantityValue")
    private double calculatedQuantityValue;

    @JsonProperty("WarehouseId")
    private String warehouseId;

    @JsonProperty("StoreInformation")
    private List<DocumentPositionStoreInformationUpdate> storeInformation;

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public double getCalculatedQuantityValue() {
        return calculatedQuantityValue;
    }

    public void setCalculatedQuantityValue(double calculatedQuantityValue) {
        this.calculatedQuantityValue = calculatedQuantityValue;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public List<DocumentPositionStoreInformationUpdate> getStoreInformation() {
        return storeInformation;
    }

    public void setStoreInformation(List<DocumentPositionStoreInformationUpdate> storeInformation) {
        this.storeInformation = storeInformation;
    }
}
