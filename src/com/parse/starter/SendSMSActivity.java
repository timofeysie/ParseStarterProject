package com.parse.starter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Prompt the user for the template to use.
 * Fill in the tag content.
 * Send the message to the entire app contact list.
 * @author timothy
 *
 */
public class SendSMSActivity extends Activity 
{

	private static final String DEBUG_TAG = "SendSMSActivity";
	final Context context = this;
	
	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String templates_table = "tbl_templates";
	private static final String contacts_table = "tbl_contacts";
	
	private Spinner templates_spinner;
	
	/** The data model for the templates spinner. */
	private List<String> templates_list;
	private Map<String,String> template_name_text;
	private Map<String,String> tags;
	private String tag_fill;
	private Map<String,String> device_contacts;
	private Map <String,String> app_contacts;
	
	/** Setup the UI for sending an SMS. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.send_sms_activity_layout);
	    final String method = "onCreate";
	    Log.i(DEBUG_TAG, method+": build 7a");
	    
	    // Spinner
	    setupDatabase();
     	createTemplatesList();
	    addItemsToTemplateSpinner();
	    
	    // Send SMS Image Button
	    ImageButton send_finished_sms_image_button = (ImageButton) findViewById(R.id.send_finished_sms_image_button);
	    send_finished_sms_image_button.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	// send the message
            	EditText send_sms_message_text = (EditText)findViewById(R.id.send_sms_message_text);
        		String message_to_send = send_sms_message_text.getText().toString();
        		//device_contacts = new TreeMap<String,String>();
        		//getDeviceContacts();
        		getAppContacts();
        		sendMessages(message_to_send);
        		Log.i(DEBUG_TAG, method+".onClick: sent messages");
        		Toast.makeText(context, "Sent to "+app_contacts.size()+" contacts.", Toast.LENGTH_SHORT).show();
        		finish();
            }
        });
	}
	
	private void sendMessages(String message_to_send)
	{
		String method = "sendMessages";
		for (Map.Entry<String, String> entry : app_contacts.entrySet())
		{
		    String name = entry.getKey();
		    String number = app_contacts.get(name);
		    if (number.length()>0 && message_to_send.length()>0)  
		    {
		    	//SmsManager sms_manager = SmsManager.getDefault();
		    	//sms_Manager.sendTextMessage(number, null, message_to_send, null, null);
		    	//ArrayList<String> parts = sms_manager.divideMessage(message_to_send);
		    	//sendSMS(number.getText().toString(),parts );
		    	//sms_manager.sendMultipartTextMessage(number, null,parts, null, null);
		    	sendSMS(number,message_to_send);
		    	Log.i(DEBUG_TAG, method+": send message to "+name+"  "+number);
		    } else
		    {
		    	Log.i(DEBUG_TAG, method+": unable to send message to "+name+"  "+number);
		    }
		}
	}
	
	private void sendSMS(String number, String message)
    {      
		final String method = "sendSMS";
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context arg0, Intent arg1) 
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                    	Log.i(DEBUG_TAG, method+"SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    	Log.i(DEBUG_TAG, method+"Generic failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    	Log.i(DEBUG_TAG, method+"No service");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                    	Log.i(DEBUG_TAG, method+"Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                    	Log.i(DEBUG_TAG, method+"Radio off");
                        break;
                }
            }
        }, new IntentFilter(SENT));
        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context arg0, Intent arg1) 
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.i(DEBUG_TAG, method+": SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(DEBUG_TAG, method+": SMS not delivered");
                        break;                      
                }
            }
        }, new IntentFilter(DELIVERED));        
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI);               
    }   
	
	/**
	 * Open the device sqlite database and load the contacts table.
	 * If this is the first time, then create the database and tables.
	 * Then add all the device contacts to the app contacts.
	 */
	private void getAppContacts()
	{
		app_contacts = new TreeMap <String,String>();
		String method = "getAppContacts";
		contacts_sqlite_db = context.openOrCreateDatabase(db_name
				,SQLiteDatabase.CREATE_IF_NECESSARY, null);
		contacts_sqlite_db.setLocale(Locale.getDefault());
		contacts_sqlite_db.setLockingEnabled(true);
		contacts_sqlite_db.setVersion(1);
		Cursor c = contacts_sqlite_db.query(contacts_table, null, null, null, null, null, null);
		startManagingCursor(c);
		Log.i(DEBUG_TAG, method+": count "+c.getCount()+" columns: "+c.getColumnCount());
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			String name = c.getString(1);
			String number = c.getString(2);
			app_contacts.put(name, number);
			c.moveToNext();
		}
	}
	
	
	/**
	 * Query the Contacts to get a list of names and phone numbers.
	 * We may need this if the user wants to choose to send it to different groups
	 * @TODO prompt user for group to send message to.
	 */
	private void getDeviceContacts()
	{
		String method = "getDeviceContacts";
		int num_of_app_contacts = 0;
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Log.i(DEBUG_TAG, method+" count "+cur.getCount());
        if (cur.getCount() > 0) 
        {
        	while (cur.moveToNext()) 
        	{
        		String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
        		String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        		Log.i(DEBUG_TAG, "readContacts: id "+id+" name "+name);
        		if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
        		{
        			//Query phone #s, only add contacts with a phone number
        			getPhoneNumbers(id);
        			num_of_app_contacts++;
        			Log.i(DEBUG_TAG, method+": num_of_app_contacts "+num_of_app_contacts);
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
	private void getPhoneNumbers(String id)
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
	        Log.i(DEBUG_TAG, method+": "+name+" ("+number+")");
	        device_contacts.put(name,number);
	    } 
	    dataCursor.close();
	}
	
	/**
	 * Add the template items from the database to the template spinner.
	 * The names go into the spinner, and the names and template body text go
	 * into the template_name_text tree map so they can be retrieved when the
	 * user chooses a template name from the spinner.
	 */
	public void addItemsToTemplateSpinner() 
	{
		final String method = "addItemsOnTemplateSpinner";
		templates_spinner = (Spinner)findViewById(R.id.templates_spinner);
		Log.i(DEBUG_TAG, method+": list size "+templates_list.size());
		ArrayAdapter<String> array_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, templates_list);
		array_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		templates_spinner.setAdapter(array_adapter);
		templates_spinner.setOnItemSelectedListener(new OnItemSelectedListener() 
		{
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
            {
                String selected = templates_spinner.getSelectedItem().toString();
		    	if (selected.equals("<none>"))
		    	{
		    		// enable user to send a message with no template
		    	} else
		    	{
		    		Log.i(DEBUG_TAG, method+": selected "+selected);
		    		String template = template_name_text.get(selected);
		    		Log.i(DEBUG_TAG, method+": template "+template);
		    		Map <String,String> tags = parseTags(template);
		    		EditText send_sms_message_text = (EditText)findViewById(R.id.send_sms_message_text);
		    		send_sms_message_text.setText(template);
		    		fillTags(tags);
		    	}
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
	}
	
	/**
	 * Go through each tag in tags in reverse order and call fillTag to throw up a dialog
	 * to get the user's input for this tag.
	 * @param tags
	 */
	private void fillTags(Map <String,String> tags)
	{
		String method = "fillTags";
		ListIterator<Map.Entry<String, String>> iter = new ArrayList<Entry<String, String>>(tags.entrySet()).listIterator(tags.size());
		while (iter.hasPrevious()) 
		{
			Entry<String, String> entry = iter.previous();
			String tag = (String)entry.getKey();
			//String value = entry.getValue();
			fillTag(tag);
		    String new_content = tag_fill;
		    tags.put(tag, new_content);
		    Log.i(DEBUG_TAG, method+": tag "+tag+" = "+tag_fill);
		}
	}
	
	/**
	 * Replace the tag with the user input and re-fill the edit text field.
	 */
	private void replaceTag()
	{
		String method = "replaceTag";
		EditText send_sms_message_text = (EditText)findViewById(R.id.send_sms_message_text);
		String message = send_sms_message_text.getText().toString();
		int start = message.indexOf("<");
        int end = message.indexOf(">");
        String first_half = message.substring(0, start);
        String second_half = message.substring(end+1, message.length());
        String new_message = first_half+tag_fill+second_half;
        Log.i(DEBUG_TAG, method+": 1st "+first_half+" + "+new_message+" + "+second_half);
        send_sms_message_text.setText(new_message);
	}
	
	/**
	 * Throw up a dialog to get the user's input for the tag passed in.
	 * @param tag The tag to be replaced with the text from the user.
	 */
	private void fillTag(String tag)
	{
		String method = "fillTag";
		tag_fill = "";
		LayoutInflater li = LayoutInflater.from(context);
		View send_sms_fill_tag_popup = li.inflate(R.layout.send_sms_fill_tag_popup, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(send_sms_fill_tag_popup);
		final EditText userInput = (EditText)send_sms_fill_tag_popup.findViewById(R.id.fill_tag_edit_text);
		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("OK",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) 
			    {
			    	tag_fill = userInput.getText().toString();
			    	// replace the tag with the user supplied tag fill.
				    replaceTag();
			    }
			  })
			.setNegativeButton("Cancel",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			    }
			  });
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
		TextView fill_tag_text_view = 
				(TextView)send_sms_fill_tag_popup.findViewById(R.id.fill_tag_text_view);
		fill_tag_text_view.setText("Enter value for <"+tag+"> :");
	}
	
	/**
	 * Go thru the template and extract all the <*tag*> items.
	 * Put them in a linked hash map to be returned.
	 * @param _template The template to parse.
	 * @return The tags parsed from between the < > characters in the template.
	 */
	private Map <String,String> parseTags(String _template)
	{
		String method = "parseTags";
		String template = new String(_template);
		Map <String,String> tags = new LinkedHashMap<String,String>();
		while (template.contains("<"))
		{
			int start = template.indexOf("<");
			int end = template.indexOf(">");
			String tag = template.substring(start+1, end);
			tags.put(tag, "");
			template = template.substring(end+1, template.length());
			Log.i(DEBUG_TAG, method+": tag "+tag);
		}
		return tags;
	}
	
	/**
	 * Set up the database and place it under management,addContact
	 * then call getContacts() to get the contacts table and show it.
	 */
	private void createTemplatesList()
	{
		templates_list = new ArrayList<String>();
		templates_list.add("<none>");
		template_name_text = new TreeMap<String,String>();
		Cursor c = contacts_sqlite_db.query(templates_table, null, null, null, null, null, null);
		startManagingCursor(c);
		getTemplates(c);
	}
	
	/**
	 * Create the list of template names and a sample of the context.
	 * @param c
	 */
	private void getTemplates(Cursor c)
	{
		String method = "getTemplatesContacts";
		Log.i(DEBUG_TAG, method+": count "+c.getCount()+" columns: "+c.getColumnCount());
		c.moveToFirst();
		while (c.isAfterLast() == false)
		{
			String name = c.getString(1);
			String template = c.getString(2);
			int sample_length = template.length();
			if (sample_length > 10)
			{
				sample_length = 10;
			}
			String template_preview = template.substring(0, sample_length);
			templates_list.add(name);
			template_name_text.put(name, template);
			Log.i(DEBUG_TAG, method+": row "+c.getPosition()+": "+name+" "+template_preview);
			c.moveToNext();
		}
	}
	
	/**
	 * Setup the database for this activity.
	 */
	private void setupDatabase()
	{
	    contacts_sqlite_db = context.openOrCreateDatabase(db_name
				,SQLiteDatabase.CREATE_IF_NECESSARY, null);
		contacts_sqlite_db.setLocale(Locale.getDefault());
		contacts_sqlite_db.setLockingEnabled(true);
		contacts_sqlite_db.setVersion(1);
	}

}
