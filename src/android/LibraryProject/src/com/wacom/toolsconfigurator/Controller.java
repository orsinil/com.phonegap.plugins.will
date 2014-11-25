package com.wacom.toolsconfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

import com.wacom.ink.StrokeInkCanvas;
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
	private int unselectColor=0xff888888;
	private int selectColor=0xffffffff;
	
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

		/*int keyIdx = paperViewsMap.indexOfValue(canvasModel.getSelectedPaperIdx());
		viewId = paperViewsMap.keyAt(keyIdx);
		activity.findViewById(viewId).setSelected(true);

		int colorIdx;
		for (int idx=0;idx<colorViewsMap.size();idx++){
			viewId = colorViewsMap.keyAt(idx);
			colorIdx = colorViewsMap.valueAt(idx);
			colorizeBtn(viewId, canvasModel.getColorAtIdx(colorIdx));
			activity.findViewById(viewId).setSelected(colorIdx==canvasModel.getSelectedColorIdx());
		}*/
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
	
	public void changheColor(int id,int destid) {
		 int colorIdx=colorViewsMap.get(id);
    	 canvasModel.setSelectedColorIdx(colorIdx);
    	 int colorSelected=canvasModel.getSelectedColor();
    	 activity.getInkCanvas().getStrokePaint().setColorRGB(colorSelected);
    	 MenuItem parentItem=activity.mainMenu.findItem(destid);
    	 parentItem.getIcon().setColorFilter(colorSelected, PorterDuff.Mode.MULTIPLY);
	}
	
	public void onMenuClicked(MenuItem item) {
		int id = item.getItemId();
		 switch (id) {
         case R.id.btn_color1:
        	 changheColor(id,R.id.btn_color);
        	 activateBrush();
        	 break;
         case R.id.btn_color2:
        	 changheColor(id,R.id.btn_color);
        	 activateBrush();
        	 break;
         case R.id.btn_color3:
        	 changheColor(id,R.id.btn_color);
        	 activateBrush();
        	 break;
         case R.id.btn_color4:
        	 changheColor(id,R.id.btn_color);
        	 activateBrush();
        	 break;
         case R.id.btn_color5:
        	 changheColor(id,R.id.btn_color);
        	 activateBrush();
        	 break;
         case R.id.btn_pen:
        	 activateBrush();
        	 break;
         case R.id.btn_eraser:
        	 activateEraser();
        	 break;
         case R.id.btn_export:
        	saveasbitmap();
        	 break;
         case R.id.btn_close:
        	 //codice per chiudere
         default:
            
     }
	}
	
	public boolean saveasbitmap() {
		StrokeInkCanvas view = (StrokeInkCanvas) activity.getInkCanvas();
		
		int width=view.getWidth();
		int height=view.getHeight();
		
		
		int size = width * height;
	    ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
	    buf.order(ByteOrder.nativeOrder());
	    view.readPixels(0, 0, width, height, buf,0);
	    int data[] = new int[size];
	    buf.asIntBuffer().get(data);
	    buf = null;
	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    bitmap.setPixels(data, size-width, -width, 0, 0, width, height);
	    data = null;

	    short sdata[] = new short[size];
	    ShortBuffer sbuf = ShortBuffer.wrap(sdata);
	    bitmap.copyPixelsToBuffer(sbuf);
	    for (int i = 0; i < size; ++i) {
	        //BGR-565 to RGB-565
	        short v = sdata[i];
	        sdata[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
	    }
	    sbuf.rewind();
	    bitmap.copyPixelsFromBuffer(sbuf);

	    try {
	        FileOutputStream fos = new FileOutputStream("/sdcard/screeshot.jpg");
	        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	        fos.flush();
	        fos.close();
	    } catch (Exception e) {
	        // handle
	    }

		
		 return true;
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
	
	/*public void onBtnToolClicked(View v) {
		if (v.getId()==R.id.btn_eraser){
			activateEraser();
		} else if (v.getId()==R.id.btn_pen){
			activateBrush();
		}
	}*/
	
	public void activateEraser(){
		canvasModel.getStrokeBuilder().setDynamics(canvasModel.getEraserDynamics());
		activity.getInkCanvas().getStrokePaint().setStrokeBrush(canvasModel.getEraserBrush());
		canvasModel.setInking(false);
		activity.mainMenu.findItem(R.id.btn_pen).getIcon().setColorFilter(unselectColor, PorterDuff.Mode.MULTIPLY);
		activity.mainMenu.findItem(R.id.btn_eraser).getIcon().setColorFilter(selectColor, PorterDuff.Mode.MULTIPLY);
	}
	
	public void activateBrush(){
		canvasModel.getStrokeBuilder().getDynamics().setPressureConfigEnabled(canvasModel.isPressureEnabled());
		canvasModel.getStrokeBuilder().setDynamics(canvasModel.getBrushDynamics());
		canvasModel.updateBrush();
		canvasModel.setInking(true);
		activity.mainMenu.findItem(R.id.btn_pen).getIcon().setColorFilter(selectColor, PorterDuff.Mode.MULTIPLY);
		activity.mainMenu.findItem(R.id.btn_eraser).getIcon().setColorFilter(unselectColor, PorterDuff.Mode.MULTIPLY);
	}
}
