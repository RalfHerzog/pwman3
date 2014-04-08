package ralfherzog.pwman3.activities.passwordlist;

import java.util.ArrayList;

import ralfherzog.pwman3.R;
import ralfherzog.pwman3.activities.main.MainActivity;
import ralfherzog.pwman3.core.PWManContentNode;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordListAdapter extends BaseAdapter {
	
    private ArrayList<PWManContentNode> data;
    private LayoutInflater inflater;
    private ClipboardManager clipboard;
    
	public PasswordListAdapter( Activity activity ) {
    	this.data 				= new ArrayList<PWManContentNode>();
        this.inflater 			= (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.clipboard = (ClipboardManager) activity.getSystemService( Context.CLIPBOARD_SERVICE );
	}
    
	public void update( ArrayList<PWManContentNode> data ) {
		this.data = data;
        this.notifyDataSetChanged();
    }
	
	public int getCount() {
        return data.size();
    }

    public PWManContentNode getItem( int position ) {
        return data.get( position );
    }

    public long getItemId( int position ) {
        return position;
    }
    
    public View getView( final int position, View convertView, ViewGroup parent ) {
        View vi = convertView;
        if( convertView == null )
        	vi = inflater.inflate( R.layout.password_list_item, null );
        
        TextView title = (TextView) vi.findViewById( R.id.password_list_list_item_text );
        ImageView copyPasteView = (ImageView) vi.findViewById( R.id.password_list_list_item_copy_paste );
        
        PWManContentNode node = data.get( position );
        title.setText( node.getUserName() + "@" + node.getUrl() );
        
        copyPasteView.setOnClickListener( newPasswordListCopyPasteListenerAction( data, position ) );
        
        // Set the listener on item
//        OnClickListener clickListener = FriendListAdapterListener.getFriendListListenerAction( this.friendListActivity, this.data, position );
//        imageButton.setOnClickListener( clickListener );
//        name.setOnClickListener( clickListener );
        
        return vi;
    }
    
    private OnClickListener newPasswordListCopyPasteListenerAction(final ArrayList<PWManContentNode> data, final int position) {
		return new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PWManContentNode node = data.get( position );
				
				ClipData clip = ClipData.newPlainText( "Password copied", node.getPassword() );
				clipboard.setPrimaryClip( clip );
				
				Toast.makeText( MainActivity.getContext(), "Password for " + node.getUserName() + "@" + node.getUrl() + " copied to clipboard", Toast.LENGTH_LONG ).show();
			}
		};
	}

}