/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.score;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Base class for all graphical elements of score. We don't added a Paint()
 * object here because we want paint to be static in all subclasses (only one
 * paint for all class objects).
 * 
 * @author krein
 * 
 */
public abstract class ScoreElement {

	/** extension value for selection frame in pixels for touching */
	protected final static int SELECTIONEXTENSION = 5;

	/** alpha value of rectangle while dragging **/
	protected final static int DRAG_RECT_ALPHA = 25;

	/** alpha value of element while dragging **/
	protected final static int DRAG_ELEMENT_ALPHA = 128;

	/** layout parameters */
	// TODO >>krein<< better use Rect directly?

	/** the origin of this element, it's upper left corner */
	private Point origin;

	/** width of this element, dimension in x-direction */
	// TODO: make private
	protected int width;

	/** height of this element, dimension in y-direction */
	// TODO: make private
	protected int height;

	/** flag if element is selected */
	protected boolean selected;

	/** space between two staff lines, used as a common placement grid */
	protected int staffLineSpacing;

	/** area of element, needed for isIn() */
	protected RectF room;

	/** if element draws itself */
	protected boolean visible;


	// +++++++ getters and setters +++++++

	/**
	 * Get origin of ScoreElement
	 * 
	 * @return 2 dimensional coordinate of origin
	 */
	public Point getOrigin() {
		return origin;
	}


	/**
	 * Set origin of ScoreElement, move element to position
	 * 
	 * @param origin
	 *            new origin of element as Point
	 */
	public void setOrigin(Point origin) {
		this.origin.x = origin.x;
		this.origin.y = origin.y;
	}


	/**
	 * Set origin of ScoreElement, move element to position
	 * 
	 * @param x
	 *            new x position of element in pixel
	 * @param y
	 *            new y position of element in pixel
	 */
	public void setOrigin(int x, int y) {
		this.origin.x = x;
		this.origin.y = y;
	}


	/**
	 * Get width of element
	 * 
	 * @return width in pixels
	 */
	public int getWidth() {
		return width;
	}


	/**
	 * Set width of element
	 * 
	 * @param width
	 *            in pixel
	 */
	public void setWidth(int width) {
		this.width = width;
	}


	/**
	 * Get height of element
	 * 
	 * @return height in pixel
	 */
	public int getHeight() {
		return height;
	}


	/**
	 * Set height of element
	 * 
	 * @param height
	 *            height in pixel
	 */
	public void setHeight(int height) {
		this.height = height;
	}


	/**
	 * Get selected state of element
	 * 
	 * @return true if element is selected
	 */
	public boolean isSelected() {

		return selected;
	}


	/**
	 * Set selection state of element
	 * 
	 * @param selected
	 *            true to set element as selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}


	/**
	 * Get visible state of element
	 * 
	 * @return true if element is visible
	 */
	public boolean isVisible() {
		return visible;
	}


	/**
	 * Set visible state of element
	 * 
	 * @param visible
	 *            true to show element
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	/**
	 * Constructor of ScoreElement
	 * 
	 * @param context
	 *            the app context, needed for access to assets
	 */
	public ScoreElement() {
		// Initialise variables
		origin = new Point();
		room = new RectF();
		width = 0;
		height = 0;
		selected = false;
		visible = true;
		staffLineSpacing = PageProperties.STAFF_MINIMUM_LINESPACING;
	}


	/**
	 * Set position and size of element.
	 * 
	 * @param origin
	 *            start position, upper left corner of element
	 * @param lineSpacing
	 *            space between staff lines in pixels
	 * @param width
	 *            width of element
	 * @param height
	 *            height of element
	 */
	protected void setLayout(
			Point origin,
			int lineSpacing,
			int width,
			int height) {
		// in Java it is not possible to overload the assignment operator
		// therefore we need to copy the Point variables one by one
		this.origin.x = origin.x;
		this.origin.y = origin.y;

		this.height = height;
		this.width = width;

		this.staffLineSpacing = lineSpacing;
	}


	/**
	 * Set position of element only.
	 * 
	 * Override this, if you want to compute size of element yourself.
	 * 
	 * @param origin
	 *            origin of this element
	 * @param lineSpacing
	 *            space between staff lines in pixels
	 */
	protected void setLayout(Point origin, int lineSpacing) {

		setOrigin(origin);
		this.staffLineSpacing = lineSpacing;
	}


	/**
	 * Draw self on canvas.
	 * 
	 * Needs to be overwritten by all child classes.
	 * 
	 * @param canvas
	 *            canvas to draw on
	 * @paint drawing style information
	 */
	protected abstract void drawSelf(Canvas canvas);


	// public abstract void accept(DistributionVisitor av);

	/**
	 * Test if coordinate is inside element's room
	 * 
	 * @param point
	 *            coordinate to test
	 * @return Return true if coordinate is inside, false otherwise
	 */
	protected boolean isIn(Point p) {

		// define area around element
		Rect area = new Rect(origin.x - SELECTIONEXTENSION, origin.y
				- SELECTIONEXTENSION, origin.x + width + SELECTIONEXTENSION,
				origin.y + height + SELECTIONEXTENSION);

		return area.contains(p.x, p.y);
	}


	/**
	 * Draw a rounded rectangle around element. Highlight element by drawing a
	 * coloured rect around it.
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 */
	protected void drawSelectionRect(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(5.0f);
		paint.setColor(PageProperties.SELECT_COLOR);
		paint.setAntiAlias(true);
		paint.setAlpha(PageProperties.SELECT_RECT_ALPHA);
		RectF area = new RectF((float) origin.x, (float) origin.y,
				(float) (origin.x + width), (float) (origin.y + height));
		canvas.drawRoundRect(
				area, PageProperties.SELECT_RECT_ROUND_RADIUS,
				PageProperties.SELECT_RECT_ROUND_RADIUS, paint);
	}


	/**
	 * Draw a rectangle around element. Used for debugging only!
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 */
	protected void drawDebugRect(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		Rect area = new Rect(origin.x, origin.y, origin.x + width, origin.y
				+ height);
		canvas.drawRect(area, paint);
	}

}
