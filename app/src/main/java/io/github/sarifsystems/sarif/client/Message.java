package io.github.sarifsystems.sarif.client;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public String version;
    public String id;
    public String action;
    public String source;
    public String destination;
    public String corrId;
    public String text;

    public JSONObject payload;

    public static Message decode(String json) throws JSONException {
        JSONObject raw = new JSONObject(json);
        Message msg = new Message();

        msg.version = raw.getString("sarif");
        msg.id = raw.getString("id");
        msg.action = raw.getString("action");
        msg.source = raw.getString("src");

        msg.destination = raw.optString("dst");
        msg.corrId = raw.optString("corr");
        msg.text = raw.optString("text");
        msg.payload = raw.optJSONObject("p");

        return msg;
    }

    public String encode() throws JSONException {
        JSONObject raw = new JSONObject();

        raw.put("sarif", this.version);
        raw.put("id", this.id);
        raw.put("action", this.action);
        raw.put("src", this.source);

        raw.putOpt("dst", this.destination);
        raw.putOpt("corr", this.corrId);
        raw.putOpt("text", this.text);
        raw.putOpt("p", this.payload);

        return raw.toString();
    }

    public boolean isAction(String action) {
        return (this.action + "/").startsWith(action + "/");
    }
}