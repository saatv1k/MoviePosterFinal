package com.example.movieposterfinal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentPlot extends Fragment {

    ReceiveString receiveString;

    TextView summaryText;
    TextView summary;

    public FragmentPlot(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentPlotView = inflater.inflate(R.layout.activity_info,null);
        summaryText = fragmentPlotView.findViewById(R.id.id_summaryText);
        summary = fragmentPlotView.findViewById(R.id.id_summary);
        return fragmentPlotView;
    }

    public interface ReceiveString{
        void receive(String str);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        receiveString = (ReceiveString) context;
    }
}
