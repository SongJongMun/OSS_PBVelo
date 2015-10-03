package com.tekinarslan.material.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.Button;

/**
 * Created by JUNYOUNG on 2015-09-07.
 */
public class SelectFriendActivity extends Activity {
    private String mUsername;
    private Switch btnSlide;
    private EditText textEditer;
    private Button toRoomBtn;
    private boolean toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toggle = false;

        setContentView(R.layout.activity_select_friend);

        toRoomBtn = (Button) this.findViewById(R.id.btnToRoom);
        textEditer = (EditText) this.findViewById(R.id.editText);
        textEditer.setVisibility(View.INVISIBLE);

        Intent intent=new Intent(this.getIntent());
        mUsername = intent.getStringExtra("username");


        btnSlide = (Switch)this.findViewById(R.id.switch1);
        btnSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!toggle)
                    textEditer.setVisibility(View.VISIBLE);
                else
                    textEditer.setVisibility(View.INVISIBLE);

                toggle=!toggle;
            }
        });

        toRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRoom(toggle);
            }
        });
    }

    public void toRoom(boolean toogle){
        EditText roomName;
        Intent intent = new Intent(this, NMapViewer.class);
        intent.putExtra("username", mUsername);
        for(int i=1; i<=6; i++){
            intent.putExtra("button"+i, "button"+i);
        }
        Log.e("state", " switch : " + toggle);
        if(toggle) {
            roomName = (EditText) findViewById(R.id.editText);
            String RN = roomName.getText().toString();

            Log.e("state", " RN : " + RN);

            if(roomName.length() == 0)
                Log.e("RoomName", "length is 0");
            else {
                intent.putExtra("isAdmin", "false");
                intent.putExtra("roomName", RN);
                startActivity(intent);
            }
        } else {
            intent.putExtra("isAdmin", "true");
            startActivity(intent);
        }
    }
}
