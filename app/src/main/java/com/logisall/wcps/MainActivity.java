package com.logisall.wcps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.woosim.printer.WoosimService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // 멀티 퍼미션 지정
    private String[] permissions = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 기기, 사진, 미디어, 파일 엑세스 권한
            Manifest.permission.ACCESS_FINE_LOCATION,   // 블루투스 허용
            Manifest.permission.CAMERA
    };
    private static final int MULTIPLE_PERMISSIONS = 101;

    public static String mMAC_ADDRESS;
    public static String mPHONE_NUM = "";
    //private String mMAC_ADDRESS = "";
    private WebView mWebView;
    private final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private ProgressDialog mDialog = null;
    private BroadcastReceiver receiver;
    SQLiteOpenHelper dbHelper;
    SQLiteDatabase db;

    public static String CAMERA_name = "NONAME";
    public static String SDRoot = "";
    public static final boolean SUPPORT_STRICT_MODE = Build.VERSION_CODES.FROYO < Build.VERSION.SDK_INT;

    //private int mPrevPosition;		//이전에 선택되었던 포지션 값
    //private ViewPager mPager;			//뷰 페이저
    //private LinearLayout mPageMark;	//현재 몇 페이지 인지 나타내는 뷰

    private static final int MSG_TIMER_EXPIRED = 1;
    private static final int BACKEY_TIMEOUT = 500;        //0.5초
    private boolean mIsBackKeyPressed = false;
    private long mCurrentTimeInMillis = 0;

    //블루투스 프린터
    // Debugging
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    // Message types sent from the BluetoothPrintService Handler
    public static final int MESSAGE_DEVICE_NAME = 1;
    public static final int MESSAGE_TOAST = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MESSAGE_DISCONNECT = 4;

    // Key names received from the BluetoothPrintService Handler
    public static final String TOAST = "toast";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_MAC = "decvice_mac";            // 블루투스 맥주소
    public static final String BT_CALLBACK = "";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 101;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 102;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int PERMISSION_DEVICE_SCAN_SECURE = 11;
    private static final int PERMISSION_DEVICE_SCAN_INSECURE = 12;

    private static final byte EOT = 0x04;
    private static final byte LF = 0x0a;
    private static final byte ESC = 0x1b;
    private static final byte GS = 0x1d;
    private static final byte[] CMD_INIT_PRT = {ESC, 0x40};        // Initialize printer (ESC @)

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private static BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the print services
    public static BluetoothPrintService mPrintService = null;
    private WoosimService mWoosim = null;

    private Menu mMenu = null;

    static Context mContext;

    private String PhoneNumber = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Context
        mContext = getApplicationContext();
        // CheckSdcard - 저장공간 확인
        CheckSdcard();

        // 이상 쓰레드 감지모드
        if (SUPPORT_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectNetwork().build());
        }

/*        // 프린터 블루투스 어댑터
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //Toast.makeText(getApplicationContext(), "어댑터 사용가능", Toast.LENGTH_LONG).show();
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            setupPrint();
*//*            if (mPrintService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                // then start the bluetooth print services
                if (mPrintService.getState() == BluetoothPrintService.STATE_NONE) {
                    Toast.makeText(getApplicationContext(), "어댑터 리스터 동작", Toast.LENGTH_LONG).show();
                    mPrintService.start();
                }
            } else setupPrint();*//*
        }*/

        mWebView = findViewById(R.id.web_view);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);

        final UDFInterface _JavaUDF = new UDFInterface(mWebView, this);
        mWebView.addJavascriptInterface(_JavaUDF, "udf");

        mWebView.getSettings().setDomStorageEnabled(true);  // 로컬 스토리지 API
        mWebView.getSettings().setDatabaseEnabled(true);  // 로컬 스토리지 API
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setWebChromeClient(new WebChromeClient(){
            //웹뷰에 alert창에 url을 제거한다.
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result)
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }
            //웹뷰에 Confirm창에 url을 제거한다.
            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final JsResult result) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file");
                    String fileName = contentDisposition.replace("attachment; filename=", "");
                    fileName = fileName.replaceAll("\"", "");
                    fileName = java.net.URLEncoder.encode(fileName,"UTF-8");
                    request.setTitle(fileName);
                    //Log.d("download",fileName);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        } else {
                            Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        }
                    }
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        }

        //웹뷰에 크롬 사용 허용//이 부분이 없으면 크롬에서 alert가 뜨지 않음
        mWebView.setWebViewClient(new WebViewClientClass());//새창열기 없이 웹뷰 내에서 다시 열기//페이지 이동 원활히 하기위해 사용

        //hkmin : 캐시삭제
        mWebView.clearCache(true);
        mWebView.clearHistory();

        // 사용권한 체크 모듈
        mWebView.loadUrl("https://wcps.logisall.com/_APP/webview/index.html");

        if (D) Log.i(TAG, "+++ ON CREATE +++");


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //****************************** 블루투스 모듈 사용 안하는 경우 주석처리
        // If the adapter is null, then Bluetooth is not supported
