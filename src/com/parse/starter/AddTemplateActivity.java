package com.parse.starter;

import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Allow the user create a template with a name, and insert special tags
 * into the body of the text so that it can be used as a template for text messages.
 * @author timothy
 *
 */
public class AddTemplateActivity extends Activity 
{

	final Context context = this;
	private static final String DEBUG_TAG = "AddTemplateActivity";
	/** Database members */
	private static final String db_name = "contacts_sqlite.db";
	SQLiteDatabase contacts_sqlite_db;
	private static final String templates_table = "tbl_templates";
	/** Setup for Adding a template */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.add_template_activity_layout);
	    Log.i(DEBUG_TAG, "onCreate: 1i");
	    
	    // Button listeners
		Button back_button = (Button)findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
            	Log.i(DEBUG_TAG, "back_button.onClick: ");
            	// @TODO Save the current contents before ending the activity. 
            	finish();
            }
        });
     	Button done_button = (Button)findViewById(R.id.done_button);
     	done_button.setOnClickListener(new View.OnClickListener() 
        {
     		public void onClick(View v)
            {
     			// @TODO Make sure the name of the new template is unique.
     			addTemplate();
     			finish();
            }
        });
     	Button location_button = (Button)findViewById(R.id.location_button);
     	location_button.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
                insertTag("location");          
            }
        });
     	Button date_button = (Button)findViewById(R.id.date_button);
     	date_button.setOnClickListener(new View.OnClickListener() 
        {
     		public void onClick(View v)
            {
     			insertTag("date"); 
            }
        });
     	Button time_button = (Button)findViewById(R.id.time_button);
     	time_button.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
            	insertTag("time"); 
            }
        });
     	Button text_button = (Button)findViewById(R.id.text_button);
     	text_button.setOnClickListener(new View.OnClickListener() 
        {
     		public void onClick(View v)
            {
     			insertTag("text"); 
            }
        });
     	Button sign_button = (Button)findViewById(R.id.sign_button);
     	sign_button.setOnClickListener(new View.OnClickListener() 
        {
     		public void onClick(View v)
            {
     			insertTag("signature"); 
            }
        });
	}
	
	private void insertTag(String tag_name)
	{
		final EditText template_edit_text = (EditText) findViewById(R.id.template_text);
		String text_to_insert = "<"+tag_name+">";
        template_edit_text.getText().insert(template_edit_text.getSelectionStart(), text_to_insert);
	}
	
	private void addTemplate()
	{
		setupDatabase();
		final String method = "addTemplate";
		EditText template_name_edit_text = (EditText)findViewById(R.id.template_name_edit_text);
		String template_name = template_name_edit_text.getText().toString();
		final EditText template_edit_text = (EditText) findViewById(R.id.template_text);
        String template_text = template_edit_text.getText().toString();
        if ((template_name.length()>0) || (template_text.length()>0))
        {
        	ContentValues content = new ContentValues();
        	content.put("name", template_name);
        	content.put("template", template_text);
        	long new_id = contacts_sqlite_db.insert(templates_table,  null,  content);
        	Log.i(DEBUG_TAG, method+": new id "+new_id);
        } else
        {
        	Toast.makeText(context, "Name or body cannot be empty!", Toast.LENGTH_SHORT).show();
        }
	}

	/**
	 * Setup the database to insert the finished template.
	 */
	private void setupDatabase()
	{
	    contacts_sqlite_db = context.openOrCreateDatabase(db_name, SQLiteDatabase.CREATE_IF_NECESSARY, null);
		contacts_sqlite_db.setLocale(Locale.getDefault());
		contacts_sqlite_db.setLockingEnabled(true);
		contacts_sqlite_db.setVersion(1);
	}
	
}
