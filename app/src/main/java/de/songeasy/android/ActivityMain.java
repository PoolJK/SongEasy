/*
 * Copyright (C) 2015 Joerg Krein
 */
package de.songeasy.android;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import de.songeasy.android.ScoreView.ScoreModes;
import de.songeasy.android.dialog.DialogAbout;
import de.songeasy.android.fileactivity.ActivityFileLoadBrowser;
import de.songeasy.android.fileactivity.ActivityFileNew;
import de.songeasy.android.fileactivity.ActivityFileSaveBrowser;
import de.songeasy.android.fileactivity.ActivityFileSaveBrowser.SaveMode;
import de.songeasy.android.fileactivity.AsyncTaskExport;
import de.songeasy.android.fileactivity.AsyncTaskLoad;
import de.songeasy.android.fileactivity.AsyncTaskSave;
import de.songeasy.android.fileactivity.FileType;
import de.songeasy.android.interfaces.OnElementDeleteListener;
import de.songeasy.android.interfaces.OnElementEditListener;
import de.songeasy.android.interfaces.OnElementMoveListener;
import de.songeasy.android.interfaces.OnElementSelectNextListener;
import de.songeasy.android.interfaces.OnSelectModeListener;
import de.songeasy.android.score.Beam;
import de.songeasy.android.score.Chord;
import de.songeasy.android.score.Coda;
import de.songeasy.android.score.Fine;
import de.songeasy.android.score.KeySignature;
import de.songeasy.android.score.KeySignature.KeyName;
import de.songeasy.android.score.Label;
import de.songeasy.android.score.Lyric;
import de.songeasy.android.score.Measure;
import de.songeasy.android.score.Measure.Direction;
import de.songeasy.android.score.MusicFont;
import de.songeasy.android.score.Note;
import de.songeasy.android.score.Rest;
import de.songeasy.android.score.Segno;
import de.songeasy.android.score.Song;
import de.songeasy.android.score.Song.Layout;
import de.songeasy.android.score.TextBlock;
import de.songeasy.android.score.Tie;
import de.songeasy.android.score.TimeSignature;


/**
 * SongEasy main activity
 *
 * @author krein
 */

public class ActivityMain extends Activity implements OnSharedPreferenceChangeListener, BillingProcessor.IBillingHandler {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    // request codes for intents
    static final int LOAD_FILE_REQUEST = 0;
    static final int SAVE_FILE_REQUEST = 1;
    static final int EXPORT_FILE_REQUEST = 2;
    static final int NEW_FILE_REQUEST = 3;
    static final int EDIT_SONG_REQUEST = 4;
    static final int PURCHASE_REQUEST = 5;

    // SKUs for our in app products
    //static final String SKU_PDF_EXPORT = "pdf_export";
    //static final String EXPORT_PDF_PRODUCT_ID = "android.test.purchased";
    static final String EXPORT_PDF_PRODUCT_ID = "pdf_export";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    /**
     * key strings for save instance state
     */
    private static final String BOTTOMBAR_VISIBLE_KEY = "BottomBarVisible";
    private static final String SONG_SAVED_REGULARY_KEY = "SongWasSavedRegulary";
    private static final String ACTIONBAR_SUBTITLE_KEY = "ActionBarSubtitle";
    private static String SE_LICENSE_KEY_START = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiOwGh4HPBZ+6b6jlWy2DVFuvEGyxQnIr/aavld1+DOwPYhpJq12Skv9WN++u5gZ2WsZw1sjEJO1GBvoXtArBgC744E9eeDhhIjrs0/4pdz+aTrIYj7C4i3sqqs6kqV7BMK952GHkMlLQKS5evNZNUHHimU+nO6kEyaJ21FJGyWYynTyvgn8ySQM0btgB3n4FqUtb1iryWU/Kvk+kPawHlW6p23jNnCEw8IlZWlyAvUQFoRoCUSTtiYbZyYDUA9EqN0Qd4/";

    /**
     * context of this activity
     */
    private Context appContext;

    /**
     * android view to show the score inside
     */
    private static ScoreView scoreView;

    /**
     * main bottom bar for editing
     */
    private BottomBarEdit bottomBar;

    /**
     * the app' actionbar on top
     */
    private ActionBar actionBar;

    /**
     * navigation drawer for editing on the left
     */
    DrawerLayout leftEditDrawer;

    /**
     * ListView to fill navigation drawer with
     */
    ExpandableListView drawerList;

    /** Items of the left edit drawer */
    private ArrayList navDrawerItems;
    private ArrayList textChildItems;
    private ArrayList annotationChildItems;
    private ArrayList noteChildItems;

    /**
     * navigation drawer toggle icon in action bar
     */
    private ActionBarDrawerToggle drawerToggle;

    /** SharedPreferences file,keeping key-value pairs */
    // private SharedPreferences sharedPreferences;

    /**
     * if the user wants to update the song date on file save
     */
    private boolean refreshSongDate = false;

    /**
     * if the last used song should be load on startup
     */
    private boolean reloadOnStart = false;

    /**
     * if file was saved by regular file save dialog already
     */
    private boolean fileSavedRegulary = false;

    /**
     * users's last storage location for song files
     */
    private File selectedFile;
    private DateFormat fileTime;

    // in-app-billing helper class
    private BillingProcessor billingP;
    private boolean readyToPurchase = false;