/*        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mPrintService == null) setupPrint();
        }*/
        // 종료
        // Make a list for com.woosim.com.woosim.webview activities
        final ListView simpleListView = (ListView) findViewById(R.id.simpleListView);

        SimpleAdapter adapter = new SimpleAdapter(this,
                getData(),
                android.R.layout.simple_list_item_1,
                new String[]{"title"},
                new int[]{android.R.id.text1});

        if (simpleListView != null) {
            simpleListView.setAdapter(adapter);
            //perform listView item click event
            simpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Map map = (Map) simpleListView.getItemAtPosition(position);
                    Intent intent = (Intent) map.get("intent");
                    startActivity(intent);
                }
            });
        }
        //****************************** 블루투스 모듈 사용 안하는 경우 주석처리 종료
    }

    private ArrayList<HashMap<String, Object>> getData() {
        ArrayList<HashMap<String, Object>> appList = new ArrayList<>();

        addItem(appList, getString(R.string.title_sign_pad), new Intent(this, SignPad.class));
        addItem(appList, getString(R.string.title_multi_language), new Intent(this, MultiLanguage.class));
        addItem(appList, getString(R.string.title_pagemode), new Intent(this, PageMode.class));

        return appList;
    }

    private void addItem(ArrayList<HashMap<String, Object>> list, String name, Intent intent) {
        HashMap<String, Object> temp = new HashMap<>();

        temp.put("title", name);
        temp.put("intent", intent);
        list.add(temp);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.i(TAG, "++ ON START ++");

        //****************************** 블루투스 모듈 사용 안하는 경우 주석처리
/*        if (!mBluetoothAdapter.isEnabled()) {
            // Request to enable bluetooth.
            // Bluetooth session will then be setup during onActivityResult
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            if (mPrintService == null) {
                // Initialize the BluetoothPrintService to perform bluetooth connections
                mPrintService = new BluetoothPrintService(this, mHandler);
            }
        }*/
    }

/*    // The handler that gets information back from the BluetoothPrintService
    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }*/

