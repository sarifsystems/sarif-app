package io.github.sarifsystems.sarif.schema;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.sarifsystems.sarif.client.Message;

/**
 * Created by me on 9/7/2016.
 */
public class AnnotatedMessage extends Message {

    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;

    public List<Attachment> attachments = new ArrayList<>();
    public int type = TYPE_INCOMING;
    public Date time;

    public AnnotatedMessage() {
        super();
        time = new Date();
    }

    public AnnotatedMessage(Message msg) {
        super();

        time = new Date();
        try {
            unmarshal(msg.encode());
            parsePayload();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parsePayload() {
        if (payload == null) {
            return;
        }

        if (payload.has("attachments")) {
            Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
            try {
                Attachment[] a = gson.fromJson(payload.getJSONArray("attachments").toString(), Attachment[].class);
                attachments = Arrays.asList(a);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
