package com.example.myapplication.lanchat.socket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.myapplication.lanchat.Event.MessageEvent;
import com.example.myapplication.lanchat.Util.Base64Util;
import com.example.myapplication.lanchat.Util.PictureUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCP {
    private static final String TAG = "TCP";
    // 消息类型为图片
    public static final int IMAGE = 1;

    //IP地址
    private String ipAddr;

    //客户端socket（发送）
    private Socket mSocket;

    //服务端socket（接收）
    private ServerSocket mServerSocket;

    byte data1[] = new byte[10 * 1024 * 1024];

    /**
     * 对方的ip地址在构造方法中传入
     *
     * @param ipAddr
     * @throws SocketException
     */
    public TCP(String ipAddr) throws SocketException {
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
            Log.d(TAG, "sendImage:生成TCP客户端Socket对象 ipAddr=" + ipAddr);
            mSocket = new Socket(ipAddr, 6000);

//            String targetPath = Environment.getExternalStorageDirectory().toString()+"/compressPic.jpg";
//            final String compressImage = PictureUtil.compressImage(path,targetPath,10);

            File file = new File(path);
            Log.d(TAG, "sendImage: 图片路径 path=" + path);
            FileInputStream in = new FileInputStream(file);

            //把输入流里的数据读进data1
//            in.read(data1);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] da=new byte[1024];
            int read=-1;
            int totalLength=0;
            while ((read=in.read(da))>0){
                //每读一段就会把读的那一段去掉，然后重新开始读，直至读完
                bout.write(da,0,read);
                totalLength+=read;
            }
            Log.d(TAG, "sendImage: ");
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (mSocket != null) {//获取输出流

                //2、调用Socket类的getOutputStream()获取输入输出流。
                OutputStream mOutStream = mSocket.getOutputStream();
//                byte[] da=new byte[1024];
//                int read=-1;
//                int totalLength=0;
//                while ((read=in.read(da))>0){
//                    mOutStream.write(da,0,read);
//                    totalLength+=read;
//                }
                DataOutputStream dataOutputStream=new DataOutputStream(mOutStream);
                long len = bout.size();

                mOutStream.write(bout.toByteArray());
                Log.d(TAG, "sendImage: send totalLength="+totalLength+" boutLength="+bout.toByteArray().length);
                //3、发送
                //把data1数据写入到OutputStream中，然后通过Socket发送
//                mOutStream.write(data1);
//                mOutStream.write(Base64Util.bitmapToBase64(bitmap).getBytes());
                mOutStream.flush();




                mSocket.shutdownOutput();





                Log.d(TAG, "sendImage: 图片发送完了！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "sendImage: 发送socket异常");
        }

    }

    /**
     * 接收图片，服务端通过ServerSocket接收
     */
    public void receiveImage() throws SocketException {

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
                Log.d("服务端接收图片过程", "receiveImage: mInStream=" + mInStream);
                Log.d(TAG, "receiveImage: =====2222");
                //4、接收数据
                //循环执行read，用来接收数据。
                //数据存在buffer中，count为读取到的数据长度。
                byte[] buffer = new byte[1024];
                //int count = mInStream.read(buffer);
                //把输入流数据读到Buffer里
                int readLength=-1;
                int totalLength = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((readLength = mInStream.read(buffer)) > 0) {
                    Log.d("服务端接收图片过程", "receiveImage: readLength="+readLength);
                    Log.d(TAG, "receiveImage: before write");
                    baos.write(buffer, 0, readLength);
                    Log.d(TAG, "receiveImage: after write");
                    totalLength += readLength;
                    Log.d("服务端接收图片过程", "receiveImage: totalLength="+totalLength);
//                    if (totalLength==859117){
//                        Log.d(TAG, "receiveImage: before end read length");
////                        readLength = mInStream.read(buffer);
//                        Log.d(TAG, "receiveImage: after end read length="+mInStream.read(buffer));
//                        break;
//                    }
                }
                Log.d(TAG, "receiveImage: 读完数据了");
                baos.flush();
                byte[] receiveData=baos.toByteArray();
                baos.close();
//                int readLength = mInStream.read(buffer);
                //再把buffer里数据写到baos对象里


                Log.d("服务端接收图片过程", "receiveImage: buffer长度=" + buffer.length);
//                baos.write(buffer, 0, readLength);
                Log.d("服务端接收图片过程", "receiveImage: readLength长度=" + readLength + "totalLength=" + totalLength);
                if (receiveData != null) {
                    Log.d("服务端接收图片过程", "receiveImage: baos长度=" + receiveData.length);

                    // 自定义事件（作为通信载体，可以发送数据）
                    MessageEvent messageEvent = new MessageEvent(IMAGE, baos.toByteArray());
                    // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                    EventBus.getDefault().post(messageEvent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