/*    private void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                String deviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "연결 성공 (" + deviceName + ")", Toast.LENGTH_SHORT).show();
                redrawMenu();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
                break;
        }
    }*/

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.i(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was not enabled during onStart(),
        // so we were paused to enable it.
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.

        if (mPrintService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            // then start the bluetooth print services
            if (mPrintService.getState() == BluetoothPrintService.STATE_NONE) {
                mPrintService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (D) Log.i(TAG, "--- ON DESTROY ---");
        // Stop the Bluetooth print services
        if (mPrintService != null) mPrintService.stop();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                } else {
                    showToast_PermissionDeny();
                }
                return;
            }
        }

    }

    /////
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (D) Log.d(TAG, "onActivityResult: " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = intent.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    String deviceName = intent.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    //Toast.makeText(getApplicationContext(), "연결 성공 SECURE(" + deviceName + ")", Toast.LENGTH_SHORT).show();
                    connectDevice(address, true, false);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = intent.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    String deviceName = intent.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    //Toast.makeText(getApplicationContext(), "연결 성공 INSECURE(" + deviceName + ")", Toast.LENGTH_SHORT).show();
                    connectDevice(address, true, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up bluetooth connections
                    mPrintService = new BluetoothPrintService(this, mHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    if (D) Log.e(TAG, "BT is not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            //하단 메뉴버튼을 통해 연결하는 경우
            case 110:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    //connectDevice(intent, true);
                    Toast.makeText(getApplicationContext(), "프린트 연결중", Toast.LENGTH_SHORT).show();


                    String address = intent.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);

                    boolean secure = true;
                    StringBuffer buf = new StringBuffer();
                    buf.append(Build.VERSION.RELEASE);
                    String version = buf.toString();
                    String target = "2.3.3";
                    if ( version.compareTo(target) > 0 ) {
                        secure = false;
                    }

                    //connectDevice(String address, boolean secure, boolean callback)
                    connectDevice(address, secure, false);
                }
                break;

            //서명
            case 18:
                if (resultCode == RESULT_OK) {
                    String img64Str2 = "";
                    String ssipath = intent.getExtras().getString("savepath");
/*                    try {
                        img64Str2 = Base64.(ssipath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        img64Str2 = "";
                    }*/
                    String filename = intent.getExtras().getString("filename");
                    String SignEventXY = intent.getExtras().getString("SignEventXY");
                    mWebView.loadUrl("javascript:SaveReg('" + filename + "');");

                    Toast.makeText(this, "사인 이미지 저장이 완료 되었습니다."   , Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    //mWebView.loadUrl("javascript:window.udf.");
                    Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                }

                break;
            //서명
            case 19:
                if (resultCode == RESULT_OK) {
                    String img64Str2 = "";
                    String ssipath = intent.getExtras().getString("savepath");
/*                    try {
                        img64Str2 = Base64.encodeFromFile(ssipath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        img64Str2 = "";
                    }*/
                    String filename = intent.getExtras().getString("filename");
                    String SignEventXY = intent.getExtras().getString("SignEventXY");
                    mWebView.loadUrl("javascript:SaveReg('" + filename + "');");

                    Toast.makeText(this, "사인 이미지 저장이 완료 되었습니다."   , Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    //mWebView.loadUrl("javascript:window.udf.");
                    Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                }

                break;

        }
    }

    public static void connectDevice(String address, boolean secure, boolean callback) {
        // Get the device MAC address
        //String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Attempt to connect to the device
        mPrintService.connect(device, secure, callback);
    }

    private void setupPrint() {
        // Initialize the BluetoothPrintService to perform bluetooth connections
        mPrintService = new BluetoothPrintService(this, mHandler);
        mWoosim = new WoosimService(mHandler);
    }

    // The Handler that gets information back from the BluetoothPrintService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //redrawMenu();
            switch (msg.what) {
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);

                    //로컬DB에 맥주소 저장
                    saveBluetoothMac(msg.getData().getString(DEVICE_MAC));
                    mMAC_ADDRESS = msg.getData().getString(DEVICE_MAC);

                    //Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "연결 성공 (" + mConnectedDeviceName + ")", Toast.LENGTH_SHORT).show();

                    System.out.println("_____ BT_CALLBACK MESSAGE_DEVICE_NAME " + msg.getData().getBoolean(BT_CALLBACK));
                    //콜백여부에 따라 인쇄시도
                    if (msg.getData().getBoolean(BT_CALLBACK))
                        //mWebView.loadUrl("javascript:SucessPrintCon();");
                        mWebView.loadUrl("javascript:pf_setBluetoothMAC("+mMAC_ADDRESS+");");

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();

                    System.out.println("_____ BT_CALLBACK MESSAGE_TOAST " + msg.getData().getBoolean(BT_CALLBACK));
                    //콜백여부에 따라 인쇄시도
/*                    if(msg.getData().getBoolean(BT_CALLBACK))
                        mWebView.loadUrl("javascript:FailPrintCon();");*/
                    break;
                case MESSAGE_READ:
                    mWoosim.processRcvData((byte[]) msg.obj, msg.arg1);
                    break;
                case WoosimService.MESSAGE_PRINTER:
                    switch (msg.arg1) {
                        case WoosimService.MSR:
                            Log.d(TAG, "MSR");
                            if (msg.arg2 == 0) {
                                Toast.makeText(getApplicationContext(), "MSR reading failure", Toast.LENGTH_SHORT).show();
                            } else {
                                byte[][] track = (byte[][]) msg.obj;
                                if (track[0] != null) {
                                    String str = new String(track[0]);
                                }
                                if (track[1] != null) {
                                    String str = new String(track[1]);
                                }
                                if (track[2] != null) {
                                    String str = new String(track[2]);
                                }
                            }
                            break;
                    }
                    break;

                case MESSAGE_DISCONNECT:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();

                    System.out.println("_____ BT_CALLBACK MESSAGE_DISCONNECT " + msg.getData().getBoolean(BT_CALLBACK));
                    if (msg.getData().getBoolean(BT_CALLBACK))
                        mWebView.loadUrl("javascript:Print();");

                    break;
            }
        }
    };


    public void CheckSdcard() {
        FileInputStream readFile = null;
        InputStreamReader inStream = null;
        BufferedReader buffReader = null;

        String strLine = null;
        String[] strSplit;
        final String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String defultPath = "/mnt";
        //String          strPath = "";

        try {
            readFile = new FileInputStream("/proc/mounts");
            inStream = new InputStreamReader(readFile);
            buffReader = new BufferedReader(inStream);

            while ((strLine = buffReader.readLine()) != null) {
                strSplit = strLine.split(" |,");

                if (strSplit[2].equals("tmpfs")) {
                    continue;
                }

                if (strSplit[0].contains("/dev/block/vold")     // "/dev/block/vold"으로 마운트된 디바이스중
                        && strSplit[1].contains(defultPath)                // read / write가 가능한 경우.
                        && strSplit[3].equals("rw")) {

                    if (externalPath.equals(strSplit[1])) {
                        continue;
                    }

                    if (!strSplit[1].equals(defultPath) && !strSplit[1].contains("asec")) {
                        SDRoot = strSplit[1];
                        break;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            //System.out.println(" ########## getAddtionalExternalStoragePath : FileNotFoundException : " +   e.toString() );

        } catch (IOException e) {
            //System.out.println(" ########## getAddtionalExternalStoragePath : IOException : " +   e.toString() );

        } finally {
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
                if (readFile != null) {
                    readFile.close();
                }
            } catch (IOException e) {

            }
        }

//System.out.println(" ############### [ String sdCardDir] : " +   SDRoot  );

        if ("".equals(SDRoot) || SDRoot == null) {
            SDRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

    private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error){
            super.onReceivedSslError(view, handler, error);
            StringBuilder sb= new StringBuilder();
            if (error != null) {
                switch (error.getPrimaryError()) {
                    case SslError.SSL_EXPIRED:
                        sb.append("이 사이트의  보안 인증서가 만료되었습니다.\n");
                        break;
                    case SslError.SSL_IDMISMATCH:
                        sb.append("이 사이트의 보안 인증서 ID가 일치하지 않습니다.\n");
                        break;
                    case SslError.SSL_NOTYETVALID:
                        sb.append("이 사이트의 보안 인증서가 아직 유효하지 않습니다.\n");
                        break;
                    case SslError.SSL_UNTRUSTED:
                        sb.append("이 사이트의 이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n");
                        break;
                    default:
                        sb.append("보안 인증서에 오류가 있습니다.\n");
                        break;
                }
            }
            sb.append("계속 진행하시겠습니까?");
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(sb.toString());
            builder.setPositiveButton("진행", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("쥐소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL", url);
            view.loadUrl(url);
            return true;
        }
    }



    /**
     * Print data.
     *
     * @param data A byte array to print.
     */
    public static void sendData(byte[] data) {
        // Check that we're actually connected before trying printing
        if (mPrintService.getState() != BluetoothPrintService.STATE_CONNECTED) {
            Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (data.length > 0)
            mPrintService.write(data);
    }

    // 맥어드레스 localStorage MAC_ADDR에 저장
    public void saveBluetoothMac(String strMac_Addr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript("localStorage.setItem('MAC_ADDR','" + strMac_Addr + "');", null);
        } else {
            mWebView.loadUrl("javascript:localStorage.setItem('MAC_ADDR','" + strMac_Addr + "');");
        }
        mMAC_ADDRESS = strMac_Addr;
    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean requestPermission() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }

        if (!permissionList.isEmpty()) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("전화번호 수집 동의");
            builder.setMessage("WCPS앱은 사전 등록된 사용자만 실행할 수 있습니다. 사전 등록 여부를 판단하기 위해 단말기 전화번호 수집이 필요하며, 수집에 동의해주셔야 사용이 가능합니다. 거절하시는 경우 앱은 종료됩니다.");
            builder.setPositiveButton("동의", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();

            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
        }

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mPHONE_NUM = manager.getLine1Number();
        if(mPHONE_NUM.startsWith("+82")) // 국제번호(+82 10...)로 되어 있을경우 010 으로 변환
        {
            mPHONE_NUM = mPHONE_NUM.replace("+82", "0");
        }
        return true;
    }

    @Override
    public void onBackPressed() {
    } // 백버튼 해제
}
