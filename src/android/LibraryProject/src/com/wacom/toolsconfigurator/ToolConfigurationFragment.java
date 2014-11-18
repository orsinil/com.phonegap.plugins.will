package com.wacom.toolsconfigurator;

import <app-package>.R;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;

import com.wacom.ink.StrokeBuilder;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.toolsconfigurator.ui.SeekBarWithTextField;
import com.wacom.toolsconfigurator.utils.Utils;

public class ToolConfigurationFragment extends Fragment
{
	protected SparseArray<SeekBarWithTextField> seekbars;
	
	private CanvasModel canvasModel;
	
	private TextureToolSettings texturesConfigSection;
	private VelocityToolSettings velocityConfigSection;
	private PressureToolSettings pressureConfigSection;
	
	public ToolConfigurationFragment()
	{
		seekbars = new SparseArray<SeekBarWithTextField>();
	}
	
	public void setModel(CanvasModel canvasModel)
	{
		this.canvasModel = canvasModel;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.toolconfig, container, false);
		
		MainActivity activity = (MainActivity)getActivity();
		
		initTextureConfiguration(v);
		
		velocityConfigSection = new VelocityToolSettings(activity, v.findViewById(R.id.velocityToolSettings), canvasModel);
		pressureConfigSection = new PressureToolSettings(activity, v.findViewById(R.id.pressureToolSettings), canvasModel);
		
		velocityConfigSection.setAlphaSectionEnabled(canvasModel.isParticleScatteringEnabled());
		pressureConfigSection.setAlphaSectionEnabled(canvasModel.isParticleScatteringEnabled());
	
