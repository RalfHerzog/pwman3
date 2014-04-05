package ralfherzog.pwman3.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ralfherzog.pwman3.MainActivity;
import ralfherzog.pwman3.database.DatabaseConstants;
import ralfherzog.pwman3.database.sqlite.column.SQLiteColumn;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTable;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTableDbversion;

import android.util.Xml;

/**
 * Parses the XML-File which sets the tablestructure <br/>
 * In this class I am using the XmlPullParser. Implemented is it as a recursive decent-parser.
 * The parser has no look-ahead and is therefore a LL0-Parser with level restriction of the elements.
 * 
 * Beware of elements with multiple childs. The while statement only differs on ">=" and ">".
 * @author RalfHerzog
 */
public class DatabaseParser {
	
	private static XmlPullParser xmlParser;
    private static final String xmlNamespace = null;
	private String databaseStructureFile;

	/**
	 * Constructor with path of xml file as parameter
	 * @param databaseStructureFile - String
	 */
	public DatabaseParser( String databaseStructureFile ) {
		this.databaseStructureFile = databaseStructureFile;
	}

	/**
	 * Parse the table structure
	 * @return ArrayList&lt;SQLiteTable&gt; - Tablestructure as array of tables
	 */
	public ArrayList<SQLiteTable> parse() {
		
		ArrayList<SQLiteTable> tables = null;
		try{
			InputStream in = MainActivity.getContext().getAssets().open( databaseStructureFile );
			xmlParser = Xml.newPullParser();
			xmlParser.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, false );
			xmlParser.setInput( in, null );
			xmlParser.nextTag();
			
			tables = parseDatabaseXml();
		} catch ( XmlPullParserException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return tables;
	}

	private ArrayList<SQLiteTable> parseDatabaseXml() throws XmlPullParserException, IOException {
		ArrayList<SQLiteTable> tables = new ArrayList<SQLiteTable>();
		
		while ( xmlParser.getEventType() != XmlPullParser.END_DOCUMENT ) {
			if ( xmlParser.getEventType() != XmlPullParser.START_TAG ) {
				xmlParser.next();
	            continue;
	        }
			
			String xmlTagName = xmlParser.getName();
			if ( xmlTagName.equals( "tables" ) ) {
				
				tables = new ArrayList<SQLiteTable>();
				
				parseXmlTables( tables );
			} else {
				xmlParser.next();
			}
		}
		
		return tables;
	}

	private void parseXmlTables( ArrayList<SQLiteTable> tables ) throws XmlPullParserException, IOException {
		
		XmlUtils.skipToNextStartTag( xmlParser ); // Should skip to 'table'
		int depth = xmlParser.getDepth();
		
		while ( xmlParser.getEventType() != XmlPullParser.END_TAG && xmlParser.getDepth() >= depth ) {
			if ( xmlParser.getEventType() != XmlPullParser.START_TAG ) {
				xmlParser.next();
	            continue;
	        }
			
			String xmlTagName = xmlParser.getName();
			if ( xmlTagName.equals( "table" ) ) {
				SQLiteTable table = parseXmlTable();
				tables.add( table );
			} else {
				xmlParser.next();
			}
		}
		
	}

	private SQLiteTable parseXmlTable() throws XmlPullParserException, IOException {
		
		String tableName = xmlParser.getAttributeValue( xmlNamespace, "name" );
		
		SQLiteTable table = null;
		
		try {
			tableName = tableName.toUpperCase( Locale.US ).charAt(0) + tableName.toLowerCase( Locale.US ).substring( 1 );
			Class<?> tableClass = Class.forName( "ralfherzog.pwman3.database.sqlite.tables" + "." + "SQLiteTable" + tableName );
			table = (SQLiteTable)tableClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			
			// Class not found, use the old method
			// TODO: complete this
			if ( tableName.equals( DatabaseConstants.DBVersion.table ) ) {
				table = new SQLiteTableDbversion();
			} else {
				throw new XmlPullParserException( "Table: " + tableName + " not known" );
			}
		}
		
		int depth = xmlParser.getDepth();
		
		XmlUtils.skipToNextStartTag( xmlParser ); // Should skip to 'columns'
		while ( xmlParser.getEventType() != XmlPullParser.END_TAG && xmlParser.getDepth() > depth ) {
			if ( xmlParser.getEventType() != XmlPullParser.START_TAG ) {
				xmlParser.next();
	            continue;
	        }
			
			String xmlTagName = xmlParser.getName();
			if ( xmlTagName.equals( "columns" ) ) {
				parseXmlTableColumns( table );
			} else {
				xmlParser.next();
			}
		}
		return table;
	}