    // does the user have the pdf export upgrade?
    boolean mPdfExportEnabled = false;

    // +++++++ getters and setters +++++++
    // +++++++++++++++++++++++++++++++++++


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Log.d(TAG, "onCreate");

        // force landscape only on small devices
        if (getResources().getBoolean(R.bool.landscape_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // hide title bar, as we use an action bar
        // requestWindowFeature(Window.FEATURE_NO_TITLE);

        // define the UI for this activity
        setContentView(R.layout.lay_activity_main);
        //To remove app icon from action bar
        //getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // we use the application context as it lives as long as our app
        appContext = getApplicationContext();

        // register for preferences change in our preferences dialog
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // create in app billing helper class
        billingP = new BillingProcessor(this, SE_LICENSE_KEY_START + getSecondPart(), this);
        // load purchased items
        billingP.loadOwnedPurchasesFromGoogle();

        // setup actionbar
        actionBar = getActionBar();
        // actionBar.setTitle(getResources().getString(R.string.app_name));
        // actionBar.setSubtitle("NameOfThisSong");
        assert actionBar != null;
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE,
                ActionBar.DISPLAY_SHOW_TITLE);
        // actionBar.setDisplayShowHomeEnabled(true);
        // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // load side menu items
        leftEditDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftEditDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerList = (ExpandableListView) findViewById(R.id.drawerListView);
        navDrawerItems = new ArrayList<NavDrawerItem>();
        textChildItems = new ArrayList<NavDrawerItem>();
        textChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_lyric, null, Lyric.class, null));
        textChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_textblock, null, TextBlock.class, null));
        textChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_label, null, Label.class, null));

        annotationChildItems = new ArrayList<NavDrawerItem>();
        annotationChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_coda, null, Coda.class, null));
        annotationChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_fine, null, Fine.class, null));
        annotationChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_segno, null, Segno.class, null));

        noteChildItems = new ArrayList<NavDrawerItem>();
        noteChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_note_4th, null, Note.class, null));
        noteChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_rest_4th, null, Rest.class, null));
        noteChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_tie, null, null, new Tie(null, null)));
        noteChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_beam_start, null, null, new Beam(Beam.Type.BEGIN, 0)));
        noteChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_beam_cont, null, null, new Beam(Beam.Type.CONTINUE, 0)));
        noteChildItems.add(new NavDrawerItem(R.drawable.btn_toggle_beam_stop, null, null, new Beam(Beam.Type.END, 0)));

        navDrawerItems.add(new NavDrawerItem(R.drawable.btn_toggle_note_8th, noteChildItems, Note.class, null));
        navDrawerItems.add(new NavDrawerItem(R.drawable.btn_toggle_chord, null, Chord.class, null));
        navDrawerItems.add(new NavDrawerItem(R.drawable.btn_toggle_text, textChildItems, null, null));
        navDrawerItems.add(new NavDrawerItem(R.drawable.btn_toggle_annotation, annotationChildItems, null, null));
        navDrawerItems.add(new NavDrawerItem(R.drawable.btn_toggle_measure, null, Measure.class, null));

        drawerList.setAdapter(new NavDrawerListAdapter(this, navDrawerItems));
        drawerList.deferNotifyDataSetChanged();

        drawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // update selected item and title, then close the drawer
                drawerList.setItemChecked(groupPosition, true);
                drawerList.setSelection(groupPosition);
                //   setTitle(navMenuTitles[position]);
                NavDrawerItem selectedGroupItem = (NavDrawerItem)navDrawerItems.get(groupPosition);
               // Toast.makeText(getApplicationContext(),"hello2"+selectedGroupItem.getIcon(),Toast.LENGTH_LONG).show();

                if (selectedGroupItem.getChildNumber() > 0) {
                    // item has children, has no action associated with
                    return false;

                } else {
                    //Change the action bar icon to selected item
                    getActionBar().setIcon(selectedGroupItem.getIcon());
                    scoreView.setMode(ScoreModes.EDIT_ADD);
                    bottomBar.deselectTool();
                    scoreView.setEditElementType(selectedGroupItem.getElementClass());
                    leftEditDrawer.closeDrawer(drawerList);
                    return true;
            }
            }
        });

        drawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // update selected item and title, then close the drawer
                drawerList.setItemChecked(groupPosition, true);
                drawerList.setSelection(groupPosition);
                //   setTitle(navMenuTitles[position]);
                leftEditDrawer.closeDrawer(drawerList);

                // as numbering is not static across group and child items, we need to get child item manually
                NavDrawerItem selectedGroupItem = (NavDrawerItem)navDrawerItems.get(groupPosition);
                NavDrawerItem clickedChildItem = selectedGroupItem.getChild(childPosition);


                if (clickedChildItem != null) {
                    if (clickedChildItem.getElementClass() != null) {
                        //Change the action bar icon with selected item
                        getActionBar().setIcon(clickedChildItem.getIcon());
                        // item has selectable children
                        scoreView.setMode(ScoreModes.EDIT_ADD);
                        bottomBar.deselectTool();
                        scoreView.setEditElementType(clickedChildItem.getElementClass());
                    } else {
                        if (clickedChildItem.getFeature() != null)
                            scoreView.onToggleFeature(clickedChildItem.getFeature());
                    }
                    return true;

                } else {
                    return false;
            }
            }
        });


        // set edit drawer unreachable by default, only activate it on edit mode
        leftEditDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // enabling action bar app icon and behaving it as toggle button
        drawerToggle = new ActionBarDrawerToggle(this, (DrawerLayout) findViewById(R.id.drawer_layout),
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        );

