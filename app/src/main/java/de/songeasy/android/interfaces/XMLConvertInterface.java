package de.songeasy.android.interfaces;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Interface for conversion of different sonf file variants. Functions must be
 * implemented to convert from version x to version y.
 * 
 * @author Fulong
 * **/

public interface XMLConvertInterface {

	/**
	 * Conversion procedure. Parse the whole xml document and call subroutines
	 * on detection of particular tags. Save detected elements, needed for
	 * conversion later on.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void convertProcedure() throws XmlPullParserException, IOException,
			ParserConfigurationException, SAXException;


	/**
	 * Get converted xml file
	 * 
	 * @return converted temporary xml file, needs to be deleted after reading
	 */
	public File getXmlSource();

}
