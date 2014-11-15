package com.wacom.toolsconfigurator;

import android.view.View;

import com.wacom.ink.path.PathBuilder.InputDynamicsType;
import com.wacom.toolsconfigurator.ui.SeekBarWithTextField;

public class PressureToolSettings extends ToolSettings {

	public PressureToolSettings(MainActivity activity, View container, CanvasModel canvasModel) {
		super(activity, container, canvasModel, InputDynamicsType.Pressure);
	}
	
	@Override
	public void init()
	{
		super.init();
		if (!canvasModel.getStrokeBuilder().getDynamics().isPressureConfigEnabled()) {
			com.wacom.toolsconfigurator.utils.Utils.setViewEnabled(container, false);
		}
	}
	
	public int getTitle()
	{
		return R.string.pressure_configuration_label;
	}
	
	public int getFunctionLabel()
	{
		return R.string.pressure_function_label;
	}

	@Override
	public int getMinValueLabel() {
		return R.string.minpressure;
	}

	@Override
	public int getMaxValueLabel() {
		return R.string.maxpressure;
	}

	@Override
	public float getMinValue() {
		return canvasModel.getStrokeBuilder().getDynamics().getMinPressure();
	}

	@Override
	public float getMaxValue() {
		return canvasModel.getStrokeBuilder().getDynamics().getMaxPressure();
	}

	@Override
	public void setMinValue(float minValue) {
		canvasModel.getStrokeBuilder().getDynamics().setMinPressure(minValue);
	}

	@Override
	public void setMaxValue(float maxValue) {
		canvasModel.getStrokeBuilder().getDynamics().setMaxPressure(maxValue);
	}

	@Override
	public SeekBarWithTextField initMinValueSeekBar() {
		return ((SeekBarWithTextField) container.findViewById(R.id.seekBarMinValue)).init(0f, 0.99f,
				getMinValue(), 0.01f, container, R.id.minvalue_value, true);
	}

	@Override
	public SeekBarWithTextField initMaxValueSeekBar() {
		return ((SeekBarWithTextField) container.findViewById(R.id.seekBarMaxValue)).init(0.01f, 1.0f,
				getMaxValue(), 0.01f, container, R.id.maxvalue_value, true);
	}

}
