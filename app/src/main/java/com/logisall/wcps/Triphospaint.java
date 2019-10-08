package com.logisall.wcps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

public class Triphospaint  extends Activity {

	LinearLayout mContent;
	LinearLayout mContentA;
	signature mSignature;
	signature mSignatureA;
	Button mClear, mGetSign, mCancel;
	public static String tempDir;
	public int count = 1; 
	public String current = null;
	public String currentA = null;
	private Bitmap mBitmap;
	private Bitmap mBitmapA;
	View mView;
	View mViewA;
	File mypath;
	File mypathA;
	
	// 결과 넘기기위함
	Bundle extra;
	Intent intent;
	
	String signName = "";
	String signNameA = "";
	public static String SignEventXY = "";
	public static String SignEventXYA = "";
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.triphospaint);
		
		
		/** 레이아웃 리사이징 **/
		LinearLayout layout_resize = (LinearLayout)findViewById(R.id.linearLayout);
		LinearLayout layout_resizeA = (LinearLayout)findViewById(R.id.linearLayoutA);
		
		LinearLayout.LayoutParams lp = null;
		LinearLayout.LayoutParams lpA = null;
		lp = (LinearLayout.LayoutParams)layout_resize.getLayoutParams();
		lpA = (LinearLayout.LayoutParams)layout_resizeA.getLayoutParams();

		//레이아웃 높이를 너비를 기존 PDA의 비율로 맞춰준다
		double PDAwidth = 80.0/160.0;	//height/width
		lp.height = (int)((double)getWindowManager().getDefaultDisplay().getWidth()*PDAwidth);
		lpA.height = (int)((double)getWindowManager().getDefaultDisplay().getWidth()*PDAwidth);
		layout_resize.setLayoutParams(lp);
		layout_resizeA.setLayoutParams(lpA);
		/** 레이아웃 리사이징 끝 **/
		
		SignEventXY = "";
		SignEventXYA = "";
		
		extra = new Bundle();
		intent = new Intent();
		
		mContent = (LinearLayout) findViewById(R.id.linearLayout);
		mContentA = (LinearLayout) findViewById(R.id.linearLayoutA);
		mSignature = new signature(this, null);
		mSignature.setBackgroundColor(Color.WHITE);
		mSignatureA = new signature(this, null);
		mSignatureA.setBackgroundColor(Color.WHITE);
		
		mContent.addView(mSignature, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mContentA.addView(mSignatureA, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		//mContent.addView(mSignature, 309, 300);
		
		mClear = (Button)findViewById(R.id.clear);
		mGetSign = (Button)findViewById(R.id.getsign);
		mGetSign.setEnabled(false); 
		mCancel = (Button)findViewById(R.id.cancel);
		mView = mContent;
		mViewA = mContentA;
		
		// 사인 Activity에있는 입력 창 데이터 :  html 호출시 파라미터로 넘어온 값이 박힌다. (HAWB_NO)
		signName = getIntent().getStringExtra("signName");
		signNameA = getIntent().getStringExtra("signNameA");
		System.out.println("signNameA:::::::::::::"+signNameA+"====");
        
		mClear.setOnClickListener(new OnClickListener()
		{         
			public void onClick(View v)
			{
				Log.v("log_tag", "Panel Cleared");
				//사인좌표 초기화
				SignEventXY = "";
				SignEventXYA = "";
				mSignature.clear();
				mSignatureA.clear();
				mGetSign.setEnabled(false);
			}
		});
		
		mGetSign.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v("log_tag", "Panel Saved");

				boolean error = captureSignature(); 
				if(!error){ 
					mView.setDrawingCacheEnabled(true);
					mSignature.save(mView, signName+"_A"); 
					//mViewA.setDrawingCacheEnabled(true);
					mSignatureA.save(mViewA, signName+"_B");
					//mSignature.save(mView); 
					//mSignatureA.save(mViewA);
					Bundle b = new Bundle();
					b.putString("status", "done"); 
					Intent intent = new Intent();
					intent.putExtras(b); 
					setResult(RESULT_OK,intent);    
					finish();
				} 
				
			} 
		}); 
  
		mCancel.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{ 
				Log.v("log_tag", "Panel Canceled");
				Bundle b = new Bundle();
				b.putString("status", "cancel"); 
				Intent intent = new Intent();
				intent.putExtras(b); 
				setResult(RESULT_CANCELED,intent);   
				finish(); 
			} 
		}); 
        
		/*
		Toast toast = Toast.makeText(this, getIntent().getStringExtra("file_dir") , Toast.LENGTH_SHORT); 
		toast.show(); 
		*/
        
	} 
	
	
	@Override
	public void onStart() {
		super.onStart();

	}

	
	@Override
	public void onPause() {
		super.onPause();

	}
	
	
	@Override
	public void onStop() {
		super.onStop();

	}
	
  
	@Override
	protected void onDestroy() { 
		Log.w("GetSignature", "onDestory");
		super.onDestroy(); 
	} 
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

