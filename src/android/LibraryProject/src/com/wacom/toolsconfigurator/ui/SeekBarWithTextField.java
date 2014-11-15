package com.wacom.toolsconfigurator.ui;

import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarWithTextField extends SeekBar{
	public static class DynamicValues{
		public float minValue;
		public float maxValue;
		public float currentValue;
		public float step;
		public boolean bDisplayAsFloat;
		
		public DynamicValues(float minValue, float maxValue, float step, float currentValue, boolean bDisplayAsFloat){
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.step = step;
			this.currentValue = currentValue;
			this.bDisplayAsFloat = bDisplayAsFloat;
		}
	}
	
	public static final int NOT_SET = -1;
	private float minValue;
	private float maxValue;
	private float defaultValue;
	private float step;
	//private int	textFieldId;
	private TextView textView;
	private float currentValue;
	private boolean bDisplayAsFloat;
	
	private SparseArray<DynamicValues> dynamicValues;
	private int dynamicId;
	
	public SeekBarWithTextField(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public SeekBarWithTextField init(float minValue, float maxValue, float defaultValue, float step, View container, int textViewId, boolean bDisplayAsFloat){
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.defaultValue = defaultValue;
		textView = (TextView)container.findViewById(textViewId);
		this.step = step;
		this.setMax((int)((maxValue-minValue) / step));
		this.bDisplayAsFloat = bDisplayAsFloat;
		dynamicValues = null;
		dynamicId = NOT_SET;
		
		updateProgress(defaultValue);

		return this;
	}

	public SeekBarWithTextField init(SparseArray<DynamicValues> dynamicValues, int dynamicId, View container, int textViewId) {
		this.dynamicValues = dynamicValues;
		DynamicValues values = dynamicValues.get(dynamicId);
		textView = (TextView)container.findViewById(textViewId);
		this.dynamicId = NOT_SET;
		if (values!=null){
			switchValues(dynamicId);
		} else {
			dynamicId = NOT_SET;
		}
		return this;
	}
	
	public void updateTextView() {
		textView.setText(getDisplayValue(bDisplayAsFloat));
	}

	public void switchValues(int dynamicId){
		DynamicValues values = dynamicValues.get(dynamicId);
		if (values==null){
			return;
		}
		
		if (this.dynamicId!=NOT_SET){
			dynamicValues.get(this.dynamicId).currentValue = getRealValue();
		}
		
		minValue = values.minValue;
		maxValue = values.maxValue;
		defaultValue = values.currentValue;
		step = values.step;
		setMax((int)((maxValue-minValue) / step));
		bDisplayAsFloat = values.bDisplayAsFloat;
		updateProgress(defaultValue);
		
		this.dynamicId = dynamicId;
	}
	
	public void updateProgress(float value){
		int seekBarValue = (int)((value-minValue) / step);
		currentValue = value;
		setProgress(seekBarValue);
		updateTextView();
	}
	
	public String getDisplayValue(boolean bDisplayAsFloat){
		float value = getRealValue();//minValue + (float)getProgress()*step;
		if (Float.isNaN(value)){
			return "";
		}
		if (bDisplayAsFloat){
			return String.format(Locale.UK, "%.2f", value);
		} else {
			return String.format(Locale.UK, "%.0f", value);
		}
	}

	public float getRealValue() {
//		Log.e("getRealValue", getMax() + "/v:" + (minValue + (float)getProgress()*step) + "/min:"+ minValue + "/max:" + maxValue + "/progress=" + getProgress());
		//return minValue + (float)getProgress()*step;	
		return currentValue;
	}

	public float getStep() {
		return step;
	}
	
	public float getMinValue() {
		return minValue;
	}
	
	public float getMaxValue() {
		return maxValue;
	}

	public void recalculateRealValue() {
		currentValue = minValue + (float)getProgress()*step;	
	}
}