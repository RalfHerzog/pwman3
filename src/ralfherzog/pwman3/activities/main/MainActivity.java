package ralfherzog.pwman3.activities.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import ralfherzog.pwman3.R;
import ralfherzog.pwman3.activities.PwmanActivity;
import ralfherzog.pwman3.activities.passwordlist.PasswordListActivity;
import ralfherzog.pwman3.cipher.Cipher;
import ralfherzog.pwman3.cipher.KeyNotFoundException;
import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.DatabaseConstants;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTableKey;
import ralfherzog.pwman3.filedialog.FileDialog;
import ralfherzog.pwman3.filedialog.FileDialogSelectionMode;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends PwmanActivity {
	
	private static Context context;
	private static boolean isRelease = false;
	
	private final String algorithm = "AES";
	private static Cipher cipher;
	
	private final int INTENT_PASSWORD_LIST = 0x00; 
	private final int INTENT_BROWSE_FILE = 0x01; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_main );
		context = getApplicationContext();
		
//		new File( Database.getInstance().getPwmanFolderDatabaseFile() ).delete();
		
		if ( !Database.getInstance().isDatabasePresent() ) {
			// Database not present
			// Show choice dialog
			showDialogSetup();
		} else {
			
			if ( isDatabaseUnlocked() ) {
				Intent passwordListIntent = new Intent( context, PasswordListActivity.class );
				startActivityForResult( passwordListIntent, INTENT_PASSWORD_LIST );
			} else {
				// Database file is present, init
				Database.getInstance().init();
				showDialogRequestPassword( false );
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}
	
	private void loadDatabase() {
		Database.getInstance().init();
		SQLiteTableKey tableKey = (SQLiteTableKey)Database.getInstance().getSQLiteTableByName( DatabaseConstants.Key.table );
		
		if ( cipher == null ) {
			cipher = new Cipher( tableKey );
		}
	}
	
	public void showDialogSetup() {
		MainActivityDialogSetup activityDialogSetup = new MainActivityDialogSetup( this );
		activityDialogSetup.show();
	}
	
	private void showDialogRequestPassword( boolean retry ) {
		MainActivityDialogRequestPassword activityDialogRequestPassword = new MainActivityDialogRequestPassword( this, retry );
		activityDialogRequestPassword.show();
	}
	
	private void showDialogSetupPassword() {
		MainActivityDialogSetupPasswordRequest activityDialogPasswordRequest = new MainActivityDialogSetupPasswordRequest( this );
		activityDialogPasswordRequest.show();
	}
	
	@Override
	protected void onActivityResult ( int requestCode, int resultCode, Intent data ) {
		if ( requestCode == INTENT_PASSWORD_LIST ) {
			if ( resultCode == RESULT_CANCELED ) {
				finish();
			} else {
				// TODO: 
			}
		}
		
		if ( requestCode == INTENT_BROWSE_FILE ) {
			if ( resultCode == RESULT_OK ) {
				String sourceFile = data.getStringExtra( FileDialog.RESULT_PATH );
				String destinationFile = Database.getInstance().getPwmanFolderDatabaseFile();
				
				boolean copyResult = true;
				try {
					copyFile( new FileInputStream( sourceFile ), new FileOutputStream( destinationFile ) );
				} catch (FileNotFoundException e) {
					// Should never happen
					copyResult = false;
					e.printStackTrace();
				} catch (IOException e) {
					// TODO: Show file copy error
					copyResult = false;
					e.printStackTrace();
				}
				
				if ( copyResult ) {
					showDialogRequestPassword( false );
				}
			} else {
				showDialogSetup();
			}
		} else {
			// TODO: User aborted file browser
		}
	}
	
	public static Cipher getCipher() {
		return cipher;
	}

	public static Context getContext() {
		return context;
	}

	public static boolean isRelease() {
		return isRelease;
	}

	public void dialogSetupPasswordOnButtonClick( View dialogView ) {
		EditText editViewPassword = (EditText) dialogView.findViewById( R.id.main_dialog_setup_password_password );
		String userPassword = editViewPassword.getText().toString();
		
		loadDatabase();
		
		boolean success = false;
		try {
			success = cipher.setup( userPassword, algorithm );
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		if ( success ) {
			// Load the database
			try {
				cipher.load( userPassword, algorithm );
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyNotFoundException e) {
				e.printStackTrace();
			}
			setDatabaseUnlocked( true );
			
			Intent passwordListIntent = new Intent( context, PasswordListActivity.class );
			startActivityForResult( passwordListIntent, INTENT_PASSWORD_LIST );
		} else {
			// TODO: Show error
		}
	}

	public void dialogSetupPasswordRequestOnButtonClickUnlock( View dialogView ) {
		
		EditText editViewPassword = (EditText) dialogView.findViewById( R.id.main_dialog_password_request_password );
		String userPassword = editViewPassword.getText().toString();
		
		loadDatabase();
		
		// Load the database with password
		boolean success = false;
		try {
			success = cipher.load( userPassword, algorithm );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyNotFoundException e) {
			e.printStackTrace();
		}
		
		if ( success ) {
			setDatabaseUnlocked( true );
			
			Intent passwordListIntent = new Intent( context, PasswordListActivity.class );
			startActivityForResult( passwordListIntent, INTENT_PASSWORD_LIST );
		} else {
			// TODO: Do not close request dialog or restart
			showDialogRequestPassword( true );
		}
	}

	public void dialogSetupOnButtonClickBrowse( View dialogView ) {
		Intent intent = new Intent( getBaseContext(), FileDialog.class );
        intent.putExtra( FileDialog.START_PATH, "/" );
        intent.putExtra( FileDialog.CAN_SELECT_DIR, false );
        intent.putExtra( FileDialog.SELECTION_MODE, FileDialogSelectionMode.MODE_OPEN );
        intent.putExtra( FileDialog.FORMAT_FILTER, new String[] { Database.DATABASE_FILE_EXTENSION } );
        startActivityForResult( intent, INTENT_BROWSE_FILE );
	}
	
	public void dialogSetupOnButtonClickCreate(View dialogView) {
		showDialogSetupPassword();
	}

}
