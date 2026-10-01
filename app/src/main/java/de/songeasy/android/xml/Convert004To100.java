/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.songeasy.android.fileactivity.FileVersion;
import de.songeasy.android.interfaces.XMLConvertInterface;
import de.songeasy.android.score.AnnotationElement;
import de.songeasy.android.score.Chord;
import de.songeasy.android.score.Chord.Kind;
import de.songeasy.android.score.Label;
import de.songeasy.android.score.Lyric;
import de.songeasy.android.score.Measure.MeasureAttribute;

/**
 * Transform song version 0.0.4 to version 1.0.0
 * 
 * @author Fulong
 */

public class Convert004To100 implements XMLConvertInterface {

	/**
	 * xml source file to be converted from version 0.0.4 to version 1.0.0
	 */
	private File xmlSource;

	/**
	 * XmlPullParse get the necessary nodes DOM to remove and add them to the
	 * document
	 */
	private XmlPullParser xmlPull;

	private ArrayList<ArrayList<AnnotationElement>> mAnnotaionElement = new ArrayList<ArrayList<AnnotationElement>>();
	// private ArrayList<ArrayList<DirectionElement>> mDirectionElement=new
	// ArrayList<ArrayList<DirectionElement>>();
	private ArrayList<MeasureAttribute> mMeasureAttributes = new ArrayList<MeasureAttribute>();


	public Convert004To100(File xmlSource, XmlPullParser xmlPull) {
		this.setXmlSource(xmlSource);
		this.setXmlPullParser(xmlPull);
	}


	public File getXmlSource() {
		return xmlSource;
	}


	private void setXmlSource(File xmlSource) {
		this.xmlSource = xmlSource;
	}


	private void setXmlPullParser(XmlPullParser xmlPull) {
		this.xmlPull = xmlPull;
	}


	/**
	 * define behavior in de-serialization of tag chord
	 * **/
	public void inChord(
			XmlPullParser xmlPull,
			ArrayList<AnnotationElement> mAnnotaionEle)
			throws XmlPullParserException, IOException {

		do {
			/*
			 * <chord text="" relativePosition="" bass="" bassAccidental=""
			 * kind="" root="" rootAccidental=""/>
			 */
			if (xmlPull.getEventType() == XmlPullParser.START_TAG
					&& xmlPull.getName().equals("chord")) {

				// deserilaization procedure
				// get properties for each chord object
				Chord newChord = new Chord();
				String text = xmlPull.getAttributeValue(0);
				newChord.setText(text);

				double relativePosition = Double.parseDouble(xmlPull
						.getAttributeValue(1));
				newChord.setRelativePosition(relativePosition);

				String bass = xmlPull.getAttributeValue(2);
				newChord.setBass(bass);

				String bassAccidental = xmlPull.getAttributeValue(3);
				newChord.setBassAccidental(bassAccidental);

				String name = xmlPull.getAttributeValue(4);
				Kind kind = Kind.AUGMENTED;
				for (Kind single : Kind.values()) {
					if (single.name().equals(name))
						kind = single;
				}
				newChord.setKind(kind);

				String root = xmlPull.getAttributeValue(5);
				newChord.setRoot(root);

				String rootAccidental = xmlPull.getAttributeValue(6);
				newChord.setRootAccidental(rootAccidental);

				mAnnotaionEle.add(newChord);

			}

			xmlPull.next();
			if (xmlPull.getName() == null)
				xmlPull.next();

			if (xmlPull.getEventType() == XmlPullParser.END_TAG
					&& xmlPull.getName().equals("chords"))
				break;

		} while (true);
	}


	/**
	 * define behavior in de-serialization of tag lyric
	 * **/
	public void inLyrics(
			XmlPullParser xmlPull,
			ArrayList<AnnotationElement> mAnnotaionEle)
			throws XmlPullParserException, IOException {

		do {
			/*
			 * <lyric text="" relativePosition="" lineNumber="" staffNumber=""/>
			 */
			if (xmlPull.getEventType() == XmlPullParser.START_TAG
					&& xmlPull.getName().equals("lyric")) {

				// deserialization procedure
				String text = xmlPull.getAttributeValue(0);

				double relativePosition = Double.parseDouble(xmlPull
						.getAttributeValue(1));

				int lineNumber = Integer.parseInt(xmlPull.getAttributeValue(2));

				int staffNumber = Integer
						.parseInt(xmlPull.getAttributeValue(3));

				Lyric newlyric = new Lyric(text, relativePosition, lineNumber,
						staffNumber);

				mAnnotaionEle.add(newlyric);

			}

			xmlPull.next();
			if (xmlPull.getName() == null)
				xmlPull.next();

			if (xmlPull.getEventType() == XmlPullParser.END_TAG
					&& xmlPull.getName().equals("lyrics"))
				break;

		} while (true);
	}


