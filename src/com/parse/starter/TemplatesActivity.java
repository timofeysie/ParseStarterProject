package com.parse.starter;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

public class TemplatesActivity extends Activity 
{
	final Context context = this;
	private static final String DEBUG_TAG = "TemplatesActivity";
	/** Table to hold the list of contacts.*/
	private TableLayout table;
	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String templates_table = "tbl_templates";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.templates_activity_layout);
	    Log.i(DEBUG_TAG, "onCreate: 1c");
	    
	    // Create Template Button
	    ImageButton add_template_button = (ImageButton) findViewById(R.id.add_template_button);
     	add_template_button.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	Log.i(DEBUG_TAG, "Create Template onClick");
            	startActivity(new Intent(TemplatesActivity.this,
                        AddTemplateActivity.class));
            }
        });
     	setupDatabase();
     	createTemplatesList();
	}
	
	/**
	 * Set up the database and place it under management,addContact
	 * then call getContacts() to get the contacts table and show it.
	 */
	private void createTemplatesList()
	{
		table = (TableLayout) findViewById(R.id.templates_table_layout);
		table.removeAllViews();
		Cursor c = contacts_sqlite_db.query(templates_table, null, null, null, null, null, null);
		startManagingCursor(c);
		getTemplatesContacts(c);
	}
	
	/**
	 * Create the list of template names and a sample of the context.
	 * @param c
	 */
	private void getTemplatesContacts(Cursor c)
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
			setupRow(name, template);
			Log.i(DEBUG_TAG, method+": row "+c.getPosition()+": "+name+" "+template_preview);
			c.moveToNext();
		}
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
		table = (TableLayout) findViewById(R.id.templates_table_layout);
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
            	//selectedContact(name, number, v);
            }
        });
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
	
	/**
	* If the user comes back after adding a template, we need to refresh the list.
	**/
	@Override
	protected void onResume() 
	{
	    super.onResume();
	    createTemplatesList();
	}

}
