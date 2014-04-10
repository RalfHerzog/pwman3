package ralfherzog.pwman3.activities.main;

import ralfherzog.pwman3.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class MainActivityDialogSetup {
	
	private View dialogView;
	private MainActivity mainActivity;
	private AlertDialog dialog;
	
	public MainActivityDialogSetup( MainActivity mainActivity ) {
		this.mainActivity = mainActivity;
		
		LayoutInflater inflater = mainActivity.getLayoutInflater();
		
		dialogView = inflater.inflate( R.layout.activity_main_dialog_setup_password, null );
		
		AlertDialog.Builder builder = new AlertDialog.Builder( mainActivity );
		builder
			.setMessage( R.string.main_dialog_startup_text )
			.setCancelable( false )
			.setNegativeButton( R.string.main_dialog_startup_button_create, onButtonClickCreate() )
			.setPositiveButton( R.string.main_dialog_startup_button_browse, onButtonClickBrowse() )
		;
		
		dialog = builder.create();
	}
	
	private DialogInterface.OnClickListener onButtonClickCreate() {
		return new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				mainActivity.dialogSetupOnButtonClickCreate( dialogView );
			}
		};
	}
	
	private DialogInterface.OnClickListener onButtonClickBrowse() {
		return new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				mainActivity.dialogSetupOnButtonClickBrowse( dialogView );
			}
		};
	}
	
	public void show() {
		dialog.show();
	}

}
