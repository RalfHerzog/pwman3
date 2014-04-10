package ralfherzog.pwman3.activities.main;

import ralfherzog.pwman3.R;
import ralfherzog.pwman3.database.Database;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivityDialogSetupPasswordRequest {
	
	private View dialogView;
	private MainActivity mainActivity;
	private AlertDialog dialog;
	
	public MainActivityDialogSetupPasswordRequest( MainActivity mainActivity ) {
		this.mainActivity = mainActivity;
		
		LayoutInflater inflater = mainActivity.getLayoutInflater();
		
		dialogView = inflater.inflate( R.layout.activity_main_dialog_setup_password, null );
		
		AlertDialog.Builder builder = new AlertDialog.Builder( mainActivity );
		builder
			.setCancelable( false )
			.setView( dialogView )
			.setPositiveButton( R.string.cancel, onButtonClickCancel() )
			.setNegativeButton( R.string.create, onButtonClickCreate() )
		;
		
		dialog = builder.create();
	}
	
	private DialogInterface.OnClickListener onButtonClickCancel() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which ) {
				
				// Hide keyboard
				mainActivity.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
				
				if ( Database.getInstance().isDatabasePresent() ) {
					mainActivity.finish();
				} else {
					mainActivity.showDialogSetup();
				}
			}
		};
	}
	
	private DialogInterface.OnClickListener onButtonClickCreate() {
		return new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				mainActivity.dialogSetupPasswordOnButtonClick( dialogView );
			}
		};
	}
	
	private TextWatcher getDialogCreatePasswordTextWatcher() {
		return new TextWatcher() {
			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count ) {
				
				EditText editViewPassword = (EditText)dialog.findViewById( R.id.main_dialog_setup_password_password );
				EditText editViewPasswordRepeat = (EditText)dialog.findViewById( R.id.main_dialog_setup_password_password_repeat );
				
				String password1 = editViewPassword.getText().toString();
				String password2 = editViewPasswordRepeat.getText().toString();
				
				if ( 
					mainActivity.checkPasswordRestriction( password1 ) && mainActivity.checkPasswordRestriction( password2 )
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
	
	public void show() {
		dialog.show();
		
		dialog.getButton( AlertDialog.BUTTON_NEGATIVE ).setEnabled( false );
		
		TextView textViewPassword1 = (TextView)dialog.findViewById( R.id.main_dialog_setup_password_password );
		TextView textViewPassword2 = (TextView)dialog.findViewById( R.id.main_dialog_setup_password_password_repeat );
		
		textViewPassword1.addTextChangedListener( getDialogCreatePasswordTextWatcher() );
		textViewPassword2.addTextChangedListener( getDialogCreatePasswordTextWatcher() );
	}

}
