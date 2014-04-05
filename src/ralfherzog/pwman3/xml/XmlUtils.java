package ralfherzog.pwman3.xml;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * XML Helper class
 * @author Ralf Herzog
 */
public class XmlUtils {

	/**
	 * Jumps to next opend element
	 * @param xmlParser
	 * @return boolean
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static boolean skipToNextStartTag( XmlPullParser xmlParser ) throws XmlPullParserException, IOException {
		boolean startTagFound = false;
		while( xmlParser.next() != XmlPullParser.START_TAG ) {
			if ( xmlParser.getEventType() == XmlPullParser.END_DOCUMENT ) {
				return false;
			}
			startTagFound = true;
		}
		return startTagFound;
	}
	
	/**
	 * Returns the content of a element
	 * @param xmlParser
	 * @return String - Inhalt
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String readText( XmlPullParser xmlParser ) throws IOException, XmlPullParserException {
	    String result = "";
	    if ( xmlParser.next() == XmlPullParser.TEXT ) {
	        result = xmlParser.getText();
	        xmlParser.next();
	    }
	    return result;
	}
	
}
