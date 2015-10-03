package com.tekinarslan.material.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class FriendsListActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        ArrayList<Listviewitem> data=new ArrayList();
        Listviewitem lion=new Listviewitem(R.drawable.lion,"Lion");
        Listviewitem tiger=new Listviewitem(R.drawable.tiger,"Tiger");
        Listviewitem dog=new Listviewitem(R.drawable.dog,"Dog");
        Listviewitem cat=new Listviewitem(R.drawable.cat,"Cat");

        data.add(lion);
        data.add(tiger);
        data.add(dog);
        data.add(cat);

        ListviewAdapter adapter1=new ListviewAdapter(this,R.layout.listviewitem,data);
        listView = (ListView) findViewById(R.id.friendList);
        listView.setAdapter(adapter1);
    }
}
