package com.logisall.wcps;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.woosim.printer.WoosimBarcode;
import com.woosim.printer.WoosimImage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Timer;

public class UDFInterface { //implements LocationListener {
	public final static int QUICK = 0;
	public Dialog dialog2;
	final String mimeType = "text/html";
	final String encoding = "utf-8";
	private Handler mHandler = new Handler();
	private WebView mWebView;
	private Context mContext;

//	String start_url_server = common.start_url_server;
	//MyProgressDialog mDialog = null;
	Timer time = null;
	
	//DB
	SQLiteOpenHelper mdbHelper;
	SQLiteDatabase mdb;
	
	public UDFInterface(WebView appView, Context context) {
		this.mWebView = appView;
		mContext = context;
	}
	
	public void M_toast(final String description) {
		mHandler.post(new Runnable() {
			
			public void run() {
				Toast.makeText(mContext, description, Toast.LENGTH_SHORT)
						.show();

/*
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("GPS정보가져오기").setMessage("GPS 위성 정보를 활성화 해주시기 바랍니다.")
				.setCancelable(false)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// GPS
						//moveConfigGPS();
					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
*/
			}
		});
	}

/*	public void close() {
		mWebView.clearHistory();
		mWebView.loadUrl(start_url_server + "index.html");
	}*/

	public void clear() {
		mWebView.clearHistory();
	}

	/**
	 * 쿠키 대신 사용할 저장 매체
	 * **/
	public void M_setSharedfernce(final String fname, final String fvalue) {
		mHandler.post(new Runnable() {
			public void run() {
				ContextWrapper wrapper = new ContextWrapper(
						mContext);

				SharedPreferences prefs = wrapper.getSharedPreferences("Name",
						wrapper.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(fname, fvalue);
				editor.commit();
			}
		});
	}

	public String M_getSharedfernce(String fname) {
		String strfvalue = "";
		SharedPreferences pre2 = mContext.getSharedPreferences("Name",
				mContext.MODE_PRIVATE);
		// SharedPreferences pre2 = getSharedPreferences("Name2",
		// Context.MODE_WORLD_WRITEABLE);
		strfvalue = pre2.getString(fname, "");

		return strfvalue;
	}

	
	public String C2DMGetDeviceId() {
		String strDeviceId = "";
		SharedPreferences prefs = mContext.getSharedPreferences("Name",
				mContext.MODE_PRIVATE);
		strDeviceId = prefs.getString("strDeviceId", "");

		return strDeviceId;
	}

/*	*//**
	 * AES 128Bit 암호화... 2012-07-03 기창. .
	 * 
	 **//*
	public String encriptByAES(String tagetStr) {
		String orgString = tagetStr;
		String key = "woosim.col";

		try {
			String enString = StringEncrypt.encryptAES(orgString, key);
			orgString = enString;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return orgString;
	}

	*//**
	 * AES 128Bit 복호화... 2012-07-03 기창. .
	 * 
	 **//*
	public String decriptByAES(String tagetStr) {
		String orgString = tagetStr;
		String key = "woosim.col";

		try {
			String enString = StringEncrypt.decryptAES(orgString, key);

			orgString = enString;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return orgString;
	}*/


/*	public void htmlImg_return(String ipath) {
		String img64Str = "";

		try {
			img64Str = Base64.encodeFromFile(ipath);

			mWebView.loadUrl("javascript:fn_PictureRead("+img64Str+");");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			img64Str = "";
		}
	}*/

	public void M_Date_Day(final String yymmdd, final String data,
                           final String gubun, final String separator) {
		// final DateFormat fmtDateAndTime = DateFormat.getDateTimeInstance();

		final Calendar dateAndTime = Calendar.getInstance();

		mHandler.post(new Runnable() {
			public void run() {
				DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
					public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
						dateAndTime.set(Calendar.YEAR, year);
						dateAndTime.set(Calendar.MONTH, monthOfYear);
						dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						updateLabel();
					}
				};
				TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
						dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
						dateAndTime.set(Calendar.MINUTE, minute);
						updateLabel();
					}
				};

				if (!"".equals(data)) {
					if ("D".equals(gubun)) {
						new DatePickerDialog(mContext, d, dateAndTime
								.get(Calendar.YEAR), dateAndTime
								.get(Calendar.MONTH), Integer.parseInt(data
								.substring(0, 2))).show();
					} else if ("M".equals(gubun)) {
						new DatePickerDialog(mContext, d, dateAndTime
								.get(Calendar.YEAR), Integer.parseInt(data
								.substring(0, 2)) - 1, Integer.parseInt(data
								.substring(3, 5))).show();
					} else {
						new DatePickerDialog(mContext, d, Integer.parseInt(data
								.substring(0, 4)), Integer.parseInt(data
								.substring(5, 7)) - 1, Integer.parseInt(data
								.substring(8, 10))).show();
					}
				} else {
					new DatePickerDialog(mContext, d, dateAndTime
							.get(Calendar.YEAR), dateAndTime
							.get(Calendar.MONTH), dateAndTime
							.get(Calendar.DAY_OF_MONTH)).show();
				}
			}

