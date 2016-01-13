package com.psgod.ui.view;

import android.content.Context;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.psgod.Utils;

/**
 * Created by Administrator on 2016/1/13 0013.
 */
public class TupppaiWebViewChrome extends WebChromeClient {

    private Context mContext;

    public TupppaiWebViewChrome(Context context){
        super();
        mContext = context;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        String msg = consoleMessage.message();

        if(msg.indexOf("tupppai://") != -1){
            Utils.skipByUrl(mContext,msg,"图派");
            return true;
        }

        return super.onConsoleMessage(consoleMessage);
    }

}
