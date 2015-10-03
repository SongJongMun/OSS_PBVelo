package com.tekinarslan.material.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;


public class SampleActivity extends ActionBarActivity {

    private boolean editBtnFlag = false;
    public String[] buttons = new String[6];
    private MemberListView mListView;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private TextView alertMessage;

    private ArrayAdapter<String> adapter;
    private ListView mDrawerList;
    ViewPager pager;
    private String titles[] = new String[]{"Member List", "Message", "LogRoom"};
    private Toolbar toolbar;

    SlidingTabLayout slidingTabLayout;

    private String isAdmin;
    private String roomName;
    private String mUsername;
    private JSONObject data;
    private Socket mSocket;
    private String memberList = "";
    private String[] memberArray = {};

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
        setContentView(R.layout.activity_sample);
        settingSocket();
        Intent intentFromMap = getIntent();

        for(int i=1; i<=buttons.length; i++){
            if(intentFromMap.getExtras().getString("button"+i).toString().equals("button"+i)){
                buttons[i-1] = "button"+i;
            } else{
                buttons[i-1] = intentFromMap.getExtras().getString("button"+i).toString();
            }
        }

        Intent intent=new Intent(this.getIntent());
        mUsername = intent.getStringExtra("username");
        isAdmin = intent.getStringExtra("isAdmin");
        roomName = "null";

        if(isAdmin.equals("false"))
            roomName = intent.getStringExtra("roomName");

        Log.e("intentMsg", isAdmin + " / " + roomName);

