/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import java.util.ArrayList;
import java.util.Collections;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
import de.songeasy.android.score.Beam.Type;
import de.songeasy.android.score.Note.StemDirection;
import de.songeasy.android.score.Note.TieType;

/**
 * A song is divided in measures. A measure contains all the musical elements
 * like staves, chords, notes, rests, lyrics, etc.
 * 
 * @author krein
 */
public class Measure extends ScoreElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private static float REPEAT_BAR_WIDTH_TO_STAFF_LINESPACING_RATIO = 0.45f;
	private static float LINES_WIDTH_TO_STAFF_LINESPACING_RATIO = 0.15f;

	/** step width of annotation horizontal movement */
	private static float ANNOTATION_HORIZONTAL_STEP_PERCENTAGE = 0.05f;

	/** indication for position in line */
	public static enum LinePosition {

		/** Measure starts a new line */
		FIRST_IN_LINE,
		/** Measure ends a line */
		LAST_IN_LINE,
	}

	/** optional attributes for measures */
	public static enum MeasureAttribute {
		/**
		 * Measure is last before a new line starts, replacement for
		 */
		LINE_BREAK,
		/** Measure introduces new key signature */
		KEY_CHANGE,
		/** Measure introduces new time signature */
		TIME_CHANGE,
		/** Measure is last before a new page starts */
		PAGE_BREAK
	}

	/** types of repeat signs of measure */
	public static enum RepeatType {
		/** no repeat */
		NO_REPEAT,
		/** start of repeating section */
		START,
		/** stop of repeating section */
		STOP,
		/** stop of repeating section and start of another one */
		BOTH,
		/** end of song */
		ENDING
	}

    /** directions for moving or selecting of elements */
	public static enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

    /** paint style for all Measure objects */
	private static Paint paint;

	/** staves in this measure */
	@ElementList
	private ArrayList<Staff> staves;

	/** notes in this measure */
	private ArrayList<Note> notes;

	/** dynamically created groups of notes connected by a beam */
	private ArrayList<ArrayList<Note>> beamGroups;

	/** dynamically created list of ties between two notes */
	private ArrayList<Tie> ties;

	/** rests in this measure */
	@ElementList(required = false)
	private ArrayList<Rest> rests;

	/** annotations in this measure */
	@ElementList(required = false)
	private ArrayList<AnnotationElement> annotations;

	/** directives in this measure */
	@ElementList(required = false)
	private ArrayList<DirectionElement> directions;

	/** repeat type of this measure */
	private RepeatType repeat;

	/** additional attribute for this measure */
	// TODO: use BitSet for allowing multiple attributes?
	private ArrayList<MeasureAttribute> attributes;

	/** element selected for editing in this measure */
	private ScoreElement selectedElement;

	/** finer horizontal placement grid in measure for elements (96th) */
	private float horizontalPlacingWidth;

	/** coarser horizontal grid for moving of elements (32th) */
	private int horizontalAddingWidth;

	/** horizontal start of staff in measure */
	private int xStaffStart;

    /** vertical start of staffs in measure */
    private int yStaffStart;

    /** height of staffs area in measure */
    private int yStaffHeight;

	/** horizontal end position of measure */
	private int xStaffEnd;

   	/** horizontal start for position of elements */
	private int xElementStart;

	/** horizontal end for position of elements */
	private int xElementEnd;

	/** link to next measure */
	private Measure nextMeasure;

	/** for special treatment of last and first measure in line */
	private ArrayList<LinePosition> linePosition;

	/** static initialization of measure's elements */
	static {
		Measure.paint = new Paint();
		Measure.paint.setAntiAlias(true);
		Measure.paint.setColor(Color.BLACK);
		Measure.paint.setTextSize(30);
        // still needed? Tie is own class now!
		Measure.paint.setStyle(Paint.Style.STROKE);
	}


	// ++++++++++ getters and setters +++++++++++
	// ++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Get reference to measure's staves.
	 * 
	 * @return staves reference
	 */
	public ArrayList<Staff> getStaves() {
		return staves;
	}


	@ElementList(name = "attribute", required = false)
	public ArrayList<MeasureAttribute> getAttributes() {
		return this.attributes;
	}


	public Measure getNextMeasure() {
		return nextMeasure;
	}


	public void setNextMeasure(Measure nextMeasure) {
		this.nextMeasure = nextMeasure;
	}


	/**
	 * Sets attributes of this measure. Old attributes will be overwritten.
	 * 
	 * @param attributes
	 *            ArrayList of type MeasureAttribute
	 */
	@ElementList(name = "attribute", required = false)
	public void setAttributes(ArrayList<MeasureAttribute> attributes) {

		this.attributes = attributes;
	}


	@Element(name = "repeat", required = false)
	public RepeatType getRepeat() {
		return repeat;
	}


	@Element(name = "repeat", required = false)
	public void setRepeat(RepeatType repeat) {
		this.repeat = repeat;
	}


	public ScoreElement getSelectedElement() {
		return selectedElement;
	}


	public ArrayList<LinePosition> getLinePosition() {
		return linePosition;
	}


	public void setLinePosition(ArrayList<LinePosition> linePosition) {
		this.linePosition = linePosition;
	}


	public void addLinePosition(LinePosition position) {
		this.linePosition.add(position);
	}


	/**
	 * This measure starts with a new key. Mark measure with KEY_CHANGE
	 * attribute and change key signature of all staves.
	 * 
	 * @param newKey
	 *            the new KeySignature of this measure's staves
	 */
	public void setKeyChange(KeySignature newKey) {
		attributes.add(MeasureAttribute.KEY_CHANGE);

		// check if key is different from current one
		if (!newKey.getKeyName().equals(
				staves.get(0).getKeySignature().getKeyName())) {

			setKey(newKey);
		}
	}


	/**
	 * Set KeySignature of measure. As each measure has is set of staves, design
	 * flaw?, each one has to be changed here. By now, each staff gets the same
	 * key.
	 * 
	 * @param newKey
	 *            the new KeySignature of this measure's staves
	 */
	public void setKey(KeySignature newKey) {

		// give all staves a new KeySignature object
		for (Staff staff : staves) {
			staff.setKeySignature(new KeySignature(newKey));
		}
	}


	/**
	 * Change time signature of measure. Mark measure with TIME_CHANGE attribute
	 * and change time signature of all staves.
	 * 
	 * @param newTime
	 *            new TimeSignature of measure's staves
	 */
	public void setTimeChange(TimeSignature newTime) {
		attributes.add(MeasureAttribute.TIME_CHANGE);

		for (Staff staff : staves) {
			staff.setTimeSignature(newTime);
		}
	}


	/**
	 * Get notes in this measure.
	 * 
	 * @return ArrayList with notes
	 */
	@ElementList(name = "notes")
	public ArrayList<Note> getNotes() {
		return this.notes;
	}


	/**
	 * Get rests in this measure
	 * 
	 * @return ArrayList with rests
	 */
	public ArrayList<Rest> getRests() {
		return this.rests;
	}


	// +++++++++++++ Constructors +++++++++++++++
	// ++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Parameterless constructor. Creates empty measure
	 */
	public Measure() {
		// Initialize variables
		notes = new ArrayList<Note>();
		rests = new ArrayList<Rest>();
		staves = new ArrayList<Staff>();
		annotations = new ArrayList<AnnotationElement>();
		directions = new ArrayList<DirectionElement>();
		attributes = new ArrayList<MeasureAttribute>();
		beamGroups = new ArrayList<ArrayList<Note>>();
        ties = new ArrayList<Tie>();
		linePosition = new ArrayList<LinePosition>();
		repeat = RepeatType.NO_REPEAT;
	}


	/**
	 * Constructor with given notes for deserialization.
	 * 
	 * @param notes
	 *            Notes for new measure
	 */
	public Measure(@ElementList(name = "notes") ArrayList<Note> notes) {
		this();
		this.notes = notes;
		// create groups of beamed notes and add remaining levels
		// createBeamGroups();
	}


	/**
	 * Try to select ScoreElement of measure at given position.
	 * 
	 * @param p
	 *            position to find element
	 * @return true if found
	 */
	public boolean selectElementAtPosition(Point p) {

		// order of checks determines selection priority!
		for (Note note : notes) {

			if (note.isIn(p)) {
				selectedElement = note;
				return true;
			}
		}

		for (Rest rest : rests) {

			if (rest.isIn(p)) {
				selectedElement = rest;
				return true;
			}
		}

		for (AnnotationElement element : annotations) {

			if (element.isIn(p)) {
				selectedElement = element;
				return true;
			}
		}

		for (DirectionElement direction : directions) {

			if (direction.isIn(p)) {
				selectedElement = direction;
				return true;
			}
		}

		for (AnnotationElement annotation : annotations) {
			if (annotation.isIn(p)) {
				selectedElement = annotation;
				return true;
			}
		}

		return false;
	}


	/**
	 * Set selected element variable to null.
	 */
	public void deselectElement() {
		selectedElement = null;
	}


	/**
	 * Remove element from measure:
	 * 
	 * @param element
	 *            ScoreElement to remove
	 */
	// TODO: use visitor pattern?
	public void removeElement(ScoreElement element) {

		if (element.getClass().equals(Note.class)) {
			notes.remove(element);
			// sort by time values
			Collections.sort(notes);
			// create groups of beamed notes and add remaining levels
			// createBeamGroups();
		}

		if (element.getClass().equals(Rest.class)) {
			rests.remove(element);
			// sort by time values
			Collections.sort(rests);
		}

		if (element instanceof AnnotationElement) {

			annotations.remove(element);
			// sort by relative position
			Collections.sort(annotations);
		}

		if (element instanceof DirectionElement) {

			directions.remove(element);
			// sort by relative position
			Collections.sort(directions);
		}

	}


	/**
	 * Add a new note at given position. Replaces already existing note at that
	 * position.
	 * 
	 * @param point
	 *            coordinate to add or remove note at
	 * @param newNote
	 *            new note to add
	 */
	public Note addNoteAtPosition(Point point, Note newNote) {

		int staffNumber = 0;

		// determine staff of new note
		for (Staff staff : staves) {

			if (staff.isIn(point)) {

				newNote.setStaffNumber(staffNumber);

				newNote.setOrigin(point);

				// snap note to vertical and horizontal grids
				snapElementToUserGrid(newNote, staff);

				// delete already existing note at position
				for (Note oldNote : notes) {
					if (oldNote.getOrigin().y == newNote.getOrigin().y
							&& oldNote.getTimePosition() == newNote
									.getTimePosition()) {
						notes.remove(oldNote);
						Log.d(TAG, "Removed note at adding position");
						break;
					}
				}

				// set note name and octave by it's position
				staff.setPitchByPosition(newNote);

				notes.add(newNote);

				// sort by time values
				Collections.sort(notes);
				// create groups of beamed notes and add remaining levels
				// createBeamGroups();

				// Log.d(
				// TAG,
				// String.format(
				// "Added note %s at %d",
				// newNote.name.toString(),
				// newNote.getOrigin().x));

				return newNote;
			}

			staffNumber++;
		}

		Log.d(TAG, "no staff found for new note: " + point.toString());

		return null;
	}


	/**
	 * Add rest at given position.
	 * 
	 * @param point
	 *            coordinate to add rest at
	 * @param newRest
	 *            new rest to add
	 */
	public Rest addRestAtPosition(Point point, Rest newRest) {

		int staffNumber = 0;

		// determine staff of new rest
		for (Staff staff : staves) {

			if (staff.isIn(point)) {

				newRest.setStaffNumber(staffNumber);

				newRest.setOrigin(point);

				// snap rest to vertical and horizontal grids
				snapElementToUserGrid(newRest, staff);

				rests.add(newRest);
				// sort by time values
				Collections.sort(rests);

				return newRest;
			}

			staffNumber++;
		}

		Log.d(TAG, "no staff found for new rest: " + point.toString());

		return null;
	}


	// TODO: one move function for both TimedElements ????

	/**
	 * Move note inside staff.
	 * 
	 * @param note
	 *            Note to move
	 * @param direction
	 *            direction in which to move note, (up,down,left,right)
	 */
	public void moveNoteInDirection(Note note, Direction direction) {

		switch (direction)
			{

			case RIGHT:
				// increment x position by one user grid
				note.setTimePosition(note.getTimePosition() + 1);

				// keep note inside this measure
				if (note.getTimePosition() > PageProperties.HORIZONTAL_PLACING_GRID)
					note.setTimePosition(PageProperties.HORIZONTAL_PLACING_GRID);
				break;

			case LEFT:
				// move one user grid left
				note.setTimePosition(note.getTimePosition() - 1);

				// keep note inside this measure
				if (note.getTimePosition() < 0)
					note.setTimePosition(0);
				break;

			case UP:
				if (note.getLineSpacingsFromMiddleLine() < 5)
					note.incrementPitch();
				break;

			case DOWN:
				if (note.getLineSpacingsFromMiddleLine() > -5)
					note.decrementPitch();
				break;

			default:
				break;
			}
	}


	/**
	 * Move rest inside staff. Different function for rest as it supports no
	 * vertical movement.
	 * 
	 * @param rest
	 *            Rest to move
	 * @param direction
	 *            direction in which to move rest, (up,down,left,right)
	 */
	public void moveRestInDirection(Rest rest, Direction direction) {
		switch (direction)
			{

			case RIGHT:
				// add percentage of user grid
				rest.setTimePosition(rest.getTimePosition() + 1);

				// keep note inside this measure
				if (rest.getTimePosition() > PageProperties.HORIZONTAL_PLACING_GRID)
					rest.setTimePosition(PageProperties.HORIZONTAL_PLACING_GRID);
				break;

			case LEFT:
				// one user grid left
				rest.setTimePosition(rest.getTimePosition() - 1);

				// keep note inside this measure
				if (rest.getTimePosition() < 0)
					rest.setTimePosition(0);
				break;

			default:
				break;
			}
	}


	/**
	 * Move AnnotationElement in requested direction.
	 * 
	 * @param annotation
	 *            AnnotationElement to move
	 * @param direction
	 *            direction in which to move
	 */
	public void moveAnnotationInDirection(AnnotationElement annotation,
			Direction direction) {

		switch (direction)
			{

			case LEFT:
				annotation.relativePosition -= ANNOTATION_HORIZONTAL_STEP_PERCENTAGE;
				if (annotation.relativePosition < 0)
					annotation.relativePosition = 0.0f;
				break;

			case RIGHT:
				annotation.relativePosition += ANNOTATION_HORIZONTAL_STEP_PERCENTAGE;
				if (annotation.relativePosition > 1.0f)
					annotation.relativePosition = 1.0f;
				break;

			default:
				break;
			}
	}


	public void moveDirectionInDirection(DirectionElement element,
			Direction direction) {

		switch (direction)
			{

			case LEFT:
				element.relativePosition -= ANNOTATION_HORIZONTAL_STEP_PERCENTAGE;
				if (element.relativePosition < 0)
					element.relativePosition = 0.0f;
				break;

			case RIGHT:
				element.relativePosition += ANNOTATION_HORIZONTAL_STEP_PERCENTAGE;
				if (element.relativePosition > 1.0f)
					element.relativePosition = 1.0f;
				break;

			default:
				break;
			}
	}


	/**
	 * Find next note in given direction.
	 * 
	 * @param note
	 *            note to start from
	 * @param direction
	 *            direction in which to find the next note
	 * @return next note in direction, null otherwise
	 */
	public Note findNoteInDirection(Note note, Direction direction) {

		if (notes.isEmpty())
			return null;

		int actualIndex = notes.indexOf(note);

		// note not found in this measure
		// return first or last note
		if (actualIndex < 0)
			switch (direction)
				{
				case LEFT:
					return notes.get(notes.size() - 1);

				case RIGHT:
					return notes.get(0);

				default:
					return null;
				}

		switch (direction)
			{

			case UP:
			case LEFT:
				actualIndex--;
				break;

			case DOWN:
			case RIGHT:
				actualIndex++;
				break;

			default:
				break;
			}

		try {
			return (notes.get(actualIndex));
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}


	/**
	 * Find next rest in given direction.
	 * 
	 * @param rest
	 *            rest to start from
	 * @param direction
	 *            direction in which to find the next rest
	 * @return next rest in direction, null otherwise
	 */
	public Rest findRestInDirection(Rest rest, Direction direction) {

        if (rests.isEmpty())
            return null;

        int actualIndex = rests.indexOf(rest);

        // rest not found in this measure
        // return first or last rest
        if (actualIndex < 0)
            switch (direction)
            {
                case LEFT:
                    return rests.get(rests.size() - 1);

                case RIGHT:
                    return rests.get(0);

                default:
                    return null;
            }

        switch (direction)
        {

            case UP:
            case LEFT:
                actualIndex--;
                break;

            case DOWN:
            case RIGHT:
                actualIndex++;
                break;

            default:
                break;
        }

        try {
            return (rests.get(actualIndex));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
	}


	/**
	 * Find next AnnotationElement in requested direction.
	 * 
	 * @param annotation
	 *            AnnotationElement to search from
	 * @param direction
	 *            direction in which to search
	 * @return next AnnotationElement in direction, null if not found
	 */
	public AnnotationElement findAnnotationInDirection(
			AnnotationElement annotation, Direction direction) {

        if (annotations.isEmpty())
            return null;

		int actualIndex = annotations.indexOf(annotation);

        // annotation not found in this measure
        // return first or last one
        if (actualIndex < 0)
            switch (direction)
            {
                case LEFT:
                    return annotations.get(annotations.size() - 1);

                case RIGHT:
                    return annotations.get(0);

                default:
                    return null;
            }

        switch (direction)
        {
            case UP:
            case LEFT:
                actualIndex--;
                break;

            case DOWN:
            case RIGHT:
                actualIndex++;
                break;

            default:
                break;
        }

        try {
            return (annotations.get(actualIndex));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
	}


	/**
	 * Snap note or rest origin to vertical grids and set timePosition spapped
	 * to horizontal grid. Vertical grid is half the line spacing of staff.
	 * Horizontal grid is the horizontal placing grid defined in PageProperties.
	 * 
	 * @param element
	 *            Element to position
	 * @param staff
	 *            Staff note is positioned in
	 */
	private void snapElementToUserGrid(TimedElement element, Staff staff) {

		// Log.d(TAG, "before snap: " + ((Integer) element.origin.y).toString()
		// + ", " + ((Integer) element.timePosition).toString());

		Point snapOrigin = new Point(element.getOrigin().x,
				element.getOrigin().y);
		// snap y position to staff's line grid
		snapOrigin.y = staff.snapToYGrid(snapOrigin.y);

		// use coarser placing grid for touch
		snapOrigin.x = snapToHorizontalAddingGrid(snapOrigin.x);

		element.setOrigin(snapOrigin);

		// set elements's time position in percent
		element.setTimePosition(Math.round((float) (element.getOrigin().x - xElementStart)
				* PageProperties.HORIZONTAL_PLACING_GRID
				/ (float) (xElementEnd - xElementStart)));

		// Log.d(TAG, "after snap: " + ((Integer) element.origin.y).toString()
		// + ", " + ((Integer) element.timePosition).toString());
	}


	/**
	 * Snap x-value to horizontal placing grid
	 * 
	 * @param xPos
	 *            x-position to snap
	 * @return snapped x-position
	 */
	private int snapToHorizontalPlacingGrid(int xPos) {

		// how much grid steps do fit into the distance?
		int steps = Math.round((float) (xPos - xElementStart)
				/ horizontalPlacingWidth);

		// multiply steps with grid
		return (int) (steps * horizontalPlacingWidth + xElementStart);
	}


	/**
	 * Snap x-value to horizontal user grid, coarser then placement grid
	 * 
	 * @param xPos
	 *            x-position to snap
	 * @return snapped x-position
	 */
	private int snapToHorizontalAddingGrid(int xPos) {

		// how much grid steps do fit into the distance?
		int steps = Math.round((float) (xPos - xElementStart)
				/ (float) horizontalAddingWidth);

		// multiply steps with grid
		return (steps * horizontalAddingWidth) + xElementStart;
	}


	/**
	 * Add new attribute to measure
	 * 
	 * @param attribute
	 *            attribute to add
	 */
	public void addAttribute(MeasureAttribute attribute) {

		if (!attributes.contains(attribute)) {
			attributes.add(attribute);
		}

	}


	/**
	 * Remove attribute from attributes list of measure.
	 * 
	 * @param attribute
	 *            attribute to remove
	 */
	public void removeAttribute(MeasureAttribute attribute) {

		attributes.remove(attribute);
	}


	/**
	 * Add staff to measure. Only needed for dynamic creation of default song.
	 * 
	 * @param staff
     *      staff to add to measure
	 */
	public void addStaff(Staff staff) {

		staves.add(staff);
	}


	/**
	 * Add a note to measure
	 * 
	 * @param note
	 *            Note to add
	 * @param staffNumber
	 *            number of staff note belongs to, starting with 0
	 */
	public void addNote(Note note, int staffNumber) {

		staffNumber = note.getStaffNumber();
		notes.add(note);
		// sort by time values
		Collections.sort(notes);
		// create groups of beamed notes and add remaining levels
		// createBeamGroups();
	}


	/**
	 * Add rest to measure.
	 * 
	 * @param rest
	 *            Rest to add
	 * @param staffNumber
	 *            Number of staff rest is placed in.
	 */
	public void addRest(Rest rest, int staffNumber) {

		staffNumber = rest.getStaffNumber();
		rests.add(rest);
		// sort by time values
		Collections.sort(rests);
	}


	/**
	 * Add AnnotationElement to measure
	 * 
	 * @param annotation
	 *            element to add
	 */
	public void addAnnotation(AnnotationElement annotation) {

		if (annotation.getClass() == Lyric.class) {

			// set staff for lyric
			for (Staff staff : staves) {

				if (staff.isIn(annotation.getOrigin())) {
					((Lyric) annotation).setStaffNumber(staves.indexOf(staff));
				}
			}
		}

		annotations.add(annotation);
		Collections.sort(annotations);
		redistributeAnnotations();
	}


	public ArrayList<AnnotationElement> getAnnotations() {
		return this.annotations;
	}


	/**
	 * Add DirectionElement to measure
	 * 
	 * @param direction
	 *            element to add
	 */
	public void addDirections(DirectionElement direction) {

		directions.add(direction);
		Collections.sort(directions);
		redistributeDirections();

	}


	public ArrayList<DirectionElement> getDirectionElements() {
		return this.directions;
	}


	/**
	 * Draw Measure with its elements
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 */
	@Override
	public void drawSelf(Canvas canvas) {

		if (!visible)
			return;

		// no staves defined, return
		if (staves.size() < 1)
			return;

		// drawDebugRect(canvas);

		// draw left bar line and staff lines if we are the first measure in
		// line or the key changes
		if (linePosition.contains(LinePosition.FIRST_IN_LINE)
				|| attributes.contains(MeasureAttribute.KEY_CHANGE)) {

            // draw left bar line (from upper to lower with one staff height
            // space)
            canvas.drawLine(xStaffStart,
                    yStaffStart, xStaffStart,
                    getOrigin().y + height
                            - PageProperties.INTER_SYSTEM_EXTRA_DISTANCE_FACTOR * staffLineSpacing
                            - Staff.getStavesHeight(), paint);

            // draw all staves with properties relative to this measure's
            // origin.
            for (Staff staff : staves) {

                staff.drawSelf(canvas);
            }

            // TODO draw system brace for all staves belonging to a system
        }

		// draw right bar line always (from upper to lower with one staff height
		// space)
		canvas.drawLine(xStaffEnd, yStaffStart,
				xStaffEnd, getOrigin().y + height
                        - PageProperties.INTER_SYSTEM_EXTRA_DISTANCE_FACTOR * staffLineSpacing
                        - Staff.getStavesHeight(),
				paint);

		// draw left repeat bracket for indicating start of repeat
		if (repeat == RepeatType.START || repeat == RepeatType.BOTH) {

			// left barlines are drawn after signatures if repeat starts
			int xrepeatStart = xElementStart - staffLineSpacing;

			// draw first broad vertical line
			paint.setStrokeWidth(staffLineSpacing
					* REPEAT_BAR_WIDTH_TO_STAFF_LINESPACING_RATIO);

			canvas.drawLine(xrepeatStart,
					yStaffStart, xrepeatStart,
					getOrigin().y + yStaffHeight, paint);

			// reset line width, better use separate paint?
			paint.setStrokeWidth(staffLineSpacing
					* LINES_WIDTH_TO_STAFF_LINESPACING_RATIO);

			// draw second thin line right of broad line
			canvas.drawLine(xrepeatStart + staffLineSpacing / 2, getOrigin().y
					+ Staff.getStavesHeight(), xrepeatStart + staffLineSpacing
					/ 2, getOrigin().y + yStaffHeight,
					paint);

			// draw two dots
			paint.setStyle(Style.FILL);

			for (Staff staff : staves) {

				canvas.drawCircle(xrepeatStart + staffLineSpacing,
						staff.getOrigin().y + (3 * staffLineSpacing) / 2,
						staffLineSpacing / 4, paint);
				canvas.drawCircle(xrepeatStart + staffLineSpacing,
						staff.getOrigin().y + (5 * staffLineSpacing) / 2,
						staffLineSpacing / 4, paint);
			}
			paint.setStyle(Style.STROKE);
		}

		// draw right repeat bracket for indicating start of repeat
		if (repeat == RepeatType.STOP || repeat == RepeatType.BOTH) {

			// draw first broad vertical line
			paint.setStrokeWidth(staffLineSpacing
					* REPEAT_BAR_WIDTH_TO_STAFF_LINESPACING_RATIO);

			canvas.drawLine(xStaffEnd, yStaffStart,
					xStaffEnd,
					getOrigin().y + yStaffHeight, paint);

			// reset line width, better use separate paint?
			paint.setStrokeWidth(staffLineSpacing
					* LINES_WIDTH_TO_STAFF_LINESPACING_RATIO);

			// draw second thin line left of broad line
			canvas.drawLine(xStaffEnd - staffLineSpacing / 2, getOrigin().y	+ Staff.getStavesHeight(),
					xStaffEnd - staffLineSpacing / 2,
                    getOrigin().y + yStaffHeight, paint);

			// draw two dots for each staff
			paint.setStyle(Style.FILL);

			for (Staff staff : staves) {

				canvas.drawCircle(xStaffEnd - staffLineSpacing,
						staff.getOrigin().y + (3 * staffLineSpacing) / 2,
						staffLineSpacing / 4, paint);
				canvas.drawCircle(xStaffEnd - staffLineSpacing,
						staff.getOrigin().y + (5 * staffLineSpacing) / 2,
						staffLineSpacing / 4, paint);
			}
			paint.setStyle(Style.STROKE);
		}

		// draw fine bar at ending
		if (repeat == RepeatType.ENDING) {

			// draw first broad vertical line
			paint.setStrokeWidth(staffLineSpacing
					* REPEAT_BAR_WIDTH_TO_STAFF_LINESPACING_RATIO);

			canvas.drawLine(xStaffEnd, yStaffStart,
					xStaffEnd,
					getOrigin().y + yStaffHeight, paint);

			// reset line width
			paint.setStrokeWidth(staffLineSpacing
					* LINES_WIDTH_TO_STAFF_LINESPACING_RATIO);

			// draw second thin line left of broad line
			canvas.drawLine(xStaffEnd - staffLineSpacing / 2,
                    yStaffStart,
					xStaffEnd - staffLineSpacing / 2,
                    getOrigin().y + yStaffHeight, paint);
		}

		// draw notes with leger lines
		// TODO: note's should draw themselves completely, tell them in layout
		// how to draw ledger lines etc.

		for (Note note : notes) {

			// draw note
			note.drawSelf(canvas);

			// draw possible ledger lines
			if (Math.abs(note.getLineSpacingsFromMiddleLine()) > 2)
				staves.get(note.getStaffNumber()).drawLegerLines(canvas, note);

			// draw possible augmentation dot with y-offset if on staff line
			// has to be done here, because note does not know not about the
			// staff's clef it is in
			if (note.isDotted) {
				if (staves.get(note.getStaffNumber()).isNoteOnLine(note))
					// draw dot above staff line
					note.drawAugmentationDot(canvas, 0, -(staffLineSpacing / 2));
				else
					note.drawAugmentationDot(canvas, 0, 0);
			}

			// note.drawDebugRect(canvas);
		}

		drawNoteBeams(canvas);

        // draw previously created ties
        for (Tie tie :ties) {
            tie.drawSelf(canvas);
        }

		// draw rests
		for (Rest rest : rests) {

			// Log.d(TAG, String.format(
			// "draw rest %d at: %d %d", rests.indexOf(rest),
			// rest.origin.x, rest.origin.y));
			rest.drawSelf(canvas);
		}

		// draw annotations
		for (AnnotationElement annotation : annotations) {

			annotation.drawSelf(canvas);
		}

		// draw directions
		for (DirectionElement direction : directions) {

			direction.drawSelf(canvas);
		}

		if (selected)
			drawSelectionRect(canvas);

		// drawDebugRect(canvas);
	}


	/**
	 * Draw beams for all grouped notes in this measure.
	 * 
	 * @param canvas
	 *            Canvas to draw on
	 */
	private void drawNoteBeams(Canvas canvas) {

		for (ArrayList<Note> group : beamGroups) {
			// get first and last note of group
			Note first = group.get(0);
			Note last = group.get(group.size() - 1);
			ArrayList<Note> subGroup = new ArrayList<Note>();

			// compute slope of all beams
			float slopeFactor = (float) (last.getOrigin().y - first.getOrigin().y)
					/ (last.getOrigin().x - first.getOrigin().x);
			int slope = Math.round(slopeFactor * first.getWidth());

			// draw all possible beam levels, from eighth to sixtyforth
			for (int level = 0; level < 5; level++) {

				for (Note note : group) {
					boolean hasBeamLevel = false;

					// check if next note has beam of that level
					for (Beam beam : note.getBeams()) {
						// add to group if has same level
						if (beam.getLevel() == level) {
							subGroup.add(note);
							hasBeamLevel = true;
						}
					}
					// check if beam ends with this note
					if (!hasBeamLevel) {
						if (!subGroup.isEmpty())
							// draw beam sub group, must be single note or
							// partial beam
							drawSubBeamGroup(canvas, subGroup, level, slope);
					}
				}
				if (!subGroup.isEmpty())
					// draw beam sub group, must be full beam or last note
					drawSubBeamGroup(canvas, subGroup, level, slope);
			}
		}
	}


	private void drawSubBeamGroup(Canvas canvas, ArrayList<Note> subGroup,
			int level, int slope) {

		if (subGroup == null)
			return;

		if (subGroup.isEmpty())
			return;

		// get first and last note of group
		Note first = subGroup.get(0);
		Note last = subGroup.get(subGroup.size() - 1);

		// determine stem direction
		StemDirection direction = first.getStemDirection();
		// height of beam is half of staff line distance
		int beamHeigth = (direction == StemDirection.UPWARDS) ? staffLineSpacing / 2
				: -staffLineSpacing / 2;
		// width of broken beams a.k.a. hooks
		int beamWidth = Math.round(first.getWidth()
				* Beam.NOTE_BEAM_WIDTH_FACTOR);

		Path beamPath = new Path();

		// handle single note hook
		if (subGroup.size() < 2) {
			// draw broken beams with one note head width

			// get parameters, depending on direction of stems
			int stemLength = (direction == StemDirection.UPWARDS) ? first
					.getStemLength() : -first.getStemLength();
			float stemXOffSet = (direction == StemDirection.UPWARDS) ? first
					.getStemXOffset() : -first.getStemXOffset();
			int beamLevelOffset = (direction == StemDirection.UPWARDS) ? staffLineSpacing
					* level
					: -staffLineSpacing * level;

			switch (first.getBeams().get(level).getType())
				{
				case BACKWARD_HOOK:
					beamPath.reset();
					// move to top of note's stem
					beamPath.moveTo(first.getOrigin().x + stemXOffSet,
							first.getOrigin().y - stemLength + beamLevelOffset);
					// move to left
					beamPath.lineTo(first.getOrigin().x + stemXOffSet
							- beamWidth, first.getOrigin().y - stemLength
							+ beamLevelOffset - slope);
					// move down half of staffline spacing
					beamPath.lineTo(first.getOrigin().x + stemXOffSet
							- beamWidth, first.getOrigin().y - stemLength
							+ beamLevelOffset - slope + beamHeigth);
					// move back to stem
					beamPath.lineTo(first.getOrigin().x + stemXOffSet,
							first.getOrigin().y - stemLength + beamLevelOffset
									+ beamHeigth);
					beamPath.close();
					Measure.paint.setStyle(Paint.Style.FILL_AND_STROKE);
					canvas.drawPath(beamPath, Measure.paint);
					Measure.paint.setStyle(Paint.Style.STROKE);
					break;

				case FORWARD_HOOK:
					beamPath.reset();
					// move to top of note's stem
					beamPath.moveTo(first.getOrigin().x + stemXOffSet,
							first.getOrigin().y - stemLength + beamLevelOffset);
					// move to right
					beamPath.lineTo(first.getOrigin().x + stemXOffSet
							+ beamWidth, first.getOrigin().y - stemLength
							+ beamLevelOffset + slope);
					// move down half of staffline spacing
					beamPath.lineTo(first.getOrigin().x + stemXOffSet
							+ beamWidth, first.getOrigin().y - stemLength
							+ beamLevelOffset + slope + beamHeigth);
					// move back to stem
					beamPath.lineTo(first.getOrigin().x + stemXOffSet,
							first.getOrigin().y - stemLength + beamLevelOffset
									+ beamHeigth);
					beamPath.close();
					Measure.paint.setStyle(Paint.Style.FILL_AND_STROKE);
					canvas.drawPath(beamPath, Measure.paint);
					Measure.paint.setStyle(Paint.Style.STROKE);
					break;

				default:
					break;
				}
		}
		// draw beam spreading over several notes
		else {
			// get parameters, depending on direction of stems
			int stemLengthFirst = (direction == StemDirection.UPWARDS) ? first
					.getStemLength() : -first.getStemLength();
			int stemLengthLast = (direction == StemDirection.UPWARDS) ? last
					.getStemLength() : -last.getStemLength();
			float stemXOffSetFirst = (direction == StemDirection.UPWARDS) ? first
					.getStemXOffset() : -first.getStemXOffset();
			float stemXOffSetLast = (direction == StemDirection.UPWARDS) ? last
					.getStemXOffset() : -last.getStemXOffset();
			int beamLevelOffset = (direction == StemDirection.UPWARDS) ? staffLineSpacing
					* level
					: -staffLineSpacing * level;

			beamPath.reset();
			// move to top of first note's stem
			beamPath.moveTo(first.getOrigin().x + stemXOffSetFirst,
					first.getOrigin().y - stemLengthFirst + beamLevelOffset);
			// move to top of last note's stem
			beamPath.lineTo(last.getOrigin().x + stemXOffSetFirst,
					last.getOrigin().y - stemLengthLast + beamLevelOffset);
			// move down half of staffline spacing
			beamPath.lineTo(last.getOrigin().x + stemXOffSetLast,
					last.getOrigin().y - stemLengthLast + beamHeigth
							+ beamLevelOffset);
			// move back to first note down
			beamPath.lineTo(first.getOrigin().x + stemXOffSetLast,
					first.getOrigin().y - stemLengthFirst + beamHeigth
							+ beamLevelOffset);
			beamPath.close();
			Measure.paint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawPath(beamPath, Measure.paint);
			Measure.paint.setStyle(Paint.Style.STROKE);

		}
		subGroup.clear();
	}


	/**
	 * Dynamical collect notes beamed together into the same beam group. Search for notes
	 * beamed together by looking for complete beam with begin, continue and end
	 * on level zero. Other levels are created by addRemainingBeamLevels
	 * afterwards.
	 */
	private void createBeamGroups() {

		ArrayList<Note> beamNotes = new ArrayList<Note>();
		boolean groupDetected = false;

		// discard all existing groups
		beamGroups.clear();

        for(Staff staff: staves ) {
            int staffNumber = staves.indexOf(staff);

            for (Note note : notes) {

                // find note with beams
                if (note.getBeams().isEmpty())
                    continue;

                // find note with current staffnumber
                if(note.getStaffNumber() != staffNumber)
                    continue;

                // delete all previous beams first, starting below level 1
                note.getBeams().subList(1, note.getBeams().size()).clear();

                // try to find first of beamed notes
                if (!groupDetected) {
                    if (note.getBeams().get(0).getType() == Type.BEGIN) {
                        // create new beam group
                        beamNotes = new ArrayList<Note>();
                        beamNotes.add(note);
                        groupDetected = true;
                    }
                } else {
                    // find more notes
                    if (note.getBeams().get(0).getType() == Beam.Type.CONTINUE) {
                        beamNotes.add(note);
                    } else {
                        // find last note
                        if (note.getBeams().get(0).getType() == Beam.Type.END) {
                            beamNotes.add(note);
                            // save group
                            if (beamNotes.size() > 1)
                                beamGroups.add(beamNotes);
                            groupDetected = false;
                        }
                    }
                }
            }
        }
		addRemainingBeamLevels();
	}

    /** Dynamical create ties between notes for later drawing */
	private void createTies() {

        // reset all ties
        ties.clear();

        for (Note note : notes) {

            if (note.getTies().contains(TieType.START)) {

                // next note with same pitch
                Note noteStop = null;

                // try to find matching note in this measure
                noteStop = findNextNoteWithSamePitch(note);

                if (noteStop != null) {
                    // second note must be placed right from first note
                    if (noteStop.getTimePosition() < note.getTimePosition())
                        noteStop = null;
                    else {
                        ties.add(new Tie(note, noteStop));
                        // add stop tie to second note, needed for MusicXML export only!
                        noteStop.addTie(Note.TieType.STOP);
                    }
                }

                // try to find note in next measure
                if (noteStop == null) {
                    if (nextMeasure != null)
                        noteStop = nextMeasure.findNextNoteWithSamePitch(note);

                    if (noteStop != null) {
                        // if we are last measure in line, draw just tie start
                        if (linePosition.contains(LinePosition.LAST_IN_LINE))
                            noteStop = null;

                        ties.add(new Tie(note, noteStop));
                        // add stop tie to second note, needed for MusicXML export only!
                        if(noteStop != null)
                            noteStop.addTie(Note.TieType.STOP);
                    }
                }
            }
        }
    }


	/**
	 * Find note with the same pitch value.
	 * 
	 * @param note
	 *            note with pitch to find
	 * @return note found, null otherwise
	 */
	public Note findNextNoteWithSamePitch(Note note) {

		for (Note checkNote : notes) {

			// check for same relative pitch, midi pitch relies on song key!
			if (checkNote.compareWith(note) == 0) {
				// check for same object
				if (!checkNote.equals(note))
					return checkNote;
			}
		}

		return null;
	}


	/**
	 * Set layout parameters of measure
	 * 
	 * @param origin
	 *            origin of this measure
     * @param lineSpacing
     *              lineSpacing of score
	 * @param width
	 *            available width for measure
	 * @param height
	 *            available height for measure
	 */
	@Override
	public void setLayout(Point origin, int lineSpacing, int width, int height) {

		super.setLayout(origin, lineSpacing, width, height);

		paint.setStrokeWidth(lineSpacing
				* LINES_WIDTH_TO_STAFF_LINESPACING_RATIO);

		// set horizontal start and end point for elements in staff
		xStaffStart = getOrigin().x;
		xElementStart = xStaffStart + staffLineSpacing / 2;
		// adjust start if measure is first in line
		if (linePosition.contains(LinePosition.FIRST_IN_LINE)) {
			xStaffStart += PageProperties.LINESMARGIN;
		}

		xStaffEnd = getOrigin().x + width;
		xElementEnd = xStaffEnd - staffLineSpacing;
		// adjust end if measure is last in line
		if (linePosition.contains(LinePosition.LAST_IN_LINE)) {
			xStaffEnd -= PageProperties.LINESMARGIN;
		}

		// positioning all staves
		redistributeStaves();

		// correct elements starting position after reditributeStaves()
		if (linePosition.contains(LinePosition.FIRST_IN_LINE)) {
			// first measure contains key and time signature
			xElementStart = xStaffStart + staves.get(0).getSignatureWidht()
					+ (staffLineSpacing * 3) / 2;
		}

        yStaffStart = getOrigin().y + Staff.getStavesHeight();
        yStaffHeight = height
                - PageProperties.INTER_SYSTEM_EXTRA_DISTANCE_FACTOR * staffLineSpacing
                - Staff.getStavesHeight();

		// set placing step width for timed elements
		horizontalPlacingWidth = (float) (xElementEnd - xElementStart)
				/ PageProperties.HORIZONTAL_PLACING_GRID;
		horizontalAddingWidth = Math
				.round((float) (xElementEnd - xElementStart)
						/ PageProperties.HORIZONTAL_ADDING_GRID);

		// Log.d(TAG,String.format("onLayout start: %d stop: %d  width: %d  horizontalPlacingWidth: %f",
		// xPositionStart, xPositionEnd, width,
		// horizontalPlacingWidth));

		// compute annotations placement
		redistributeAnnotations();

		// compute directions placement
		redistributeDirections();

		// compute positions of notes
		redistributeNotes();

        createTies();

        layoutTies();

		// create groups of beamed notes for later drawing
		createBeamGroups();

		// compute layout of note beams
		layoutBeamGroups();

		// compute rest placement
		redistributeRests();

		// TODO: use visitor pattern for redistribution of ScoreElements?
	}


	/**
	 * Positioning of staves contained by this measure.
	 * 
	 * The height of the staves is determined previously inside page. The
	 * distance between two staves is double the staves' height.
	 */
	private void redistributeStaves() {

		if (staves.size() < 1)
			return;

		Point staffStart = new Point();

		int line = 1;

		for (Staff staff : staves) {

			// x position is that of measure
			staffStart.x = xStaffStart;

			// y position is that of measure plus distance of one staff height
			staffStart.y = getOrigin().y + Staff.getStavesHeight() * line;

			staff.setLayout(staffStart, staffLineSpacing);

			// add distance to next staff of two times the staves' height
			line += PageProperties.STAVES_DISTANCE_FACTOR;
		}
	}


	/**
	 * Position chords according to their parameters.
	 */
	private void redistributeChord(Chord chord) {

		Point chordPosition = new Point();

		chordPosition.y = getOrigin().y
				+ Staff.getStavesHeight()
				- (int) (staffLineSpacing * PageProperties.CHORDS_DISTANCE_FROM_STAFF_RATIO);
		chordPosition.x = getOrigin().x
				+ (int) (width * chord.relativePosition);
		chord.setLayout(chordPosition, staffLineSpacing);
	}


	/**
	 * Reposition lyrics on change of layout.
	 */
	private void redistributeLyric(Lyric lyric) {

		Point lyricPosition = new Point();
		Point staffOrigin = new Point();

		// get the y-origin of the staff the lyric belongs to
		staffOrigin = staves.get(lyric.getStaffNumber()).getOrigin();

		lyricPosition.y = staffOrigin.y + Staff.getStavesHeight();
		lyricPosition.y += (int) (staffLineSpacing * PageProperties.LYRICS_DISTANCE_FROM_STAFF_RATIO);

		// recompute lyric height before arranging them in lines
		lyric.computeSize(staffLineSpacing);

		lyricPosition.y += (staffLineSpacing
				* PageProperties.LYRICS_LINE_DISTANCE_RATIO + lyric.getHeight())
				* lyric.getLineNumber();

		lyricPosition.x = getOrigin().x
				+ (int) (width * lyric.relativePosition);

		lyric.setLayout(lyricPosition, staffLineSpacing);
	}


	/**
	 * Reposition label annotations
	 */
	private void redistributeLabel(Label label) {

		Point labelPosition = new Point();

		labelPosition.y = getOrigin().y
				+ Staff.getStavesHeight()
				- (int) (staffLineSpacing * PageProperties.LABEL_DISTANCE_FROM_STAFF_RATIO);
		labelPosition.x = getOrigin().x
				+ (int) (width * label.relativePosition);
		label.setLayout(labelPosition, staffLineSpacing);

	}


	/**
	 * Reposition annotations, Chord, Label, Lyric
	 */
	private void redistributeAnnotations() {

		for (AnnotationElement element : annotations) {
			if (element.getClass() == Label.class)
				redistributeLabel((Label) element);

			if (element.getClass() == Chord.class)
				redistributeChord((Chord) element);

			if (element.getClass() == Lyric.class)
				redistributeLyric((Lyric) element);
		}
	}


	/**
	 * Reposition directions
	 */
	private void redistributeDirections() {

		Point directionPosition = new Point();

		for (DirectionElement direction : directions) {

			directionPosition.y = getOrigin().y
					+ Staff.getStavesHeight()
					- (int) (staffLineSpacing * PageProperties.DIRECTION_DISTANCE_FROM_STAFF_RATIO);
			directionPosition.x = getOrigin().x
					+ (int) (width * direction.getRelativePosition());
			direction.setLayout(directionPosition, staffLineSpacing);
		}
	}


	/**
	 * Recompute note's positions
	 */
	private void redistributeNotes() {

		// if no staves are defined we can not positioning any notes
		if (staves.size() < 1)
			return;

		Point notePosition = new Point();
		Staff noteStaff;

		for (Note note : notes) {

			noteStaff = staves.get(note.getStaffNumber());
			// set y position according to staff it belongs to and set
			// stepsFromMiddleLine
			notePosition.y = noteStaff.getNoteYPos(note);

			// set x-position in steps of placing width
			notePosition.x = (int) (xElementStart + note.getTimePosition()
					* horizontalPlacingWidth);

			// snap x-position into grid
			notePosition.x = snapToHorizontalPlacingGrid(notePosition.x);

			// position note
			// notes don't care about height for now
			note.setLayout(notePosition, staffLineSpacing);

			// Log.d(TAG, String.format( "distribute note %s at %d",
			// note.name.toString(),
			// notePosition.x));

			// TODO compute x position dependent of note value of predecessor on
			// same pitch
		}
	}


	/**
	 * Add remaining beams to all notes being part of a beam group. Level zero
	 * of beams must already exist with BEGIN, (CONTINUE), END to form a beam
	 * group. Adding remaining beams appropriate to the length of notes.
	 * Algorithm looks forward and creates sub beam groups. If next note is
	 * smaller, group is closed and set up.
	 */
	private void addRemainingBeamLevels() {

		for (ArrayList<Note> group : beamGroups) {

			TimedElement.NoteLength currentLength;
			ArrayList<Note> subGroup = new ArrayList<Note>();

			int level = 0;

			// iterate over remaining lengths lower than quarter
			for (int n = TimedElement.NoteLength.EIGHTH.ordinal(); n > TimedElement.NoteLength.SIXTYFOURTH
					.ordinal(); n--) {

				currentLength = TimedElement.NoteLength.values()[n];
				level++;

                // over every note of group, try to build sub groups
                for (Note note : group) {

					// add note to current sub group
					if (note.isShorter(currentLength)) {
						subGroup.add(note);

					} else {
						// if note with different length, close sub group
						// if notes were found
						if (!subGroup.isEmpty())
							setupSubBeamGroups(subGroup, level, false);

					}
				}
				// if group is not empty we have a full beam or only the last
				// note is smaller
				if (!subGroup.isEmpty())
					setupSubBeamGroups(subGroup, level, true);
			}
		}
	}


	/**
	 * Add beam level to a group of notes.
	 * 
	 * @param subGroup
	 *            notes smaller than actual length
	 * @param level
	 *            beam level to add
	 */
	private void setupSubBeamGroups(ArrayList<Note> subGroup, int level,
			boolean isLast) {

		// if only one note, add hook
		if (subGroup.size() < 2) {
			if (isLast)
				// must be the last note of group
				subGroup.get(0).addBeam(
						new Beam(Beam.Type.BACKWARD_HOOK, level));
			else
				subGroup.get(0)
						.addBeam(new Beam(Beam.Type.FORWARD_HOOK, level));

		} else {

			// first note gets begin type
			subGroup.get(0).addBeam(new Beam(Beam.Type.BEGIN, level));
			// last note gets end type
			subGroup.get(subGroup.size() - 1).addBeam(
					new Beam(Beam.Type.END, level));
			// all other get continue type
			for (int i = 1; i < subGroup.size() - 1; i++) {
				subGroup.get(i).addBeam(new Beam(Beam.Type.CONTINUE, level));
			}
		}

		// group is handled
		subGroup.clear();
	}


	/**
	 * Adjust length and direction of stems for notes forming a beam group.
	 */
	private void layoutBeamGroups() {

		for (ArrayList<Note> group : beamGroups) {

			Note first = group.get(0);
			Note last = group.get(group.size() - 1);

			// determine direction of stems
			StemDirection stemDirection = StemDirection.UPWARDS;

			// if beam group consists of two notes only, the note further
			// from middle line determines the stem direction
			if (group.size() < 3) {
				// find note further from middle line
				Note leadNote = (Math
						.abs(first.getLineSpacingsFromMiddleLine()) > Math
						.abs(group.get(1).getLineSpacingsFromMiddleLine())) ? first
						: group.get(1);
				stemDirection = leadNote.getStemDirection();

			} else {
				// for more notes the majority of notes determines the stem
				// direction, get arithmetic middle of steps by adding all
				int sumOfSteps = 0;
				for (Note note : group) {
					sumOfSteps += note.getLineSpacingsFromMiddleLine();
				}

				if (sumOfSteps > 0)
					stemDirection = StemDirection.DOWNWARDS;
				else
					stemDirection = StemDirection.UPWARDS;

			}

			// set selected direction for all notes in group
			for (Note note : group) {
				note.setStemDirection(stemDirection);
			}

			// compute stem lengths
			int stemLength = (staffLineSpacing * 7) / 2;
			// 1. set stem length of first and lastnote to standard = one
			// octave = 3.5 staffline spacing
			first.setStemLength(stemLength);
			last.setStemLength(stemLength);

			if (group.size() > 2) {
				// 2. calculate virtual line between first and last note's stem
				// compute slope of line
				float slope = (float) (last.getOrigin().y - first.getOrigin().y)
						/ (last.getOrigin().x - first.getOrigin().x);

				// if
				// slope = -slope;

				// 3. set stem length of rest of notes that they will end at
				// that virtual line
				for (int n = 1; n < group.size() - 1; n++) {
					Note actNote = group.get(n);
					Note prevNote = group.get(n - 1);

					// length = length of previous + delta y origin + delta y
					// equation of line, sum positve or negative line
					stemLength = (stemDirection == StemDirection.UPWARDS) ? prevNote
							.getStemLength() : -prevNote.getStemLength();
					stemLength += actNote.getOrigin().y
							- prevNote.getOrigin().y;
					stemLength += Math.round(-slope
							* (actNote.getOrigin().x - prevNote.getOrigin().x));
					// lenght is absolute
					actNote.setStemLength(Math.abs(stemLength));
				}
			}

			// TODO: 4. increase or decrease stem length until at least every
			// stem has length of one full space between note head and innermost
			// beam and one has minimum of
			// one octave (3,5 steps)

			// 5. add one staffline spacing for every additional beam level
			// get max. beam level of group
			int maxLevel = 1;
			for (Note note : group) {
				// get max. beam level of note
				for (Beam beam : note.getBeams()) {
					if (beam.getLevel() > maxLevel)
						maxLevel = beam.getLevel();
				}
			}

			// adjust stem length according to additional beam level
			for (Note note : group) {
				note.setStemLength(note.getStemLength() + staffLineSpacing
						* (maxLevel - 1));
			}
		}
	}

    private void layoutTies() {
        for (Tie tie :ties) {

            // fake origin, tie needs staffline spacing only
            tie.setLayout(getOrigin(), staffLineSpacing);
            // it tie without end note, set staff end as end point
            if(tie.getNoteStop() == null)
                tie.setxStaffEnd(xStaffEnd);

        }
    }

	/**
	 * Recompute rest's positions
	 */
	private void redistributeRests() {

		// if no staves are defined we can not positioning any rests
		if (staves.size() < 1)
			return;

		Point restPosition = new Point();
		int staffOrigin = 0;

		for (Rest rest : rests) {

			// get the y-origin of the staff the rest belongs to
			staffOrigin = staves.get(rest.getStaffNumber()).getOrigin().y;

			// set x-position in grids of measure width
			restPosition.x = (int) (xElementStart + rest.getTimePosition()
					* horizontalPlacingWidth);

			// tell rest the position of the upper staff line
			restPosition.y = staffOrigin;

			// positioning of rest
			rest.setLayout(restPosition, staffLineSpacing);
		}
	}

} // class
