/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

/**
 * The Staff, five lines with origin and name.
 * 
 * The relative position of the staff lines is uniform in a note system.
 * Therefore the NoteSystem class holds this information.
 * 
 * @author krein
 * 
 */
public class Staff extends ScoreElement {

	private static final int DEBUG_OFFSET = 0;

	/** the steps from staff y-origin to reference C, C5 for violin clef */
	private static final int CLEF_VIOLIN_REF_C_YPOS_STEPS = 3;
	/** octave of the reference C, C5 for violin clef */
	private static final int CLEF_VIOLIN_REF_C_OCTAVE = 5;
	/** the steps from staff y-origin to reference C, C3 for bass clef */
	private static final int CLEF_BASS_REF_C_YPOS_STEPS = 5;
	/** octave of the reference C, C5 for violin clef */
	private static final int CLEF_BASS_REF_C_OCTAVE = 3;

	/** number of vertical positioning steps for an octave */
	public static final int STEPS_PER_OCTAVE = 7;
	/** number of musical lines, maybe later 6 for guitar tabs ;-) */
	public static final int NUMBER_OF_LINES = 5;

	public static final float STAFF_LINE_WIDTH_TO_SPACING_RATIO = 0.12f;

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** paint style for all Staff objects */
	private static Paint paint;

	// TODO: these have same names as parent class variables :-(
	// TODO: all staves the same height?
	/** height of all staves */
	private static int height;

	/** width of all staves */
	private static int width;

	/** line spacing of all staves */
	private static int lineSpacing;

	/** time signature of this staff */
	@Element(name = "timeSignature")
	private TimeSignature timeSignature;

	/** number of NoteSystem this staff belongs to, zero for no system */
	private int systemNumber;

	/**
	 * widht of staff's signature elements, clef sign + key signature + time
	 * signature
	 */
	private int signatureWidth;

	/** key signature for staff */
	@Element(name = "keySignature")
	private KeySignature keySignature;

	/** clef sign for staff */
	@Element(name = "clefSign")
	private ClefSign clefSign;

	/** name of staff */
	@Element(name = "name")
	private String name;

	/**
	 * octave shift 0 means no octave shift, 1 stands for 8va, -1 stands for
	 * 8vb, etc
	 */
	@Attribute(name = "octaveShift")
	public int octaveShift;

	/** Initialize static class elements */
	static {
		Staff.paint = new Paint();
		Staff.paint.setColor(Color.BLACK);
		Staff.paint.setAntiAlias(true);
		Staff.paint.setStrokeWidth(lineSpacing
				* STAFF_LINE_WIDTH_TO_SPACING_RATIO);
		Staff.width = 0;
		Staff.height = 0;
		Staff.lineSpacing = PageProperties.STAFF_MINIMUM_LINESPACING;
	}


	// +++++++ getters and setters ++++++++
	// ++++++++++++++++++++++++++++++++++++

	/**
	 * Get height of all staves.
	 * 
	 * @return height of one staff in pixels
	 */
	public static int getStavesHeight() {
		return Staff.height;
	}


	/**
	 * Set height of all staves.
	 * 
	 * @param height
	 *            the height to set
	 */
	public static void setStavesHeight(int height) {
		Staff.height = height;
	}


	/**
	 * Set line spacing for all staves.
	 * 
	 * @param staffLineSpacing
	 *            new spacing between staff lines
	 */
	public static void setLineSpacing(int staffLineSpacing) {
		Staff.lineSpacing = staffLineSpacing;
	}


	/**
	 * Get width of all staves.
	 * 
	 * @return the width
	 */
	public static int getStavesWidth() {
		return Staff.width;
	}


	/**
	 * Get name of this staff (e.g. Piano left, Violin)
	 * 
	 * @return name of this staff
	 */
	public String getName() {
		return name;
	}


	/**
	 * Set width of all staves.
	 * 
	 * @param width
	 *            the width to set
	 */
	public static void setStavesWidth(int width) {
		Staff.width = width;
	}


	@Attribute(name = "systemNumber")
	public int getSystemNumber() {
		return systemNumber;
	}


