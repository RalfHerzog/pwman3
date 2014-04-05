package ralfherzog.pwman3.database.sqlite.tables;

import java.util.ArrayList;
import java.util.Iterator;

import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.sqlite.SQLiteSelect;
import ralfherzog.pwman3.database.sqlite.column.SQLiteColumn;

import android.content.ContentValues;
import android.database.SQLException;

public class SQLiteTable {
	
	private String tableName;
	private ArrayList<SQLiteColumn> columns;
	
	protected SQLiteTable( String name ) {
		setTableName( name );
		setColumns( new ArrayList<SQLiteColumn>() );
	}
	
	public void create() throws SQLException {
		Database.getInstance().execute( getSQLCreate() );
	}
	
	public ArrayList<ContentValues> select() {
		return select( null, null, null, 0 );
	}
	public ArrayList<ContentValues> select( int count ) {
		return select( null, null, null, count );
	}
	public ArrayList<ContentValues> select( String[] columns, int count ) {
		return select( columns, null, null, count );
	}
	public ArrayList<ContentValues> select( String[] columns, String selection, int count ) {
		return select( columns, selection, null, count );
	}
	public ArrayList<ContentValues> select( String[] columns, String selection, String orderBy, int count ) {
		SQLiteSelect select = new SQLiteSelect();
		select.table = tableName;
		select.selection = selection;
		select.orderBy = orderBy;
		
		if ( columns == null ) {
			columns = new String[ this.columns.size() ];
			for ( int i = 0 ; i < this.columns.size() ; i++ ) {
				columns[ i ] = this.columns.get( i ).getName();
			}
		}
		select.columns = columns;
		
		return Database.getInstance().select( select, count );
	}
	
	public long insert( ContentValues contentValues ) {
		return Database.getInstance().insert( tableName, contentValues );
	}
	
	public void deleteFrom() throws SQLException {
		Database.getInstance().execute( "DELETE FROM " + tableName + ";" );
	}
	public void deleteFromWhere( String where ) throws SQLException {
		Database.getInstance().execute( "DELETE FROM " + tableName + " WHERE " + where + ";" );
	}
	public void drop() {
		Database.getInstance().execute( "DROP TABLE " + tableName + ";" );
	}
	
	private String getSQLCreate() {
		String sql = "CREATE TABLE IF NOT EXISTS ";
		
		sql += tableName;
		sql += " ( ";
		
		Iterator<SQLiteColumn> columnIterator = columns.iterator();
		while( columnIterator.hasNext() ) {
			SQLiteColumn column = columnIterator.next();
			
			sql += column.getName();
			sql += " ";
			sql += column.getTypeAsString();
			
			if ( column.getIsPrimaryKey() != null ) {
				sql += " PRIMARY KEY";
			}
			if ( column.getIsAutoIncrement() != null ) {
				sql += " AUTOINCREMENT";
			}
			if ( column.getIsUnsigned() != null ) {
				sql += " UNSIGNED";
			}
			if ( column.getIsNull() != null ) {
				if ( !column.getIsNull() ) {
					sql += " NOT";
				}
				sql += " NULL";
			}
			if ( column.getIsUnique() != null ) {
				if ( column.getIsUnique() ) {
					sql += " UNIQUE";
				}
			}
			if ( column.getDefault() != null ) {
				sql += " DEFAULT '" + column.getDefault() + "'";
			}
			
			if ( columnIterator.hasNext() ) {
				sql += ", ";
			}
		}
		sql += " ); ";
		return sql;
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName( String tableName ) {
		this.tableName = tableName;
	}
	public ArrayList<SQLiteColumn> getColumns() {
		return columns;
	}
	public void setColumns( ArrayList<SQLiteColumn> columns ) {
		this.columns = columns;
	}
	public SQLiteTable withName( String name ) {
		this.setTableName( name );
		return this;
	}
	public SQLiteTable withColumns( ArrayList<SQLiteColumn> columns ) {
		this.setColumns( columns );
		return this;
	}
	public boolean addColumn( SQLiteColumn column ) {
		return columns.add( column );
	}

}
