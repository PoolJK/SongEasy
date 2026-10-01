/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import org.simpleframework.xml.Attribute;

/**
 * Parent class for Notes and Rests. Keeps parameters applicable for all
 * elements with time and position.
 * 
 * @author jorgkrein
 * 
 */
public abstract class TimedElement extends ScoreElement implements
		Comparable<TimedElement> {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** applicable length values of a timed element */
	public static enum NoteLength {
		SIXTYFOURTH, THIRTYSECOND, SIXTEENTH, EIGHTH, QUARTER, HALF, FULL
	};

	/** time value of timed element */
	@Attribute(name = "length")
	private NoteLength length;

	/**
	 * flag if timed element has augmentation dot dotted, adding half the time
	 * value to it
	 */
	@Attribute(name = "isDotted", required = false)
	public boolean isDotted;

	/**
	 * x position of element within measure, with resolution of 96 steps
	 */
	@Attribute(name = "timePosition")
	private int timePosition;

	/**
	 * the number of the staff timed element belongs to, needed for systems with
	 * multiple staves
	 */
	@Attribute(name = "staffNumber")
	private int staffNumber;


	// +++++++ getters and setters +++++++
	// +++++++++++++++++++++++++++++++++++

	/**
	 * Get note's staff number
	 * 
	 * @return Number of staff note is in
	 */
	public int getStaffNumber() {
		return staffNumber;
	}


	/**
	 * Set note's staff number
	 * 
	 * @param staffNumber
	 *            number off staff note belongs to
	 */
	public void setStaffNumber(int staffNumber) {
		this.staffNumber = staffNumber;
	}


	/**
	 * Get length value of element.
	 * 
	 * @return the time length of element
	 */
	public NoteLength getLength() {
		return length;
	}


	/**
	 * Set length of element
	 * 
	 * @param length
	 *            the time length of element
	 */
	public void setLength(NoteLength length) {
		this.length = length;
	}


	/**
	 * Get relative position inside measure. 0.0 for start 1.0 for end of
	 * measure.
	 * 
	 * @return relative position, from 0.0 to 1.0
	 */
	public int getTimePosition() {
		return timePosition;
	}


	/**
	 * Set relative position inside measure.
	 * 
	 * @param timePosition
	 *            relative position, from 0.0 to 1.0
	 */
	public void setTimePosition(int timePosition) {
		this.timePosition = timePosition;
	}


	/**
	 * If this element sounds longer than other.
	 * 
	 * @param other
	 *            Note length to compare with
	 * @return true if longer, else false
	 */
	public boolean isLonger(TimedElement.NoteLength other) {
		if (this.getLength().ordinal() > other.ordinal())
			return true;
		else
			return false;
	}


	/**
	 * If this element sounds shorter than other.
	 * 
	 * @param other
	 *            Note length to compare with
	 * @return true if shorter, else false
	 */
	public boolean isShorter(TimedElement.NoteLength other) {
		if (this.getLength().ordinal() < other.ordinal())
			return true;
		else
			return false;
	}


	@Override
	public int compareTo(TimedElement other) {

		if (this.getTimePosition() < other.getTimePosition())
			return -1;

		if (this.getTimePosition() > other.getTimePosition())
			return 1;

		return 0;
	}

}
