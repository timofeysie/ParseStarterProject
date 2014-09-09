package com.parse.starter;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class GroupsActivity extends Activity 
{

	final Context context = this;
	private static final String DEBUG_TAG = "GroupsActivity";
	
	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String groups_table = "tbl_groups";
	private static final String contacts_groups_table = "tbl_contacts_groups";
	
	private static final int add_group_id = 2;
	private String new_group_name;
	private SimpleCursorAdapter adapter;
	
	/** Create a list of app contacts and their numbers. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.groups_activity_layout);
	    String method = "onCreate";
	    Log.i(DEBUG_TAG, method+": build 5e");
	    setupDatabase();
		Cursor cursor = contacts_sqlite_db.query(groups_table, null, null, null, null, null, null);
		Log.i(DEBUG_TAG, method+" count "+cursor.getCount());
		String[] from = new String[] {"name"};
		int[]  to = new int[] {R.id.itemTextView};
		adapter = new SimpleCursorAdapter(this,R.layout.groups_activity_layout, cursor, from, to);
	    ListView listView = (ListView)findViewById(android.R.id.list);
	    listView.setAdapter(adapter);
	}
	
	/**
	 * This is wired into the layout to be called by using android:onClick="execute".
	 * @param v
	 */
	public void execute(View v)
	{
		String method = "execute";
		switch(v.getId())
		{
        case R.id.sync_image_button:
        	Log.i(DEBUG_TAG, method+": add group");
        	showNameInputDialog();
        	break;
		}
	}
	
	/**
	 * Check that the new group name is valid,
	 * then insert the group name into the groups table.
	 * Next, start the GroupActivity to let the user choose contacts
	 * to associate with this new group.
	 */
	private void addGroup()
	{
		String method = "";
		if ((new_group_name != null) || (new_group_name.length()>0))
		{
			// add group.  go to group page to select contacts to add.
			Log.i(DEBUG_TAG, method+": add group");
			insertGroup();
		} else
		{
			// show toast: name required
			Log.i(DEBUG_TAG, method+": name required");
		}
	}
	
	/**
	 * Insert the group name into the groups table.
	 */
	private void insertGroup()
	{
		final String method = "insertGroup";
		ContentValues contact = new ContentValues();
		contact.put("name", new_group_name);
		long new_id = contacts_sqlite_db.insert(groups_table,  null,  contact);
		Log.i(DEBUG_TAG, method+": new id "+new_id);
		//adapter.notifyDataSetChanged();
		adapter.getCursor().requery();
	}
	
	/**
	 * This is the main entry point to create a new group.
	 * Throw up a dialog to accept the user input for a new group name.
	 * If this completes by using the OK button, then we call addGroup().
	 */
	private void showNameInputDialog()
	{
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View promptView = layoutInflater.inflate(R.layout.input_name_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(promptView);
		final EditText input = (EditText) promptView.findViewById(R.id.userInput);
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int id) 
			{
				new_group_name = input.getText().toString();
				addGroup();
			}
		}).setNegativeButton("Cancel",new DialogInterface.OnClickListener() 
		{
		    public void onClick(DialogInterface dialog, int id) 
		{
		    	dialog.cancel();
		    	}
		    });
		AlertDialog alertD = alertDialogBuilder.create();
		alertD.show();
	}
	
	/**
	 * Set up the db for use.
	 */
	private void setupDatabase()
	{
	    contacts_sqlite_db = context.openOrCreateDatabase(db_name
				,SQLiteDatabase.CREATE_IF_NECESSARY, null);
		contacts_sqlite_db.setLocale(Locale.getDefault());
		contacts_sqlite_db.setLockingEnabled(true);
		contacts_sqlite_db.setVersion(1);
	}
	
	@Override
    public void onDestroy()
	{
        super.onDestroy();
        contacts_sqlite_db.close();
    }

	/**
	 * Create the menu items.
	 * Add group lets a user input a new group name and then start the GroupActivity to fill the group.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0 , add_group_id, 0, R.string.add_group);
		return true;
	}
	
	/**
	 * For choice "add group" we call showNameInputDialog().
	 */
	 public boolean onOptionsItemSelected(MenuItem item) 
	 {
	    String method = "onOptionsItemSelected";
	    getIntent();
	    if (item.getItemId() == add_group_id)
	    {
	    	Log.i(DEBUG_TAG, method+": add group");
	    	showNameInputDialog();
	    }
	    return super.onOptionsItemSelected(item);
	 }
}
