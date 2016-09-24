package io.github.sarifsystems.sarif;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.github.sarifsystems.sarif.schema.AnnotatedMessage;
import io.github.sarifsystems.sarif.schema.Attachment;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AnnotatedMessage> messages;

    public MessageAdapter() {
        messages = new ArrayList<>();
    }

    public void addMessage(AnnotatedMessage message) {
        messages.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == AnnotatedMessage.TYPE_OUTGOING) {
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
        AnnotatedMessage msg = messages.get(i);
        String text = msg.text;
        if (text == null || text.isEmpty()) {
            text = "." + msg.action;
        }

        String time = (new SimpleDateFormat("HH:mm")).format(msg.time);
        if (getItemViewType(i) == AnnotatedMessage.TYPE_OUTGOING) {
            MessageViewHolder h = (MessageViewHolder) holder;
            h.messageText.setText(text);
            h.messageInfo.setText(time);
        } else {
            CardViewHolder h = (CardViewHolder) holder;
            h.messageText.setText(text);
            h.messageInfo.setText(time);
            h.tiles.removeAllViews();

            if (msg.attachments != null) {
                boolean first = true;
                for (Attachment attach : msg.attachments) {
                    LayoutInflater inflater = LayoutInflater.from(h.tiles.getContext());
                    View tile = inflater.inflate(R.layout.tile_attachment, h.tiles, false);

                    if (!first) {
                        tile.findViewById(R.id.divider).setVisibility(View.GONE);
                    }

                    setTextOrHide(tile.findViewById(R.id.attach_author), attach.authorMarkup(), true);
                    setTextOrHide(tile.findViewById(R.id.attach_title), attach.titleMarkup(), true);
                    setTextOrHide(tile.findViewById(R.id.attach_text), attach.text, false);
                    setTextOrHide(tile.findViewById(R.id.attach_footer), attach.footer, false);

                    ImageView img = (ImageView) tile.findViewById(R.id.attach_image);
                    if (attach.imageUrl != null && !attach.imageUrl.isEmpty()) {
                        Picasso.with(h.tiles.getContext()).load(attach.imageUrl).into(img);
                    } else {
                        img.setVisibility(View.GONE);
                    }

                    h.tiles.addView(tile, h.tiles.getChildCount(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    first = false;
                }
            }
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageInfo;
        public LinearLayout tiles;

        public CardViewHolder(View v) {
            super(v);

            messageText = (TextView) v.findViewById(R.id.message_text);
            messageInfo = (TextView) v.findViewById(R.id.message_info);
            tiles = (LinearLayout) v.findViewById(R.id.message_tiles);
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

    public static void setTextOrHide(View v, String text, boolean isHtml) {
        TextView tv = (TextView) v;
        if (text != null && !text.isEmpty()) {
            if (isHtml) {
                tv.setText(Html.fromHtml(text));
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                tv.setText(text);
            }
        } else {
            tv.setVisibility(View.GONE);
        }
    }
}

