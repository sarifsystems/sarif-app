package io.github.sarifsystems.sarif;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.sarifsystems.sarif.client.Message;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int DIR_INCOMING = 0;
    public static final int DIR_OUTGOING = 1;

    private List<Pair<Message, Integer>> messages;

    public MessageAdapter() {
        messages = new ArrayList<>();
    }

    public void addMessage(Message message, int direction) {
        messages.add(new Pair(message, direction));
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == DIR_OUTGOING) {
            View v = inflater.inflate(R.layout.message_right, parent, false);
            RecyclerView.ViewHolder vh = new MessageViewHolder(v);
            return vh;
        }
        View v = inflater.inflate(R.layout.card, parent, false);
        RecyclerView.ViewHolder vh = new CardViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        Message msg = messages.get(i).first;
        String text = msg.text;
        if (text == null || text.isEmpty()) {
            text = "." + msg.action;
        }

        if (getItemViewType(i) == DIR_OUTGOING) {
            MessageViewHolder h = (MessageViewHolder) holder;
            h.messageText.setText(text);
            h.messageInfo.setText("12:03");
        } else {
            CardViewHolder h = (CardViewHolder) holder;
            h.messageText.setText(text);
            h.messageInfo.setText("09:13");
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageInfo;

        public CardViewHolder(View v) {
            super(v);

            messageText = (TextView) v.findViewById(R.id.message_text);
            messageInfo = (TextView) v.findViewById(R.id.message_info);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageInfo;

        public MessageViewHolder(View v) {
            super(v);

            messageText = (TextView) v.findViewById(R.id.txtMessage);
            messageInfo = (TextView) v.findViewById(R.id.txtDate);
        }
    }
}

