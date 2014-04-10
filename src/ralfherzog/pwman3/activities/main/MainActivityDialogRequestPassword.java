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

public class MainActivityDialogRequestPassword {
	
	private View dialogView;
	private MainActivity mainActivity;
	private AlertDialog dialog;
	
	public MainActivityDialogRequestPassword( MainActivity mainActivity, boolean retry ) {
		this.mainActivity = mainActivity;
		
		LayoutInflater inflater = mainActivity.getLayoutInflater();
		
		dialogView = inflater.inflate( R.layout.activity_main_dialog_request_password, null );
		
		if ( retry ) {
			TextView textViewTitle = (TextView) dialogView.findViewById( R.id.main_dialog_password_request_title );
			textViewTitle.setText( R.string.main_dialog_request_password_text_retry_failure );
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder( mainActivity );
		builder
			.setView( dialogView )
			.setCancelable( false )
			.setPositiveButton( R.string.cancel, onButtonClickCancel() )
			.setNegativeButton( R.string.main_dialog_request_button_unlock, onButtonClickUnlock() )
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
	
	private DialogInterface.OnClickListener onButtonClickUnlock() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which ) {
				mainActivity.dialogSetupPasswordRequestOnButtonClickUnlock( dialogView );
			}
		};
	}
	
	private TextWatcher getDialogPasswordTextWatcher() {
		return new TextWatcher() {
			@Override
			public void onTextChanged( CharSequence s, int start, int before, int count ) {
				
				EditText editViewPassword = (EditText)dialog.findViewById( R.id.main_dialog_password_request_password );
				
				String password = editViewPassword.getText().toString();
				
				if ( mainActivity.checkPasswordRestriction( password ) ) {
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
		
		EditText textViewPassword = (EditText)dialog.findViewById( R.id.main_dialog_password_request_password );
		
		textViewPassword.addTextChangedListener( getDialogPasswordTextWatcher() );
	}

}
