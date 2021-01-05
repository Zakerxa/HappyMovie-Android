package com.android.happymovies;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.android.happymovies.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {
    private WebView ZinMinHtet;
    private AdView mAdView;
    private InterstitialAd CustomShow;
    private RewardedVideoAd mRewardedVideoAd;
    private Context context;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private WebChromeClient ChromeClient;
    SwipeRefreshLayout swipe;
    private InterstitialAd BackAds;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();

        swipe = findViewById(R.id.swipe);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               ZinMinHtet.clearCache(true);
                loadWebsite();

            }
        });


        loadWebsite();

        ZinMinHtet.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent= new Intent();

                if (url.startsWith("https://m.me")) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }

                if(url.startsWith("tel")){
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            public  void  onPageFinished(WebView view, String url){

                swipe.setRefreshing(false);
                ZinMinHtet.clearCache(true);
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                ZinMinHtet.setVisibility(View.VISIBLE);
            }

//            @Override
//            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//                //Toast.makeText(getApplicationContext(), "No internet connection,Can't connect right now.", Toast.LENGTH_LONG).show();
//            }


        });



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, 1);
                    }
                }
            }
        },10000);
        //Runtime External storage permission for saving download files


///////////////////////////////////////////////////////////////////////////// Google Admod Banner

        MobileAds.initialize(this, "ca-app-pub-6083110028149432~7745210921");//app id

        handler.postDelayed(new Runnable() {
            public void run() {
                CustomShow.loadAd(new AdRequest.Builder().build());
                mAdView = findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();

                mAdView.loadAd(adRequest);
            }
        },5000);



/////////////////////////////////////////////////////////////////// Custom Hide Intertitial Ads///////////////////////////////////////////////

        CustomShow = new InterstitialAd(this);
        CustomShow.setAdUnitId("ca-app-pub-6083110028149432/3371078269");

        handler.postDelayed(new Runnable() {
            public void run() {
                CustomShow.loadAd(new AdRequest.Builder().build());
            }
        },8000);
        CustomShow.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {
                LoadCustomShowAds();
            }

            @Override
            public void onAdLeftApplication() {
                LoadCustomShowAds();
            }

            @Override
            public void onAdClosed() {
               LoadCustomShowAds();
            }
        });

////////////////////////////////////////////////////// Back Ads ////////////////////////////////////////////////////////////////////

        BackAds = new InterstitialAd(this);
        BackAds.setAdUnitId("ca-app-pub-6083110028149432/4300391833");

        handler.postDelayed(new Runnable() {
            public void run() {
                BackAds.loadAd(new AdRequest.Builder().build());
            }
        },6000);
        BackAds.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdLeftApplication() {
                Back(MainActivity.this).show();
            }

            @Override
            public void onAdClosed() {
                Back(MainActivity.this).show();
            }
        });

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.getMediationAdapterClassName();
        mRewardedVideoAd.loadAd("ca-app-pub-6083110028149432/2171700750", new AdRequest.Builder().build());
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                VideoAds();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                VideoAds();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                //Toast.makeText(getApplicationContext(), "!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRewardedVideoCompleted() {
                VideoAds();
            }


        });

        ZinMinHtet.setDownloadListener(new DownloadListener() {


            public void onDownloadStart(String url, String userAgent,String contentDisposition, String mimeType, long contentLength) {

                if(CustomShow.isLoaded()){

                        DownloadManager.Request request = new DownloadManager.Request(
                                Uri.parse(url));
                        request.setMimeType(mimeType);
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading ...");
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                        url, contentDisposition, mimeType));
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(), "Downloading Video...", Toast.LENGTH_LONG).show();

                }else{

                    if(mRewardedVideoAd.isLoaded()){
                        mRewardedVideoAd.show();
                        DownloadManager.Request request = new DownloadManager.Request(
                                Uri.parse(url));
                        request.setMimeType(mimeType);
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading ...");
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                        url, contentDisposition, mimeType));
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(), "Downloading Video...", Toast.LENGTH_LONG).show();
                    }else{
                        DownloadManager.Request request = new DownloadManager.Request(
                                Uri.parse(url));
                        request.setMimeType(mimeType);
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading ...");
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                        url, contentDisposition, mimeType));
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(), "Downloading Video...", Toast.LENGTH_LONG).show();
                    }
                }


            }

        });





        if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {
            loadWebsite();
        }



        String filename = "HappyMovies";
        String fileContents = "hm";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File f = new File(Environment.getDataDirectory(), "subdir");
        f.canRead();
        f.canWrite();
        getFilesDir();
        createDir();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            if (requestCode == FILECHOOSER_RESULTCODE) {

                if (null == this.mUploadMessage) {
                    return;

                }

                Uri result = null;

                try {
                    if (resultCode != RESULT_OK) {

                        result = null;

                    } else {

                        // retrieve from the private variable if the intent is null
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;

            }
        }

        return;
    }

