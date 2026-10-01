/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import de.songeasy.android.interfaces.Draggable;
import de.songeasy.android.score.Note;
import de.songeasy.android.score.PageProperties;
import de.songeasy.android.score.ScoreElement;

/**
 * Support for a ScoreElement to be dragged on screen as bitmap. Image can be
 * positioned and draws itself on canvas.
 * 
 * @author krein
 * 
 */
public class DragImage {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** alpha value of rectangle while dragging **/
	private final static int RECT_ALPHA = 25;

	/** radius of selection rectangle corners */
	private static final float RECT_ROUND_RADIUS = 5.0f;

	/** additional margin for highlighting rectangle */
	private static final int RECT_EXTENSION = 6;

	/** color for drag rectangle */
	private static final int RECT_COLOR = Color.GRAY;

	/** bitmap to move on screen */
	private Bitmap bitmap;
	/** canvas to draw element on */

	/** origin of dragged element Bitmap */
	private Point currentPosition;

	/** position of element before dragging */
	private Point originalPosition;

	/** distance between origin of dragged element (bitmap) and touch position **/
	private Point touchOffset;

	private Canvas canvas;

	/** paint to draw rectangle with */
	private Paint paint;


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

	public int getWidth() {
		return bitmap.getWidth();
	}


	public int getHeight() {
		return bitmap.getHeight();
	}


	public Point getCurrentPosition() {
		return currentPosition;
	}


	/**
	 * Constructor with ScoreElement to be draged as parameter. Creates a bitmap
	 * of ScoreElement which then can be dragged on a canvas.
	 * 
	 * @param element
	 *            ScoreElement to be dragged as bitmap
	 * @param touchPosition
	 *            position of touch where dragging starts
	 */
	public DragImage(ScoreElement element, Point touchPosition)
			throws IllegalArgumentException {

		if (element == null || touchPosition == null) {
			throw new IllegalArgumentException();
		}

		if ((element instanceof Draggable) != true) {
			throw new IllegalArgumentException();
		}

		// create new bitmap with size of element plus extension for
		// highlighting rectangle
		bitmap = Bitmap.createBitmap(
				element.getWidth() + 2 * RECT_EXTENSION, element.getHeight()
						+ 2 * RECT_EXTENSION, Config.RGB_565);

		// create new canvas for bitmap
		canvas = new Canvas(bitmap);

		// create paint for drawing of rectangle
		paint = new Paint();

		canvas.drawColor(Color.WHITE);

		// let element draw itself on bitmap
		if (element instanceof Draggable)
			((Draggable) element).drawDrag(canvas, new Point(RECT_EXTENSION,
					RECT_EXTENSION));

		// add rectangle around image
		drawDragRect();

		// compute distance between touched point and origin of bitmap
		if (element.getClass() == Note.class) {
			touchOffset = new Point(touchPosition.x - element.getOrigin().x
					+ Note.NOTE_CROSS_SIZE / 2, touchPosition.y
					- element.getOrigin().y - Note.NOTE_CROSS_SIZE / 2);
		} else
			touchOffset = new Point(touchPosition.x - element.getOrigin().x
					+ PageProperties.getDrawselectionextension(),
					touchPosition.y - element.getOrigin().y
							- PageProperties.getDrawselectionextension());
		currentPosition = new Point(touchPosition.x - touchOffset.x,
				touchPosition.y - touchOffset.y);

		originalPosition = new Point(element.getOrigin());
	}


	/**
	 * Draw highlighting rectangle around dragged element.
	 */
	private void drawDragRect() {
		paint.setStyle(Style.FILL);
		paint.setColor(RECT_COLOR);
		paint.setAntiAlias(true);
		paint.setAlpha(RECT_ALPHA);
		RectF area = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		canvas.drawRoundRect(area, RECT_ROUND_RADIUS, RECT_ROUND_RADIUS, paint);
	}


	/**
	 * Draw bitmap on canvas at current position.
	 * 
	 * @param canvas
	 */
	public void drawSelf(Canvas canvas) {

		if (bitmap != null)
			canvas.drawBitmap(
					bitmap, currentPosition.x, currentPosition.y, null);
	}


	/**
	 * Move image on screen with new touch position. Position must be corrected
	 * by current scroll position of view.
	 * 
	 * @param touchPosition
	 */
	public void move(Point touchPosition) {

		currentPosition.x = touchPosition.x - touchOffset.x;
		currentPosition.y = touchPosition.y - touchOffset.y;
	}

}
