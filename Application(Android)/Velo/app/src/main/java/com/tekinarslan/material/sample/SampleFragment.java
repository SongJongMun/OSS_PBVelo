package com.tekinarslan.material.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SampleFragment extends Fragment {

    private static ArrayList<String> mListItems;

    private ListView mListView;

    private static final String ARG_POSITION = "position";

    private int position;

    public static SampleFragment newInstance(int position) {
        SampleFragment f = new SampleFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListItems = new ArrayList<String>();

        for (int i = 1; i <= 100; i++) {
            mListItems.add(i + ". item - currnet page: " + (position + 1));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        position = getArguments().getInt(ARG_POSITION);
        View rootView = inflater.inflate(R.layout.page, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fabButton);
        fab.setDrawableIcon(getResources().getDrawable(R.drawable.plus));
        switch (position) {
            case 0:
                rootView = inflater.inflate(R.layout.memberlist, container, false);
                mListView = (ListView) rootView.findViewById(R.id.listView);
//                fab.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                break;
            case 1:
                rootView = inflater.inflate(R.layout.message, container, false);
                fab = (FloatingActionButton) rootView.findViewById(R.id.fabButton);
                fab.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                fab.setDrawableIcon(getResources().getDrawable(R.drawable.plus));
                break;
            case 2:
                rootView = inflater.inflate(R.layout.logroom, container, false);
                break;
            case 3:
                fab.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
                //progressBarCircular.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
                break;
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      if(mListView!=null) {
          mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item, android.R.id.text1, mListItems));
      }
    }

}