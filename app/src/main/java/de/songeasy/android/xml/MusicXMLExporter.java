package de.songeasy.android.xml;

/**
 * Method of exporting the song matching to structure
 * of  the standard MusicXML file
 * @author Fulong
 * 
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import de.songeasy.android.score.AnnotationElement;
import de.songeasy.android.score.Chord;
import de.songeasy.android.score.Chord.Kind;
import de.songeasy.android.score.Coda;
import de.songeasy.android.score.DirectionElement;
import de.songeasy.android.score.Fine;
import de.songeasy.android.score.Label;
import de.songeasy.android.score.Measure;
import de.songeasy.android.score.Note;
import de.songeasy.android.score.Note.StemDirection;
import de.songeasy.android.score.Rest;
import de.songeasy.android.score.ScoreElement;
import de.songeasy.android.score.Segno;
import de.songeasy.android.score.Song;
import de.songeasy.android.score.Staff;
import de.songeasy.android.score.TimedElement;

public class MusicXMLExporter {

	private final String namespace = "";
	private final String FileName = "_musicXML.xml";
	private final static String major = "major";

	private final String MUSICXML = "\"DOCTYPE score-partwise PUBLIC \"-"
			+ "//Recordare//DTD MusicXML 2.0 Partwise//EN\" \"http://www.musicxml.org/dtds/partwise.dtd\"";

	private final String[][] INSTRUMENTS = {
			{ "Voice", "Vo.", "53", "73.88", "846.43" },
			{ "Piano", "Pno.", "1", "90.12", "829.01" },
			{ "Bass", "B.", "53", "64.16", "854.98" } };

	private boolean isVoice = true, isPiano = true, isBass = true;

	private enum KeySign {
		AMAJOR("3", major), AMINOR("0", major), ASHARPMINOR("7", major), AFLATMAJOR(
				"-4", major), AFLATMINOR("-7", major), BMAJOR("5", major), BMINOR(
				"2", major), BFLATMAJOR("-2", major), BFLATMINOR("-5", major), CMAJOR(
				"0", major), CMINOR("-3", major), CSHARPMAJOR("7", major), CSHARPMINOR(
				"4", major), CFLATMAJOR("-7", major), DMAJOR("2", major), DMINOR(
				"-1", major), DSHARPMINOR("6", major), DFLATMAJOR("-5", major), EMAJOR(
				"4", major), EMINOR("1", major), EFLATMAJOR("-3", major), EFLATMINOR(
				"-6", major), FMAJOR("-1", major), FMINOR("-4", major), FSHARPMAJOR(
				"6", major), FSHARPMINOR("3", major), GMAJOR("1", major), GMINOR(
				"-2", major), GSHARPMINOR("5", major), GFLATMAJOR("-6", major);

		String fifths;
		String mode;


		KeySign(String fifths, String mode) {
			this.fifths = fifths;
			this.mode = mode;
		}

	}

	private Song song;
	private File musciXMLFile;


	public MusicXMLExporter(Song song, String xmlFilePath) {
		setSong(song);
		setXmlFile(xmlFilePath);

	}


	private void setSong(Song song) {
		this.song = song;
	}


	private void setXmlFile(String xmlFilePath) {
		this.musciXMLFile = new File(xmlFilePath + "" + FileName);
		try {
			if (!musciXMLFile.exists()) {
				musciXMLFile.createNewFile();
			}
			FileWriter fWriter = new FileWriter(musciXMLFile);
			fWriter.write(xmlWriter());
			fWriter.close();
		} catch (IOException e) {
			// file create fails in case errors happen
			musciXMLFile.delete();
		}
	}


	private String xmlWriter() throws IllegalArgumentException,
			IllegalStateException, IOException {
		// initialization
		StringWriter xmlWriter = new StringWriter();
		XmlSerializer xml = Xml.newSerializer();
		xml.setOutput(xmlWriter);
		// start of document

		xml.startDocument("utf-8", true);
		xml.comment(MUSICXML);

		xml.startTag(namespace, "score-partwise");
		xml.attribute(namespace, "version", song.getVersion());

		xml.startTag(namespace, "identification");
		xml.startTag(namespace, "encoding");
		xml.startTag(namespace, "software");
		xml.text("" + song.getTitleBlock().getComposer());
		xml.endTag(namespace, "software");
		xml.startTag(namespace, "encoding-date");
		xml.text("" + song.getTitleBlock().getDate());
		xml.endTag(namespace, "encoding-date");
		xml.endTag(namespace, "encoding");
		xml.endTag(namespace, "identification");

		xml.startTag(namespace, "defaults");
		getPageLayoutParts(xml);
		xml.endTag(namespace, "defaults");

		/**
		 * <credit page="1"> <credit-words default-x="595.275"
		 * default-y="1627.09" font-size="24" justify="center" valign="top">Main
		 * Title</credit-words> </credit>
		 **/
		String title = song.getTitleBlock().getTitle();
		if (title != null) {
			xml.startTag(namespace, "credit");
			xml.attribute(namespace, "page", "1");
			xml.startTag(namespace, "credit-words");
			xml.attribute(namespace, "default-x", "595.275");
			xml.attribute(namespace, "default-y", "1627.09");
			xml.attribute(namespace, "font-size", "24");
			xml.attribute(namespace, "justify", "center");
			xml.attribute(namespace, "valign", "top");
			xml.text(title);
			xml.endTag(namespace, "credit-words");
			xml.endTag(namespace, "credit");
		}

		/**
		 * <credit page="1"> <credit-words default-x="1133.86"
		 * default-y="1560.09" font-size="12" justify="right"
		 * valign="top">Composer</credit-words> </credit> <credit page="1">
		 * <credit-words default-x="56.6929" default-y="1560.09" font-size="12"
		 * justify="left" valign="top">Lyricer</credit-words> </credit> <credit
		 * page="1"> <credit-words default-x="595.275" default-y="113.386"
		 * font-size="8" justify="center" valign="bottom">Copy
		 * Right</credit-words> </credit>
		 * **/
		String composer = song.getTitleBlock().getComposer();
		if (title != null) {
			xml.startTag(namespace, "credit");
			xml.attribute(namespace, "page", "1");
			xml.startTag(namespace, "credit-words");
			xml.attribute(namespace, "default-x", "1133.86");
			xml.attribute(namespace, "default-y", "1560.09");
			xml.attribute(namespace, "font-size", "12");
			xml.attribute(namespace, "justify", "right");
			xml.attribute(namespace, "valign", "top");
			xml.text("" + composer);
			xml.endTag(namespace, "credit-words");
			xml.endTag(namespace, "credit");
			xml.startTag(namespace, "credit");
			xml.attribute(namespace, "page", "1");
			xml.startTag(namespace, "credit-words");
			xml.attribute(namespace, "default-x", "56.6929");
			xml.attribute(namespace, "default-y", "1560.09");
			xml.attribute(namespace, "font-size", "12");
			xml.attribute(namespace, "justify", "left");
			xml.attribute(namespace, "valign", "top");
			xml.text("" + composer);
			xml.endTag(namespace, "credit-words");
			xml.endTag(namespace, "credit");
		}

		xml.startTag(namespace, "credit");
		xml.attribute(namespace, "page", "1");
		xml.startTag(namespace, "credit-words");
		xml.attribute(namespace, "default-x", "595.275");
		xml.attribute(namespace, "default-y", "113.386");
		xml.attribute(namespace, "font-size", "8");
		xml.attribute(namespace, "justify", "center");
		xml.attribute(namespace, "valign", "bottom");
		xml.text("CopyRight SongEasy By Krein");
		xml.endTag(namespace, "credit-words");
		xml.endTag(namespace, "credit");

		xml.startTag(namespace, "part-list");
		getInstrumentInfo(xml);
		xml.endTag(namespace, "part-list");

		xml.startTag(namespace, "part");
		xml.attribute(namespace, "id", "P1");
		getMeasureParts(xml);
		xml.endTag(namespace, "part");

		xml.endTag(namespace, "score-partwise");
		xml.endDocument();

		return xmlWriter.toString();
	}


	private void getPageLayoutParts(XmlSerializer xml)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/* music pages properties use system default */
		/**
		 * 
		 "<scaling>" + "<millimeters>7.05556</millimeters>" +
		 * "<tenths>40</tenths>" + "</scaling>" +
		 */
		xml.startTag(namespace, "scaling");
		xml.startTag(namespace, "millimeters");
		xml.text("7.05556");
		xml.endTag(namespace, "millimeters");
		xml.startTag(namespace, "tenths");
		xml.text("40");
		xml.endTag(namespace, "tenths");
		xml.endTag(namespace, "scaling");

		/**
		 * "<page-layout>" + "<page-height>1683.78</page-height>" +
		 * "<page-width>1190.55</page-width>" + "<page-margins type=\"even\">" +
		 * "<left-margin>56.6929</left-margin>" +
		 * "<right-margin>56.6929</right-margin>" +
		 * "<top-margin>56.6929</top-margin>" +
		 * "<bottom-margin>113.386</bottom-margin>" + "</page-margins>" +
		 * "<page-margins type=\"odd\">" + "<left-margin>56.6929</left-margin>"
		 * + "<right-margin>56.6929</right-margin>" +
		 * "<top-margin>56.6929</top-margin>" +
		 * "<bottom-margin>113.386</bottom-margin>" + "</page-margins>" +
		 * "</page-layout>";
		 * **/

		xml.startTag(namespace, "page-layout");
		xml.startTag(namespace, "page-height");
		xml.text("1683.78");
		xml.endTag(namespace, "page-height");
		xml.startTag(namespace, "page-width");
		xml.text("1190.55");
		xml.endTag(namespace, "page-width");
		xml.startTag(namespace, "page-margins");
		xml.attribute(namespace, "type", "even");
		xml.startTag(namespace, "left-margin");
		xml.text("56.6929");
		xml.endTag(namespace, "left-margin");
		xml.startTag(namespace, "right-margin");
		xml.text("56.6929");
		xml.endTag(namespace, "right-margin");
		xml.startTag(namespace, "top-margin");
		xml.text("56.6929");
		xml.endTag(namespace, "top-margin");
		xml.startTag(namespace, "bottom-margin");
		xml.text("113.386");
		xml.endTag(namespace, "bottom-margin");
		xml.endTag(namespace, "page-margins");
		xml.startTag(namespace, "page-margins");
		xml.attribute(namespace, "type", "odd");
		xml.startTag(namespace, "left-margin");
		xml.text("56.6929");
		xml.endTag(namespace, "left-margin");
		xml.startTag(namespace, "right-margin");
		xml.text("56.6929");
		xml.endTag(namespace, "right-margin");
		xml.startTag(namespace, "top-margin");
		xml.text("56.6929");
		xml.endTag(namespace, "top-margin");
		xml.startTag(namespace, "bottom-margin");
		xml.text("113.386");
		xml.endTag(namespace, "bottom-margin");
		xml.endTag(namespace, "page-margins");
		xml.endTag(namespace, "page-layout");
	}


	private void getInstrumentInfo(XmlSerializer xml)
			throws IllegalArgumentException, IllegalStateException, IOException {
		String InstrumentName = "";
		String InstrumentNameAbbr = "";
		String Value = "53";

		for (int i = 0; i < INSTRUMENTS.length; i++) {
			ArrayList<Staff> staves = song.getMeasures().get(0).getStaves();
			for (Staff tempor : staves) {
				String name = tempor.getName();
				if (name != null && name.contains("Voice") && isVoice) {
					InstrumentName += INSTRUMENTS[0][0] + "_";
					InstrumentNameAbbr += INSTRUMENTS[0][1] + "_";
					Value = INSTRUMENTS[0][2];
					isVoice = false;
				} else if (name != null && name.contains("Piano") && isPiano) {
					InstrumentName += INSTRUMENTS[1][0] + "_";
					InstrumentNameAbbr += INSTRUMENTS[1][1] + "_";
					Value = INSTRUMENTS[1][2];
					isPiano = false;
				} else if (name != null && name.contains("Bass") && isBass) {
					InstrumentName += INSTRUMENTS[2][0] + "_";
					InstrumentNameAbbr += INSTRUMENTS[2][1] + "_";
					Value = INSTRUMENTS[2][2];
					isBass = false;
				}
			}
		}
		// correction of Strings
		int InstruLength = InstrumentName.length() - 1;
		InstrumentName = InstrumentName.substring(0, InstruLength);
		int InstruAbbLength = InstrumentNameAbbr.length() - 1;
		InstrumentNameAbbr = InstrumentNameAbbr.substring(0, InstruAbbLength);

		/**
		 * "<score-part id=\"P1\">" +
		 * "<part-name>"+InstrumentName+"</part-name>" +
		 * "<part-abbreviation>"+InstrumentNameAbbr+"</part-abbreviation>" +
		 * "<score-instrument id=\"P1-I3\">" +
		 * "<instrument-name>"+InstrumentName+"</instrument-name>" +
		 * "</score-instrument>" + "<midi-instrument id=\"P1-I3\">" +
		 * "<midi-channel>1</midi-channel>" +
		 * "<midi-program>"+Value+"</midi-program>" + "<volume>78.7402</volume>"
		 * + "<pan>0</pan>" + "</midi-instrument>" + "</score-part>";
		 **/
		/* software ware cares about page numbers, system default */
		xml.startTag(namespace, "score-part");
		xml.attribute(namespace, "id", "P1");
		xml.startTag(namespace, "part-name");
		xml.text(InstrumentName);
		xml.endTag(namespace, "part-name");
		xml.startTag(namespace, "part-abbreviation");
		xml.text(InstrumentNameAbbr);
		xml.endTag(namespace, "part-abbreviation");
		xml.startTag(namespace, "score-instrument");
		xml.attribute(namespace, "id", "P1-I3");
		xml.startTag(namespace, "instrument-name");
		xml.text(InstrumentName);
		xml.endTag(namespace, "instrument-name");
		xml.endTag(namespace, "score-instrument");
		xml.startTag(namespace, "midi-instrument");
		xml.attribute(namespace, "id", "P1-I3");
		xml.startTag(namespace, "midi-channel");
		xml.text("1");
		xml.endTag(namespace, "midi-channel");
		xml.startTag(namespace, "midi-program");
		xml.text(Value);
		xml.endTag(namespace, "midi-program");
		xml.startTag(namespace, "volume");
		xml.text("78.7402");
		xml.endTag(namespace, "volume");
		xml.startTag(namespace, "pan");
		xml.text("0");
		xml.endTag(namespace, "pan");
		xml.endTag(namespace, "midi-instrument");
		xml.endTag(namespace, "score-part");

	}


	private void getMeasureParts(XmlSerializer xml)
			throws IllegalArgumentException, IllegalStateException, IOException {
		ArrayList<Measure> measures = song.getMeasures();
		int measureLength = measures.size();

		int newLineIndex = 0;

		for (int index = 0; index < measureLength; index++) {
			Measure currentM = measures.get(index);
			// Measure width calculation
			int width = currentM.getWidth();

			// number of staves
			int stavesLength = currentM.getStaves().size();

			xml.startTag(namespace, "measure");
			xml.attribute(namespace, "number", String.valueOf(index + 1));
			xml.attribute(namespace, "width", String.valueOf(width));

			// first measure
			if (index == 0) {
				// print tag
				getPrintMeasure(xml, stavesLength, false);

				// attribute tag
				xml.startTag(namespace, "attributes");
				/* 16 for a quarter node as default */
				// "<divisions>16</divisions>";
				xml.startTag(namespace, "divisions");
				xml.text("16");
				xml.endTag(namespace, "divisions");
				getKeySignature(xml, currentM);
				getTimeSignature(xml, currentM);
				getClefSignature(xml, currentM, stavesLength);
				xml.endTag(namespace, "attributes");
			} else if (newLineIndex == index) {
				// new line with new measure
				/**
				 * <print new-system="yes">...</print>
				 * **/
				getPrintMeasure(xml, stavesLength, true);

			}

			for (int i = 0; i < stavesLength; i++) {
				// i stands for index of the staff
				// store all score elements, ready for reorder
				ArrayList<ScoreElement> ScoreElements = new ArrayList<ScoreElement>();

				// get TimedElements such Note, Rest
				getTimedElements(currentM, ScoreElements);

				/** direction and annotation could belong to second staff? **/
				// get DirectionElements such Coda, Segno, Fine
				if (i == 0)
					getDirectionElement(currentM, ScoreElements);
				// get AnnotationElements such Label, Lyric, chord
				if (i == 0)
					getAnnotationElement(currentM, ScoreElements);

				// re-order all elements
				resortElements(ScoreElements, 0, ScoreElements.size() - 1);

				for (ScoreElement score : ScoreElements) {

					if (score instanceof Note) {
						getNote(xml, (Note) score, i, stavesLength);
					} else if (score instanceof Rest) {
						getRest(xml, (Rest) score, i);
					} else if (score instanceof DirectionElement) {
						getDirection(xml, (DirectionElement) score, i);
					} else {
						getAnnotation(xml, (AnnotationElement) score, i);
					}
				}
			}

			// if last measure of line
			if (currentM.getAttributes().contains(
					Measure.MeasureAttribute.LINE_BREAK)) {
				newLineIndex = index + 1;
			}

			if (index == measureLength - 1) {
				// end of song, last measure
				// "<barline location=\"right\"><bar-style>light-heavy</bar-style></barline>";
				xml.startTag(namespace, "barline");
				xml.attribute(namespace, "location", "right");
				xml.startTag(namespace, "bar-style");
				xml.text("light-heavy");
				xml.endTag(namespace, "bar-style");
				xml.endTag(namespace, "barline");
			}

			xml.endTag(namespace, "measure");
		}

	}


	/* Margin value uses system default */
	private void getPrintMeasure(XmlSerializer xml, int stavesLength,
			boolean newLine) throws IllegalArgumentException,
			IllegalStateException, IOException {
		/**
		 * {"Voice","Vo.","53", "73.88", "846.43"}, {"Piano","Pno.","1",
		 * "90.12", "829.01"}, {"Bass","B.","53","64.16", "854.98"}
		 * 
		 * "<system-layout>" + "<system-margins>" +
		 * "<left-margin>leftMargin</left-margin>" +
		 * "<right-margin>rightMargin</right-margin>" + "</system-margins>" +
		 * "<top-system-distance>180.00</top-system-distance>" +
		 * "</system-layout>";
		 * **/
		String leftMargin = "73.88";
		String rightMargin = "846.43";
		if (!this.isVoice) {
			leftMargin = "73.88";
			rightMargin = "846.43";
		} else if (!this.isPiano) {
			leftMargin = "90.12";
			rightMargin = "829.01";
		} else if (!this.isBass) {
			leftMargin = "64.16";
			rightMargin = "854.98";
		}

		xml.startTag(namespace, "print");
		if (newLine)
			xml.attribute(namespace, "new-system", "yes");
		xml.startTag(namespace, "system-layout");
		xml.startTag(namespace, "system-margins");
		xml.startTag(namespace, "left-margin");
		xml.text(leftMargin);
		xml.endTag(namespace, "left-margin");
		xml.startTag(namespace, "right-margin");
		xml.text(rightMargin);
		xml.endTag(namespace, "right-margin");
		xml.endTag(namespace, "system-margins");
		xml.startTag(namespace, "top-system-distance");
		xml.text("180.00");
		xml.endTag(namespace, "top-system-distance");
		xml.endTag(namespace, "system-layout");
		xml.endTag(namespace, "print");

		if (stavesLength > 1) {
			/**
			 * "<staff-layout number=\"2\"><staff-distance>65.00</staff-distance></staff-layout>"
			 * **/
			for (int i = 1; i < stavesLength; i++) {
				xml.startTag(namespace, "staff-layout");
				xml.attribute(namespace, "number", String.valueOf(i + 1));
				xml.startTag(namespace, "staff-distance");
				xml.text("65");
				xml.endTag(namespace, "staff-distance");
				xml.endTag(namespace, "staff-layout");
			}
		}
	}


	private void getKeySignature(XmlSerializer xml, Measure currentM)
			throws IllegalArgumentException, IllegalStateException, IOException {
		String keySignatureName = currentM.getStaves().get(0).getKeySignature()
				.getKeyName().name();
		/*
		 * <key> <fifths>1</fifths> <mode>major</mode> </key>
		 */
		xml.startTag(namespace, "key");
		for (KeySign tempor : KeySign.values()) {
			if (tempor.name().equals(keySignatureName)) {
				xml.startTag(namespace, "fifths");
				xml.text(tempor.fifths);
				xml.endTag(namespace, "fifths");
				xml.startTag(namespace, "mode");
				xml.text(tempor.mode);
				xml.endTag(namespace, "mode");
			}
		}
		xml.endTag(namespace, "key");
	}


	private void getTimeSignature(XmlSerializer xml, Measure currentM)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/*
		 * <time> <beats>3</beats> <beat-type>4</beat-type> </time>
		 */
		/** possible more keys **/
		int beats = currentM.getStaves().get(0).getTimeSignature()
				.getNumerator();
		int beatsType = currentM.getStaves().get(0).getTimeSignature()
				.getDenominator();
		xml.startTag(namespace, "time");
		xml.startTag(namespace, "beats");
		xml.text(String.valueOf(beats));
		xml.endTag(namespace, "beats");
		xml.startTag(namespace, "beat-type");
		xml.text(String.valueOf(beatsType));
		xml.endTag(namespace, "beat-type");
		xml.endTag(namespace, "time");
	}


	private void getClefSignature(XmlSerializer xml, Measure currentM,
			int staves) throws IllegalArgumentException, IllegalStateException,
			IOException {
		// for first staff of Voice, Piano..., sometimes on second
		// for number of staves >1, <staves>2</staves>
		if (staves > 1) {
			xml.startTag(namespace, "staves");
			xml.text(String.valueOf(staves));
			xml.endTag(namespace, "staves");
		}
		/*
		 * defining the structure of the clef elements
		 * //"<clef number=\"1\"><sign>G</sign><line>2</line></clef>"; //for
		 * other staves, usually on second staff
		 * //"<clef number=\"2\"><sign>F</sign><line>4</line></clef>";
		 */
		for (int i = 0; i < staves; i++) {
			String nameClef = currentM.getStaves().get(i).getClefSignature()
					.getClef().name();
			xml.startTag(namespace, "clef");
			xml.attribute(namespace, "number", String.valueOf(i + 1));
			if (nameClef.equals("VIOLIN")) {
				xml.startTag(namespace, "sign");
				xml.text("G");
				xml.endTag(namespace, "sign");
				xml.startTag(namespace, "line");
				xml.text("2");
				xml.endTag(namespace, "line");
			} else if (nameClef.equals("BASS")) {
				xml.startTag(namespace, "sign");
				xml.text("F");
				xml.endTag(namespace, "sign");
				xml.startTag(namespace, "line");
				xml.text("4");
				xml.endTag(namespace, "line");
			}
			xml.endTag(namespace, "clef");
		}

	}


	/** Implementation of Notes **/
	private void getTimedElements(Measure currentM,
			ArrayList<ScoreElement> scoreElements)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/** Specification handle **/

		// get notes
		ArrayList<Note> notes = currentM.getNotes();
		for (Note note : notes) {
			scoreElements.add(note);
		}
		// get rest
		ArrayList<Rest> rests = currentM.getRests();
		for (Rest rest : rests) {
			scoreElements.add(rest);
		}

	}


	private void getDirectionElement(Measure currentM,
			ArrayList<ScoreElement> scoreElements)
			throws IllegalArgumentException, IllegalStateException, IOException {
		ArrayList<DirectionElement> directions = currentM
				.getDirectionElements();
		for (DirectionElement currentDirect : directions) {
			scoreElements.add(currentDirect);
		}
	}


	private void getAnnotationElement(Measure currentM,
			ArrayList<ScoreElement> scoreElements) {
		ArrayList<AnnotationElement> annotations = currentM.getAnnotations();
		for (AnnotationElement annotation : annotations) {
			scoreElements.add(annotation);
		}
	}


	private void resortElements(ArrayList<ScoreElement> scores, int left,
			int right) {
		/* quick sort of re-order of note according to their time position */
		if (scores != null && scores.size() != 0) {
			int ltemp = left, rtemp = right;
			ScoreElement value = scores.get((left + right) / 2);
			/**
			 * ltemp ==rtemp still need to test if the order is fixed
			 * **/
			while (ltemp <= rtemp) {
				while (scores.get(ltemp).getOrigin().x < value.getOrigin().x) {
					ltemp++;
				}
				while (scores.get(rtemp).getOrigin().x > value.getOrigin().x) {
					rtemp--;
				}
				if (rtemp >= ltemp) {
					ScoreElement temp = scores.get(rtemp);
					scores.set(rtemp, scores.get(ltemp));
					scores.set(ltemp, temp);
					rtemp--;
					ltemp++;
				}
			}
			if (left < rtemp)
				resortElements(scores, left, ltemp - 1);
			if (right > ltemp)
				resortElements(scores, rtemp + 1, right);
		}
	}


	private void getNote(XmlSerializer xml, Note note, int indexStaff,
			int stavesLength) throws IllegalArgumentException,
			IllegalStateException, IOException {
		/**
		 * //needed to be implemented later <note length="FULL" isDotted="false"
		 * staffNumber="0" timePosition="8" accidental="NONE" tie="NONE"
		 * name="G" midiPitch="0" octave="4" voice="0"/> length="HALF",
		 * length="QUARTER", length="EIGHTH", length="SIXTEENTH",
		 * length="THIRTYSECOND" <note default-x="85.65" default-y="-15.00">
		 * <pitch> <step>C</step> <octave>5</octave> </pitch>
		 * <duration>32</duration> <voice>1</voice> <type>whole</type>
		 * <type>half</type> <type>quarter</type> <type>eighth</type>
		 * <type>16th</type> <type>32nd</type> step>G or octave>4
		 * <stem>down</stem> step<G <stem>up</stem> <staff>index</staff> </note>
		 * **/

		if (note.getStaffNumber() == indexStaff) {

			String type = "whole";
			String stem = "up";
			StemDirection stemD = note.getStemDirection();
			if (stemD.equals(StemDirection.DOWNWARDS))
				stem = "down";

			type = determineType(note.getLength());

			xml.startTag(namespace, "note");
			/*
			 * determine the position
			 */
			xml.attribute(namespace, "default-x",
					String.valueOf(note.getOrigin().x));
			xml.attribute(namespace, "default-y",
					String.valueOf(note.getOrigin().y));
			xml.startTag(namespace, "pitch");
			xml.startTag(namespace, "step");
			xml.text(note.getName().name());
			xml.endTag(namespace, "step");
			xml.startTag(namespace, "octave");
			xml.text(String.valueOf(note.getOctave()));
			xml.endTag(namespace, "octave");
			xml.endTag(namespace, "pitch");
			/*
			 * for the duration, right now default 64
			 */
			xml.startTag(namespace, "duration");
			xml.text(determindDuaration(type));
			xml.endTag(namespace, "duration");
			/** determined by staff 1->1, 2->5 **/
			xml.startTag(namespace, "voice");
			xml.text(determineVoice(indexStaff));
			xml.endTag(namespace, "voice");
			xml.startTag(namespace, "type");
			xml.text(type);
			xml.endTag(namespace, "type");
			xml.startTag(namespace, "stem");
			xml.text(stem);
			xml.endTag(namespace, "stem");
			xml.startTag(namespace, "staff");
			xml.text(String.valueOf(indexStaff + 1));
			xml.endTag(namespace, "staff");
			xml.endTag(namespace, "note");

			if (stavesLength > 2 && indexStaff == 0) {
				/**
				 * <backup> <duration>4</duration> </backup>
				 * **/
				xml.startTag(namespace, "backup");
				xml.startTag(namespace, "duration");
				xml.text("4");
				xml.endTag(namespace, "duration");
				xml.endTag(namespace, "backup");

			}
		}

	}


	private void getRest(XmlSerializer xml, Rest rest, int indexStaff)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/**
		 * <note> <rest> <display-step>G</display-step>
		 * <display-octave>4</display-octave> </rest> <duration>1</duration>
		 * <voice>1</voice> <type>16th</type> <staff>1</staff> </note>
		 * **/

		if (rest.getStaffNumber() == indexStaff) {
			String type = "whole";
			/*
			 * missing octave and step elements for rest, not affect import of
			 * musicXML
			 * 
			 * if(rest.octave>4 || afterNoteNameG(rest.name))...?
			 */
			type = determineType(rest.getLength());

			xml.startTag(namespace, "note");
			/*
			 * determine the position
			 */
			xml.attribute(namespace, "default-x",
					String.valueOf(10 + indexStaff * 20));
			xml.attribute(namespace, "default-y",
					String.valueOf(-10 - indexStaff * 2));
			xml.startTag(namespace, "rest");
			/**
			 * Not display be default, other the position can be specified
			 * 
			 * xml.startTag(namespace, "display-step"); xml.text("G");
			 * xml.endTag(namespace, "display-step"); xml.startTag(namespace,
			 * "display-octave"); xml.text("4"); xml.endTag(namespace,
			 * "display-octave");
			 **/
			xml.endTag(namespace, "rest");
			/*
			 * determine the duration by default the whole value is 64
			 */
			xml.startTag(namespace, "duration");
			xml.text(determindDuaration(type));
			xml.endTag(namespace, "duration");
			xml.startTag(namespace, "voice");
			xml.text(determineVoice(indexStaff));
			xml.endTag(namespace, "voice");
			xml.startTag(namespace, "type");
			xml.text(type);
			xml.endTag(namespace, "type");
			xml.startTag(namespace, "staff");
			xml.text(String.valueOf(indexStaff + 1));
			xml.endTag(namespace, "staff");
			xml.endTag(namespace, "note");
		}

	}


	private String determindDuaration(String type) {
		if (type.endsWith("half"))
			return "32";
		else if (type.endsWith("quarter"))
			return "16";
		else if (type.endsWith("eighth"))
			return "8";
		else if (type.endsWith("16th"))
			return "4";
		else if (type.endsWith("32nd"))
			return "2";
		else if (type.endsWith("64th"))
			return "1";
		return "32";
	}


	private String determineVoice(int staffNumber) {
		return staffNumber == 0 ? "1" : "5";
	}


	private boolean afterNoteNameG(Note.NoteName current) {
		return (current == Note.NoteName.A || current == Note.NoteName.B) ? true
				: false;
	}


	private String determineType(TimedElement.NoteLength noteLength) {

		if (noteLength == TimedElement.NoteLength.HALF)
			return "half";
		else if (noteLength == TimedElement.NoteLength.QUARTER)
			return "quarter";
		else if (noteLength == TimedElement.NoteLength.EIGHTH)
			return "eighth";
		else if (noteLength == TimedElement.NoteLength.SIXTEENTH)
			return "16th";
		else if (noteLength == TimedElement.NoteLength.THIRTYSECOND)
			return "32nd";
		else if (noteLength == TimedElement.NoteLength.SIXTYFOURTH)
			return "64th";
		return "whole";
	}


	private void getDirection(XmlSerializer xml,
			DirectionElement currentDirect, int indexStaff)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/**
		 * <direction placement="above"> <direction-type> Coda --> <coda
		 * default-x="-9" default-y="41"/> Fine --> <words default-y="68"
		 * halign="center" default-x="12">Fine</words> Segno --> <segno
		 * default-x="39" default-y="24"/> </direction-type>
		 * <staff>index</staff> <sound coda="coda"/> <sound fine="yes"/> <sound
		 * segno="segno"/> </direction>
		 * **/
		/*
		 * No attributes for the position of direction elements cannot be
		 * determined in musicXML file, could be bug
		 */
		String directName = "coda";
		String soundAttr = "coda", soundAttrValue = "coda";
		xml.startTag(namespace, "direction");
		xml.attribute(namespace, "placement", "above");
		xml.startTag(namespace, "direction-type");
		if (currentDirect instanceof Coda) {
			directName = soundAttr = soundAttrValue = "coda";
			xml.startTag(namespace, directName);
			xml.attribute(namespace, "default-x",
					String.valueOf(currentDirect.getOrigin().x));
			xml.attribute(namespace, "default-y",
					String.valueOf(currentDirect.getOrigin().y));
			xml.endTag(namespace, directName);
		} else if (currentDirect instanceof Segno) {
			directName = soundAttr = soundAttrValue = "segno";
			xml.startTag(namespace, directName);
			xml.attribute(namespace, "default-y",
					String.valueOf(currentDirect.getOrigin().y));
			xml.attribute(namespace, "halign", "center");
			xml.attribute(namespace, "default-x",
					String.valueOf(currentDirect.getOrigin().x));
			xml.text("Fine");
			xml.endTag(namespace, directName);
		} else if (currentDirect instanceof Fine) {
			directName = "words";
			soundAttr = "fine";
			soundAttrValue = "yes";
			xml.startTag(namespace, directName);
			xml.attribute(namespace, "default-x",
					String.valueOf(currentDirect.getOrigin().x));
			xml.attribute(namespace, "default-y",
					String.valueOf(currentDirect.getOrigin().y));
			xml.endTag(namespace, directName);
		}
		xml.endTag(namespace, "direction-type");
		xml.startTag(namespace, "staff");
		xml.text(String.valueOf(indexStaff + 1));
		xml.endTag(namespace, "staff");
		xml.startTag(namespace, "sound");
		xml.attribute(namespace, soundAttr, soundAttrValue);
		xml.endTag(namespace, "sound");
		xml.endTag(namespace, "direction");

	}


	private void getAnnotation(XmlSerializer xml,
			AnnotationElement currentAnno, int indexStaff)
			throws IllegalArgumentException, IllegalStateException, IOException {

		if (currentAnno instanceof Chord) {
			getChord(xml, (Chord) currentAnno, indexStaff);
		} else {
			/**
			 * for label <direction placement="above"> <direction-type> <words
			 * relative-y="0" relative-x="0">label1</words> </direction-type>
			 * <staff>1</staff> </direction>
			 * **/

			/**
			 * for lyrics <direction placement="below"> <direction-type> <words
			 * relative-y="0" relative-x="0">lyric1</words> </direction-type>
			 * <staff>1</staff> </direction>
			 * **/
			String place = "below";
			if (currentAnno instanceof Label)
				place = "above";
			xml.startTag(namespace, "direction");
			xml.attribute(namespace, "placement", place);
			xml.startTag(namespace, "direction-type");
			xml.startTag(namespace, "words");
			// xml.attribute(namespace, "relative-y",
			// String.valueOf(currentAnno.getOrigin().y));
			// xml.attribute(namespace, "relative-x",
			// String.valueOf(currentAnno.getOrigin().x));
			xml.text(currentAnno.getText());
			xml.endTag(namespace, "words");
			xml.endTag(namespace, "direction-type");
			xml.startTag(namespace, "staff");
			xml.text(String.valueOf(indexStaff + 1));
			xml.endTag(namespace, "staff");
			xml.endTag(namespace, "direction");

		}
	}


	/* Implementation of Chord */
	private void getChord(XmlSerializer xml, Chord currentChord, int indexStaff)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/**
		 * <annotationElement class="de.songeasy.android.score.Chord"
		 * text="Ab#5/B" relativePosition="0.5050724615329417" bass="B"
		 * bassAccidental="" kind="AUGMENTED" root="A" rootAccidental="b"/>
		 * 
		 * for chord <harmony print-frame="no" relative-x="x" relative-y="y">
		 * <root> <root-step>C</root-step> <root-alter>-1</root-alter> </root>
		 * <kind text="m">minor</kind> <bass> <bass-step>C</bass-step>
		 * <bass-alter>-1</bass-alter> </bass> </harmony>
		 * **/

		String rootAcci = accidentDetermine(currentChord.getRootAccidental());
		String bassAcci = accidentDetermine(currentChord.getBassAccidental());

		xml.startTag(namespace, "harmony");
		xml.attribute(namespace, "print-frame", "no");
		xml.attribute(namespace, "relative-x",
				String.valueOf(currentChord.getOrigin().x));
		// xml.attribute(namespace, "relative-y",
		// String.valueOf(currentChord.getOrigin().y));
		xml.startTag(namespace, "root");
		xml.startTag(namespace, "root-step");
		xml.text(currentChord.getRoot());
		xml.endTag(namespace, "root-step");
		xml.startTag(namespace, "root-alter");
		xml.text(rootAcci);
		xml.endTag(namespace, "root-alter");
		xml.endTag(namespace, "root");

		getKind(xml, currentChord.getKind());

		xml.startTag(namespace, "bass");
		xml.startTag(namespace, "bass-step");
		xml.text(currentChord.getBass());
		xml.endTag(namespace, "bass-step");
		xml.startTag(namespace, "bass-alter");
		xml.text(bassAcci);
		xml.endTag(namespace, "bass-alter");
		xml.endTag(namespace, "bass");
		xml.endTag(namespace, "harmony");
	}


	private String accidentDetermine(String acci) {
		if (acci != null && !acci.equals("") && !acci.equals(" ")) {
			return acci.equals("#") ? "1" : "-1";
		}
		return "0";
	}


	private void getKind(XmlSerializer xml, Kind kind)
			throws IllegalArgumentException, IllegalStateException, IOException {
		/**
		 * kind="AUGMENTED"
		 * 
		 * <kind text=" ">major</kind> <kind text="m">minor</kind> <kind
		 * text="+">augmented</kind> <kind text="dim">diminished</kind> <kind
		 * text="7">dominant</kind> <kind text="Maj7">major-seventh</kind> <kind
		 * text="m7">minor-seventh</kind> <kind
		 * text="dim7">diminished-seventh</kind> <kind
		 * text="7+">augmented-seventh</kind> <kind
		 * text="m7b5">half-diminished</kind> <kind
		 * text="m9M7">major-minor</kind> <kind text="6">major-sixth</kind>
		 * <kind text="m6">minor-sixth</kind> <kind text="add2">major</kind>
		 * <kind text="madd2">minor</kind> <kind text="9">dominant-ninth</kind>
		 * <kind text="Maj9">major-ninth</kind> <kind
		 * text="m9">minor-ninth</kind> <kind text="11">dominant-11th</kind>
		 * <kind text="Maj11">major-11th</kind> <kind
		 * text="m11">minor-11th</kind> <kind text="13">dominant-13th</kind>
		 * <kind text="Maj13">major-13th</kind> <kind
		 * text="m13">minor-13th</kind> <kind
		 * text="sus2">suspended-second</kind> <kind
		 * text="sus4">suspended-fourth</kind>
		 * **/
		String attr = "";
		String text = "";
		boolean majorminor2 = false;

		xml.startTag(namespace, "kind");
		if (kind.name() != null) {

			if (kind.name().equals("MAJOR")) {
				attr = " ";
				text = "major";
			} else if (kind.name().equals("MINOR")) {
				attr = "m";
				text = "minor";
			} else if (kind.name().equals("AUGMENTED")) {
				attr = "+";
				text = "augmented";
			} else if (kind.name().equals("DIMINISHED")) {
				attr = "dim";
				text = "diminished";
			} else if (kind.name().equals("DOMINANT")) {
				attr = "7";
				text = "dominant";
			} else if (kind.name().equals("MAJORSEVENTH")) {
				attr = "Maj7";
				text = "major-seventh";
			} else if (kind.name().equals("MINORSEVENTH")) {
				attr = "m7";
				text = "minor-seventh";
			} else if (kind.name().equals("DIMINISHEDSEVENTH")) {
				attr = "dim7";
				text = "diminished-seventh";
			} else if (kind.name().equals("AUGMENTEDSEVENTH")) {
				attr = "7+";
				text = "augmented-seventh";
			} else if (kind.name().equals("HALFDIMINISHED")) {
				attr = "m7b5";
				text = "half-diminished";
			} else if (kind.name().equals("MAJORMINOR")) {
				attr = "m9M7";
				text = "major-minor";
			} else if (kind.name().equals("MAJORSIXTH")) {
				attr = "6";
				text = "major-sixth";
			} else if (kind.name().equals("MINORSIXTH")) {
				attr = "m6";
				text = "minor-sixth";
			} else if (kind.name().equals("MAJORSECOND")) {
				attr = "add2";
				text = "major";
				majorminor2 = true;
			} else if (kind.name().equals("MINORSECOND")) {
				attr = "madd2";
				text = "minor";
				majorminor2 = true;
			} else if (kind.name().equals("DOMINANTNINTH")) {
				attr = "9";
				text = "dominant-ninth";
			} else if (kind.name().equals("MAJORNINTH")) {
				attr = "Maj9";
				text = "major-ninth";
			} else if (kind.name().equals("MINORNINTH")) {
				attr = "m9";
				text = "minor-ninth";
			} else if (kind.name().equals("DOMINANT11TH")) {
				attr = "11";
				text = "dominant-11th";
			} else if (kind.name().equals("MAJOR11TH")) {
				attr = "Maj11";
				text = "major-11th";
			} else if (kind.name().equals("MINOR11TH")) {
				attr = "m11";
				text = "minor-11th";
			} else if (kind.name().equals("DOMINANT13TH")) {
				attr = "13";
				text = "dominant-13th";
			} else if (kind.name().equals("MAJOR13TH")) {
				attr = "Maj13";
				text = "major-13th";
			} else if (kind.name().equals("MINOR13TH")) {
				attr = "m13";
				text = "minor-13th";
			} else if (kind.name().equals("SUSPENDEDSECOND")) {
				attr = "sus2";
				text = "suspended-second";
			} else if (kind.name().equals("SUSPENDEDFOURTH")) {
				attr = "sus4";
				text = "suspended-fourth";
			}

			xml.attribute(namespace, "text", attr);
			xml.text(text);
		}
		xml.endTag(namespace, "kind");

		if (majorminor2) {
			/*
			 * <degree> <degree-value>9</degree-value>
			 * <degree-alter>0</degree-alter> <degree-type>add</degree-type>
			 * </degree>
			 */
			xml.startTag(namespace, "degree");
			xml.startTag(namespace, "degree-value");
			xml.text("9");
			xml.endTag(namespace, "degree-value");
			xml.startTag(namespace, "degree-alter");
			xml.text("0");
			xml.endTag(namespace, "degree-alter");
			xml.startTag(namespace, "degree-type");
			xml.text("add");
			xml.endTag(namespace, "degree-type");
			xml.endTag(namespace, "degree");
		}

	}
}