	/**
	 * define behavior in de-serialization of tag label
	 * **/
	public void inLabel(
			XmlPullParser xmlPull,
			ArrayList<AnnotationElement> mAnnotaionEle)
			throws XmlPullParserException, IOException {

		do {

			/*
			 * <label text="" relativePosition=""/>
			 */
			if (xmlPull.getEventType() == XmlPullParser.START_TAG
					&& xmlPull.getName().equals("label")) {

				// deserialization procedure
				String text = xmlPull.getAttributeValue(0);

				double relativePosition = Double.parseDouble(xmlPull
						.getAttributeValue(1));

				Label newlabel = new Label(text, relativePosition);

				mAnnotaionEle.add(newlabel);

			}

			xmlPull.next();
			if (xmlPull.getName() == null)
				xmlPull.next();

			if (xmlPull.getEventType() == XmlPullParser.END_TAG
					&& xmlPull.getName().equals("labels"))
				break;

		} while (true);
	}


	/**
	 * define behavior in de-serialization of tag attribute
	 * **/
	public void inAttribute(
			XmlPullParser xmlPull,
			MeasureAttribute mMeasureAttribute) throws XmlPullParserException,
			IOException {

		/*
		 * <attribute>FIRST_IN_LINE</attribute>
		 */
		xmlPull.next();
		String attrname = xmlPull.getText();

		for (MeasureAttribute single : MeasureAttribute.values()) {
			if (single.name().equals(attrname)) {
				mMeasureAttribute = single;
				this.mMeasureAttributes.add(mMeasureAttribute);
			}
		}

	}


	/**
	 * Write converted file from memory to disk
	 * 
	 * @param doc
	 *            DOM document to write
	 * @throws TransformerException
	 */
	public void writeOutConvertedResult(Document doc)
			throws TransformerException {

		DOMSource source = new DOMSource(doc);
		/*
		 * String path=xmlSource.getParent();File convert=new
		 * File(path+"/converted.xml");
		 */
		StreamResult result = new StreamResult(xmlSource);
		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf = tff.newTransformer();
		tf.transform(source, result);
	}


	@Override
	public void convertProcedure() throws XmlPullParserException, IOException,
			ParserConfigurationException, SAXException {
		/**
		 * structure of measure in song with version 0.0.4 <measure>
		 * <annotations/> <chords></chords> <directions/> <staves></staves>
		 * <labels></labels> <lyrics/> <notes></notes> <rests></rests>
		 * <attribute>FIRST_IN_LINE</attribute> <repeat>START</repeat>
		 * </measure>
		 * **/
		// deserialization of direction element not done yet???
		while (xmlPull.getEventType() != XmlPullParser.END_DOCUMENT) {

			xmlPull.next();
			// <Tag:start event> </Tag:end event> null, accelerate search
			if (xmlPull.getName() == null)
				xmlPull.next();

			// handle events in measures
			if (xmlPull.getEventType() == XmlPullParser.START_TAG
					&& xmlPull.getName().equals("measure")) {

				// needed elements
				ArrayList<AnnotationElement> mAnnotaionEle = new ArrayList<AnnotationElement>();
				// ArrayList<DirectionElement> mDirectionEle=new
				// ArrayList<DirectionElement>();
				MeasureAttribute mMeasureAttribute = MeasureAttribute.LINE_BREAK;

				do {
					// de-serialize of Chords
					if (xmlPull.getEventType() == XmlPullParser.START_TAG
							&& xmlPull.getName().equals("chords")) {
						// some bugs, unable to jump out of loop
						inChord(xmlPull, mAnnotaionEle);
					}

					// de-serialize of lyric
					if (xmlPull.getEventType() == XmlPullParser.START_TAG
							&& xmlPull.getName().equals("lyrics")) {
						inLyrics(xmlPull, mAnnotaionEle);
					}

					// de-serialize of label
					if (xmlPull.getEventType() == XmlPullParser.START_TAG
							&& xmlPull.getName().equals("labels")) {
						inLabel(xmlPull, mAnnotaionEle);
					}

					// de-serialize of MeasureAttribute
					if (xmlPull.getEventType() == XmlPullParser.START_TAG
							&& xmlPull.getName().equals("attribute")) {
						inAttribute(xmlPull, mMeasureAttribute);
					}

					xmlPull.next();
					// <Tag:start event> </Tag:end event> null, accelerate
					// search
					if (xmlPull.getName() == null)
						xmlPull.next();

					// leave the scope of tag measure
					if ((xmlPull.getEventType() == XmlPullParser.END_TAG && xmlPull
							.getName().equals("measure"))) {
						break;
					}

				} while (true);

				this.mAnnotaionElement.add(mAnnotaionEle);

			}
		}

		// DOM to convert to standard version
		xml_0_0_4_convert();
	}


