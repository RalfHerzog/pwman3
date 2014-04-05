package ralfherzog.pwman3.cipher;

/**
 * Container for an algorithm
 * @author RalfHerzog
 */
public class Algorithm {
	private String name;
	private int supportedKeyLengths[];
	private int blockSize;
	private boolean keyPadding = true;
	
	/**
	 * Initialises an algo
	 * @param name - Name of the cipher
	 * @param supportedKeyLengths - in bytes
	 * @param blockSize - in bytes
	 */
	public Algorithm( String name, int[] supportedKeyLengths, int blockSize, boolean keyPadding ) {
		this.name = name;
		this.supportedKeyLengths = supportedKeyLengths;
		this.blockSize = blockSize;
		this.keyPadding = keyPadding;
	}
	public Algorithm( String name, int[] supportedKeyLengths, int blockSize ) {
		this( name, supportedKeyLengths, blockSize, true );
	}

	public String getName() {
		return name;
	}
	
	public int getSupportedKeyLengthMax() {
		return supportedKeyLengths[ supportedKeyLengths.length - 1 ];
	}

	public int[] getSupportedKeyLengths() {
		return supportedKeyLengths;
	}

	public int getBlockSize() {
		return blockSize;
	}
	
	public boolean isKeyPadding() {
		return keyPadding;
	}
}
