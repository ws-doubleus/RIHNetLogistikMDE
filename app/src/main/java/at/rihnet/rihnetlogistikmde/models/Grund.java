package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

public class Grund {
    private String grund;
    private String type;
    private Integer order;

    public Grund(String grund, String type, Integer order) {
        this.grund = grund;
        this.type = type;
        this.order = order;
    }

    public String getGrund() {
        return grund;
    }

    public void setGrund(String grund) {
        this.grund = grund;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @NonNull
    @Override
    public String toString() {
        return grund;
    }
}
