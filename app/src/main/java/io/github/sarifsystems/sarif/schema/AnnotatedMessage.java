package io.github.sarifsystems.sarif.schema;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
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

    public AnnotatedMessage() {
        super();
    }

    public AnnotatedMessage(Message msg) {
        super();

        try {
            unmarshal(msg.encode());
            Log.d("Sarif", msg.encode());
            parsePayload();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parsePayload() {
        Log.d("Sarif", "payload?");
        if (payload == null) {
            return;
        }

        Log.d("Sarif", "payload found");

        if (payload.has("attachments")) {
            Gson gson = new GsonBuilder().create();
            Log.d("Sarif", "attachments");
            try {
                Attachment[] a = gson.fromJson(payload.getJSONArray("attachments").toString(), Attachment[].class);
                attachments = Arrays.asList(a);
                Log.d("Sarif", "attachments num " + attachments.size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
