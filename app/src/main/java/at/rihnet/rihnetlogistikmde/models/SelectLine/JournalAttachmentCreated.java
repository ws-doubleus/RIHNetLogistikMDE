package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalAttachmentCreated {
    @JsonProperty("JournalIdentifier")
    private String journalIdentifier;

    @JsonProperty("AttachmentId")
    private int attachmentId;

    public String getJournalIdentifier() {
        return journalIdentifier;
    }

    public void setJournalIdentifier(String journalIdentifier) {
        this.journalIdentifier = journalIdentifier;
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }
}
