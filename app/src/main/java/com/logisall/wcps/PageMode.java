package com.logisall.wcps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.woosim.printer.WoosimBarcode;
import com.woosim.printer.WoosimCmd;
import com.woosim.printer.WoosimImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PageMode extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_mode);
    }

    /*
     * On click methods
     */
    public void printLabel(View v) throws IOException {
        MainActivity.mPrintService.write(WoosimCmd.initPrinter());
        sendImg(0, 0, R.drawable.logo);
        MainActivity.mPrintService.write(WoosimCmd.printData());

        String str1 = "SHIP TO:\n";
        String str2 = "        #501, Daerung Technotown 3rd\n        448, Gasan-dong Gumcheon-gu\n        Seoul, Rep. of Korea\n";
        String str3 = "http://www.woosim.com/";
        String str4 = "ITEM    : Printer";
        String str5 = "Quantity: 10";
        String str6 = "TRACKING NUMBER:";
        String str7 = "134 35490 7564";

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
        byteStream.write(WoosimCmd.setCodeTable(WoosimCmd.MCU_RX, WoosimCmd.CT_CP437, WoosimCmd.FONT_LARGE));
        byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));
        byteStream.write(str1.getBytes());
        byteStream.write(WoosimCmd.setCodeTable(WoosimCmd.MCU_RX, WoosimCmd.CT_CP437, WoosimCmd.FONT_MEDIUM));
        byteStream.write(WoosimCmd.setTextStyle(false, false, false, 1, 1));
        byteStream.write(str2.getBytes());
        byteStream.write(WoosimCmd.setPageMode());
        byteStream.write(WoosimCmd.PM_setArea(0, 0, 384, 300));
        byteStream.write(WoosimImage.drawBox(2, 1, 370, 0, 4));
        byteStream.write(WoosimCmd.PM_setPosition(0, 7));
        byteStream.write(WoosimBarcode.create2DBarcodeQRCode(0, (byte)0x4D, 3, str3.getBytes()));
        byteStream.write(WoosimCmd.setCodeTable(WoosimCmd.MCU_RX, WoosimCmd.CT_CP437, WoosimCmd.FONT_LARGE));
        byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));
        byteStream.write(WoosimCmd.PM_setPosition(100, 20));
        byteStream.write(str4.getBytes());
        byteStream.write(WoosimCmd.PM_setPosition(100, 55));
        byteStream.write(str5.getBytes());
        byteStream.write(WoosimImage.drawBox(2, 90, 370, 0, 4));
        byteStream.write(WoosimCmd.setTextStyle(false, false, false, 1, 1));
        byteStream.write(WoosimCmd.PM_setPosition(0, 100));
        byteStream.write(str6.getBytes());
        byteStream.write(WoosimCmd.setCodeTable(WoosimCmd.MCU_RX, WoosimCmd.CT_CP437, WoosimCmd.FONT_MEDIUM));
        byteStream.write(WoosimCmd.setTextStyle(false, false, false, 1, 1));
        byteStream.write(WoosimCmd.PM_setPosition(130, 130));
        byteStream.write(str7.getBytes());
        byteStream.write(WoosimCmd.PM_setPosition(20, 160));
        byteStream.write(WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 100, false, str7.getBytes()));
        byteStream.write(WoosimCmd.PM_printStdMode());
        byteStream.write(WoosimCmd.feedToMark());

        MainActivity.mPrintService.write(byteStream.toByteArray());
    }

    public void printImageText(View v) {
        MainActivity.mPrintService.write(WoosimCmd.initPrinter());
        MainActivity.mPrintService.write(WoosimCmd.setPageMode());

        sendImg(0, 0, R.drawable.logo);
        sendImg(280, 0, R.drawable.android);

        MainActivity.mPrintService.write(WoosimCmd.PM_setArea(0, 0, 384, 150));
        String str = "Hello, Woosim!";
        MainActivity.mPrintService.write(WoosimCmd.PM_setPosition(70, 75));
        MainActivity.mPrintService.write(WoosimCmd.setCodeTable(WoosimCmd.MCU_RX, WoosimCmd.CT_CP437, WoosimCmd.FONT_LARGE));
        MainActivity.mPrintService.write(str.getBytes());

        MainActivity.mPrintService.write(WoosimCmd.PM_printStdMode());
    }

    private void sendImg(int x, int y, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), id, options);
        if (bmp == null) return;

        byte[] data = WoosimImage.drawBitmap(x, y, bmp);
        bmp.recycle();

        MainActivity.mPrintService.write(data);
    }
}