			private void updateLabel() {
				String rtn_data = "";
				String tempSeparator = "";

				// 구분자로 아무런 값도 들어오지 않으면 하이픈(-)이 구분자
				if (separator.equals("")) {
					tempSeparator = "-";
				} else {
					tempSeparator = separator;
				}

				if ("D".equals(gubun)) {
					rtn_data = pad(dateAndTime.get(Calendar.DAY_OF_MONTH));
				} else if ("M".equals(gubun)) {
					rtn_data = pad(dateAndTime.get(Calendar.MONTH) + 1)
							+ tempSeparator
							+ pad(dateAndTime.get(Calendar.DAY_OF_MONTH));
				} else {
					rtn_data = Integer.toString(dateAndTime.get(Calendar.YEAR))
							+ tempSeparator
							+ pad(dateAndTime.get(Calendar.MONTH) + 1)
							+ tempSeparator
							+ pad(dateAndTime.get(Calendar.DAY_OF_MONTH));
				}

				rtn_data = "document.getElementById('" + yymmdd + "').value='"
						+ rtn_data + "';";

				mWebView.loadUrl("javascript:" + rtn_data);
			}
		});
	}

	public static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}


	public static String addhypne(String src) {
		if (src == null)
			return "";

		src = src.trim();

		StringBuffer sb = new StringBuffer();
		sb.append(src.substring(0, 4));
		sb.append("-");
		sb.append(src.substring(4, 6));
		sb.append("-");
		sb.append(src.substring(6));

		return sb.toString();
	}

	/** UDF 카메라 호출 TEST startActivityForResult 12 **/
	public void CallCamera(String name) {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		MainActivity.CAMERA_name = name;
		((Activity) mContext).startActivityForResult(cameraIntent, 12);
	}

	/** UDF 갤러리 호출 TEST startActivityForResult 11 **/
	public void CallGallery() {

		Uri muri = Uri.parse("content://media/external/images/media");
		Intent i = new Intent(Intent.ACTION_VIEW, muri);

		i.setAction(Intent.ACTION_GET_CONTENT);
		i.setType("image/*");
		((Activity) mContext).startActivityForResult(i, 11);

	}

	// 종료버튼
	@JavascriptInterface
	public void CallEndProcess() {
			//common.APP_RUN_CHECK = false;
			//mWebView.loadUrl("javascript:M_ProgressDialogClose();");

			((Activity) mContext).finish();
			((ActivityManager) mContext
					.getSystemService(mContext.ACTIVITY_SERVICE))
					.restartPackage(mContext.getPackageName());
			android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	@JavascriptInterface
	public String getCommon(String valName) {
		String val = "";

		if ("internet_chk".equals(valName)) {
			//val = String.valueOf(common.internet_chk);
		}
		if ("version".equals(valName)) {
			//val = common.version;
		}
		if ("device_Address".equals(valName)) {
			//val = common.device_Address;
		}
		if ("userID".equals(valName)) {
			//val = common.userID;
		}
		if ("phoneNo".equals(valName)) {
			//val = common.phoneNo;
			val = MainActivity.mPHONE_NUM;
		}
		if ("serial".equals(valName)) {
			//val = common.serial;
		}
		if ("macAddress".equals(valName)) {
			//val = common.macAddress;
		}
		
		if("reg_id".equals( valName )){
			//val = common.reg_id ;
		}

		return val;
	}


	/**
	 * M_Clear 히스토리를 날려버린다. 메인페이지에 사용.
	 * */
/*	public void M_Clear() {
		mWebView.clearHistory();
	}

	public void M_ProgressDialogClose_New() {
		mHandler.post(new Runnable() {
			public void run() {
				if (mDialog.isShowing())
					mDialog.dismiss();
			}
		});
	}

	public void M_ProgressDialog_New() {
		mHandler.post(new Runnable() {
			public void run() {
				mDialog = MyProgressDialog.show(mContext, "", "", true, true,
						null);
			}
		});
	}

	public void M_ProgressDialog_New_NOCANCEL() {
		mHandler.post(new Runnable() {
			public void run() {
				mDialog = MyProgressDialog.show(mContext, "", "", true, false,
						null);
			}
		});
	}*/
	
	//싸인패드
	@JavascriptInterface
	 public void CallSignView(String signName, String signNameA, String oA ){

    	Intent i = new Intent( mContext , Triphospaint.class);
    	i.putExtra("signName", signName  );
//Log.d(common.LogTagName, "_____ signName :"+signName);
		if (oA.equals("O"))
		{
			((Activity) mContext).startActivityForResult( i , 18  );
		} else {
			((Activity) mContext).startActivityForResult( i , 19  );
		}
    	 
    }

	//프린터
	@JavascriptInterface
	public void SetPrinter(){
		Intent i = new Intent( mContext , DeviceList.class);
    	((Activity) mContext).startActivityForResult( i ,110  );
    }
	 
	public boolean MultiPartFormData(String FileName, String sendUrl, String type) {
		try
		{
	    	String path = "";
    	
	    	if (Build.VERSION.SDK_INT < 4.4){
	    		path = MainActivity.SDRoot+"/PICTURE/";
	    	} else {
	    		path = Environment.getExternalStorageDirectory()+"/PICTURE/";
	    	}
	    	
			//파일 위치
			if(type.equals("SIGN")){
		    	if (Build.VERSION.SDK_INT < 4.4){
		    		path = MainActivity.SDRoot+"/LogisALL_SIGN/";
		    	} else {
		    		path = Environment.getExternalStorageDirectory()+"/LogisALL_SIGN/";
		    	}
			}
				
			String exsistingFile = FileName;

			//파일을 받는 부분 소스가 필요함
			//String SERVICE_ENDPOINT = "http://210.109.94.32:80/iy/FILERECEIVE.jsp";
			String SERVICE_ENDPOINT = sendUrl;
			System.out.println(sendUrl);

			//org.apache.http.client.HttpClient 로 실행
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost Post = new HttpPost(SERVICE_ENDPOINT);
			
			Post.setHeader("Connection", "Keep-Alive");
			Post.setHeader("Accept-Charset", "UTF-8");
			Post.setHeader("ENCTYPE", "multipart/form-data");
			
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			//MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
			
			entity.addPart("testParam1", new StringBody("Englsh", Charset.forName("UTF-8")));		//영어테스트
			entity.addPart("testParam2", new StringBody("한글", Charset.forName("UTF-8")));			//한글테스트

			entity.addPart("FileName", new FileBody(new File(path+"/"+exsistingFile))); 			//파일
			//entity.addPart("FileName", new FileBody(new File(path+"/"+exsistingFile), "IMAGE/PNG", Charset.forName("UTF-8")));
			
			Post.setEntity(entity);
			
			//10초 응답시간 타임아웃 설정
            HttpParams params = httpclient.getParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpConnectionParams.setConnectionTimeout(params, 10*1000);
            HttpConnectionParams.setSoTimeout(params, 10*1000);
			
			HttpResponse response = httpclient.execute(Post);
			HttpEntity resEntity = response.getEntity();
			
			// requset -  0 성공, -1 파일없음, -2 Exception
			String requset = EntityUtils.toString(resEntity);
			requset = requset.replaceAll("\r\n", "");

			if(resEntity != null)
			{
System.out.println("_____ 0 requset :"+requset);
				if(requset.equals("0"))
				{
System.out.println("_____ 1");

					//전송후 파일삭제
					//M_fileDelete(type, FileName+".bmp");

					return true;
				}
				else
				{
System.out.println("_____ 2");
					return false;
				}
			}
			else
			{
System.out.println("_____ 3");
				return false;
			}

		}
		catch(Exception e)
		{
//Log.e("___ MultiPartFormData Exception", e.toString());
System.out.println("__ err :"+e.toString());
			//return e.toString();
			return false;
		}
	}
	
	
	public int M_fileDelete(String type, String filename){
		

    	String path = "";
    	
    	if (Build.VERSION.SDK_INT < 4.4){
    		path = MainActivity.SDRoot+"/PICTURE/";
    	} else {
    		path = Environment.getExternalStorageDirectory()+"/PICTURE/";
    	}
    	
		//파일 위치
		if(type.equals("SIGN")){
	    	if (Build.VERSION.SDK_INT < 4.4){
	    		path = MainActivity.SDRoot+"/LogisALL_SIGN/";
	    	} else {
	    		System.out.println("KIT KAT");
	    		path = Environment.getExternalStorageDirectory()+"/LogisALL_SIGN/";
	    	}
		}
		
		try {
		
			String sfpath = "" ;
			sfpath = path + filename ;
//Log.d(common.LogTagName, "_____ M_fileDelete :"+sfpath);
	
			File file = new File(sfpath);
		  
			if (file.exists()){
				if( file.delete() ){	  
					return 1 ;
				}else{
					return 0 ;
				}
				
			}else{
				return 0 ;
			}
		}
		catch(Exception e ) {
			e.printStackTrace();
//Log.e(common.LogTagName, "파일 삭제 실패 :"+e.toString());
	
			return -100;
		}
	}
	
	public void HistoryBack() {
		if(mWebView.canGoBack()) 
		{
			mWebView.goBack();
		}
	}
	
	public void Reload() {
		mWebView.reload();
	}
	
	/*public String ConnectCheck() {
	
		String result = "-1";
		int timeout = 3000;
        InputStream content = null;
			
		HttpResponse response = null;
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), timeout);
		HttpConnectionParams.setSoTimeout(client.getParams(), timeout);

		if(common.internet_chk) {

			HttpGet httpGet = new HttpGet(common.start_url_server+"ConnectCheck.jsp");
			try {
				response = client.execute(httpGet);
				content = response.getEntity().getContent();

				StringBuffer out = new StringBuffer();
				byte[] buffer = new byte[4094];
				int readSize;
				while ( (readSize = content.read(buffer)) != -1) {
				    out.append(new String(buffer, 0, readSize));
				}
				
				result = out.toString();
		
				content.close();
			}
			catch (ClientProtocolException cpe) {
				// TODO Auto-generated catch block
				//연결은 됐는데 안될
				cpe.printStackTrace();
				return "-1";
			} 
			catch (IOException ioe) {
				// TODO Auto-generated catch block
				//연결은 됐는데 안될 
				ioe.printStackTrace();
				return "-1";
			}
			*//*
			catch (MalformedURLException e) {
				e.printStackTrace();
				return "-1";
			}
			catch (SocketTimeoutException e) {
				e.printStackTrace();
				return "-1";
			}
			catch (HttpHostConnectException e) {
				e.printStackTrace();
				return "-1";
				
			}
			*//*
			catch (Exception e) {
				e.printStackTrace();
				return "-3";
			}
			finally {
				
				if(response != null ) {
					if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						result = "-1";
					}
					else {
						//do nothing
						//result = "1";
					}
				}
				else {
					result = "-2";
				}
			}
		}
		else {
			result = "-2";
		}

		return result;


	}*/
	
	
	@JavascriptInterface
	public String getWidth() {
		int nWidth, nHeight;
		Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		nWidth = display.getWidth();
		nHeight = display.getHeight() - 50;
		
		return String.valueOf(nWidth);
	}

	@JavascriptInterface
	public String getHeight() {
		int nWidth, nHeight;
		Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		nWidth = display.getWidth();
		nHeight = display.getHeight() - 50;
		
		return String.valueOf(nHeight);
	}
	
	@JavascriptInterface
	public void printTextLarge(String printData) {
		
    	byte[] text = null;
		try {
			text = printData.getBytes("EUC-KR");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (text.length == 0) return;
    	
    	ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
    	/*
    	byte[] cmd = {ESC, 0x45, (byte)(mEmphasis ? 1 : 0),		// ESC E
    				  ESC, 0x2D, (byte)(mUnderline ? 1 : 0),	// ESC -
    				  GS,  0x21, mCharsize,						// GS !	
    				  ESC, 0x61, mJustification,
    				  ESC, 0x74, (byte) 255};				// ESC a
    	*/

        /*if (position == 1) mCharsize = (byte)0x11;
    	else if (position == 2) mCharsize = (byte)0x22;
    	else if (position == 3) mCharsize = (byte)0x33;
    	else if (position == 4) mCharsize = (byte)0x44;
    	else if (position == 5) mCharsize = (byte)0x55;
    	else if (position == 6) mCharsize = (byte)0x66;
    	else if (position == 7) mCharsize = (byte)0x77;
    	else mCharsize = (byte)0x00;
        byte mCharsize = (byte)0x01;
        */
        
        final byte EOT = 0x04;
        final byte LF = 0x0a;
        final byte ESC = 0x1b;
        final byte GS = 0x1d;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        
        byte[] cmd = {GS,  0x21, 0x11 };    // ESC t


    	
    	buffer.append(cmd, 0, cmd.length);
    	buffer.append(text, 0, text.length);
    	buffer.append(LF);

		MainActivity.sendData(CMD_INIT_PRT);
		MainActivity.sendData(buffer.toByteArray());
    	//main.sendData(CMD_INIT_PRT);
    }
	@JavascriptInterface
	public void printTextMid(String printData) {
		
    	byte[] text = null;
		try {
			text = printData.getBytes("EUC-KR");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (text.length == 0) return;
    	
    	ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
    	/*
    	byte[] cmd = {ESC, 0x45, (byte)(mEmphasis ? 1 : 0),		// ESC E
    				  ESC, 0x2D, (byte)(mUnderline ? 1 : 0),	// ESC -
    				  GS,  0x21, mCharsize,						// GS !	
    				  ESC, 0x61, mJustification,
    				  ESC, 0x74, (byte) 255};				// ESC a
    	*/

        /*if (position == 1) mCharsize = (byte)0x11;
    	else if (position == 2) mCharsize = (byte)0x22;
    	else if (position == 3) mCharsize = (byte)0x33;
    	else if (position == 4) mCharsize = (byte)0x44;
    	else if (position == 5) mCharsize = (byte)0x55;
    	else if (position == 6) mCharsize = (byte)0x66;
    	else if (position == 7) mCharsize = (byte)0x77;
    	else mCharsize = (byte)0x00;
        byte mCharsize = (byte)0x01;
        */
        
        final byte EOT = 0x04;
        final byte LF = 0x0a;
        final byte ESC = 0x1b;
        final byte GS = 0x1d;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        
        byte[] cmd = {GS,  0x21, 0x10 };    // ESC t


    	
    	buffer.append(cmd, 0, cmd.length);
    	buffer.append(text, 0, text.length);
    	buffer.append(LF);
    	
    	MainActivity.sendData(CMD_INIT_PRT);
		MainActivity.sendData(buffer.toByteArray());
    	//main.sendData(CMD_INIT_PRT);
    }
	@JavascriptInterface
	public void printText(String printData) {
		
    	byte[] text = null;
		try {
			text = printData.getBytes("EUC-KR");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (text.length == 0) return;
    	
    	ByteArrayBuffer buffer = new ByteArrayBuffer(1024);
    	/*
    	byte[] cmd = {ESC, 0x45, (byte)(mEmphasis ? 1 : 0),		// ESC E
    				  ESC, 0x2D, (byte)(mUnderline ? 1 : 0),	// ESC -
    				  GS,  0x21, mCharsize,						// GS !	
    				  ESC, 0x61, mJustification,
    				  ESC, 0x74, (byte) 255};				// ESC a
    	*/

        /*if (position == 1) mCharsize = (byte)0x11;
    	else if (position == 2) mCharsize = (byte)0x22;
    	else if (position == 3) mCharsize = (byte)0x33;
    	else if (position == 4) mCharsize = (byte)0x44;
    	else if (position == 5) mCharsize = (byte)0x55;
    	else if (position == 6) mCharsize = (byte)0x66;
    	else if (position == 7) mCharsize = (byte)0x77;
    	else mCharsize = (byte)0x00;
        byte mCharsize = (byte)0x01;
        */
        
        final byte EOT = 0x04;
        final byte LF = 0x0a;
        final byte ESC = 0x1b;
        final byte GS = 0x1d;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        
    	byte[] cmd = {/*GS,  0x21, mCharsize,*/
    				  ESC, 0x74, (byte) 255};				// ESC t
    	
    	buffer.append(cmd, 0, cmd.length);
    	buffer.append(text, 0, text.length);
    	buffer.append(LF);

		MainActivity.sendData(CMD_INIT_PRT);
		MainActivity.sendData(buffer.toByteArray());
    	//main.sendData(CMD_INIT_PRT);
    }

	@JavascriptInterface
    public int pirntBMPImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        InputStream istr;
        Bitmap bitmap = null;
        AssetManager assetManager = mContext.getAssets();
        try{
        	istr = assetManager.open("bi.bmp");	
        } catch (IOException e){
//       		Log.e(common.LogTagName, "resource decoding is failed");
    		return -1;
        }
        
        bitmap = BitmapFactory.decodeStream(istr);

        byte[] data1 = WoosimImage.putARGBbitmap(0, 0, bitmap);
        bitmap.recycle();
        
        final byte ESC = 0x1b;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        byte[] cmd_pagemode = {ESC, 0x4c};      // Select page mode (ESC L)
        byte[] cmd_print = {ESC, 0x0c};         // Print data and delete in page mode
        byte[] cmd_stdmode = {ESC, 0x53};       // Select standard mode (ESC S)

        //main.sendData(CMD_INIT_PRT);
		MainActivity.sendData(cmd_pagemode);
		MainActivity.sendData(data1);
		MainActivity.sendData(cmd_print);
		MainActivity.sendData(cmd_stdmode);
        //main.sendData(CMD_INIT_PRT);
   	
    	return -1;
    }
	@JavascriptInterface
    public int pirntBMPImageGong() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        InputStream istr;
        Bitmap bitmap = null;
        AssetManager assetManager = mContext.getAssets();
        try{
        	istr = assetManager.open("gongcha.bmp");	
        } catch (IOException e){
//       		Log.e(common.LogTagName, "resource decoding is failed");
    		return -1;
        }
        
        bitmap = BitmapFactory.decodeStream(istr);

        byte[] data1 = WoosimImage.putARGBbitmap(0, 0, bitmap);
        bitmap.recycle();
        
        final byte ESC = 0x1b;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        byte[] cmd_pagemode = {ESC, 0x4c};      // Select page mode (ESC L)
        byte[] cmd_print = {ESC, 0x0c};         // Print data and delete in page mode
        byte[] cmd_stdmode = {ESC, 0x53};       // Select standard mode (ESC S)

        //main.sendData(CMD_INIT_PRT);
		MainActivity.sendData(cmd_pagemode);
		MainActivity.sendData(data1);
		MainActivity.sendData(cmd_print);
		MainActivity.sendData(cmd_stdmode);
        //main.sendData(CMD_INIT_PRT);
   	
    	return -1;
    }
	@JavascriptInterface
    public int pirntBMPImageChain() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        InputStream istr;
        Bitmap bitmap = null;
        AssetManager assetManager = mContext.getAssets();
        try{
        	istr = assetManager.open("chainplus.bmp");	
        } catch (IOException e){
//       		Log.e(common.LogTagName, "resource decoding is failed");
    		return -1;
        }
        
        bitmap = BitmapFactory.decodeStream(istr);

        byte[] data1 = WoosimImage.putARGBbitmap(0, 0, bitmap);
        bitmap.recycle();
        
        final byte ESC = 0x1b;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        byte[] cmd_pagemode = {ESC, 0x4c};      // Select page mode (ESC L)
        byte[] cmd_print = {ESC, 0x0c};         // Print data and delete in page mode
        byte[] cmd_stdmode = {ESC, 0x53};       // Select standard mode (ESC S)

        //main.sendData(CMD_INIT_PRT);
		MainActivity.sendData(cmd_pagemode);
		MainActivity.sendData(data1);
		MainActivity.sendData(cmd_print);
		MainActivity.sendData(cmd_stdmode);
        //main.sendData(CMD_INIT_PRT);
   	
    	return -1;
    }
	@JavascriptInterface
    public int pirntBMPKPNImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        InputStream istr;
        Bitmap bitmap = null;
        AssetManager assetManager = mContext.getAssets();
        try{
        	istr = assetManager.open("biKPN.bmp");	
        } catch (IOException e){
//       		Log.e(common.LogTagName, "resource decoding is failed");
    		return -1;
        }
        
        bitmap = BitmapFactory.decodeStream(istr);

        byte[] data1 = WoosimImage.putARGBbitmap(0, 0, bitmap);
        bitmap.recycle();
        
        final byte ESC = 0x1b;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        byte[] cmd_pagemode = {ESC, 0x4c};      // Select page mode (ESC L)
        byte[] cmd_print = {ESC, 0x0c};         // Print data and delete in page mode
        byte[] cmd_stdmode = {ESC, 0x53};       // Select standard mode (ESC S)

        //main.sendData(CMD_INIT_PRT);
		MainActivity.sendData(cmd_pagemode);
		MainActivity.sendData(data1);
		MainActivity.sendData(cmd_print);
		MainActivity.sendData(cmd_stdmode);
        //main.sendData(CMD_INIT_PRT);
   	
    	return -1;
    }
	@JavascriptInterface
    public int pirntBMPImage(String filename1, String filename2) {
    	String path = "";
    	
    	if (Build.VERSION.SDK_INT < 4.4){
    		path = MainActivity.SDRoot+"/LogisALL_SIGN/";
    	} else {
    		System.out.println("KIT KAT");
    		path = Environment.getExternalStorageDirectory()+"/LogisALL_SIGN/";
    	}
    	
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	
    	
    	
		options.inScaled = false;
		//public static Bitmap decodeResource (Resources res, int id, BitmapFactory.Options opts) 
    	//Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo1, options);
    	//Bitmap bmp = BitmapFactory.decodeFile(path+filename);
	
		System.out.println("signname1 = "+path+filename1);
		System.out.println("signname2 = "+path+filename2);
    	Bitmap bmp1 = BitmapFactory.decodeFile(path+filename1, options);
    	Bitmap bmp2 = BitmapFactory.decodeFile(path+filename2, options);
		System.out.println("signname1 = "+path+filename1);
		System.out.println("signname2 = "+path+filename2);
    	int viewHeight = 200;
    	float width = bmp1.getWidth();
    	float height = bmp1.getHeight();
    	//System.out.println("path:::::"+path+filename); 
    	// Calculate image's size by maintain the image's aspect ratio
    	if(height > viewHeight)
    	{
    	    float percente = (float)(height / 100);
    	    float scale = (float)(viewHeight / percente);
    	    width *= (scale / 170);
    	    height *= (scale / 170);
    	    System.out.println("w : h = "+ width + ":" + height);
    	}

    	// Resizing image
    	Bitmap sizingBmp1 = Bitmap.createScaledBitmap(bmp1, (int) width, (int) height, true);
    	Bitmap sizingBmp2 = Bitmap.createScaledBitmap(bmp2, (int) width, (int) height, true);
    	
    	if (bmp1 == null) {
//    		Log.e(common.LogTagName, "resource decoding is failed");
    		return -1;
    	}
    	
    	//byte[] data1 = WoosimImage.printRGBbitmap(0, 0, 250, 120, sizingBmp1);
    	//byte[] data2 = WoosimImage.printRGBbitmap(255, 0, 250, 120, sizingBmp2);
        byte[] data1 = WoosimImage.putARGBbitmap(0, 0, sizingBmp1);
        byte[] data2 = WoosimImage.putARGBbitmap(255, 0, sizingBmp2);

    	bmp1.recycle();
    	bmp2.recycle();
    	
        final byte ESC = 0x1b;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
    	byte[] cmd_pagemode = {ESC, 0x4c};	// Select page mode (ESC L)
    	byte[] cmd_print = {ESC, 0x0c};
    	byte[] cmd_stdmode = {ESC, 0x53};	// Select standard mode (ESC S)
    	
    	//main.sendData(CMD_INIT_PRT);
		MainActivity.sendData(cmd_pagemode);
		MainActivity.sendData(data1);
		MainActivity.sendData(data2);
		MainActivity.sendData(cmd_print);
		MainActivity.sendData(cmd_stdmode);
    	//main.sendData(CMD_INIT_PRT);
    	
	
    	return -1;
    }
	@JavascriptInterface
    public int pirntBARCODE(String chitNum) {
    	final byte[] barcode =  chitNum.getBytes();
    	ByteArrayBuffer buffer = new ByteArrayBuffer(1024);

        final byte ESC = 0x1b;
        final byte[] CMD_INIT_PRT = {ESC, 0x40};		// Initialize printer (ESC @)
        byte[] ESCO1 ={0x1b, '$', (byte)140, 0x00};
    	byte[] cmd_print = {0x0A};
    	byte[] cmd_stdmode = {ESC, 0x53};	// Select standard mode (ESC S)
    	byte[] cmd_pagemode = {ESC, 0x4c};	// Select page mode (ESC L)
    	//byte[] setPosition = { 0x1b, '$', 0x8C, 0 };	// Select standard mode (ESC S)
    	
    	final String title5 = "";
    	byte[] CODE39 = WoosimBarcode.createBarcode(WoosimBarcode.CODE39, 2, 60, false, barcode);
    	
    	
    	buffer.append(title5.getBytes(), 0, title5.getBytes().length);
    	buffer.append(ESCO1, 0, ESCO1.length);
    	buffer.append(CODE39, 0, CODE39.length);
    	//buffer.append(cmd_print, 0, cmd_print.length);
    	//buffer.append(cmd_stdmode, 0, cmd_stdmode.length);
    	

    	//main.sendData(cmd_pagemode);
		MainActivity.sendData(CMD_INIT_PRT);
		MainActivity.sendData(buffer.toByteArray());
		MainActivity.sendData(cmd_print);
   	
    	return -1;
    }
    
    @JavascriptInterface
    public boolean princConnectCheck() {
System.out.println("______ princConnectCheck :"+MainActivity.mPrintService.getState()+"|"+BluetoothPrintService.STATE_CONNECTED);
		if (MainActivity.mPrintService.getState() != BluetoothPrintService.STATE_CONNECTED)
			return false;
		else
			return true;
    }
    
    
    //웹에서 인쇄버튼을 통하여 연결하는 경우에는 callback 함수를 호출하게 한다..
	@JavascriptInterface
	public void BluetoothConnect(String address) {

		if(address.equals("")) {
			
		}
		else {
			if (MainActivity.mPrintService.getState() != BluetoothPrintService.STATE_CONNECTED) {
			
				boolean secure = true;
				StringBuffer buf = new StringBuffer();
				buf.append(Build.VERSION.RELEASE);
				String version = buf.toString();
				String target = "2.3.3";
				if ( version.compareTo(target) > 0 ) {
					secure = false;
				}
	Log.d("", "_____BluetoothConnect Build version :"+ version);
	Log.d("", "_____BluetoothConnect secure :"+ secure);
				//connectDevice(String address, boolean secure, boolean callback)
				MainActivity.connectDevice(address, secure, true);
	    		
			}
		}
	}

	//전화걸기
	public void Calling(final String phoneNo) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel://" + phoneNo));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		//((Activity) mContext).startActivity(intent);
	}
		/*
		//phone = phoneNo.trim().replaceAll("-", "");
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
		alt_bld.setTitle("알림").setMessage(phoneNo+"로 연결합니다.").setPositiveButton("확인", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNo));
				((Activity) mContext).startActivity(intent);
			}
		})
		.setNegativeButton("닫기", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which)
			{
			}
		})
		.create().show();
		*/
	//}
    
