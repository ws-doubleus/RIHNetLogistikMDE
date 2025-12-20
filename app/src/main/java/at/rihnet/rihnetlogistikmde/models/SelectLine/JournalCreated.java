package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JournalCreated {
    @JsonProperty("JournalIdentifier")
    private String journalIdentifier;

    public String getJournalIdentifier() {
        return journalIdentifier;
    }

    public void setJournalIdentifier(String journalIdentifier) {
        this.journalIdentifier = journalIdentifier;
    }
}
