package com.parse.starter;

import java.io.File;
import java.util.Date;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Main activity for the ParseStarterProject.
 * There are two type of contacts referred to in this app.
 * 1. Device contacts are those contained in the phones default contacts database.
 * 2. App contacts are those contained in this app's database.
 * If a user chooses the sync function, any contacts that have been deleted in the device contacts will
 * also be deleted in the app contacts.  On the contrary, contacts deleted in the app contacts will not
 * be deleted from the device contacts.
 * @author timothy
 *
 */
public class ParseStarterProjectActivity extends Activity 
{
	final Context context = this;
	private static final String DEBUG_TAG = "ParseStarterProjectActivity";
	
	/** Contact Object members */
	private static final String CONTACT_OBJECT = "ContactObject";
	private static final String CONTACT_NAME = "name";
	private static final String CONTACT_PHONE = "number";
	
	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String CREATE_CONTACTS_TABLE = "CREATE TABLE tbl_contacts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT);";
	private static final String CREATE_TEMPLATES_TABLE = "CREATE TABLE tbl_templates ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, template TEXT);";
	
	private static final String contacts_table = "tbl_contacts";
	private static final String templates_table = "tbl_templates";
	

	private int num_of_phone_contacts = 0;
	private int num_of_app_contacts = 0;
	
	/** for TestObject foobie value */
	private String foobie = "";
	private static final String TEST_ID = "PFeT2Lqbnx";
	private static final String TEST_OBJECT = "TestObject";
	private static final String TEST_FIELD = "foobie";
	
	/** Set up listeners for buttons, Parse.com analytics, then fetch the current status
	 * from Parse, then get the total counts of app and device contacts.*/
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i(DEBUG_TAG, "onCreate: 16a");
		// Track statistics around application opens
		ParseAnalytics.trackAppOpened(getIntent());
		setUpButtons();
		
