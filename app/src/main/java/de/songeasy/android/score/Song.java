/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import de.songeasy.android.score.ClefSign.ClefName;
import de.songeasy.android.score.Measure.LinePosition;
import de.songeasy.android.score.Measure.MeasureAttribute;
import de.songeasy.android.score.Measure.RepeatType;
import de.songeasy.android.score.TitleBlock.DisplayMode;

/**
 * A Song holding all the measures. Provides functions for manipulating the data
 * of score.
 *
 * @author krein
 */
@Root
public class Song extends ScoreElement {

    // string with class name for logging
    private final String TAG = this.getClass().getSimpleName();

    /**
     * predefined layout types of song
     */
    public enum Layout {
        VOICE, PIANO, VOICE_PIANO, PIANO_BASS, VOICE_PIANO_BASS
    }

    ;

    /**
     * name and composer of song
     */
    // @Element(name = "titleblock", required = false)
    private TitleBlock titleBlock;

    /**
     * version of song
     */
    @Attribute(name = "version")
    private String version;

    /**
     * size of song
     */
    @Attribute(required = false)
    private int numberOfMeasures = 0;

    /**
     * List with measures of this song
     */
    @ElementList
    private ArrayList<Measure> measures;

    /**
     * List with additional text blocks, placed below score
     */
    @ElementList(required = false)
    private ArrayList<TextBlock> textBlocks;

    /**
     * List with page breaks in song
     */
    private ArrayList<PageBreak> pageBreaks;

    /**
     * upper left corner of first measure
     */
    private Point startOfMeasures;

    /**
     * lowest point in y-direction of song
     */
    private int lowerEdge;

    private int endOfMeasures;

    /**
     * current selected ScoreElement
     */
    private ScoreElement selectedElement;

    /**
     * measure where selected element lies in, if not measure itself
     */
    Measure measureOfSelectedElement;

    /**
     * ScoreElement saved for later insertion
     */
    private ScoreElement elementClipBoard;

    /**
     * flag for changed state
     */
    private boolean isChanged;

    /**
     * index of first measure to draw
     */
    private int indexFirstMeasure;

    /**
     * index of last measure to draw
     */
    private int indexLastMeasure;


    private boolean exportIsActive;

    // +++++++ getters and setters +++++++
    // +++++++++++++++++++++++++++++++++++

    /**
     * Get measures of song
     *
     * @return array with measures
     */
    public ArrayList<Measure> getMeasures() {
        return measures;
    }


    /**
     * Find measure at given position.
     *
     * @param position position to look at
     * @return found measure, null otherwise
     */
    public Measure getMeasureAtPosition(Point position) {

        // find measure at position to add new element
        for (Measure measure : measures) {

            if (measure.isIn(position) == true) {

                return measure;
            }
        }

        // no measure found at position
        return null;
    }


    /**
     * Get TitleBlock of song
     *
     * @return the TitleBlock
     */
    @Element(name = "titleblock")
    public TitleBlock getTitleBlock() {
        return titleBlock;
    }


    /**
     * Set new TitleBlock for song
     *
     * @param titleBlock new TitleBlock to set
     */
    @Element(name = "titleblock")
    public void setTitleBlock(TitleBlock titleBlock) {
        this.titleBlock = titleBlock;
    }


    /**
     * Get element currently selected
     *
     * @return element selected by user
     */
    public ScoreElement getSelectedElement() {
        return selectedElement;
    }


    /**
     * If any element of song is selected
     *
     * @return true is element is selected
     */
    public boolean isElementSelected() {
        if (selectedElement != null)
            return true;
        else
            return false;
    }


    /**
     * Set file version of song.
     *
     * @param version String with version number, e.g. "1.0.1"
     */
    public void setVersion(String version) {
        this.version = version;
    }


    /**
     * Get file version number of song
     *
     * @return String with version number, e.g. "1.0.1"
     */
    public String getVersion() {
        return version;
    }


    public boolean isChanged() {
        return isChanged;
    }


