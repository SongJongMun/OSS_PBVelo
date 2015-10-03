package com.tekinarslan.material.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tekinarslan.material.sample.helper.SQLiteHandler;
import com.tekinarslan.material.sample.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private TextView txtName;
    private TextView txtEmail;
    private android.widget.Button btnLogout;
    private android.widget.Button btnPageSlide;

    private SQLiteHandler db;
    private SessionManager session;

    private String mUsername;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=new Intent(this.getIntent());

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.id);
        btnLogout = (android.widget.Button) findViewById(R.id.btnLogout);
        btnPageSlide = (android.widget.Button) findViewById(R.id.btnPageSlide);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();


        //String name = user.get("name");
        //String id = user.get("email");

        String name ;
        String id;


        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        // Displaying the user details on the screen
        //txtName.setText(name);
        //txtEmail.setText(id);

        mUsername = name;
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("login", onLogin);
        mSocket.on("invite", inviteRoom);
        mSocket.connect();

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        btnPageSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPageSlide();
            }
        });
        socketLogin();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //라이딩시작
    private void toPageSlide() {
        Intent intent = new Intent(this, SelectFriendActivity.class);
        intent.putExtra("username", mUsername);
        startActivity(intent);
    }

//    //기록실
//    public void toLogRoom(View view) {
//        Intent intent = new Intent(this, LogRoomActivity.class);
//        startActivity(intent);
//    }
//
//    //설정
//    public void toSetting(View view) {
//        Intent intent = new Intent(this, SettingActivity.class);
//        startActivity(intent);
//    }
//
//    //친구목록
//    public void toFriendList(View view){
//        Intent myIntent = new Intent(this, FriendsListActivity.class);
//        startActivity(myIntent);
//    }

    private void socketLogin(){
        // Socket Server Login
        if(mSocket.connected()) {
            mSocket.emit("add user", mUsername);
            Toast.makeText(getApplicationContext(), "send addUser Message", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_LONG).show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("SocketState : ", String.valueOf(mSocket.connected()));
                    if(!mSocket.connected()){
                        mSocket.connect();
                    } else {
                        mSocket.emit("add user", mUsername);
                    }
                }
            }, 3000);
        }
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            //Log.d(TAG, "Socket Reseive : Login");
            JSONObject data = (JSONObject) args[0];

            int numUsers;

            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }
            //Toast.makeText(MainActivity.this, "Join Success : " + mUsername + " / " + numUsers, Toast.LENGTH_LONG).show();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Socket", "Error : Socket onConnectError");
        }
    };

    private Emitter.Listener inviteRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Socket", "Invete From SOmeone");
        }
    };
}