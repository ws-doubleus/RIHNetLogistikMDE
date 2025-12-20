package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentPositionUpdateModel {
    @JsonProperty("ArticleNumber")
    private String articleNumber;

    @JsonProperty("CalculatedQuantityValue")
    private double calculatedQuantityValue;

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

    public List<DocumentPositionStoreInformationUpdate> getStoreInformation() {
        return storeInformation;
    }

    public void setStoreInformation(List<DocumentPositionStoreInformationUpdate> storeInformation) {
        this.storeInformation = storeInformation;
    }
}
