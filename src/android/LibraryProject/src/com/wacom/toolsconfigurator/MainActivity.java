package com.wacom.toolsconfigurator;

/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

import java.io.File;
import java.util.LinkedList;
import com.pinaround.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import com.wacom.ink.rendering.RenderingContext;
import com.wacom.ink.StrokeBuilder;
import com.wacom.ink.StrokeInkCanvas;
import com.wacom.ink.path.SpeedPathBuilder;
import com.wacom.ink.path.PathBuilder.InputDynamicsType;
import com.wacom.ink.path.PathBuilder.PropertyFunction;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rasterization.SolidColorBrush;
import com.wacom.ink.rasterization.StrokeJoin;
import com.wacom.ink.rasterization.StrokePaint;
import com.wacom.ink.rendering.RenderingContext;
import com.wacom.ink.smooth.MultiChannelSmoothener;
import com.wacom.ink.tools.DynamicsConfig;
import com.wacom.ink.utils.IntentManager;
import com.wacom.ink.utils.IntentResponseHandler;
import com.wacom.ink.utils.Logger;
import com.wacom.ink.utils.TouchUtils;
import com.wacom.ink.utils.Utils;


/**
 * Our activity. This is where we start.
 */
public class MainActivity extends Activity implements Ink{
	private final static Logger logger = new Logger(MainActivity.class);

	private CanvasModel canvasModel;
	
	public static boolean USE_HISTORICAL_EVENTS = false;
	
	private Controller controller;
	private RenderingContext renderingContext;
	private IntentManager intentManager;
	private StrokeInkCanvas inkCanvas;
	private Matrix canvasMx = new Matrix();
	private boolean bReady;
	private TouchUtils.TouchPointID prevPoint = new TouchUtils.TouchPointID();
	private LinkedList<StrokeBuilder.CompositePoint> historicalEvents = new LinkedList<StrokeBuilder.CompositePoint>();;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		intentManager = new IntentManager();
		intentManager.addIntentResponseHandler(111,
				new IntentResponseHandler() {
					@Override
					public boolean handleResponse(int resultCode, Intent data) {
						if (resultCode == Activity.RESULT_OK
								&& data.getData() != null) {
							File copiedFile = Utils.copyFile(data.getData(),
									getFilesDir().getAbsolutePath());
							// copiedFile
						}
						return true;
					}
				});
		
		intentManager.addIntentResponseHandler(222,
				new IntentResponseHandler() {
					@Override
					public boolean handleResponse(int resultCode, Intent data) {
						if (resultCode == Activity.RESULT_OK && data.getData() != null) {
							Utils.copyFile(data.getData(), getFilesDir().getAbsolutePath());
						}
						return true;
					}
				});
		

