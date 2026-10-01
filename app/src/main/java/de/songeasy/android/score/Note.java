/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.score;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import de.songeasy.android.interfaces.Draggable;

/**
 * Class for musical notes. Draws note head with stem and appropriate number of
 * flags. Origin of note is the center of the note head.
 * 
 * @author krein /jansen
 * 
 */
public class Note extends TimedElement implements Draggable {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** fraction representing relative time length of a 96th note (1/96) */
	private final static float NINETY_SIXTH_NOTE_FRACTION = 0.0104167f;
	// private final static float NINETY_SIXTH_NOTE_FRACTION = 1 /

	/** default text size for notes */
	public final static int NOTEREFERENCETEXTSIZE = 35;

	/** factor between stem line width and staff line spacing */
	private final static float STEM_LINEWIDTH_TO_STAFF_LINE_SPCACING_FACTOR = 0.15f;

	/** size of drawn cross when adding or dragging notes */
	public final static int NOTE_CROSS_SIZE = 150;

	/** applicable basic note names */
	// TODO: reduce to basic names
	public static enum NoteName {
		C, D, E, F, G, A, B
	};

	/** possible types of ties, connecting notes with curved lines */
	public static enum TieType {
		NONE, START, STOP
	};

	/** accidental of note */
	public static enum NoteAccidental {
		NONE, DOUBLE_FLAT, FLAT, NATURAL, SHARP, DOUBLE_SHARP
	};

	/** applicable directions of note stem */
	public static enum StemDirection {
		NONE, UPWARDS, DOWNWARDS
	}

	/** paint style for all Note objects */
	private static Paint paint;

	/** multiplier for correction of stem's x-position related to note head */
	private static float stemXOffsetMultiplier;

	/** width of accidentals */
	private static int accidentalSharpWidth;
	private static int accidentalFlatWidth;

	/** length of stem */
	private int stemLength;

	/** name of basic note value */
	@Attribute(name = "name")
	private NoteName name;

	/**
	 * octave the note is located in, goes from 0 to 8 according to midi
	 * standard, e.g. al value of 6 means the octave between C5 and C6, thus
	 * between C'' and C''', thus between midi key 72 and 84
	 */
	@Attribute(name = "octave")
	private int octave;

	/** accidental of note, from -2 (two bs) to +2 (two crosses) */
	@Attribute(name = "accidental")
	public NoteAccidental accidental;

	/**
	 * ties, that connects notes to sound with no interruption, a note can have
	 * max. two ties: one of type stop and one of type start
	 */
	@ElementList(required = false)
	private ArrayList<TieType> ties;

	/**
	 * beams, grouping notes of same length visually together first note in a
	 * beamed group has beam of type begin, last of type end
	 */
	@ElementList(required = false)
	private ArrayList<Beam> beams;

	/** indicator if note is drawn on a staff line */
	public boolean isOnLine;

	/** pitch value of note, referring to the midi standard, 60 = c' */
	@Attribute(name = "midiPitch", required = false)
	public int midiPitch;

	/** the voice this note belongs too, needed for staves with multiple voices */
	@Attribute(name = "voice")
	private int voice;

	/** the direction of this note's stem if existent, default is upwards */
	private StemDirection stemDirection;

	/**
	 * offset of stem in x direction, a little bit more than just half of note
	 * head
	 */
	private float stemXOffset;
	private float flagXOffset;
	/** distance in Y direction between flags */
	private int flagSpace;

	/**
	 * indicates whether this note has neighbour notes that draw flags, default
	 * is no
	 */
	private boolean hasFlaggedNeighbors;

	/**
	 * distance of note position in steps of lineSpacing from middle staff line,
	 * needed to compute necessary ledger lines
	 */
	private int lineSpacingsFromMiddleLine;

	/** the sign for drawing the note head */
	private char[] sign;

