package jp.co.yahoo.appfeedback.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by taicsuzu on 2016/10/03.
 */

public class TabFragment extends Fragment{

    private int position;

    public TabFragment setPosition(int position){
        this.position = position;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tab, null);

        ((TextView)root.findViewById(R.id.tab_text)).setText("Page: "+position);

        return root;
    }
}