	private void parseXmlTableColumns( SQLiteTable table ) throws XmlPullParserException, IOException {
		
		XmlUtils.skipToNextStartTag( xmlParser ); // Should skip to 'column'
		int depth = xmlParser.getDepth();
		
		while ( xmlParser.getEventType() != XmlPullParser.END_TAG && xmlParser.getDepth() >= depth ) {
			if ( xmlParser.getEventType() != XmlPullParser.START_TAG ) {
				xmlParser.next();
	            continue;
	        }
			
			String xmlTagName = xmlParser.getName();
			if ( xmlTagName.equals( "column" ) ) {
				SQLiteColumn column = parseXmlTableColumn();
				table.addColumn( column );
			} else {
				xmlParser.next();
			}
		}
	}


	private SQLiteColumn parseXmlTableColumn() throws XmlPullParserException, IOException {
		
		String columnName = xmlParser.getAttributeValue( xmlNamespace, "name" );
		String columnType = xmlParser.getAttributeValue( xmlNamespace, "type" );

		SQLiteColumn column = new SQLiteColumn( columnName, columnType );
		
		int depth = xmlParser.getDepth();
		XmlUtils.skipToNextStartTag( xmlParser ); // Should skip to 'column'
		
		if ( xmlParser.getDepth() <= depth ) {
			// Optional tags missing, return
			return column;
		}

		do {
			if ( xmlParser.getEventType() != XmlPullParser.START_TAG ) {
				xmlParser.next();
	            continue;
	        }
			
			if ( xmlParser.getDepth() == depth ) {
				// Found tag on same depth, because multiple are allowed we must return here
				break;
			}
			if ( xmlParser.getDepth() < depth ) {
				// XmlUtils.skipToNextStartTag( xmlParser );
				break;
			}
			parseXmlTableColumnOptions( column );
		} while( XmlUtils.skipToNextStartTag( xmlParser ) );
		return column;
	}
	
	private void parseXmlTableColumnOptions( SQLiteColumn column ) throws XmlPullParserException, IOException {
		String xmlTagName = xmlParser.getName();
		if ( xmlTagName.equals( "isPrimaryKey" ) ) {
			String value = XmlUtils.readText( xmlParser );
			column.setIsPrimaryKey( Boolean.valueOf( value ) );
		} else if ( xmlTagName.equals( "isNull" ) ) {
			String value = XmlUtils.readText( xmlParser );
			column.setIsNull( Boolean.valueOf( value ) );
		} else if ( xmlTagName.equals( "isUnique" ) ) {
			String value = XmlUtils.readText( xmlParser );
			column.setUnique( Boolean.valueOf( value ) );
		} else if ( xmlTagName.equals( "default" ) ) {
			column.setDefault( XmlUtils.readText( xmlParser ) );
		} else if ( xmlTagName.equals( "isUnsigned" ) ) {
			String value = XmlUtils.readText( xmlParser );
			column.setIsUnsigned( Boolean.valueOf( value ) );
		} else if ( xmlTagName.equals( "isAutoIncrement" ) ) {
			String value = XmlUtils.readText( xmlParser );
			column.setIsAutoIncrement( Boolean.valueOf( value ) );
		} else if ( xmlTagName.equals( "comment" ) ) {
			column.setComment( XmlUtils.readText( xmlParser ) );
		}
	}
	
}
