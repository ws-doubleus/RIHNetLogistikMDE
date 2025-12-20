package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class DocumentJournalModelCreate {
    @JsonProperty("Date")
    private String date;

    @JsonProperty("ContactKindIdentifier")
    private int contactKindIdentifier;

    @JsonProperty("StatusId")
    private int statusId;

    @JsonProperty("Label")
    private String label;

    @JsonProperty("Text")
    private String text;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getContactKindIdentifier() {
        return contactKindIdentifier;
    }

    public void setContactKindIdentifier(int contactKindIdentifier) {
        this.contactKindIdentifier = contactKindIdentifier;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
