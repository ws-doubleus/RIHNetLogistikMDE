package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public class Inventory implements Serializable {
    @JsonProperty("Number")
    private String number;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Comment")
    private String comment;

    @JsonProperty("Date")
    private Date date;

    @JsonProperty("Employee")
    private String employee;

    @JsonProperty("Location")
    private String location;

    @JsonProperty("KindFlag")
    private int kindFlag;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getKindFlag() {
        return kindFlag;
    }

    public void setKindFlag(int kindFlag) {
        this.kindFlag = kindFlag;
    }
}