////////////////////////////// Click Count Ads ///////////////////////////////////


    public  File createDir() {
        File DIR=null;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            DIR=new File(android.os.Environment.getExternalStorageDirectory()+"Happy Movies");
        else
            DIR=context.getCacheDir();
        if(!DIR.exists())
            DIR.mkdirs();
        return DIR;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void loadWebsite() {

        ZinMinHtet = (WebView) findViewById(R.id.webView);
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        String unencodedHtml =
                "&lt;html&gt;&lt;body&gt;'%23' is the percent code for ‘#‘ &lt;/body&gt;&lt;/html&gt;";
        String encodedHtml = Base64.encodeToString(unencodedHtml.getBytes(),
                Base64.NO_PADDING);
        ZinMinHtet.loadData(encodedHtml, "text/html", "base64");
        String MyUA = "Mozilla/5.0 (Android,11231tkt) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/81.0.4044.117 mobile Safari/537.36";
        // ZinMinHtet.setWebViewClient(new Browser_home());

        ZinMinHtet = findViewById(R.id.webView); // start web view

        ChromeClient = new WebChromeClient();
        ZinMinHtet.setWebChromeClient(ChromeClient);
        ZinMinHtet.setWebChromeClient(new MyChrome());

        if (Build.VERSION.SDK_INT >= 21) {
            // chromium, enable hardware acceleration
            ZinMinHtet.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            ZinMinHtet.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        ZinMinHtet.getKeepScreenOn();
        ZinMinHtet.getSettings().setAppCacheEnabled(true);
        ZinMinHtet.getSettings().setAllowFileAccess(true);
        ZinMinHtet.getSettings().setDomStorageEnabled(true);
        ZinMinHtet.getSettings().setAppCachePath(getBaseContext().getCacheDir().getPath());
        ZinMinHtet.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );
        ZinMinHtet.getSettings().setAppCacheMaxSize(10 * 1024 * 1024);

        WebSettings webSettings = ZinMinHtet.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);

        webSettings.setAppCachePath(getBaseContext().getCacheDir().getPath());
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setSaveFormData(true);

        webSettings.setCacheMode( WebSettings.LOAD_DEFAULT );
        webSettings.setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString(MyUA);
        webSettings.setSavePassword(true);
        webSettings.setEnableSmoothTransition(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setAppCacheMaxSize(10 * 1024 * 1024);

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            ZinMinHtet.loadUrl("https://legendgaming.zakerxa.com/");
        } else {
            ZinMinHtet.setVisibility(View.GONE);
        }

    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void VideoAds() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mRewardedVideoAd.loadAd("ca-app-pub-6083110028149432/2171700750", new AdRequest.Builder().build());
            }
        },300000);
    }



    public  void LoadCustomShowAds(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                CustomShow.loadAd(new AdRequest.Builder().build());
            }
        },300000);
    }



    public AlertDialog.Builder Back(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("Are you sure to exit");

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });
        return builder;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {
        }

        // For Android 5.0
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {

                }

                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};

            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;

        }

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }

            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            if (CustomShow.isLoaded()) {
                CustomShow.show();
            }
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;

        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }

            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846);
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to open Internet or Wifi to watch HappyMovies.");

        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });
        return builder;
    }



    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        } else
            return false;
    }


    @Override
    public void onBackPressed() {

        if(ZinMinHtet.canGoBack()) {
            ZinMinHtet.goBack();
        } else {
            if(BackAds.isLoaded()){
                BackAds.show();
            }else{
                Back(MainActivity.this).show();
            }

        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }



}