	static {

		Note.paint = new Paint();
		Note.paint.setAntiAlias(true);
		Note.paint.setColor(Color.BLACK);
		Note.paint.setTypeface(MusicFont.musicTypeface);
		Note.paint.setTextSize((float) NOTEREFERENCETEXTSIZE);
		Note.stemXOffsetMultiplier = MusicFont.NOTE_STEM_X_OFFSET_CORRECTION;
		Note.accidentalSharpWidth = 5;
		Note.accidentalFlatWidth = 5;
	}


	// ++++++++++++++++ setters, getters ++++++++++++++++++++
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Get array of beams this note belongs to.
	 * 
	 * @return array with beams
	 */
	public ArrayList<Beam> getBeams() {
		return beams;
	}


	/**
	 * Add new beam note should belong to.
	 * 
	 * @param newBeam
	 *            beam to add
	 */
	public void addBeam(Beam newBeam) {
		this.beams.add(newBeam);
	}


	/**
	 * Get ties of note.
	 * 
	 * @return the ties of this note, max. two are possible
	 */
	public ArrayList<TieType> getTies() {
		return ties;
	}


	/**
	 * Get the midi pitch of note.
	 * 
	 * @return the midiPitch
	 */
	public int getMidiPitch() {

		return midiPitch;
	}


	/**
	 * Set midi pitch, relative pitch and octave.
	 * 
	 * @param midiPitch
	 *            the midiPitch to set
	 */
	public void setMidiPitch(int midiPitch) {

		this.midiPitch = midiPitch;

		setNameAndOctaveByMidiPitch();

	}


	/**
	 * Get relative name of note.
	 * 
	 * @return note name
	 */
	public NoteName getName() {
		return name;
	}


	/**
	 * Set relative note name.
	 * 
	 * @param name
	 *            new name of note.
	 */
	public void setName(NoteName name) {
		this.name = name;
		setMidiPitchByNameAndOctave();
	}


	/**
	 * Get octave of note.
	 * 
	 * @return octave note is in
	 */
	public int getOctave() {
		return octave;
	}


	/**
	 * Set octave of note.
	 * 
	 * @param octave
	 *            new octave note is in
	 */
	public void setOctave(int octave) {
		this.octave = octave;
		setMidiPitchByNameAndOctave();
	}


	/**
	 * Get the note's vertical distance from staff's middle line in
	 * lineSpacings, can be positive or negative
	 * 
	 * @return the lineSpacings steps from staff's middle line
	 */
	public int getLineSpacingsFromMiddleLine() {
		return lineSpacingsFromMiddleLine;
	}


	/**
	 * Set the note's vertical distance from staff's middle line in
	 * lineSpacings, can be positive or negative
	 * 
	 * @param lineSpacingsFromMiddleLine
	 *            the lineSpacings steps from staff's middle line
	 */
	public void setLineSpacingsFromMiddleLine(int lineSpacingsFromMiddleLine) {
		this.lineSpacingsFromMiddleLine = lineSpacingsFromMiddleLine;
	}


	/**
	 * Get direction of stem for this note
	 * 
	 * @return the direction of the stem of this note
	 */
	public StemDirection getStemDirection() {
		return this.stemDirection;
	}


	/**
	 * Set new direction of note stem.
	 * 
	 * @param newDirection
	 *            new direction of stem
	 */
	public void setStemDirection(StemDirection newDirection) {
		this.stemDirection = newDirection;
	}


	/**
	 * Get direction of note stem.
	 * 
	 * @return direction
	 */
	public int getStemLength() {
		return stemLength;
	}


	/**
	 * Get distance in x direction of stem from note mid.
	 * 
	 * @return stem x offset in pixels
	 */
	public float getStemXOffset() {
		return this.stemXOffset;
	}


	/**
	 * Set length of note stem
	 * 
	 * @param stemLength
	 *            new length of stem
	 */
	public void setStemLength(int stemLength) {
		this.stemLength = stemLength;
	}


