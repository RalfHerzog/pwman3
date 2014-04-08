package ralfherzog.pwman3.activities.main;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import ralfherzog.pwman3.R;
import ralfherzog.pwman3.activities.PwmanActivity;
import ralfherzog.pwman3.activities.passwordlist.PasswordListActivity;
import ralfherzog.pwman3.cipher.Cipher;
import ralfherzog.pwman3.cipher.KeyNotFoundException;
import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.DatabaseConstants;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTableKey;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends PwmanActivity {
	
	private static Context context;
	private static boolean isRelease = false;
	
	private final String algorithm = "AES";
	private static Cipher cipher;
	
	private View viewUnlock;
	private View viewCreate;
	private TextView textTitle;
	
	private SQLiteTableKey tableKey;
	
	private static boolean databaseUnlocked = false;
	
	private final int INTENT_FRIEND_LIST = 0x00; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_main );
		context = getApplicationContext();
		
		viewUnlock = (View)findViewById( R.id.main_view_unlock );
		viewCreate = (View)findViewById( R.id.main_view_create );
		textTitle = (TextView)findViewById( R.id.main_text_title );
		
		tableKey = (SQLiteTableKey)Database.getInstance().getSQLiteTableByName( DatabaseConstants.Key.table );
		
		cipher = new Cipher( tableKey );
		
		if ( tableKey.hasCryptedKey() ) {
			if ( databaseUnlocked ) {
				// TODO: switch to password list activity
				Intent passwordListIntent = new Intent( this, PasswordListActivity.class );
				startActivityForResult( passwordListIntent, INTENT_FRIEND_LIST );
			} else {
				textTitle.setText( getText( R.string.main_text_database_unlock ) );
				
				showView( viewCreate, false );
				showView( viewUnlock, true );
			}
		} else {
			textTitle.setText( getText( R.string.main_text_database_create ) );
			
			showView( viewUnlock, false );
			showView( viewCreate, true );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onButtonClickUnlock( View buttonView ) {
		
		EditText editPassword = (EditText) findViewById( R.id.main_unlock_password );
		String passwordString = editPassword.getText().toString();
		
		if ( !checkPasswordRestriction( passwordString ) ) {
			// TODO: Show error due to password restriction
			editPassword.setError( getString( R.string.main_text_password_restriction ) );
			return;
		}
		
		boolean passwordCorrect = false;
		try {
			passwordCorrect = cipher.load( passwordString, algorithm );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyNotFoundException e) {
			e.printStackTrace();
		}
		
		if ( !passwordCorrect ) {
			editPassword.setError( getString( R.string.main_text_password_wrong ) );
			return;
		}
		// TODO: switch to password list activity
		databaseUnlocked = true;
		
		Intent passwordListIntent = new Intent( this, PasswordListActivity.class );
		startActivityForResult( passwordListIntent, INTENT_FRIEND_LIST );
	}
	
	public void onButtonClickCreate( View buttonView ) {
		final LayoutInflater inflater = getLayoutInflater();
		
		final View dialogView = inflater.inflate( R.layout.activity_main_dialog_create, null );
		
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder
			.setView( dialogView )
			.setPositiveButton( R.string.cancel, null )
			.setNegativeButton( R.string.create, new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which ) {
					
					TextView textViewPassword1 = (TextView) dialogView.findViewById( R.id.main_dialog_create_password_1 );
					String userPassword = textViewPassword1.getText().toString();
					
					boolean success = false;
					try {
						success = cipher.setup( userPassword, algorithm );
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					
					if ( success ) {
						// TODO: Switch to password list activity
					} else {
						// TODO: Show error
					}
				}
			})
		;
		
		AlertDialog dialog = builder.create();
		dialog.show();
		
		dialog.getButton( AlertDialog.BUTTON_NEGATIVE ).setEnabled( false );
		
		TextView textViewPassword1 = (TextView)dialog.findViewById( R.id.main_dialog_create_password_1 );
		TextView textViewPassword2 = (TextView)dialog.findViewById( R.id.main_dialog_create_password_2 );
		
		textViewPassword1.addTextChangedListener( getDialogCreatePasswordTextWatcher( dialog ) );
		textViewPassword2.addTextChangedListener( getDialogCreatePasswordTextWatcher( dialog ) );
	}
	
	private TextWatcher getDialogCreatePasswordTextWatcher( final AlertDialog dialog ) {
		return new TextWatcher() {
			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count ) {
				
				TextView textViewPassword1 = (TextView)dialog.findViewById( R.id.main_dialog_create_password_1 );
				TextView textViewPassword2 = (TextView)dialog.findViewById( R.id.main_dialog_create_password_2 );
				
				String password1 = textViewPassword1.getText().toString();
				String password2 = textViewPassword2.getText().toString();
				
				// TODO: Add more password restrictions here
				if ( 
					checkPasswordRestriction( password1 ) && checkPasswordRestriction( password2 )
					&& password1.equals( password2 )
				) {
					dialog.getButton( AlertDialog.BUTTON_NEGATIVE ).setEnabled( true );
				} else {
					dialog.getButton( AlertDialog.BUTTON_NEGATIVE ).setEnabled( false );
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
	}
	
	@Override
	protected void onActivityResult ( int requestCode, int resultCode, Intent data ) {
		if ( requestCode == INTENT_FRIEND_LIST ) {
			finish();
		}
	}
	
	private boolean checkPasswordRestriction( String password ) {
		boolean success = true;
		success &= password.length() >= 4;
		return success;
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

}