/*

		new MyAsyncTask().execute(common.start_url_server+"ConnectCheck.jsp");
		//new MyAsyncTask().execute(common.start_url_server+"ConnectCheck.jsp");
		return "";
		
	// background 작업에 사용할 data의 자료형: String 형
	// background 작업 진행 표시를 위해 사용할 인자: Void 형
	// background 작업의 결과를 표현할 자료형: HttpResponse 형
	// 인자를 사용하지 않은 경우 Void Type 으로 지정.
	//AsyncTask<Params, Progress, Result>
	public class MyAsyncTask extends AsyncTask<String, Void, HttpResponse> {

		//Background 작업 시작전에 UI 작업을 진행 한다.
		@Override
		protected void onPreExecute() {			 
			super.onPreExecute();
		}
	
		//Background 작업을 진행 한다.
		@Override
		protected HttpResponse doInBackground(String... params) {
	
			HttpResponse response = null;
			HttpClient client = new DefaultHttpClient(); 
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 3000);

			if(common.internet_chk) {		
				HttpGet httpGet = new HttpGet(params[0]); 
				try {
					response = client.execute(httpGet);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block					
					e.printStackTrace();
				}
			}

			try {
				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
Log.d(common.LogTagName,"_____ : "+response.getStatusLine().getStatusCode());
Log.d(common.LogTagName,"_____ : "+HttpStatus.SC_OK);
					return null;
				}
			} catch (Exception e) {
			}
			
			return response;
		}
	
		//Background 작업이 끝난 후 UI 작업을 진행 한다.
		@Override
		protected void onPostExecute(HttpResponse result) {
			super.onPostExecute(result);

			if(result != null) {
				try {
					EndEntity(result.getEntity());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace(); 
				}
			}
			else {
			
			}
	
		}
	
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
	
	private String EndEntity(HttpEntity entity) throws IllegalStateException, IOException {

		String line, result = "";
		if(entity != null){
		
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
	
			while((line = br.readLine()) != null) { 
				result += line; 
			}
		}
		else result = "-1";

		
Log.d(common.LogTagName,"_____ result :"+result);
		
		return result;
	}
*/
	
	
	
	
	
	/***** uln에서는 필요없는것들 ******/
	
	
	/*
	 * public void M_currgps() { mHandler.post(new Runnable() { public void
	 * run() { fn_gps(); } }); }
	 * 
	 * public void fn_gps() { try { Criteria criteria = new Criteria();
	 * criteria.setPowerRequirement(Criteria.POWER_MEDIUM); // 전원은 중간으로 소모
	 * criteria.setAccuracy(Criteria.ACCURACY_FINE); // 정확도는 다소 높게
	 * 
	 * // LocationListener의 핸들을 얻음 locManager = (LocationManager)
	 * mContext.getSystemService(Context.LOCATION_SERVICE);
	 * 
	 * // GPS로 부터 위치정보를 업데이트 요청, 1초마다 5km 이동시
	 * locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000,
	 * 5, this);
	 * 
	 * // 기지국으로 부터 위치정보를 업데이트 요청 // locManager.removeUpdates(this); // GPS 끄기
	 * LocationManager locMgr =
	 * (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	 * boolean isGPSEnabled = locMgr.isProviderEnabled
	 * (LocationManager.GPS_PROVIDER);
	 * 
	 * if (!isGPSEnabled) { // Intent locationSettingsIntent = new
	 * Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); //
	 * startActivity(locationSettingsIntent); }
	 * 
	 * // 주소를 확인하기 위한 Geocoder KOREA 와 KOREAN 둘다 가능 geoCoder = new
	 * Geocoder(mContext, Locale.KOREAN);
	 * 
	 * String point_juso = GetLocations();
	 * 
	 * if(point_juso.indexOf("0.0") >-1 ) {
	 * locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
	 * 600000, 5, this);
	 * 
	 * Location loc =
	 * locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //
	 * 맨마지막
	 * 
	 * if(loc != null) { onLocationChanged(loc); } } } catch (Exception e3) { //
	 * TODO Auto-generated catch block e3.printStackTrace(); } } public void
	 * M_gpsend(){ locManager.removeUpdates(this); } public void
	 * onLocationChanged(Location location) { myLocation = location; String
	 * point_juso = GetLocations();
	 * 
	 * point_juso = point_juso.replaceAll("대한민국 ", ""); String addr =
	 * point_juso.substring(point_juso.indexOf("@@") + 2); point_juso =
	 * point_juso.substring(0, point_juso.indexOf("@@"));
	 * 
	 * String curr_lat = point_juso.substring(0,point_juso.indexOf(",")-1);
	 * String curr_lng = point_juso.substring(point_juso.indexOf(",")+1);
	 * 
	 * System.out.println(addr); System.out.println(curr_lat);
	 * System.out.println(curr_lng);
	 * 
	 * try { // 결과 리턴 자바스크립트 호출 mWebView.loadUrl("javascript:fn_rtnGPS(" + "'" +
	 * curr_lat + "', '" + curr_lng + "', " + "'" + URLEncoder.encode(addr,
	 * "UTF-8") + "');" );
	 * mWebView.loadUrl("javascript:setSessionStorage('RE_LOCATION_JUSO','"+
	 * URLEncoder.encode(addr, "utf-8")
	 * +"' );setSessionStorage('RE_LOCATION_X','"+curr_lat+
	 * "' );setSessionStorage('RE_LOCATION_Y','"+curr_lng+"' );"); } catch
	 * (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * }
	 */