	/**
	 * Constructor for note with parameters
	 *
	 * @param name
	 *            basic name
	 * @param accidental
	 *            possible accidental
	 * @param octave
	 *         octave for note
	 * @param length
	 *            musical length
	 * @param isDotted
	 * 			if note is dotted
	 * @param timePosition
	 *          relative time position in measure
	 * @param staffNumber
	 *          number of staff note is related to
	 */
	public Note(@Attribute(name = "name") NoteName name,
			@Attribute(name = "accidental") NoteAccidental accidental,
			@Attribute(name = "octave") int octave,
			@Attribute(name = "length") NoteLength length,
			@Attribute(name = "isDotted") boolean isDotted,
			@Attribute(name = "timePosition") int timePosition,
			@Attribute(name = "staffNumber") int staffNumber) {

		this.name = name;
		this.accidental = accidental;
		this.octave = octave;
		this.setStaffNumber(staffNumber);
		this.setLength(length);
		this.isDotted = isDotted;
		this.setTimePosition(timePosition);
		lineSpacingsFromMiddleLine = 0;
		hasFlaggedNeighbors = false;
		voice = 0;
		stemDirection = StemDirection.UPWARDS;
		stemLength = 30;
		ties = new ArrayList<TieType>();
		beams = new ArrayList<Beam>();

		sign = new char[1];

		// select note symbol from font
		switch (length)
			{
			// open note head
			case FULL:
				sign = MusicFont.NoteSymbols.FULL.getCode();
				break;
			// open note head, maybe different?
			case HALF:
				sign = MusicFont.NoteSymbols.HALF.getCode();
				break;

			// filled note head
			case QUARTER:
			case EIGHTH:
			case SIXTEENTH:
			case THIRTYSECOND:
			case SIXTYFOURTH:
				sign = MusicFont.NoteSymbols.QUARTER.getCode();
				break;
			}
	}


	private void setNameAndOctaveByMidiPitch() {
		name = getRelativePitchByMidi(this.midiPitch);

		// TODO: update accidental according to pitch for C#, D#, etc.

		// integer arithmetic, cuts away decimals
		octave = (this.midiPitch / 12) - 1;

		// just in case of, better exception?
		if (octave < 0)
			octave = 0;
		if (octave > 8)
			octave = 8;

	}


	/**
	 * Set midi pitch according to note name and octave
	 */
	private void setMidiPitchByNameAndOctave() {

		midiPitch = (octave + 1) * 12;

		switch (name)
			{

			case C:
				midiPitch += 0;
				break;

			case D:
				midiPitch += 2;
				break;

			case E:
				midiPitch += 4;
				break;

			case F:
				midiPitch += 5;
				break;

			case G:
				midiPitch += 7;
				break;

			case A:
				midiPitch += 9;
				break;

			case B:
				midiPitch += 11;
				break;

			default:
				break;
			}
	}


	/**
	 * Get relative note pitch by midi note number.
	 * 
	 * @param midiPitch
	 *            midi note number according to midi standard
	 * @return relative pitch
	 */
	private static NoteName getRelativePitchByMidi(int midiPitch) {

		switch (midiPitch % 12)
			{
			case 0:
				return (NoteName.C);

			case 1:
				return (NoteName.C);

			case 2:
				return (NoteName.D);

			case 3:
				return (NoteName.D);

			case 4:
				return (NoteName.E);

			case 5:
				return (NoteName.F);

			case 6:
				return (NoteName.F);

			case 7:
				return (NoteName.G);

			case 8:
				return (NoteName.G);

			case 9:
				return (NoteName.A);

			case 10:
				return (NoteName.A);

			case 11:
				return (NoteName.B);

			default:
				return (NoteName.C);

			}
	}


	/**
	 * Returns the graphical steps away from the octave's C position. A step is
	 * half of the staff line's distance, it's the vertical the grid in which
	 * notes can be placed.
	 * 
	 * @return steps from C
	 */
	public int getStepsFromName() {

		switch (name)
			{

			case C:
				return (0);

			case D:
				return (1);

			case E:
				return (2);

			case F:
				return (3);

			case G:
				return (4);

			case A:
				return (5);

			case B:
				return (6);

			default:
				return (0);
			}
	}


