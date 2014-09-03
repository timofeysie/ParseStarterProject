ParseStarterProject
===================

Parse cloud and sms template demo.

######Instructions for use

To build ParseStarterProject, you must make sure you have the correct SDK libraries for the target listed in the AndroidManifest.xml file.
The current settings are:
```
<uses-sdk android:minSdkVersion="8" android:targetSdkVersion="19"/>
```
On another system, targeting SDK 10, had to use these settings:
```
<uses-sdk android:minSdkVersion="10" android:targetSdkVersion="10"/>
```

You will also need to reset the directory to the android.jar file in the SDK on your system.  

After installing the app and running it for the first time, you will see two indicators on the main screen:
*  Device contacts
*  App contacts

Choose “Sync” to load all the contacts from the device to the app.  After this, you can go to the “Contacts” activity, and choose contacts to delete from the list.  These contacts will not be deleted from the device contacts.  However, if you delete contacts from the device and later sync, then the deleted contacts will also be deleted from the app contacts.

Choose “Templates” to create a template into which you can insert tags, which will later be replaced by content you choose.  “Add Template” will allow you to create a template and add it to the list.

Once you have a template, choose “New SMS” and you can then select the template to send.  The menu at the top let’s you choose the template.  You can then input values for the tags you created in that template.  Then, to finish, choose “Send SMS to contacts” and all the contacts in your list will be sent that message.

This is just a demo project and is provided on an as-is basis.  See the ParseStarterProjectTest repository for JUnit test for the activities described here.

This app was developed on a 64 bit installation of Fedora 19 Linux in Eclipse 4.3.1.
The app has been run using the Eclipse Android Virtual Device with the following settings:
```
Device: Nexus S (4””, 480 x 800: hdpi)
Target: Android 2.3.3 - API Level 10
CPU/ABI: ARM (armeable)
Keyboard: hardware present
Memory Options: RAM 343, VM 32
Internal Storage: 200 MiB
```


##To do

- create a list of groups:
1. device contacts
2. app contacts
3. family
4. friends
5. aquaintences
7. co-workers
8. Group 8
9. Group 9
10. Group 10

Let the user send a message vie template to a single contact from a group.
