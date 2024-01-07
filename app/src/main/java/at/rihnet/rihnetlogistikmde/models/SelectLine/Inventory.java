package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Inventory {
    @JsonProperty("Number")
    private String Number;

    @JsonProperty("Status")
    private String Status;

    @JsonProperty("Comment")
    private String Comment;

    @JsonProperty("Date")
    private Date Date;
}
