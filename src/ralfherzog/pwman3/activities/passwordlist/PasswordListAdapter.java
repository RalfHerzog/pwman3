package ralfherzog.pwman3.activities.passwordlist;

import java.util.ArrayList;
import ralfherzog.pwman3.R;
import ralfherzog.pwman3.core.PWManContentNode;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PasswordListAdapter extends BaseAdapter {
	
    private ArrayList<PWManContentNode> data;
    private LayoutInflater inflater;
    
	public PasswordListAdapter( Activity activity ) {
    	this.data 				= new ArrayList<PWManContentNode>();
        this.inflater 			= (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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
        
        PWManContentNode node = data.get( position );
        title.setText( node.getUserName() + "@" + node.getUrl() );
        
        // Set the listener on item
//        OnClickListener clickListener = FriendListAdapterListener.getFriendListListenerAction( this.friendListActivity, this.data, position );
//        imageButton.setOnClickListener( clickListener );
//        name.setOnClickListener( clickListener );
        
        return vi;
    }

}