	/**
	 * Returns the name of a new note depending of its distance from the C key.
	 * 
	 * @param stepsFromC
	 *            the distance from C in half the spacing of staff lines
	 * @return
	 */
	public static NoteName getRelativePitchFromSteps(int stepsFromC) {

		switch (stepsFromC)
			{

			case -6:
				return NoteName.D;

			case -5:
				return NoteName.E;

			case -4:
				return NoteName.F;

			case -3:
				return NoteName.G;

			case -2:
				return NoteName.A;

			case -1:
				return NoteName.B;

			case 0:
				return NoteName.C;

			case 1:
				return NoteName.D;

			case 2:
				return NoteName.E;

			case 3:
				return NoteName.F;

			case 4:
				return NoteName.G;

			case 5:
				return NoteName.A;

			case 6:
				return NoteName.B;

			default:
				return NoteName.C;
			}

	}


	/**
	 * Compares if note is higher, lower or same sounding than other note.
	 * Compares octave and note name.
	 * 
	 * @param other
	 *            , note to compare with
	 * @return 0 if same, -1 if lower than other, 1 if higher than other
	 */
	public int compareWith(Note other) {
		// check octave first
		if (this.getOctave() > other.getOctave())
			return 1;

		if (this.getOctave() < other.getOctave())
			return -1;

		// octave is same, check for note name
		if (this.getName().ordinal() > other.getName().ordinal())
			return 1;

		if (this.getName().ordinal() < other.getName().ordinal())
			return -1;

		// octave an name are same
		return 0;
	}


	/**
	 * Add new tie to note. Max. one start and one stop are allowed.
	 * 
	 * @param newTie
	 *            tie to add
	 */
	public void addTie(TieType newTie) {

		// max. two ties are allowed
		if (getTies().size() > 1)
			return;

		// check for already existing tie start

		if (newTie == TieType.START) {
			if (getTies().contains(TieType.START))
				return;
		}

		// check for already existing tie stop
		if (newTie == TieType.STOP) {
			if (getTies().contains(TieType.STOP))
				return;
		}

		getTies().add(newTie);
	}


