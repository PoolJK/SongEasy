/*
 * Copyright (C) 2016 Joerg Krein
 */
package de.songeasy.android.fileactivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import de.songeasy.android.score.PageProperties;
import de.songeasy.android.score.Song;

/**
 * Exporting bitmap in different graphic formats. To export a drawing use
 * getCanvas() and draw on this canvas. Call export() with desired file format
 * to create graphic file.
 *
 * @author Joerg Krein
 */
public class FileExport {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    public enum PaperType {
        A4, LETTER
    }


    /**
     * see {@link http://de.wikipedia.org/wiki/Papierformat} for music paper
     * formats!
     */
    /**
     * Parameters for DIN A4 format
     */
    private static final int PAPER_A4_WIDTH = 210 * 4;
    private static final int PAPER_A4_HEIGHT = 297 * 4;
    private static final int PAPER_A4_HORIZONTAL_MARGIN = 10;
    private static final int PAPER_A4_VERTICAL_MARGIN = 10;
    /**
     * Parameters for letter format
     */
    private static final int PAPER_LETTER_WIDTH = 216 * 4;
    private static final int PAPER_LETTER_HEIGHT = 279 * 4;
    private static final int PAPER_LETTER_HORIZONTAL_MARGIN = 12;
    private static final int PAPER_LETTER_VERTICAL_MARGIN = 10;

    private Bitmap bitmap;
    private Canvas canvas;
    private String fileName;
    private int pageWidth;
    private int pageHeight;
    private Point origin;
    private PaperType paperType;
    private FileType fileType;
    private CreatePDF createPDF = null;
    private Song song;
    private int pageCount;


    // +++++++ getters and setters +++++++
    // +++++++++++++++++++++++++++++++++++

    /**
     * Get canvas to draw on for export.
     *
     * @return canvas to draw on
     */
    public Object getCanvas() {
        return canvas;
    }


    /**
     * Get width of image to draw.
     * Image is smaller than page to allow for free margins.
     *
     * @return width of image
     */
    public int getBitmapWidth() {
        int retWidth = this.pageWidth;

        switch (paperType) {
            case A4:
            default:
                retWidth -= 2 * PAPER_A4_HORIZONTAL_MARGIN;
                break;

            case LETTER:
                retWidth -= 2 * PAPER_LETTER_HORIZONTAL_MARGIN;
                break;
        }

        return retWidth;
    }


    /**
     * Get height of bitmap draw
     * Image is smaller than page to allow for free margins.
     *
     * @return height of image
     */
    public int getBitmapHeigth() {

        int retHeight = this.pageHeight;

        switch (paperType) {
            case A4:
            default:
                retHeight -= 2 * PAPER_A4_VERTICAL_MARGIN;
                break;

            case LETTER:
                retHeight -= 2 * PAPER_LETTER_VERTICAL_MARGIN;
                break;
        }

        return retHeight;
    }


    /**
     * Get origin of image to draw
     *
     * @return origin of image
     */
    public Point getOrigin() {
        return this.origin;
    }


    /**
     * Constructor for file exporter.
     *  @param file      Name of exported file, extension will be added on export
     * @param type      type of file to export
     * @param paperType type of paper for export, currently only A4 and Letter supported
     * @param pages     number of pages
     */
    public FileExport(File file, FileType type, PaperType paperType, Song songToExport, int pages) {

        this.fileName = file.getAbsolutePath();
        this.fileType = type;
        this.song = songToExport;
        this.pageCount = pages;
        this.paperType = paperType;

        // strip extension from name
        if (fileName.contains("."))
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        switch (paperType) {
            case A4:
            default:
                this.pageWidth = PAPER_A4_WIDTH;
                this.pageHeight = PAPER_A4_HEIGHT;
                this.origin = new Point(PAPER_A4_HORIZONTAL_MARGIN,
                        PAPER_A4_VERTICAL_MARGIN);
                break;

            case LETTER:
                this.pageWidth = PAPER_LETTER_WIDTH;
                this.pageHeight = PAPER_LETTER_HEIGHT;
                this.origin = new Point(PAPER_LETTER_HORIZONTAL_MARGIN,
                        PAPER_LETTER_VERTICAL_MARGIN);
                break;
        }

        // create color bitmap with 8 bits resolution to draw on
        bitmap = Bitmap.createBitmap(pageWidth, pageHeight + 50, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        if(fileType == FileType.PDF)
            try {
                createPDF = new CreatePDF( new File(fileName + fileType.getExtension()));
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
    }


    /**
     * Exports bitmap to given file format.
     * @param page number of page to export, starting with 0
     */
    public void write(int page) {

        // clear bitmap with white background
        bitmap.eraseColor(Color.WHITE);

        PageProperties.setZoomFactor(1.0f);
        // layout song for paper format
        PageProperties.calculateStaffLineSpacing(getBitmapWidth());

        song.setLayout(getOrigin(), PageProperties.getStaffLineSpacing(),
                getBitmapWidth(), getBitmapHeigth(), page);

        // draw page into bitmap
        song.drawSelf(this.getCanvas(), page);

        switch (fileType) {

            case PNG:
                // one file for each page
                // save bitmap to png file
                FileOutputStream fos = null;

                // create string with page number for file name
                String pageNum = "";
                if (pageCount > 1)
                    pageNum = "-P" + String.valueOf(page + 1);

                try {
                    fos = new FileOutputStream(new File(fileName + pageNum + fileType.getExtension()));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                    fos.flush();
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case PDF:
                try {
                    // Generate PDF page
                    createPDF.addPage(bitmap);

                    if (page + 1 == pageCount)
                        createPDF.closePDF();
                    break;

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    break;
                }

            default:
                break;
        }

    }

} // class