//System.out.println("_______ onWindowFocusChanged");	
//System.out.println("_______ 1 :"+String.valueOf(mContent.getWidth()));	//720
//System.out.println("_______ 2 :"+String.valueOf(mContent.getHeight()));	//1081
//System.out.println("_______ 1 :"+mView.getWidth());
//System.out.println("_______ 2 :"+mView.getHeight());
//System.out.println("_______ 1 :"+mSignature.getWidth());
//System.out.println("_______ 2 :"+mSignature.getHeight());

	}
  
	private boolean captureSignature() { 
	
		boolean error = false; 
		String errorMessage = "";
		
		if(error){
			Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 105, 50);
			toast.show();
		}
	
	    return error;
	} 
  
	private boolean prepareDirectory()  
	{ 
		try {
			if (makedirs()) {
				return true; 
			} else {
				return false; 
			}
		} catch (Exception e) {
			e.printStackTrace(); 
			Toast.makeText(this, "Could not initiate File System.. Is Sdcard mounted properly?", Toast.LENGTH_LONG).show();
			return false; 
		}
	}

  
	private boolean makedirs()  
	{ 
		File tempdir = new File(tempDir);
		if (!tempdir.exists())
			tempdir.mkdirs();
		
		if (tempdir.isDirectory()) {
			File[] files = tempdir.listFiles();
			for (File file : files)
			{
				/*
				if (!file.delete())
				{
				System.out.println("Failed to delete " + file); 
				}
				*/
			}
		}
		return (tempdir.isDirectory());
	}

	public class signature extends View
	{ 
		private static final float STROKE_WIDTH = 5f; 
		private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2; 
		private Paint paint = new Paint();
		private Path path = new Path();

		private float lastTouchX; 
		private float lastTouchY; 
		private final RectF dirtyRect = new RectF();

		public signature(Context context, AttributeSet attrs)
		{ 
			super(context, attrs); 
			paint.setAntiAlias(true); 
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(STROKE_WIDTH); 
		} 

		@SuppressLint("WrongThread")
		public void save(View v, String filename)
		{ 
			Log.v("log_tag", "Width: " + v.getWidth());
			Log.v("log_tag", "Height: " + v.getHeight());
			if(mBitmap == null) { 
				mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
			} 
			Canvas canvas = new Canvas(mBitmap);

			/** 저장 경로 및 파일명**/
	
			// 이것이 파일명이 된다.         
			String fileNames = filename;
	
			current = fileNames + ".bmp";  //  recino.jpg

			tempDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LogisALL_SIGN/";
			System.out.println("::::::::::::::::::::::::::::::"+tempDir);
	
			// 경로 확인 및 생성 (디렉토리)
			prepareDirectory();
			File directory = new File(tempDir) ;
			mypath= new File( directory, current );
			//Toast.makeText(this, mypath.length(),Toast.LENGTH_SHORT);
			//Toast.makeText(this, mypath.length(), Toast.LENGTH_SHORT).show();
			//.LENGTH_LONG(mypath.length())
			/** 저장 경로 및 파일명**/
			String scnpath =  mypath.toString() ;
			System.out.println("scnpath : "+scnpath);
	
			try {
				FileOutputStream mFileOutStream = new FileOutputStream(mypath);
	
				v.draw(canvas);
	
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 30, mFileOutStream);
				mFileOutStream.flush();
				mFileOutStream.close();
				if ( mypath.length() < 1){
					//Toast toast = toast.makeText(this, "!!!", Toast.LENGTH_SHORT).show();
					//Toast toast = Toast.makeText(this,  tempDir, Toast.LENGTH_SHORT);
					//System.out.println("size!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!111!");	
				}
				 
				// 겔러리에 보이게 미디어 스캐닝
				//sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse( "file://"+ scnpath  )));
				//sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse( "file://"+ scnpathA  )));
			} catch(Exception e) {
				System.out.println("#################### e.toString() "  + e.toString());
				Log.v("log_tag", e.toString());
	
			}finally{
				extra.putInt("data", 1);
	
				//main 의 onActivityResult 로 넘길 데이터   >>>
				extra.putString( "savepath", scnpath );
				extra.putString( "SignEventXY", SignEventXY );
				extra.putString( "filename", current );
				intent.putExtras(extra);
				setResult(RESULT_OK, intent);
				finish();
				
			}
	
			//this.setResult(RESULT_OK, intent); // 성공했다는 결과값을 보내면서 데이터 꾸러미를 지고 있는 intent를 함께 전달한다.
		} 
  
		public void clear()  
		{
			path.reset();
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawPath(path, paint);
			//canvas.drawPath(path, paint);

/*			
			canvas.drawColor(Color.WHITE);
			
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			
			//피알원 상중하
			//String str = "-01-01035025038025041025048024054024059024064023069023073022076021076021-01-01052026052027051028051030051031051034050036050038050041051042051043051043052043052043-01-01070026070026071028071030072032072034072037071039069042067044067044-01-01056049056049055049056048057048060048063048066047069047072047075047080047083047086046086046-01-01096018096020095023095026095029095033095036095039096042096044097047097048098049098049098049-01-01130023129023128024127025127026126027126028127029128030129031130031132032135032136032139031140031142029142029141027140026138024137024136024136024136024-01-01163015162015162016161018160019160021159023159025160028160030161033161034161034-01-01165029166029167029169028171028174028178028182029184029187029187029-01-01152046153046154046157046160046164046166046168046169046170047170048167053166055163058163058163059165059170059174058178058180058183058183058-01-01211024210024210025211026212028213029215030216030218031219031220030221029221029221027220026219025218024217024214024214024-01-01195042197043201043206043211043216043223043233043233043-01-01227047226048225049224051223053223055223055-01-01225060-01-01239050239050240049241049242049243049245049249048254046254046-01-01268030267030266031266033265034264036263038262041260045259049257051254054252055250055249055248055248056248057248058248060249062252064257065260065262064264064266064268064268064";
			//String str = "-01-01030110030110031109034109037109043108049107054107060106066105072103073103073103-01-01046110047112047115047117047119048122049123050124052126054127055127055127-01-01064112065114064116063120062124060126058128055130054131051132050132049133049132050132052131059129062129066128073127078127082126082126-01-01097102096104096106096109097112098116099120099123100127101129102132102133102133102133-01-01133105133105131106130107129109128110127111129114130114132115133115134115136114136114137112138111137110137109136108135107135107134107134106134106134106-01-01163094163095163096163097162099162102162105163107164110165112167115168114169113169112170111170110171109172109175108177107179107182107184107187108187108-01-01149124151124153124157124160124163124165124166124167125167126166127165129160134160135159137160139163139166139173139176139181138185137185137-01-01213101213101212102212103212104212105212107213108215109215109216110218110219109220107220106218103217102212101209102209102-01-01202120206120210119215119220118226117231117234117239116239116-01-01226123225123225124224127224129223132223134224136224137227139227139-01-01237124239123240123242123244122246122249121254120258119258119-01-01270100269101269102267104267106267108267111267114267117266123266123-01-01251127251128250129248131248132247134246135246135247137249137252137260137266137276137280137283137287137289138289138";
			//String str = "-01-01028233030233032233036232040232045232049232058232061232066232066232-01-01045238045240044241043243042246042249042252042255042257042259043260044260044260-01-01060234060235060237060239060243059246056251053255050258048259045260044261044261044261045261048261051261055261062260065260070259074257077255077255-01-01092223092225092227092231092235092240092244093249094253096257096259097260098262099262099262-01-01131229130230130231129232129234130235131237133238134239136239139239139238140236139235137232136231133230132230130231130231-01-01168221167223167225168228168230168232168233168233-01-01170230171230172230174230177230181231182232182232-01-01142251143250146250151250155250159251162251165252166253166253166257164259163262163263163264167266170266176265180264184263186263189262189262-01-01215226215226215227215227215228216228217229218229220229221229224228224227224226224225220223215223210224210224-01-01202235204235207235211235216235221236225237229237233238233238-01-01219248218249218251218253218255218257219258220259221258223257225256227254228254231252234251241251243250247250253249256249261246262244265237266234268232268231268232268232266237266241265246265246-01-01243262242263242265242267243269245270248271252271259270264268271266276265283264286264289266289266";
			//String str = "-01-01033114038111045109053106060104067103073101079100081100081105080109078115076120073130072135070141069144070146072147072147-01-01109095109096109097110099110102109107109112108116108120107124108126108126-01-01116120118120121120124120127119130119133118136117137117139116139116-01-01096164095165094167093170093172094173095175096177099177102177106172108170109165109164109162107162102164099165099167100168100168-01-01154108154109153112153115152118151121151122151122-01-01156102157102158101160100163099165098167098169098171098172098174098177099179099181101182102183105184109183112181120181124178131175134168137162138160138158138157138158138161137164136171134177132181131185130188128188128-01-01206105206105207105207105207106207108206111206115204120202125202125-01-01178151177154177157175161174164173167172170172171172172173173178173181172190170197168200168206169209169210170210170-01-01224095225095227095230095232095235097237098239100241103242106243110243113243119242121242125242125242125-01-01249100250100252100255099257099260098263098266098268098268098-01-01258121258121259121260120263118267115267115-01-01282094282095282097281100281104280108280113280117280121280125280127280127-01-01245166244167244169244171244173245175248175251175255174259172262170266164266162262155258153251153247154243155243155";
			//스와치 상중하
			//String str = "-01-01064023057039048058051057055051055051-01-01069032078039086045091050092053092053-01-01056087066084085080096080102079104079104079-01-01135025133023128032130043137042142032135021126025126025-01-01133042133054134079135083136080151077161075166075166075-01-01178026177038175056177073178081179082179082-01-01190055193056196057199058199058-01-01217025220025221027208036207040224041231045221061214074215075215075-01-01233062240071241073241073-01-01260022262027264056264077267084267084";
			//String str = "-01-01069098064108057125054131051135051135051135-01-01074098075101082117089129093152093152-01-01053175065176090175108172113170114170114170-01-01129110129118131128134129139123141114133108124115124115-01-01135122134137131164130177139174148170157167161165165162165162-01-01172107174127178159180177181183181183-01-01185147188145196145202144202144-01-01218105219105215111209118212119217120220134216160218157220150232154241162243162243162-01-01267091266111266138269157271159271159";
			//String str = "-01-01061183060201051228050235051232051232-01-01064192073203085216091227093230093230-01-01049269064269092270102270112268112268-01-01136198134197131199127207130217140221149217147203130196121203121203-01-01139212136230135253142264152260159257168252172250178250178250-01-01184195181213182253189272190274190274-01-01197228202230214232214232-01-01221193212208208215219215227211227219220242218249224243233238245247250255250255-01-01274169271193267255278288278288";
			//스와치 다른방법
			//String str = "-01-01048138045146039162036174036175036175-01-01054133057139065154070163072169072169-01-01033199048196069192080191087190087190-01-01104142099149098162102166113159114150102145095165100185109194120192129186133184135182135182-01-01142136137156136173138190139196140196140196-01-01141167145166152166158165158165-01-01167137160152156159161158168154170153168161163173162177165174169171176175181185185187185187-01-01200135195161198201200210200210";
			
			String _posX = "";
			String _posY = "";
			
			int beforeX = -1;
			int beforeY = -1;
			while(str.length() >= 6)
			{
				try{
					_posX = str.substring(0,3);
					_posY = str.substring(3,6);
					
					int posX = -1;
					int posY = -1;
					
					posX = Integer.parseInt(_posX);
					posY = Integer.parseInt(_posY);
					
					if(beforeX > 0 && beforeY > 0 && posX > 0 && posY > 0)
					{
						canvas.drawLine(beforeX, beforeY, posX, posY, paint);
					}
					
					if(beforeX != posX && beforeY != posY)
					{
						beforeX = posX;
						beforeY = posY;
					}
				
				}catch(Exception e){
					// to do nothing
					System.out.println("ERROR : "+_posX+"   "+_posY);
				}
				
				str = str.substring(6);
			}
*/
		}
  
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{ 
			float eventX = event.getX(); 
			float eventY = event.getY(); 
			double w = 0.0;
			double h = 0.0;
			double ilyangX = 0.0;
			double ilyangY = 0.0;			
			
			mGetSign.setEnabled(true); 

			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:

				path.moveTo(eventX, eventY);
				lastTouchX = eventX;
				lastTouchY = eventY;
				SignEventXY += "-01-01";
				return true;

				case MotionEvent.ACTION_MOVE:

				case MotionEvent.ACTION_UP:

				resetDirtyRect(eventX, eventY);
				int historySize = event.getHistorySize();
				for (int i = 0; i < historySize; i++)
				{
					float historicalX = event.getHistoricalX(i); 
					float historicalY = event.getHistoricalY(i); 
					expandDirtyRect(historicalX, historicalY); 
					path.lineTo(historicalX, historicalY); 
				}
				
/*				
				//case 1 방법

				//일양 사인이 보여지는 부분의 해상도에 맞추기위해 계산을 해준다
				//사인부분 너비구하기
				w = (double)mSignature.getWidth();		//720
				h = (double)mSignature.getHeight();		//720
				
				//비율 구하기
				w = w/130.0;
				h = h/69.0;
				
				//
				ilyangX = (double)eventX/w;
				ilyangY = (double)eventY/h;
				
				//3자리씩 맞춰준다
				SignEventXY += String.format("%03d",(int)ilyangX) +""+ String.format("%03d",(int)ilyangY);
				//SignEventXY += Integer.toString((int)eventX) +""+ Integer.toString((int)eventX);

*/
				//case 2 방법

				//터치 스크린에서 얻어진 가로 좌표에 Scale_X를 곱해주고, 세로 좌표에 Scale_Y를 곱해주면 해상도가 어떻든 간에 309x300 좌표계를 사용할 수 있습니다.
				float Scale_X = 309f; // canvas.getWidth();
				float Scale_Y = 300f; // canvas.getHeight();

				SignEventXY += String.format("%03d",(int)(eventX/Scale_X*100)) +""+ String.format("%03d",(int)(eventY/Scale_Y*100));

				
				path.lineTo(eventX, eventY);
				break; 

				default: 
				debug("Ignored touch event: " + event.toString()); 
				return false; 
			} 

			invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH), 
			(int) (dirtyRect.top - HALF_STROKE_WIDTH), 
			(int) (dirtyRect.right + HALF_STROKE_WIDTH), 
			(int) (dirtyRect.bottom + HALF_STROKE_WIDTH)); 

			lastTouchX = eventX; 
			lastTouchY = eventY; 

			return true; 
		} 

		private void debug(String string){
		} 

		private void expandDirtyRect(float historicalX, float historicalY)  
		{ 
			if (historicalX < dirtyRect.left) { 
				dirtyRect.left = historicalX; 
			}  
			else if (historicalX > dirtyRect.right) { 
				dirtyRect.right = historicalX; 
			} 

			if (historicalY < dirtyRect.top) { 
				dirtyRect.top = historicalY; 
			}
			else if (historicalY > dirtyRect.bottom) { 
				dirtyRect.bottom = historicalY; 
			} 
		} 

		private void resetDirtyRect(float eventX, float eventY)  
		{ 
			dirtyRect.left = Math.min(lastTouchX, eventX);
			dirtyRect.right = Math.max(lastTouchX, eventX);
			dirtyRect.top = Math.min(lastTouchY, eventY);
			dirtyRect.bottom = Math.max(lastTouchY, eventY);
		} 

		private String getTodaysDate() {
			final Calendar c = Calendar.getInstance();
			int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
			((c.get(Calendar.MONTH) + 1) * 100) +
			(c.get(Calendar.DAY_OF_MONTH));

			//Log.d("DATE:",String.valueOf(todaysDate)); 
			return(String.valueOf(todaysDate));
		} 
      
		private String getCurrentTime() {

			final Calendar c = Calendar.getInstance();

			DecimalFormat Format 	= new DecimalFormat("00");
			String currentTime = Format.format( (int)(c.get(Calendar.HOUR_OF_DAY)) )
					  + Format.format( (int)(c.get(Calendar.MINUTE)) )
					  + Format.format( (int)(c.get(Calendar.SECOND)) );

			return(String.valueOf(currentTime));
			/*
			final Calendar c = Calendar.getInstance(); 
			int currentTime =     (c.get(Calendar.HOUR_OF_DAY) * 10000) +  
			(c.get(Calendar.MINUTE) * 100) +  
			(c.get(Calendar.SECOND)); 
			Log.d("TIME:",String.valueOf(currentTime)); 
			return(String.valueOf(currentTime)); 
			*/

		} 

		/**
		 * Bitmap이미지의 가로, 세로 사이즈를 리사이징 한다.
		 * 
		 * @param source 원본 Bitmap 객체
		 * @param maxResolution 제한 해상도
		 * @return 리사이즈된 이미지 Bitmap 객체
		 */
		public Bitmap resizeBitmapImage(Bitmap source, int maxResolution)
		{
			int width 		= source.getWidth();
			int height 		= source.getHeight();
			int newWidth 	= width;
			int newHeight 	= height;
			float rate 		= 0.0f;

			if(width > height) {
				if(maxResolution < width)
				{
					rate 		= maxResolution / (float) width;
					newHeight 	= (int) (height * rate);
					newWidth 	= maxResolution;
				}
			}
			else {
				if(maxResolution < height)
				{
					rate 		= maxResolution / (float) height;
					newWidth 	= (int) (width * rate);
					newHeight 	= maxResolution;
				}
			}

			return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
		}
	}
	
}