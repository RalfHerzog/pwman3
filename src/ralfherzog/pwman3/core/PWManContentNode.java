package ralfherzog.pwman3.core;

import java.util.ArrayList;

import android.util.Base64;

import ralfherzog.pwman3.cipher.Cipher;

/**
 * A content node which holds all data stored in a node
 * @author RalfHerzog
 */
public class PWManContentNode {
	
	private String databaseRowOriginal;
	
	private Integer id;
	private String userName;
	private String password;
	private String url;
	private String notes;
	private ArrayList<String> tags;
	
	private final String NODE_DATA_SEPARATOR = "##";
	private final String NODE_DATA_END = "**";
	
	private final String NODE_USERNAME 	= "username:";
	private final String NODE_PASSWORD 	= "password:";
	private final String NODE_URL 		= "url:";
	private final String NODE_NOTES 	= "notes:";
	private final String NODE_TAGS 		= "tags:";
	private final String NODE_TAG 		= "tag:";
	private final String NODE_TAG_END 	= "**endtag**";

	private Cipher cipher;
	
	public PWManContentNode( Cipher cipher ) {
		this.cipher = cipher;
	}
	
	/**
	 * Parse a row from database as stored
	 * @param cipher 
	 * @param databaseRow - String
	 * @return boolean
	 */
	public boolean parseNodeDatabaseRow( String databaseRow ) {
		
		this.databaseRowOriginal = databaseRow;
		
		this.userName = parseNodeValue( databaseRow, NODE_USERNAME );
		this.password = parseNodeValue( databaseRow, NODE_PASSWORD );
		this.url = parseNodeValue( databaseRow, NODE_URL );
		this.notes = parseNodeValue( databaseRow, NODE_NOTES );
		
		this.tags = parseNodeTags( databaseRow );
		
		return decrypt();
	}

	/**
	 * Parse a database row value
	 * @param databaseRow - String
	 * @param node - The node to parse (eg. username)
	 * @return String - value of node
	 */
	private String parseNodeValue( String databaseRow, String node ) {
		
		int startPos = databaseRow.indexOf( node );
		String nodeBegin = databaseRow.substring( startPos );
		
		int endPos = nodeBegin.indexOf( NODE_DATA_SEPARATOR );
		if ( endPos == -1 ) {
			endPos = nodeBegin.indexOf( NODE_DATA_END );
		}
		String nodeValue = nodeBegin.substring( node.length(), endPos - 1 );
		
		return nodeValue;
	}
	
	/**
	 * Parse tags stored in database row
	 * @param databaseRow - String
	 * @return ArrayList&lt;String&gt; - Array of tags
	 */
	private ArrayList<String> parseNodeTags( String databaseRow ) {
		
		if ( !databaseRow.contains( NODE_TAGS ) ) {
			return null;
		}
		
		int startPos = databaseRow.indexOf( NODE_TAGS );
		
		String tags = databaseRow.substring( startPos + NODE_TAGS.length() );
		
		ArrayList<String> tagList = new ArrayList<String>();
		while( tags.length() > NODE_TAG.length() ) {
			tags = tags.substring( NODE_TAG.length() );
			
			int endPos = tags.indexOf( NODE_TAG_END );
			if ( endPos == 0 ) {
				// No tags stored
				break;
			}
			
			String tag = tags.substring( 0, endPos - 1 );
			
			tagList.add( tag );
			
			startPos = tags.indexOf( NODE_TAG );
			if ( startPos == -1 ) {
				// No more tags
				break;
			}
			tags = tags.substring( startPos );
		}
		return tagList;
	}
	
	/**
	 * Decrypt the data stored in this object
	 * @return boolean - always true
	 */
	public boolean decrypt() {
		byte[] data = null;
		
		// Username
		try {
			data = Base64.decode( userName, Base64.DEFAULT );
			userName = new String( cipher.decrypt( data ) );
			userName = stripTailingWhiteSpaces( userName );
		} catch( IllegalArgumentException e ) {
			userName = "<ERROR>";
		}
		
		// Password
		try {
			data = Base64.decode( password, Base64.DEFAULT );
			password = new String( cipher.decrypt( data ) );
			password = stripTailingWhiteSpaces( password );
		} catch( IllegalArgumentException e ) {
			password = "";
		}
		
		// URL
		try {
			data = Base64.decode( url, Base64.DEFAULT );
			url = new String( cipher.decrypt( data ) );
			url = stripTailingWhiteSpaces( url );
		} catch( IllegalArgumentException e ) {
			url = "";
		}
		
		// Notes
		try {
			data = Base64.decode( notes, Base64.DEFAULT );
			notes = new String( cipher.decrypt( data ) );
			notes = stripTailingWhiteSpaces( notes );
		} catch( IllegalArgumentException e ) {
			notes = "";
		}
		
		// Tags
		ArrayList<String> newTags = new ArrayList<String>();
		for( String tag : tags ) {
			try {
				data = Base64.decode( tag, Base64.DEFAULT );
			} catch( IllegalArgumentException e ) {
				data = null;
			}
			
			String tagPlain = new String( cipher.decrypt( data ) );
			tagPlain = stripTailingWhiteSpaces( tagPlain );
			
			newTags.add( tagPlain );
		}
		tags = newTags;
		
		return true;
	}
	
	/**
	 * Generates the database data row which could be saved. This is the opposite of parseNodeDatabaseRow().
	 * @return String - encrypted database row
	 */
	public String generateDatabaseString() {
		String databaseRow = "";
		
		databaseRow += NODE_USERNAME + encryptData( getUserName() ) + NODE_DATA_SEPARATOR;
		databaseRow += NODE_PASSWORD + encryptData( getPassword() ) + NODE_DATA_SEPARATOR;
		databaseRow += NODE_URL + encryptData( getUrl() ) + NODE_DATA_SEPARATOR;
		databaseRow += NODE_NOTES + encryptData( getNotes() ) + NODE_DATA_SEPARATOR;
		
		databaseRow += NODE_TAGS;
		for ( String tag : getTags() ) {
			databaseRow += NODE_TAG + encryptData( tag ) + "\n" + NODE_TAG_END;
		}
		
		return databaseRow;
	}
	
	/**
	 * Encrypts a string so that it become part of database node row
	 * @param cipher
	 * @param plaintext
	 * @return String - encrypted
	 */
	private String encryptData( String plaintext ) {
		if ( plaintext == null ) {
			plaintext = "";
		}
		return Base64.encodeToString( cipher.encrypt( plaintext.getBytes(), true ), Base64.DEFAULT );
	}
	
	/**
	 * Removes tailing withspaces
	 * @param nodeValue - String
	 * @return String - striped String
	 */
	private String stripTailingWhiteSpaces( String nodeValue ) {
		int lastPos = nodeValue.length();
		for ( lastPos-- ; lastPos >= 0 ; lastPos-- ) {
			if ( nodeValue.charAt( lastPos ) != ' ' ) {
				break;
			}
		}
		nodeValue = nodeValue.substring( 0, lastPos + 1 );
		return nodeValue;
	}
	
	public String getDatabaseRowOriginal() {
		return databaseRowOriginal;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		if (this.id != null) {
			throw new RuntimeException( "Do you really want to modify the content node's database id?" );
		}
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public ArrayList<String> getTags() {
		if ( tags == null ) {
			return new ArrayList<String>( 0 );
		}
		return tags;
	}
	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

}
