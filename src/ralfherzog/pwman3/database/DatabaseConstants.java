package ralfherzog.pwman3.database;

public class DatabaseConstants {
	
	public static class Nodes {
		public static final String table = "NODES";
		
		public static final String columns[] = { "ID", "DATA" };
		public static final String columnID = columns[0];
		public static final String columnData = columns[1];
	}
	
	public static class Tags {
		public static final String table = "TAGS";
		
		public static final String columns[] = { "ID", "DATA" };
		public static final String columnID = columns[0];
		public static final String columnData = columns[1];
	}
	
	public static class Lookup {
		public static final String table = "LOOKUP";
		
		public static final String columns[] = { "NODE", "TAG" };
		public static final String columnNode = columns[0];
		public static final String columnTag = columns[1];
	}
	
	public static class Key {
		public static final String table = "KEY";
		
		public static final String columns[] = { "THEKEY" };
		public static final String columnTheKey = columns[0];
	}
	
	public static class DBVersion {
		public static final String table = "DBVERSION";
		
		public static final String columns[] = { "DBVERSION" };
		public static final String columnDBVersion = columns[0];
	}
}
