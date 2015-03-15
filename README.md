Requirement:
Create a native Android app to simplify paying bills with your smartphone.   
1.	A user should be able to register/log in at the app with Facebook connect only.   
2.	A user should be able to take a picture/scan his invoice and get a suggestion of payment info as where to send the money, what sum and what date.  
3.	The user must be able to adapt/correct scanned data.
4.	The user must finish the payment of the bills by choosing a new or saved credit card to pay for the invoices.   
 
Understanding:
An Android application which has 3 main components as listed below. 
1.	Application authentication.
2.	Scan the invoice and identify the details for payment.
3.	Integrate Payment gateway.
Detailed understanding provided in following dedicated pages. 
The application is developed with an aim to achieve the functionality within the time frame than code optimization. I agree code optimization has to carry out for really reliable software piece.

Pre-requisites:
•	The Application verified on Android 5.0.1 using Android Eclipse ADT.
•	In step2, the actual requirement is to take picture or scan the invoice. But, only the second part is implemented. Hence it’s expected to copy the provided Image (Invoice, path: <downloaded_folder>\GM_assignment_final\Images\Invoice\bill.jpg) and select the image (The invoice) by tapping onto SCAN   button from the Action bar menu (Would be avail in below for smaller screen size). Currently, the scan may support only provided image though the image content read using regular expression.
•	Libraries used: 
a) Facebook SDK, v3.21.1 as Library attached to Application. Includes Facebook’s other Jars such android-support-v4.jar and bolts-android1.1.2.jar (Both from FB SDK) 
b) PayPal SDK, v2.8.4 as .Jar file attached to projects.
