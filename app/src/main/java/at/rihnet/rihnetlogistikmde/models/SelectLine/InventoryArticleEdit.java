package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class InventoryArticleEdit {
    @JsonProperty("Quantity")
    private double quantity;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("ExpirationDate")
    private Date expirationDate;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonProperty("PriceQuantity")
    private double priceQuantity;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("Serialnumber")
    private String serialnumber;

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public double getPriceQuantity() {
        return priceQuantity;
    }

    public void setPriceQuantity(double priceQuantity) {
        this.priceQuantity = priceQuantity;
    }

    public String getSerialnumber() {
        return serialnumber;
    }

    public void setSerialnumber(String serialnumber) {
        this.serialnumber = serialnumber;
    }
}
