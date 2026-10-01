/*
 * Copyright (C) 2015 Joerg Krein
 */

package de.songeasy.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.OverScroller;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

import de.songeasy.android.dialog.DialogAddTimedElement;
import de.songeasy.android.dialog.DialogEditAnnotation;
import de.songeasy.android.dialog.DialogEditChord;
import de.songeasy.android.dialog.DialogEditLyric;
import de.songeasy.android.dialog.DialogEditMeasure;
import de.songeasy.android.dialog.DialogEditMeasure.Mode;
import de.songeasy.android.dialog.DialogEditNote;
import de.songeasy.android.dialog.DialogEditRest;
import de.songeasy.android.dialog.DialogEditTextBlock;
import de.songeasy.android.interfaces.Draggable;
import de.songeasy.android.interfaces.OnChordSelectedListner;
import de.songeasy.android.interfaces.OnLabelEditListener;
import de.songeasy.android.interfaces.OnLengthSelectedListener;
import de.songeasy.android.interfaces.OnLyricsEnteredListener;
import de.songeasy.android.interfaces.OnMeasureSetupListener;
import de.songeasy.android.interfaces.OnNoteEditListener;
import de.songeasy.android.interfaces.OnRestEditListener;
import de.songeasy.android.interfaces.OnTextBlockEnteredListener;
import de.songeasy.android.score.Beam;
import de.songeasy.android.score.Chord;
import de.songeasy.android.score.Coda;
import de.songeasy.android.score.Fine;
import de.songeasy.android.score.KeySignature;
import de.songeasy.android.score.Label;
import de.songeasy.android.score.Lyric;
import de.songeasy.android.score.Measure;
import de.songeasy.android.score.Measure.RepeatType;
import de.songeasy.android.score.Note;
import de.songeasy.android.score.Note.NoteAccidental;
import de.songeasy.android.score.Note.NoteName;
import de.songeasy.android.score.PageProperties;
import de.songeasy.android.score.Rest;
import de.songeasy.android.score.ScoreElement;
import de.songeasy.android.score.Segno;
import de.songeasy.android.score.Song;
import de.songeasy.android.score.TextBlock;
import de.songeasy.android.score.Tie;
import de.songeasy.android.score.TimeSignature;
import de.songeasy.android.score.TimedElement.NoteLength;

/**
 * A music score containing all staffs, chords, lyrics, etc.
 * 
 * @author krein
 */
// TODO add pagebreak
public class ScoreView extends View {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();
    /* minimum scroll value before snap to zero */
	private static final int SCROLLSNAPDISTANCE = 20;
    /* defines velocity of fling effect, bigger value means slower fling */
    private static final int FLING_VELOCITY_DIVIDER = 2;
    /* defines amount of overscrolling effect, bigger value means less overscrolling */
    private static final int FLING_OVERSCROLL_DIVIDER = 15;

	/** modes for this activity */
	public static enum ScoreModes {
		/** just viewing */
		VIEW,
		/** select elements and applying actions on them */
		EDIT_SELECT,
		/** adding new elements */
		EDIT_ADD,
		/** play the song */
		PLAY
	}

    /** context of activity */
	private Context context;

    /** the song shown in this view */
	private Song song;

	/**
	 * origin of song, used for song positioning in onLayout(), defined here
	 * because we should not allocate objects in onLayout()
	 */
	private Point songOrigin;

	/** actual length for editing */
	private Note.NoteLength editLength;

	/** actual type for editing */
	private Class<? extends ScoreElement> editElementType;

	/** mode this score is in */
	private ScoreModes mode;

	/** last touch position */
	private Point lastTouch;

	/** touch interpreter helper class */
	private GestureDetector detector;

	private ScaleGestureDetector scaleDetector;

    private OverScroller overScroller;

	/** flag if an element is dragged */
	private boolean elementIsDragged;

	/** flag that element is currently added */
	private boolean isAdding;

	/** image of currently dragged ScoreElement */
	private DragImage dragImage;

	// we need to scroll further down if bottom bars are
	// displayed, TODO: needs to be more adaptive
    private int verticalScrollOffset = 0;

	// the currently displayed page in mode VIEW, first page is 0 !
	private int currentPage = 0;


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

	/**
	 * Set extra scroll offset.
	 * 
	 * @param verticalScrollOffset
	 *            offset in pixel
	 */
	public void setVerticalScrollOffset(int verticalScrollOffset) {
		this.verticalScrollOffset = verticalScrollOffset;
	}


	/**
	 * Get song currently displayed in view.
	 * 
	 * @return song, null otherwise
	 */
	public Song getSong() {

		return song;
	}


	@SuppressLint("WrongCall")
	public void setSong(Song song) {

		this.song = song;
		// song gets its new layout, its the view's space with margins
		// we are calling layout here once, because Android sometimes
		// doesn't call onLayout() after loading
		setCurrentPage(0);
		// onLayout(true, 0, 0, 0, 0);
	}


    /**
	 * Get current mode of this view.
	 * 
	 * @return the mode of type ScoreModes
	 */
	public ScoreModes getMode() {

		return mode;
	}

	/**
	 * Get the current type of element to be edited or add.
	 * 
	 * @return currently selected type for edit
	 */
	public Class<? extends ScoreElement> getEditElementType() {

		return editElementType;
	}


	/**
	 * Set type to edit or add.
	 * 
	 * @param editElementType Type of element that is edit
	 */
	public void setEditElementType(Class<? extends ScoreElement> editElementType) {

		this.editElementType = editElementType;
		// deselect any element of possibly different type
		if (song != null)
			song.deselectElement();

		invalidate();
	}


	/**
	 * Set mode for this view.
	 * 
	 * @param newMode
	 *            the mode to set
	 */
	@SuppressLint("WrongCall")
	public void setMode(ScoreModes newMode) {

		ScoreModes oldMode = this.mode;

		// deselect elements when switching to non edit mode
		if (song != null && newMode == ScoreModes.VIEW) {
			song.deselectElement();
            currentPage = 0;
        }

		// avoid drawing cross hairs in select mode
		if(newMode == ScoreModes.EDIT_SELECT) {
			isAdding = false;
            invalidate();
		}

		this.mode = newMode;

		// onLayout(true, 0, 0, 0, 0);
		// invalidate();
		// reset view if we switch from view mode to one of the edit modes or reverse
		if ((newMode == ScoreModes.VIEW && (oldMode == ScoreModes.EDIT_ADD || oldMode == ScoreModes.EDIT_SELECT))
				|| (oldMode == ScoreModes.VIEW && (newMode == ScoreModes.EDIT_ADD || newMode == ScoreModes.EDIT_SELECT))) {
			scrollTo(0, 0);
			setZoom(1.0f);
		}
	}


