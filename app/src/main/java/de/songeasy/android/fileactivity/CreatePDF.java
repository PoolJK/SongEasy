package de.songeasy.android.fileactivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import com.pdfjet.*;
import de.songeasy.android.ScoreView;
import de.songeasy.android.score.Song;

import java.io.*;

/**
 * Create PDF file from bitmaps
 *
 * 1. bitmapToByteArrayInputStream: for drawing an image into PDF an inputstream is needed,
 * while compressing your bitmap into PNG you can save the outputstream as ByteArray
 * which can be transferred in an inputstream
 *
 * 2. generatePDF: draw your view(inputstream) as an image on one page and generate a PDF-File
 */

public class CreatePDF {

    private final String TAG = this.getClass().getSimpleName();

    private PDF pdf;

    /**
     * Constructor for class.
     * @param file File with name to save to
     * @throws Exception
     */
    public CreatePDF(File file) throws Exception {
        // open file stream
        pdf = new PDF(new BufferedOutputStream(new FileOutputStream(file)));
      }

    /**
     * Create ByteArrayInputStream from Bitmap
     * @param bm Bitmap to convert
     * @return created ByteArrayInputStream
     * @throws IOException
     */
    private ByteArrayInputStream bitmapToByteArrayInputStream(Bitmap bm) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // fill output strean with png
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // fill byte array with output stream
        byte[] byteArray = baos.toByteArray();
        baos.close();
        // return with input stream
        return new ByteArrayInputStream(byteArray);
    }

    /**
     * Add new page to PDF file.
     * @param bm bitmap to fill new page with
     * @throws Exception
     */
    public void addPage(Bitmap bm) throws Exception {

        float[] dimensions = new float[2];
         // Set the width and height for pdf page
        dimensions[0] = (float) bm.getWidth();
        dimensions[1] = (float) bm.getHeight();

        // Draw view as image on page
        ByteArrayInputStream bais = bitmapToByteArrayInputStream(bm);
        Page page = new Page(pdf, dimensions);
        Image image = new Image(pdf, bais, ImageType.PNG);
        image.setPosition(0, 0);
        image.drawOn(page);
        bais.close();
    }

    public void closePDF() throws Exception {
            pdf.close();
    }
}