		return v;
	}
	
	private void initTextureConfiguration(View v)
	{
//		Log.e("XXX", "initTextureConfiguration");
		StrokeBuilder strokeBuilder = canvasModel.getStrokeBuilder();
		
		Switch switchPressure = (Switch)v.findViewById(R.id.switchPressure);
		
		switchPressure.setChecked(strokeBuilder.getDynamics().isPressureConfigEnabled());
		switchPressure.setOnCheckedChangeListener(onSwitchPressureButtonCheckedChangeListener);
		
		Switch switchTexture = (Switch)v.findViewById(R.id.switchTexture);
		switchTexture.setChecked(canvasModel.isParticleScatteringEnabled());
		
		Utils.setViewEnabled(v.findViewById(R.id.textureToolSettings), canvasModel.isParticleScatteringEnabled());
		switchTexture.setOnCheckedChangeListener(onSwitchTextureButtonCheckedChangeListener);
		texturesConfigSection = new TextureToolSettings(getActivity(), canvasModel, v);
		
		seekbars.put(R.id.seekBarTextureSpacing, ((SeekBarWithTextField)v.findViewById(R.id.seekBarTextureSpacing)).init(0.05f, 5.0f, 
				canvasModel.getScatterStrokeBrush().getSpacing(), 0.05f, v, R.id.textureSpacingValue, true));
		
		seekbars.put(R.id.seekBarTextureScattering, ((SeekBarWithTextField)v.findViewById(R.id.seekBarTextureScattering)).init(0.0f, 5.0f, 
				canvasModel.getScatterStrokeBrush().getScattering(), 0.05f, v, R.id.textureScatteringValue, true));
		
		for (int i = 0; i < seekbars.size(); i++)
		{
			int key = seekbars.keyAt(i);
			seekbars.get(key).setOnSeekBarChangeListener(seekBarChangeListener);
		}
		
		int incDecRadiusBtns[] = 
				new int[]{R.id.btnIncTextureSpacing, R.id.btnIncTextureScattering, R.id.btnDecTextureSpacing, R.id.btnDecTextureScattering};
		
		for (int i = 0; i < incDecRadiusBtns.length; i++)
		{
			ImageView btn = (ImageView)v.findViewById(incDecRadiusBtns[i]);
			btn.setOnClickListener(incDecListener);
		}
		
		Switch switchButton = (Switch)v.findViewById(R.id.switchTextureMaxBlend);
		switchButton.setChecked(canvasModel.getScatterStrokeBrush().getBlendMode()==BlendMode.BLENDMODE_MAX);
		switchButton.setOnCheckedChangeListener(switchButtonCheckedChangeListener);
		
		((RadioGroup)v.findViewById(R.id.textureRotation)).setOnCheckedChangeListener(radioButtonChangeListener);
		
		((RadioButton)v.findViewById(R.id.radioBtnTextureRotationOff)).setChecked(false);
		((RadioButton)v.findViewById(R.id.radioBtnTextureRotationRandom)).setChecked(false);
		((RadioButton)v.findViewById(R.id.radioBtnTextureRotationTrajectory)).setChecked(false);
		
		if (canvasModel.getScatterStrokeBrush().shouldRotateAlongTrajectory()) {
			((RadioButton)v.findViewById(R.id.radioBtnTextureRotationTrajectory)).setChecked(true);
		} else if (canvasModel.getScatterStrokeBrush().shouldRotateRandom()) {
			((RadioButton)v.findViewById(R.id.radioBtnTextureRotationRandom)).setChecked(true);
		} else {
			((RadioButton)v.findViewById(R.id.radioBtnTextureRotationOff)).setChecked(true);
		}
	}
	
	private OnCheckedChangeListener onSwitchPressureButtonCheckedChangeListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			canvasModel.setPressureEnabled(isChecked);
			canvasModel.getStrokeBuilder().getDynamics().setPressureConfigEnabled(isChecked);
			pressureConfigSection.init();
			pressureConfigSection.setAlphaSectionEnabled(isChecked && canvasModel.isParticleScatteringEnabled());
		}
	};
	
	private OnCheckedChangeListener onSwitchTextureButtonCheckedChangeListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{ 
			canvasModel.setParticleScatteringEnabled(isChecked);
			velocityConfigSection.init();
			velocityConfigSection.setAlphaSectionEnabled(isChecked);
			
			if (canvasModel.isPressureEnabled()) {
				pressureConfigSection.init();
				pressureConfigSection.setAlphaSectionEnabled(isChecked);
			}
			Utils.setViewEnabled(getView().findViewById(R.id.textureToolSettings), isChecked);
		}
	};
	
	private OnClickListener incDecListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			String tag = (String) v.getTag();
			SeekBarWithTextField skView = null;

			if (tag!=null)
			{
				if (tag.equalsIgnoreCase(getActivity().getString(R.string.tag_texture_spacing)))
				{
					skView = seekbars.get(R.id.seekBarTextureSpacing);
				}
				else if (tag.equalsIgnoreCase(getActivity().getString(R.string.tag_texture_scattering)))
				{
					skView = seekbars.get(R.id.seekBarTextureScattering);
				}
				else
				{
					return;
				}
			}
			else 
			{
				return;
			}
			
			float newValue;
			switch (v.getId())
			{
				case R.id.btnIncTextureSpacing:
				case R.id.btnIncTextureScattering:
					newValue = skView.getRealValue() + skView.getStep();
					newValue = Math.min(skView.getMaxValue(), Math.max(skView.getMinValue(), newValue));
					skView.updateProgress(newValue);
					break;
				case R.id.btnDecTextureSpacing:
				case R.id.btnDecTextureScattering:
					newValue = skView.getRealValue() - skView.getStep();
					newValue = Math.min(skView.getMaxValue(), Math.max(skView.getMinValue(), newValue));
					skView.updateProgress(newValue);
					break;
			}
		}
	};
	
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			SeekBarWithTextField skView = (SeekBarWithTextField)seekBar;
			
			if (fromUser)
			{
				skView.recalculateRealValue();
				skView.updateTextView();
			}
			
			switch (seekBar.getId())
			{
				case R.id.seekBarTextureSpacing:
					canvasModel.getScatterStrokeBrush().setSpacing(skView.getRealValue());
					break;
				case R.id.seekBarTextureScattering:
					canvasModel.getScatterStrokeBrush().setScattering(skView.getRealValue());
					break;
			}
		}
	};
	
	private OnCheckedChangeListener switchButtonCheckedChangeListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			canvasModel.getScatterStrokeBrush().setBlendMode(isChecked ? BlendMode.BLENDMODE_MAX : BlendMode.BLENDMODE_NORMAL);
		}
	};
	
	private RadioGroup.OnCheckedChangeListener radioButtonChangeListener = new RadioGroup.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			canvasModel.getScatterStrokeBrush().setRotateRandom(false);
			canvasModel.getScatterStrokeBrush().setRotateAlongTrajectory(false);
			
			switch (checkedId)
			{
				case R.id.radioBtnTextureRotationOff:
					break;
				case R.id.radioBtnTextureRotationRandom:
					canvasModel.getScatterStrokeBrush().setRotateRandom(true);
					break;
				case R.id.radioBtnTextureRotationTrajectory:
					canvasModel.getScatterStrokeBrush().setRotateAlongTrajectory(true);
					break;
			}
		}
	};
}