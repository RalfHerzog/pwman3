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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static Context context;
	private static boolean isRelease = false;
	
	private final String password = "PASSWORD";
	private final String algorithm = "AES";
	private Cipher cipher;
	
	private Button buttonDatabaseCreate;
	private Button buttonDatabaseDelete;
	private TextView textTitle;
	private SQLiteTableKey tableKey;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		context = getApplicationContext();
		
		buttonDatabaseCreate = (Button)findViewById( R.id.main_button_database_create );
		buttonDatabaseDelete = (Button)findViewById( R.id.main_button_database_delete );
		textTitle = (TextView)findViewById( R.id.main_text_title );
		
		tableKey = (SQLiteTableKey)Database.getInstance().getSQLiteTableByName( DatabaseConstants.Key.table );
		
		cipher = new Cipher( tableKey );
		
		boolean databaseReady = false;
		try {
			cipher.load( password, algorithm );
			databaseReady = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyNotFoundException e) {
			e.printStackTrace();
		}
		
		initLayout( databaseReady );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void initLayout( boolean databaseReady ) {
		if ( databaseReady ) {
			textTitle.setText( getText( R.string.main_text_database_ready ) );
			
			buttonDatabaseCreate.setVisibility( View.GONE );
			buttonDatabaseDelete.setVisibility( View.VISIBLE );
		} else {
			textTitle.setText( getText( R.string.main_text_database_not_setup ) );

			buttonDatabaseDelete.setVisibility( View.GONE );
			buttonDatabaseCreate.setVisibility( View.VISIBLE );
		}
	}
	
	public void onButtonClickDelete( View buttonView ) {
		tableKey.deleteFrom();
		
		initLayout( false );
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
					
					try {
						cipher.setup( userPassword, algorithm );
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					initLayout( true );
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
				if ( password1.equals( password2 ) ) {
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

	public static Context getContext() {
		return context;
	}

	public static boolean isRelease() {
		return isRelease;
	}

}
