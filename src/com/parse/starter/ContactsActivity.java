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
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;
import static org.curchod.util.Tables.*;

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
	
	/** Table to hold the list of contacts.*/
	private TableLayout table;
	
	private SimpleCursorAdapter adapter;
	
	/** Setup the database and display the list of contacts.
	 * The queryDatabase method calls setupRow() on each contact and 
	 * adds a listener to it.  When the user click on a contact,
	 * selectedContact() is called, a popup appears and 
	 * allows the user to delete the contact or cancel.*/
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.contacts_activity_layout);
	    Log.i(DEBUG_TAG, "onCreate: 5b");
     	createAppContactsView();
	}
	
	private void createAppContactsView()
	{
		String method = "createContactsView";
		Cursor cursor = contacts_sqlite_db.query(contacts_table, null, null, null, null, null, null);
		Log.i(DEBUG_TAG, method+" count "+cursor.getCount());
		String[] from = new String[] {"name"};
		int[]  to = new int[] {R.id.itemTextView};
     	adapter = new SimpleCursorAdapter(this,R.layout.contacts_activity_layout, cursor, from, to);
	    ListView listView = (ListView)findViewById(android.R.id.list);
	    listView.setAdapter(adapter);
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
		adapter.getCursor().requery();
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
	
	@Override
    public void onDestroy()
	{
        super.onDestroy();
        contacts_sqlite_db.close();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0 , 1, 0, R.string.add_contact);
		return true;
	}
	
	 public boolean onOptionsItemSelected(MenuItem item) 
	 {
	    String method = "onOptionsItemSelected";
	    getIntent();
	    if (item.getItemId() == 1)
	    {
	    	Log.i(DEBUG_TAG, method+": Add Contacts");
	    	addContact();
	    }
	    return super.onOptionsItemSelected(item);
	 }
	 
	public void execute(View v)
	{
			String method = "execute";
			Log.i(DEBUG_TAG, method+": selected "+v.getId());
	}

}
