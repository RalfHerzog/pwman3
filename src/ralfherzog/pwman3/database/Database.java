package ralfherzog.pwman3.database;

import java.io.File;
import java.util.ArrayList;

import ralfherzog.pwman3.activities.main.MainActivity;
import ralfherzog.pwman3.database.sqlite.SQLiteSelect;
import ralfherzog.pwman3.database.sqlite.column.SQLiteColumn;
import ralfherzog.pwman3.database.sqlite.column.SQLiteColumnType;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTable;
import ralfherzog.pwman3.xml.DatabaseParser;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

/**
 * This is the interace to the sqlite database. <br/>
 * Here are basic methods such as (SELECT, INSERT, DELETE) defined.
 * @author RalfHerzog
 */
public class Database {

	/** Database folder */
	private final String DATABASE_FOLDER = "pwman3"; 
	/** Database file */
	private final String DATABASE_NAME = "pwman.db"; 
	
	private final String DATABASE_STRUCTURE_FILE = "database_structure_0_4.xml";
	
	/** Singleton Object */
	private static Database databaseObject; 
	/** The database connection */
	private SQLiteDatabase sqliteDatabase = null; 
	
	private ArrayList<SQLiteTable> tables;
	private String pwmanFolderDatabase;
	
	/**
	 * Singleton constructor
	 */
	private Database() {
		if ( !MainActivity.isRelease() ) {
			File dataDirectory = Environment.getExternalStorageDirectory();
			
			String pwmanDatabaseFolder = dataDirectory.getAbsolutePath() + File.separator + DATABASE_FOLDER + File.separator;
			if ( !( new File( pwmanDatabaseFolder ).isDirectory() ) ) {
				if ( !( new File( pwmanDatabaseFolder ).mkdirs() ) ) {
					Log.e("pwmanDatabaseFolder", "mkdirs(): create " + pwmanDatabaseFolder + " failed");
				}
			}
			pwmanFolderDatabase = pwmanDatabaseFolder + DATABASE_NAME;
			
			// Delete DB file for test purposes only
//			new File( pwmanFolderDatabase ).delete();
		}
		
	}
	
	private void init() {
		if ( !MainActivity.isRelease() ) {
			sqliteDatabase = SQLiteDatabase.openOrCreateDatabase( pwmanFolderDatabase, null );
		} else {
			sqliteDatabase = MainActivity.getContext().openOrCreateDatabase( DATABASE_NAME, Context.MODE_PRIVATE, null );
		}
		
		DatabaseParser databaseParser = new DatabaseParser( DATABASE_STRUCTURE_FILE );
		tables = databaseParser.parse();
		
		for ( SQLiteTable table : tables ) {
			table.create();
		}
		
		checkDBVersion();
	}

	/**
	 * Checks the database version and performs an update if necessary.
	 * The update consist on saving data, deleting the tables and recreate them.
	 */
	private void checkDBVersion() {
		// TODO: Complete this
	}

	/**
	 * Executes an INSERT-Statement. The data is passed as {@link ContentValues}.
	 * @param table - The table
	 * @param contentValues - The columndata
	 * @return long - insert id, -1 if error occoured
	 */
	public long insert( String table, ContentValues contentValues ) {
		return sqliteDatabase.insert( table, null, contentValues );
	}
	
	/**
	 * Executes a sql-statement
	 * @param sql - SQL Code
	 * @throws SQLException If invalid SQL-Statement
	 */
	public void execute( String sql ) throws SQLException {
		sqliteDatabase.execSQL( sql );
	}

	/**
	 * Executes a SELECT-Statement. 
	 * @param sqlQuery - SQLQuery Object with SELECT data.
	 * @param count - Number of rows to read. 0 for infinite
	 * @return ArrayList&lt;ContentValues&gt; - ArrayList of columnvalues
	 * @throws IllegalArgumentException
	 * @see SQLiteDatabase
	 */
	public ArrayList<ContentValues> select( SQLiteSelect sqlQuery, int count ) throws IllegalArgumentException, SQLiteException {
		
		if ( !sqlQuery.isCorrect() ) {
			throw new IllegalArgumentException( "Not all necessary data set" );
		}
		
		// Check for existing table
		SQLiteTable table = getSQLiteTableByName( sqlQuery.table );
		if ( table == null ) {
			throw new SQLiteException( "Table '" + sqlQuery.table + "' not found in XML-Tree" );
		}
		
		// Check columns availability
		for ( String column : sqlQuery.columns ) {
			if ( getSQLiteColumnByName( table, column ) == null ) {
				throw new SQLiteException( "Column '" + column + "' not found in table '" + sqlQuery.table + "' out of XML-Tree" );
			}
		}
		
		Cursor cursor = sqliteDatabase.query( 
				sqlQuery.table, 
				sqlQuery.columns, 
				sqlQuery.selection, 
				sqlQuery.selectionArgs, 
				sqlQuery.groupBy, 
				sqlQuery.having, 
				sqlQuery.orderBy
		);
		return doSelect( table, cursor, count );
	}
	