		EditText edittext = (EditText) findViewById(R.id.status_edit_text);
		edittext.setOnKeyListener(new OnKeyListener() 
		{
			@Override
		    public boolean onKey(View v, int keyCode, KeyEvent event) 
		    {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) 
		        {
		        	updateStatusData();
		            return true;
		        }
		        return false;
		    }
		});
		
     	fetchParseData();
     	setupContactsCount();
	}
	
	/**
	 * Set up the bottons so that they will respond to clicks in the execute method.
	 */
	@SuppressWarnings("unused")
	private void setUpButtons()
	{
		// Submit new status button
		// Button status_submit_button = (Button)findViewById(R.id.status_submit_button);
		ImageView contacts_image_button = (ImageView)findViewById(R.id.contacts_image_button);
		ImageView templates_image_button = (ImageView)findViewById(R.id.templates_image_button);
		ImageView new_sms_image_button = (ImageView)findViewById(R.id.new_sms_image_button);
		ImageView groups_image_button = (ImageView)findViewById(R.id.groups_image_button);
		ImageView sync_image_button = (ImageView)findViewById(R.id.sync_image_button);
	}
	
	/**
	 * Handle all button clicks here.
	 * Replace:
	 * case R.id.status_submit_button:
        	updateStatusData();
            break;
     * With enter key listener.
	 * @param v
	 */
	public void execute(View v)
	{
		switch(v.getId())
		{
        case R.id.sync_image_button:
        	syncContacts();
        	break;
        case R.id.contacts_image_button:
        	startActivity(new Intent(ParseStarterProjectActivity.this, ContactsActivity.class));
        	break;
        case R.id.templates_image_button:
        	startActivity(new Intent(ParseStarterProjectActivity.this, TemplatesActivity.class));
        	break;
        case R.id.new_sms_image_button:
        	startActivity(new Intent(ParseStarterProjectActivity.this, SendSMSActivity.class));
        	break;
        case R.id.groups_image_button:
        	startActivity(new Intent(ParseStarterProjectActivity.this, GroupsActivity.class));
        	break;
		}
	}
	
	/**
	 * Set the number of device and app contacts in the contact labels.
	 */
	private void setupContactsCount()
	{
		num_of_phone_contacts = 0;
		num_of_app_contacts = 0;
		String method = "setupContactsCount()";
		Map<String,String> app_contacts = new TreeMap<String,String>();
    	Map<String,String> device_contacts = new TreeMap<String,String>();
		getAppContacts(app_contacts);
    	getDeviceContacts(device_contacts);
    	TextView phone_contacts_count = (TextView)findViewById(R.id.phone_contacts);
    	TextView app_contacts_count = (TextView)findViewById(R.id.app_contacts);
    	phone_contacts_count.setText(Integer.toString(num_of_phone_contacts));
    	app_contacts_count.setText(Integer.toString(num_of_app_contacts));
    	Log.i(DEBUG_TAG, method+": num_of_phone_contacts "+num_of_phone_contacts);
    	Log.i(DEBUG_TAG, method+": num_of_app_contacts "+num_of_app_contacts);
	}
	
	/**
	 * 1 get App contacts
     * 2 get device contacts
	 * Go thru all the phone contacts and if there are any that are in the app contacts which
	 * are not in the phone contacts, then we will delete them also from the app contacts.
	 * @param app_contacts (See device_contacts)
	 * @param device_contacts The TreeMap will contain id/number pairs to allow us to 
	 * eliminate contacts that have been deleted from the device but not to app contacts
	 * when the user does a sync function.
	 */
	private void syncContacts()
	{
		Map<String,String> app_contacts = new TreeMap<String,String>();
    	Map<String,String> device_contacts = new TreeMap<String,String>();
    	getAppContacts(app_contacts);
    	Log.i(DEBUG_TAG, "Sync ImageButton.onClick: app contacts: "+app_contacts.size());
    	getDeviceContacts(device_contacts);
    	Log.i(DEBUG_TAG, "Sync ImageButton.onClick: dev contacts: "+device_contacts.size());
		Map<String,String>app_contacts_to_remove = new TreeMap<String,String>();
		String removals = "";
		String method = "synchContacts";
		Iterator<Map.Entry<String,String>> iter = app_contacts.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry<String,String> entry = iter.next();
			String name = entry.getKey();
			String number = entry.getValue();
	        if (!device_contacts.containsKey(name))
	        {
	        	// delete app contact
	        	Log.i(DEBUG_TAG, method+": didn't find "+name+" "+number);
	        	removals = removals+" "+name;
	        	app_contacts_to_remove.put(name, number);
	        }
	    }
		if (app_contacts_to_remove.size()>0)
		{
			for(String name: app_contacts_to_remove.keySet())
			{
	            app_contacts.remove(name);
	            String number = app_contacts_to_remove.get(name);
	            contacts_sqlite_db.delete(contacts_table, "name='"+name+"' AND number='"+number+"'", null);
	            num_of_app_contacts = app_contacts.size();
	            Log.i(DEBUG_TAG, method+": num_of_app_contacts "+num_of_app_contacts);
	        }
			Toast.makeText(context, "Removed "+removals+" from app contacts.", Toast.LENGTH_SHORT).show();
			TextView app_contacts_count = (TextView)findViewById(R.id.app_contacts);
			app_contacts_count.setText(Integer.toString(num_of_app_contacts));
		}
		if ((device_contacts.size() > 0) && (app_contacts.size() == 0))
		{
			// we should put all device contacts into the app contacts.
			Log.i(DEBUG_TAG, method+": device contacts > 0 && app_contacts == 0, so add all device contacts.");
			addAddDeviceContacts(device_contacts);
			updateDeviceAndAppCounts();
		}
	}
	
	/**
	 * Update the UI for the new count of app and device contacts.
	 */
	private void updateDeviceAndAppCounts()
	{
		TextView phone_contacts_count = (TextView)findViewById(R.id.phone_contacts);
    	TextView app_contacts_count = (TextView)findViewById(R.id.app_contacts);
    	phone_contacts_count.setText(Integer.toString(num_of_phone_contacts));
    	app_contacts_count.setText(Integer.toString(num_of_app_contacts));
	}
	
	/**
	 * This method will insert all the contacts in the device contacts into the
	 * app contacts.
	 * @param device_contacts Contacts to be inserted into the app contacts db.
	 */
	private void addAddDeviceContacts(Map<String,String>device_contacts)
	{
		num_of_app_contacts = 0;
		String method = "addAddDeviceContacts";
		for(String name: device_contacts.keySet())
		{
			String number = device_contacts.get(name);
	        insertAppContact(name, number);
	        num_of_app_contacts++;
	        Log.i(DEBUG_TAG, method+": inserted "+name);
	    }
		num_of_phone_contacts = num_of_app_contacts;
	}
	
	/**
	 * Query the Contacts to get a list of names and phone numbers.
	 * If a contact has been deleted from the phone, it should also be deleted from the app.
	 * However, if a contact has been deleted from the app, it should not be deleted from the phone.
	 * So we need to keep a list of contacts.
	 * * @param device_contacts The TreeMap will contain id/number pairs to allow us to 
	 * eliminate contacts that have been deleted from the device but not to app contacts
	 * when the user does a sync function.
	 */
	private void getDeviceContacts(Map<String,String> device_contacts)
	{
		String method = "getDeviceContacts";
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Log.i(DEBUG_TAG, method+" count "+cur.getCount());
        if (cur.getCount() > 0) 
        {
        	while (cur.moveToNext()) 
        	{
        		String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
        		String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		//Log.i(DEBUG_TAG, "readContacts: id "+id+" name "+name);
        		if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
        		{
        			//Query phone #s, only add contacts with a phone number
        			getPhoneNumbers(id, device_contacts);
        			num_of_phone_contacts++;
        			//Log.i(DEBUG_TAG, method+": num_of_phone_contacts "+num_of_phone_contacts);
        		}
            }
        }
	}
	
	/**
	 * Pass in the contact id to query the database for that contact's phone number.
	 * Then we can assemble the name and phone number and add that to the device contacts.
	 * @param id contact id.
	 * @param device_contacts The TreeMap will contain id/number pairs to allow us to 
	 * eliminate contacts that have been deleted from the device but not to app contacts
	 * when the user does a sync function.
	 */
	private void getPhoneNumbers(String id, Map<String,String> device_contacts)
	{
		String method = "getPhoneNumbers";
		// Return all the PHONE data for the contact.
	    String where = ContactsContract.Data.CONTACT_ID+" = "+id+" AND "+
	        ContactsContract.Data.MIMETYPE + " = '"+
	        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE+"'";
	    String[] projection = new String[] 
	    {
	        ContactsContract.Data.DISPLAY_NAME,
	        ContactsContract.CommonDataKinds.Phone.NUMBER
	    };
	    Cursor dataCursor = 
	        getContentResolver().query(ContactsContract.Data.CONTENT_URI,
	        projection, where, null, null);
	    // Get the indexes of the required columns.
	    int nameIdx = dataCursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME);
	    int phoneIdx = dataCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
	    while(dataCursor.moveToNext()) 
	    {
	        String name = dataCursor.getString(nameIdx);
	        String number = dataCursor.getString(phoneIdx);
	        //Log.i(DEBUG_TAG, method+": "+name+" ("+number+")");
	        device_contacts.put(name,number);
	    } 
	    dataCursor.close();
	}

	/**
	 * Update the value of the status (test field with the foobie variable).
	 */
	private void updateGui()
	{
		TextView status_text_view_label = (TextView)findViewById(R.id.status_text_view);
		if (foobie.equals("barbie"))
		{
			foobie = "Available";
		}
		status_text_view_label.setText("Current status: "+foobie);
	    EditText editText1 = (EditText)findViewById(R.id.status_edit_text);
	    editText1.setText("");
	}
	
	/**
	 * Send a new value of foobie to Parse.  TO increment an int value, use:
	 * test_object.increment("score");
	 * or
	 * increment(key, amount)
	 */
	public void updateStatusData()
	{
		final String method = "updateStatusData";
		EditText status_edit_text = (EditText)findViewById(R.id.status_edit_text);
    	foobie = status_edit_text.getText().toString().trim();
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TEST_OBJECT);
		query.getInBackground(TEST_ID, new GetCallback<ParseObject>() {
		  public void done(ParseObject test_object, ParseException e) {
		    if (e == null) {
		        test_object.put(TEST_FIELD, foobie);
		    	test_object.saveInBackground();
		    	updateGui();
		    	Log.i(DEBUG_TAG, method+": updated");
		    } else {
		    	Log.i(DEBUG_TAG, method+": something went wrong");
		    }
		  }
		});
	}
	
	/**
	 * Create a new contact for the app.
	 * @param name The contact's name.
	 * @param number The contact's phone number.
	 */
	private void insertAppContact(String name, String number)
	{
		final String method = "insertContact";
		ContentValues contact = new ContentValues();
		contact.put("name", name);
		contact.put("number", number);
		long new_id = contacts_sqlite_db.insert("tbl_contacts",  null,  contact);
		Log.i(DEBUG_TAG, method+": new id "+new_id);
	}
	
	/**
	 * Open the device sqlite database and load the contacts table.
	 * If this is the first time, then create the database and tables.
	 * Then add all the device contacts to the app contacts.
	 */
	private void getAppContacts(Map <String,String> app_contacts)
	{
		String method = "getAppContacts";
		contacts_sqlite_db = context.openOrCreateDatabase(db_name
				,SQLiteDatabase.CREATE_IF_NECESSARY, null);
		contacts_sqlite_db.setLocale(Locale.getDefault());
		contacts_sqlite_db.setLockingEnabled(true);
		contacts_sqlite_db.setVersion(1);
		/*** Un-comment to reset the db
		contacts_sqlite_db.execSQL("DROP TABLE "+contacts_table);
		contacts_sqlite_db.execSQL("DROP TABLE "+templates_table);
		contacts_sqlite_db.execSQL(CREATE_CONTACTS_TABLE);
		contacts_sqlite_db.execSQL(CREATE_TEMPLATES_TABLE);
		//***/
		boolean first_time = false;
		try
		{
			tryQuery(app_contacts, first_time);
		} catch (SQLiteException e){
		    if (e.getMessage().toString().contains("no such table"))
		    {
	            Log.e(DEBUG_TAG, "Creating tables because they doesn't exist yet." );
	            // create table
	            contacts_sqlite_db.execSQL(CREATE_CONTACTS_TABLE);
				contacts_sqlite_db.execSQL(CREATE_TEMPLATES_TABLE);
	            // re-run query
				first_time = true;
				tryQuery(app_contacts, first_time);
		    }
	    }
		Log.i(DEBUG_TAG, method+": num_of_app_contacts "+num_of_app_contacts);
	}
	
	/**
	 * If this is the first time the app has been run this will cause an error.
	 * Otherwise, we can get the curor to manage the contacts table.
	 * @param app_contacts 
	 * @param first_time if first time is true, then the users from the device contacts
	 * will be add to the phone contacts.
	 */
	private void tryQuery(Map <String,String> app_contacts, boolean first_time)
	{
		String method = "tryQuery";
		Cursor c = contacts_sqlite_db.query(contacts_table, null, null, null, null, null, null);
		startManagingCursor(c);
		Log.i(DEBUG_TAG, method+": count "+c.getCount()+" columns: "+c.getColumnCount());
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			String name = c.getString(1);
			String number = c.getString(2);
			app_contacts.put(name, number);
			if (first_time)
			{
				insertAppContact(name, number);
				Log.i(DEBUG_TAG, method+": first_time: insert row "+c.getPosition()+": "+name+" "+number+" "+num_of_app_contacts);
			}
			num_of_app_contacts++;
			//Log.i(DEBUG_TAG, method+": row "+c.getPosition()+": "+name+" "+number+" "+num_of_app_contacts);
			c.moveToNext();
		}
	}
	
	/**
	 * Close the database before the app is closed.
	 */
	@Override
    public void onDestroy()
	{
        super.onDestroy();
        contacts_sqlite_db.close();
    }
	
	/**
	 * Get information about the test field.
	 * Used as the status of the current user.
	 */
	private void fetchParseData()
	{
		final String method = "fetchData";
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TEST_OBJECT);
		query.getInBackground(TEST_ID, new GetCallback<ParseObject>() {
		  public void done(ParseObject object, ParseException e) {
		    if (e == null) {
		    	ParseObject this_test_object = (ParseObject)object;
		    	Date createdAt = this_test_object.getCreatedAt();
		    	foobie = this_test_object.getString(TEST_FIELD);
		    	Log.i(DEBUG_TAG, method+": query - "+foobie+" date "+createdAt.toString());
		    } else {
		    	Log.i(DEBUG_TAG, method+": something went wrong");
		    }
		    updateGui();
		  }
		});
	}
	
	/**
	 * If the user has added new contacts in the Contacts screen, then we need to refresh the
	 * contacts count here.  There are much better ways to do this, but it will keep the count
	 * up to date for now.
	 */
	@Override
	protected void onResume() 
	{
	    super.onResume();
	    setupContactsCount();
	}
	
	// -------- UNUSED CODE -----------------------------------------------------
	
	/**
	 * Dump all the columns and rows of a table.
	 * @param c Cursor of a particular query to print.
	 */
	private void logCursorInfo(Cursor c)
	{
		String method = "logCursorInfo";
		Log.i(DEBUG_TAG, method+": count "+c.getCount()+" columns: "+c.getColumnCount());
		String row_headers = "||";
		for (int i = 0; i < c.getColumnCount(); i++)
		{
			row_headers = row_headers.concat(c.getColumnName(i)+" ||");
		}
		Log.i(DEBUG_TAG, method+": columns "+row_headers);
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			String row_results = "||";
			for (int i = 0; i < c.getColumnCount(); i++)
			{
				row_results = row_results.concat(c.getString(i)+"||");
			}
			Log.i(DEBUG_TAG, method+": row "+c.getPosition()+": "+row_results);
			c.moveToNext();
		}
		Log.i(DEBUG_TAG, method+": end");
	}
	
	/**
	 * This will create a whole new text object with a new id
	 * but only stored in the device until sent to Parse.com.
	 */
	private void pinContactData(String name, String number)
	{
		final String method = "pinContactData";
		try
		{
			ParseObject contact_object = new ParseObject(CONTACT_OBJECT);
			contact_object.put(CONTACT_NAME, name);
			contact_object.put(CONTACT_PHONE, number);
			contact_object.pinInBackground(new SaveCallback( ) {
	        @Override
	        public void done(ParseException e) {
	            if(e == null) 
	            {
	            	Log.i(DEBUG_TAG, method+": Success.");
			    } else {
			    	Log.i(DEBUG_TAG, method+": Failure.");
	            }
	        }
			});
		} catch (java.lang.IllegalArgumentException iae)
		{
			Log.i(DEBUG_TAG, method+": iae for "+name+" "+number);
		}
	}
	
	/**
	 * If data has changed on Parse.
	 * @param new_message
	 */
	private void refreshData()
	{
		final String method = "refreshData";
		ParseObject test_object = new ParseObject(TEST_OBJECT);
		test_object.fetchInBackground(new GetCallback<ParseObject>() 
		{
			  public void done(ParseObject object, ParseException e)
			  {
			    if (e == null) {
			    	updateGui();
			    	Log.i(DEBUG_TAG, method+": Success.");
			    } else {
			    	Log.i(DEBUG_TAG, method+": Failure.");
			    }
			  }
		});
	}
	
	private void pinData(String message)
	{
		ParseObject testObject = new ParseObject(TEST_OBJECT);
		testObject.put(TEST_FIELD, message);
		testObject.pinInBackground(null);
	}
	
	private void loadLocalData()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TEST_OBJECT);
		query.fromLocalDatastore();
		query.getInBackground(TEST_ID, new GetCallback<ParseObject>() {
		  public void done(ParseObject object, ParseException e) {
		    if (e == null) {
		      // object will be your game score
		    } else {
		      // something went wrong
		    }
		  }
		});
	}
	
	/**
	 * This will create a whole new text object with a new id.
	 */
	private void setData(String message)
	{
		ParseObject testObject = new ParseObject(TEST_OBJECT);
		testObject.put(TEST_FIELD, message);
		testObject.saveInBackground();
	}
	
	/**
	 * This will create array object.
	 */
	private void setDataArray(String field, String [] messages)
	{
		ParseObject test_object = new ParseObject(TEST_OBJECT);
		test_object.addAllUnique(field, Arrays.asList(messages));
		test_object.saveInBackground();
	}
	
	private void deleteObject()
	{
		
	}
	
	public void setFoobie(String _foobie)
	{
		this.foobie = _foobie;
	}
	
}
