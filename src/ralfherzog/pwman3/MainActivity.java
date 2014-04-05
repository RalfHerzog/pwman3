package ralfherzog.pwman3;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import ralfherzog.pwman3.cipher.Cipher;
import ralfherzog.pwman3.cipher.KeyNotFoundException;
import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.DatabaseConstants;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTableKey;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;

public class MainActivity extends Activity {
	
	private static Context context;
	private static boolean isRelease = false;
	
	private final String password = "PASSWORD";
	private final String algorithm = "AES";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();
		Database.getInstance();
		
		SQLiteTableKey tableKey = (SQLiteTableKey)Database.getInstance().getSQLiteTableByName( DatabaseConstants.Key.table );
//		tableKey.deleteFrom();
		
		Cipher cipher = new Cipher( tableKey );
		try {
			cipher.load( password, algorithm );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyNotFoundException e) {
			// TODO: Ask user to setup new database key and delete all database data
			try {
				cipher.setup( password, algorithm );
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static Context getContext() {
		return context;
	}

	public static boolean isRelease() {
		return isRelease ;
	}

}
