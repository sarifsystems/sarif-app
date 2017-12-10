package io.github.sarifsystems.sarif;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.sarifsystems.sarif.client.SarifMessage;
import io.github.sarifsystems.sarif.client.SarifClientListener;

public class CardsFragment extends Fragment implements SarifClientListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SarifAwareActivity sarifActivity;

    public CardsFragment() {
        // Required empty public constructor
    }

    public static CardsFragment newInstance(String param1, String param2) {
        CardsFragment fragment = new CardsFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cards, container, false);
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

    @Override
    public void onConnected() {

    }

    @Override
    public void onMessageReceived(SarifMessage msg) {

    }

    @Override
    public void onConnectionLost(Exception e) {

    }
}
