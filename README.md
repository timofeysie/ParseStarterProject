ParseStarterProject
===================

Parse cloud and sms template demo.

######Instructions for use

After installing the app and running it for the first time, you will see two indicators on the main screen:
*  Device contacts
*  App contacts

Choose “Sync” to load all the contacts from the device to the app.  After this, you can go to the “Contacts” activity, and choose contacts to delete from the list.  These contacts will not be deleted from the device contacts.  However, if you delete contacts from the device and later sync, then the deleted contacts will also be deleted from the app contacts.

Choose “Templates” to create a template into which you can insert tags, which will later be replaced by content you choose.  “Add Template” will allow you to create a template and add it to the list.

Once you have a template, choose “New SMS” and you can then select the template to send.  The menu at the top let’s you choose the template.  You can then input values for the tags you created in that template.  Then, to finish, choose “Send SMS to contacts” and all the contacts in your list will be sent that message.

This is just a demo project and is provided on an as-is basis.  See the ParseStarterProjectTest repository for JUnit test for the activities described here.