//      public void onDrawerClosed(View view) {
//                context.getActionBar().setTitle(mTitle);
//                // calling onPrepareOptionsMenu() to show action bar icons
//                invalidateOptionsMenu();
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                context.getActionBar().setTitle(mDrawerTitle);
//                // calling onPrepareOptionsMenu() to hide action bar icons
//                invalidateOptionsMenu();
//            }
//        };
        ((DrawerLayout) findViewById(R.id.drawer_layout)).setDrawerListener(drawerToggle);

        // instantiate scoreview
        scoreView = (ScoreView) findViewById(R.id.scoreViewMain);

        // setup bottomBar
        bottomBar = (BottomBarEdit) findViewById(R.id.bottomBarEditMain);

         bottomBar.setOnElementDeleteListener(new OnElementDeleteListener() {

            @Override
            public void onElementDelete() {

                scoreView.onDeleteSelectedElement();
            }
        });


        bottomBar.setOnElementEditListener(new OnElementEditListener() {

            @Override
            public void onElementEdit() {
                scoreView.onEditElement();
                if (Build.VERSION.SDK_INT >= 11) {
                    invalidateOptionsMenu();
                }
            }
        });

        bottomBar
                .setOnElementSelectNextListener(new OnElementSelectNextListener() {

                    @SuppressLint("WrongCall")
                    @Override
                    public void onSelectNext(Direction direction) {

                        Song song = scoreView.getSong();
                        if (song != null) {
                            song.selectNextElementInDirection(direction);
                            scoreView.onLayout(true, 0, 0, 0, 0);
                            scoreView.invalidate();
                        }

                    }
                });

        bottomBar.setOnElementMoveListener(new OnElementMoveListener() {

            @SuppressLint("WrongCall")
            @Override
            public void onMove(Direction direction) {

                Song song = scoreView.getSong();
                if (song != null) {
                    song.moveSelectedElement(direction);
                    scoreView.onLayout(true, 0, 0, 0, 0);
                    scoreView.invalidate();
                }
            }
        });

        bottomBar.setOnModeSelectListener(new OnSelectModeListener() {

            @Override
            public void onSelectMode(ScoreModes mode) {

                switch (mode) {
                    case EDIT_SELECT:
                        getActionBar().setIcon(R.drawable.btn_toggle_select);
                        break;

                    default:
                        break;
                }
                // close drawer ?
                scoreView.setMode(mode);
            }
        });

        bottomBar.setVisibility(View.INVISIBLE);
        // register context menu for score (long click)
        // registerForContextMenu(scoreView);

        // load font with music symbols
        MusicFont.LoadFont(appContext);

        /*
        // get screen size
        switch (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                Log.d(TAG, "small screen");
                break;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                Log.d(TAG, "normal screen");
                break;

            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                Log.d(TAG, "large screen");
                break;

            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                Log.d(TAG, "xlarge screen");
                break;
        }
        // get density (hdpi, xhdpi ...)
        int density = getResources().getDisplayMetrics().densityDpi;
        float dpHeight = getResources().getDisplayMetrics().ydpi;
        float dpWidth = getResources().getDisplayMetrics().xdpi;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                Log.d(TAG, "Density LDPI");
                break;

            case DisplayMetrics.DENSITY_MEDIUM:
                Log.d(TAG, String.format("Density MDPI width: %f", dpWidth));
                break;

            case DisplayMetrics.DENSITY_HIGH:
                Log.d(TAG, String.format("Density HDPI width: %f", dpWidth));
                break;

            case DisplayMetrics.DENSITY_XHIGH:
                Log.d(TAG, String.format("Density XHDPI width: %f", dpWidth));
                break;

            case DisplayMetrics.DENSITY_XXHIGH:
                Log.d(TAG, String.format("Density XXHDPI width: %f", dpWidth));
                break;

            case DisplayMetrics.DENSITY_XXXHIGH:
                Log.d(TAG, String.format("Density XXXHDPI width: %f", dpWidth));
                break;
        }
        */
    }


    /**
     * Add menu items to the activity's options menu
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Log.d(TAG, "onCreateOptionMenu");
        getMenuInflater().inflate(R.menu.options_menu_main, menu);
        setBarIcons(menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * *************************************************************************************
     */

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Log.d(TAG, "onPrepareOptionMenu");
        setBarIcons(menu);

        // hide not purchased items
        if (!billingP.isPurchased(EXPORT_PDF_PRODUCT_ID))
            menu.findItem(R.id.opt_menu_file_export_pdf).setVisible(false);

