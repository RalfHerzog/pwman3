package ralfherzog.pwman3.database.sqlite.column;

public class SQLiteColumnFactory {
	public static SQLiteColumn createTableColumnObject( String name, String type ) {
		return createTableColumnObject( name, type, null, null, null, null, null );
	}
	
	public static SQLiteColumn createTableColumnObject( 
			String name, 
			String type, 
			Boolean isPrimaryKey, 
			Boolean isNull, 
			Boolean isUnsigned,
			String comment,
			String extraOptions
	) {
		SQLiteColumn sqLiteColumn = new SQLiteColumn( name, type );
		sqLiteColumn.setIsPrimaryKey( isPrimaryKey );
		sqLiteColumn.setIsNull( isNull );
		sqLiteColumn.setIsUnsigned( isUnsigned );
		sqLiteColumn.setComment( comment );
		sqLiteColumn.setExtraOptions( extraOptions );
		return sqLiteColumn;
	}
}
