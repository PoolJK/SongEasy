package de.songeasy.android.xml;

import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

import android.util.Log;
import de.songeasy.android.interfaces.OnXmlNextMeasureListener;
import de.songeasy.android.interfaces.OnXmlNumberOfMeasuresListener;

/**
 * Simple XML Serializer Visitor
 * 
 * Holds callback functions for read and write. Gets called by the
 * (de)serializer, needed later for implementing a progress bar.
 * 
 * @author krein
 * 
 */
public class XMLVisitor implements Visitor {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	// fall back number of measures if old file format
	private final static int DEFAULTNUMBEROFMEASURES = 20;

	private OnXmlNextMeasureListener onNextMeasureListener = null;
	private OnXmlNumberOfMeasuresListener onNumberOfMeasuresListener = null;


    // +++++++ getters and setters +++++++
    // +++++++++++++++++++++++++++++++++++

	public void setOnNextMeasureListener(OnXmlNextMeasureListener listener) {

		this.onNextMeasureListener = listener;
	};


	public void setOnNumberOfMeasuresListener(
			OnXmlNumberOfMeasuresListener listener) {

		this.onNumberOfMeasuresListener = listener;
	};


	@Override
	public void read(Type type, NodeMap<InputNode> node) throws Exception {

		int measureNumber = 0;

		if (node.getName().equals("song")) {

			try {
				InputNode attribute = node.getNode().getAttribute(
						"numberOfMeasures");
				if (attribute == null)
					measureNumber = DEFAULTNUMBEROFMEASURES;
				else
					measureNumber = Integer.parseInt(attribute.getValue());

				if (onNumberOfMeasuresListener != null)
					onNumberOfMeasuresListener
							.onNumberOfMeasures(measureNumber);

				// Log.d(TAG, String.format("measureNumber: %d",
				// measureNumber));
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}

		}

		if (node.getName().equals("measure")) {
			// measureCounter++;
			if (onNextMeasureListener != null)
				onNextMeasureListener.onNextMeasure();
			// Log.d(TAG, String.format("measure: %d", measureCounter));
		}
	}


	@Override
	public void write(Type type, NodeMap<OutputNode> node) throws Exception {

		if (node.getName().equals(new String("measure"))) {
			// measureCounter++;
			// Log.d(TAG, String.format("measure: %d", measureCounter));
		}

		if (onNextMeasureListener != null)
			onNextMeasureListener.onNextMeasure();
	}

} // class

