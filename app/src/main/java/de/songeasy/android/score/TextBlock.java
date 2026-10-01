package de.songeasy.android.score;

import org.simpleframework.xml.Element;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;
import de.songeasy.android.interfaces.Draggable;

/**
 * Block with additional text to be placed under score. Consists of title and
 * multiline text.
 * 
 * @author jorgkrein
 * 
 */
public class TextBlock extends ScoreElement implements Draggable {

	// TODO: use android.text.StaticLayout for placing text?

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private static final int DEFAULT_TEXT_SIZE = 30;

	/** ratio of staff line spacing and lyrics font */
	private static final float TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR = 2.5f;

	/** ratio of staff line spacing and title and composer text distance */
	private static final float TEXT_DISTANCE_TO_STAFF_LINE_SPACING_FACTOR = 2.0f;

	/** ratio between title text size and composer name text size */
	private static final float TITLE_TO_CONTENT_TEXT_SIZE_RATIO = 1.3f;

	/** paint to use for drawing text */
	private static TextPaint textPaint;

	/** size of text block title */
	private static float titleTextSize;

	/** size of text block content */
	private static float contentTextSize;

	/** title of text block, e.g. 'Verse 2' */
	@Element(name = "title")
	private String title = null;

	/** text of text block, e.g. lyric for Verse 2 */
	@Element(name = "content")
	private String content = null;

	private Point titlePosition;

	private Point contentPosition;


	// +++++++ getters and setters +++++++
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}

	/** static initialization of text blocks's paint */
	static {

		TextBlock.textPaint = new TextPaint();
		TextBlock.textPaint.setAntiAlias(true);
		TextBlock.textPaint.setColor(Color.BLACK);
		TextBlock.textPaint.setTypeface(Typeface.DEFAULT);
		TextBlock.titleTextSize = DEFAULT_TEXT_SIZE;
		TextBlock.contentTextSize = DEFAULT_TEXT_SIZE
				/ TITLE_TO_CONTENT_TEXT_SIZE_RATIO;
	}


	public TextBlock(@Element(name = "title") String title, @Element(
			name = "content") String content) {
		this.title = title;
		this.content = content;
		titlePosition = new Point();
		contentPosition = new Point();
	}


	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		// lines of content text
		int contentLinesCounter = 0;
		// longest content string
		String maxContentString = new String();
		// length of title in pixel
		float titleLength = 0;
		// length of content in pixel
		float contentLength = 0;

		// adjust text sizes according to linespacing
		titleTextSize = lineSpacing * TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR;

		contentTextSize = titleTextSize / TITLE_TO_CONTENT_TEXT_SIZE_RATIO;

		// set position of strings
		// put title in left upper corner
		titlePosition = new Point(origin.x, origin.y);

		if (title != null) {
			// add height of title string to start position
			TextBlock.textPaint.setTextSize(titleTextSize);
			Rect bounds = new Rect();
			TextBlock.textPaint.getTextBounds(title, 0, title.length(), bounds);
			titlePosition.y += bounds.height();
			// (int) (-textPaint.ascent() + textPaint.descent());

			TextBlock.textPaint.setTextSize(titleTextSize);
			titleLength = TextBlock.textPaint.measureText(title);
		}

		// set height to height of title
		height = titlePosition.y - getOrigin().y;
		// add distance to content
		height += (int) (lineSpacing * TEXT_DISTANCE_TO_STAFF_LINE_SPACING_FACTOR);

		// put text content below
		contentPosition = new Point(titlePosition);
		contentPosition.y += (int) (lineSpacing * TEXT_DISTANCE_TO_STAFF_LINE_SPACING_FACTOR);

		if (content != null) {
			// add height of content block
			TextBlock.textPaint.setTextSize(contentTextSize);
			contentLinesCounter = content.split("\n").length;
			height += (contentLinesCounter - 1)
					* (int) (-textPaint.ascent() + textPaint.descent());

			// compute width of block by longest string
			// get line of content text with longest string
			for (String line : content.split("\n")) {

				if (line.length() > maxContentString.length())
					maxContentString = line;
			}

			TextBlock.textPaint.setTextSize(contentTextSize);
			contentLength = TextBlock.textPaint.measureText(maxContentString);
		}

		// set width to longest length, title or content
		width = (int) ((titleLength > contentLength) ? titleLength
				: contentLength);
	}


	@Override
	protected void drawSelf(Canvas canvas) {

		if (visible != true)
			return;

		// position of current line of content
		Point linePosition = new Point(contentPosition);

		// draw heading if exists
		if (title != null) {

			TextBlock.textPaint.setTextSize(titleTextSize);
			TextBlock.textPaint.setTypeface(Typeface.DEFAULT_BOLD);

			canvas.drawText(
					title, titlePosition.x, titlePosition.y,
					TextBlock.textPaint);
		}

		// draw text if exists
		if (content != null) {

			TextBlock.textPaint.setTextSize(contentTextSize);
			TextBlock.textPaint.setTypeface(Typeface.DEFAULT);

			// draw all lines of content text
			for (String line : content.split("\n")) {
				canvas.drawText(
						line, linePosition.x, linePosition.y,
						TextBlock.textPaint);

				// or better getFontMetrics().leading ?
				linePosition.y += -textPaint.ascent() + textPaint.descent();
			}
		}

		if (selected == true)
			drawSelectionRect(canvas);

		// drawDebugRect(canvas);
	}


	@Override
	public void drawDrag(Canvas canvas, Point position) {

		int xVectorDiff = position.x - getOrigin().x;
		int yVectorDiff = position.y - getOrigin().y;

		// move positions by vector difference
		titlePosition.offset(xVectorDiff, yVectorDiff);
		contentPosition.offset(xVectorDiff, yVectorDiff);

		drawSelf(canvas);

		// restore original positions
		titlePosition.offset(-xVectorDiff, -yVectorDiff);
		contentPosition.offset(-xVectorDiff, -yVectorDiff);
	}


	/**
	 * Return true if the specified ScoreElement overlaps the area of this one.
	 * Must overlap height and width by more than a third.
	 * 
	 * @param element
	 *            ScoreElement checked for overlapping
	 * @param position
	 *            current position of element
	 * @return true if element overlaps this one
	 */
	public boolean overlaps(ScoreElement element, Point position) {

		Rect rectA = new Rect(getOrigin().x, getOrigin().y, getOrigin().x
				+ width, getOrigin().y + height);
		Rect rectB = new Rect(position.x, position.y, position.x
				+ element.getWidth(), position.y + element.getHeight());

		if (rectA.left + rectA.width() / 3 < rectB.right
				&& rectB.left < rectA.right - rectA.width() / 3
				&& rectA.top + rectA.height() / 3 < rectB.bottom
				&& rectB.top < rectA.bottom - rectA.height() / 3) {
			return true;
		}

		else {
			Log.d(TAG, "no overlapping!!!!!");
			return false;
		}
	}

} // class
