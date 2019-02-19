package com.sharprogramming.xkcd;

import android.telephony.gsm.GsmCellLocation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

public class XKCDcomic {
    private int month, num, year, day;
    private String link, news, safe_title, transcript, alt, img, title, imgRetina;
    // Adding a filepath for favorites as they will be persisted
    private String filepath;

    public final static int MOST_RECENT_COMIC = 0;
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }

    public String getSafe_title() {
        return safe_title;
    }

    public void setSafe_title(String safe_title) {
        this.safe_title = safe_title;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgRetina() {
        return imgRetina;
    }

    public void setImgRetina(String imgRetina) {
        this.imgRetina = imgRetina;
    }

    public void setFilepath(String filepath){
        this.filepath = filepath;
    }

    public String getFilepath(){
        return filepath;
    }

    public static void getComic(int number, Response.Listener<XKCDcomic> responseListener, Response.ErrorListener errorListener){
        RequestQueue queue = MainActivity.getRequestQueue();
        if (queue == null){
            android.util.Log.e("VolleyError", "Volley request queue not yet ready");
            return;
        }
        String url = "https://xkcd.now.sh";

        // if the number is negative it is an invalid comic
        if (number < 0){
            android.util.Log.e("XKCDError", "No negative comics");
            return;
        }else if (number != MOST_RECENT_COMIC){
            // If we don't want the most recent comic, we have to add on the number
            url = url.concat("/" + number);
        }

        Response.Listener<String> stringResponseListener = stringResponse -> {
            XKCDcomic comicResponse = new Gson().fromJson(stringResponse, XKCDcomic.class);
            responseListener.onResponse(comicResponse);
        };

        StringRequest request = new StringRequest(Request.Method.GET, url, stringResponseListener, errorListener);

        queue.add(request);
    }

    @Override
    public String toString() {
        return "XKCDcomic{" +
                "month=" + month +
                ", num=" + num +
                ", year=" + year +
                ", day=" + day +
                ", link='" + link + '\'' +
                ", news='" + news + '\'' +
                ", safe_title='" + safe_title + '\'' +
                ", transcript='" + transcript + '\'' +
                ", alt='" + alt + '\'' +
                ", img='" + img + '\'' +
                ", title='" + title + '\'' +
                ", imgRetina='" + imgRetina + '\'' +
                '}';
    }
}