	private void xml_0_0_4_convert() throws ParserConfigurationException,
			SAXException, IOException {

		/****
		 * //successfully deserialized all needed element
		 * ArrayList<ArrayList<AnnotationElement>>
		 * mAnnotaionEles=mAnnotaionElement;
		 * ArrayList<ArrayList<DirectionElement>>
		 * mDirectionEles=mDirectionElement; ArrayList<MeasureAttribute>
		 * mMeasureAttrs=mMeasureAttributes;
		 ****/

		/**
		 * DOM method to modify the content of XML DOM load xml file into memory
		 * and make modification
		 * **/
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = db.parse(xmlSource);

		// get root element
		Element root = doc.getDocumentElement();

		if (root.getNodeName().equals("song")) {
			root.setAttribute("version", FileVersion.V100.getVersionString());
		}

		NodeList measureNodes = root.getElementsByTagName("measure");

		for (int i = 0; i < measureNodes.getLength(); i++) {
			/*
			 * <measure>...</measure>
			 */
			Element measure = (Element) measureNodes.item(i);
			if (measure.hasChildNodes()) {
				/**
				 * version 0.0.4 <measure> <annotations/> <chords></chords>
				 * <directions/> <staves></staves> <labels></labels> <lyrics/>
				 * <notes></notes> <rests></rests> <attribute>TEXT</attribute>
				 * <repeat>TEXT</repeat> </measure>
				 * **/
				NodeList childNodes = measure.getChildNodes();

				for (int k = 0; k < childNodes.getLength(); k++) {
					/**
					 * modification about measure tag convert to structure of
					 * version 1.0.0
					 * **/
					Node currentNode = childNodes.item(k);

					/**
					 * version 1.0.0 <annotations/> <directions></directions>
					 * <staves></staves> <notes/> <rests/>
					 * <attribute></attribute> <repeat></repeat>
					 * **/

					if (currentNode.getNodeName().equals("annotations")) {
						addAnnotationElement(doc, i, measure, currentNode);
						continue;
					}

					if (currentNode.getNodeName().equals("chords")) {
						measure.removeChild(currentNode);
						continue;
					}

					if (currentNode.getNodeName().equals("labels")) {
						measure.removeChild(currentNode);
						continue;
					}

					if (currentNode.getNodeName().equals("lyrics")) {
						measure.removeChild(currentNode);
						continue;
					}

					if (currentNode.getNodeName().equals("attribute")) {
						Node attributesNew = doc.createElement("attribute");
						Node attributesNewChild = doc
								.createElement("measureAttribute");
						String text = mMeasureAttributes.get(i).name();
						// Text attributesNewChildText=doc.createTextNode(text);
						attributesNewChild.setTextContent(text);
						attributesNew.appendChild(attributesNewChild);
						measure.replaceChild(attributesNew, currentNode);
						continue;
					}

				}

				/**
				 * //add child node to it Node annotationOld=childNodes.item(0);
				 * //remove old nodes Node chordsOld=childNodes.item(1); Node
				 * labelsOld=childNodes.item(4); Node
				 * lyricsOld=childNodes.item(5); //replace old Node
				 * attributesOld=childNodes.item(8);
				 * 
				 * Node attributesNew=doc.createElement("attribute"); Node
				 * attributesNewChild=doc.createElement("measureAttribute");
				 * String text=mMeasureAttributes.get(i).name(); //Text
				 * attributesNewChildText=doc.createTextNode(text);
				 * attributesNewChild.setTextContent(text);
				 * attributesNew.appendChild(attributesNewChild);
				 * measure.replaceChild(attributesNew, attributesOld);
				 * 
				 * measure.removeChild(lyricsOld);
				 * measure.removeChild(labelsOld);
				 * measure.removeChild(chordsOld);
				 **/

			}

		}

		try {
			writeOutConvertedResult(doc);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void addAnnotationElement(
			Document doc,
			int i,
			Element measure,
			Node annotationOld) {

		ArrayList<AnnotationElement> currentAnnotation = mAnnotaionElement
				.get(i);

		if (currentAnnotation.size() != 0) {

			Node annotationNew = doc.createElement("annotations");

			for (AnnotationElement child : currentAnnotation) {
				if (child instanceof Chord) {
					/*
					 * <annotationElement
					 * class="de.songeasy.android.score.Chord"text=""
					 * relativePosition="" bass="" bassAccidental="" kind=""
					 * root="" rootAccidental=""/>
					 */
					Chord temple = (Chord) child;

					Element annotation = doc.createElement("annotationElement");

					String className = Chord.class.getName();
					Attr attrClass = doc.createAttribute("class");
					attrClass.setValue(className);
					annotation.setAttributeNode(attrClass);

					Attr attrText = doc.createAttribute("text");
					attrText.setValue(temple.getText());
					annotation.setAttributeNode(attrText);

					Attr relativePosition = doc
							.createAttribute("relativePosition");
					relativePosition.setValue(String.valueOf(temple
							.getRelativePosition()));
					annotation.setAttributeNode(relativePosition);

					Attr bass = doc.createAttribute("bass");
					bass.setValue(temple.getBass());
					annotation.setAttributeNode(bass);

					Attr bassAccidental = doc.createAttribute("bassAccidental");
					bassAccidental.setValue(temple.getBassAccidental());
					annotation.setAttributeNode(bassAccidental);

					Attr kind = doc.createAttribute("kind");
					kind.setValue(temple.getKind().name());
					annotation.setAttributeNode(kind);

					Attr attrRoot = doc.createAttribute("root");
					attrRoot.setValue(temple.getRoot());
					annotation.setAttributeNode(attrRoot);

					Attr rootAccidental = doc.createAttribute("rootAccidental");
					rootAccidental.setValue(temple.getRootAccidental());
					annotation.setAttributeNode(rootAccidental);

					annotationNew.appendChild(annotation);
				}

				if (child instanceof Lyric) {
					/*
					 * <annotationElement
					 * class="de.songeasy.android.score.Lyric" text=""
					 * relativePosition="" lineNumber="" staffNumber=""/>
					 */
					Lyric temple = (Lyric) child;

					Element annotation = doc.createElement("annotationElement");
					annotation.setAttribute("class", Lyric.class.getName());
					annotation.setAttribute("text", temple.getText());
					annotation.setAttribute("relativePosition",
							String.valueOf(temple.getRelativePosition()));
					annotation.setAttribute("lineNumber",
							String.valueOf(temple.getLineNumber()));
					annotation.setAttribute("staffNumber",
							String.valueOf(temple.getStaffNumber()));

					annotationNew.appendChild(annotation);
				}

				if (child instanceof Label) {
					/*
					 * <annotationElement
					 * class="de.songeasy.android.score.Label" text=""
					 * relativePosition=""/>
					 */
					Label temple = (Label) child;

					Element annotation = doc.createElement("annotationElement");
					annotation.setAttribute("class", Label.class.getName());
					annotation.setAttribute("text", temple.getText());
					annotation.setAttribute("relativePosition",
							String.valueOf(temple.getRelativePosition()));

					annotationNew.appendChild(annotation);
				}
			}

			measure.replaceChild(annotationNew, annotationOld);

		}
	}

}