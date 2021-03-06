package com.github.GuoFangPeng;


import android.content.Context;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GoogleTranslateUtil {
    private WebView webView;
    private GoogleTranslateCallBack googleTranslateCallBack;
    public static String defaulanguage="zh-CN";

    public GoogleTranslateUtil(Context context, GoogleTranslateCallBack googleTranslateCallBack) {
        webView=new WebView(context);
        this.googleTranslateCallBack=googleTranslateCallBack;
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);  //开启js
        webView.loadUrl("file:///android_asset/index.html");
        webView.setWebViewClient(new WebViewClient());
    }

    static String url = "https://translate.google.cn/translate_a/single";
//    static String  tkk = "434674.96463358"; // 随时都有可能需要更新的TKK值
//暂时不清楚tkk的获得方式  询问原作者中... 先写死在html里了

    /**功能：请求谷歌翻译
     *等待修改想法  剪切法只能获取到一段文字 但不用剪切法，对于一词多义的单字会出现大量结果
     * @param url
     */
    private void sendGet(String url) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                //获取下载的内容输入流
                ResponseBody body = response.body();
                String tem=body.string();
                tem=tem.substring(4,tem.indexOf(",")-1);//简单粗暴地用剪切法
                googleTranslateCallBack.getGoogleTransCallBackResult(0, tem);

            } catch (IOException e) {
                Log.e("tse","=====sendGet======" + e.toString());
                googleTranslateCallBack.getGoogleTransCallBackResult(4,"获取翻译结果失败");
            }

        }).start();

    }

    /**
     * 功能：获取谷歌翻译的url
     * @param tk
     * @param q
     * @param from
     * @param to
     * @return
     */

    private  String construct_url(String tk, String q, String from, String to) {
            String base = url + "?client=webapp&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&otf=2&ssel=0&tsel=0&kc=1&tk=" + tk + "&q=" + q + "&sl=" + from + "&tl=" + to;
            Log.i("tse","=====base======" + base);
            return base;
    }

    /**
     * @param q 待翻译文本
     */
    public void query(String q) {
        try {
            //q = URLEncoder.encode(q);
            //不能encode   会出错
            q=inputPr(q);
            Log.i("tse","=====src======" +q );
            callEvaluateJavascript(q,"auto",defaulanguage);   //获得tk
        } catch (Exception e) {
            googleTranslateCallBack.getGoogleTransCallBackResult(1,"原文encode失败");
            Log.e("tse","=====encode error=====" + e.getMessage());
        }
    }

    /**
     * @param q      待翻译文本
     * @param from   翻译源语言
     */
    public void query(String q,String from) {
        try {
            q=inputPr(q);
            Log.i("tse","=====src======" +q );
            callEvaluateJavascript(q,from,defaulanguage);
        } catch (Exception e) {
            googleTranslateCallBack.getGoogleTransCallBackResult(1,"原文encode失败");
            Log.e("tse","=====encode error=====" + e.getMessage());
        }
    }


    /**
     *
     * @param q      待翻译文本
     * @param from   翻译源语言
     * @param to     目标语言
     */
    public void query(String q,String from,String to) {
        try {
            q=inputPr(q);
            Log.i("tse","=====src======" +q );
            callEvaluateJavascript(q,from,to);
        } catch (Exception e) {
            googleTranslateCallBack.getGoogleTransCallBackResult(1,"原文encode失败");
            Log.e("tse","=====encode error=====" + e.getMessage());
        }
    }

    /**
     *去除特殊字符   未完善
     * 理论上应该要去除全部转义字符 但暂时没想到好方法
     * @return
     */

    private String inputPr(String q)
    {
        String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        String s=q.replaceAll("\r|\n", " ");
        s=s.replaceAll(regEx, "");
        return s;
    }
    /**通过本地js获取tk
     * @param q
     * @param from
     * @param to
     */
   private void callEvaluateJavascript( String q,String from,String to) {
        webView.evaluateJavascript("wo('"+q+"')", value -> {
            Log.i("tse", value);
            if (value.isEmpty()||value.equals("null"))
            {
                googleTranslateCallBack.getGoogleTransCallBackResult(2,"获取tk失败");
            }
            else {
                String tk = value.substring(1, value.length() - 1);
                Log.i("tse","=====tk=====" + tk);
                String baseUrl = construct_url(tk, q, from,to);
                sendGet(baseUrl);
            }
        });
    }

    /**记得一定要post到主进程
     *
     */
    public interface GoogleTranslateCallBack {
        /**
         * 记得一定要post到主进程
         * @param code   错误码,返回0是正确
         * @param response  错误信息/翻译后文本
         */
        void getGoogleTransCallBackResult(int code,String response);
    }

}