	@Override
	public void drawSelf(Canvas canvas) {

		if (visible != true)
			return;

		int flagCounter = 0;

		// draw flags only if note is not part of beam group
		if (getBeams().isEmpty()) {
			// draw flag(s)
			switch (getLength())
				{
				// draw flags according to length values first
				case SIXTYFOURTH:

					if (!hasFlaggedNeighbors) {
						if (stemDirection == StemDirection.UPWARDS) {
							canvas.drawText(
									MusicFont.NoteSymbols.UPFLAG.getCode(), 0,
									1, getOrigin().x + flagXOffset,
									getOrigin().y - stemLength + flagSpace
											* flagCounter++, Note.paint);
						} else {
							canvas.drawText(
									MusicFont.NoteSymbols.DOWNFLAG.getCode(),
									0, 1, getOrigin().x - flagXOffset,
									getOrigin().y + stemLength - flagSpace
											* flagCounter++, Note.paint);
						}
					} // fall through, draw all with quarter note head

				case THIRTYSECOND:

					if (!hasFlaggedNeighbors) {
						if (stemDirection == StemDirection.UPWARDS) {

							canvas.drawText(
									MusicFont.NoteSymbols.UPFLAG.getCode(), 0,
									1, getOrigin().x + flagXOffset,
									getOrigin().y - stemLength + flagSpace
											* flagCounter++, Note.paint);
						} else {
							canvas.drawText(
									MusicFont.NoteSymbols.DOWNFLAG.getCode(),
									0, 1, getOrigin().x - flagXOffset,
									getOrigin().y + stemLength - flagSpace
											* flagCounter++, Note.paint);
						}
					} // fall through, draw all with quarter note head

				case SIXTEENTH:
					if (!hasFlaggedNeighbors) {
						if (stemDirection == StemDirection.UPWARDS) {
							canvas.drawText(
									MusicFont.NoteSymbols.UPFLAG.getCode(), 0,
									1, getOrigin().x + flagXOffset,
									getOrigin().y - stemLength + flagSpace
											* flagCounter++, Note.paint);
						} else {
							canvas.drawText(
									MusicFont.NoteSymbols.DOWNFLAG.getCode(),
									0, 1, getOrigin().x - flagXOffset,
									getOrigin().y + stemLength - flagSpace
											* flagCounter++, Note.paint);

						}
					} // fall through, draw all with quarter note head

				case EIGHTH:
					if (!hasFlaggedNeighbors) {
						if (stemDirection == StemDirection.UPWARDS) {
							canvas.drawText(
									MusicFont.NoteSymbols.UPFLAG.getCode(), 0,
									1, getOrigin().x + flagXOffset,
									getOrigin().y - stemLength + flagSpace
											* flagCounter++, Note.paint);
						} else {
							canvas.drawText(
									MusicFont.NoteSymbols.DOWNFLAG.getCode(),
									0, 1, getOrigin().x - flagXOffset,
									getOrigin().y + stemLength - flagSpace
											* flagCounter++, Note.paint);
						}
					}
					break;
				}
		}

		// draw head with stem
		switch (getLength())
			{
			case SIXTYFOURTH:
			case THIRTYSECOND:
			case SIXTEENTH:
			case EIGHTH:
			case QUARTER:
				// head
				canvas.drawText(MusicFont.NoteSymbols.QUARTER.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Note.paint);
				// stem
				if (stemDirection == StemDirection.UPWARDS) {
					canvas.drawLine(getOrigin().x + stemXOffset, getOrigin().y,
							getOrigin().x + stemXOffset, getOrigin().y
									- stemLength, Note.paint);
				} else {
					canvas.drawLine(getOrigin().x - stemXOffset, getOrigin().y,
							getOrigin().x - stemXOffset, getOrigin().y
									+ stemLength, Note.paint);
				}
				break;

			// draw head with stem
			case HALF:
				canvas.drawText(MusicFont.NoteSymbols.HALF.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Note.paint);
				// draw stem
				if (stemDirection == StemDirection.UPWARDS) {
					canvas.drawLine(getOrigin().x + stemXOffset, getOrigin().y,
							getOrigin().x + stemXOffset, getOrigin().y
									- stemLength, Note.paint);
				} else {
					canvas.drawLine(getOrigin().x - stemXOffset, getOrigin().y,
							getOrigin().x - stemXOffset, getOrigin().y
									+ stemLength, Note.paint);
				}
				break;

			// draw head
			case FULL:
				canvas.drawText(MusicFont.NoteSymbols.FULL.getCode(), 0, 1,
						getOrigin().x, getOrigin().y, Note.paint);
				break;

			}

		// we only draw accidental if not at the end of a tie
		boolean drawAccidental = true;

		for (TieType tie : this.getTies()) {

			if (tie == TieType.STOP) {
				drawAccidental = false;
			}
		}

		if (drawAccidental == true) {
			// draw accidentals
			switch (accidental)
				{

				case NONE:
					break;

				case DOUBLE_SHARP:
					canvas.drawText(
							MusicFont.AccidentalSymbols.CROSS.getCode(),
							0,
							1,
							getOrigin().x - width - accidentalSharpWidth * 1.5f,
							getOrigin().y, Note.paint);
					// fall through to single sharp

				case SHARP:
					canvas.drawText(
							MusicFont.AccidentalSymbols.CROSS.getCode(),
							0,
							1,
							getOrigin().x - width - accidentalSharpWidth * 0.5f,
							getOrigin().y, Note.paint);
					break;

				case DOUBLE_FLAT:
					canvas.drawText(
							MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
							getOrigin().x - width - accidentalFlatWidth * 1.5f,
							getOrigin().y, Note.paint);
					// fall through to single flat

				case FLAT:
					canvas.drawText(
							MusicFont.AccidentalSymbols.BSIGN.getCode(), 0, 1,
							getOrigin().x - width - accidentalFlatWidth * 0.5f,
							getOrigin().y, Note.paint);
					break;

				case NATURAL:
					canvas.drawText(
							MusicFont.AccidentalSymbols.NATURAL.getCode(), 0,
							1, getOrigin().x - width - accidentalSharpWidth
									* 0.5f, getOrigin().y, Note.paint);
					break;

				default:
					break;
				}
		}

		if (selected == true)
			drawSelectionRect(canvas);

		// drawDebugRect(canvas);
	}