	/**
	 * Set note length for edit operations
	 * 
	 * @param editLength
	 *            the length for editing
	 */
	public void setEditLength(Note.NoteLength editLength) {

		this.editLength = editLength;
	}


	/**
	 * @return the editLength
	 */
	public Note.NoteLength getEditLength() {

		return editLength;
	}


	/**
	 * Set zoom factor of song. Changes the space between staff lines and
	 * therefore the size of the score.
	 * 
	 * @param factor
	 *            factor to multiply width and height of song with
	 */
	@SuppressLint("WrongCall")
	public void setZoom(float factor) {

		PageProperties.setZoomFactor(factor);
		onLayout(true, 0, 0, 0, 0);
		invalidate();
	}


	/**
	 * Set page of song to be displayed. First page has index 0!
	 * 
	 * @param page
	 *            page of song to be displayed
	 */
	@SuppressLint("WrongCall")
	public void setCurrentPage(int page) {

		int maxPage = 1;
		if (song != null)
			maxPage = song.getNumberOfPages() - 1;

		if (page >= 0 && page <= maxPage) {
			this.currentPage = page;

			onLayout(true, 0, 0, 0, 0);
			invalidate();
		}
	}


	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * The constructor of class ScoreView
	 * 
	 * @param context Context of app
	 */
	public ScoreView(Context context) {

		this(context, null, 0);
	}


	/**
	 * Second constructor for ScoreView, for use in graphical layout editor
	 * only.
	 * 
	 * @param context Context of app
	 * @param attrs Attribute set to use
	 */
	public ScoreView(Context context, AttributeSet attrs) {

		this(context, attrs, 0);
	}


	/**
	 * Third constructor for ScoreView, for use in graphical layout editor only.
	 * 
	 * @param context Context of app
	 * @param attrs Attribute set to use
	 * @param defStyle Style definition to use
	 */
	public ScoreView(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);
		this.context = context;

		// white background
		setBackgroundColor(Color.WHITE);

		/* paint style */
        Paint paint = new Paint();
		paint.setAntiAlias(true);
		// paint.setTypeface(Typeface.createFromAsset(
		// context.getAssets(), "fonts/ntedfont.otf"));
		paint.setTextSize(30);

		// needed for showing large text size under Android 4.2.2
		// see also
		// http://stackoverflow.com/questions/17029858/nexus-7-4-2-2-canvas-drawtext-letters-f-and-j-wont-display
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		detector = new GestureDetector(context, new GestureListener());

		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        overScroller = new OverScroller(context);

