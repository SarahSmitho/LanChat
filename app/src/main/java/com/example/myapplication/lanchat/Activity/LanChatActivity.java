package com.example.myapplication.lanchat.Activity;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.lanchat.Adapter.MsgAdapter;
import com.example.myapplication.lanchat.Bean.Msg;
import com.example.myapplication.lanchat.Event.MessageEvent;
import com.example.myapplication.lanchat.Util.App;
import com.example.myapplication.lanchat.Util.SDUtil;
import com.example.myapplication.lanchat.socket.TCP;
import com.example.myapplication.lanchat.socket.UDP;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LanChatActivity extends AppCompatActivity {

    private static final String TAG = "LanChatActivity";

    private static final int WRITE_EXTERNAL_STORAGE = 101;
    private static final int CHOOSE_PHOTO = 1;

    private static final String IPADDR="192.168.48.66";

    //消息输入框
    private EditText editText;
    //输入框监听
    private TextWatcher textWatcher;
    //发送按钮
    private Button sendBt;
    //发送图片
    private Button sendImageBt;


    private ListView msgListView;
    private List<Msg> msgList = new ArrayList<Msg>();
    private MsgAdapter msgAdapter;

    private Handler handler;


    private UDP udp;
    private TCP tcp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_chat);

        try {
            udp = new UDP(IPADDR);
            //System.err:     at com.example.myapplication.lanchat.Activity.LanChatActivity$2.onClick
            // (LanChatActivity.java:125)
            Log.d(TAG, "onClick: 生成udp对象");
        } catch (SocketException e) {
            e.printStackTrace();
        }

        //监听消息输入框
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    sendBt.setClickable(true);
                } else {
                    sendBt.setClickable(false);
                }
            }
        };

        initView();

        handler = new Handler();
        //发送文字消息按钮的点击事件
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sendContent = editText.getText().toString();
                if (!TextUtils.isEmpty(sendContent)) {
                    Msg msg = new Msg(Msg.SENT, Msg.TEXT, sendContent);
                    //把发送的消息放到消息List里
                    msgList.add(msg);
                    //有新消息时，刷新ListView中的显示
                    msgAdapter.notifyDataSetInvalidated();
                    //将ListView定位到最后一行
                    msgListView.setSelection(msgList.size());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //清空输入框的内容
                            editText.setText("");
                        }
                    });



                    //开启一个线程来发送消息
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: 开始发消息");
                            //通过UDPSocket发送消息
                            udp.sendDataWithUDPSocket(sendContent);
                        }
                    }).start();

                }
                editText.setText("");
            }
        });

// 点击发送图片按钮，选择图片
        sendImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                    //动态申请获取访问 读写磁盘的权限,也可以在注册文件上直接注册
                    if (ContextCompat.checkSelfPermission(LanChatActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LanChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);

                    } else {
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_PHOTO);
                    }
                }else{
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_PHOTO);
                }
                Log.d(TAG, "onClick: 选择图片成功");

            }


        });

        //EventBus注册,subscriber，订阅者为该Activity
        EventBus.getDefault().register(this);
    }


    private void SocketServerThread(){
        //开启一个线程来接收图片
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 通过UDPSocket发送图片
                try {
                    tcp.receiveImage();
                    Log.d("服务端接收图片过程", "run: 接收图片");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 初始化视图界面
     */
    private void initView() {
        editText = findViewById(R.id.et_send_content);
        sendBt = findViewById(R.id.btn_send);
        sendImageBt = findViewById(R.id.btn_image);
        editText.addTextChangedListener(textWatcher);
        // 初始化消息数据
        //OtherUtil.initMsg(msgList);
        msgAdapter = new MsgAdapter(LanChatActivity.this, R.layout.msg_item, msgList);
        msgListView = findViewById(R.id.msg_list_view);
        msgListView.setAdapter(msgAdapter);
        // 一开始消息输入框内容为空，设置为不可点击
        sendBt.setClickable(false);

        try {
            Log.d(TAG, "onActivityResult: 生成tcp对象");
            tcp=new TCP(IPADDR);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        SocketServerThread();



        //开启一个线程来接收消息
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: 开始收消息");
                udp.ServerReceviedByUdp();
            }
        }).start();


    }

    /**
     * 事件处理（真正的处理逻辑），收到消息（事件）后做处理
     * MAIN：表示事件处理函数的线程在主线程（UI）线程，因此在这里不能进行耗时操作
     * 这里主要做一些UI的更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showWords(MessageEvent messageEvent) {
        Log.d(TAG, "showWords: 是否金放过发,"+messageEvent.getType());
        if(!TextUtils.isEmpty(messageEvent.getWords()) || messageEvent.getImage() != null){
            // 判断接收的消息类型,魔法值，0为文字，1为图片
            if(messageEvent.getType() == 0){
                Msg msg = new Msg(Msg.RECEIVED, Msg.TEXT,  messageEvent.getWords());
                msgList.add(msg);
                msgAdapter.notifyDataSetChanged();//有新消息时，刷新ListView中的显示
                msgListView.setSelection(msgList.size());//将ListView定位到最后一行
            }else if(messageEvent.getType() == 1){
                Log.d(TAG, "showWords: image size="+(messageEvent.getImage().length));
                Bitmap bitmap = BitmapFactory.decodeByteArray(messageEvent.getImage(), 0, messageEvent.getImage().length);

                Log.d("服务端接收图片过程", "showWords: bitmap="+bitmap);

                Msg msg = new Msg(Msg.RECEIVED,Msg.IMAGE,bitmap);
                Log.d(TAG, "showWords: 显示图片,bitmap="+(bitmap!=null));
                msgList.add(msg);
                msgAdapter.notifyDataSetChanged();//有新消息时，刷新ListView中的显示
                msgListView.setSelection(msgList.size());//将ListView定位到最后一行
            }

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    final String path = SDUtil.getFilePathByUri(LanChatActivity.this,data.getData());

                    if(!TextUtils.isEmpty(imageUri.toString())){
                        Log.d(TAG, "onActivityResult: ");
                        Msg msg = new Msg(Msg.SENT,Msg.IMAGE,imageUri);
                        msgList.add(msg);
                        Log.d("客户端发送图片过程", "onActivityResult: URI="+imageUri+" "+path);
                        //有新消息时，刷新ListView中的显示
                        msgAdapter.notifyDataSetChanged();
                        Log.d("客户端发送图片过程", "onActivityResult: Msg="+msg);
                        //将ListView定位到最后一行
                        msgListView.setSelection(msgList.size());



                        // 开启一个线程来发送图片
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 通过TCPSocket发送图片
                                try {
                                    
                                    tcp.sendImage(path);
                                    Log.d("客户端发送图片过程", "run: 开始给服务器端发消息 path="+path);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭socket连接，避免端口被持续占用
        udp.disconnect();
        //EventBus解注册，取消订阅
        EventBus.getDefault().unregister(this);
    }
}