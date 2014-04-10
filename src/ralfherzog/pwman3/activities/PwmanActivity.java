package ralfherzog.pwman3.activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class PwmanActivity extends Activity {
	
	private static long lastFocusTimestamp = 0;
	private static final long DATABASE_UNLOCKED_TIMEOUT = 3;
	
	private static boolean databaseUnlocked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * Shows the progress UI and hides the friend list form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	protected void showView(final View view, final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ) {
			int shortAnimTime = getResources().getInteger( android.R.integer.config_shortAnimTime );
			
			view.setVisibility( View.GONE );
			view.animate().setDuration( shortAnimTime ).alpha( show ? 1 : 0 ).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd( Animator animation ) {
					view.setVisibility( show ? View.VISIBLE : View.GONE );
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			view.setVisibility( show ? View.VISIBLE : View.GONE );
		}
	}
	
	public boolean checkPasswordRestriction( String password ) {
		boolean success = true;
		success &= password.length() >= 4;
		return success;
	}
	
	protected void copyFile( InputStream in, OutputStream out ) throws IOException {
		byte[] buffer = new byte[ 1024 ];
		int read;
		while( ( read = in.read( buffer ) ) != -1 ){
			out.write(buffer, 0, read);
		}
	}
	
	@Override
	protected void onResume() {
		onWindowFocusChanged( true );
		super.onResume();
	}
	
	@Override
	public void onWindowFocusChanged( boolean hasFocus ) {
		if ( !hasFocus ) {
			// Activity looses focus
			lastFocusTimestamp = getTimestamp();
		} else {
			// Activity gains focus
			if ( getTimestamp() > lastFocusTimestamp + DATABASE_UNLOCKED_TIMEOUT ) {
			} else {
			}
		}
	}
	
	protected boolean isDatabaseUnlocked() {
		return databaseUnlocked;
	}
	
	protected void setDatabaseUnlocked( boolean unlocked ) {
		PwmanActivity.databaseUnlocked = unlocked;
	}
	
	protected long getTimestamp() {
		return (long)System.currentTimeMillis() / 1000;
	}
}
