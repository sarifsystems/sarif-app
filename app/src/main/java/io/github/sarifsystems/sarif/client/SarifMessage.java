package io.github.sarifsystems.sarif.client;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class SarifMessage {

    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @SerializedName("sarif")
    public String version;

    public String id;

    public String action;

    @SerializedName("src")
    public String source;

    @SerializedName("dst")
    public String destination;

    @SerializedName("corr")
    public String corrId;

    public String text;

    @SerializedName("p")
    public JsonElement payload;

    public SarifMessage() {

    }

    public SarifMessage(String action) {
        this.action = action;
    }


    public SarifMessage(String action, JsonElement payload) {
        this.action = action;
        this.payload = payload;
    }

    public boolean isAction(String action) {
        return (this.action + "/").startsWith(action + "/");
    }

    public String getText() {
        if (text == null || text.isEmpty()) {
            return action + " from " + source;
        }
        return text;
    }

    public String toString() {
        String s = "#" + this.id + " " + this.action;
        if (text != null && !text.isEmpty()) {
            s += " \"" + text + "\"";
        }
        return s;
    }
}