        mListView = new MemberListView(getSupportFragmentManager());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
        }

        pager = (ViewPager) findViewById(R.id.viewpager);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), titles));
        pager.setAdapter(mListView);
        alertMessage = (TextView)findViewById(R.id.messageFrame);

        slidingTabLayout.setViewPager(pager);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);

        mDrawerLayout.setDrawerListener(drawerToggle);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, memberArray);
        mDrawerList.setAdapter(adapter);

        CharSequence cs = roomName;
        if(roomName.equals("null"))
            cs = mUsername;

        Log.e("title", cs.toString());

        setTitle(cs);

        /*
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        mDrawerList.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                        toolbar.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                        mDrawerLayout.closeDrawer(Gravity.START);
                        break;
                    case 1:
                        mDrawerList.setBackgroundColor(getResources().getColor(R.color.red));
                        toolbar.setBackgroundColor(getResources().getColor(R.color.red));
                        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.red));
                        mDrawerLayout.closeDrawer(Gravity.START);

                        break;
                    case 2:
                        mDrawerList.setBackgroundColor(getResources().getColor(R.color.blue));
                        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
                        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.blue));
                        mDrawerLayout.closeDrawer(Gravity.START);

                        break;
                    case 3:
                        mDrawerList.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
                        toolbar.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
                        slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
                        mDrawerLayout.closeDrawer(Gravity.START);

                        break;
                }

            }
        });
        */
        socketLogin();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isAdmin.equals("true"))
            mSocket.emit("rmRoom");
        else
            mSocket.emit("exRoom");

        mSocket.disconnect();
        mSocket.off("login", onLogin);
        mSocket.off("mkRoom", makeRoom);
        mSocket.off("enterRoom", enterRoom);
        mSocket.off("user joined", userJoined);
        mSocket.off("exitRoom", userLefted);
        mSocket.off("message", getMessage);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    private void socketLogin(){
        // Socket Server Login
        if(mSocket.connected()) {
            mSocket.emit("add user", mUsername);

            if(isAdmin.equals("true"))
                mSocket.emit("mkRoom");
            else
                mSocket.emit("enRoom", roomName);

        } else {
            Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_LONG).show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("SocketState : ", String.valueOf(mSocket.connected()));
                    if (!mSocket.connected()) {
                        mSocket.connect();
                    } else {
                        mSocket.emit("add user", mUsername);

                        if(isAdmin.equals("true"))
                            mSocket.emit("mkRoom");
                        else
                            mSocket.emit("enRoom", roomName);
                    }
                }
            }, 2000);
        }
    }

    private void settingSocket(){
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("login", onLogin);
        mSocket.on("mkRoom", makeRoom);
        mSocket.on("enterRoom", enterRoom);
        mSocket.on("user joined", userJoined);
        mSocket.on("exitRoom", userLefted);
        mSocket.on("message", getMessage);
        mSocket.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void messageBtnClicked(View v){
        if(!editBtnFlag){
            switch (v.getId()) {
                case R.id.button1:
                    mSocket.emit("sendMessage", "Data1");
                    Toast.makeText(getApplicationContext(), "Button 1", Toast.LENGTH_SHORT).show();
                    //alertMessage.setText("Button1 Clicked!" + memberList);
                    break;
                case R.id.button2:
                    mSocket.emit("sendMessage", "Data2");
                    Toast.makeText(getApplicationContext(), "Button 2", Toast.LENGTH_SHORT).show();
                    //alertMessage.setText("Button2 Clicked!");
                    break;
                case R.id.button3:
                    mSocket.emit("sendMessage", "Data3");
                    Toast.makeText(getApplicationContext(), "Button 3", Toast.LENGTH_SHORT).show();
                    //alertMessage.setText("Button3 Clicked!");
                    break;
                case R.id.button4:
                    mSocket.emit("sendMessage", "Data4");
                    Toast.makeText(getApplicationContext(), "Button 4", Toast.LENGTH_SHORT).show();
                    //alertMessage.setText("Button4 Clicked!");
                    break;
                case R.id.button5:
                    mSocket.emit("sendMessage", "Data5");
                    Toast.makeText(getApplicationContext(), "Button 5", Toast.LENGTH_SHORT).show();
                    //alertMessage.setText("Button5 Clicked!");
                    break;
                case R.id.button6:
                    mSocket.emit("sendMessage", "Data6");
                    Toast.makeText(getApplicationContext(), "Button 6", Toast.LENGTH_SHORT).show();
                    //alertMessage.setText("Button6 Clicked!");
                    break;
                case R.id.fabButton:
    //                CharSequence[] items = {"1번버튼", "2번버튼", "3번버튼", "4번버튼", "5번버튼", "6번버튼" };
    //                AlertDialog.Builder builder = new AlertDialog.Builder(this);
    //                builder.setTitle("바꿀 버튼을 선택하세요")        // 제목 설정
    //                        .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
    //                            // 목록 클릭시 설정
    //                            public void onClick(DialogInterface dialog, int index) {
    //                                Toast.makeText(getApplicationContext(), items[index], Toast.LENGTH_SHORT).show();
    //                                editBtnName(index);
    //                            }
    //                        });
    //
    //                AlertDialog dialog = builder.create();    // 알림창 객체 생성
    //                dialog.setCanceledOnTouchOutside(true);
    //                dialog.show();    // 알림창 띄우기
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("버튼 이름을 수정하시겠습니까?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            editBtnFlag = true;
                            changeBtnColor(true);
                            Toast.makeText(SampleActivity.this, "수정하려는 버튼을 클릭하세요", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editBtnFlag = false;
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setTitle("버튼 이름변경");
    //                alert.setIcon();
                    alert.show();
                    break;
                case R.id.toMap:
                    Intent intent = new Intent(this, NMapViewer.class);
                    Log.e("intetPut", roomName + " / " + isAdmin + " / " + mUsername);
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("username", mUsername);

                    intent.putExtra("buttonFlag", "true");
                    intent.putExtra("button1", buttons[0]);
                    intent.putExtra("button2", buttons[1]);
                    intent.putExtra("button3", buttons[2]);
                    intent.putExtra("button4", buttons[3]);
                    intent.putExtra("button5", buttons[4]);
                    intent.putExtra("button6", buttons[5]);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.refreshButton:
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, memberArray);
                    mDrawerList.setAdapter(adapter);
                    reloadButtonName();
                    break;
                case R.id.messageFrame:
                    AlertDialog.Builder alertMessage = new AlertDialog.Builder(this);

                    alertMessage.setTitle("Send Custom Message");
                    alertMessage.setMessage("전송할 메시지를 입력하시오");

                    final EditText input = new EditText(this);
                    alertMessage.setView(input);

                    alertMessage.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            mSocket.emit("sendMessage", "PM"+value);
                        }
                    });

                    alertMessage.setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });

                    alertMessage.show();
                    break;
            }
        } else{
            switch(v.getId()){
                case R.id.button1:
                    editBtnName(1);
                    break;
                case R.id.button2:
                    editBtnName(2);
                    break;
                case R.id.button3:
                    editBtnName(3);
                    break;
                case R.id.button4:
                    editBtnName(4);
                    break;
                case R.id.button5:
                    editBtnName(5);
                    break;
                case R.id.button6:
                    editBtnName(6);
                    break;
                case R.id.toMap:
                    Intent intent = new Intent(this, NMapViewer.class);
                    Log.e("intetPut", roomName + " / " + isAdmin + " / " + mUsername);
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("isAdmin", isAdmin);
                    intent.putExtra("username", mUsername);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.refreshButton:
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, memberArray);
                    mDrawerList.setAdapter(adapter);
                    reloadButtonName();
                    break;
            }
            editBtnFlag = false;
            changeBtnColor(false);
        }
    }

    private void reloadButtonName(){
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        Button button6 = (Button) findViewById(R.id.button6);

        button1.setText(buttons[0]);
        button2.setText(buttons[1]);
        button3.setText(buttons[2]);
        button4.setText(buttons[3]);
        button5.setText(buttons[4]);
        button6.setText(buttons[5]);
    }

    private void changeBtnColor(boolean flag){
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        Button button6 = (Button) findViewById(R.id.button6);
        if(flag){
            button1.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
            button2.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
            button3.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
            button4.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
            button5.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
            button6.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));
        }else{
            button1.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
            button2.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
            button3.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
            button4.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
            button5.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
            button6.setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
        }
    }

    private void editBtnName(final int buttonNumber){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("메세지 설정");
        alert.setMessage("메세지를 입력하세요.");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                AppCompatButton tempButton1;
                switch (buttonNumber) {
                    case 1:
                        tempButton1 = (AppCompatButton)findViewById(R.id.button1);
                        tempButton1.setText(value);
                        buttons[0] = value;
                        break;
                    case 2:
                        tempButton1 = (AppCompatButton)findViewById(R.id.button2);
                        tempButton1.setText(value);
                        buttons[1] = value;
                        break;
                    case 3:
                        tempButton1 = (AppCompatButton)findViewById(R.id.button3);
                        tempButton1.setText(value);
                        buttons[2] = value;
                        break;
                    case 4:
                        tempButton1 = (AppCompatButton)findViewById(R.id.button4);
                        tempButton1.setText(value);
                        buttons[3] = value;
                        break;
                    case 5:
                        tempButton1 = (AppCompatButton)findViewById(R.id.button5);
                        tempButton1.setText(value);
                        buttons[4] = value;
                        break;
                    case 6:
                        tempButton1 = (AppCompatButton)findViewById(R.id.button6);
                        tempButton1.setText(value);
                        buttons[5] = value;
                        break;
                }
            }
        });


        alert.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();
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
            Log.e("SOCKET", "Join Success : " + mUsername + " / " + numUsers);
            //Toast.makeText(SampleActivity.this, "Join Success : " + mUsername + " / " + numUsers, Toast.LENGTH_LONG).show();
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Socket", "Error : Socket onConnectError");
        }
    };

    private Emitter.Listener getMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            data = (JSONObject) args[0];

            final String msgData;

            try {
                msgData = data.getString("message");
            } catch (JSONException e) {
                return;
            }

            Log.e("SOCKET", "Msg : " + msgData);

            if(!msgData.substring(0,2).equals("PM"))
                btnSetColor(Integer.parseInt(msgData.substring(4)));
            else
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertMessage.setText("Message Arrived : " + msgData.substring(2));
                    }
                });
            //Toast.makeText(getApplicationContext(), "Msg : " + msgData, Toast.LENGTH_LONG).show();
        }
    };

    private Emitter.Listener makeRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            data = (JSONObject) args[0];

            String roomID;

            try {
                roomID = data.getString("roomID");
                memberList = data.getString("member");
            } catch (JSONException e) {
                return;
            }

            Log.e("SOCKET", "mkRoom : " + roomID);
            Log.e("SOCKET", "list : " + memberList);
            updateMemberList(memberList);

            /*insertListView(memberList);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SampleActivity.this.adapter.add("ASB");
                    SampleActivity.this.adapter.notifyDataSetChanged();
                }
            });*/
            //Toast.makeText(getApplicationContext(), "mkRoom : " + roomID, Toast.LENGTH_LONG).show();
        }
    };


    private Emitter.Listener enterRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            data = (JSONObject) args[0];

            String roomID;

            try {
                roomID = data.getString("roomID");
                memberList = data.getString("member");
            } catch (JSONException e) {
                return;
            }

            Log.e("SOCKET", "enterRoom : " + roomID);
            Log.e("SOCKET", "list : " + memberList);
            updateMemberList(memberList);
            //Toast.makeText(getApplicationContext(), "enterRoom : " + roomID, Toast.LENGTH_LONG).show();
        }
    };

    public void btnSetColor(int buttonNumber){
        switch (buttonNumber) {
            case 1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Button btn = (Button) findViewById(R.id.button1);
                                Timer timer = new Timer();

                                btn.setBackgroundColor(Color.RED);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // Your logic here...

                                        // When you need to modify a UI element, do so on the UI thread.
                                        // 'getActivity()' is required as this is being ran from a Fragment.
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                                findViewById(R.id.button1).setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                                                alertMessage.setText("Message Arrived : " + buttons[0]);
                                            }
                                        });
                                    }
                                }, 3000);
                            }
                        });
                    }
                }).start();
                break;
            case 2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Button btn = (Button) findViewById(R.id.button2);
                                Timer timer = new Timer();

                                btn.setBackgroundColor(Color.RED);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // Your logic here...

                                        // When you need to modify a UI element, do so on the UI thread.
                                        // 'getActivity()' is required as this is being ran from a Fragment.
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                                findViewById(R.id.button2).setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                                                alertMessage.setText("Message Arrived : " + buttons[1]);
                                            }
                                        });
                                    }
                                }, 3000);
                            }
                        });
                    }
                }).start();
                break;
            case 3:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Button btn = (Button) findViewById(R.id.button3);
                                Timer timer = new Timer();

                                btn.setBackgroundColor(Color.RED);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // Your logic here...

                                        // When you need to modify a UI element, do so on the UI thread.
                                        // 'getActivity()' is required as this is being ran from a Fragment.
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                                findViewById(R.id.button3).setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                                                alertMessage.setText("Message Arrived : " + buttons[2]);
                                            }
                                        });
                                    }
                                }, 3000);
                            }
                        });
                    }
                }).start();
                break;
            case 4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Button btn = (Button) findViewById(R.id.button4);
                                Timer timer = new Timer();

                                btn.setBackgroundColor(Color.RED);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // Your logic here...

                                        // When you need to modify a UI element, do so on the UI thread.
                                        // 'getActivity()' is required as this is being ran from a Fragment.
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                                findViewById(R.id.button4).setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                                                alertMessage.setText("Message Arrived : " + buttons[3]);
                                            }
                                        });
                                    }
                                }, 3000);
                            }
                        });
                    }
                }).start();
                break;
            case 5:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Button btn = (Button) findViewById(R.id.button5);
                                Timer timer = new Timer();

                                btn.setBackgroundColor(Color.RED);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // Your logic here...

                                        // When you need to modify a UI element, do so on the UI thread.
                                        // 'getActivity()' is required as this is being ran from a Fragment.
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                                findViewById(R.id.button5).setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                                                alertMessage.setText("Message Arrived : " + buttons[4]);
                                            }
                                        });
                                    }
                                }, 3000);
                            }
                        });
                    }
                }).start();
                break;
            case 6:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Button btn = (Button) findViewById(R.id.button6);
                                Timer timer = new Timer();

                                btn.setBackgroundColor(Color.RED);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // Your logic here...

                                        // When you need to modify a UI element, do so on the UI thread.
                                        // 'getActivity()' is required as this is being ran from a Fragment.
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                                                findViewById(R.id.button6).setBackgroundColor(getResources().getColor(R.color.material_deep_teal_500));
                                                alertMessage.setText("Message Arrived : " + buttons[5]);
                                            }
                                        });
                                    }
                                }, 3000);
                            }
                        });
                    }
                }).start();
                break;
        }
    }

    private Emitter.Listener userJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            data = (JSONObject) args[0];

            String joinUser;

            try {
                joinUser = data.getString("username");
                memberList = data.getString("member");
            } catch (JSONException e) {
                return;
            }

            Log.e("SOCKET", "userJoined : " + joinUser);
            Log.e("SOCKET", "list : " + memberList);
            updateMemberList(memberList);
            //Toast.makeText(getApplicationContext(), "userJoined : " + joinUser, Toast.LENGTH_LONG).show();
        }
    };

    private Emitter.Listener userLefted = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            data = (JSONObject) args[0];

            String leftUser;

            try {
                leftUser = data.getString("username");
                memberList = data.getString("member");
            } catch (JSONException e) {
                return;
            }

            Log.e("SOCKET", "userLefted : " + leftUser);
            updateMemberList(memberList);
            //Toast.makeText(getApplicationContext(), "userLefted : " + leftUser, Toast.LENGTH_LONG).show();
        }
    };

    public void updateMemberList(String ML){
        String[] temp;
        String delimiter = "\"";

        temp = ML.split(delimiter);

        String[] resultParse = new String[temp.length/2];
        int count = 0;

        for(int i = 0; i < temp.length ; i++)
            if(i%2 == 1) resultParse[count++] = temp[i];

        memberArray = resultParse;
    }

    private class MemberListView extends FragmentPagerAdapter {

        public MemberListView(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            return SampleFragment.newInstance(index);
        }

        @Override
        public int getCount() { return 3; }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

}