		setWillNotDraw(false);
		// show horizontal and vertical scrollbars
		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);

		final TypedArray a = context.getTheme().obtainStyledAttributes(
				new int[0]);
		//final TypedArray a = context.obtainStyledAttributes(com.android.internal.R.styleable.View);
        //Not working with API 21 (Lollipop) yet!
        // see also http://stackoverflow.com/questions/26448771/initializescrollbars-is-undefined
		//initializeScrollbars(a);
		a.recycle();


		// create one instance of PageProperties
		// pageProperties = new PageProperties();

		lastTouch = new Point();
		songOrigin = new Point();
		song = null;

		// set defaults
		editLength = NoteLength.QUARTER;
		mode = ScoreModes.VIEW;
		isAdding = false;
		elementIsDragged = false;
	}


	/**
	 * The onDraw function of this view. Called by android on gui changes.
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		//Log.d(TAG, String.format("onDraw  x: %d  y: %d", getScrollX(), getScrollY()));

        // computeScrollOffset() returns true if a fling is in progress
        if(!overScroller.isFinished()) {
            overScroller.computeScrollOffset();
            int scrollX = overScroller.getCurrX();
            int scrollY = overScroller.getCurrY();
            scrollTo(scrollX,scrollY);
            Log.d(TAG, String.format("onDraw scrollTo: %d %d", scrollX,scrollY));
            postInvalidateDelayed(20);
        }


		// Log.d(TAG, String.format("onDraw:  width: %d  height: %d",
		// getWidth(), getHeight()));

		// scrollBarVertical.setParameters(
		// computeVerticalScrollRange(), computeVerticalScrollOffset(),
		// computeVerticalScrollExtent(), true);
		//
		// canvas.save();
		// canvas.scale(5.0f, 5.0f);
		// canvas.restore();

		if (song != null) {

            if (song.isExportIsActive() == true) return;

			switch (mode)
				{

				case EDIT_SELECT:
				case EDIT_ADD:
				default:
					song.drawSelf(canvas);

					if (elementIsDragged)
						dragImage.drawSelf(canvas);

					if (isAdding)
						drawCrossHairs(canvas, lastTouch);

					break;

				case VIEW:
					song.drawSelf(canvas, currentPage);
					break;
				}

		} else {
			// Log.d(TAG, "onDraw: song is null!");
		}
	}


	/**
	 * Draw cross hairs to visualize touch coordinate for positioning of
	 * elements.
	 * 
	 * @param canvas
	 *            canvas to draw on
	 * @param position
	 *            position of touch, already corrected by getScroll()
	 */
	private void drawCrossHairs(Canvas canvas, Point position) {

		Paint chPaint = new Paint();
		chPaint.setStyle(Style.STROKE);
		chPaint.setStrokeWidth(2.0f);
		chPaint.setColor(Color.BLUE);
		chPaint.setAntiAlias(true);

		// Log.d(TAG, String.format("touch: %d  %d", position.x, position.y));

		// draw horizontal line
		canvas.drawLine(getLeft() + getScrollX()
				+ PageProperties.CROSSHAIRS_MARGIN, getTop()
				+ (float) position.y, getRight() + getScrollX()
				- PageProperties.CROSSHAIRS_MARGIN, getTop()
				+ (float) position.y, chPaint);

		// Log.d(TAG, String.format(
		// "CH: x1:%.2f  y1:%.2f  x2:%.2f  y2:%.2f", getX() + getScrollX()
		// + 10.0f, (float) position.y, getX() + getScrollX()
		// + getWidth() - 10.0f, (float) position.y));

		// draw vertical line
		canvas.drawLine(getLeft() + (float) position.x, getTop() + getScrollY()
				+ PageProperties.CROSSHAIRS_MARGIN, getLeft()
				+ (float) position.x, getBottom() + getScrollY()
				- PageProperties.CROSSHAIRS_MARGIN, chPaint);
		//
		// Log.d(TAG, String.format(
		// "CV: x1:%.2f  y1:%.2f  x2:%.2f  y2:%.2f", (float) position.x,
		// getY() + getScrollY() + 10.0f, (float) position.x, getY()
		// + getScrollY() + getHeight() + getScrollY() - 10.0f));
	}


	/**
	 * Called to determine the size requirements for this view and all of its
	 * children. onMeasure gets called twice. First time with height = 0!
	 * 
	 * @see android.view.SurfaceView#onMeasure(int, int)
	 */
	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	//
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	//
	// // get size supplied by parent
	// int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
	// int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	//
	// // Log.d(TAG,
	// // String.format("onMeasure:  widthParent: %d  heightParent: %d",
	// // parentWidth, parentHeight));
	// // song.onMeasure
	//
	// int songNeededHeight = parentHeight;
	//
	// // only if both parameters are set
	// if (!(parentWidth == 0 || parentHeight == 0)) {
	//
	// if (song != null) {
	//
	// // calculate staffline spacing with available width minus all
	// // margins
	// pageProperties.calculateStaffLineSpacing(parentWidth
	// - (2 * PageProperties.LINESMARGIN)
	// - (2 * PageProperties.HORIZONTALMARGIN));
	//
	// // get height needed by song
	// songNeededHeight = song.getHeight(parentWidth - 2
	// * PageProperties.HORIZONTALMARGIN);
	//
	// // take maximum of needed and provided
	// songNeededHeight = (songNeededHeight > parentHeight) ? songNeededHeight
	// : parentHeight;
	//
	// // sometimes onLayout doesn't get called, so we do setLayout
	// // here
	// // song.setLayout(new Point(0,0), parentWidth - 2 *
	// // PageProperties.HORIZONTALMARGIN, songNeededHeight);
	// }
	// }
	//
	// setMeasuredDimension(parentWidth, songNeededHeight);
	//
	// Log.d(TAG, String.format("measured: %d, %d", parentWidth,
	// songNeededHeight));
	// }

	/**
	 * Layout song with height and width of this view.
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		// only if something has changed and we have a song
		if (!changed || song == null)
			return;

        if (song.isExportIsActive() == true) return;

		// Log.d(TAG, String.format(
		// "onLayout:  changed: %b  l: %d  t: %d  r: %d  b: %d width: %d",
		// changed, l, t, r, b, getWidth()));

		// recompute staffline spacing with given width, minus all margins
		PageProperties
				.calculateStaffLineSpacing((int) (getWidth() * PageProperties
						.getZoomFactor())
						- (2 * PageProperties.HORIZONTALMARGIN));

		songOrigin.set(0 + PageProperties.HORIZONTALMARGIN,
				0 + PageProperties.VERTICALMARGIN);

		switch (mode)
			{

			case EDIT_SELECT:
			case EDIT_ADD:
			default:
				// re-layout all score elements for all content, taking zoom
				// into account
				song.setLayout(songOrigin,
						PageProperties.getStaffLineSpacing(),
						(int) (getWidth() * PageProperties.getZoomFactor())
								- (2 * PageProperties.HORIZONTALMARGIN),
						(int) (getHeight() * PageProperties.getZoomFactor())
								- (2 * PageProperties.VERTICALMARGIN));
				break;

			case VIEW:
				// re-layout all score elements for current page, taking zoom
				// into account
				song.setLayout(songOrigin,
						PageProperties.getStaffLineSpacing(),
						(int) (getWidth() * PageProperties.getZoomFactor())
								- (2 * PageProperties.HORIZONTALMARGIN),
						(int) (getHeight() * PageProperties.getZoomFactor())
								- (2 * PageProperties.VERTICALMARGIN),
						currentPage);
				break;

			}

	}


	/**
	 * Called when the size of this view has changed.
	 * 
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	// @Override
	// protected void onSizeChanged(int w, int h, int oldW, int oldH) {
	//
	// Log.d(TAG,
	// String.format("onSizeChanged:  w: %d  h: %d  oldW: %d  oldH: %d",
	// w, h, oldW, oldH));
	//
	// if (song != null) {
	// Point songStart = new Point(0 + PageProperties.HORIZONTALMARGIN,
	// 0 + PageProperties.VERTICALMARGIN);
	// // re-layout all score elements
	// song.setLayout(songStart, pageProperties.getStaffLineSpacing(),
	// getWidth() - (2 * PageProperties.HORIZONTALMARGIN), getHeight()
	// - (2 * PageProperties.VERTICALMARGIN));
	// }
	// }

	/**
	 * Save song temporarily to app's cache directory.
	 * 
	 * @param context
	 *            context of activity
	 */
	public void rescueSong(Context context) {

		if (song == null)
			return;

		Serializer serializer = new Persister();
		File path = context.getCacheDir();
		File result = new File(path, "SaveSong.xml");

		try {

			serializer.write(song, result);
		} catch (Exception e) {

			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * Load song from app's cache directory.
	 * 
	 * @param context Context of app
	 */
	@SuppressLint("WrongCall")
	public void restoreSong(Context context) {

		Serializer deserializer = new Persister();
		File path = context.getCacheDir();
		File source = new File(path, "SaveSong.xml");

		try {
			song = deserializer.read(Song.class, source);
			onLayout(true, 0, 0, 0, 0);

		} catch (java.lang.Exception e) {

			song = null;
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * Handling touch events of this view. Touch is handled different, dependent
	 * on the state this view is in: view, edit,play. Calls GestureDetector and
	 * ScaleDetector first. Moving of dragged elements is handled here.
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 *      android.view.MotionEvent)
	 */
	@SuppressLint("WrongCall")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		// Log.d(TAG, "onTouch");
		// let our GestureDetector differentiate the touch input
		scaleDetector.onTouchEvent(ev);
		detector.onTouchEvent(ev);
		Measure measureOfTouch;

        // prevent ScrollView from intercepting our zoom gesture
		if (scaleDetector.isInProgress()) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}

		// save last touch position
		lastTouch.x = (int) ev.getX() + getScrollX();
		lastTouch.y = (int) ev.getY() + getScrollY();

		// handle move event
		if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {

			// we are in drag mode
			if (elementIsDragged) {

				// move image to new position
				dragImage.move(new Point((int) ev.getX() + getScrollX(),
						(int) ev.getY() + getScrollY()));

				// scroll if user reaches end of view
				if (dragImage.getCurrentPosition().x + 0.75
						* dragImage.getWidth() > getScrollX() + getWidth())
					scrollBy(20, 0);

				if (dragImage.getCurrentPosition().x + 0.25
						* dragImage.getWidth() < getScrollX())
					scrollBy(-20, 0);

				if (dragImage.getCurrentPosition().y - 0.5
						* dragImage.getHeight() < getScrollY())
					scrollBy(0, -20);

				if (dragImage.getCurrentPosition().y + 0.5
						* dragImage.getHeight() > getScrollY() + getHeight()
						- 30)
					scrollBy(0, 20);

				// force redraw to update position of dragged element
				invalidate();
			} else {
				// we are in adding mode
				if (isAdding) {
					// move note vertically in steps of half staffline spacing
					if (editElementType == Note.class) {
						measureOfTouch = song.getMeasureAtPosition(lastTouch);
						if (measureOfTouch != null) {
							// TODO: staff hardcoded, getStaff in measure
							// needed!
							// snap y coordinate of last touch point
							lastTouch.y = measureOfTouch.getStaves().get(0)
									.snapToYGrid(lastTouch.y);
						}
					}
				}
				invalidate();
			}

		}

		// handle up event
		if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
			// move element to new position
			if (elementIsDragged) {

				// paste element at new position and delete original
				if (song.pasteElementAtPosition(dragImage.getCurrentPosition()))
					song.deleteSelectedElement();

				// force rearranging of elements
				onLayout(true, 0, 0, 0, 0);
				invalidate();
			}
			// add element at position
			if (mode == ScoreModes.EDIT_ADD) {

				onAddElement(new Point((int) ev.getX() + getScrollX(),
						(int) ev.getY() + getScrollY()));
			}

			elementIsDragged = false;
			performClick();

		}

		return true;
	}


	/**
	 * Call edit dialog for score element previously selected
	 */
	public void onEditElement() {

		ScoreElement element = song.getSelectedElement();

		if (element == null)
			return;

		// Log.d(TAG, "onEditElement");

		// show save icon when song is changed
		song.setChanged(true);
		((Activity) context).invalidateOptionsMenu();

		if (element.getClass() == Measure.class) {

			DialogEditMeasure measureEditDialog = new DialogEditMeasure(
					context, Mode.EDIT, (Measure) element);
			measureEditDialog.setTitle(context
					.getString(R.string.title_edit_measure));

			measureEditDialog
					.setOnMeasureSetupListener(new OnMeasureSetupListener() {

						@SuppressLint("WrongCall")
						@Override
						public void onMeasureSetup(Bundle parameters) {

							if (parameters == null)
								return;

							Measure editMeasure = (Measure) song
									.getSelectedElement();
							if (editMeasure == null)
								return;

							if (parameters
                                    .getBoolean(DialogEditMeasure.KEY_MEASURE_NEWLINE))

								song.insertNewLine(editMeasure);
							else
								song.removeNewLine(editMeasure);

							if (parameters
                                    .getBoolean(DialogEditMeasure.KEY_MEASURE_PAGEBREAK)) {
								song.addPageBreak(editMeasure);
							}

							else {
								song.removePageBreak(editMeasure);
							}

							boolean repeatStart = parameters.getBoolean(
									DialogEditMeasure.KEY_MEASURE_REPEAT_START,
									false);

							boolean repeatStop = parameters.getBoolean(
									DialogEditMeasure.KEY_MEASURE_REPEAT_STOP,
									false);

							boolean repeatEnding = parameters
									.getBoolean(
											DialogEditMeasure.KEY_MEASURE_REPEAT_ENDING,
											false);

							if (repeatStart)
								editMeasure.setRepeat(RepeatType.START);

							if (repeatStop)
								editMeasure.setRepeat(RepeatType.STOP);

							if (repeatStart && repeatStop)
								editMeasure.setRepeat(RepeatType.BOTH);

							if (repeatEnding)
								editMeasure.setRepeat(RepeatType.ENDING);

							if (!repeatStart && !repeatEnding && !repeatStop)
								editMeasure.setRepeat(RepeatType.NO_REPEAT);

							if (parameters
                                    .getBoolean(DialogEditMeasure.KEY_MEASURE_KEYCHANGE)) {
								song.addKeyChange(
										editMeasure,
										(KeySignature) parameters
												.getParcelable(DialogEditMeasure.KEY_MEASURE_KEYSIGNATURE));
								onLayout(true, 0, 0, 0, 0);
							}

							if (parameters
                                    .getBoolean(DialogEditMeasure.KEY_MEASURE_TIMECHANGE))
								editMeasure
										.setTimeChange((TimeSignature) parameters
												.getParcelable(DialogEditMeasure.KEY_MEASURE_TIMESIGNATURE));

							onLayout(true, 0, 0, 0, 0);
							invalidate();
						}
					});

			measureEditDialog.show();
		}

		if (element.getClass() == Note.class) {
			DialogEditNote noteDialog = new DialogEditNote(context,
					(Note) element);
			noteDialog.setOnNoteEditListener(new OnNoteEditListener() {

				@SuppressLint("WrongCall")
				@Override
				public void onNoteEdit(Note note) {

					if (note != null)
						// show changed note
						onLayout(true, 0, 0, 0, 0);
					invalidate();
				}
			});
			noteDialog.show();
		}

		if (element.getClass() == Rest.class) {
			DialogEditRest restDialog = new DialogEditRest(context,
					(Rest) element);
			restDialog.setOnRestEditListener(new OnRestEditListener() {

				@Override
				public void onRestEdit(Rest rest) {

					if (rest != null)
						// show changed note
						invalidate();
				}
			});
			restDialog.show();
		}

		if (element.getClass() == Chord.class) {
			// open edit dialog for chords
			DialogEditChord chordEditDialog = new DialogEditChord(context,
					(Chord) element);
			chordEditDialog.setTitle(context
					.getString(R.string.title_edit_chord));
			chordEditDialog
					.setOnChordSelectedListener(new OnChordSelectedListner() {

						@Override
						public void onChordSelected(Chord chord) {

							if (chord != null)
								invalidate();
						}
					});
			chordEditDialog.show();
		}

		if (element.getClass() == Lyric.class) {
			// open edit dialog for lyric
			DialogEditLyric lyricEditDialog = new DialogEditLyric(context,
					((Lyric) element).getText(),
					((Lyric) element).getLineNumber());
			lyricEditDialog.setTitle(getResources().getString(R.string.title_edit_lyrics));
			lyricEditDialog
					.setOnLyricsEnteredListener(new OnLyricsEnteredListener() {

						@Override
						public void onLyricsEntered(String text, int lineNumber) {

							if (text != null) {
								// edit text
								((Lyric) song.getSelectedElement())
										.setText(text);
								((Lyric) song.getSelectedElement())
										.setLineNumber(lineNumber);
								// force redraw
								invalidate();
							}
						}
					});
			lyricEditDialog.show();
		}

		if (element.getClass() == Label.class) {
			// open edit dialog for annotation
			DialogEditAnnotation annotationEditDialog = new DialogEditAnnotation(
					context, ((Label) element).getText());
			annotationEditDialog.setTitle(context
					.getString(R.string.title_edit_annotation));
			annotationEditDialog.setLabelListener(new OnLabelEditListener() {

				@Override
				public void onLabelEdit(String text) {
					// edit text
					if (text != null)
						((Label) song.getSelectedElement()).setText(text);
					// force redraw
					invalidate();
				}
			});

			annotationEditDialog.show();
		}

		if (element.getClass() == TextBlock.class) {

			DialogEditTextBlock textBoxEditDialog = new DialogEditTextBlock(
					context, ((TextBlock) element));
			textBoxEditDialog.setTitle(context
					.getString(R.string.title_edit_textblock));
			textBoxEditDialog
					.setOnTextBlockEnteredListener(new OnTextBlockEnteredListener() {

						@Override
						public void onTextBlockEntered(TextBlock textBlock) {

							if (textBlock != null)
								invalidate();
						}
					});

			textBoxEditDialog.show();
		}
	} // onEditElement



    @SuppressLint("WrongCall")
    public void onToggleFeature(ScoreElement feature) {

        ScoreElement element = song.getSelectedElement();

        if (element == null)
            return;

        if (feature.getClass() == Tie.class) {
            // ties can only be applied to notes
            if(element.getClass() != Note.class)
                return;

            if(((Note)element).getTies().contains(Note.TieType.START))
                ((Note)element).getTies().clear();
            else
                ((Note)element).addTie(Note.TieType.START);

            onLayout(true, 0, 0, 0, 0);
            // force redraw
            invalidate();
            return;
        }

        if (feature.getClass() == Beam.class) {
            // beams can only be applied to notes
            if (element.getClass() != Note.class)
                return;

            if (((Note) element).getBeams().size() != 0) {
                ((Note) element).getBeams().clear();
                return;
            }

            switch (((Beam) feature).getType()) {
                case BEGIN:
                    ((Note) element).addBeam(new Beam(Beam.Type.BEGIN, 0));
                    break;

                case CONTINUE:
                    ((Note) element).addBeam(new Beam(Beam.Type.CONTINUE, 0));
                    break;

                case END:
                    ((Note) element).addBeam(new Beam(Beam.Type.END, 0));
                    break;
            }

            onLayout(true, 0, 0, 0, 0);
            // force redraw
            invalidate();
        }
    }



	/**
	 * Handle touch event (show_press) in mode EDIT_ADD Adds element to song,
	 * previously selected in BottomBar.
	 * 
	 * @param point
	 *            position to add new element at
	 */
	@SuppressLint("WrongCall")
	private void onAddElement(Point point) {

		// Log.d(TAG, "onAddElement");

		// update actionbar for quick save button
		song.setChanged(true);
		((Activity) context).invalidateOptionsMenu();

		// if (song.selectMeasureAtPosition(point) == false)
		// return;

		// try to add new element at touched position
        // use label for always calling invalidate at end of if clause and to avoid unnecessary if checks
        adding: if (editElementType != null && isAdding) {

			isAdding = false;

			// remember point of touch for delayed calls of dialogs
			lastTouch = point;

			if (editElementType.equals(Note.class)) {

				DialogAddTimedElement addNoteDialog = new DialogAddTimedElement(
						context, Note.class);

				addNoteDialog.setPosition(new Point(point.x - getScrollX(),
						point.y - getScrollY()));

				addNoteDialog
						.setOnLengthSelectedListener(new OnLengthSelectedListener() {

							@Override
							public void onLengthSelected(NoteLength length) {
								song.addNoteAtPosition(new Note(NoteName.C,
                                        NoteAccidental.NONE, 0, length, false,
                                        0, 0), lastTouch);
								onLayout(true, 0, 0, 0, 0);
								invalidate();

							}
						});

				addNoteDialog.show();
				break adding;
			}

			if (editElementType.equals(Rest.class)) {
				DialogAddTimedElement addNoteDialog = new DialogAddTimedElement(
						context, Note.class);

				addNoteDialog.setPosition(new Point(point.x - getScrollX(),
                        point.y - getScrollY()));

				addNoteDialog
						.setOnLengthSelectedListener(new OnLengthSelectedListener() {

                            @Override
                            public void onLengthSelected(NoteLength length) {
                                song.addRestAtPosition(new Rest(length, 0, 0),
                                        lastTouch);
                                onLayout(true, 0, 0, 0, 0);
                                invalidate();

                            }
                        });

				addNoteDialog.show();
				break adding;
			}

			if (editElementType.equals(Chord.class)) {

				Measure touchedMeasure = song.getMeasureAtPosition(point);
				if (touchedMeasure == null)
					break adding;

				Chord chordToAdd = new Chord();

                DialogEditChord chordAddDialog = new DialogEditChord(context,
                        chordToAdd);
                chordAddDialog.setTitle(context
                        .getString(R.string.title_add_chord));
                chordAddDialog
                        .setOnChordSelectedListener(new OnChordSelectedListner() {

                            @Override
                            public void onChordSelected(Chord chord) {

                                if (chord != null) {
                                    song.addAnnotationAtPosition(chord,
                                            lastTouch);
                                }
                                // force redraw
                                invalidate();
                            }
                        });

                chordAddDialog.show();
                // return directly, invalidate is called in dialog callback
                return;
			}

			if (editElementType.equals(Lyric.class)) {

				Measure touchedMeasure = song.getMeasureAtPosition(point);
				if (touchedMeasure == null)
					break adding;

				DialogEditLyric lyricAddDialog = new DialogEditLyric(context,
						"", 0);
				lyricAddDialog.setTitle(getResources().getString(R.string.title_add_lyrics));

				lyricAddDialog
						.setOnLyricsEnteredListener(new OnLyricsEnteredListener() {

                            @Override
                            public void onLyricsEntered(String text,
                                                        int lineNumber) {

                                if (text != null) {
                                    song.addAnnotationAtPosition(new Lyric(
                                                    text, 0.0f, 0, lineNumber),
                                            lastTouch);
                                }
                                // force redraw
                                invalidate();
                            }
                        });
				lyricAddDialog.show();
                // return directly, invalidate is called in dialog callback
                return;
			}

			if (editElementType.equals(TextBlock.class)) {

				TextBlock textBlockToAdd = new TextBlock(null, null);

				DialogEditTextBlock textBlockAddDialog = new DialogEditTextBlock(
						context, textBlockToAdd);

				textBlockAddDialog.setTitle(context
						.getString(R.string.title_add_textblock));

				textBlockAddDialog
						.setOnTextBlockEnteredListener(new OnTextBlockEnteredListener() {

                            @Override
                            public void onTextBlockEntered(TextBlock textBlock) {

                                if (textBlock != null) {
                                    song.addTextBlock(new TextBlock(textBlock
                                            .getTitle(), textBlock.getContent()));
                                }
                                onLayout(true, 0, 0, 0, 0);
                                invalidate();
                            }
                        });

				textBlockAddDialog.show();
                // return directly, invalidate is called in dialog callback
                return;
			}

			if (editElementType.equals(Label.class)) {

				Measure touchedMeasure = song.getMeasureAtPosition(point);
				if (touchedMeasure == null)
					break adding;

				DialogEditAnnotation annotationAddDialog = new DialogEditAnnotation(
						context, "");
				annotationAddDialog.setTitle(context
                        .getString(R.string.title_add_annotation));
                annotationAddDialog.setLabelListener(new OnLabelEditListener() {

                    @Override
                    public void onLabelEdit(String text) {

                        if (text != null) {
                            song.addAnnotationAtPosition(new Label(text, 0.0f),
                                    lastTouch);
                        }
                        // force redraw
                        invalidate();
                    }
                });

				annotationAddDialog.show();
                // return directly, invalidate is called in dialog callback
                return;
			}

			if (editElementType.equals(Coda.class)) {

				song.addDirectionAtPosition(new Coda(0), point);

				// force rearranging of elements
				onLayout(true, 0, 0, 0, 0);
                break adding;
            }

			if (editElementType.equals(Segno.class)) {

				song.addDirectionAtPosition(new Segno(0), point);
				onLayout(true, 0, 0, 0, 0);
                break adding;
            }

			if (editElementType.equals(Fine.class)) {

				song.addDirectionAtPosition(new Fine(0), point);
				onLayout(true, 0, 0, 0, 0);
                break adding;
            }

			if (editElementType.equals(Measure.class)) {

				Measure touchedMeasure = song.getMeasureAtPosition(point);
				if (touchedMeasure == null)
					break adding;

				DialogEditMeasure measureEditDialog = new DialogEditMeasure(
						context, Mode.ADD, touchedMeasure);
                measureEditDialog.setTitle(context
                        .getString(R.string.title_add_measure));
                measureEditDialog.setOnMeasureSetupListener(new OnMeasureSetupListener() {

                    @Override
                    public void onMeasureSetup(Bundle parameters) {

                        if (parameters != null) {
                            int count = parameters
                                    .getInt(DialogEditMeasure.KEY_MEASURE_COUNT);

                            boolean newLine = parameters
                                    .getBoolean(
                                            DialogEditMeasure.KEY_MEASURE_NEWLINE,
                                            false);
                            boolean repeatStart = parameters
                                    .getBoolean(
                                            DialogEditMeasure.KEY_MEASURE_REPEAT_START,
                                            false);
                            boolean repeatStop = parameters
                                    .getBoolean(
                                            DialogEditMeasure.KEY_MEASURE_REPEAT_STOP,
                                            false);
                            boolean repeatEnding = parameters
                                    .getBoolean(
                                            DialogEditMeasure.KEY_MEASURE_REPEAT_ENDING,
                                            false);

                            boolean keyChange = parameters
                                    .getBoolean(DialogEditMeasure.KEY_MEASURE_KEYCHANGE);

                            song.addMeasureAtPosition(count, newLine,
                                    repeatStart, repeatStop,
                                    repeatEnding, lastTouch);

                            onLayout(true, 0, 0, 0, 0);
                        }
                        invalidate();
                    }
                });

				measureEditDialog.show();
                break adding;
            } // is measure
		} // is adding
		// force redraw
		invalidate();
	} //onAddElement



    /**
     * Delete selected element in song.
     */
    @SuppressLint("WrongCall")
    public void onDeleteSelectedElement() {

        song.deleteSelectedElement();
        onLayout(true, 0, 0, 0, 0);
        invalidate();
    }


    /**
     * Handle touch event in edit mode edit.
     *
     * @param event
     *            the motion event, we consider ACTION_DOWN only
     * @return true if event was consumed
     */
    private boolean onSelectElement(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {

            case MotionEvent.ACTION_DOWN:

                // get touched measure, just to be sure
                Point point = new Point((int) event.getX() + getScrollX(),
                        (int) event.getY() + getScrollY());

                if (!song.selectElementAtPosition(point)) {
                    song.deselectElement();
                }
                invalidate();

                return true;

            default:
                return false;
        }
    }


    /**
     * Handle touch event in play mode.
     *
     * @param event Touch even to handle
     * @return true if event was consumed
     */
    private boolean onPlay(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {

            case MotionEvent.ACTION_DOWN:

                Point point = new Point((int) event.getX() + getScrollX(),
                        (int) event.getY() + getScrollY());

                if (song.selectMeasureAtPosition(point))
                    invalidate();
                return false;

            default:
                return false;
        }
    }


	/**
	 * goto previous page of song
	 */
	private void browsePageBack() {

		if (currentPage > 0) {
			currentPage--;
			scrollTo(0, 0);
		}

	}


	/**
	 * goto next page of song
	 */
	private void browsePageForward() {
		if (song == null)
			return;

		if (currentPage < song.getNumberOfPages() - 1) {
			currentPage++;
			scrollTo(0, 0);
		}
	}


	@Override
	protected int computeHorizontalScrollRange() {
		int horzRange = getWidth();

		if (song != null) {
			horzRange = song.getWidth();
		}

		// Log.d(TAG, String.format("horz range: %d", horzRange));

		return horzRange;
	}


	@Override
	protected int computeVerticalScrollRange() {
		int vertRange = getHeight();

		if (song != null) {
			vertRange = song.getHeight();
		}

		// Log.d(TAG, String.format("vert range: %d", vertRange));

		return vertRange;
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {

			float scaleFactor = PageProperties.getZoomFactor();
			scaleFactor *= detector.getScaleFactor();

			// don't let song get too small or too large
			scaleFactor = Math.max(PageProperties.PAGE_MIN_ZOOM,
					Math.min(scaleFactor, PageProperties.PAGE_MAX_ZOOM));

			setZoom(scaleFactor);

			return true;
		}
	}

	/**
	 * GestureListener class for this view. Differentiates simple touch gestures
	 * for us.
	 */
	@SuppressLint("WrongCall")
	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		/** handle ShowPress (down touch for a second) for different modes */
		@Override
		public void onShowPress(MotionEvent e) {

			switch (mode)
				{

/*				case VIEW:
                    // flip page if user touches the first or last third of
					// screen
					if ((int) e.getX() >= (getWidth() / 3) * 2) {
						browsePageForward();
						onLayout(true, 0, 0, 0, 0);
						invalidate();
					} else if ((int) e.getX() <= (getWidth() / 3)) {
						browsePageBack();
						onLayout(true, 0, 0, 0, 0);
						invalidate();
					}
					break;*/

				case EDIT_SELECT:
					if (song != null)
						onSelectElement(e);
					break;

				case EDIT_ADD:
					if (song != null) {
						invalidate();
						isAdding = true;
					}
					// handleAddElementTouch(e);
					break;

				case PLAY:
					onPlay(e);
					break;

				default:
					break;
				}
			// Log.d(TAG, "onShowPress");
		}


		/** handle scroll gesture for this view */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

            if(song == null) return false;

            Log.d(TAG, String.format("onScroll x: %d  y: %d", (int)distanceX, (int)distanceY));
