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

    public void unmarshal(String json) throws JSONException {
        JSONObject raw = new JSONObject(json);

        version = raw.optString("sarif");
        id = raw.optString("id");
        action = raw.optString("action");
        source = raw.optString("src");

        destination = raw.optString("dst");
        corrId = raw.optString("corr");
        text = raw.optString("text");
        payload = raw.optJSONObject("p");
    }

    public static Message decode(String json) throws JSONException {
        Message msg = new Message();
        msg.unmarshal(json);
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

    public String getText() {
        if (text == null || text.isEmpty()) {
            return action + "from " + source;
        }
        return text;
    }
}