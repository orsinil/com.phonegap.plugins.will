package com.wacom.toolsconfigurator;

import android.view.View;

import com.wacom.ink.path.PathBuilder.InputDynamicsType;
import com.wacom.toolsconfigurator.ui.SeekBarWithTextField;

public class VelocityToolSettings extends ToolSettings {
	public VelocityToolSettings(MainActivity activity, View container, CanvasModel canvasModel) {
		super(activity, container, canvasModel, InputDynamicsType.Velocity);
	}
	
	@Override
	public int getTitle()
	{
		return R.string.velocity_configuration_label;
	}
	
	@Override
	public int getFunctionLabel()
	{
		return R.string.velocity_function_label;
	}

	@Override
	public int getMinValueLabel() {
		return R.string.minvelocity;
	}

	@Override
	public int getMaxValueLabel() {
		return R.string.maxvelocity;
	}

	@Override
	public float getMinValue() {
		return canvasModel.getStrokeBuilder().getDynamics().getMinVelocity();
	}

	@Override
	public float getMaxValue() {
		return canvasModel.getStrokeBuilder().getDynamics().getMaxVelocity();
	}

	@Override
	public void setMinValue(float minValue) {
		canvasModel.getStrokeBuilder().getDynamics().setMinVelocity(minValue);
	}

	@Override
	public void setMaxValue(float maxValue) {
		canvasModel.getStrokeBuilder().getDynamics().setMaxVelocity(maxValue);
	}

	@Override
	public SeekBarWithTextField initMinValueSeekBar() {
		return ((SeekBarWithTextField)container.findViewById(R.id.seekBarMinValue)).init(5f, 3999f,
				getMinValue(), 1f, container, R.id.minvalue_value, false);
	}

	@Override
	public SeekBarWithTextField initMaxValueSeekBar() {
		return ((SeekBarWithTextField) container.findViewById(R.id.seekBarMaxValue)).init(6f, 4000f,
				getMaxValue(), 1f, container, R.id.maxvalue_value, false);
	}
}
