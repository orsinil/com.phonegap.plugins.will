package com.wacom.toolsconfigurator.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

public class RadioButtonWithImage extends RadioButton{
	private float checkedAlpha;
	private float unCheckedAlpha;
	private ImageView image;
	
	public RadioButtonWithImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void init(boolean bChecked, ImageView image, final OnCheckedChangeListener listener, float checkedAlpha, float unCheckedAlpha){
		this.checkedAlpha = checkedAlpha;
		this.unCheckedAlpha = unCheckedAlpha;
		this.image = image;
		this.setChecked(bChecked);
		
		updateImage();
		
		setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateImage();
				listener.onCheckedChanged(buttonView, isChecked);
			}
		});
	}

	protected void updateImage() {
		if (isChecked()){
			image.setAlpha(checkedAlpha);
		} else {
			image.setAlpha(unCheckedAlpha);
		}
	}

	private void colorizeBtn(ImageView imageView, int color) {
		imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}
}