package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentPositionReadModel implements Serializable {
    @JsonProperty("Identifier")
    private String identifier;

    @JsonProperty("PredecessorIdentifier")
    private String predecessorIdentifier;

    @JsonProperty("Quantity")
    private double quantity;

    @JsonProperty("ArticleNumber")
    private String articleNumber;

    @JsonProperty("IsWarehouseArticle")
    private boolean isWarehouseArticle;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPredecessorIdentifier() {
        return predecessorIdentifier;
    }

    public void setPredecessorIdentifier(String predecessorIdentifier) {
        this.predecessorIdentifier = predecessorIdentifier;
    }

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

    public boolean isWarehouseArticle() {
        return isWarehouseArticle;
    }

    public void setWarehouseArticle(boolean warehouseArticle) {
        this.isWarehouseArticle = warehouseArticle;
    }
}