/*
            Log.d(TAG, String.format(
                    "onScroll e1: %.0f/%.0f  e2: %.0f/%.0f  dX: %f  dY: %f",
                    e1.getX(), e1.getY(), e2.getX(), e2.getY(), distanceX,
                    distanceY));
*/

            if (!scaleDetector.isInProgress() && !elementIsDragged && !isAdding) {
                // invalidate();
                int xDistance = Math.round(distanceX);
                int yDistance = Math.round(distanceY);

                // scroll in x direction only
                // TODO: is always one call in the past, allows unwanted overscrolling
                if (Math.abs(xDistance) > Math.abs(yDistance)) {
                    // only scroll to right max the difference between view
                    // and song width
                    // only scroll to left if not already max left
                    if ((xDistance > 0 && getScrollX() < song.getWidth()
                            - getWidth())
                            || (xDistance < 0) && (getScrollX() > 0)) {

                        // avoid scrolling to negative
                        if ((xDistance < 0)
                                && ((getScrollX() + xDistance) < SCROLLSNAPDISTANCE))
                            // scroll to complete left
                            scrollTo(0, getScrollY());
                        else
                            // scroll horizontally
                            scrollBy(xDistance, 0);
                    }
                    // scroll in y direction only
                } else {
                    // only scroll down max the difference between view and
                    // song height
                    // only scroll up if not already on zop
                    if ((yDistance > 0 && getScrollY() < song
                            .getHeight() - getHeight() + verticalScrollOffset
                            + 2 * PageProperties.VERTICALMARGIN)
                            || (yDistance < 0) && (getScrollY() > 0)) {

                        // avoid overscrolling to negative
                        if ((yDistance < 0)
                                && ((getScrollY() + yDistance) < SCROLLSNAPDISTANCE))
                            // scroll to complete top
                            scrollTo(getScrollX(), 0);

                            // avoid overscrolling to positive
                        else if ((yDistance > 0)
                                && ((getScrollY() + yDistance) > (song
                                .getHeight() - getHeight())
                                + verticalScrollOffset - 10))
                            // scroll to complete bottom
                            scrollTo(getScrollX(), song.getHeight()
                                    - getHeight() + verticalScrollOffset
                                    + 2 * PageProperties.VERTICALMARGIN);
                        else
                            // scroll vertically
                            scrollBy(0, yDistance);
                    }
                }
            }
            return true;
		}


		/** handle long press gesture, used for drag and drop */
		@Override
		public void onLongPress(MotionEvent event) {

            if(song == null) return;

			if (mode == ScoreModes.EDIT_SELECT) {
				// prepare dragging
				Point point = new Point((int) event.getX() + getScrollX(),
						(int) event.getY() + getScrollY());

				if (song.selectElementAtPosition(point)) {

					// get handle on currently selected element
					ScoreElement draggedElement = song.getSelectedElement();

					if (draggedElement instanceof Draggable) {
						// start dragging
						// short vibration if possible
						// Vibrator vibrator = (Vibrator) context
						// .getSystemService(Service.VIBRATOR_SERVICE);
						// if (vibrator.hasVibrator() == true) {
						// synchronized (vibrator) {
						// vibrator.vibrate(500);
						// }
						// }
						elementIsDragged = true;

						// create bitmap of dragged element
						dragImage = new DragImage(draggedElement, point);

						// drop dragged element at current position
						song.copySelectedElement();

						invalidate();
					}

				}
				song.deselectElement();
			}
			// Log.d(TAG, "onLongPress");
		}


		/**
		 * handle double tap, open edit element dialog
		 */
		@Override
		public boolean onDoubleTap(MotionEvent event) {

            if(song == null) return false;

			if (mode == ScoreModes.EDIT_SELECT) {

				Point point = new Point((int) event.getX() + getScrollX(),
						(int) event.getY() + getScrollY());

                song.selectElementAtPosition(point);

                invalidate();
                // call edit handler
                onEditElement();
			}
			return true;
		}

        /**
         * single tap, flip pages in view mode
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            switch (mode) {

                case VIEW:
                    // flip page if user touches the first or last third of
                    // screen
                    if ((int) e.getX() >= (getWidth() / 3) * 2) {
                        browsePageForward();
                        onLayout(true, 0, 0, 0, 0);
                        invalidate();
                    } else if ((int) e.getX() <= (getWidth() / 3)) {
                        browsePageBack();
                        onLayout(true, 0, 0, 0, 0);
                        invalidate();
                    }
                    break;
            }
            return true;
        }

        /**
         * @see <a href="http://developer.android.com/training/custom-views/making-interactive.html">google</a>
         * @see <a href="http://www.tiemenschut.com/support-fling-gesture-bounce-effect/">tiemenshut</a>
         * @see <a href="http://stackoverflow.com/questions/4951142/smooth-scrolling-in-android">stackoverflow</a>
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(song == null) return false;

			// overScroller.forceFinished(true);
			int maxX = song.getWidth() - getWidth();
            // add some extra scroll to left for showing most right elements
            maxX += maxX / (FLING_OVERSCROLL_DIVIDER * 2);
            if(maxX < 0) maxX = 0;
			int maxY = song.getHeight() - getHeight() + verticalScrollOffset
					+ 2 * PageProperties.VERTICALMARGIN;
            if(maxY < 0) maxY = 0;

            // fling in one axis ony
            if (Math.abs(velocityX) > Math.abs(velocityY))
                velocityY = 0;
            else
                velocityX = 0;

			overScroller
					.fling(
                            getScrollX(), getScrollY(),
                            (int) -velocityX / FLING_VELOCITY_DIVIDER,
                            (int) -velocityY / FLING_VELOCITY_DIVIDER,
                            0, maxX, 0, maxY,
                            maxX / FLING_OVERSCROLL_DIVIDER, maxY / FLING_OVERSCROLL_DIVIDER);

			Log.d(TAG,
                    String.format(
                            "onFling:  x: %d  y: %d  velX: %d  velY: %d  minX: %d  maxX: %d  minY: %d  maxY: %d  overX: %d  overY: %d",
                            getScrollX(), getScrollY(),
                            (int) -velocityX / FLING_VELOCITY_DIVIDER,
                            (int) -velocityY / FLING_VELOCITY_DIVIDER,
                            0, maxX, 0, maxY,
                            maxX / FLING_OVERSCROLL_DIVIDER, maxY / FLING_OVERSCROLL_DIVIDER));
            postInvalidate();
			return true;
        }
    } // class GestureListener


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSaveInstanceState()
	 */
	@Override
	protected Parcelable onSaveInstanceState() {

		Log.d(TAG, "onSaveInstanceState: " + mode.toString());

		final Parcelable superState = super.onSaveInstanceState();

		// Save the instance state
		final SavedState myState = new SavedState(superState);
		myState.saveMode = getMode();
		myState.saveEditType = editElementType;
		myState.saveScrollOffset = verticalScrollOffset;
		myState.currentPage = currentPage;

		// remove selection on rotating, implement saving later!
		// if (song != null)
		// song.deselectElement();

		return myState;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {

		if (!state.getClass().equals(SavedState.class)) {
			// didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());

		Log.d(TAG, "onRestoreInstanceState: " + myState.saveMode.toString());

		setMode(myState.saveMode);
		setEditElementType(myState.saveEditType);
		setVerticalScrollOffset(myState.saveScrollOffset);
		setCurrentPage(myState.currentPage);
		// onLayout(true, 0, 0, 0, 0);
	}

	/**
	 * SavedState, a subclass of {@link BaseSavedState}, will store the state of
	 * scoreView It is important to always call through to super methods.
	 */
	private static class SavedState extends BaseSavedState {

		// mode this view is in
		ScoreModes saveMode;
		// type of element to edit in edit mode
		private Class<? extends ScoreElement> saveEditType;
		// vertical scroll offset
		private int saveScrollOffset;
		// displayed page
		private int currentPage;


		public SavedState(Parcel source) {

			super(source);

			// restore parameters
			saveMode = (ScoreModes) source.readSerializable();
			saveEditType = (Class<? extends ScoreElement>) source.readSerializable();
			saveScrollOffset = source.readInt();
			currentPage = source.readInt();
		}


		@Override
		public void writeToParcel(Parcel dest, int flags) {

			super.writeToParcel(dest, flags);

			// save the mode
			dest.writeSerializable(saveMode);
			dest.writeSerializable(saveEditType);
			dest.writeInt(saveScrollOffset);
			dest.writeInt(currentPage);
		}


		public SavedState(Parcelable superState) {

			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

			public SavedState createFromParcel(Parcel in) {

				return new SavedState(in);
			}


			public SavedState[] newArray(int size) {

				return new SavedState[size];
			}
		};
	}


} // class ScoreView
