package io.github.sarifsystems.sarif.schema;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.sarifsystems.sarif.client.SarifMessage;

@Entity
public class Message {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    private String version;
    private String id;
    private String action;
    private String source;
    private String destination;
    private String corrId;
    private String text;

    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;

    private int type = TYPE_INCOMING;
    private Date time;

    private JsonObject payload;

    @Ignore
    public List<Attachment> attachments = new ArrayList<>();

    public Message() {
        super();
        setTime(new Date());
        setPayload(new JsonObject());
    }

    public Message(SarifMessage msg) {
        setVersion(msg.version);
        setId(msg.id);
        setAction(msg.action);
        setSource(msg.source);
        setDestination(msg.destination);
        setCorrId(msg.corrId);
        setText(msg.text);
        if (msg.payload != null && msg.payload.isJsonObject()) {
            setPayload(msg.payload.getAsJsonObject().deepCopy());
        } else {
            setPayload(new JsonObject());
        }

        setTime(new Date());
    }

    public SarifMessage toMessage() {
        SarifMessage msg = new SarifMessage();

        msg.version = getVersion();
        msg.id = getId();
        msg.action = getAction();
        msg.source = getSource();
        msg.destination = getDestination();
        msg.corrId = getCorrId();
        msg.text = getText();
        if (!getPayload().keySet().isEmpty()) {
            msg.payload = getPayload().deepCopy();
        }

        return msg;
    }

    public void parsePayload() {
        if (payload.has("attachments")) {
            Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

            Attachment[] a = gson.fromJson(payload.getAsJsonArray("attachments"), Attachment[].class);
            attachments = Arrays.asList(a);
        }
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCorrId() {
        return corrId;
    }

    public void setCorrId(String corrId) {
        this.corrId = corrId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public void setPayload(JsonObject payload) {
        this.payload = payload;
        parsePayload();
    }
}