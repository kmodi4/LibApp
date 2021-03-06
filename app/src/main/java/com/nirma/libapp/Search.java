package com.nirma.libapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Search extends AppCompatActivity {

    WebView browse;
    WebSettings ws;
    private static final String url = "http://librarysearch.nirmauni.ac.in/";
    private ProgressDialog progressDialog=null;
    private boolean isredirected = false;
    private Context context;
    private Handler TimeOutHandler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.searchtoolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        browse = (WebView) findViewById(R.id.searchwebView);
        TimeOutHandler = new Handler();
        browse.setWebViewClient(new MyWebViewClient());
        browse.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if(newProgress>50){                                              //if 50% is loaded then close Progressbar
                    if(progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.cancel();
                        progressDialog.dismiss();
                        progressDialog = null;
                        TimeOutHandler.removeCallbacks(runnable);
                    }
                }
            }
        });
        ws = browse.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= 19) {
            browse.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            browse.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        browse.loadUrl(url);


    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(Uri.parse(url).getHost().equals(url+"/")){
                return false;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            isredirected = false;
        }

        @Override
        public void onLoadResource(WebView view, final String url) {
            super.onLoadResource(view, url);
            if(!isredirected){
                if(progressDialog==null){
                    progressDialog = new ProgressDialog(Search.this){
                        @Override
                        public void onBackPressed() {
                            super.onBackPressed();

                                browse.stopLoading();
                                progressDialog.cancel();
                                progressDialog.dismiss();
                                progressDialog = null;
                                Search.this.finish();

                        }

                    };
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();

                    runnable = new  Runnable() {
                        @Override
                        public void run() {
                            if(progressDialog!=null && progressDialog.isShowing()) {
                                final Intent mainIntent = new Intent(Search.this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                browse.stopLoading();
                                startActivity(mainIntent);
                                Search.this.finish();
                                Toast.makeText(getApplication(),"Slow Internet Connection",Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    TimeOutHandler.postDelayed(runnable, 60000);

                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isredirected=true;

            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
                TimeOutHandler.removeCallbacks(runnable);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if(browse.getUrl().equals(url)){
            this.finish();

        }
        else{
            browse.goBack();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;



        }

        return super.onOptionsItemSelected(item);
    }
}