	/**
	 * Readout the data from resulttable
	 * @param table 
	 * @param cursor - Resulttable
	 * @param count - Number of rows to read. 0 for infinite
	 * @return ArrayList&lt;ContentValues&gt; - ArrayList of columnvalues
	 * @throws IllegalArgumentException
	 * @see Cursor
	 */
	private ArrayList<ContentValues> doSelect( SQLiteTable table, Cursor cursor, int count ) throws IllegalArgumentException {
		
		boolean infiniteLimit = ( count == 0 );
		
		String columns[] = cursor.getColumnNames();
		
		ArrayList<ContentValues> dataRows = new ArrayList<ContentValues>();
		if ( cursor.moveToFirst() ) {
			do {
				// Get data
				ContentValues contentValues = new ContentValues();
				for ( String columnName : columns ) {
	        		int columnIndex = cursor.getColumnIndexOrThrow( columnName );
	        		
	        		SQLiteColumn column = getSQLiteColumnByName( table, columnName );
	        		switch( column.getPrimitiveType() ) {
	        			case SQLiteColumnType.TYPE_BLOB:
		        			byte[] blob = cursor.getBlob( columnIndex );
		        			contentValues.put( columns[ columnIndex ], blob );
		        			break;
	        			case SQLiteColumnType.TYPE_INTEGER:
		        			int integer = cursor.getInt( columnIndex );
		        			contentValues.put( columns[ columnIndex ], integer );
		        			break;
	        			case SQLiteColumnType.TYPE_REAL:
	        				double number = cursor.getDouble( columnIndex );
		        			contentValues.put( columns[ columnIndex ], number );
		        			break;
	        			case SQLiteColumnType.TYPE_TEXT:
		        			String text = cursor.getString( columnIndex );
		        			contentValues.put( columns[ columnIndex ], text );
		        			break;
	        		}
	        		
	        	}
				dataRows.add( contentValues );
			} while( cursor.moveToNext() && ( infiniteLimit || --count > 0 ) );
			
        }
        if ( cursor != null && !cursor.isClosed() ) {
        	cursor.close();
        }
        
		return dataRows;
	}
	
	/**
	 * Returns a table to given name
	 * @param name  String
	 * @return SQLiteTable - {@link SQLiteTable} object or null
	 */
	public SQLiteTable getSQLiteTableByName( String name ) {
		for ( SQLiteTable table : tables ) {
			if ( table.getTableName().equals( name ) ) {
				return table;
			}
		}
		return null;
	}
	
	/**
	 * Returns a column to given name
	 * @param table - Table
	 * @param name - Columnname
	 * @return SQLiteColumn - {@link SQLiteTable} object or null
	 */
	public SQLiteColumn getSQLiteColumnByName( SQLiteTable table, String name ) {
		for( SQLiteColumn column : table.getColumns() ) {
			if ( column.getName().equals( name ) ) {
				return column;
			}
		}
		return null;
	}
	
	/**
	 * Returns all tables
	 * @return
	 */
	public ArrayList<SQLiteTable> getTables() {
		return tables;
	}
	
	/**
	 * Singleton method. Important is to call init(). The problem was that the
	 * methods in the constructor need an initialised object to work with.
	 * @return Database
	 */
	public static Database getInstance() {
		if ( databaseObject == null ) {
			databaseObject = new Database();
			databaseObject.init();
		}
		return databaseObject;
	}
	
	/**
	 * Closes the database connection
	 */
	public void shutdown() {
		try {
			sqliteDatabase.close();
		} catch ( IllegalStateException e ) {
			e.printStackTrace();
		}
	}
}
