package com.example.myapplication.lanchat.Event;

import android.graphics.Bitmap;

/**
 * eventbus 事件（作为事件的载体）
 */
public class MessageEvent {
    private int type;
    private String words;//文字消息
    private byte[] image;//图片字节流
    private int imageId;//头像ID
    private Bitmap bitmap;

    /**
     * 构造函数（文字消息事件）
     * @param type
     * @param words
     */
    public MessageEvent(int type, String words){
        this.type=type;
        this.words=words;
    }

    /**
     * 构造函数（图片消息事件）
     * @param type
     * @param image
     */
    public MessageEvent(int type, byte[] image) {
        this.type = type;
        this.image = image;
    }

    public MessageEvent(int type, Bitmap image) {
        this.type = type;
        this.bitmap = image;
    }



    /**
     * getType
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * getWords
     * @return
     */
    public String getWords() {
        return words;
    }

    /**
     * getImage
     * @return
     */
    public byte[] getImage() {
        return image;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
