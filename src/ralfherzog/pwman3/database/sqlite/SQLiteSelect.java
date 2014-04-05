package ralfherzog.pwman3.database.sqlite;

/**
 * Class for all data which is necessary for a SQL-Select 
 */
public class SQLiteSelect {
	public String table = null;
	public String[] columns = null;
	public String selection = null;
	public String[] selectionArgs = null;
	public String groupBy = null;
	public String having = null;
	public String orderBy = null;
	
	public boolean isCorrect() {
		return ( this.table != null && this.table != "" && this.columns != null );
	}
}