    public void setChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }


    /**
     * Get number of measures song consists of
     *
     * @return the numberOfMeasures
     */
    public int getNumberOfMeasures() {
        return numberOfMeasures;
    }


    /**
     * Returns aspect ration of song
     *
     * @return
     */
    public float getAspectRatio() {

        if (getHeight() > getWidth())
            return (float) getHeight() / getWidth();
        else
            return (float) getWidth() / getHeight();
    }


    /**
     * Get the number of pages this song consists of
     *
     * @return the number of pages, 1 minimum
     */
    public int getNumberOfPages() {
        return pageBreaks.size() + 1;
    }


    public boolean isExportIsActive() {
        return exportIsActive;
    }

    public void setExportIsActive(boolean exportIsActive) {
        this.exportIsActive = exportIsActive;
    }

    /**
     * Recalculate number of measures for displaying progress bar.
     */
    public void updateNumberOfMeasures() {
        this.numberOfMeasures = measures.size();
    }


    /**
     * Fill list of page breaks after loading of song.
     */
    public void updatePageBreaks() {

        for (Measure measure : measures) {
            if (measure.getAttributes().contains(MeasureAttribute.PAGE_BREAK))
                pageBreaks.add(new PageBreak(measure));
        }
    }


    /**
     * Constructor for creation of song from file. Creates list for measures to
     * put in.
     *
     * @param titleBlock TitleBlock with song title and name of composer
     */
    public Song(@Element(name = "titleblock") TitleBlock titleBlock,
                @Attribute(name = "version") String version) {

        this.titleBlock = titleBlock;

        // create new array for text blocks
        textBlocks = new ArrayList<TextBlock>();

        startOfMeasures = new Point(getOrigin());

        // create array for song elements
        measures = new ArrayList<Measure>();

        pageBreaks = new ArrayList<PageBreak>();

        setVersion(version);

        // updatePageBreaks();

        isChanged = false;
    }


    /**
     * Constructor with more parameters for dynamic creation of song. Creates
     * list of measures with two measures containing the number of staffs as
     * defined in layout parameter.
     *
     * @param name     Name of Song
     * @param composer Name of composer
     * @param layout   One of predefined layouts for song
     * @param key      the key signature of the song
     * @param time     the time signature the song starts with
     * @param date     date to be displayed in song
     * @param version  file version of song
     */

    public Song(String name, String composer, Layout layout, KeySignature key,
                TimeSignature time, String date, String version) {

        setVersion(version);

        // create new title block
        titleBlock = new TitleBlock(name, composer, date);

        startOfMeasures = new Point(getOrigin());

        // create array for song elements
        measures = new ArrayList<Measure>();

        textBlocks = new ArrayList<TextBlock>();

        pageBreaks = new ArrayList<PageBreak>();

        // add first measure
        Measure firstMeasure = new Measure();
        // firstMeasure.addAttribute(MeasureAttribute.FIRST_IN_LINE);
        addMeasure(firstMeasure);

        // add staves as according to layout parameter
        switch (layout) {

            case VOICE:
                firstMeasure.addStaff(new Staff("Voice", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                break;

            case PIANO:
                firstMeasure.addStaff(new Staff("PianoLeft", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("PianoRight", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.BASS),
                        new TimeSignature(time)));
                break;

            case VOICE_PIANO:
                firstMeasure.addStaff(new Staff("Voice", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("PianoLeft", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("PianoRight", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.BASS),
                        new TimeSignature(time)));

                break;

            case PIANO_BASS:

                firstMeasure.addStaff(new Staff("PianoLeft", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("PianoRight", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.BASS),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("Bass", 0, 1, new KeySignature(
                        key), new ClefSign(ClefName.BASS), new TimeSignature(
                        time)));

                break;

            case VOICE_PIANO_BASS:
                firstMeasure.addStaff(new Staff("Voice", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("PianoLeft", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.VIOLIN),
                        new TimeSignature(time)));
                firstMeasure.addStaff(new Staff("PianoRight", 0, 1,
                        new KeySignature(key), new ClefSign(ClefName.BASS),
                        new TimeSignature(time)));
                addStaff(new Staff("Bass", 0, 1, new KeySignature(key),
                        new ClefSign(ClefName.BASS), new TimeSignature(time)));
                break;

            default:
                break;
        }

        // add one measure more to have one with LAST_IN_LINE
        Measure lastMeasure = new Measure();
        lastMeasure.addAttribute(MeasureAttribute.LINE_BREAK);
        addMeasure(lastMeasure);

        isChanged = false;
    }


    /**
     * Deletes previously selected ScoreElement.
     */
    public void deleteSelectedElement() {

        if (selectedElement != null) {
            if (selectedElement.getClass() == Measure.class) {

                deleteMeasure((Measure) selectedElement);
            } else {
                if (measureOfSelectedElement != null) {
                    measureOfSelectedElement.removeElement(selectedElement);
                } else {
                    // TextBlock is the only ScoreElement not owned by Measure
                    if (selectedElement.getClass() == TextBlock.class) {
                        textBlocks.remove((TextBlock) selectedElement);
                    }
                }
            }
            selectedElement = null;
            measureOfSelectedElement = null;
        }
    }


    /**
     * Copy selected element into clipboard. Remove selected element from Song
     * and add it to clipboard.
     */
    public void cutOutSelectedElement() {

        elementClipBoard = selectedElement;

        deleteSelectedElement();

        elementClipBoard.setSelected(false);
    }


    public void copySelectedElement() {

        elementClipBoard = selectedElement;
    }


    public void hideSelectedElement() {
        selectedElement.setVisible(false);
    }


    /**
     * Paste ScoreElement from clipboard at position.
     *
     * @param position position to paste element at
     * @return true on success, false if adding at requested position fails
     */
    // TODO: better use exception handling than return true/false ?
    public boolean pasteElementAtPosition(Point position) {

        boolean returnValue = false;

        if (elementClipBoard == null)
            return false;

        if (elementClipBoard instanceof AnnotationElement)
            returnValue = addAnnotationAtPosition(
                    (AnnotationElement) elementClipBoard, position);

        if (elementClipBoard instanceof DirectionElement)
            returnValue = addDirectionAtPosition(
                    (DirectionElement) elementClipBoard, position);

        if (elementClipBoard.getClass() == Note.class)
            returnValue = addNoteAtPosition((Note) elementClipBoard, position);

        if (elementClipBoard.getClass() == TextBlock.class)
            returnValue = swapTextBlocks((TextBlock) elementClipBoard, position);

        if (returnValue == true) {
            elementClipBoard.setSelected(false);
            elementClipBoard = null;
        }

        // return false if adding at desired position fails
        return returnValue;
    }


    public void moveSelectedElement(Measure.Direction direction) {

        // only elements inside a measure are supported currently
        if (selectedElement == null || measureOfSelectedElement == null)
            return;

        if (selectedElement.getClass() == Note.class) {
            measureOfSelectedElement.moveNoteInDirection(
                    (Note) selectedElement, direction);
        }

        if (selectedElement.getClass() == Rest.class) {
            measureOfSelectedElement.moveRestInDirection(
                    (Rest) selectedElement, direction);
        }

        if (selectedElement instanceof AnnotationElement) {
            measureOfSelectedElement.moveAnnotationInDirection(
                    (AnnotationElement) selectedElement, direction);
        }

        if (selectedElement instanceof DirectionElement)
            measureOfSelectedElement.moveDirectionInDirection(
                    (DirectionElement) selectedElement, direction);
    }


    /**
     * Selects next element of the same type in direction.
     *
     * @param direction Direction to look for the next element.
     * @return true if next element was found and selected
     */
    public boolean selectNextElementInDirection(Measure.Direction direction) {

        // only elements inside a measure are supported currently
        if (selectedElement == null)
            return false;

        if (selectedElement.getClass() == Note.class) {

            // try to find note in measure of selected note
            Note nextNote = measureOfSelectedElement.findNoteInDirection(
                    (Note) selectedElement, direction);

            Measure nextMeasure = measureOfSelectedElement;
            int index = measures.indexOf(nextMeasure);

            // try to find note in all existing measures
            while (nextNote == null && nextMeasure != null) {
                switch (direction) {
                    case LEFT:
                        index--;
                        try {
                            nextMeasure = measures.get(index);
                        } catch (IndexOutOfBoundsException e) {
                            nextMeasure = null;
                        }
                        // try to find note in next measure on the left
                        if (nextMeasure != null)
                            nextNote = nextMeasure.findNoteInDirection(
                                    (Note) selectedElement, direction);
                        break;

                    case RIGHT:
                        index++;
                        try {
                            nextMeasure = measures.get(index);
                        } catch (IndexOutOfBoundsException e) {
                            nextMeasure = null;
                        }
                        // try to find note in next measure on the right
                        if (nextMeasure != null)
                            nextNote = nextMeasure.findNoteInDirection(
                                    (Note) selectedElement, direction);

                        break;

                    default:
                        break;
                }
            }

            if (nextNote != null) {
                measureOfSelectedElement = nextMeasure;
                selectElement(nextNote, measureOfSelectedElement);
                return true;
            }
        }

        if (selectedElement.getClass() == Rest.class) {
            // try to find note in measure of selected note
            Rest nextRest = measureOfSelectedElement.findRestInDirection(
                    (Rest) selectedElement, direction);

            Measure nextMeasure = measureOfSelectedElement;
            int index = measures.indexOf(nextMeasure);

            // try to find note in all existing measures
            while (nextRest == null && nextMeasure != null) {
                switch (direction) {
                    case LEFT:
                        index--;
                        try {
                            nextMeasure = measures.get(index);
                        } catch (IndexOutOfBoundsException e) {
                            nextMeasure = null;
                        }
                        // try to find note in next measure on the left
                        if (nextMeasure != null)
                            nextRest = nextMeasure.findRestInDirection(
                                    (Rest) selectedElement, direction);
                        break;

                    case RIGHT:
                        index++;
                        try {
                            nextMeasure = measures.get(index);
                        } catch (IndexOutOfBoundsException e) {
                            nextMeasure = null;
                        }
                        // try to find note in next measure on the right
                        if (nextMeasure != null)
                            nextRest = nextMeasure.findRestInDirection(
                                    (Rest) selectedElement, direction);
                        break;

                    default:
                        break;
                }
            }

            if (nextRest != null) {
                measureOfSelectedElement = nextMeasure;
                selectElement(nextRest, measureOfSelectedElement);
                return true;
            }
        }

        if (selectedElement instanceof AnnotationElement) {
            AnnotationElement nextAnnotationElement = measureOfSelectedElement
                    .findAnnotationInDirection(
                            (AnnotationElement) selectedElement, direction);

            Measure nextMeasure = measureOfSelectedElement;
            int index = measures.indexOf(nextMeasure);

            // try to find annotation in all existing measures
            while (nextAnnotationElement == null && nextMeasure != null) {
                switch (direction) {
                    case LEFT:
                        index--;
                        try {
                            nextMeasure = measures.get(index);
                        } catch (IndexOutOfBoundsException e) {
                            nextMeasure = null;
                        }
                        // try to find note in next measure on the left
                        if (nextMeasure != null)
                            nextAnnotationElement = nextMeasure.findAnnotationInDirection(
                                    (AnnotationElement) selectedElement, direction);
                        break;

                    case RIGHT:
                        index++;
                        try {
                            nextMeasure = measures.get(index);
                        } catch (IndexOutOfBoundsException e) {
                            nextMeasure = null;
                        }
                        // try to find note in next measure on the right
                        if (nextMeasure != null)
                            nextAnnotationElement = nextMeasure.findAnnotationInDirection(
                                    (AnnotationElement) selectedElement, direction);
                        break;

                    default:
                        break;
                }
            }

            if (nextAnnotationElement != null) {
                measureOfSelectedElement = nextMeasure;
                selectElement(nextAnnotationElement, measureOfSelectedElement);
                return true;
            }
        }

        if (selectedElement.getClass() == Measure.class) {
            int index = getMeasures().indexOf((Measure) selectedElement);
            switch (direction) {
                case LEFT:
                    if (index - 1 >= 0)
                        selectElement(getMeasures().get(index - 1), null);
                    break;

                case RIGHT:
                    if (index + 1 < getMeasures().size())
                        selectElement(getMeasures().get(index + 1), null);
                    break;

                default:
                    break;
            }
            return true;
        }

        return false;
    }


    /**
     * Add new Staff to Song. Staves are ordered in the sequence they are added,
     * first gets index 0. Add staff to each measure, as measures owns the
     * staves.
     *
     * @param staff Staff to add to song
     */
    public void addStaff(Staff staff) {

        // tell each measure the initial properties of this new staff
        for (Measure measure : measures) {

            measure.addStaff(staff);
        }
    }


    /**
     * Add new measure to end of song. Copys all staves of previous measure if
     * exists.
     *
     * @param measure Measure to add.
     */
    public void addMeasure(Measure measure) {

        int size = measures.size();

        if (size > 0)
            // copy staves from previous measure
            for (Staff staff : measures.get(size - 1).getStaves()) {
                measure.addStaff(new Staff(staff));
            }

        measures.add(measure);

        updateNumberOfMeasures();
    }


    /**
     * Delete measure from song. Try to adjust attributes of surrounding
     * measures so that start and beginning of a line are kept.
     *
     * @param measureToDelete
     */
    public void deleteMeasure(Measure measureToDelete) {

        int index = measures.indexOf(measureToDelete);

        if (index < 0)
            return;

        // move possible line break to previous measure
        if (measureToDelete.getAttributes().contains(
                MeasureAttribute.LINE_BREAK)) {

            if ((index - 1) > 0)
                measures.get(index - 1).addAttribute(
                        MeasureAttribute.LINE_BREAK);

        }

        // delete measure from array
        measures.remove(measureToDelete);

        updateNumberOfMeasures();
    }


    /**
     * Inserts a new measure after another.
     *
     * @param newMeasure      measure to add
     * @param previousMeasure measure the new measure is added after
     */
    private void insertMeasure(Measure newMeasure, Measure previousMeasure) {

        // copy staves from previous measure
        for (Staff staff : previousMeasure.getStaves()) {
            newMeasure.addStaff(new Staff(staff));
        }

        // add measure after previous one
        int index = measures.indexOf(previousMeasure);
        measures.add(index + 1, newMeasure);

        updateNumberOfMeasures();
    }


    /**
     * Insert a new line attribute to measure.
     *
     * @param measure Measure to start in a new line after.
     */
    public void insertNewLine(Measure measure) {

        measure.addAttribute(MeasureAttribute.LINE_BREAK);
    }


    /**
     * Remove new line attribute from measure.
     *
     * @param measure Measure to remove new line attribute from
     */
    public void removeNewLine(Measure measure) {

        measure.removeAttribute(MeasureAttribute.LINE_BREAK);

        // remove possible page break (only allowed in last measure of line)
        measure.removeAttribute(MeasureAttribute.PAGE_BREAK);
    }


    /**
     * Insert a page break attribute to measure
     *
     * @param measure Measure to insert a page break after
     */
    public void addPageBreak(Measure measure) {

        // no pagebreak on last measure
        if (measures.indexOf(measure) >= measures.size() - 1) return;

        // only page breaks on last measure of line
        if (!measure.getAttributes().contains(MeasureAttribute.LINE_BREAK)) return;

        measure.addAttribute(MeasureAttribute.PAGE_BREAK);
        pageBreaks.add(new PageBreak(measure));
        Log.d(TAG, String.format("page break added at measure %d", measures.indexOf(measure)));
    }


    /**
     * Remove page break from measure
     *
     * @param measure
     */
    public void removePageBreak(Measure measure) {

        measure.removeAttribute(MeasureAttribute.PAGE_BREAK);

        // find page break associated with measure
        for (Iterator<PageBreak> pBreakIt = pageBreaks.iterator(); pBreakIt
                .hasNext(); ) {

            PageBreak pBreak = pBreakIt.next();

            // remove page break
            if (pBreak.getMeasure().equals(measure))
                pBreakIt.remove();
        }
        Log.d(TAG, String.format("page break removed at measure %d", measures.indexOf(measure)));
    }


    /**
     * Add new TextBlock to song
     *
     * @param newTextBlock TextBlock to add
     */
    public void addTextBlock(TextBlock newTextBlock) {

        textBlocks.add(newTextBlock);
    }


    /**
     * Add new key for song from given measure. Set new key for all measures
     * starting from given one.
     *
     * @param newKey       new KeySignature for song
     * @param startMeasure measure to change key from
     */
    public void addKeyChange(Measure startMeasure, KeySignature newKey) {

        int index = measures.indexOf(startMeasure);

        if (index < 0)
            return;

        // first measure gets a keychange flag set to display the new key
        // signature
        measures.get(index).setKeyChange(newKey);
        index++;

        // set new key signature to all measures from this one
        for (; index < measures.size(); index++) {

            measures.get(index).setKey(newKey);
        }
    }


    /**
     * Remove TextBlock from song
     *
     * @param textBlockToDelete TextBlock to remove from song
     */
    public void deleteTextBlock(TextBlock textBlockToDelete) {

        textBlocks.remove(textBlockToDelete);
    }


    @Override
    public void drawSelf(Canvas canvas) {

        // drawDebugRect(canvas);
//        Log.d(TAG, String.format("draw  from: %d/%d  to: %d/%d", getOrigin().x, getOrigin().y
//                , getOrigin().x + getWidth(), getOrigin().y + getHeight()));

        // draw title block
        titleBlock.drawSelf(canvas);

        // draw all measures with content
        for (Measure measure : measures) {
            measure.drawSelf(canvas);

            if (measure.getAttributes().contains(MeasureAttribute.PAGE_BREAK)) {
                // find page break associated with measure
                for (PageBreak pbreak : pageBreaks) {
                    if (pbreak.getMeasure().equals(measure))
                        pbreak.drawSelf(canvas);
                }
            }
        }

        // draw textblocks
        for (TextBlock textBlock : textBlocks) {
            textBlock.drawSelf(canvas);
        }

        // drawDebugRect(canvas);
    }


    /**
     * Draw selected page on canvas.
     *
     * @param canvas Canvas to draw on
     * @param page   page to draw
     */
    public void drawSelf(Object canvas, int page) {
        // drawDebugRect(canvas);

        // draw title block
        titleBlock.drawSelf((Canvas) canvas);

        // draw all measures with content
        for (int i = indexFirstMeasure; i <= indexLastMeasure; i++) {

            measures.get(i).drawSelf((Canvas) canvas);
        }

        // draw additional text on last page only
        if (page == pageBreaks.size())
            // draw textblocks
            for (TextBlock textBlock : textBlocks) {
                textBlock.drawSelf((Canvas) canvas);
            }
    }


    /**
     * Set position and size of song elements. Recalculates staff height, in
     * case that onLayout gets called with different parameters than onMeasure.
     * Horizontal and vertical distances from ScoreView's margins must already
     * be taken in account.
     *
     * @param origin      start position, upper left corner of element
     * @param lineSpacing spacing between staff lines
     * @param width       width of song sheet
     * @param height      height of song sheet
     */
    @Override
    public void setLayout(Point origin, int lineSpacing, int width, int height) {

        super.setLayout(origin, lineSpacing, width, height);

        // Log.d(TAG, String.format(
        // "width: %d  height: %d  spacing: %d", width, height,
        // lineSpacing));

        startOfMeasures.x = origin.x;
        startOfMeasures.y = origin.y;

        // set layout of title block
        // title block height is that of one and a half staves
        int titleBlockHeight = (int) (Staff.getStavesHeight() * 1.5f);

        titleBlock.setPagesCurrent(1);
        titleBlock.setPagesTotal(pageBreaks.size() + 1);

        Point titleStart = new Point(origin);
        titleStart.y += lineSpacing
                * PageProperties.TITLEBLOCK_UPPER_MARGIN_TO_STAFF_LINE_SPACING_FACTOR;

        titleBlock.setLayout(titleStart, lineSpacing, width, titleBlockHeight,
                DisplayMode.FULL);

        // let measures start below title block + staff height
        startOfMeasures.y += titleBlock.getHeight()
                + (int) (Staff.getStavesHeight() * 1.5f);

        // Log.d(TAG, String.format("titleStartX: %d", titleStart.x));

        // compute position of measures
        redistributeMeasures(0, measures.size() - 1);

        redistributeTextBlocks();

        // set height of song after song elements are distributed
        setHeight(lowerEdge - getOrigin().y);
    }


    /**
     * Set position and size of song elements for a given page.
     *
     * @param origin      start position of song
     * @param lineSpacing space between note lines
     * @param width       available width for song
     * @param height      available height of song
     * @param page        page of song to layout
     */
    public void setLayout(Point origin, int lineSpacing, int width, int height, int page) {

        // save parameters
        super.setLayout(origin, lineSpacing, width, height);
        startOfMeasures.x = origin.x;
        startOfMeasures.y = origin.y;

        // set layout of title block
        // default title block height is that of one and a half staves
        int titleBlockHeight = (int) (Staff.getStavesHeight() * 1.5f);

        titleBlock.setPagesCurrent(page + 1);
        titleBlock.setPagesTotal(pageBreaks.size() + 1);

        Point titleStart = new Point(origin);
        titleStart.y += lineSpacing
                * PageProperties.TITLEBLOCK_UPPER_MARGIN_TO_STAFF_LINE_SPACING_FACTOR;

        if (page == 0)
            titleBlock.setLayout(titleStart, lineSpacing, width,
                    titleBlockHeight, DisplayMode.FULL);
        else
            titleBlock.setLayout(titleStart, lineSpacing, width,
                    titleBlockHeight, DisplayMode.PAGE_NUMBERS_ONLY);

        // let measures start below title block + staff height
        startOfMeasures.y += titleBlock.getHeight()
                + (int) (Staff.getStavesHeight() * 1.5f);

        // Log.d(TAG, String.format("titleStartX: %d", titleStart.x));

        // find first measure of selected page
        Measure firstMeasure = null;

        if (page == 0) {
            // on first page start with first measure
            firstMeasure = measures.get(0);
        } else {
            // for later pages search for measures before page breaks
            int pageCounter = 1;
            for (Measure actMeasure : measures) {
                if (actMeasure.getAttributes().contains(MeasureAttribute.PAGE_BREAK)) {
                    if (page == pageCounter) {
                        int indexOfLastPb = measures.indexOf(actMeasure);
                        try {
                            // get measure after page break
                            firstMeasure = measures.get(indexOfLastPb + 1);

                        } catch (IndexOutOfBoundsException e) {
                            // if no more measures after, measure with page break is last
                            // measure
                            firstMeasure = measures.get(indexOfLastPb);
                        }
                        break;
                    }
                    pageCounter++;
                }
            }
        }

        // find last measure of selected page, default is last one
        Measure lastMeasure = measures.get(measures.size() - 1);

        // search for measures with page breaks
        int pageCounter = 0;
        for (Measure actMeasure : measures) {
            if (actMeasure.getAttributes().contains(MeasureAttribute.PAGE_BREAK)) {
                if (page == pageCounter) {
                    lastMeasure = actMeasure;
                    break;
                }
                pageCounter++;
            }
        }

        Log.d(TAG, String.format("page: %d   firstMeasure: %d   lastMeasure:  %d",
                page, measures.indexOf(firstMeasure), measures.indexOf(lastMeasure)));

        // compute position of measures
        redistributeMeasures(measures.indexOf(firstMeasure),
                measures.indexOf(lastMeasure));

        // text blocks on end of last page
        if (page == pageBreaks.size())
            redistributeTextBlocks();

        // set height of song after song elements are distributed
        setHeight(lowerEdge - getOrigin().y);
    }


    /**
     * Recompute layout of measures. For multiple pages, from and to measure are
     * not set to first and last measure of song. Set each measure's link to
     * next measure. Layout lines for indicating page breaks. Layout starts in
     * first line on top of page.
     *
     * @param fromMeasure index of first measure to layout
     * @param toMeasure   index of last measure to layout
     */
    private void redistributeMeasures(int fromMeasure, int toMeasure) {

        if (measures.size() < 1)
            return;

        if ((toMeasure - fromMeasure) < 1)
            return;

        // remind indexes of measures to show
        indexFirstMeasure = fromMeasure;
        indexLastMeasure = toMeasure;

        Point start = new Point();
        int measureWidth = 0;
        int measureHeight = 0;
        int signatureWidth = 0;
        int idxFirstInLine = 0;
        int idxLastInLine = 0;
        int line = 1;
        int measuresInLine = 0;
        // set measure counter to first measure
        int idxMeasure = fromMeasure;

        // compute height of measure (assuming each measure has the same number
        // of staves)
        measureHeight = measures.get(0).getStaves().size()
                * Staff.getStavesHeight()
                * PageProperties.STAVES_DISTANCE_FACTOR
                + PageProperties.INTER_SYSTEM_EXTRA_DISTANCE_FACTOR * staffLineSpacing;

        // compute width of the signature signs, added to width of every first
        // measure in line
        // TODO: this is ugly and only temporarily till dynamic measure width
        // comes
        signatureWidth = measures.get(0).getStaves().get(0).getSignatureWidht();

        // layout measures line by line
        while (idxMeasure <= toMeasure) {

            // first measure is always the first in line
            idxFirstInLine = idxMeasure;

            // wait for line break;
            while (!measures.get(idxMeasure).getAttributes()
                    .contains(MeasureAttribute.LINE_BREAK)
                    && idxMeasure < toMeasure)
                idxMeasure++;

            idxLastInLine = idxMeasure;

            // compute measures in line
            measuresInLine = idxLastInLine - idxFirstInLine + 1;

            // should not happen
            if (measuresInLine < 1) {
                idxMeasure++;
                continue;
            }

            measureWidth = Math.round((float) (getWidth() - signatureWidth)
                    / measuresInLine);

            // Log.d(TAG,
            // String.format(
            // "songwidth: %d  measuresInLine: %d   measureWidth: %d   signatureWidth: %d",
            // getWidth(), measuresInLine, measureWidth,
            // signatureWidth));

            // upper start of measures in line
            start.y = startOfMeasures.y + measureHeight * (line - 1);

            // tell each measure in line its layout properties
            for (int n = idxFirstInLine; n <= idxLastInLine; n++) {

                // left start of measure
                start.x = startOfMeasures.x + signatureWidth + measureWidth
                        * (n - idxFirstInLine);

                // reset line position parameter
                measures.get(n).getLinePosition().clear();

                // layout measure, take position in line into account
                if (idxFirstInLine == idxLastInLine) {
                    // only measure in line
                    start.x = startOfMeasures.x;
                    measures.get(n).addLinePosition(LinePosition.FIRST_IN_LINE);
                    measures.get(n).addLinePosition(LinePosition.LAST_IN_LINE);
                    measures.get(n).setLayout(start, staffLineSpacing,
                            measureWidth + signatureWidth, measureHeight);

                } else if (n == idxFirstInLine) {
                    // first measure in line
                    start.x = startOfMeasures.x;
                    measures.get(n).addLinePosition(LinePosition.FIRST_IN_LINE);
                    measures.get(n).setLayout(start, staffLineSpacing,
                            measureWidth + signatureWidth, measureHeight);

                } else if (n == idxLastInLine) {
                    // last measure in line
                    measures.get(n).addLinePosition(LinePosition.LAST_IN_LINE);
                    measures.get(n).setLayout(start, staffLineSpacing,
                            measureWidth, measureHeight);
                } else {
                    // measure in the middle of line
                    measures.get(n).setLayout(start, staffLineSpacing,
                            measureWidth, measureHeight);
                }

                // set pointer to next measure in chain
                if ((n + 1) < measures.size())
                    measures.get(n).setNextMeasure(measures.get(n + 1));
                else
                    // last measure of song gets null pointer
                    measures.get(n).setNextMeasure(null);

                // layout pagebreaks (dotted line between measures)
                if (measures.get(idxLastInLine).getAttributes()
                        .contains(MeasureAttribute.PAGE_BREAK)) {
                    for (PageBreak pbreak : pageBreaks) {
                        if (pbreak.getMeasure().equals(
                                measures.get(idxLastInLine)))
                            // layout dotted line below last measure in line
                            pbreak.setLayout(new Point(getOrigin().x, measures
                                            .get(idxLastInLine).getOrigin().y
                                            + measureHeight), staffLineSpacing,
                                    getWidth(), getHeight());
                    }
                }
                // Log.d(TAG, String.format(
                // "Measure %d at: %d,%d", index, start.x, start.y));
            }

            // next line of measures
            line++;
            // next measure to layout
            idxMeasure++;
        }

        // set lowest y of score
        lowerEdge = measures.get(idxLastInLine).getOrigin().y;
        lowerEdge += measures.get(idxLastInLine).getHeight();

        // remember lowest edge of measures
        endOfMeasures = lowerEdge;
    }


    /**
     * Layout text blocks below last measure. From left to right, two blocks in
     * a row
     */
    private void redistributeTextBlocks() {

        // count text blocks for placing on a new column two
        int blocks = 0;

        // let text blocks start on left below last measure
        Point textBlockStart = new Point(
                startOfMeasures.x,
                lowerEdge
                        + (int) (staffLineSpacing * PageProperties.TEXTBLOCK_UPPER_MARGIN_TO_STAFF_LINE_SPACING_FACTOR));

        for (TextBlock textBlock : textBlocks) {

            textBlock.setLayout(textBlockStart, staffLineSpacing);

            // recalculate lower edge of song
            if (textBlock.getOrigin().y + textBlock.getHeight() > lowerEdge)
                lowerEdge = textBlock.getOrigin().y + textBlock.getHeight();

            // move odd blocks to right side of score
            textBlockStart.x += width / 2;

            blocks++;

            // move even blocks on left side of score
            if ((blocks % 2) == 0) {
                textBlockStart.y = lowerEdge
                        + (int) (staffLineSpacing * PageProperties.TEXTBLOCK_SPACING_TO_STAFF_LINE_SPACING_FACTOR);
                textBlockStart.x = startOfMeasures.x;
            }
        }
    }


    /**
     * Select ScoreElement in measure for editing. Deselect previously selected
     * element and selects new one. Sets selected flags of elements for update
     * of visual highlighting. Remember element and measure ,if not not a
     * measure itself, in global variables.
     *
     * @param element ScoreElement to mark as selected
     * @param measure Measure element belongs to, null if element is measure
     */
    private void selectElement(ScoreElement element, Measure measure) {

        // check if something was selected previously
        if (selectedElement != null) {
            // uncheck previously selected only if not the same
            if (selectedElement.equals(element) == false) {
                selectedElement.setSelected(false);
                selectedElement = element;
                selectedElement.setSelected(true);
                measureOfSelectedElement = measure;

            }
        } else {
            // if nothing was selected previously
            selectedElement = element;
            selectedElement.setSelected(true);
            measureOfSelectedElement = measure;
        }
    }


    /**
     * Deselect previously selected ScoreElement.
     */
    public void deselectElement() {

        if (selectedElement != null) {
            selectedElement.setSelected(false);
            selectedElement = null;
            measureOfSelectedElement = null;
        }
    }


    /**
     * Select ScoreElement found at given coordinate. If ScoreElement inside
     * measure is found, element and measure will be remebered. Element will be
     * selected (highlighted). If only measure will be found, measure will be
     * selected. If TextBlock will be found, TextBlock will be selected. Nothing
     * otherwise.
     *
     * @param position position to look at for element
     * @return true, if element was found at position
     */
    public boolean selectElementAtPosition(Point position) {

        // look for measure and element inside measure
        for (Measure measure : measures) {

            // find measure at position
            if (measure.isIn(position) == true) {

                // measure is the selected element
                selectElement(measure, null);

                // try to select element inside measure at position
                if (measure.selectElementAtPosition(position)) {

                    selectElement(measure.getSelectedElement(), measure);
                }

                return true;
            }
        }

        // look for textblocks
        for (TextBlock textBlock : textBlocks) {

            if (textBlock.isIn(position)) {
                selectElement(textBlock, null);
                return true;
            }
        }
        return false;
    }


    /**
     * Select measure found at given position. Select (highlight) and remember
     * measure if found.
     *
     * @param position Pposition to look for measure
     * @return true if measure was found at position
     */
    public boolean selectMeasureAtPosition(Point position) {

        for (Measure measure : measures) {
            // find measure at position
            if (measure.isIn(position) == true) {

                // measure is the selected element
                selectElement(measure, null);
                return true;
            }
        }
        return false;
    }


    /**
     * Add annotation at given position to measure.
     *
     * @param element  AnnotationElement to add
     * @param position Position to add element at
     */
    public boolean addAnnotationAtPosition(AnnotationElement element,
                                           Point position) {

        Measure measureForNewElement = getMeasureAtPosition(position);

        if (measureForNewElement == null)
            return false;

        element.setRelativePosition((double) (position.x - measureForNewElement
                .getOrigin().x) / measureForNewElement.getWidth());

        element.setOrigin(position);

        selectElement(element, measureForNewElement);

        measureForNewElement.addAnnotation(element);

        return true;
    }


    /**
     * Add DirectionElement at given position to measure.
     *
     * @param element  DirectionElement to add
     * @param position Position to add repeat at
     */
    public boolean addDirectionAtPosition(DirectionElement element,
                                          Point position) {

        Measure measureForNewElement = getMeasureAtPosition(position);

        if (measureForNewElement == null)
            return false;

        element.setRelativePosition((double) (position.x - measureForNewElement
                .getOrigin().x) / measureForNewElement.getWidth());

        selectElement(element, measureForNewElement);

        measureForNewElement.addDirections(element);

        return true;
    }


    /**
     * Swap specified TextBlock with another one at given position
     *
     * @param newTextBlock first TextBlock to swap
     * @param position     position of second Textblock to swap
     * @return true if swapping was done
     */
    public boolean swapTextBlocks(TextBlock newTextBlock, Point position) {

        // user can add TextBlock below measures only
        if (position.y < endOfMeasures)
            return false;

        int index1 = textBlocks.indexOf(newTextBlock);
        // given TextBlock not found in list
        if (index1 < 0)
            return false;

        // check for overlapping TextBlock at position
        for (TextBlock textBlock : textBlocks) {

            if (textBlock.overlaps(newTextBlock, position) == true) {

                try {
                    Collections.swap(textBlocks, index1,
                            textBlocks.indexOf(textBlock));
                    return true;
                } catch (IndexOutOfBoundsException e) {
                    return false;
                }
            }
        }

        // no TextBlock at position found
        return false;
    }


    /**
     * Add note at given position.
     *
     * @param note     Note to add
     * @param position position at where to add
     */
    public boolean addNoteAtPosition(Note note, Point position) {

        Measure measureForNewElement = getMeasureAtPosition(position);

        if (measureForNewElement == null)
            return false;

        selectElement(note, measureForNewElement);

        measureForNewElement.addNoteAtPosition(position, note);

        return true;
    }


    /**
     * Add rest at given position.
     *
     * @param rest     Rest to add
     * @param position position at where to add
     */
    public void addRestAtPosition(Rest rest, Point position) {

        Measure measureForNewElement = getMeasureAtPosition(position);

        if (measureForNewElement == null)
            return;

        selectElement(rest, measureForNewElement);

        measureForNewElement.addRestAtPosition(position, rest);
    }


    /**
     * Add measure after the one found at position.
     *
     * @param count       number of measures to add
     * @param newLine     if the last measure to add starts a new line
     * @param repeatStart if the first measure to add has a repeat start mark
     * @param repeatStop  if the last measure to add has a repeat end mark
     * @param ending      if the last measure to add has a ending mark
     * @param position    the position of the measure to add new measures after
     */
    public void addMeasureAtPosition(int count, boolean newLine,
                                     boolean repeatStart, boolean repeatStop, boolean ending,
                                     Point position) {

        if (count < 1)
            return;

        Measure measureForNewElement = getMeasureAtPosition(position);

        if (measureForNewElement == null)
            return;

        // treat first measure specially
        Measure firstMeasure = new Measure();

        // add repeat start in first measure if requested
        if (repeatStart == true)
            firstMeasure.setRepeat(RepeatType.START);

        // add new measure after selected one
        insertMeasure(firstMeasure, measureForNewElement);

        // one measure was added
        count--;

        // add rest of measures as specified
        while (count > 0) {

            Measure newMeasure = new Measure();

            // treat last measure specially
            if (count == 1) {
                // add repeat in last measure if selected
                if (repeatStop == true)
                    newMeasure.setRepeat(RepeatType.STOP);
                else {
                    if (ending == true)
                        newMeasure.setRepeat(RepeatType.ENDING);
                }

                // add first newline to previous measure if requested
                if (newLine == true)
                    newMeasure.addAttribute(MeasureAttribute.LINE_BREAK);
            }
            // add new measure after previously added one
            insertMeasure(newMeasure, firstMeasure);

            // next one after this new one
            firstMeasure = newMeasure;

            // next measure
            count--;
        }

        updateNumberOfMeasures();
    }

} // class

