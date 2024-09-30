package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomFields1 {
    @JsonProperty("CustomText1")
    private String customText1;

    @JsonProperty("CustomText2")
    private String customText2;

    public String getCustomText1() {
        return customText1;
    }

    public void setCustomText1(String customText1) {
        this.customText1 = customText1;
    }

    public String getCustomText2() {
        return customText2;
    }

    public void setCustomText2(String customText2) {
        this.customText2 = customText2;
    }
}