	/**
	 * Draw augmentation dot right of note.
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 * @param xOffSet
	 *            offset in x direction, needed if note is part of chord
	 * @param yOffSet
	 *            offset in y direction, needed if note is placed on staff line
	 */
	public void drawAugmentationDot(Canvas canvas, int xOffSet, int yOffSet) {

		canvas.drawText(MusicFont.NoteSymbols.DOT.getCode(), 0, 1,
				getOrigin().x + width / 2 + width / 4, getOrigin().y + yOffSet,
				Note.paint);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.escore.ScoreElement#isIn(android.graphics.Point)
	 */
	@Override
	public boolean isIn(Point p) {

		// define area around element
		Rect area = new Rect(getOrigin().x - (width / 2) - SELECTIONEXTENSION,
				getOrigin().y - (height / 2) - SELECTIONEXTENSION,
				getOrigin().x + (width / 2) + SELECTIONEXTENSION, getOrigin().y
						+ (height / 2) + SELECTIONEXTENSION);

		return area.contains(p.x, p.y);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.escore.ScoreElement#drawDebugRect(android.graphics.Canvas)
	 */
	@Override
	public void drawDebugRect(Canvas canvas) {

		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		Rect area = new Rect(getOrigin().x - (width / 2), getOrigin().y
				- (height / 2), getOrigin().x + (width / 2), getOrigin().y
				+ (height / 2));

		canvas.drawRect(area, paint);
	}


	/**
	 * Overridden because origin of note is center of note head.
	 * 
	 * @see de.songeasy.android.score.ScoreElement#drawSelectionRect(Canvas)
	 */
	@Override
	public void drawSelectionRect(Canvas canvas) {

		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLUE);
		paint.setAntiAlias(true);
		paint.setAlpha(PageProperties.SELECT_RECT_ALPHA);

		RectF area = new RectF(
				(float) (getOrigin().x - (width / 2) - PageProperties
						.getDrawselectionextension()),
				(float) (getOrigin().y - (height / 2) - PageProperties
						.getDrawselectionextension()), (float) (getOrigin().x
						+ (width / 2) + PageProperties
						.getDrawselectionextension()), (float) (getOrigin().y
						+ (height / 2) + PageProperties
						.getDrawselectionextension()));

		canvas.drawRoundRect(area, PageProperties.SELECT_RECT_ROUND_RADIUS,
				PageProperties.SELECT_RECT_ROUND_RADIUS, paint);
	}


	/**
	 * Set position of note.
	 * 
	 * Compute room taken by note.
	 */
	@Override
	public void setLayout(Point origin, int lineSpacing) {

		super.setLayout(origin, lineSpacing);

		// adjust size of note symbol corresponding to space between staff lines
		Note.paint.setTextSize((float) lineSpacing
				* MusicFont.SYMBOL_TO_STAFF_LINE_SPACING_FACTOR);

		// set width of stem lines
		Note.paint.setStrokeWidth(lineSpacing
				* STEM_LINEWIDTH_TO_STAFF_LINE_SPCACING_FACTOR);

		// compute room of note
		// TODO add space for possible dot and accidental
		Rect bounds = new Rect();

		Note.paint.getTextBounds(sign, 0, sign.length, bounds);

		width = bounds.width();
		height = bounds.height();

		// set x offset of stem lines and flags
		stemXOffset = ((float) width / 2) * Note.stemXOffsetMultiplier;
		flagXOffset = ((float) width / 2) * Note.stemXOffsetMultiplier;

		// set space between flags
		flagSpace = Math.round(Note.paint.getTextSize() / 6);

		// set distance of flats
		Note.paint.getTextBounds(MusicFont.AccidentalSymbols.BSIGN.getCode(),
				0, 1, bounds);
		accidentalFlatWidth = bounds.width();

		// set distance of sharps
		Note.paint.getTextBounds(MusicFont.AccidentalSymbols.CROSS.getCode(),
				0, 1, bounds);
		accidentalSharpWidth = bounds.width();

		// shift note right by half of it's width
		// this.origin.x += width / 2;

		// set beam direction and length
		// if part of a beam group these parameters are set up by externally
		if (getBeams().isEmpty()) {
			// draw stem downwards if note is placed on or above 3rd line
			// (middle)
			if (lineSpacingsFromMiddleLine >= 0)
				setStemDirection(StemDirection.DOWNWARDS);
			else
				setStemDirection(StemDirection.UPWARDS);

			// stem length is one octave = 7 times half lines spacing
			setStemLength((lineSpacing * 7) / 2);
		}
	};


	/**
	 * Get the note's relative fraction of a measure.
	 * 
	 * 1.0 equals the length of a full note
	 * 
	 * @return the note's relative time fraction
	 */
	public float getFraction() {

		float fraction = 0.0f;

		switch (getLength())
			{

			case FULL:
				fraction = 1.0f;
				break;

			case HALF:
				if (isDotted)
					fraction = 0.75f;
				else
					fraction = 0.5f;
				break;

			case QUARTER:
				if (isDotted)
					fraction = 0.375f;
				else
					fraction = 0.25f;
				break;

			case EIGHTH:
				if (isDotted)
					fraction = 0.1875f;
				else
					fraction = 0.125f;
				break;

			case SIXTEENTH:
				if (isDotted)
					fraction = 0.09375f;
				else
					fraction = 0.0625f;
				break;

			case THIRTYSECOND:
				if (isDotted)
					fraction = 0.046875f;
				else
					fraction = 0.03125f;
				break;

			case SIXTYFOURTH:
				if (isDotted)
					fraction = 0.0234375f;
				else
					fraction = 0.015625f;
				break;
			}

		return fraction;
	}


	/**
	 * Increment pitch of note by one full note.
	 */
	public void incrementPitch() {

		if (name == NoteName.B) {
			octave += 1;
			name = NoteName.C;
		} else {

			name = NoteName.values()[name.ordinal() + 1];
		}
		midiPitch += 1;
	}


	/**
	 * Decrement pitch of note by one full note.
	 */
	public void decrementPitch() {

		if (name == NoteName.C) {
			octave -= 1;
			name = NoteName.B;
		} else {

			name = NoteName.values()[name.ordinal() - 1];
		}
		midiPitch -= 1;
	}


	/**
	 * Compute how many 96th notes represent this note
	 * 
	 * @return Equivalent 96th notes
	 */
	public int equivalent96thNotes() {

		return Math.round(getFraction() / NINETY_SIXTH_NOTE_FRACTION);
	}


	@Override
	public void drawDrag(Canvas canvas, Point position) {

		int xVectorDiff = position.x - getOrigin().x;
		int yVectorDiff = position.y - getOrigin().y;

		// move positions by vector difference
		getOrigin().offset(xVectorDiff, yVectorDiff);

		drawSelf(canvas);

		// restore original positions
		getOrigin().offset(-xVectorDiff, -yVectorDiff);

	}

} // class