//       boolean drawerOpen = leftEditDrawer.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.opt_menu_settings_app).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * Only show ActionBar icons suitable for current mode
     *
     * @param menu Options menu to set up with appropriate icons
     */
    private void setBarIcons(Menu menu) {
        Drawable icon;
        // show mode icon according to current mode
        switch (scoreView.getMode()) {

            case VIEW:
                // show view mode icon
                icon = menu.findItem(R.id.opt_menu_mode_view).getIcon();
                menu.findItem(R.id.opt_menu_mode).setIcon(icon);
                // hide quick save button
                menu.findItem(R.id.opt_menu_file_save_quick).setVisible(false);
                 // Log.d(TAG, "setBarIcons: mode view");
                break;

            case EDIT_ADD:
                // fall through, both use the same icon
            case EDIT_SELECT:
                // show edit mode icon
                icon = menu.findItem(R.id.opt_menu_mode_edit).getIcon();
                menu.findItem(R.id.opt_menu_mode).setIcon(icon);
                // show quick save icon if song was saved regulary before
                if (scoreView.getSong() != null) {
                    if (fileSavedRegulary)
                        menu.findItem(R.id.opt_menu_file_save_quick)
                                .setVisible(true);
                    else
                        menu.findItem(R.id.opt_menu_file_save_quick)
                                .setVisible(false);
                } else
                    menu.findItem(R.id.opt_menu_file_save_quick).setVisible(
                            false);
                // Log.d(TAG, "setBarIcons: mode edit");
                break;

            case PLAY:
                icon = menu.findItem(R.id.opt_menu_mode_play).getIcon();
                menu.findItem(R.id.opt_menu_mode).setIcon(icon);
                menu.findItem(R.id.opt_menu_file_save_quick).setVisible(false);
                // Log.d(TAG, "setBarIcons: mode play");
                break;

            default:
                break;
        }

        // hide not purchased items
        if (!billingP.isPurchased(EXPORT_PDF_PRODUCT_ID))
            menu.findItem(R.id.opt_menu_file_export_pdf).setVisible(false);

        // hide not yet supported features
        if (!Features.WIRELESS_SYNC.isEnabled())
            menu.findItem(R.id.opt_menu_social).setVisible(false);

        if (!Features.PLAY.isEnabled())
            menu.findItem(R.id.opt_menu_mode_play).setVisible(false);
    }


    /**
     * The activity's options menu event handler
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    // TODO show some progress bar while loading or saving like in
    // http://blog.ribomation.com/2011/07/android-and-xml/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // kinda strange !?
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.opt_menu_file_load:
                // intent for starting activity LoadFileExplorer
                final Intent loadFileIntent = new Intent(this,
                        ActivityFileLoadBrowser.class);

                // call ActivityFileLoad with last storage location
                final Bundle loadBundle = new Bundle();

                // File testFile = new File("/sdfds/dfg");
                loadBundle.putSerializable(
                        ActivityFileLoadBrowser.KEY_SELECTED_FOLDER,
                        selectedFile);

                loadFileIntent.putExtras(loadBundle);

                // start LoadFileExplorer for storage
                startActivityForResult(loadFileIntent, LOAD_FILE_REQUEST);
                return true;

            case R.id.opt_menu_file_save_quick:
                if (selectedFile != null && fileSavedRegulary) {

                    scoreView.getSong().setChanged(false);

                    if (refreshSongDate) {
                        fileTime = DateFormat.getDateInstance();
                        fileTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String time = fileTime.format(new Date());
                        scoreView.getSong().getTitleBlock().setDate(time);
                    }

                    // start file save activity in new task
                    AsyncTaskSave task = new AsyncTaskSave(this,
                            scoreView.getSong(), selectedFile);
                    task.execute();

                    return true;
                }
                // fall through to file save explorer

            case R.id.opt_menu_file_save:

                // try to get song from view
                Song songToSave = scoreView.getSong();

                // create default song if no song exists
                if (songToSave == null) {
                    Toast.makeText(this, "Nothing to save!", Toast.LENGTH_SHORT)
                            .show();
                    return true;
                    // songToSave = scoreView.createDefaultSong();
                }

                final Intent saveFileIntent = new Intent(this,
                        ActivityFileSaveBrowser.class);
                final Bundle saveBundle = new Bundle();

                String songName = songToSave.getTitleBlock().getTitle();

                if (songName != null)
                    saveBundle.putString(ActivityFileSaveBrowser.KEY_SONG_NAME,
                            songName);
                else
                    saveBundle.putString(ActivityFileSaveBrowser.KEY_SONG_NAME,
                            "EmptySong");

                saveBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_FILE_TYPE,
                        FileType.COMPRESSED_SONG);

                saveBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_SELECTED_FOLDER,
                        selectedFile);

                saveBundle.putSerializable(ActivityFileSaveBrowser.KEY_MODE,
                        SaveMode.SAVE);

                saveFileIntent.putExtras(saveBundle);
                startActivityForResult(saveFileIntent, SAVE_FILE_REQUEST);
                return true;

            case R.id.opt_menu_file_export_image:
                if (scoreView.getSong() == null) {
                    Toast.makeText(this, "Nothing to export!",
                            Toast.LENGTH_SHORT).show();
                    return true;
                    // songToSave = scoreView.createDefaultSong();
                }

                Intent exportImageIntent = new Intent(this,
                        ActivityFileSaveBrowser.class);
                Bundle exportImageBundle = new Bundle();

                String exPortImageName = scoreView.getSong().getTitleBlock()
                        .getTitle();

                if (exPortImageName != null)
                    exportImageBundle.putString(
                            ActivityFileSaveBrowser.KEY_SONG_NAME,
                            exPortImageName);
                else
                    exportImageBundle.putString(
                            ActivityFileSaveBrowser.KEY_SONG_NAME, "EmptySong");

                exportImageBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_FILE_TYPE, FileType.PNG);

                // start save file activity with parameter of storage location
                // selectedFile can be null at this point!?
                exportImageBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_SELECTED_FOLDER,
                        selectedFile);

                exportImageBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_MODE, SaveMode.EXPORT);

                exportImageIntent.putExtras(exportImageBundle);
                startActivityForResult(exportImageIntent, EXPORT_FILE_REQUEST);
                return true;

            // export song as pdf file
            case R.id.opt_menu_file_export_pdf:
                if (scoreView.getSong() == null) {
                    Toast.makeText(this, R.string.error_no_song_to_export,
                            Toast.LENGTH_SHORT).show();
                    return true;
                    // songToSave = scoreView.createDefaultSong();
                }

                if (!billingP.isPurchased(EXPORT_PDF_PRODUCT_ID)) {
                    Toast.makeText(this, R.string.error_option_not_purchased, Toast.LENGTH_LONG);
                    return true;
                }

                Intent exportPDFIntent = new Intent(this,
                        ActivityFileSaveBrowser.class);
                Bundle exportPDFBundle = new Bundle();

                String exPortPDFName = scoreView.getSong().getTitleBlock()
                        .getTitle();

                if (exPortPDFName != null)
                    exportPDFBundle.putString(
                            ActivityFileSaveBrowser.KEY_SONG_NAME,
                            exPortPDFName);
                else
                    exportPDFBundle.putString(
                            ActivityFileSaveBrowser.KEY_SONG_NAME, "EmptySong");

                exportPDFBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_FILE_TYPE, FileType.PDF);

                // start save file activity with parameter of storage location
                // selectedFile can be null at this point!?
                exportPDFBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_SELECTED_FOLDER,
                        selectedFile);

                exportPDFBundle.putSerializable(
                        ActivityFileSaveBrowser.KEY_MODE, SaveMode.EXPORT);

                exportPDFIntent.putExtras(exportPDFBundle);
                startActivityForResult(exportPDFIntent, EXPORT_FILE_REQUEST);
                return true;

            // open preferences activity
            case R.id.opt_menu_settings_app:
                startActivity(new Intent(this, ActivityEditPreferences.class));
                return true;

            // open ActivityFileNew for editing song parameters
            case R.id.opt_menu_settings_song:
                if (scoreView.getSong() != null) {
                    final Intent fileSettingsIntent = new Intent(this,
                            ActivityFileNew.class);
                    final Bundle fileSettingsBundle = new Bundle();
                    // put parameters of current song in bundle
                    // open activity in edit mode
                    fileSettingsBundle
                            .putSerializable(ActivityFileNew.KEY_MODE,
                                    ActivityFileNew.Mode.EDIT);
                    fileSettingsBundle.putString(ActivityFileNew.KEY_SONG_NAME,
                            scoreView.getSong().getTitleBlock().getTitle());
                    fileSettingsBundle.putString(
                            ActivityFileNew.KEY_COMPOSER_NAME, scoreView
                                    .getSong().getTitleBlock().getComposer());
                    // get key signature of first staff of first measure
                    fileSettingsBundle.putParcelable(
                            ActivityFileNew.KEY_KEY_SIGNATURE, scoreView
                                    .getSong().getMeasures().get(0).getStaves()
                                    .get(0).getKeySignature());
                    // put time signature of first staff of first measure
                    fileSettingsBundle.putParcelable(
                            ActivityFileNew.KEY_TIME_SIGNATURE, scoreView
                                    .getSong().getMeasures().get(0).getStaves()
                                    .get(0).getTimeSignature());

                    fileSettingsIntent.putExtras(fileSettingsBundle);
                    startActivityForResult(fileSettingsIntent,
                            EDIT_SONG_REQUEST);


                }
                return true;

            case R.id.opt_menu_file_new:
                final Intent newFileIntent = new Intent(this,
                        ActivityFileNew.class);
                final Bundle fileNewBundle = new Bundle();
                // put parameters of current song in bundle
                // open activity in new song mode
                fileNewBundle.putSerializable(ActivityFileNew.KEY_MODE,
                        ActivityFileNew.Mode.NEW);
                newFileIntent.putExtras(fileNewBundle);
                startActivityForResult(newFileIntent, NEW_FILE_REQUEST);
                return true;

            case R.id.opt_menu_help:
                final Intent showHelpIntent = new Intent(this,
                        ActivityHelp.class);
                startActivity(showHelpIntent);
                return true;

            case R.id.opt_menu_about:
                DialogAbout about = new DialogAbout(this);
                about.show();
                return true;

            case R.id.opt_menu_billing:
                // intent for starting activity LoadFileExplorer
                final Intent purchaseIntent = new Intent(this, ActivityPurchase.class);
                final Bundle purchaseBundle = new Bundle();

                if (billingP.isPurchased(EXPORT_PDF_PRODUCT_ID))
                    purchaseBundle.putBoolean(ActivityPurchase.KEY_OPTION_SELECTED, true);
                else
                    purchaseBundle.putBoolean(ActivityPurchase.KEY_OPTION_SELECTED, false);

                purchaseIntent.putExtras(purchaseBundle);
                startActivityForResult(purchaseIntent, PURCHASE_REQUEST);

                //setWaitScreen(true);
                /* TODO: for security, generate your payload here for verification. See the comments on
                 *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
                 *        an empty string, but on a production app you should carefully generate this. */
                //String payload = "IDOfUser";
                //iabHelper.launchPurchaseFlow(this, SKU_PDF_EXPORT, RC_REQUEST, mPurchaseFinishedListener, payload);
                return true;

            // If home icon is clicked return to main Activity
            case android.R.id.home:
                // Intent intent = new Intent(this, OverviewActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * Evaluate results from started sub-activities.
     *
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @SuppressLint("WrongCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case LOAD_FILE_REQUEST:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        if (!data.hasExtra(ActivityFileLoadBrowser.KEY_SELECTED_FILE))
                            break;
                        selectedFile = (File) data.getExtras().getSerializable(
                                ActivityFileLoadBrowser.KEY_SELECTED_FILE);
                        saveSelectedFilePath();

                        // start loading of file with progress bar
                        AsyncTaskLoad task = new AsyncTaskLoad(this,
                                scoreView.getSong(), selectedFile);
                        task.execute();
                        setFileSavedNormally(false);
                        break;

                    default:
                        scoreView.invalidate();
                        break;
                }
                break;

            case SAVE_FILE_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        if (!data.hasExtra(ActivityFileSaveBrowser.KEY_SELECTED_FILE))
                            break;

                        selectedFile = (File) data.getExtras().getSerializable(
                                ActivityFileSaveBrowser.KEY_SELECTED_FILE);

                        // remember last selected file in preferences
                        saveSelectedFilePath();

                        // refresh songdate on save if requested
                        if (refreshSongDate) {
                            fileTime = DateFormat.getDateInstance();
                            fileTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String time = fileTime.format(new Date());
                            scoreView.getSong().getTitleBlock().setDate(time);
                        }

                        scoreView.getSong().setChanged(false);
                        if (Build.VERSION.SDK_INT >= 11) {
                            invalidateOptionsMenu();
                        }
                        // start file save activity in new task
                        AsyncTaskSave task = new AsyncTaskSave(this,
                                scoreView.getSong(), selectedFile);
                        task.execute();

                        setFileSavedNormally(true);
                        break;

                    default:
                        break;
                }
                break;

            case EXPORT_FILE_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // check if file name was selected
                        if (!data.hasExtra(ActivityFileSaveBrowser.KEY_SELECTED_FILE))
                            break;

                        selectedFile = (File) data.getExtras().getSerializable(
                                ActivityFileSaveBrowser.KEY_SELECTED_FILE);

                        FileType fileType = (FileType) data.getExtras()
                                .getSerializable(
                                        ActivityFileSaveBrowser.KEY_FILE_TYPE);
                        Song songToExport = scoreView.getSong();

                        // reset view to standard before exporting
                        scoreView.setZoom(1.f);
                        scoreView.scrollTo(0, 0);

                        // deselect previous selected element
                        songToExport.deselectElement();

                        AsyncTaskExport exportTask = new AsyncTaskExport(this, songToExport,
                                selectedFile, fileType);
                        exportTask.execute();

                        // refresh view
                        scoreView.onLayout(true, 0, 0, 0, 0);
                        break;

                    default:
                        break;
                }
                break;

            case NEW_FILE_REQUEST:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        String newSongName = "NewSong";
                        String newSongComposerName = "";
                        KeySignature newSongKey = new KeySignature(
                                KeyName.CMAJOR);

                        TimeSignature newSongTime = new TimeSignature(4, 4);
                        Song.Layout newSongLayout = Layout.VOICE;

                        if (data.hasExtra(ActivityFileNew.KEY_SONG_NAME)) {
                            newSongName = data.getExtras().getString(
                                    ActivityFileNew.KEY_SONG_NAME);
                        }

                        if (data.hasExtra(ActivityFileNew.KEY_COMPOSER_NAME)) {
                            newSongComposerName = data.getExtras().getString(
                                    ActivityFileNew.KEY_COMPOSER_NAME);
                        }

                        if (data.hasExtra(ActivityFileNew.KEY_KEY_SIGNATURE)) {
                            newSongKey = data.getExtras().getParcelable(
                                    ActivityFileNew.KEY_KEY_SIGNATURE);
                        }

                        if (data.hasExtra(ActivityFileNew.KEY_TIME_SIGNATURE)) {
                            newSongTime = data.getExtras().getParcelable(
                                    ActivityFileNew.KEY_TIME_SIGNATURE);
                        }

                        if (data.hasExtra(ActivityFileNew.KEY_LAYOUT)) {
                            newSongLayout = (Song.Layout) data
                                    .getExtras()
                                    .getSerializable(ActivityFileNew.KEY_LAYOUT);
                        }
                        // date ??
                        // ******************************************************************
                        Song newSong = new Song(newSongName,
                                newSongComposerName, newSongLayout, newSongKey,
                                newSongTime, "21.12.2013", "1.0.1");
                        newSong.setChanged(true);

                        if (Build.VERSION.SDK_INT >= 11) {
                            invalidateOptionsMenu();
                        }
                        scoreView.setSong(newSong);
                        // reset view
                        scoreView.setZoom(1.f);
                        scoreView.scrollTo(0, 0);

                        // file has no path and name
                        selectedFile = null;
                        setFileSavedNormally(false);
                        // scoreView.invalidate();
                        actionBar.setSubtitle("");
                        break;
                }
                break;

            case EDIT_SONG_REQUEST:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Song songToEdit = scoreView.getSong();

                        if (data.hasExtra(ActivityFileNew.KEY_SONG_NAME)) {
                            songToEdit.getTitleBlock().setTitle(
                                    data.getExtras().getString(
                                            ActivityFileNew.KEY_SONG_NAME));
                            // actionBar.setSubtitle(data.getExtras().getString(
                            // ActivityFileNew.KEY_SONG_NAME));
                        }

                        if (data.hasExtra(ActivityFileNew.KEY_COMPOSER_NAME)) {
                            songToEdit.getTitleBlock().setComposer(
                                    data.getExtras().getString(
                                            ActivityFileNew.KEY_COMPOSER_NAME));
                        }

                        // if (data.hasExtra(ActivityFileNew.KEY_KEY_SIGNATURE))
                        // {
                        // newSongKey = data.getExtras().getParcelable(
                        // ActivityFileNew.KEY_KEY_SIGNATURE);
                        // }
                        //
                        // if
                        // (data.hasExtra(ActivityFileNew.KEY_TIME_SIGNATURE)) {
                        // newSongTime = data.getExtras().getParcelable(
                        // ActivityFileNew.KEY_TIME_SIGNATURE);
                        // }
                        // update view
                        scoreView.invalidate();
                        songToEdit.setChanged(true);
                        break;

                    default:
                        break;
                }
                break;

            case PURCHASE_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (!data.hasExtra(ActivityPurchase.KEY_OPTION_SELECTED))
                            break;

                        if ((boolean) data.getExtras().getBoolean(ActivityPurchase.KEY_OPTION_SELECTED)) {
                            if (!billingP.isPurchased(EXPORT_PDF_PRODUCT_ID)) {
                                billingP.purchase(this, EXPORT_PDF_PRODUCT_ID);
                                //Toast.makeText(appContext, "Product purchased!", Toast.LENGTH_LONG).show();
                            } else {
                                //billingP.consumePurchase(EXPORT_PDF_PRODUCT_ID);
                                Toast.makeText(appContext, "Product already purchased!", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }

        if (!billingP.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * Actualise preferred storage location if user changed it.
     *
     * @param sharedPreferences preferences that were changes
     * @param key               key of value that was changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        if (key.equals(getResources().getString(R.string.pref_key_update_date))) {
            refreshSongDate = sharedPreferences.getBoolean(getResources()
                    .getString(R.string.pref_key_update_date), false);
        }

        // if (key == getResources().getString(R.string.pref_key_load_on_start))
        // {
        // reloadOnStart = sharedPreferences.getBoolean(getResources()
        // .getString(R.string.pref_key_load_on_start), false);
        // }
    }


	/*
     * rotate device: onSaveInstanceState onPause
	 * onRetainNonConfigurationInstance -----rotation--------- onCreate
	 * onRestoreInstanceState onResume
	 */

	/*
     * back key: onPause ----recently used------ onCreate on_Resume (maybe
	 * implement onBackKey and save in SharedPreferences?)
	 */

	/*
	 * home key: onSaveInstanceState onPause ----recently used------ on_Resume
	 * (works without any saving!)
	 */

    /**
     * Save current song in RAM if we are getting destroyed, but will be
     * reactivated shortly after, e.g. the device is rotated.
     *
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @SuppressWarnings("deprecation")
    @Override
    public Object onRetainNonConfigurationInstance() {

        Log.d(TAG, "onRetainNonConfigurationInstance");

        Song songToSave = scoreView.getSong();

        return songToSave;
    }


    /**
     * Save song title in bundle on activity destroy. Song is saved in
     * onRetainNonConfigurationInstance()
     *
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle) {

        Log.d(TAG, "onSaveInstanceState");

        super.onSaveInstanceState(bundle);

        // if (scoreView.getSong() != null)
        // bundle.putString(SONG_NAME_KEY, scoreView.getSong().getName());

        // save subtitle of actionbar
        bundle.putCharSequence(ACTIONBAR_SUBTITLE_KEY, actionBar.getSubtitle());

        if (bottomBar.getVisibility() == View.VISIBLE)
            bundle.putBoolean(BOTTOMBAR_VISIBLE_KEY, true);
        else
            bundle.putBoolean(BOTTOMBAR_VISIBLE_KEY, false);

        bundle.putBoolean(SONG_SAVED_REGULARY_KEY, fileSavedRegulary);
    }


    /**
     * Save current state in RAM if we are getting killed. (non-Javadoc)
     *
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        Log.d(TAG, "onRestoreInstanceState");


        super.onRestoreInstanceState(savedInstanceState);

		/* in case of restart restore song */
        Song song = (Song) getLastNonConfigurationInstance();
        // set song from saved object, only if it is not null
        if (song != null) {
            // only if there isn't already a song
            if (scoreView.getSong() == null) {
                scoreView.setSong(song);
            }
        }

        if (savedInstanceState.getBoolean(BOTTOMBAR_VISIBLE_KEY)) {
            bottomBar.setVisibility(View.VISIBLE);
            // show left navigation drawer
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            // show default action in action bar
            getActionBar().setIcon(R.drawable.btn_toggle_select);
        } else {
            bottomBar.setVisibility(View.INVISIBLE);
        }


        actionBar.setSubtitle(savedInstanceState
                .getCharSequence(ACTIONBAR_SUBTITLE_KEY));

        setFileSavedNormally(savedInstanceState
                .getBoolean(SONG_SAVED_REGULARY_KEY));
    }


    /**
     * Activity is about to be left by user. Store last used file in
     * preferences.
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {

        Log.d(TAG, "onPause");

        // must call super first
        super.onPause();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(
                getResources().getString(
                        R.string.pref_key_last_storage_location),
                selectedFile.getPath());

        editor.apply();

        // if (midi != null)
        // midi.stop();
    }


    /**
     * Resume activity that was interrupted. Load song from temporarily file if
     * we were moved to background only.
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        Log.d(TAG, "onResume");

        // must call super first
        super.onResume();

        // final File sharedDir = getCacheDir();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        // +++ restore user settings from Preferences ++++
        if (prefs.contains(getResources().getString(
                R.string.pref_key_update_date)))
            refreshSongDate = prefs.getBoolean(
                    getResources().getString(R.string.pref_key_update_date),
                    false);

        if (prefs.contains(getResources().getString(
                R.string.pref_key_load_on_start)))
            reloadOnStart = prefs.getBoolean(
                    getResources().getString(R.string.pref_key_load_on_start),
                    false);

        // load last used file storage location
        if (prefs.contains(getResources().getString(
                R.string.pref_key_last_storage_location))) {
            selectedFile = new File(prefs.getString(
                    getResources().getString(
                            R.string.pref_key_last_storage_location), ""));
        } else
            selectedFile = new File("/mnt/sdcard");

        // reload last file if user set up so and it's not already loaded by
        // restoreInstanceState
        if (reloadOnStart && scoreView.getSong() == null) {
            // start loading of file with progress bar
            if (selectedFile.isFile()) {
                AsyncTaskLoad task = new AsyncTaskLoad(this,
                        scoreView.getSong(), selectedFile);
                task.execute();

            }
        }
    }


    /**
     * Unbind from in-app-billing when quitting.
     */
    @Override
    public void onDestroy() {
        if (billingP != null)
            billingP.release();

        super.onDestroy();
    }

    /**
     * Set ScoreView to mode selected by user. Show corresponding icon in
     * ActionBar.
     *
     * @param item selected item
     */
    public void onModeSelect(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.opt_menu_mode_view:
                bottomBar.setVisibility(View.INVISIBLE);
                leftEditDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                scoreView.setMode(ScoreModes.VIEW);
                scoreView.setVerticalScrollOffset(0);
                // hide left navigation drawer
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setHomeButtonEnabled(false);
                // show app icon in action bar
                actionBar.setIcon(getResources().getDrawable(R.drawable.launcher));
                break;

            case R.id.opt_menu_mode_edit:
                if (scoreView.getSong() != null) {
                    leftEditDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    scoreView.setMode(ScoreModes.EDIT_SELECT);
                    // show bottom bar, bottom bar restores last edit mode
                    bottomBar.setVisibility(View.VISIBLE);
                    scoreView.setVerticalScrollOffset(bottomBar.getSize());
                    // show left navigation drawer
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeButtonEnabled(true);
                    // show default action in action bar
                    getActionBar().setIcon(R.drawable.btn_toggle_select);
                }
                break;

            case R.id.opt_menu_mode_play:
                if (scoreView.getSong() != null) {
                    bottomBar.setVisibility(View.INVISIBLE);
                    scoreView.setMode(ScoreModes.PLAY);
                    scoreView.setVerticalScrollOffset(0);
                }
                break;

            default:
                break;
        }
        if (Build.VERSION.SDK_INT >= 11) {
            invalidateOptionsMenu();
        }
    }


    public void onOptionsItemZoomFullScreen(MenuItem item) {
        scoreView.setZoom(1.f);
        scoreView.scrollTo(0, 0);
    }


    /**
     * Save path to last selected file to preferences
     */
    private void saveSelectedFilePath() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(
                getResources().getString(
                        R.string.pref_key_last_storage_location),
                selectedFile.getPath());
        editor.commit();
    }


    void setFileSavedNormally(boolean value) {
        fileSavedRegulary = value;
        if (Build.VERSION.SDK_INT >= 11) {
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        invalidateOptionsMenu();
        Log.d(TAG, "onProductPurchased:" + s);
        //Toast.makeText(this, "onProductPurchased: " + s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored:");
        //Toast.makeText(this, "onPurchaseHistoryRestored", Toast.LENGTH_LONG).show();

        for (String sku : billingP.listOwnedProducts())
            Log.d(TAG, "Owned Managed Product: " + sku);

        for (String sku : billingP.listOwnedSubscriptions())
            Log.d(TAG, "Owned Subscription: " + sku);
    }

    @Override
    public void onBillingError(int i, Throwable throwable) {
        Log.d(TAG, "onBillingError:" + Integer.toString(i));
        Toast.makeText(this, "onBillingError: " + Integer.toString(i), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized");
        //Toast.makeText(this, "onBillingInitialized", Toast.LENGTH_LONG).show();
        readyToPurchase = true;
    }

    /**
     * Get second part of secret license key
     *
     * @return String with second part of license key
     */
    private String getSecondPart() {
        String start = "wWS2XlcxaEvleUuzljc9oQAb8Xb3nddBKpLc5YFPTY8sLwQebSqdEaDyalqpiht2wXXqLLZyhrGQ94IQIDAQABmPX";
        String returnStr = start.substring(0, start.length() - 3);
        return returnStr;
    }
} // class
