package de.songeasy.android.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.robotium.solo.Solo;

import de.songeasy.android.ActivityMain;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.text.format.DateFormat;
import android.view.Display;
import de.songeasy.android.R;

public class ActivityMainTest extends ActivityInstrumentationTestCase2<ActivityMain> {
	
	
	private Solo solo;
	
	StringBuilder log = new StringBuilder();
	

	// constructor
	public ActivityMainTest() {
		super( ActivityMain.class);
	}

	@Override
	protected void setUp() throws Exception {
		//setUp() is run before a test case is started. 
		//This is where the solo object is created.
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	@Override
	protected void tearDown() throws Exception {
		//tearDown() is run after a test case has finished. 
		//finishOpenedActivities() will finish all the activities that have been opened during the test execution.
		solo.finishOpenedActivities();
	}
	
	//test
	public void testAddNote() throws Exception {
		
		try{
			deviceInfo();

			solo.clearLog();	// clear Logcat
			solo.unlockScreen();	// Unlock the lock screen
			solo.assertCurrentActivity("wrong activity", ActivityMain.class); // check that the right activity is running
			log.append("ActivityMain is running" + "\n");
			solo.clickOnActionBarItem(R.id.opt_menu_file);
			//solo.clickInList(3); // click new Song
			//solo.clickOnButton("TEST");
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_new_song), true);
			log.append("Click TopBarMenu(new Song)"+ "\n");
			solo.clickOnEditText(0); // click Name
			log.append("Click Name"+ "\n");
			solo.enterText(0, "test"); 
			log.append("Click Composer"+ "\n");
			solo.clickOnEditText(1); // click Composer
			solo.enterText(1, "test");
			log.append("Enter Text"+ "\n");
			solo.clickOnButton("OK");
			solo.setActivityOrientation(Solo.LANDSCAPE);
			solo.clickOnActionBarItem(R.id.opt_menu_mode);			
			//solo.clickInList(2); // click Edit
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_mode_edit), true);
			log.append("Click TopBarMenu(Edit)"+ "\n");
			solo.clickOnButton(0); // click toggleButtonAddNote
			log.append("Click toggleButtonAddNote"+ "\n");
			solo.clickLongOnScreen(160, 360);
			solo.clickInList(4); // click on 4th Note;
			
			log.append("Click on 4th Note"+ "\n");
			solo.clickOnActionBarItem(R.id.opt_menu_mode);			
			//solo.clickInList(1); // click View
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_mode_view), true);
			log.append("Click TopBarMenu(View)"+ "\n");
			solo.setActivityOrientation(Solo.PORTRAIT);
			solo.clickOnActionBarItem(R.id.opt_menu_file);			
			//solo.clickInList(2); // click save Song
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_save_song), true);
			log.append("Click TopBarMenu(save Song)"+ "\n");
			solo.waitForText(solo.getString(R.string.opt_menu_save_file),1,2000);
			
			if(Build.VERSION.SDK_INT < 21){
				solo.clickOnText(solo.getString(R.string.opt_menu_save_file));
                // Button-text has to be visible!
			} else {
				solo.clickOnMenuItem(solo.getString(R.string.opt_menu_save_file));
			}
			
			solo.clearEditText(0);
			solo.enterText(0, "test-file");
			log.append("Enter Text"+ "\n");
			solo.clickOnButton("OK");
			solo.clickOnActionBarItem(R.id.opt_menu_file);			
			//solo.clickInList(1); // click load Song
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_load_song), true);
			solo.waitForText("test-file.sea", 1 ,2000);
			solo.clickOnText("test-file.sea");
			log.append("Test-File loaded"+ "\n");
			solo.waitForDialogToClose();
				
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_about));
			solo.goBack();
			solo.clickOnMenuItem(solo.getString(R.string.opt_menu_help));
			solo.goBack();
			solo.takeScreenshot();
			
			saveLogToFile();
			
		} catch(Error e){
			log.append(e.getMessage()+"\n");
			saveLogToFile();
			throw e;
		}
	}

	
	public void deviceInfo(){
		
		String androidVersion = Build.VERSION.RELEASE;
		log.append("Android Version: " + androidVersion + "\n");
		
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		log.append("Device's resolution X: " + width + " Y: " + height + "\n\n");
	}
	
	
	public void saveLogToFile(){
		try {
			
			String myCommand = "logcat -d *:E";
		    Process process = Runtime.getRuntime().exec(myCommand);
		    BufferedReader buffReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    
		    log.append("\n");
		    String line;
		    while ((line = buffReader.readLine()) != null) {
		    log.append(line + "\n");
	        }
		    
		    
		    String dirName = DateFormat.format("ddMMyy-hhmmss", System.currentTimeMillis()) + "_Test";
		    File dirFile = new File(Environment.getExternalStorageDirectory()+"/Robotium-Tests/"+ dirName);
		    
		    String fileName = DateFormat.format("ddMMyy-hhmmss", System.currentTimeMillis()) + "_logcat" + ".txt";
		    File outputFile = new File(dirFile, fileName);
		    
    		if(!outputFile.isFile() && !dirFile.isDirectory()) {
                dirFile.mkdirs(); // make directories
    		}
		    		    
		    
    		
		    BufferedWriter buffwriter = new BufferedWriter(new FileWriter(outputFile));
		    buffwriter.write(log.toString());
		    buffwriter.close();
		    
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	

}
