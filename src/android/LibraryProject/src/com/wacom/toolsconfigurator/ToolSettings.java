package com.wacom.toolsconfigurator;
import com.pinaround.R;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.wacom.ink.path.PathBuilder.InputDynamicsType;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.tools.DynamicsConfig.PropertyDynamicsConfig;
import com.wacom.ink.tools.ToolSet;
import com.wacom.ink.utils.Utils;
import com.wacom.toolsconfigurator.ui.RadioButtonWithImage;
import com.wacom.toolsconfigurator.ui.SeekBarWithTextField;

public abstract class ToolSettings {
	protected SparseArray<SeekBarWithTextField> seekbars;
	protected SparseArray<SeekBarWithTextField.DynamicValues> brushDynamicsValues;

	protected View container;
	protected CanvasModel canvasModel;
	protected MainActivity activity;
	private InputDynamicsType dynamicsType;
	
	public ToolSettings(MainActivity activity, View container, CanvasModel canvasModel, InputDynamicsType dynamicsType)
	{
		this.container = container;
		this.canvasModel = canvasModel;
		this.activity = activity;
		this.dynamicsType = dynamicsType;
		
		init();
	}

	protected InputDynamicsType getDynamicsType(){
		return dynamicsType;
	}
	
	public PropertyDynamicsConfig getWidthConfig(){
		return canvasModel.getStrokeBuilder().getDynamics().getPropertyConfig(getDynamicsType(), PropertyName.Width);
	}
	
	public PropertyDynamicsConfig getAlphaConfig(){
		return canvasModel.getStrokeBuilder().getDynamics().getPropertyConfig(getDynamicsType(), PropertyName.Alpha);
	}
	
	public abstract int getTitle();
	public abstract int getFunctionLabel();
	
	public abstract int getMinValueLabel();
	public abstract int getMaxValueLabel();
	public abstract float getMinValue();
	public abstract float getMaxValue();
	public abstract void setMinValue(float minValue);
	public abstract void setMaxValue(float maxValue);

	public abstract SeekBarWithTextField initMinValueSeekBar();
	public abstract SeekBarWithTextField initMaxValueSeekBar();

