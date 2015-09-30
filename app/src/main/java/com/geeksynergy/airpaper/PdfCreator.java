package com.geeksynergy.airpaper;

import android.app.Activity;
import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PdfCreator {
    PdfDocument document;
    View content;

    public void createPdf(Activity activity, Context context) {
        document = new PdfDocument();
        View content = activity.findViewById(R.id.ll);
        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth(),
                content.getHeight() - 20, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        content.draw(page.getCanvas());
        document.finishPage(page);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmss");
        String pdfName = "PDF_" + sdf.format(Calendar.getInstance().getTime()) + ".pdf";
        File outFile = new File(Environment.getExternalStorageDirectory(), pdfName);
        try {
            outFile.createNewFile();
            OutputStream out = new FileOutputStream(outFile);
            document.writeTo(out);
            document.close();
            out.close();
            Toast.makeText(activity.getBaseContext(), "Document Saved as " + pdfName, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

//    try
//			{
//				Environment.getExternalStorageState();
//                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmss");
//                String pdfName = "pdfdemo" + sdf.format(Calendar.getInstance().getTime()) + ".pdf";
//                File root = new File(Environment.getExternalStorageDirectory(), "WeatherForecast");
//                if (!root.exists()) {
//                    root.mkdirs();
//                }
////        File outFile = new File(Environment.getExternalStorageDirectory(), pdfName);
//
//				File outFile = new File(root, pdfName);
//				FileWriter writer = new FileWriter(outFile);
//				writer.write(document.toString());
//				writer.flush();
//				writer.close();
//				//Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
//			}
//			catch(IOException e)
//			{
//				e.printStackTrace();
//			}


    }

}
