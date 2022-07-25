package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.myapplication.lanchat.Event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPFail {
    private static final String TAG = "TCP";
    // 消息类型为图片
    public static final int IMAGE = 1;

    //IP地址
    private String ipAddr;

    //客户端socket（发送）
    private Socket mSocket;

    //服务端socket（接收）
    private ServerSocket mServerSocket;

    byte data1[] = new byte[40 * 1024];

    /**
     * 对方的ip地址在构造方法中传入
     * @param ipAddr
     * @throws SocketException
     */
    public TCPFail(String ipAddr) throws SocketException {
        this.ipAddr = ipAddr;
    }

    /**
     * 图片转换为文件流并发送，客户端通过Socket发送
     *
     * @param path
     * @throws FileNotFoundException
     */
    public void sendImage(String path) throws FileNotFoundException {
        System.out.println("==================");
        //1、创建一个Socket，连接到服务器端、指定端口号。放在子线程中运行，否则会有问题。
        try {//指定ip地址和端口号
            Log.d(TAG, "sendImage:生成TCP客户端Socket对象 ipAddr="+ipAddr);
            mSocket = new Socket(ipAddr, 6000);

//            String targetPath = Environment.getExternalStorageDirectory().toString()+"/compressPic.jpg";
//            final String compressImage = PictureUtil.compressImage(path,targetPath,10);

            File file = new File(path);
            Log.d(TAG, "sendImage: 图片路径 path="+path);
            FileInputStream in = new FileInputStream(file);
            //把输入流里的数据读进data1
            in.read(data1);
            Bitmap bitmap=BitmapFactory.decodeFile(path);
            if (mSocket != null) {//获取输出流
                //2、调用Socket类的getOutputStream()获取输入输出流。
                OutputStream mOutStream = mSocket.getOutputStream();
                //3、发送
                //把data1数据写入到OutputStream中，然后通过Socket发送
                mOutStream.write(data1);
//                mOutStream.write(Base64Util.bitmapToBase64(bitmap).getBytes());
                mOutStream.flush();
                System.out.println("连接服务端成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接服务端失败！");
        }

    }

    /**
     * 接收图片，服务端通过ServerSocket接收
     */
    public void receiveImage() throws SocketException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 1、开启服务
        try {    //开启服务、指定端口号
            mServerSocket = new ServerSocket(6000);
            Log.d(TAG, "receiveImage: 开启服务端成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "receiveImage: 开启服务端失败");
        }

        while (true) {
            //2、调用ServerSocket的accept(),监听连接请求，如果客户端请求连接，则接收连接，返回Scoekt对象。
                 //通过ServerSocket的accept()接收客户端的Socket，然后获取客户端Socket中的输入流
            try {
                mSocket = mServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //3、调用Socket类的getInputStream()和getOutputStream()获取输入输出流。
            //获取输入流
            try {
                Log.d(TAG, "receiveImage: TCP====================");
                // mSocket.getInputStream()获取客户端Socket中的InputStream
                InputStream mInStream = mSocket.getInputStream();
                Log.d("服务端接收图片过程", "receiveImage: mInStream="+mInStream);
                Log.d(TAG, "receiveImage: =====2222");
                //4、接收数据
                //循环执行read，用来接收数据。
                //数据存在buffer中，count为读取到的数据长度。
                byte[] buffer = new byte[40 * 1024];
                //int count = mInStream.read(buffer);
                //把输入流数据读到Buffer里

                int readLength=mInStream.read(buffer);
                //他们改改就把这里改错了


                //再把buffer里数据写到baos对象里
                Log.d("服务端接收图片过程", "receiveImage: buffer长度="+buffer.length);
                baos.write(buffer, 0, readLength);
                //这个地方，从0开始写一直写的是从输入流读出来的长度，因为这个buffer一直存在，一直缓存之前的数据
                //所以baos的长度会将之前所有从输出流读出的长度加起来，


                /*2022-07-23 10:46:24.414 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: mInStream=java.net.SocketInputStream@66f2e9c
                2022-07-23 10:46:24.509 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: buffer长度=40960
                2022-07-23 10:46:24.510 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: readLength长度14480
                2022-07-23 10:46:24.510 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: baos长度=14480
                2022-07-23 10:46:24.520 4155-4155/com.example.myapplication D/服务端接收图片过程: showWords: bitmap=android.graphics.Bitmap@c06ac2b
                2022-07-23 10:46:35.364 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: mInStream=java.net.SocketInputStream@1275a3
                2022-07-23 10:46:35.439 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: buffer长度=40960
                2022-07-23 10:46:35.440 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: readLength长度8688
                2022-07-23 10:46:35.440 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: baos长度=23168
                2022-07-23 10:46:35.491 4155-4155/com.example.myapplication D/服务端接收图片过程: showWords: bitmap=android.graphics.Bitmap@6c5f0a0
                2022-07-23 10:46:39.412 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: mInStream=java.net.SocketInputStream@bccc8ff
                2022-07-23 10:46:39.533 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: buffer长度=40960
                2022-07-23 10:46:39.534 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: readLength长度14480
                2022-07-23 10:46:39.534 4155-4228/com.example.myapplication D/服务端接收图片过程: receiveImage: baos长度=37648
                2022-07-23 10:46:39.590 4155-4155/com.example.myapplication D/服务端接收图片过程: showWords: bitmap=android.graphics.Bitmap@fa976cc
                */


                Log.d("服务端接收图片过程", "receiveImage: readLength长度"+readLength);
                if (baos.toByteArray() != null) {
                    Log.d("服务端接收图片过程", "receiveImage: baos长度="+baos.toByteArray().length);
//                    String bitmapString=new String(baos.toByteArray());
//                    Bitmap bitmap=Base64Util.base64ToBitmap(bitmapString);
                    // 自定义事件（作为通信载体，可以发送数据）
                    MessageEvent messageEvent = new MessageEvent(IMAGE, baos.toByteArray());
//                    // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                    EventBus.getDefault().post(messageEvent);
//                    MessageEvent messageEvent = new MessageEvent(IMAGE,bitmap);
//                    EventBus.getDefault().post(messageEvent);
                }
//                Log.d(TAG, "receiveImage: ===="+mInStream);
//                Bitmap bitmap= BitmapFactory.decodeStream(mInStream);
//                Log.d(TAG, "receiveImage: tcp bitmap="+(bitmap!=null));
//                if (bitmap!=null){
//                    MessageEvent messageEvent = new MessageEvent(IMAGE,bitmap);
//                    EventBus.getDefault().post(messageEvent);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //5、服务不再需要，则关闭服务
//        if(mServerSocket != null){
//            try {
//                mServerSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
