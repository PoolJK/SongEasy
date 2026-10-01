/*
 * Copyright (C) 2013 Joerg Krein
 */
package de.songeasy.android.score;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;

import android.graphics.Color;

/**
 * Chord designator floating over System
 * 
 * @author krein
 * 
 */
public class Chord extends AnnotationElement {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	// TODO: put string names herein also, as in FileTypes class?
	/** basic kinds of chord */
	public static enum Kind {
		// Triads:
		/** major third, perfect fifth (empty) */
		MAJOR(" "),
		/** minor third, perfect fifth (m) */
		MINOR("m"),
		/** major third, augmented fifth (#5) */
		AUGMENTED("#5"),
		/** minor third, diminished fifth (�,(mb5) */
		DIMINISHED("°"),
		/** major triad, minor seventh (7) */
		DOMINANT("7"),
		/** major triad, major seventh (M7) */
		MAJORSEVENTH("M7"),
		/** minor triad, minor seventh (m7) */
		MINORSEVENTH("m7"),
		/** diminished triad, diminished seventh (°7) */
		DIMINISHEDSEVENTH("°7"),
		/** augmented triad, minor seventh (7(#5)) */
		AUGMENTEDSEVENTH("7(#5)"),
		/** diminished triad, minor seventh (m7(b5)) */
		HALFDIMINISHED("m7(b5)"),
		/** minor triad, major seventh (mM7) */
		MAJORMINOR("mM7"),
		// Sixths
		/** major triad, added sixth (6) */
		MAJORSIXTH("6"),
		/** minor triad, added sixth (m6) */
		MINORSIXTH("m6"),
		// Seconds(eqivalent to 9th, but same octave as base
		/** major triad, added second */
		MAJORSECOND("add2"),
		/** minor triad, added second */
		MINORSECOND("madd2"),
		// Ninths:
		/** dominant-seventh, major ninth (9) */
		DOMINANTNINTH("9"),
		/** major-seventh, major ninth (M79) */
		MAJORNINTH("M79"),
		/** minor-seventh, major ninth (b9) */
		MINORNINTH("b9"),
		// 11ths u(sually as the basis for alteration):
		/** dominant-ninth, perfect 11th (11) */
		DOMINANT11TH("11"),
		/** major-ninth, perfect 11th (M11) */
		MAJOR11TH("M11"),
		/** minor-ninth, perfect 11th (m11) */
		MINOR11TH("m11"),
		// 13ths (usually as the basis for alteration):
		/** dominant-11th, major 13th (13) */
		DOMINANT13TH("13"),
		/** major-11th, major 13th (M13) */
		MAJOR13TH("M13"),
		/** minor-11th, major 13th (m13) */
		MINOR13TH("m13"),
		// Suspended:
		/** major second, perfect fifth (sus2) */
		SUSPENDEDSECOND("sus2"),
		/** perfect fourth, perfect fifth (sus4) */
		SUSPENDEDFOURTH("sus4");
		// Functional sixths:
		// NEAPOLITAN(""), ITALIAN(""), FRENCH(""), GERMAN(""),
		// Other:
		/** pedal-point bass */
		// PEDAL(""),
		/** perfect fifth */
		// POWER(""), TRISTAN("");

		private String name;


		Kind(String name) {
			this.name = new String(name);
		}


		/**
		 * Get string array with string representations of all enum values.
		 * 
		 * @return string array with all names
		 */
		public static String[] getNames() {

			ArrayList<String> names = new ArrayList<String>();

			for (Kind kind : Kind.values()) {
				names.add(kind.name);
			}

			return names.toArray(new String[names.size()]);
		}


		/**
		 * Get string representation of enum value.
		 * 
		 * @return name of enum value
		 */
		public String getName() {
			return this.name;
		}


		/**
		 * Get ordinal (index) of enum string representation.
		 * 
		 * @param name
		 *            string of enum
		 * @return index of enum if found, throws IndexOutOfBoundsException
		 *         otherwise
		 */
		public static Kind getValueByName(String name) {

			for (Kind kind : Kind.values()) {
				if (kind.name.equals(name))
					return kind;
			}
			throw (new IndexOutOfBoundsException());
		}
	};

	/** ratio of staff line spacing and chord font */
	private static final float TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR = 2.8f;

	/** note names for chord root */
	public static final String[] ROOT = new String[] { "A", "B", "C", "D", "E",
			"F", "G" };

	/** note names for chord optional bass */
	public static final String[] BASS = new String[] { " ", "A", "B", "C", "D",
			"E", "F", "G" };

