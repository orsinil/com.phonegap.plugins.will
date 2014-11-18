package com.wacom.toolsconfigurator;

import com.pinaround.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;

import com.wacom.ink.path.PathBuilder.InputDynamicsType;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.Utils;

/*
 * Created by Zahari Pastarmadjiev
 * Copyright (c) 2013 Wacom. All rights reserved.
 */


/**
 * Our controller - it keeps our model up-to-date and makes calls to the Inking Engine.
 */
public class Controller{
	private final static Logger logger = new Logger(Controller.class);
	private CanvasModel canvasModel;
	private MainActivity activity;
	private ToolConfigurationFragment toolConfigurationFragment;
	private SparseIntArray colorViewsMap;
	private SparseIntArray paperViewsMap;
	
	public Controller(MainActivity activity, CanvasModel canvasModel){
		this.activity = activity;
		this.canvasModel = canvasModel;
	}

	public void initialize() {
		int viewId;
		int viewIds[];

		colorViewsMap = new SparseIntArray();
		viewIds = new int[] {R.id.btn_color1, R.id.btn_color2, R.id.btn_color3, R.id.btn_color4, R.id.btn_color5};
		for (int idx=0;idx<viewIds.length;idx++){
			colorViewsMap.put(viewIds[idx], idx);
		}

		paperViewsMap = new SparseIntArray();
		viewIds = new int[] {R.id.btn_changebackground1, R.id.btn_changebackground2, R.id.btn_changebackground3};
		for (int idx=0;idx<viewIds.length;idx++){
			paperViewsMap.put(viewIds[idx], idx);
		}

		int keyIdx = paperViewsMap.indexOfValue(canvasModel.getSelectedPaperIdx());
		viewId = paperViewsMap.keyAt(keyIdx);
		activity.findViewById(viewId).setSelected(true);

		int colorIdx;
		for (int idx=0;idx<colorViewsMap.size();idx++){
			viewId = colorViewsMap.keyAt(idx);
			colorIdx = colorViewsMap.valueAt(idx);
			colorizeBtn(viewId, canvasModel.getColorAtIdx(colorIdx));
			activity.findViewById(viewId).setSelected(colorIdx==canvasModel.getSelectedColorIdx());
		}
	}

	public void onBtnSettinsClicked() {
		toolConfigurationFragment = new ToolConfigurationFragment();
		toolConfigurationFragment.setModel(canvasModel);
		activity.getFragmentManager().beginTransaction().add(R.id.mainContainer, toolConfigurationFragment).commit();
	}

	public void onBtnExportClicked() {
		ExportDialogFragment newFragment = new ExportDialogFragment();
		newFragment.setModel(canvasModel);

		newFragment.show(activity.getFragmentManager(), "dialog");
	}

	public void onBtnClearClicked() {
		activity.clear();
	}

	public void onBtnCloseConfigClicked() {
		activity.getFragmentManager().beginTransaction().remove(toolConfigurationFragment).commit();
	}

	public void onBtnResetClicked() {
		activity.resetSettings();

		if (toolConfigurationFragment.isVisible())
		{
			activity.getFragmentManager().beginTransaction().remove(toolConfigurationFragment).commit();
			toolConfigurationFragment = new ToolConfigurationFragment();
			toolConfigurationFragment.setModel(canvasModel);
			activity.getFragmentManager().beginTransaction().add(R.id.mainContainer, toolConfigurationFragment).commit();
			canvasModel.initializeEvaluationTool();
			canvasModel.updateBrush();
		}
	}
	
	public void onBtnBackgroundClicked(View view) {
		int paperIdx = paperViewsMap.get(view.getId());
		if (canvasModel.getSelectedPaperIdx()==paperIdx){
			if (Logger.LOG_ENABLED) logger.d("this paper is already active / " + paperIdx);
		} else {
			if (canvasModel.getSelectedPaperIdx()!=CanvasModel.PAPER_UNKNOWN){
				int keyIdx = paperViewsMap.indexOfValue(canvasModel.getSelectedPaperIdx());
				int androidViewId = paperViewsMap.keyAt(keyIdx);
				activity.findViewById(androidViewId).setSelected(false);
			}
			canvasModel.setSelectedPaperIdx(paperIdx);
			view.setSelected(true);

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			opts.inScaled = false;

			Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), canvasModel.getSelectedPaperResId(), opts);
			activity.getInkCanvas().setBackground(Utils.cropAndScaleBitmapAtCenterPt(bitmap, activity.getInkCanvas().getWidth(), activity.getInkCanvas().getHeight()));
			activity.getInkCanvas().updateScene();
			activity.getInkCanvas().presentToScreen();
		}
		if (Logger.LOG_ENABLED) logger.d("onChangeBackgroundClicked / " + view + " / " + view.getId() + " / " + view.getTag());
	}
	
	public void onBtnColorClicked(View view) {
		int colorIdx = colorViewsMap.get(view.getId());
		if (canvasModel.getSelectedColorIdx()==colorIdx){
			if (Logger.LOG_ENABLED) logger.d("this color is already active / " + colorIdx);
		} else {
			if (canvasModel.getSelectedColorIdx()!=CanvasModel.COLOR_UNKNOWN){
				int keyIdx = colorViewsMap.indexOfValue(canvasModel.getSelectedColorIdx());
				int androidViewId = colorViewsMap.keyAt(keyIdx);
				ImageView currentColorImageView = (ImageView) activity.findViewById(androidViewId);
				currentColorImageView.setSelected(false);
				colorizeBtn(currentColorImageView, canvasModel.getSelectedColor());
			}
			canvasModel.setSelectedColorIdx(colorIdx);
			colorizeBtn((ImageView)view, canvasModel.getSelectedColor());
			view.setSelected(true);
			activity.getInkCanvas().getStrokePaint().setColorRGB(canvasModel.getSelectedColor());
		}
		if (Logger.LOG_ENABLED) logger.d("onColorBtnClicked / " + view + " / " + view.getId() + " / " + view.getTag());

	}
	
	private void colorizeBtn(int imageViewId, int color) {
		colorizeBtn((ImageView)activity.findViewById(imageViewId), color);
	}

	private void colorizeBtn(ImageView imageView, int color) {
		imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}
	
	public void onBtnToolClicked(View v) {
		if (v.getId()==R.id.btn_eraser){
			activateEraser();
		} else if (v.getId()==R.id.btn_pen){
			activateBrush();
		}
	}
	
	public void activateEraser(){
		canvasModel.getStrokeBuilder().setDynamics(canvasModel.getEraserDynamics());
		activity.getInkCanvas().getStrokePaint().setStrokeBrush(canvasModel.getEraserBrush());
		canvasModel.setInking(false);
		activity.findViewById(R.id.btn_pen).setSelected(false);
		activity.findViewById(R.id.btn_eraser).setSelected(true);
	}
	
	public void activateBrush(){
		canvasModel.getStrokeBuilder().getDynamics().setPressureConfigEnabled(canvasModel.isPressureEnabled());
		canvasModel.getStrokeBuilder().setDynamics(canvasModel.getBrushDynamics());
		canvasModel.updateBrush();
		canvasModel.setInking(true);
		activity.findViewById(R.id.btn_eraser).setSelected(false);
		activity.findViewById(R.id.btn_pen).setSelected(true);
	}
}