/*
	public void onProviderDisabled(String provider) {
		// Toast.makeText(mContext, "GPS 연결안됨", Toast.LENGTH_SHORT).show();
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		// Toast.makeText(mContext, "GPS연결됨", Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	public String GetLocations() {
		// 텍스트뷰를 찾음
		StringBuffer juso = new StringBuffer();
		geoCoder = new Geocoder(mContext, Locale.KOREAN);
		if (myLocation != null) {
			latPoint = myLocation.getLatitude();
			lngPoint = myLocation.getLongitude();
			speed = (float) (myLocation.getSpeed() * 3.6);

			try {
				// 위도,경도를 이용하여 현재 위치의 주소를 가져온다.
				List<Address> addresses;
				addresses = geoCoder.getFromLocation(latPoint, lngPoint, 1);
				for (Address addr : addresses) {
					int index = addr.getMaxAddressLineIndex();
					for (int i = 0; i <= index; i++) {
						juso.append(addr.getAddressLine(i));
						juso.append(" ");
					}
					juso.append("\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String point_juso = "";
		point_juso = String.valueOf(latPoint);
		point_juso = point_juso + "," + String.valueOf(lngPoint) + "@@" + juso;

		return point_juso;
	}
*/
	
/*
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			MuiltView okview = new MuiltView();
			okview.View(msg.obj.toString(), msg.arg1, mContext);

		}
	};


	Handler mhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case GPSEND:
				M_gpsend();
				break;

			}
		}
	};


	public String gpsRtnFname = "";

	// 반환 함수 존재하는 M_currgps
	public void M_currgps(final String ftnFn) {
		fn_gps(ftnFn);

		//mHandler.post(new Runnable() { public void run() { fn_gps( ftnFn ); }
		//  });
		 
	}

	public void fn_gps(final String ftnFn) {
		try {
			gpsRtnFname = ftnFn;
			gpsDialog = ProgressDialog.show(mContext, "위치 조회중...",
					"위치 정보를 조회중입니다.", true, false);
			
			Criteria criteria = new Criteria();
			criteria.setPowerRequirement(Criteria.POWER_HIGH); // 전원은 중간으로 소모
			criteria.setAccuracy(Criteria.ACCURACY_FINE); // 정확도는 다소 높게
			locManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				alertCheckGPS();// GPS
			}
			
			try{
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,	3000, 10, gpsListener);
				locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000, 10, netListener);
			}catch(Exception e){
				System.out.println(">>>>>>>>>>>>>Exception e  ::"+e.toString() );
			}

			time = new Timer();
			TimerTask GPSTask = new TimerTask() {
				public void run() {
					try {
						mhandler.sendEmptyMessage(GPSEND);
					} catch (Exception e) {
						time.cancel();
						time = null;
						gpsDialog.dismiss();
					}
				}
			};

			time.schedule(GPSTask, 20000);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
			gpsRtnFname = "";
		}
	}
	

	// GPS
	private void alertCheckGPS() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("GPS정보가져오기").setMessage("GPS 위성 정보를 활성화 해주시기 바랍니다.")
				.setCancelable(false)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// GPS
						moveConfigGPS();
					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// GPS
	private void moveConfigGPS() {
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		mContext.startActivity(gpsOptionsIntent);
		
	}
	public void updateWithNewLocation(Location location, String provider) {
		// 여기에서 처리를 해준다.
		// provider 값으로 location이 어떤 provider에서 들어왔는지 알 수 있다.
		if (isGpsReceived) { // gps 수신여부 체크
			if (LocationManager.GPS_PROVIDER.equals(provider)) {
				try {
					locManager.removeUpdates(gpsListener);
					locManager.removeUpdates(netListener);
					locManager = null;
					time.cancel();
					time = null;
					gpsDialog.dismiss();

				} catch (Exception e) {
					if (gpsDialog.isShowing())
						gpsDialog.dismiss();
				}

				myLocation = location; // 네트워크 위치정보
				
				
				String point_juso = GetLocations();
				point_juso = point_juso.replaceAll("대한민국 ", "");
				String addr = point_juso.substring(point_juso.indexOf("@@") + 2);
				point_juso = point_juso.substring(0, point_juso.indexOf("@@"));
				String curr_lat = point_juso.substring(0, point_juso.indexOf(",") - 1);
				String curr_lng = point_juso.substring(point_juso.indexOf(",") + 1);

				System.out.println("####" + addr);
				System.out.println("####" + curr_lat);
				System.out.println("####" + curr_lng);

				try {
					if ("".equals(gpsRtnFname)) {

						mWebView.loadUrl("javascript:setSessionStorage('RE_LOCATION_JUSO','"
								+ URLEncoder.encode(addr, "utf-8")
								+ "' );setSessionStorage('RE_LOCATION_X','"
								+ curr_lat
								+ "' );setSessionStorage('RE_LOCATION_Y','"
								+ curr_lng
								+ "' );");
					} else {
						// 결과 리턴 자바스크립트 호출
						// locManager.removeUpdates(this)
						mWebView.loadUrl("javascript:" + gpsRtnFname + "(" + "'"
								+ curr_lat + "', '" + curr_lng + "', " + "'"
								+ URLEncoder.encode(addr, "UTF-8") + "');");

						gpsRtnFname = "";
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			} else {
				long gpsGenTime = locManager.getLastKnownLocation(
						LocationManager.GPS_PROVIDER).getTime(); // 마지막으로 수신된
																	// GPS 위치정보
				long curTime = System.currentTimeMillis(); // 현재 시간
				if ((curTime - gpsGenTime) > 20000) { // gps 정보가 20초 이상 오래된 정보이면
														// 네트워크 위치정보 사용
					try {
						locManager.removeUpdates(gpsListener);
						locManager.removeUpdates(netListener);
						locManager = null;
						time.cancel();
						time = null;
						gpsDialog.dismiss();

					} catch (Exception e) {
						if (gpsDialog.isShowing())
							gpsDialog.dismiss();
					}

					myLocation = location; // 네트워크 위치정보
					String point_juso = GetLocations();
					point_juso = point_juso.replaceAll("대한민국 ", "");
					String addr = point_juso.substring(point_juso.indexOf("@@") + 2);
					point_juso = point_juso.substring(0, point_juso.indexOf("@@"));
					String curr_lat = point_juso.substring(0, point_juso.indexOf(",") - 1);
					String curr_lng = point_juso.substring(point_juso.indexOf(",") + 1);

					System.out.println("####" + addr);
					System.out.println("####" + curr_lat);
					System.out.println("####" + curr_lng);

					try {
						if ("".equals(gpsRtnFname)) {

							mWebView.loadUrl("javascript:setSessionStorage('RE_LOCATION_JUSO','"
									+ URLEncoder.encode(addr, "utf-8")
									+ "' );setSessionStorage('RE_LOCATION_X','"
									+ curr_lat
									+ "' );setSessionStorage('RE_LOCATION_Y','"
									+ curr_lng
									+ "' );");
						} else {
							// 결과 리턴 자바스크립트 호출
							// locManager.removeUpdates(this)
							mWebView.loadUrl("javascript:" + gpsRtnFname + "(" + "'"
									+ curr_lat + "', '" + curr_lng + "', " + "'"
									+ URLEncoder.encode(addr, "UTF-8") + "');");

							gpsRtnFname = "";
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					}
					isGpsReceived = false; // 플래그를 해제
					
				}
			}
		} else {
			myLocation = location; // 네트워크 위치정보
			try {
				locManager.removeUpdates(gpsListener);
				locManager.removeUpdates(netListener);
				locManager = null;
				time.cancel();
				time = null;
				gpsDialog.dismiss();

			} catch (Exception e) {
				if (gpsDialog.isShowing())
					gpsDialog.dismiss();
			}
			String point_juso = GetLocations();
			point_juso = point_juso.replaceAll("대한민국 ", "");
			String addr = point_juso.substring(point_juso.indexOf("@@") + 2);
			point_juso = point_juso.substring(0, point_juso.indexOf("@@"));
			String curr_lat = point_juso.substring(0, point_juso.indexOf(",") - 1);
			String curr_lng = point_juso.substring(point_juso.indexOf(",") + 1);

			System.out.println("####" + addr);
			System.out.println("####" + curr_lat);
			System.out.println("####" + curr_lng);

			try {
				if ("".equals(gpsRtnFname)) {

					mWebView.loadUrl("javascript:setSessionStorage('RE_LOCATION_JUSO','"
							+ URLEncoder.encode(addr, "utf-8")
							+ "' );setSessionStorage('RE_LOCATION_X','"
							+ curr_lat
							+ "' );setSessionStorage('RE_LOCATION_Y','"
							+ curr_lng
							+ "' );");
				} else {
					// 결과 리턴 자바스크립트 호출
					// locManager.removeUpdates(this)
					mWebView.loadUrl("javascript:" + gpsRtnFname + "(" + "'"
							+ curr_lat + "', '" + curr_lng + "', " + "'"
							+ URLEncoder.encode(addr, "UTF-8") + "');");

					gpsRtnFname = "";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}

	LocationListener gpsListener = new LocationListener() {
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onProviderDisabled(String provider) {

		}

		public void onLocationChanged(Location location) {
			isGpsReceived = true; // gps 위치정보가 수신되면 플래그를 set
			updateWithNewLocation(location, "gps");
			
		}
	};

	LocationListener netListener = new LocationListener() {
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onProviderDisabled(String provider) {

		}

		public void onLocationChanged(Location location) {
			updateWithNewLocation(location, "network");
		
		}
	};

	public void M_gpsend() {

		if (time != null) {
			locManager.removeUpdates(gpsListener);
			locManager.removeUpdates(netListener);
			locManager = null;
			time.cancel();
			time = null;
		}

		try {
			if (gpsDialog.isShowing())
				gpsDialog.dismiss();
		} catch (Exception e) {
			if (gpsDialog.isShowing())
				gpsDialog.dismiss();
		}
		if(gpsRtnFname.equals("Location.MapDraw")){
			new AlertDialog.Builder(mContext).setTitle("알림")
			.setMessage("GPS정보를 받아올수 없습니다.")
			.setNegativeButton("확인", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mWebView.loadUrl("javascript:" + gpsRtnFname + "(" + "'"
							+ "0" + "', '" + "0" + "', " + "'"
							+ "위치정보없음" + "');");

					gpsRtnFname = "";
					dialog.dismiss();
				}
			}).show();
		}else{
			new AlertDialog.Builder(mContext).setTitle("알림")
			.setMessage("GPS정보를 받아올수 없습니다. 활동등록을 진행하시겠습니까?")
			.setNegativeButton("취소", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					gpsRtnFname = "";
					dialog.dismiss();
				}
			}).setPositiveButton("확인", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mWebView.loadUrl("javascript:" + gpsRtnFname + "(" + "'"
							+ "0" + "', '" + "0" + "', " + "'"
							+ "위치정보없음" + "');");

					gpsRtnFname = "";
					dialog.dismiss();
				}
			}).show();
		}
		

	}

	public void onLocationChanged(Location location) {
		try {
			locManager.removeUpdates(this);
			locManager = null;
			time.cancel();
			time = null;
			gpsDialog.dismiss();

		} catch (Exception e) {
			if (gpsDialog.isShowing())
				gpsDialog.dismiss();
		}

		myLocation = location;
		String point_juso = GetLocations();
		point_juso = point_juso.replaceAll("대한민국 ", "");
		String addr = point_juso.substring(point_juso.indexOf("@@") + 2);
		point_juso = point_juso.substring(0, point_juso.indexOf("@@"));
		String curr_lat = point_juso.substring(0, point_juso.indexOf(",") - 1);
		String curr_lng = point_juso.substring(point_juso.indexOf(",") + 1);

		System.out.println("####" + addr);
		System.out.println("####" + curr_lat);
		System.out.println("####" + curr_lng);

		try {
			if ("".equals(gpsRtnFname)) {

				mWebView.loadUrl("javascript:setSessionStorage('RE_LOCATION_JUSO','"
						+ URLEncoder.encode(addr, "utf-8")
						+ "' );setSessionStorage('RE_LOCATION_X','"
						+ curr_lat
						+ "' );setSessionStorage('RE_LOCATION_Y','"
						+ curr_lng
						+ "' );");
			} else {
				// 결과 리턴 자바스크립트 호출
				// locManager.removeUpdates(this)
				mWebView.loadUrl("javascript:" + gpsRtnFname + "(" + "'"
						+ curr_lat + "', '" + curr_lng + "', " + "'"
						+ URLEncoder.encode(addr, "UTF-8") + "');");

				gpsRtnFname = "";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
*/

