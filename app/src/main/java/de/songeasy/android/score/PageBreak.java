/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.score;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;

/**
 * Draw an indication of a page break
 * 
 * @author krein
 * 
 */
public class PageBreak extends ScoreElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/* Measure this page break is associated with */
	private Measure measure;

	/* Path to draw dashed line with */
	private Path path;

	/** Paint of to draw with */
	private static Paint paint = new Paint();


	// +++++++ getters and setters +++++++

	public Measure getMeasure() {
		return measure;
	}

	/* setup Paint for dotted/dashed line */
	static {
		PageBreak.paint.setAntiAlias(true);
		PageBreak.paint.setColor(Color.BLACK);
		PageBreak.paint.setStrokeWidth(2);
		PageBreak.paint.setStyle(Style.STROKE);
		PageBreak.paint.setPathEffect(new DashPathEffect(new float[] { 5, 5,
				20, 5 }, 0));
	}


	public PageBreak(Measure measure) {
		this.measure = measure;
		path = new Path();
	}


	@Override
	protected void setLayout(Point origin, int lineSpacing, int width,
			int height) {
		// in Java it is not possible to overload the assignment operator
		// therefore we need to copy the Point variables one by one
		getOrigin().x = origin.x;
		getOrigin().y = origin.y;

		this.height = height;
		this.width = width;

		path.reset();
		path.moveTo(getOrigin().x, getOrigin().y);
		path.lineTo(getOrigin().x + getWidth(), getOrigin().y);
		// path.close();

		this.staffLineSpacing = lineSpacing;
	}

	@Override
	protected void drawSelf(Canvas canvas) {
		canvas.drawPath(path, PageBreak.paint);
	}
}
