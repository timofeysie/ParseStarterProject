package com.parse.starter;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class GroupsActivity extends Activity 
{

	final Context context = this;
	private static final String DEBUG_TAG = "GroupsActivity";
	
	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String contacts_table = "tbl_contacts";
	
	/** Create a list of app contacts and their numbers. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.groups_activity_layout);
	    String method = "onCreate";
	    Log.i(DEBUG_TAG, method+": build 2b");
	    setupDatabase();
	    String[] columns = {"_id","name"};
		Cursor cursor = contacts_sqlite_db.query(contacts_table, null, null, null, null, null, null);
		Log.i(DEBUG_TAG, method+" count "+cursor.getCount());
		String[] from = new String[] {"name"};
		int[]  to = new int[] {R.id.itemTextView};
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.groups_activity_layout, cursor, from, to);
	    ListView listView = (ListView)findViewById(android.R.id.list);
	    listView.setAdapter(adapter);
	}
	
	/*
	public SimpleCursorAdapter getGuideAdapter() 
	{
	    SimpleCursorAdapter adapter = null;
	    SQLiteDatabase db = SQLiteDatabaseHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery("SELECT DISTINCT oid as _id, name, number FROM CHAN_TABLE ORDER BY number", null);
	    if (cursor.moveToFirst()) {
	        String[] columnNames = { "name" };
	        int[] resIds = { R.id.channel_name };
	        adapter = new SimpleCursorAdapter(this, R.layout.channel_selector_item, cursor, columnNames, resIds);
	    }
	    return adapter; 
	}
	*/
	
	private void anotherWay()
	{
		Cursor mCursor = getContacts();
	    startManagingCursor(mCursor);
	    // now create a new list adapter bound to the cursor.
	    // SimpleListAdapter is designed for binding to a Cursor.
	    ListAdapter adapter = new SimpleCursorAdapter(this, // Context.
	        android.R.layout.two_line_list_item, // Specify the row template
	                            // to use (here, two
	                            // columns bound to the
	                            // two retrieved cursor
	                            // rows).
	        mCursor, // Pass in the cursor to bind to.
	        // Array of cursor columns to bind to.
	        new String[] { ContactsContract.Contacts._ID,
	            ContactsContract.Contacts.DISPLAY_NAME },
	        // Parallel array of which template objects to bind to those
	        // columns.
	        new int[] { android.R.id.text1, android.R.id.text2 });

	    // Bind to our new adapter.
	    //setListAdapter(adapter);
	  }

	  private Cursor getContacts() {
	    // Run query
	    Uri uri = ContactsContract.Contacts.CONTENT_URI;
	    String[] projection = new String[] {ContactsContract.Contacts._ID,
	        ContactsContract.Contacts.DISPLAY_NAME };
	    String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
	        + ("1") + "'";
	    String[] selectionArgs = null;
	    String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
	        + " COLLATE LOCALIZED ASC";

	    return managedQuery(uri, projection, selection, selectionArgs,
	        sortOrder);
	}
	
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

}
