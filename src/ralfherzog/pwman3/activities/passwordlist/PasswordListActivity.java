package ralfherzog.pwman3.activities.passwordlist;

import java.util.ArrayList;

import ralfherzog.pwman3.R;
import ralfherzog.pwman3.activities.PwmanActivity;
import ralfherzog.pwman3.activities.main.MainActivity;
import ralfherzog.pwman3.cipher.Cipher;
import ralfherzog.pwman3.core.PWManContentNode;
import ralfherzog.pwman3.database.Database;
import ralfherzog.pwman3.database.DatabaseConstants;
import ralfherzog.pwman3.database.sqlite.tables.SQLiteTableNodes;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class PasswordListActivity extends PwmanActivity {
	
	private ArrayList<PWManContentNode> nodes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.activity_password_list );
		
		ListView passwordListView = (ListView) findViewById( R.id.password_list_list );
		
		PasswordListAdapter passwordListAdapter = new PasswordListAdapter( this );
		passwordListView.setAdapter( passwordListAdapter );
		
		Cipher cipher = MainActivity.getCipher();
		assert cipher != null;
		
		SQLiteTableNodes tableNodes = (SQLiteTableNodes) Database.getInstance().getSQLiteTableByName( DatabaseConstants.Nodes.table );
		nodes = tableNodes.getNodes( cipher );
		
		passwordListAdapter.update( nodes );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClickImageCopyPaste( View view ) {
		
	}

}
