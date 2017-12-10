package io.github.sarifsystems.sarif;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.github.sarifsystems.sarif.schema.Message;
import io.github.sarifsystems.sarif.schema.Attachment;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;

    public MessageAdapter() {
        messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == Message.TYPE_OUTGOING) {
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
        Message msg = messages.get(i);
        String text = msg.getText();
        if (text == null || text.isEmpty()) {
            text = "/" + msg.getAction();
        }

        String time = (new SimpleDateFormat("HH:mm")).format(msg.getTime());
        if (getItemViewType(i) == Message.TYPE_OUTGOING) {
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

