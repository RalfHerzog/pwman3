package ralfherzog.pwman3.cipher;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTable;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTableKey;

/**
 * The Cipher class. Here goes the decryption and encryption
 * @author RalfHerzog
 */
public class Cipher {
	
	/** Encryption/Decryption TAG */
	private final static String TAG = "PWMANCRYPTO";

	private byte[] password; // The supplied password for database
	private byte[] databaseKey; // The database key
	private Algorithm algorithm; // Used algorithm
	
	private SQLiteTableKey tableKey; // The database connection class

	private ArrayList<Algorithm> algorithms; // All supported algorithms
	
	public Cipher( SQLiteTableKey tableKey ) {
		this.tableKey = tableKey;
		
		algorithms = new ArrayList<Algorithm>();
		algorithms.add( new Algorithm( "AES", new int[]{ 16, 24, 32 }, 16 ) );
		// TODO: Add more supported ciphers here
//		algorithms.add( new PWManAlgorithm( "Blowfish", new int[]{ 16 }, 8, false ) );
	}
	
	/**
	 * Decrypt a ciphertext with default key
	 * @param encryptedText
	 * @return byte[] - plainText
	 */
	public byte[] decrypt( byte[] encryptedText ) {
		return decrypt( this.databaseKey, encryptedText, true );
	}
	
	/**
	 * Decrypt a ciphertext with default key and choose if TAG is removed afterwards
	 * @param encryptedText
	 * @return byte[] - plainText
	 */
	public byte[] decrypt( byte[] encryptedText, boolean removeTag ) {
		return decrypt( this.databaseKey, encryptedText, removeTag );
	}
	
