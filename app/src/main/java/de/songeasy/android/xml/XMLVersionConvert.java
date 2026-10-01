/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;
import de.songeasy.android.fileactivity.FileVersion;
import de.songeasy.android.interfaces.XMLConvertInterface;

/**
 * Convert song file to newest version. Check file version and call respective
 * converter. If no matching converter found, file is left unchanged.
 * 
 * @author fulong/krein
 * 
 */
public class XMLVersionConvert {

	private File xmlSource;

	/*
	 * XmlPullParse get the necessary nodes DOM to remove and add them to the
	 * document
	 */
	private XmlPullParser xmlPull;


	/**
	 * Get converted song file.
	 * 
	 * @return
	 */
	public File getConvertedXml() {
		return xmlSource;
	}


	private void setXmlSource(File xmlSource) {
		this.xmlSource = xmlSource;
	}


	/**
	 * Constructor of class
	 * 
	 * @param xmlSource
	 *            xml song file to convert
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public XMLVersionConvert(File xmlSource) throws XmlPullParserException,
			IOException, ParserConfigurationException, SAXException {
		setXmlSource(xmlSource);

	}


	/**
	 * Do conversion of file. If conversion is necessary file link is redirected
	 * to converted result.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void convert() throws XmlPullParserException, IOException,
			ParserConfigurationException, SAXException {

		XMLConvertInterface converter = null;

		String versionString = versionDetect();

		// set suitable converter
		if (FileVersion.V004.isVersion(versionString))
			converter = new Convert004To100(xmlSource, xmlPull);

		if (FileVersion.V100.isVersion(versionString))
			converter = new Convert100To101(xmlSource, xmlPull);

		if (converter != null) {
			// call converting routine
			converter.convertProcedure();
			xmlSource = converter.getXmlSource();
		}
	}


	/**
	 * Detection of song version. Enables call of specific file converter later.
	 * 
	 * @return String with version name
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private String versionDetect() throws XmlPullParserException, IOException {

		InputStream reader = new FileInputStream(xmlSource);
		xmlPull = Xml.newPullParser();
		xmlPull.setInput(reader, null);
		String versionValue = null;

		if (xmlPull.getEventType() == XmlPullParser.START_DOCUMENT) {

			xmlPull.next();

			if (xmlPull.getEventType() == XmlPullParser.START_TAG
					&& xmlPull.getName().equals("song")) {

				versionValue = xmlPull.getAttributeValue(0);
			}
		}

		return versionValue;
	}
}