		if (Logger.LOG_ENABLED)
			logger.i("activity.onCreate " + this);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.inkingCanvas);
		surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){

			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				renderingContext = new RenderingContext(new RenderingContext.EGLConfigSpecification());
				renderingContext.initContext();

				renderingContext.createEGLSurface(holder);
				renderingContext.bindEGLContext();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {

			}
		});
		
		inkCanvas = new StrokeInkCanvas(surfaceView, new StrokeInkCanvas.DefaultCallback() {
			@Override
			public void onReadyToStroke(StrokeInkCanvas canvas) {
				initializeInk();
				
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				opts.inScaled = false;
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.paper_bg_1, opts);

				inkCanvas.setBackground(Utils.cropAndScaleBitmapAtCenterPt(bitmap, inkCanvas.getWidth(), inkCanvas.getHeight()));
				inkCanvas.presentToScreen();
//				inkCanvas.getStrokePaint().setStrokeBrush(canvasModel.getShapeFillStrokeBrush());
//				inkCanvas.getStrokePaint().setColorRGB(canvasModel.getSelectedColor());
//				inkCanvas.getStrokePaint().setRoundCapBeginning(true);
//				inkCanvas.getStrokePaint().setRoundCapEnding(true);
				
				controller.activateBrush();
				
				inkCanvas.clear();
				inkCanvas.presentToScreen(canvasMx);
				bReady = true; 
			}
		});

		surfaceView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return processTouch(event);
			}
		});
	}

	public StrokeInkCanvas getInkCanvas(){
		return inkCanvas;
	}

	public boolean processTouch(MotionEvent event){
		if (!bReady){
			return false;
		}
		int code = TouchUtils.filterMotionEventForInking(event, prevPoint);
		int ptId = prevPoint.getPointerId();
		float pressure = event.getPressure();
		double timestamp = TouchUtils.getTimestamp(event);

		float x = event.getX();
		float y = event.getY();

		if (Logger.LOG_ENABLED) logger.i("filterMotionEventForInking: code_" + code + " / " + canvasModel.isPressureEnabled() + " | " + x + "," + y + " / " + prevPoint.getOldX() + "," + prevPoint.getOldY());

		switch (code){ 
		case TouchUtils.STROKE_EVENT_BEGIN:
			if (TouchUtils.isStylusEvent(event) && canvasModel.isPressureEnabled() && canvasModel.isInking()){
				canvasModel.getStrokeBuilder().setInputDynamicsType(InputDynamicsType.Pressure);
				updateStrokePaint(InputDynamicsType.Pressure);
			} else {
				canvasModel.getStrokeBuilder().setInputDynamicsType(InputDynamicsType.Velocity);
				updateStrokePaint(InputDynamicsType.Velocity);
			}
			canvasModel.getStrokeBuilder().beginStroke(x, y, pressure, timestamp, true);
			canvasModel.getStrokeBuilder().drawStrokeBeginning(inkCanvas);  
			//inkCanvas.presentToScreen();
			break;
		case TouchUtils.STROKE_EVENT_FORCEEND: 
			x = prevPoint.getOldX();
			y = prevPoint.getOldY();
		case TouchUtils.STROKE_EVENT_END:
			canvasModel.getStrokeBuilder().endStroke(x, y, pressure, timestamp, true);
			canvasModel.getStrokeBuilder().drawStrokeEnding(inkCanvas);
			inkCanvas.presentToScreen(canvasMx);
			break; 
		case TouchUtils.STROKE_EVENT_MOVE:
			if (USE_HISTORICAL_EVENTS && event.getHistorySize()>0){
				if (Logger.LOG_ENABLED) logger.i("historical events: " + event.getHistorySize());
				historicalEvents.clear();
				StrokeBuilder.CompositePoint pt;
				for (int i=0;i<event.getHistorySize();i++){
					pt = new StrokeBuilder.CompositePoint();
					pt.x = event.getHistoricalX(ptId, i);
					pt.y = event.getHistoricalY(ptId, i);
					pt.pressure = event.getHistoricalPressure(ptId, i);
					pt.timestamp = TouchUtils.getTimestamp(event.getHistoricalEventTime(i));
					historicalEvents.add(pt);
				}
				pt = new StrokeBuilder.CompositePoint();
				pt.x = x;
				pt.y = y;
				pt.pressure = pressure;
				pt.timestamp = timestamp;
				historicalEvents.add(pt);
				canvasModel.getStrokeBuilder().addControlPoints(historicalEvents, true, true, true);
				canvasModel.getStrokeBuilder().drawStrokeParts(inkCanvas, true);
			} else {
				canvasModel.getStrokeBuilder().addControlPoint(x, y, pressure, timestamp, true, true, true);
				canvasModel.getStrokeBuilder().drawStrokePart(inkCanvas, true);
			} 
			inkCanvas.presentToScreen(canvasMx);
			break;  
		case TouchUtils.STROKE_EVENT_FAIL:
			break;
		}

		return true;
	}

	@Override
	public void onAttachedToWindow() {
		if (Logger.LOG_ENABLED)
			logger.i("onAttachedToWindow");
		super.onAttachedToWindow();
	}

	@Override
	public void onBackPressed() {
		if (Logger.LOG_ENABLED)
			logger.i("onBackPressed");
		super.onBackPressed();
	}

	@Override
	public void onContentChanged() {
		if (Logger.LOG_ENABLED)
			logger.i("onContentChanged");
		super.onContentChanged();
	}

	@Override
	protected void onDestroy() {
		if (Logger.LOG_ENABLED)
			logger.i("onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetachedFromWindow() {
		if (Logger.LOG_ENABLED)
			logger.i("onDetachedFromWindow");
		super.onDetachedFromWindow();
	}

	@Override
	protected void onPause() {
		if (Logger.LOG_ENABLED)
			logger.i("onPause");
		super.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		if (Logger.LOG_ENABLED)
			logger.i("onPostCreate");
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onPostResume() {
		if (Logger.LOG_ENABLED)
			logger.i("onPostResume");
		super.onPostResume();
	}

	@Override
	protected void onRestart() {
		if (Logger.LOG_ENABLED)
			logger.i("onRestart");
		super.onRestart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (Logger.LOG_ENABLED)
			logger.i("onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (Logger.LOG_ENABLED)
			logger.i("onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		if (Logger.LOG_ENABLED)
			logger.i("onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (Logger.LOG_ENABLED)
			logger.i("onStop");
		super.onStop();
	}

	public void onBtnSettingsClicked(View v) {
		controller.activateBrush();
		controller.onBtnSettinsClicked();
	}

	public void onBtnClearClicked(View v) {
		controller.onBtnClearClicked();
	}

	public void onBtnCloseConfigClicked(View v) {
		// updateInkingEngineToolSet();

		controller.onBtnCloseConfigClicked();
	}

	public void onBtnExportClicked(View v) {
		controller.onBtnExportClicked();
	}

	public void onBtnResetClicked(View v) {
		controller.onBtnResetClicked();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		intentManager.processIntentResponse(requestCode, resultCode, data);

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void initializeInk(){
		canvasModel = new CanvasModel(this);
		controller = new Controller(this, canvasModel);
		controller.initialize();
	}

	public void resetSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		inkCanvas.clear();
		inkCanvas.presentToScreen();
	}
	
	public void onBtnBackgroundClicked(View view) {
		controller.onBtnBackgroundClicked(view);
	}
	
	public void onBtnColorClicked(View view) {
		controller.onBtnColorClicked(view);
	}
	
	public void updateStrokePaint(InputDynamicsType type){
		DynamicsConfig dynConf = canvasModel.getStrokeBuilder().getDynamics();
 
		if (dynConf.isPropertyConfigEnabled(type, PropertyName.Width)){
			if (Logger.LOG_ENABLED) logger.i("selectTool / updateStrokePaint set NaN width " + type + " => " + PropertyName.Width);
			inkCanvas.getStrokePaint().setWidth(Float.NaN);
			inkCanvas.getStrokePaint().setAlpha(1.0f);
		}

		if (dynConf.isPropertyConfigEnabled(type, PropertyName.Alpha)){
			if (Logger.LOG_ENABLED) logger.i("selectTool / updateStrokePaint set NaN alpha " + type + " => " + PropertyName.Alpha);
			inkCanvas.getStrokePaint().setAlpha(Float.NaN);
		}
		
		if (Logger.LOG_ENABLED) logger.i("selectTool / updateStrokePaint: " + inkCanvas.getStrokePaint().toString());
	}
	
	public void onBtnToolClicked(View v) {
		controller.onBtnToolClicked(v);
	}
}
