/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Element;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Title block to be displayed on top of song. Shows song name, composer, date
 * and page number.
 * 
 * @author krein
 */
//TODO: create header and footer classes for all pages displaying page numbers, etc.
public class TitleBlock extends ScoreElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private static final int DEFAULT_TEXT_SIZE = 30;

	/** ratio of staff line spacing and lyrics font */
	private static final float TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR = 2.5f;

	/** ratio of staff line spacing and title and composer text distance */
	private static final float TEXT_DISTANCE_TO_STAFF_LINE_SPACING_FACTOR = 1.5f;

	/** ratio between title text size and composer name text size */
	private static final float TITLE_TO_COMPOSER_TEXT_SIZE_RATIO = 1.5f;

	/** name of song */
	@Element(name = "name")
	private String title = null;

	/** name of song composer */
	@Element(name = "composer", required = false)
	private String composer = null;

	/** last editing date of song */
	@Element(name = "editDate", required = false)
	private String editDate = null;

	/** number of current page */
	private String pageCurrent = null;

	/** total number of pages */
	private String pagesTotal = null;

	/** remember drawing mode */
	private DisplayMode mode;

	private static Paint paint;

	/** text sizes of contents */
	private static float titleTextSize;
	private static float composerTextSize;

	// position of title block contents
	private Point titlePosition;
	private Point composerPosition;
	private Point datePosition;
	private Point pagesPosition;

	/** modes for drawing of title bar */
	public enum DisplayMode {
		/** show all elements, for first page */
		FULL,
		/** show pages numbers only, for following pages */
		PAGE_NUMBERS_ONLY
	};


	// +++++++ getters and setters +++++++

	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getComposer() {
		return composer;
	}


	public void setComposer(String composer) {
		this.composer = composer;
	}


	public String getDate() {
		return editDate;
	}


	public void setDate(String date) {
		this.editDate = date;
	}


	public void setPagesCurrent(int pageCurrent) {
		this.pageCurrent = ((Integer) pageCurrent).toString();
	}


	public void setPagesTotal(int pagesTotal) {
		if (pagesTotal != 0)
			this.pagesTotal = ((Integer) pagesTotal).toString();
		else
			this.pagesTotal = null;
	}

	/** static initialization of title blocks's paint */
	static {

		TitleBlock.paint = new Paint();
		TitleBlock.paint.setAntiAlias(true);
		TitleBlock.paint.setColor(Color.BLACK);
		TitleBlock.paint.setTypeface(Typeface.SERIF);
		TitleBlock.titleTextSize = DEFAULT_TEXT_SIZE;
		TitleBlock.composerTextSize = DEFAULT_TEXT_SIZE
				/ TITLE_TO_COMPOSER_TEXT_SIZE_RATIO;
	}


	/**
	 * Constructor of TitleBlock
	 * 
	 * @param name
	 *            name of song
	 * @param composer
	 *            name of composer
	 * @param editDate
	 *            date of last song edit
	 */
	public TitleBlock(@Element(name = "name") String name,
			@Element(name = "composer") String composer,
			@Element(name = "editDate") String editDate) {
		this.title = name;
		this.composer = composer;
		this.editDate = editDate;
		titlePosition = new Point();
		composerPosition = new Point();
		datePosition = new Point();
		pagesPosition = new Point();
		mode = DisplayMode.FULL;
	}


	@Override
	public void setLayout(Point origin, int lineSpacing, int width, int height) {

		// save parameters first
		super.setLayout(origin, lineSpacing, width, height);

		// adjust text sizes according to linespacing
		TitleBlock.titleTextSize = lineSpacing
				* TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR;
		TitleBlock.composerTextSize = TitleBlock.titleTextSize
				/ TITLE_TO_COMPOSER_TEXT_SIZE_RATIO;

		Rect bounds = new Rect();

		if (mode == DisplayMode.FULL) {
			// set positions of all contents
			if (title != null) {
				TitleBlock.paint.setTextSize(TitleBlock.titleTextSize);
				TitleBlock.paint
						.getTextBounds(title, 0, title.length(), bounds);
				// postion title in middle of song
				titlePosition.x = getOrigin().x + width / 2 - bounds.width()
						/ 2;
				titlePosition.y = getOrigin().y + bounds.height();
			}

			if (composer != null) {
				// position composer
				TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
				TitleBlock.paint.getTextBounds(composer, 0, composer.length(),
						bounds);

				composerPosition.x = origin.x + width / 2 - bounds.width() / 2;
				composerPosition.y = titlePosition.y
						+ bounds.height()
						+ (int) (lineSpacing * TEXT_DISTANCE_TO_STAFF_LINE_SPACING_FACTOR);

				// set height of title bar(composer is lowest element)
				setHeight(composerPosition.y - getOrigin().y);

				TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
				TitleBlock.paint.getTextBounds(editDate, 0, editDate.length(),
						bounds);
			}
			if (editDate != null) {
				// position date
				datePosition.y = titlePosition.y;
				TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
				TitleBlock.paint.getTextBounds(editDate, 0, editDate.length(),
						bounds);
				datePosition.x = origin.x + width - bounds.width();
			}
			pagesPosition.x = origin.x;
			pagesPosition.y = titlePosition.y;

		} else {
			// set position of page numbers only

			TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
			TitleBlock.paint.getTextBounds(editDate, 0, editDate.length(),
					bounds);

			pagesPosition.x = getOrigin().x;
			pagesPosition.y = getOrigin().y + bounds.height();

			// set height of title block (pagesPosition is lowest)
			setHeight(pagesPosition.y - getOrigin().y);
		}

		// Log.d(TAG, String.format(
		// "titlePositionX: %d  datePositionX: %d", titlePosition.x,
		// datePosition.x));
	}


	/**
	 * Layout title bar elements for different modes. TitleBar is looking
	 * different on first and following pages, e.g.
	 * 
	 * @param mode
	 *            Mode the title should be displayed in.
	 */
	public void setLayout(Point origin, int lineSpacing, int width, int height,
			DisplayMode mode) {

		this.mode = mode;
		this.setLayout(origin, lineSpacing, width, height);
	}


	@Override
	public void drawSelf(Canvas canvas) {

		if (mode == DisplayMode.FULL) {

			TitleBlock.paint.setTextSize(TitleBlock.titleTextSize);
			if (title != null) {
				canvas.drawText(title, titlePosition.x, titlePosition.y,
						TitleBlock.paint);
			}
			if (composer != null) {
				TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
				canvas.drawText(composer, composerPosition.x,
						composerPosition.y, TitleBlock.paint);
			}
			if (editDate != null) {
				TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
				canvas.drawText(editDate, datePosition.x, datePosition.y,
						TitleBlock.paint);
			}
		}

		if (pageCurrent != null && pagesTotal != null) {
			String pages = new String(pageCurrent);
			pages += "/";
			pages += pagesTotal;
			TitleBlock.paint.setTextSize(TitleBlock.composerTextSize);
			canvas.drawText(pages, pagesPosition.x, pagesPosition.y,
					TitleBlock.paint);
		}

		// drawDebugRect(canvas);
	}

} // class
