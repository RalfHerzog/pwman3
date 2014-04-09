package ralfherzog.pwman3.database.sqlite.tables;

import java.util.ArrayList;

import android.content.ContentValues;

import ralfherzog.pwman3.cipher.Cipher;
import ralfherzog.pwman3.core.PWManContentNode;
import ralfherzog.pwman3.database.DatabaseConstants;

public class SQLiteTableNodes extends SQLiteTable {

	public SQLiteTableNodes() {
		super( DatabaseConstants.Nodes.table );
	}
	
	public ArrayList<PWManContentNode> getNodes( Cipher cipher ) {
		ArrayList<ContentValues> selectResults = select();
		
		ArrayList<PWManContentNode> nodes = new ArrayList<PWManContentNode>();
		for ( ContentValues contentValues : selectResults ) {
			String databaseRow = new String( contentValues.getAsByteArray( DatabaseConstants.Nodes.columnData ) );
			
			PWManContentNode node = new PWManContentNode( cipher );
			node.parseNodeDatabaseRow( databaseRow );
			nodes.add( node );
		}
		return nodes;
	}

}
