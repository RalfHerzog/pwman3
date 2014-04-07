package ralfherzog.pwman3.database.sqlite.tables;

import java.util.ArrayList;

import android.content.ContentValues;
import android.util.Base64;
import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.DatabaseConstants;
import ralfherzog.pwman3.database.sqlite.SQLiteSelect;

public class SQLiteTableKey extends SQLiteTable {

	public SQLiteTableKey() {
		super( DatabaseConstants.Key.table );
	}

	public boolean hasCryptedKey() {
		return getKeyCrypted() != null;
	}
	
	public byte[] getKeyCrypted() {
		SQLiteSelect sqLiteSelect = new SQLiteSelect();
		sqLiteSelect.table = DatabaseConstants.Key.table;
		sqLiteSelect.columns = DatabaseConstants.Key.columns;
		
		ArrayList<ContentValues> databaseKeyData = Database.getInstance().select( sqLiteSelect, 1 );
		if ( databaseKeyData.size() == 0 ) {
			return null;
		}
		ContentValues keyRow = databaseKeyData.get( 0 );
		String keyBase64 = keyRow.getAsString( DatabaseConstants.Key.columnTheKey );
		return Base64.decode( keyBase64, Base64.DEFAULT );
	}
	
	public boolean insertKeyCrypted( String theKey ) {
		ContentValues keyData = new ContentValues();
		keyData.put( DatabaseConstants.Key.columnTheKey, theKey );
		
		long rowId = Database.getInstance().insert( DatabaseConstants.Key.table, keyData );
		return rowId >= 0;
	}

}