	public void init()
	{
		com.wacom.toolsconfigurator.utils.Utils.setViewEnabled(container, true);
		
		seekbars = new SparseArray<SeekBarWithTextField>();
		brushDynamicsValues = new SparseArray<SeekBarWithTextField.DynamicValues>();
		
		((TextView)container.findViewById(R.id.titleLabel)).setText(getTitle());
		
		seekbars.put(R.id.seekBarMinRadius, ((SeekBarWithTextField)container.findViewById(R.id.seekBarMinRadius)).init(0.5f, 50f, getWidthConfig().getMinValue(), 0.1f, container, R.id.minradius_value, true));
		seekbars.put(R.id.seekBarMaxRadius, ((SeekBarWithTextField)container.findViewById(R.id.seekBarMaxRadius)).init(0.5f, 50f, getWidthConfig().getMaxValue(), 0.1f, container, R.id.maxradius_value, true));
		seekbars.put(R.id.seekBarMinAlpha, ((SeekBarWithTextField)container.findViewById(R.id.seekBarMinAlpha)).init(0.0f, 1f, getAlphaConfig().getMinValue(), 0.1f, container, R.id.minAlphaValue, true));
		seekbars.put(R.id.seekBarMaxAlpha, ((SeekBarWithTextField)container.findViewById(R.id.seekBarMaxAlpha)).init(0.0f, 1f, getAlphaConfig().getMaxValue(), 0.1f, container, R.id.maxAlphaValue, true));
		
		seekbars.put(R.id.seekBarMinValue, initMinValueSeekBar());
		seekbars.put(R.id.seekBarMaxValue, initMaxValueSeekBar());
		
		((TextView)container.findViewById(R.id.minvalue_label)).setText(getMinValueLabel());
		((TextView)container.findViewById(R.id.maxvalue_label)).setText(getMaxValueLabel());
		
		((TextView)container.findViewById(R.id.functionLabel)).setText(getFunctionLabel());
		Log.e("XXX", "toolsettings init getWidthConfig: " + getWidthConfig().toString() + " / " + getWidthConfig().isSigmoidFunc());
		brushDynamicsValues.put(R.id.radioBtnExponential, new SeekBarWithTextField.DynamicValues(0.1f, 8.0f, 0.1f, (getWidthConfig().isExponentialFunc()?Utils.getNonNan(getWidthConfig().getFunctionParameter(), 0.5f):0.5f), true));
		brushDynamicsValues.put(R.id.radioBtnSigmoid, new SeekBarWithTextField.DynamicValues(0.0f, 1.0f, 0.1f, (getWidthConfig().isSigmoidFunc()?Utils.getNonNan(getWidthConfig().getFunctionParameter(), 0.5f):0.5f), true));
		brushDynamicsValues.put(R.id.radioBtnPeriodic, new SeekBarWithTextField.DynamicValues(1.0f, 5.0f, 1.0f, (getWidthConfig().isPeriodicFunc()?Utils.getNonNan(getWidthConfig().getFunctionParameter(), 1.0f):1.0f), false));

		int brushDynamicsFuncId = SeekBarWithTextField.NOT_SET;
		if (getWidthConfig().isExponentialFunc()){
			brushDynamicsFuncId = R.id.radioBtnExponential;
		} else if (getWidthConfig().isSigmoidFunc()){
			brushDynamicsFuncId = R.id.radioBtnSigmoid;
		} else if (getWidthConfig().isPeriodicFunc()){
			brushDynamicsFuncId = R.id.radioBtnPeriodic;
		} else {
			brushDynamicsFuncId = R.id.radioBtnExponential;
		}

		seekbars.put(R.id.seekBarFunctionParameter, ((SeekBarWithTextField)container.findViewById(R.id.seekBarFunctionParameter)).init(brushDynamicsValues, brushDynamicsFuncId, container, R.id.functionparameter_value));
		
		seekbars.put(R.id.seekBarStartRadius, ((SeekBarWithTextField)container.findViewById(R.id.seekBarStartRadius)).init(0.5f, 50f, getWidthConfig().getInitialValue(), 0.1f, container, R.id.startradius_value, true));
		seekbars.put(R.id.seekBarEndRadius, ((SeekBarWithTextField)container.findViewById(R.id.seekBarEndRadius)).init(0.5f, 50f, getWidthConfig().getFinalValue(), 0.1f, container, R.id.endradius_value, true));
		seekbars.put(R.id.seekBarSmoothingRadius, ((SeekBarWithTextField)container.findViewById(R.id.seekBarSmoothingRadius)).init(0.0f, 1.0f, getWidthConfig().getSmoothingAlpha(), 0.1f, container, R.id.smoothingradius_value, true));
		
		seekbars.put(R.id.seekBarStartAlpha, ((SeekBarWithTextField)container.findViewById(R.id.seekBarStartAlpha)).init(0f, 1f, getAlphaConfig().getInitialValue(), 0.1f, container, R.id.startalpha_value, true));
		seekbars.put(R.id.seekBarEndAlpha, ((SeekBarWithTextField)container.findViewById(R.id.seekBarEndAlpha)).init(0f, 1f, getAlphaConfig().getFinalValue(), 0.1f, container, R.id.endalpha_value, true));
		seekbars.put(R.id.seekBarSmoothingAlpha, ((SeekBarWithTextField)container.findViewById(R.id.seekBarSmoothingAlpha)).init(0.0f, 1.0f, getAlphaConfig().getSmoothingAlpha(), 0.1f, container, R.id.smoothingalpha_value, true));
		
		for (int i = 0; i < seekbars.size(); i++)
		{
			int key = seekbars.keyAt(i);
			seekbars.get(key).setOnSeekBarChangeListener(seekBarChangeListener);
		}
		
		((RadioButtonWithImage)container.findViewById(R.id.radioBtnExponential)).init(getWidthConfig().isExponentialFunc(), (ImageView) container.findViewById(R.id.imageExponential), radioButtonChangeListener, 1.0f, 0.3f);
		((RadioButtonWithImage)container.findViewById(R.id.radioBtnPeriodic)).init(getWidthConfig().isPeriodicFunc(), (ImageView) container.findViewById(R.id.imagePeriodic), radioButtonChangeListener, 1.0f, 0.3f);
		((RadioButtonWithImage)container.findViewById(R.id.radioBtnSigmoid)).init(getWidthConfig().isSigmoidFunc(), (ImageView) container.findViewById(R.id.imageSigmoid), radioButtonChangeListener, 1.0f, 0.3f);
		
		int switchButtons[] = 
				new int[]{R.id.switchReverse, R.id.switchSmoothingRadius, R.id.switchStartRadius, R.id.switchEndRadius, 
					R.id.switchStartAlpha, R.id.switchEndAlpha, R.id.switchSmoothingAlpha};
		
		for (int i = 0; i < switchButtons.length; i++)
		{
			Switch switchButton = (Switch)container.findViewById(switchButtons[i]);
			
			switch (switchButton.getId())
			{
				case R.id.switchReverse:
					switchButton.setChecked(getWidthConfig().shouldFlip());
					break;
				case R.id.switchSmoothingRadius:
					switchButton.setChecked(canvasModel.getStrokeBuilder().isSmoothingEnabled(getDynamicsType(), PropertyName.Width));
					seekbars.get(R.id.seekBarSmoothingRadius).setEnabled(canvasModel.getStrokeBuilder().isSmoothingEnabled(getDynamicsType(), PropertyName.Width));
					break;
				case R.id.switchStartRadius:
					switchButton.setChecked(!Float.isNaN(getWidthConfig().getInitialValue()));
					seekbars.get(R.id.seekBarStartRadius).setEnabled(!Float.isNaN(getWidthConfig().getInitialValue()));
					break;
				case R.id.switchEndRadius:
					switchButton.setChecked(!Float.isNaN(getWidthConfig().getFinalValue()));
					seekbars.get(R.id.seekBarEndRadius).setEnabled(!Float.isNaN(getWidthConfig().getFinalValue()));
					break;
				case R.id.switchSmoothingAlpha:
					switchButton.setChecked(canvasModel.getStrokeBuilder().isSmoothingEnabled(getDynamicsType(), PropertyName.Alpha));
					seekbars.get(R.id.seekBarSmoothingAlpha).setEnabled(canvasModel.getStrokeBuilder().isSmoothingEnabled(getDynamicsType(), PropertyName.Alpha));
					
					/*
					if (!seekbars.get(R.id.seekBarSmoothingAlpha).isEnabled())
					{
						seekbars.get(R.id.seekBarSmoothingAlpha).updateProgress(0.6f);
					}
					*/
					break;
				case R.id.switchStartAlpha:
					switchButton.setChecked(!Float.isNaN(getAlphaConfig().getInitialValue()));
					seekbars.get(R.id.seekBarStartAlpha).setEnabled(!Float.isNaN(getAlphaConfig().getInitialValue()));
					break;
				case R.id.switchEndAlpha:
					switchButton.setChecked(!Float.isNaN(getAlphaConfig().getFinalValue()));
					seekbars.get(R.id.seekBarEndAlpha).setEnabled(!Float.isNaN(getAlphaConfig().getFinalValue()));
					break;
			}
			
			switchButton.setOnCheckedChangeListener(switchButtonCheckedChangeListener);
		}

		int incDecRadiusBtns[] = 
				new int[]{R.id.btnIncMinRadius, R.id.btnIncMaxRadius, R.id.btnDecMinRadius, R.id.btnDecMaxRadius, 
				R.id.btnIncMinAlpha, R.id.btnIncMaxAlpha, R.id.btnDecMinAlpha, R.id.btnDecMaxAlpha};
		
		for (int i = 0; i < incDecRadiusBtns.length; i++)
		{
			ImageView v = (ImageView)container.findViewById(incDecRadiusBtns[i]);
			v.setOnClickListener(incDecListener);
		}
	}
	
