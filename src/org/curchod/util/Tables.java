package org.curchod.util;

import android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("unused")
public class Tables 
{
	
	/** Database members */
	public static final String db_name = "contacts_sqlite.db";
	public static SQLiteDatabase contacts_sqlite_db;
	public static final String CREATE_CONTACTS_TABLE = "CREATE TABLE tbl_contacts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, number TEXT);";
	public static final String CREATE_TEMPLATES_TABLE = "CREATE TABLE tbl_templates ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, template TEXT);";
	public static final String CREATE_GROUPS_TABLE = "CREATE TABLE tbl_groups ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, template TEXT);";
	public static final String CREATE_CONTACTS_GROUPS_TABLE = 
			"CREATE TABLE tbl_contacts_groups (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "groups_id INTEGER NOT NULL CONSTRAINT groups_id REFERENCES tbl_groups(_id) ON DELETE CASCADE, "
			+ "contacts_id INTEGER NOT NULL CONSTRAINT contacts_id REFERENCES tbl_contacts(_id) ON DELETE CASCADE)";

	/** Contact Object members */
	public static final String contacts_table = "tbl_contacts";
	public static final String CONTACT_OBJECT = "ContactObject";
	public static final String CONTACT_NAME = "name";
	public static final String CONTACT_PHONE = "number";
	
	/** Groups table*/
	public static final String groups_table = "tbl_groups";
	
}
