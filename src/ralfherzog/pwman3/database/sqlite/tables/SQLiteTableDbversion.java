package ralfherzog.pwman3.database.sqlite.tables;

import ralfherzog.pwman3.database.DatabaseConstants;
import android.content.ContentValues;

public class SQLiteTableDbversion extends SQLiteTable {

	public SQLiteTableDbversion() {
		super( DatabaseConstants.DBVersion.table );
	}

	public long insertVersion( String DBVersion ) {
		ContentValues contentValues = new ContentValues();
		contentValues.put( DatabaseConstants.DBVersion.columnDBVersion, DBVersion );
		
		return insert( contentValues );
	}
}