	public void setAlphaSectionEnabled(boolean enabled) {
		com.wacom.toolsconfigurator.utils.Utils.setViewsWithTagEnabled(container, container.getContext().getResources().getString(R.string.texture_option_tag2), enabled);
	}

	private OnClickListener incDecListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			String tag = (String) v.getTag();
			SeekBarWithTextField skView = null;

			if (tag!=null){
				if (tag.equalsIgnoreCase(activity.getResources().getString(R.string.tag_maxradius))){
					skView = seekbars.get(R.id.seekBarMaxRadius);
				} else if (tag.equalsIgnoreCase(activity.getResources().getString(R.string.tag_minradius))){
					skView = seekbars.get(R.id.seekBarMinRadius);
				} else if (tag.equalsIgnoreCase(activity.getResources().getString(R.string.tag_max_alpha))){
					skView = seekbars.get(R.id.seekBarMaxAlpha);
				} else if (tag.equalsIgnoreCase(activity.getResources().getString(R.string.tag_min_alpha))){
					skView = seekbars.get(R.id.seekBarMinAlpha);
				} else {
					return;
				}
			} else {
				return;
			}
			float newValue;
			switch (v.getId()){
				case R.id.btnIncMinRadius:
				case R.id.btnIncMaxRadius:
				case R.id.btnIncMinAlpha:
				case R.id.btnIncMaxAlpha:
					newValue = skView.getRealValue() + skView.getStep();
					newValue = Math.min(skView.getMaxValue(), Math.max(skView.getMinValue(), newValue));
					skView.updateProgress(newValue);
					break;
				case R.id.btnDecMinRadius:
				case R.id.btnDecMaxRadius:
				case R.id.btnDecMinAlpha:
				case R.id.btnDecMaxAlpha:
					newValue = skView.getRealValue() - skView.getStep();
					newValue = Math.min(skView.getMaxValue(), Math.max(skView.getMinValue(), newValue));
					skView.updateProgress(newValue);
					break;
			}
		}
	};

	private OnCheckedChangeListener switchButtonCheckedChangeListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Switch btn = (Switch)buttonView;
			switch (btn.getId()){
				case R.id.switchReverse:
					getWidthConfig().setFlip(btn.isChecked());
					getAlphaConfig().setFlip(btn.isChecked());
					//activity.updateInkingEngineToolSet();
					break;
				case R.id.switchSmoothingRadius:
					if (btn.isChecked()){
						getWidthConfig().setSmoothingAlpha(seekbars.get(R.id.seekBarSmoothingRadius).getRealValue());
						canvasModel.getStrokeBuilder().configureSmoothenerForProperty(getDynamicsType(), PropertyName.Width);
					} else {
						getWidthConfig().setSmoothingAlpha(Float.NaN);
						canvasModel.getStrokeBuilder().disableSmoothenerForProperty(getDynamicsType(), PropertyName.Width);
					}
					seekbars.get(R.id.seekBarSmoothingRadius).setEnabled(btn.isChecked());
					break;
				case R.id.switchStartRadius:
					if (btn.isChecked()){
						getWidthConfig().setInitialValue(seekbars.get(R.id.seekBarStartRadius).getRealValue());
					} else {
						getWidthConfig().setInitialValue(Float.NaN);
					}
					//activity.updateInkingEngineToolSet();
					seekbars.get(R.id.seekBarStartRadius).setEnabled(btn.isChecked());
					break;
				case R.id.switchEndRadius:
					if (btn.isChecked()){
						getWidthConfig().setFinalValue(seekbars.get(R.id.seekBarEndRadius).getRealValue());
					} else {
						getWidthConfig().setFinalValue(Float.NaN);
					}
					//activity.updateInkingEngineToolSet();
					seekbars.get(R.id.seekBarEndRadius).setEnabled(btn.isChecked());
					break;
				case R.id.switchSmoothingAlpha:
					if (btn.isChecked()){
						getAlphaConfig().setSmoothingAlpha(seekbars.get(R.id.seekBarSmoothingAlpha).getRealValue());
						canvasModel.getStrokeBuilder().configureSmoothenerForProperty(getDynamicsType(), PropertyName.Alpha);
					} else {
						getAlphaConfig().setSmoothingAlpha(Float.NaN);
						canvasModel.getStrokeBuilder().disableSmoothenerForProperty(getDynamicsType(), PropertyName.Alpha);
					}
					//activity.updateInkingEngineToolSet();
					seekbars.get(R.id.seekBarSmoothingAlpha).setEnabled(btn.isChecked());
					break;
				case R.id.switchStartAlpha:
					if (btn.isChecked()){
						getAlphaConfig().setInitialValue(seekbars.get(R.id.seekBarStartAlpha).getRealValue());
					} else {
						getAlphaConfig().setInitialValue(Float.NaN);
					}
					//activity.updateInkingEngineToolSet();
					seekbars.get(R.id.seekBarStartAlpha).setEnabled(btn.isChecked());
					break;
				case R.id.switchEndAlpha:
					if (btn.isChecked()){
						getAlphaConfig().setFinalValue(seekbars.get(R.id.seekBarEndAlpha).getRealValue());
					} else {
						getAlphaConfig().setFinalValue(Float.NaN);
					}
					//activity.updateInkingEngineToolSet();
					seekbars.get(R.id.seekBarEndAlpha).setEnabled(btn.isChecked());
					break;
			}
		}

	};

	private OnCheckedChangeListener radioButtonChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			RadioButton btn = (RadioButton)buttonView;
			RadioButton btnExp = (RadioButton)container.findViewById(R.id.radioBtnExponential);
			RadioButton btnSig = (RadioButton)container.findViewById(R.id.radioBtnSigmoid);
			RadioButton btnPer = (RadioButton)container.findViewById(R.id.radioBtnPeriodic);

			switch (buttonView.getId()){
				case R.id.radioBtnExponential:
					if (btn.isChecked()){
						btnSig.setChecked(false);
						btnPer.setChecked(false);
						seekbars.get(R.id.seekBarFunctionParameter).switchValues(R.id.radioBtnExponential);
						getWidthConfig().setFunction(ToolSet.BRUSH_DYNAMICS_FUNC_EXPONENTIAL);
						getWidthConfig().setFunctionParameter(seekbars.get(R.id.seekBarFunctionParameter).getRealValue());
//						Log.e("X", "FUNC: EXPONENTIAL / " + seekbars.get(R.id.seekBarFunctionParameter).getRealValue());
						//activity.updateInkingEngineToolSet();
					}
					break;
				case R.id.radioBtnSigmoid:
					if (btn.isChecked()){
						btnExp.setChecked(false);
						btnPer.setChecked(false);
						seekbars.get(R.id.seekBarFunctionParameter).switchValues(R.id.radioBtnSigmoid);
						getWidthConfig().setFunction(ToolSet.BRUSH_DYNAMICS_FUNC_SIGMOID);
						getWidthConfig().setFunctionParameter(seekbars.get(R.id.seekBarFunctionParameter).getRealValue());
//						Log.e("X", "FUNC: SIGMOID / " + seekbars.get(R.id.seekBarFunctionParameter).getRealValue());
						//activity.updateInkingEngineToolSet();
					}
					break;
				case R.id.radioBtnPeriodic:
					if (btn.isChecked()){
						btnExp.setChecked(false);
						btnSig.setChecked(false);
						seekbars.get(R.id.seekBarFunctionParameter).switchValues(R.id.radioBtnPeriodic);
						getWidthConfig().setFunction(ToolSet.BRUSH_DYNAMICS_FUNC_PERIODIC);
						getWidthConfig().setFunctionParameter(seekbars.get(R.id.seekBarFunctionParameter).getRealValue());
//						Log.e("X", "FUNC: PERIODIC / " + seekbars.get(R.id.seekBarFunctionParameter).getRealValue());
						//activity.updateInkingEngineToolSet();
					}
					break;
			}
		}
	};

	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener(){
		@Override       
		public void onStopTrackingTouch(SeekBar seekBar) {      
			//activity.updateInkingEngineToolSet();
		}

		@Override       
		public void onStartTrackingTouch(SeekBar seekBar) {     
		}       

		@Override       
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			SeekBarWithTextField skView = (SeekBarWithTextField)seekBar;

			SeekBarWithTextField minView = null;
			SeekBarWithTextField maxView = null;

			switch (seekBar.getId()){
				case R.id.seekBarMinRadius:
				case R.id.seekBarMaxRadius:
					minView = seekbars.get(R.id.seekBarMinRadius);
					maxView = seekbars.get(R.id.seekBarMaxRadius);
					break;
					
				case R.id.seekBarMinAlpha:
				case R.id.seekBarMaxAlpha:
					minView = seekbars.get(R.id.seekBarMinAlpha);
					maxView = seekbars.get(R.id.seekBarMaxAlpha);
					break;
					
				case R.id.seekBarMinValue:
				case R.id.seekBarMaxValue:
					minView = seekbars.get(R.id.seekBarMinValue);
					maxView = seekbars.get(R.id.seekBarMaxValue);
					break;

				case R.id.seekBarSmoothingRadius:
					break;
			}

			if (fromUser){
				skView.recalculateRealValue();
				skView.updateTextView();
			}
//			Log.e("SB_XX", "UPDATE MODEL!");
			switch (seekBar.getId()){
				case R.id.seekBarStartRadius:
//					Log.e("SB_XX", "UPDATE MODEL seekBarStartRadius/ " + skView.getRealValue());
					getWidthConfig().setInitialValue(skView.getRealValue());
					break;
				case R.id.seekBarEndRadius:
//					Log.e("SB_XX", "UPDATE MODEL seekBarEndRadius/ " + skView.getRealValue());
					getWidthConfig().setFinalValue(skView.getRealValue());
					break;

				case R.id.seekBarMinRadius:
//					Log.e("SB_XX", "UPDATE MODEL seekBarMinRadius/ " + skView.getRealValue());
					getWidthConfig().setMinValue(skView.getRealValue());
					break;
				case R.id.seekBarMaxRadius:
//					Log.e("SB_XX", "UPDATE MODEL seekBarMaxRadius/ " + skView.getRealValue());
					getWidthConfig().setMaxValue(skView.getRealValue());
					break;
					
				case R.id.seekBarStartAlpha:
//					Log.e("SB_XX", "UPDATE MODEL seekBarStartRadius/ " + skView.getRealValue());
					getAlphaConfig().setInitialValue(skView.getRealValue());
					break;
				case R.id.seekBarEndAlpha:
//					Log.e("SB_XX", "UPDATE MODEL seekBarEndRadius/ " + skView.getRealValue());
					getAlphaConfig().setFinalValue(skView.getRealValue());
					break;
					
				case R.id.seekBarMinAlpha:
//					Log.e("SB_XX", "UPDATE MODEL seekBarMinRadius/ " + skView.getRealValue());
					getAlphaConfig().setMinValue(skView.getRealValue());
					break;
				case R.id.seekBarMaxAlpha:
//					Log.e("SB_XX", "UPDATE MODEL seekBarMaxRadius/ " + skView.getRealValue());
					getAlphaConfig().setMaxValue(skView.getRealValue());
					break;

				case R.id.seekBarMinValue:
//					Log.e("SB_XX", "UPDATE MODEL seekBarMinValue/ " + skView.getRealValue());
					setMinValue(skView.getRealValue());
					break;
				case R.id.seekBarMaxValue:
//					Log.e("SB_XX", "UPDATE MODEL seekBarMaxValue/ " + skView.getRealValue());
					setMaxValue(skView.getRealValue());
					break;

				case R.id.seekBarSmoothingRadius:
//					Log.e("SB_XX", "UPDATE MODEL seekBarSmoothing/ " + skView.getRealValue());
					getWidthConfig().setSmoothingAlpha(skView.getRealValue());
					break;
					
				case R.id.seekBarFunctionParameter:
//					Log.e("SB_XX", "UPDATE MODEL seekBarFunctionParameter/ " + skView.getRealValue());
					getWidthConfig().setFunctionParameter(skView.getRealValue());
					break;					
			}


			switch (seekBar.getId()){
				case R.id.seekBarMinRadius:
					if (minView.getRealValue()>maxView.getRealValue()){
						maxView.updateProgress(minView.getRealValue());
						getWidthConfig().setMaxValue(maxView.getRealValue());
//						Log.e("SB_XX", "UPDATE MODEL fix max of seekBarMinRadius/ " + max.getRealValue());
					}
					break;
				case R.id.seekBarMaxRadius:
					if (minView.getRealValue()>maxView.getRealValue()){
						minView.updateProgress(maxView.getRealValue());
						getWidthConfig().setMinValue(minView.getRealValue());
//						Log.e("SB_XX", "UPDATE MODEL fix min of seekBarMaxRadius/ " + min.getRealValue());
					}
					break;
					
				case R.id.seekBarMinAlpha:
					if (minView.getRealValue()>maxView.getRealValue()){
						maxView.updateProgress(minView.getRealValue());
						getAlphaConfig().setMaxValue(maxView.getRealValue());
//						Log.e("SB_XX", "UPDATE MODEL fix max of seekBarMinRadius/ " + max.getRealValue());
					}
					break;
				case R.id.seekBarMaxAlpha:
					if (minView.getRealValue()>maxView.getRealValue()){
						minView.updateProgress(maxView.getRealValue());
						getAlphaConfig().setMinValue(minView.getRealValue());
//						Log.e("SB_XX", "UPDATE MODEL fix min of seekBarMaxRadius/ " + min.getRealValue());
					}
					break;

				case R.id.seekBarMinValue:
					if (minView.getRealValue() >= maxView.getRealValue()){
						maxView.updateProgress(minView.getRealValue() + maxView.getStep());
						setMaxValue(maxView.getRealValue());
//						Log.e("SB_XX", "UPDATE MODEL fix max of seekBarMinValue/ " + max.getRealValue());
					}
					break;
				case R.id.seekBarMaxValue:
					if (minView.getRealValue() >= maxView.getRealValue()){
						minView.updateProgress(maxView.getRealValue() - minView.getStep());
						setMinValue(minView.getRealValue());
//						Log.e("SB_XX", "UPDATE MODEL fix min of seekBarMaxValue/ " + min.getRealValue());
					}
					break;
			}

			if (!fromUser){
				//activity.updateInkingEngineToolSet();
			}
		}
	};

}