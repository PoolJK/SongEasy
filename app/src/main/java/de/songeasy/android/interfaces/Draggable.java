/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.interfaces;

import android.graphics.Canvas;
import android.graphics.Point;

/**
 * Interface for elements that should be dragged on a canvas.
 * 
 * @author krein
 * 
 */
public interface Draggable {

	/**
	 * Draw itself at position on canvas with specified alpha.
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 * @param position
	 *            Position to draw at
	 */
	void drawDrag(Canvas canvas, Point position);
}
