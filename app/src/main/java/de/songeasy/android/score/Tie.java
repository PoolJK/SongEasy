/*
 * Copyright (C) 2015 Joerg Krein
 */
package de.songeasy.android.score;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * A tie, connection two notes with a curved line.
 * Indicating that these notes are played as one.
 */
public class Tie extends ScoreElement {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    private static float TIE_LINE_WIDTH_TO_STAFF_LINESPACING_RATIO = 0.16f;

    /** paint style for all Note objects */
    private static Paint paint;

    /** note where tie starts from */
    private Note noteStart;
    /** note where tie ends at */
    private Note noteStop;
    /** end of staff, neede for open tie with no end note */
    private int xStaffEnd;


    static {

        Tie.paint = new Paint();
        Tie.paint.setAntiAlias(true);
        Tie.paint.setTextSize(30);
        Tie.paint.setColor(Color.BLACK);
        Tie.paint.setStyle(Paint.Style.STROKE);
    }


    // ++++++++++++++++ setters, getters ++++++++++++++++++++
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /** Get note where tie ends at */
    public Note getNoteStop() {
        return noteStop;
    }

    /** set end of staff, needed for tie with no endign note at end of measure */
    public void setxStaffEnd(int xStaffEnd) {
        this.xStaffEnd = xStaffEnd;
    }

    /** Constructor of Tie class
     * @param noteStart
     *      note where tie starts at
     * @param noteStop
     *      note where tie stops at
     */
    public Tie(Note noteStart, Note noteStop ) {
        this.noteStart = noteStart;
        this.noteStop = noteStop;
    }


    @Override
    protected void drawSelf(Canvas canvas) {
        // if no second note, draw open tie
        if(noteStop != null)
            drawTie(canvas);
        else
            drawTieStart(canvas);
    }

    @Override
    protected void setLayout(Point origin, int lineSpacing) {
        super.setLayout(origin, lineSpacing);
        Tie.paint.setStrokeWidth(lineSpacing * TIE_LINE_WIDTH_TO_STAFF_LINESPACING_RATIO);
    }

    /**
     * Draws begin of tie for note. As ending note is on next line, we draw just
     * the start indication of a tie.
     *
     * @param canvas
     *             Canvas to draw on
     */
    private void drawTieStart(Canvas canvas) {

        Path tiePath = new Path();
        Point tieStart = new Point(noteStart.getOrigin());
        Point tieStop = new Point(xStaffEnd, noteStart.getOrigin().y);

        // let tie start right from note head
        tieStart.x += noteStart.getWidth() / 2;

        // draw below or above notes, depending on direction of stem
        switch (noteStart.getStemDirection())
        {
            case UPWARDS:
                tieStart.y += noteStart.getHeight() / 2;
                tieStop.y += Math.round(1.8f * staffLineSpacing);
                break;

            case DOWNWARDS:
                tieStart.y -= noteStart.getHeight() / 2;
                tieStop.y -= Math.round(1.8f * staffLineSpacing);
                break;

            default:
                break;
        }

        tiePath.reset();
        tiePath.moveTo(tieStart.x, tieStart.y);
        tiePath.cubicTo(tieStart.x, tieStart.y, ((tieStop.x - tieStart.x) / 4)
                        + tieStart.x, ((tieStop.y - tieStart.y) / 4) * 3 + tieStart.y,
                tieStop.x, tieStop.y);

        canvas.drawPath(tiePath, Tie.paint);
    }



    /**
     * Draws tie between two notes.
     *
     * @param canvas
     *          Canvas to draw on
     */
    // TODO: Use array of lines to create a thicker line with small ends like
    // here: http://corner.squareup.com/2012/07/smoother-signatures.html
    private void drawTie(Canvas canvas) {

        Path tiePath = new Path();
        Point tieStart = new Point(noteStart.getOrigin());
        Point tieStop = new Point(noteStop.getOrigin());

        tieStart.x += noteStart.getWidth() / 2;
        tieStop.x -= noteStop.getWidth() / 2;

        int tieHeight = Math.round(2.2f * staffLineSpacing);

        // draw below or above notes, depending on direction of stem
        switch (noteStart.getStemDirection())
        {
            case UPWARDS:
                tieStart.y += noteStart.getHeight() / 2;
                tieStop.y += noteStop.getHeight() / 2;
                break;

            case DOWNWARDS:
                tieStart.y -= noteStart.getHeight() / 2;
                tieStop.y -= noteStop.getHeight() / 2;
                tieHeight = -tieHeight;
                break;

            default:
                break;
        }

        tiePath.reset();
        tiePath.moveTo(tieStart.x, tieStart.y);
        tiePath.cubicTo(tieStart.x, tieStart.y, (tieStart.x + tieStop.x) / 2,
                tieStart.y + tieHeight, tieStop.x, tieStop.y);

        canvas.drawPath(tiePath, Tie.paint);
    }
}
