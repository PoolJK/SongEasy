/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.songeasy.android.fileactivity.FileVersion;
import de.songeasy.android.interfaces.XMLConvertInterface;
import de.songeasy.android.score.Measure;

/**
 * Conversion of song files from version 1.0.0 to version 1.0.1.
 * 
 * @author krein
 */

public class Convert100To101 implements XMLConvertInterface {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/**
	 * XML source file to be converted from version 0.0.4 to version 1.0.0
	 */
	private File xmlSource;

	/**
	 * XmlPullParse get the necessary nodes DOM to remove and add them to the
	 * document
	 */
	private XmlPullParser xmlPull;


	private void setXmlSource(File xmlSource) {
		this.xmlSource = xmlSource;
	}


	private void setXmlPullParser(XmlPullParser xmlPull) {
		this.xmlPull = xmlPull;
	}


	@Override
	public File getXmlSource() {

		return xmlSource;
	}


	/**
	 * Constuctor of class.
	 * 
	 * @param xmlSource
	 *            XML source file to get converted
	 * @param xmlPull
	 *            XML pull parser to be used
	 */
	public Convert100To101(File xmlSource, XmlPullParser xmlPull) {
		this.setXmlSource(xmlSource);
		this.setXmlPullParser(xmlPull);
	}


	@Override
	public void convertProcedure() throws XmlPullParserException, IOException,
			ParserConfigurationException, SAXException {

		// do conversion with created DOM
		xml_1_0_0_convert();
	}


	/**
	 * Creates DOM for original version. Manipulates DOM nodes to convert to new
	 * version.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void xml_1_0_0_convert() throws ParserConfigurationException,
			SAXException, IOException {

		/** create new DOM document and parse original XML file into it */
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = db.parse(xmlSource);

		// get root element
		Element root = doc.getDocumentElement();

		// change to target version identifier
		if (root.getNodeName().equals("song")) {
			root.setAttribute("version", FileVersion.V101.getVersionString());
		}

		// find all measureattributes
		NodeList measureAttributeNodes = doc
				.getElementsByTagName("measureAttribute");

		// convert attributes
		for (int n = 0; n < measureAttributeNodes.getLength(); n++) {

			Node attributeNode = measureAttributeNodes.item(n);
			String content = attributeNode.getTextContent();
			// Log.d(TAG, String.format("Node Value: %s", content));
			// change LAST_IN_LINE with LINE_BREAK
			if (content.equals("LAST_IN_LINE")) {
				attributeNode
						.setTextContent(Measure.MeasureAttribute.LINE_BREAK
								.toString());
			} else if (content.equals("FIRST_IN_LINE")) {
				attributeNode.getParentNode().removeChild(attributeNode);
			}
		}

		// find all notes
		NodeList noteNodes = doc.getElementsByTagName("note");

		// remove note tie attributes
		for (int n = 0; n < noteNodes.getLength(); n++) {

			Node noteNode = noteNodes.item(n);
			if (((Element) noteNode).hasAttribute("tie"))
				((Element) noteNode).removeAttribute("tie");
		}

		try {
			writeOutConvertedResult(doc);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Write converted file from memory to disk
	 * 
	 * @param doc
	 *            DOM document to write
	 * @throws TransformerException
	 */
	private void writeOutConvertedResult(Document doc)
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

}