	/**
	 * Decrypt a ciphertext with given key
	 * @param key - Used key
	 * @param encryptedText
	 * @return byte[] - plainText
	 */
	public byte[] decrypt( byte[] key, byte[] encryptedText, boolean removeTag ) {

		SecretKey secret_key = new SecretKeySpec( key, algorithm.getName() );

		byte[] decrypted = null;
		try {
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance( algorithm.getName() + "/ECB/NoPadding" );
			cipher.init( javax.crypto.Cipher.DECRYPT_MODE, secret_key );
			
			decrypted = cipher.doFinal( encryptedText );
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		
		if ( removeTag ) {
			decrypted = removeTag( decrypted );
		}
		
		return decrypted;
	}
	
	/**
	 * Prepares a plaintext to fit for blocksize
	 * @param plainText
	 * @param blocksize - in bytes
	 * @return byte[] - padded plaintext
	 */
	private byte[] prepareForEncryption( byte[] plainText, int blocksize ) {
		
		int newPlainTextBlocks = (int)plainText.length / blocksize;
		int newPlainTextAddBlock = plainText.length % blocksize > 0 ? 1 : 0;
		int newPlainTextSize = blocksize * ( newPlainTextBlocks + newPlainTextAddBlock );
		
		if ( newPlainTextSize == plainText.length ) {
			// Plaintext is already a multiple of blocksize 
			return plainText;
		}
		
		byte[] encrypted = new byte[ newPlainTextSize ];
		
		for ( int i = 0 ; i < plainText.length ; i++ ) {
			encrypted[i] = plainText[i];
		}
		for ( int i = plainText.length ; i < newPlainTextSize ; i++ ) {
			encrypted[i] = ' ';
		}
		
		return encrypted;
	}

	/**
	 * Encrypt a plaintext with default key
	 * @param plaintext
	 * @return byte[] - cipherText
	 */
	public byte[] encrypt( byte[] plainText ) {
		return encrypt( this.databaseKey, plainText, this.algorithm, true );
	}
	
	/**
	 * Encrypt a plaintext with default key and choose if TAG is added before encryption
	 * @param plaintext
	 * @return byte[] - cipherText
	 */
	public byte[] encrypt( byte[] plainText, boolean addTag ) {
		return encrypt( this.databaseKey, plainText, this.algorithm, addTag );
	}
	
	/**
	 * Encrypt a plaintext with given key
	 * @param plaintext
	 * @return byte[] - cipherText
	 */
	public byte[] encrypt( byte[] key, byte[] plainText, Algorithm algorithm, boolean addTag ) {
		
		SecretKey secret_key = new SecretKeySpec( key, algorithm.getName() );
		
		if ( addTag ) {
			plainText = addTag( plainText );
		}
		plainText = prepareForEncryption( plainText, algorithm.getBlockSize() );
		
		byte[] encrypted = null;
		try {
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance( algorithm.getName() + "/ECB/NoPadding" );
			cipher.init( javax.crypto.Cipher.ENCRYPT_MODE, secret_key );
			
			encrypted = cipher.doFinal( plainText );
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}

		return encrypted;
	}
	
	/**
	 * Pad the userpassword with spaces left aligned. <br/>
	 * The length of the padded key is depending on the blocksize of the used cipher.
	 * The smallest possible blocksize is used.
	 * @param key
	 * @param lengths
	 * @return byte[] - Padded key
	 */
	private byte[] padKey( byte[] key, int[] lengths ) {
		
		// max blocksize
		int maxLength = lengths[ lengths.length - 1 ];
		int keyLength = key.length;
		
		if ( keyLength > maxLength ) {
			// Keylength larger than bigest blocksize. Return cropped key
			return copyOf( key, maxLength );
		}
		
		// Determine next acceptable keylength
		int newKeyLength = 0;
		for ( int pos = lengths.length - 1 ; pos >= 0 ; pos-- ) {
			int length = lengths[ pos ];
			if ( length < keyLength ) {
				// Key too long
				break;
			}
			// Set next acceptable keylength
			newKeyLength = length;
		}
		
		byte[] padKey = copyOf( key, newKeyLength );
		
		// Set remaining bytes to ' ' (space) 
		for ( int pos = keyLength ; pos < newKeyLength ; pos++ ) {
			padKey[ pos ] = ' ';
		}
		return padKey;
	}
	
	private byte[] copyOf( byte[] data, int newLength ) {
		byte result[] = new byte[ newLength ];
		for ( int i = 0 ; i < data.length ; i++ ) {
			result[i] = data[i];
		}
		return result;
	}

	/**
	 * Returns the plain database key
	 * @return byte[] - plain database key
	 */
	public byte[] getKey() {
		return databaseKey;
	}
	
	/**
	 * Setup the access to the database. <br/>
	 * Loads the key, try to decrypt and store.
	 * @param pwmanFile - Database file
	 * @return boolean
	 * @throws FileNotFoundException
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyNotFoundException 
	 */
	public boolean load( String password, String algorithmName ) throws FileNotFoundException, NoSuchAlgorithmException, KeyNotFoundException {
		if ( tableKey == null ) {
			throw new RuntimeException( "Cipher: tableKey not set" );
		}
		byte[] keyCrypted = tableKey.getKeyCrypted();
		if ( keyCrypted == null ) {
			throw new KeyNotFoundException();
		}
		
		if ( password == null || password.length() == 0 ) {
			throw new RuntimeException( "Cipher: password not set" );
		}
		this.password = password.getBytes();
		
		if ( algorithmName == null || algorithmName.length() == 0 ) {
			throw new NoSuchAlgorithmException();
		}
		
		this.algorithm = getAlgorithmByName( algorithmName );
		if ( this.algorithm == null ) {
			throw new NoSuchAlgorithmException( "Algorithm: " + algorithmName + " is not supported or not found" );
		}
		
		byte[] keyForDatabaseKey = this.password;
		if ( algorithm.isKeyPadding() ) {
			keyForDatabaseKey = padKey( this.password, algorithm.getSupportedKeyLengths() );
		}
		
		// Here goes the decryption of database key
		byte[] keyDatabaseOriginal = decrypt( keyForDatabaseKey, keyCrypted, false );
		
		if ( !checkKey( keyDatabaseOriginal ) ) {
			databaseKey = null;
			return false;
		}
		
		byte[] keyBase64 = removeTag( keyDatabaseOriginal );
		databaseKey = Base64.decode( keyBase64, Base64.DEFAULT );
		
		return true;
	}
	
	private Algorithm getAlgorithmByName( String algorithmName ) {
		
		Algorithm algorithm = null;
		for ( Algorithm pwmanAlgorithm : algorithms ) {
			if ( pwmanAlgorithm.getName().compareTo( algorithmName ) == 0 ) {
				algorithm = pwmanAlgorithm;
			}
		}
		return algorithm;
	}

	/**
	 * Setup a new fresh database with new key
	 * @param password
	 * @param algorithmName
	 * @return boolean
	 * @throws NoSuchAlgorithmException 
	 */
	public boolean setup( String password, String algorithmName ) throws NoSuchAlgorithmException {
		
		Algorithm algorithm = getAlgorithmByName( algorithmName );
		if ( algorithm == null ) {
			throw new NoSuchAlgorithmException( "Algorithm: " + algorithmName + " is not supported or not found" );
		}
		
		for ( SQLiteTable table : Database.getInstance().getTables() ) {
			table.drop();
			table.create();
		}
		
		// Get secure random key
		SecureRandom secureRandom = new SecureRandom();
		byte databaseKey[] = new byte[ algorithm.getSupportedKeyLengthMax() ];
		secureRandom.nextBytes( databaseKey );
		
		// View the password as bytes and pad with spaces if needed
		byte passwordBytes[] = password.getBytes();
		if ( algorithm.isKeyPadding() ) {
			passwordBytes = padKey( passwordBytes, algorithm.getSupportedKeyLengths() );
		}
		
		// Encrypt the Base64 respresentation of the generated key and add the TAG in front
		String databaseKeyBase64 = Base64.encodeToString( databaseKey, Base64.NO_WRAP );
		byte[] encryptedKey = encrypt( passwordBytes, databaseKeyBase64.getBytes(), algorithm, true );
		
		// Finally encode the Base64 encoded encrypted database key with added TAG
		String encryptedKeyBase64 = Base64.encodeToString( encryptedKey, Base64.NO_WRAP );
		boolean insertSuccess = tableKey.insertKeyCrypted( encryptedKeyBase64 );
		if ( insertSuccess ) {
			this.password = passwordBytes;
			this.algorithm = algorithm;
			this.databaseKey = encryptedKey;
		}
		return insertSuccess;
	}
	
	/**
	 * Check if database key was correctly decrypted
	 * @param keyDatabaseOriginal - byte[]
	 * @return boolean
	 */
	private boolean checkKey( byte[] keyDatabaseOriginal ) {
		return new String( keyDatabaseOriginal ).startsWith( TAG );
	}
	
	/**
	 * Removes the prepended TAG
	 * @param data - byte[]
	 * @return byte[] - cleaned data
	 */
	private byte[] removeTag( byte[] data ) {
		int indexStart = TAG.length();
		return new String( data ).substring( indexStart ).getBytes();
	}
	
	/**
	 * Prepend the TAG to data
	 * @param data - byte[]
	 * @return byte[] - data with prepended TAG
	 */
	private byte[] addTag( byte[] data ) {
		int plainTextNewLength = TAG.length() + data.length;
		byte[] plainTextNew = new byte[ plainTextNewLength ];
		
		int i = 0;
		for ( i = 0 ; i < TAG.length() ; i++ ) {
			plainTextNew[i] = (byte)TAG.charAt(i);
		}
		for ( int j = 0 ; i < plainTextNewLength ; i++, j++ ) {
			plainTextNew[i] = data[j];
		}
		return plainTextNew;
	}
	
	public byte[] getPassword() {
		return password;
	}

	public void setPassword(byte[] password) {
		this.password = password;
	}
	
}
