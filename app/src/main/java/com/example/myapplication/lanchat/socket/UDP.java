package com.example.myapplication.lanchat.socket;

import android.text.TextUtils;
import android.util.Log;

import com.example.myapplication.lanchat.Event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 这是核心的通信类（文字）
 */
public class UDP {
    private static final String TAG = "UDP";
    // 消息类型为文字
    public static final int TEXT = 0;

    //使用DatagramSocket进行基于UDP的Socket通信
    private DatagramSocket socket = new DatagramSocket(1985);

    //ip地址
    private String ipAddr;

    /**
     * 对方的ip地址在构造方法中传入
     *
     * @param ipAddr
     * @throws SocketException
     */
    public UDP(String ipAddr) throws SocketException {
        this.ipAddr = ipAddr;
    }

    /**
     * 客户端发送、收到消息的方法
     *
     * @param str
     */
    public void sendDataWithUDPSocket(String str) {
        try {
            Log.d(TAG, "sendDataWithUDPSocket: 开始发消息");
            // 获得IP地址，创建服务地址对象
            InetAddress serverAddress = InetAddress.getByName(ipAddr);
            // 将文字信息转化成字节流数据
            Log.d(TAG, "sendDataWithUDPSocket: ipAddr="+ipAddr);
            byte data[] = str.getBytes();
            // 将字节流数据打包成一个数据包（数据，数据长度，IP地址，端口号）
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 10025);
            // 通过socket发送该数据包
            socket.send(packet);

//            //获取响应
//            packet=new DatagramPacket(data,data.length);
//            //接受数据报
//            socket.receive(packet);
//            //
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    *
     */

    /**
     * 接收消息的方法
     */
    public void ServerReceviedByUdp() {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(10025);
            while (true) {
                // 先定义内存空间，创建一个空的数据包
                byte data[] = new byte[4 * 1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                // 将接受的数据存入数据包
                socket.receive(packet);
                // 将数据包转成String类型
                String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
                // 通过EventBus将数据传给wordsEvent bean对象，在Maintivity中通过wordsEvent获取数据并加以处理（实现对应的UI更新）
                if (!TextUtils.isEmpty(result)) {
                    MessageEvent messageEvent = new MessageEvent(TEXT, result);
                    EventBus.getDefault().post(messageEvent);
                }
                Log.d(TAG, "ServerReceviedByUdp: 文字收到信息为："+ result);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭断开socket
     */
    public void disconnect() {
        socket.close();
        socket.disconnect();
    }

}