	/** accidental names for chords */
	public static final String[] ACCIDENTAL = new String[] { " ", "#", "b" };

	/** base note of chord */
	@Attribute(name = "root")
	private String root;

	/** accidental of base note */
	@Attribute(name = "rootAccidental")
	private String rootAccidental;

	/** kind of chord */
	@Attribute(name = "kind")
	private Kind kind;

	/** bass note of chord */
	@Attribute(name = "bass")
	private String bass;

	/** accidental of bass note */
	@Attribute(name = "bassAccidental")
	private String bassAccidental;


	// +++++++++++++ getters and setters ++++++++++//

	/** get root note of chord */
	public String getRoot() {
		return root;
	}


	/** set root note of chord */
	public void setRoot(String root) {

		this.root = root;
		generateName();
	}


	/** get accidental of root note */
	public String getRootAccidental() {
		return rootAccidental;
	}


	/** set accidental of root note */
	public void setRootAccidental(String rootAccidental) {
		this.rootAccidental = rootAccidental;
		generateName();
	}


	/** get kind of chord */
	public Kind getKind() {
		return kind;
	}


	/** set kind of chord */
	public void setKind(Kind kind) {
		this.kind = kind;
		generateName();
	}


	/** get base note of chord */
	public String getBass() {
		return bass;
	}


	/** set kind of chord */
	public void setBass(String bass) {
		this.bass = bass;
		generateName();
	}


	/** get accidental of base note */
	public String getBassAccidental() {
		return bassAccidental;
	}


	/** set accidental of base note */
	public void setBassAccidental(String bassAccidental) {
		this.bassAccidental = bassAccidental;
		generateName();
	}


	/**
	 * Parameterless constructor of chord class. Creates chord with 'standard'
	 * values (A major).
	 */
	public Chord() {

		this(ROOT[0], ACCIDENTAL[0], Kind.MAJOR, BASS[0], ACCIDENTAL[0]);
	}


	/**
	 * Constructor of class without position We don't pass string array here
	 * 
	 * @param base
	 *            base of chord name
	 * @param accidental
	 *            accidental of chord name
	 * @param gender
	 *            gender of chord name
	 * @param modifier1
	 *            modifier of first add
	 * @param add1
	 *            first additional tone
	 * @param modifier2
	 *            modifier of second add
	 * @param add2
	 *            second additional tone
	 */
	public Chord(
			@Attribute(name = "root") String root,
			@Attribute(name = "rootAccidental") String rootaccidental,
			@Attribute(name = "kind") Kind kind,
			@Attribute(name = "bass") String bass,
			@Attribute(name = "bassAccidental") String bassaccidental) {

		this(root, rootaccidental, kind, bass, bassaccidental, 0.0);
	}


	/**
	 * Constructor of class chord with relative position because the serializer
	 * needs to know how to create an object of this class.
	 * 
	 * @param base
	 *            base of chord name
	 * @param accidental
	 *            accidental of chord name
	 * @param gender
	 *            gender of chord name
	 * @param modifier1
	 *            modifier of first add
	 * @param add1
	 *            first additional tone
	 * @param modifier2
	 *            modifier of second add
	 * @param add2
	 *            second additional tone
	 * @param relativePosition
	 *            desired position in percent, 0 => start of measure, 0.5 =>
	 *            middle of measure, 1 => end of measure
	 */
	public Chord(
			@Attribute(name = "root") String root,
			@Attribute(name = "rootAccidental") String rootaccidental,
			@Attribute(name = "kind") Kind kind,
			@Attribute(name = "bass") String bass,
			@Attribute(name = "bassAccidental") String bassaccidental,
			@Attribute(name = "relativePosition") double relativePosition) {

		super("", relativePosition, TEXT_SIZE_TO_STAFF_LINE_SPACING_FACTOR);

		this.relativePosition = relativePosition;
		this.root = root;
		this.rootAccidental = rootaccidental;
		this.kind = kind;
		this.bass = bass;
		this.bassAccidental = bassaccidental;

		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextSize(25);

		// serializer can not create AnnotationElement object with parameter
		// 'text' from not yet existing Chord object
		// so we need to fill text later when object is created
		generateName();
	}


	private void generateName() {
		if (bass.isEmpty())
			setText(root + rootAccidental + kind.getName());
		else
			setText(root + rootAccidental + kind.getName() + "/" + bass
					+ bassAccidental);
	}

} // class