/*	//로컬테이블 전체 조회
	public StringBuffer getLocalTableList() {

		StringBuffer rtnData = new StringBuffer();
		SQLiteOpenHelper dbHelperUdf	= new DBManager(mContext);
		SQLiteDatabase dbUdf			= dbHelperUdf.getWritableDatabase();

		//생성된 TABLE 보기
		Cursor result = dbUdf.rawQuery("select name from sqlite_master where type='table' ", null);
		if(result.getCount() > 0)
		{
			rtnData.append("[{").append("\"");
			rtnData.append("res");
			rtnData.append("\":\"");
			rtnData.append(result.getCount());
			rtnData.append("\"").append(",");
			rtnData.append("\"");
			rtnData.append("rtnData");
			rtnData.append("\":");

			rtnData.append("[");
			int i = 0;
			while (result.moveToNext())
			{
				rtnData.append("{");
				//System.out.println("__________ ilyang table :"+result.getString(0));
				rtnData.append("\"");
				rtnData.append("TABELNAME");
				rtnData.append("\":");
				rtnData.append("\"");
				rtnData.append(result.getString(0));
				rtnData.append("\"");

				rtnData.append("},");
			}

			if(result.getCount() > 0)
				rtnData.delete(rtnData.length()-1, rtnData.length());


			rtnData.append("]");
			rtnData.append("}]");
		}
		else {

			rtnData.append("[{").append("\"");
			rtnData.append("res");
			rtnData.append("\":\"");
			rtnData.append("-1");
			rtnData.append("\"");
			rtnData.append("}]");
		}


		dbUdf.close();
		dbHelperUdf.close();

		return rtnData;
	}

	public void BeginTran() {

		boolean tranFlag = false;
		//DB
		try {
			tranFlag = mdb.inTransaction();
		}
		catch(Exception e) {
			Log.d(common.LogTagName, "__________ tranFlag Exception :"+e.toString());
			tranFlag = false;
		}
		Log.d(common.LogTagName, "__________ tranFlag :"+tranFlag);
		//기존에 락이 걸려있으면 rollback을 한다
		if(tranFlag) {

			mdb.endTransaction();

			mdb.close();
			mdbHelper.close();
		}

		mdbHelper	= new DBManager(mContext);
		mdb		= mdbHelper.getWritableDatabase();

		mdb.beginTransaction();
	}


	public void SuccessTran() {

		mdb.setTransactionSuccessful();
	}

	public void endTran() {

		mdb.endTransaction();

		mdb.close();
		mdbHelper.close();
	}


	// TableName : 로컬테이블명, whereCondion : 조건절, ColumnName : 업데이트 컬럼, Param_list : 업데이트 값
	// ex. TableName : ORDER_SEQ
	// ex. whereCondion : ORD_NUM = '20130321S0025' AND INS_DATE = '20130321'
	// ex. ColumnName : ORDER_STATUS@@SEQ
	// ex. Param_list : 3@@1
	public String M_localexecForCH(final String TableName, final String ColumnName, final String ParamList,
								   final String whereCondion, final String gubun) {

		StringBuffer rtnData = new StringBuffer();

		try
		{
			String[] ColumnArgs = null;
			if(!ColumnName.trim().equals(""))
				ColumnArgs = ColumnName.split("@@");

			String[] ParamArgs = null;
			if(!ParamList.trim().equals(""))
				ParamArgs = ParamList.split("@@",ColumnArgs.length);

			ContentValues value = new ContentValues();

			if(!ColumnName.trim().equals("") && !ParamList.trim().equals("")) {
				for(int i=0 ; i < ColumnArgs.length ; i++) {
					value.put(ColumnArgs[i], ParamArgs[i]);
//Log.d(common.LogTagName, "_____ : "+ColumnArgs[i]+","+ParamArgs[i]);
				}
			}
//Log.d(common.LogTagName, "_____ : "+TableName+","+whereCondion);

			int cnt = 0;

			//if(gubun.equals("select")) {
			//public Cursor query (String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
			//Cursor cursor = mdb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
			//}

			if(gubun.toUpperCase().equals("UPDATE"))
				cnt = mdb.update(TableName, value, whereCondion, null);
			else if(gubun.toUpperCase().equals("INSERT"))
				cnt = (int) mdb.insert(TableName, null, value);
			else
				cnt = mdb.delete(TableName, whereCondion, null);

			rtnData.append("[{").append("\"");
			rtnData.append("res");
			rtnData.append("\":\"");
			rtnData.append(Integer.toString(cnt));
			rtnData.append("\"");
			rtnData.append("}]");

		}
		catch (Exception e)
		{
			Log.e(common.LogTagName, "_____ M_localexecForCH '"+gubun+"' e :"+e.toString());
			e.printStackTrace();

			String errLog = "-2";
			//키값에 중복데이터가 들어온경우
			if(e.toString().indexOf("constraint failed") > 0)
				errLog = "-1";

			rtnData.append("[{").append("\"");
			rtnData.append("res");
			rtnData.append("\":\"");
			rtnData.append(errLog);
			rtnData.append("\"").append(",");
			//에러로그
			rtnData.append("\"");
			rtnData.append("rtnData");
			rtnData.append("\":\"");
			rtnData.append(e.toString());
			rtnData.append("\"");
			rtnData.append("}]");

		}
		finally{
		}
		String returnValue = rtnData.toString();
		rtnData.delete(0, returnValue.length());

		return returnValue;
	}


	// 로컬DB - 쿼리문
	public StringBuffer M_localSelectQuery(final String sql, final String Param_list) {
		SQLiteOpenHelper dbHelperUdf = new DBManager(mContext);
		SQLiteDatabase dbUdf = dbHelperUdf.getWritableDatabase();

		StringBuffer rtnData = new StringBuffer();
		Cursor result = null;

		try {
			String[] args = null;
			if (!Param_list.trim().equals(""))
				args = Param_list.split("@@");

			if (sql.substring(0, 8).toUpperCase().indexOf("SELECT") <= -1) {
				if (Param_list.equals(""))
					result = dbUdf.rawQuery(sql, null);
				else
					result = dbUdf.rawQuery(sql, args);

				Log.d(common.LogTagName, "_____ M_localSelectQuery cnt : "	+ result.getCount() + "|" + sql);

				rtnData.append("[{").append("\"");
				rtnData.append("res");
				rtnData.append("\":\"");
				rtnData.append("1");
				rtnData.append("\"");
				rtnData.append("}]");
			} else {
				if (Param_list.equals(""))
					result = dbUdf.rawQuery(sql, null);
				else
					result = dbUdf.rawQuery(sql, args);

				rtnData.append("[{").append("\"");
				rtnData.append("res");
				rtnData.append("\":\"");
				rtnData.append(result.getCount());
				rtnData.append("\"").append(",");
				rtnData.append("\"");
				rtnData.append("rtnData");
				rtnData.append("\":");

				rtnData.append("[");
				while (result.moveToNext()) {

					rtnData.append("{");
					// 커서의 각 칼럼 접근
					for (int i = 0; i <= result.getColumnCount() - 1; i++) {
						rtnData.append("\"");
						rtnData.append(result.getColumnName(i));
						rtnData.append("\":");
						rtnData.append("\"");
						rtnData.append(result.getString(i));
						rtnData.append("\",");
					}
					// 맨마지막 로우 , 제거
					rtnData.delete(rtnData.length() - 1, rtnData.length());

					rtnData.append("},");
				}

				if (result.getCount() > 0)
					rtnData.delete(rtnData.length() - 1, rtnData.length());

				rtnData.append("]");
				rtnData.append("}]");
			}

		} catch (Exception e) {
			Log.e(common.LogTagName, "_____ M_localSelectQuery e :" + e.toString());
			e.printStackTrace();

			// [{"res":"-1","rtnData":"android.database.sqlite.SQLiteConstraintException: constraint failed"}]
			String errLog = "-2";
			// 키값에 중복데이터가 들어온경우
			if (e.toString().indexOf("constraint failed") > 0)
				errLog = "-1";

			rtnData.append("[{").append("\"");
			rtnData.append("res");
			rtnData.append("\":\"");
			rtnData.append(errLog);
			rtnData.append("\"").append(",");
			// 에러로그
			rtnData.append("\"");
			rtnData.append("rtnData");
			rtnData.append("\":\"");
			rtnData.append(e.toString());
			rtnData.append("\"");
			rtnData.append("}]");

		} finally {
			// DB를 닫는다.
			result.close();

			dbUdf.close();
			dbHelperUdf.close();
		}
//Log.d(common.LogTagName, "_____ M_localSelectQuery rtnData :" + rtnData);


		return rtnData;
	}*/
}