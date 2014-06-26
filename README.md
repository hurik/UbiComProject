# UbiComProject #

## Classes ##

### MainActivity.java ###
Shows own location and locations of near friends.

###	RegisterActivity.java ###
Starts when you start the first time the app. You must enter your Phonenumber to register your phone on the server. When the own phone number is stored at the sim card, you must not enter the number.

### AllowedNumbersActivity ###
In the activity Allowed Number you see all registered and existing Numbers from your Phone. Here you can enable or disable sending the position to your friend.


### services.BootBroadcastReceiver.java ###
Starts the LocationService and NotificationService after the handy was booted.

### services.LocationService.java ###
Service which gets the own location and upload it on the FTP server.

### services.NotificationService.java ###
Service which gets the location of known numbers from the FTP server and provide them for the MainActicity.java. It also creates notifications, if a friend is near.

### helpers.ApplicationData.java ###
ApplicationData saves your and all position of your friend. Also saves the Date of the last Notification from every person.

### helpers.CalendarEvents.java ###
In ClanderEvents you can get the current or next Event from the Calendar or you can check if the owner of the Phone is busy or not.  

### helpers.InternetConnection.java ###
Here you can check if the phone connected to the internet.

### helpers.Notify.java ###
notify is a class to create a Notification on your Phone. After clicking the Notification youzZoom to the location of your friend near.

### helpers.Phonebook.java ###
With the class Phonebook you can read all Numbers from your Phone or get the name associated with a phonenumber.
And you can check whether a phonenumbers starts with +49 and correct them if necessary.

### helpers.Preferences.java ###
In the preferences all settings are stored.
This applies the allowednumbers, the app version, the registration ID and your own number.

## Tutorials ##

1. [Android working with Google Maps V2](http://www.androidhive.info/2013/08/android-working-with-google-maps-v2/)
2. [How to add Google Maps in an Android app in 34592 simple steps](http://www.creativepulse.gr/en/blog/2014/how-to-add-google-maps-in-an-android-app-in-34592-simple-steps)
3. [Android Services](http://www.tutorialspoint.com/android/android_services.htm)
4. [Background service with location listener in android](http://stackoverflow.com/questions/14478179/background-service-with-location-listener-in-android)
5. [Using LocalBroadcastManager in Service to Activity Communications](http://www.intertech.com/Blog/using-localbroadcastmanager-in-service-to-activity-communications/)
6. [commons-net-3.3-bin.zip](http://commons.apache.org/proper/commons-net/download_net.cgi)
7. [How to import org.apache.commons.net.ftp.FTPClient] (http://stackoverflow.com/questions/8280594/how-to-import-org-apache-commons-net-ftp-ftpclient)
8. [FTP API] (http://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html#storeFile%28java.lang.String,%20java.io.InputStream%29)
