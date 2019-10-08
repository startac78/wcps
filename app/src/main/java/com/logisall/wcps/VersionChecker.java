package com.logisall.wcps;

import android.os.AsyncTask;

import org.jsoup.Jsoup;

import java.io.IOException;

/* 안드로이드 그레들(app) dependencies에 implementation 'org.jsoup:jsoup:1.7.3' 추가 후 진행 (Jsoup)*/
public class VersionChecker extends AsyncTask<String, String, String> {
    String newVersion;

    @Override
    protected String doInBackground(String... params) {
        try {
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=com.logisall.wcps")
                    .timeout(30000)
                    .get()
                    .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                    .first()
                    .ownText();
        } catch (IOException e){
            e.printStackTrace();
        }
        return newVersion;
    }

}
