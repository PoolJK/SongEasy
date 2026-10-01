/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.score;

import android.graphics.Canvas;

import org.simpleframework.xml.Attribute;

/**
 * Beam for visually grouping notes together. Each note belonging to a beam has
 * its own beam object(s).
 * 
 * @author krein
 * 
 */
public class Beam extends ScoreElement {

	/** possible types of beams */
	public static enum Type {
		NONE, BEGIN, END, CONTINUE, FORWARD_HOOK, BACKWARD_HOOK
	};

	/** factor between note head width and width of broken beams */
	public static float NOTE_BEAM_WIDTH_FACTOR = 0.6f;

	/** possible vertical directions of beams */
	// public static enum Slant {
	// STRAIGHT, UPWARDS, DOWNWORDS
	// };

	/**
	 * Type of beam
	 */
	@Attribute(name = "type")
	Type type;

	/**
	 * Level of beam, 0 is for eight, 5 for 256th note. Add one for MidiXml
	 * export.
	 */
	@Attribute(name = "level")
	public int level;

	// ++++++++++++++++ setters, getters ++++++++++++++++++++
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public Type getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}


	/**
	 * Constructor of Beam class
	 * 
	 * @param type
	 *            type of beam
	 * @param level
	 *            level of beam (eighth to sixtyfourth)
	 */
	public Beam(@Attribute(name = "type") Beam.Type type, @Attribute(
			name = "level") int level) {
		this.type = type;
		this.level = level;
	}

    @Override
    protected void drawSelf(Canvas canvas) {

    }
}