	@Attribute(name = "systemNumber")
	public void setSystemNumber(int systemNumber) {
		this.systemNumber = systemNumber;
	}


	/**
	 * Get key signature of this staff
	 * 
	 * @return the KeySignature value
	 */
	public KeySignature getKeySignature() {
		return keySignature;
	}


	/**
	 * Set key signature of this staff
	 * 
	 * @param keySignature
	 *            new KeySignature object
	 */
	public void setKeySignature(KeySignature keySignature) {
		this.keySignature = keySignature;
	}


	/**
	 * Get time signature of this staff
	 * 
	 * @return the TimeSignature value of this staff
	 */
	public TimeSignature getTimeSignature() {
		return timeSignature;
	}


	/**
	 * Set clef signature of this staff
	 * 
	 * @param timeSignature
	 *            new TimeSignature object
	 */
	public void setClefSignature(ClefSign timeSignature) {
		this.clefSign = timeSignature;
	}


	/**
	 * Get clef signature of this staff
	 * 
	 * @return the TimeSignature value of this staff
	 */
	public ClefSign getClefSignature() {
		return clefSign;
	}


	/**
	 * Set time signature of this staff
	 * 
	 * @param timeSignature
	 *            new TimeSignature object
	 */
	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
	}


	/**
	 * Get width of signatures at start of first measure. Needed for placing
	 * other score elements after.
	 * 
	 * @return width of signature elements in pixel
	 */
	public int getSignatureWidht() {
		return signatureWidth;
	}


	/**
	 * Constructor of Staff class
	 * 
	 * @param name
	 *            name of staff
	 */
	public Staff(@Element(name = "name") String name,
			@Attribute(name = "octaveShift") int octaveShift,
			@Attribute(name = "systemNumber") int systemNumber,
			@Element(name = "keySignature") KeySignature keySignature,
			@Element(name = "clefSign") ClefSign clefSign,
			@Element(name = "timeSignature") TimeSignature timeSignature) {

		this.name = name;
		this.octaveShift = octaveShift;
		this.keySignature = keySignature;
		this.clefSign = clefSign;
		this.systemNumber = systemNumber;
		this.timeSignature = timeSignature;
		this.signatureWidth = 0;
	}


	/**
	 * Copy constructor of class.
	 * 
	 * @param from
	 *            object to copy from
	 */
	public Staff(Staff from) {
		// call constructor with values form existing object
		this(from.getName(), Staff.getStavesHeight(), Staff.getStavesWidth(),
				new KeySignature(from.keySignature),
				new ClefSign(from.clefSign), new TimeSignature(
						from.timeSignature));
	}


	/**
	 * Draw staff lines with whole page width.
	 * 
	 * @param canvas
	 *            canvas to draw on
	 */
	@Override
	public void drawSelf(Canvas canvas) {

		if (visible != true)
			return;

		// draw staff lines
		for (int lineNr = 0; lineNr < Staff.NUMBER_OF_LINES; lineNr++) {
			try {
				canvas.drawLine(getOrigin().x - DEBUG_OFFSET, getOrigin().y
						+ lineSpacing * lineNr, getOrigin().x + Staff.width
						+ DEBUG_OFFSET, getOrigin().y + lineSpacing * lineNr,
						paint);
			} catch (Exception e) {
				Log.d("Measure", e.getMessage());
			}
			// Log.d(TAG,String.format("Staffend: %d", origin.x + Staff.width +
			// DEBUG_OFFSET));
		}

		// draw all child elements
		clefSign.drawSelf(canvas);
		keySignature.drawSelf(canvas);
		timeSignature.drawSelf(canvas);

		// draw end of signatures for debugging
		// canvas.drawLine(
		// getOrigin().x + signatureWidth, getOrigin().y, getOrigin().x
		// + signatureWidth, getOrigin().y + height, paint);
	}


	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		// position clef sign with staff's origin
		// clefSign.setLayout(new Point(origin.x + lineSpacing / 2, origin.y
		// + lineSpacing / 2), lineSpacing);
		clefSign.setLayout(getOrigin(), lineSpacing);

		// position key signature with clef offset and clef dependent key note
		switch (clefSign.getClef())
			{

			case VIOLIN:
				// y-position for violin clef is the note G
				keySignature.setLayout(
						new Point(getOrigin().x + clefSign.getWidth()
								+ lineSpacing / 2, getOrigin().y + lineSpacing
								* 3), lineSpacing);
				break;

			case BASS:
				// y-position for bass clef is the note F
				keySignature.setLayout(
						new Point(getOrigin().x + clefSign.getWidth()
								+ lineSpacing / 2, getOrigin().y + lineSpacing
								* 4), lineSpacing);
				break;

			default:
				Log.d("Staff", "clefSing not supported!");
				break;
			}

		// position time signature
		timeSignature.setLayout(new Point(keySignature.getOrigin().x
				+ keySignature.width, getOrigin().y + lineSpacing * 2),
				lineSpacing);

		// get width of staff's signature elements
		signatureWidth = timeSignature.getOrigin().x + timeSignature.width
				+ timeSignature.width / 2 - getOrigin().x;

		Staff.paint.setStrokeWidth(lineSpacing
				* STAFF_LINE_WIDTH_TO_SPACING_RATIO);
	}


	/**
	 * Test if element is in the staff's area
	 * 
	 * Expands the staff's range with staffHeight above and below.
	 * 
	 * @see de.songeasy.android.score.ScoreElement#isIn(android.graphics.Point)
	 */
	@Override
	public boolean isIn(Point p) {

		// define area around element
		Rect area = new Rect(getOrigin().x, getOrigin().y - Staff.height,
				getOrigin().x + Staff.width, getOrigin().y + 2 * Staff.height);

		return area.contains(p.x, p.y);
	}


	/**
	 * Snap vertical coordinate to line grid of staff. Can not be static because
	 * staves have different y-origins.
	 * 
	 * @param yPos
	 *            y-value to snap
	 * @return snapped y-value
	 */
	public int snapToYGrid(int yPos) {

		float lineGrid = (float) Staff.lineSpacing / 2;

		// integer division with vertical snap grid (half line spacing), to get
		// the number of grid steps from staff origin
		int steps = Math.round((float) (yPos - getOrigin().y) / lineGrid);

		// move to snapped vertical grid starting from staff y-origin
		return Math.round(steps * lineGrid) + getOrigin().y;
	}


	/**
	 * Return note's y position by it's octave and key. Set the parameter
	 * stepsFromMiddleLine.
	 * 
	 * @param note
	 *            the note to positioning
	 * 
	 * @return the note's y value when drawn into this staff in pixels
	 */
	public int getNoteYPos(Note note) {

		int yPos = 0;

		// number of steps from staff's origin
		int steps = 0;

		// set pitch for different clefs
		switch (clefSign.getClef())
			{

			case VIOLIN:
				// calc steps from staff's origin
				steps = CLEF_VIOLIN_REF_C_YPOS_STEPS - note.getStepsFromName()
						+ (CLEF_VIOLIN_REF_C_OCTAVE - note.getOctave())
						* STEPS_PER_OCTAVE;
				break;

			case ALT:
				Log.d(TAG, "Clef not supported!");
				break;

			case TENOR:
				Log.d(TAG, "Clef not supported!");
				break;

			case BASS:
				steps = CLEF_BASS_REF_C_YPOS_STEPS - note.getStepsFromName()
						+ (CLEF_BASS_REF_C_OCTAVE - note.getOctave())
						* STEPS_PER_OCTAVE;
				break;

			default:
				Log.d(TAG, "Clef not supported!");
				break;
			}

		// return note's absolute y position in pixel
		yPos = Math.round((float) getOrigin().y + (float) steps
				* ((float) lineSpacing / 2));

		// Log.d(TAG, "Note Y-Pos: " + ((Integer) yPos).toString());

		// cut value to integer to get steps of line spacings
		try {

			note.setLineSpacingsFromMiddleLine(((getOrigin().y + lineSpacing * 2) - yPos)
					/ lineSpacing);
		} catch (ArithmeticException e) {
			Log.d(TAG, e.getMessage());
		}

		// Log.d(TAG,
		// "Steps from Middle:"
		// + ((Integer) note.stepsFromMiddleLine).toString());

		return yPos;
	}


	/**
	 * Report if note is placed on staff line.
	 * 
	 * @param note
	 * @return true if note is on staff line
	 */
	public boolean isNoteOnLine(Note note) {

		float distanceFromMiddleLine;
		int stepsFromMiddleLine;

		distanceFromMiddleLine = (float) (getOrigin().y + lineSpacing * 2)
				- note.getOrigin().y;

		stepsFromMiddleLine = (int) (distanceFromMiddleLine / (lineSpacing / 2));

		if ((stepsFromMiddleLine % 2) == 0)
			return true;
		else
			return false;
	}


	/**
	 * Set name and octave note by touch coordinate.
	 * 
	 * @param note
	 *            note to set the pitch by coordinate
	 */
	public void setPitchByPosition(Note note) {

		int yNoteStep = lineSpacing / 2;
		int stepsFromRefC = 0;

		switch (clefSign.getClef())
			{

			case VIOLIN:
				stepsFromRefC = Math
						.round(((getOrigin().y + CLEF_VIOLIN_REF_C_YPOS_STEPS
								* yNoteStep) - note.getOrigin().y)
								/ yNoteStep);

				if (stepsFromRefC < 0)
					note.setOctave(CLEF_VIOLIN_REF_C_OCTAVE - 1
							+ ((stepsFromRefC + 1) / STEPS_PER_OCTAVE));
				else
					note.setOctave(CLEF_VIOLIN_REF_C_OCTAVE
							+ (stepsFromRefC / STEPS_PER_OCTAVE));

				note.setName(Note.getRelativePitchFromSteps(stepsFromRefC
						% STEPS_PER_OCTAVE));
				break;

			case ALT:
				Log.d(TAG, "Clef not yet supported!");
				break;

			case TENOR:
				Log.d(TAG, "Clef not yet supported!");
				break;

			case BASS:
				stepsFromRefC = Math
						.round(((getOrigin().y + CLEF_BASS_REF_C_YPOS_STEPS
								* yNoteStep) - note.getOrigin().y)
								/ yNoteStep);

				if (stepsFromRefC < 0)
					note.setOctave(CLEF_BASS_REF_C_OCTAVE - 1
							- ((stepsFromRefC + 1) / STEPS_PER_OCTAVE));
				else
					note.setOctave(CLEF_BASS_REF_C_OCTAVE
							+ (stepsFromRefC / STEPS_PER_OCTAVE));

				note.setName(Note.getRelativePitchFromSteps(stepsFromRefC
						% STEPS_PER_OCTAVE));
				break;

			default:
				Log.d(TAG, "Clef not yet supported!");
				break;
			}

	}


	/**
	 * Draw leger line for note if it is positioned outside the staff. If a note
	 * is positioned more than a half line spacing above or below the staff
	 * lines leger line(s) have to be drawn. The leger line should stand out
	 * half line spacing. staff spacing.
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 * @param note
	 *            Note to draw leger lines for
	 */
	public void drawLegerLines(Canvas canvas, Note note) {

		int count = Math.abs(note.getLineSpacingsFromMiddleLine())
				- NUMBER_OF_LINES / 2;
		int legerLineLength = note.width + lineSpacing;
		int increment;
		int yPos = 0;

		if (note.getLineSpacingsFromMiddleLine() > 0) {
			// draw first line one line spacing above staff
			yPos = getOrigin().y - lineSpacing;
			increment = -lineSpacing;
		} else {
			// draw first line one line spacing below staff
			yPos = getOrigin().y + Staff.height + lineSpacing;
			increment = lineSpacing;
		}

		// draw lines
		while (count > 0) {

			canvas.drawLine(note.getOrigin().x - (legerLineLength / 2), yPos,
					note.getOrigin().x + (legerLineLength / 2), yPos, paint);
			yPos += increment;
			count--;
		}

	}

} // class
