package com.parse.starter;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

/**
 * Create a table of contacts.  The user can select a contact to delete.
 * The user can also select to add a user and enter their name and phone number.
 * @author timothy
 *
 */
public class ContactsActivity extends Activity 
{
	
	final Context context = this;
	private static final String DEBUG_TAG = "ContactsActivity";

	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String contacts_table = "tbl_contacts";
	
	/** Table to hold the list of contacts.*/
	private TableLayout table;
	
	/** Setup the database and display the list of contacts.
	 * The queryDatabase method calls setupRow() on each contact and 
	 * adds a listener to it.  When the user click on a contact,
	 * selectedContact() is called, a popup appears and 
	 * allows the user to delete the contact or cancel.*/
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.contacts_activity_layout);setContentView(R.layout.contacts_activity_layout);setContentView(R.layout.contacts_activity_layout);setContentView(R.layout.contacts_activity_layout);
	    Log.i(DEBUG_TAG, "onCreate: 3d");
	    // Add Contacts
     	ImageButton add_contact_button = (ImageButton) findViewById(R.id.add_contact_button);
     	add_contact_button.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	addContact();
            }
        });
     	createAppContactsList();
	}
	/**
	 * Throw up a dialog to get first name, last name and phone number.
	 */
	private void addContact()
	{
		final String method = "addContact";
		LayoutInflater layout_inflater = LayoutInflater.from(context);
		final View popup_view = layout_inflater.inflate(R.layout.add_contact_popup, null);
		final AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(context);
		alert_dialog_builder.setView(popup_view);
		alert_dialog_builder.setCancelable(false).setPositiveButton(R.string.add,
    		new DialogInterface.OnClickListener() 
        	{
			    public void onClick(DialogInterface dialog,int id) 
			    {			    		
			    	final EditText add_first_name_edit_text = (EditText) popup_view.findViewById(R.id.add_first_name_edit_text);
			    	final EditText add_last_name_edit_text = (EditText) popup_view.findViewById(R.id.add_last_name_edit_text);
			    	final EditText add_phone_number_edit_text = (EditText) popup_view.findViewById(R.id.add_phone_number_edit_text);
			    	String name = add_first_name_edit_text.getText().toString().trim();
			    	name = name+" "+add_last_name_edit_text.getText().toString();
			    	String number = add_phone_number_edit_text.getText().toString();
				    Log.i(DEBUG_TAG, method+": "+name+" "+number);
				    addDeviceContact(name, number);
				    addAppContact(name, number);
				    setupRow(name, number);
			    }
			  }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			  {
				  public void onClick(DialogInterface dialog,int id)
				  {
					  dialog.cancel();
				  }
			  });
    	AlertDialog alert_dialog = alert_dialog_builder.create();
    	alert_dialog.show();
	}
	
	/**
	 * Insert new contact into the device database.
	 * @param name
	 * @param number
	 */	
	private void addDeviceContact(String name, String number)
	{
		ArrayList<ContentProviderOperation> contentProviderOperation = new ArrayList<ContentProviderOperation>();
		contentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				   .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
				   .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
				   .build());
		if (name != null) 
		{
			contentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				    .withValue(ContactsContract.Data.MIMETYPE,
				      ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				      name).build());
		}
		if (number != null) 
		{
			contentProviderOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			    .withValue(ContactsContract.Data.MIMETYPE,
			      ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
			    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
			      number)
			    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
			      ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
			    .build());
		}
		// Asking the Contact provider to create a new contact
		try 
		{
			context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentProviderOperation);
		} catch (Exception e) 
		{
		  e.printStackTrace(); //show exception in toast
		  Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Insert contact into the device contacts list.
	 * @param name
	 * @param number
	 */
	private void addAppContact(String name, String number)
	{
		final String method = "addAppContact";
		ContentValues contact = new ContentValues();
		contact.put("name", name);
		contact.put("number", number);
		long new_id = contacts_sqlite_db.insert("tbl_contacts",  null,  contact);
		Log.i(DEBUG_TAG, method+": new id "+new_id);
	}
	
	/**
	 * Select a contact to delete.  The name and number must both match the contact to delete.
	 * 
	 * @param name
	 * @param number
	 * @param v
	 */
	private void selectedContact(final String name, final String number, final View v)
	{
		final String method = "selectedContact";
		LayoutInflater layout_inflater = LayoutInflater.from(context);
		View popup_view = layout_inflater.inflate(R.layout.delete_contact_popup, null);
		final AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(context);
		alert_dialog_builder.setView(popup_view);
		final TextView delete_contact_popup_text = (TextView) popup_view.findViewById(R.id.delete_contact_popup_text);
		delete_contact_popup_text.setText(name);
		String message = R.string.delete+" "+name+"?";
		Log.i(DEBUG_TAG, method+" popup message "+message);
		alert_dialog_builder.setCancelable(false).setPositiveButton(R.string.delete,
    		new DialogInterface.OnClickListener() 
        	{
			    public void onClick(DialogInterface dialog,int id)
			    {			    		
			    	Log.i(DEBUG_TAG, method+": delete");
			    	table.removeView(v);
			    	contacts_sqlite_db.delete(contacts_table, "name='"+name+"' AND number='"+number+"'", null);
			    	dialog.cancel();
			    }
			  }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			  {
				  public void onClick(DialogInterface dialog,int id)
				  {
					  dialog.cancel();
				  }
			  });
    	AlertDialog alert_dialog = alert_dialog_builder.create();
    	alert_dialog.show();
	}
	
	/**
	 * Create a table layout programmatically and add an on click listener.
	 * The table has two rows, for name and phone number.
	 * When a user selects a contact, selectedContact() will prompt the user to delete
	 * the contact or cancel the operation.
	 */
	private void setupRow(String name, String number)
	{
		final String method = "setupRow";
		table = (TableLayout) findViewById(R.id.contacts_table_layout);
		table.setVerticalScrollBarEnabled(true);
		table.setColumnStretchable(2, true);
		TableRow row = new TableRow(this);
		TextView name_text_view = new TextView(this);
        TextView number_text_view = new TextView(this);   
        name_text_view.setText(name);
        number_text_view.setText(number);
        row.addView(name_text_view);
        row.addView(number_text_view);
        table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        row.setClickable(true);
        row.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	String inner_method = "onClick";
            	final String method = "setupRow.onClick";
            	String selected_row_id = v.toString();
            	TableRow t = (TableRow)v;
                TextView firstTextView = (TextView) t.getChildAt(0);
                TextView secondTextView = (TextView) t.getChildAt(1);
                String name = firstTextView.getText().toString();
                String number = secondTextView.getText().toString();
            	Log.i(DEBUG_TAG, method+"."+inner_method+" row_id "+selected_row_id+" "+name+" "+number);
            	selectedContact(name, number, v);
            }
        });
	}
	
	/**
	 * Set up the database and place it under management,addContact
	 * then call getContacts() to get the contacts table and show it.
	 */
	private void createAppContactsList()
	{
		setupDatabase();
		// table, columns, selection, selectionArgs, groupBy, having, orderBy
		Cursor c = contacts_sqlite_db.query(contacts_table, null, null, null, null, null, null);
		startManagingCursor(c);
		getAppContacts(c);
	}
	
	private void getAppContacts(Cursor c)
	{
		String method = "getAppContacts";
		Log.i(DEBUG_TAG, method+": count "+c.getCount()+" columns: "+c.getColumnCount());
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			String name = c.getString(1);
			String number = c.getString(2);
			setupRow(name, number);
			Log.i(DEBUG_TAG, method+": row "+c.getPosition()+": "+name+" "+number);
			c.moveToNext();
		}
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
