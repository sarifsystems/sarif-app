package io.github.sarifsystems.sarif;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.sarifsystems.sarif.client.Message;
import io.github.sarifsystems.sarif.client.SarifClientListener;
import io.github.sarifsystems.sarif.service.SarifService;


public class MessagingFragment extends Fragment implements SarifClientListener, SarifService.MessageReceiver {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView messagesList;
    private MessageAdapter messageAdapter;
    private SarifAwareActivity sarifActivity;

    public MessagingFragment() {}

    public static MessagingFragment newInstance(String param1, String param2) {
        MessagingFragment fragment = new MessagingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.messaging, container, false);
        messagesList = (RecyclerView) v.findViewById(R.id.listMessages);
        messagesList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter();
        messagesList.setAdapter(messageAdapter);

        final EditText messageBodyField = (EditText) v.findViewById(R.id.messageBodyField);
        final Button sendButton = (Button) v.findViewById(R.id.sendButton);
        messageBodyField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendButton.performClick();
                    return true;
                }
                return false;
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = messageBodyField.getText().toString();
                messageBodyField.setText("");
                publishMessage(body);
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SarifAwareActivity) {
            sarifActivity = (SarifAwareActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must extend SarifAwareActivity");
        }
        sarifActivity.addSarifListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sarifActivity.removeListener(this);
        sarifActivity = null;
    }

    protected void publishMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        Message msg = new Message();
        msg.action = "natural/handle";
        msg.text = message;

        sarifActivity.getSarif().request(msg, this);
        messageAdapter.addMessage(msg, MessageAdapter.DIR_OUTGOING);
        messagesList.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onMessageReceived(final Message msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.addMessage(msg, MessageAdapter.DIR_INCOMING);
                messagesList.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onConnectionLost(Exception e) {

    }
}
