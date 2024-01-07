package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomFields1 {
    @JsonProperty("CustomText1")
    private String CustomText1;

    @JsonProperty("CustomText2")
    private String CustomText2;

    public String getCustomText1() {
        return CustomText1;
    }

    public void setCustomText1(String customText1) {
        CustomText1 = customText1;
    }

    public String getCustomText2() {
        return CustomText2;
    }

    public void setCustomText2(String customText2) {
        CustomText2 = customText2;
    }
}
