/*
 * Created by Zahari Pastarmadjiev.
 * Copyright (c) 2013 Wacom. All rights reserved.
 */

package com.wacom.toolsconfigurator;
import <app-package>.R;
import android.util.SparseIntArray;

import com.wacom.ink.StrokeBuilder;
import com.wacom.ink.StrokeInkCanvas;
import com.wacom.ink.path.PathBuilder.InputDynamicsType;
import com.wacom.ink.path.PathBuilder.PropertyFunction;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.rasterization.ScatterStrokeBrush;
import com.wacom.ink.rasterization.ShapeFillStrokeBrush;
import com.wacom.ink.rasterization.StrokeBrush;
import com.wacom.ink.tools.DynamicsConfig;
import com.wacom.ink.utils.Logger;

public class CanvasModel {
	private static Logger logger = new Logger(CanvasModel.class);

	public static final int PAPER_UNKNOWN = -1;
	public static final int COLOR_UNKNOWN = -1;
	public static final int TOOL_ID_NOT_SET = -1;
	public static final int FIRST_INDEX = 0;
	
	private StrokeBuilder strokeBuilder;
	private ShapeFillStrokeBrush shapeFillBrush;
	private ScatterStrokeBrush scatterStrokeBrush;
	
	private ShapeFillStrokeBrush eraserBrush;
	private DynamicsConfig eraserDynamics;
	private DynamicsConfig brushDynamics;
	
	private boolean bParticleScatteringEnabled;
	private boolean bPressureEnabled;
	
	private StrokeInkCanvas inkCanvas;
	private MainActivity context;
	
	private SparseIntArray colors;
	private SparseIntArray papers;
	private int selectedColorIdx;
	private int selectedPaperIdx;
	private boolean bInking;
	
	public CanvasModel(MainActivity context) {
		this.context = context;
		inkCanvas = context.getInkCanvas();
		
		eraserBrush = new ShapeFillStrokeBrush(true);
		eraserBrush.setGradientAntialiazingEnabled(true);
		eraserBrush.setBlendMode(BlendMode.BLENDMODE_MAX);
		eraserBrush.setLayerBlendMode(BlendMode.BLENDMODE_ERASE);
		
		eraserDynamics = new DynamicsConfig();
		
		eraserDynamics.setMinMovement(4.0f);
		eraserDynamics.setMinVelocity(50.0f);
		eraserDynamics.setMaxVelocity(2000.0f);
		
		eraserDynamics.setMinPressure(0.2f);
		eraserDynamics.setMaxPressure(0.9f);
		
		DynamicsConfig.PropertyDynamicsConfig propertyConfig = eraserDynamics.getPropertyConfig(InputDynamicsType.Velocity, PropertyName.Width);
		
		propertyConfig.setMinValue(8.0f);
		propertyConfig.setMaxValue(112.0f);
		propertyConfig.setInitialValue(8.0f);
		propertyConfig.setFinalValue(8.0f);
		propertyConfig.setFunction(PropertyFunction.Power);
		propertyConfig.setFunctionParameter(1.0f);
		propertyConfig.setEnabled(true);
		propertyConfig.setSmoothingAlpha(0.75f);
		propertyConfig.setSmoothingBeta(0.04844f);
		
		propertyConfig = eraserDynamics.getPropertyConfig(InputDynamicsType.Velocity, PropertyName.Alpha);
		
		propertyConfig.setMinValue(1.0f);
		propertyConfig.setMaxValue(1.0f);
		propertyConfig.setInitialValue(1.0f);
		propertyConfig.setFinalValue(1.0f);
		propertyConfig.setFunction(PropertyFunction.Power);
		propertyConfig.setFunctionParameter(1.0f);
		propertyConfig.setEnabled(true);
		propertyConfig.setSmoothingAlpha(0.75f);
		propertyConfig.setSmoothingBeta(0.04844f);
		
		strokeBuilder = new StrokeBuilder(context);
		strokeBuilder.initialize();
		
		initializeEvaluationTool();
		
		bInking = true;
		
		selectedColorIdx = FIRST_INDEX;
		selectedPaperIdx = FIRST_INDEX;
		
		colors = new SparseIntArray();
		papers = new SparseIntArray();
		
		int colorResIds[] = new int[]{R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6};
		for (int idx=0;idx<colorResIds.length;idx++){
			setColorAtIdx(idx, context.getResources().getColor(colorResIds[idx]));
		}

		int paperResIds[] = new int[]{R.drawable.paper_bg_1, R.drawable.paper_bg_2, R.drawable.paper_bg_3, R.drawable.paper_bg_4, R.drawable.paper_bg_5};
		for (int idx=0;idx<paperResIds.length;idx++){
			setPaperResIdAtIdx(idx, paperResIds[idx]);
		}
	}

	public void initializeEvaluationTool() {
		shapeFillBrush = new ShapeFillStrokeBrush(true);
		shapeFillBrush.setGradientAntialiazingEnabled(true);
		shapeFillBrush.setBlendMode(BlendMode.BLENDMODE_NORMAL);
		shapeFillBrush.setLayerBlendMode(BlendMode.BLENDMODE_NORMAL);
		
		scatterStrokeBrush = new ScatterStrokeBrush(true);
		scatterStrokeBrush.setBlendMode(BlendMode.BLENDMODE_NORMAL);
		scatterStrokeBrush.setLayerBlendMode(BlendMode.BLENDMODE_NORMAL);
		scatterStrokeBrush.setFillTextureFilename("tool_fill_6.png");
		scatterStrokeBrush.setShapeTextureFilename("tool_shape_6.png");
		scatterStrokeBrush.setScattering(0.15f);
		scatterStrokeBrush.setSpacing(0.15f);
		scatterStrokeBrush.setRandomizeFill(true);
		scatterStrokeBrush.setRotateRandom(true);
		scatterStrokeBrush.setRotateAlongTrajectory(false);
		
		bParticleScatteringEnabled = false;
		bPressureEnabled = false;
		
		brushDynamics = new DynamicsConfig();
		
		DynamicsConfig.PropertyDynamicsConfig propertyConfig = brushDynamics.getPropertyConfig(InputDynamicsType.Velocity, PropertyName.Width);
		
		propertyConfig.setMinValue(1.0f);
		propertyConfig.setMaxValue(3.3f);
		propertyConfig.setInitialValue(2.4f);
		propertyConfig.setFinalValue(1.0f);
		propertyConfig.setFunction(PropertyFunction.Sigmoid);
		propertyConfig.setFunctionParameter(0.6191646f);
		propertyConfig.setEnabled(true);
		propertyConfig.setSmoothingAlpha(0.75f);
		propertyConfig.setSmoothingBeta(0.04844f);
		
		propertyConfig = brushDynamics.getPropertyConfig(InputDynamicsType.Velocity, PropertyName.Alpha);
		
		propertyConfig.setMinValue(0.05f);
		propertyConfig.setMaxValue(0.2f);
		propertyConfig.setInitialValue(Float.NaN);
		propertyConfig.setFinalValue(Float.NaN);
		propertyConfig.setFunction(PropertyFunction.Power);
		propertyConfig.setFunctionParameter(1.0f);
		propertyConfig.setSmoothingAlpha(0.75f);
		propertyConfig.setSmoothingBeta(0.04844f);
		propertyConfig.setEnabled(false);
		
		propertyConfig = brushDynamics.getPropertyConfig(InputDynamicsType.Pressure, PropertyName.Width);
		
		propertyConfig.setMinValue(1.0f);
		propertyConfig.setMaxValue(3.3f);
		propertyConfig.setInitialValue(2.4f);
		propertyConfig.setFinalValue(1.0f);
		propertyConfig.setFunction(PropertyFunction.Sigmoid);
		propertyConfig.setFunctionParameter(0.6191646f);
		propertyConfig.setEnabled(true);
		propertyConfig.setSmoothingAlpha(0.75f);
		propertyConfig.setSmoothingBeta(0.04844f);
		
		propertyConfig = brushDynamics.getPropertyConfig(InputDynamicsType.Pressure, PropertyName.Alpha);
		
		propertyConfig.setMinValue(0.05f);
		propertyConfig.setMaxValue(0.2f);
		propertyConfig.setInitialValue(Float.NaN);
		propertyConfig.setFinalValue(Float.NaN);
		propertyConfig.setFunction(PropertyFunction.Power);
		propertyConfig.setFunctionParameter(1.0f);
		propertyConfig.setSmoothingAlpha(0.75f);
		propertyConfig.setSmoothingBeta(0.04844f);
		propertyConfig.setEnabled(false);
		
		brushDynamics.setMinMovement(4.0f);
		brushDynamics.setMinVelocity(50.0f);
		brushDynamics.setMaxVelocity(2000.0f);
		
		brushDynamics.setMinPressure(0.2f);
		brushDynamics.setMaxPressure(0.9f);
		
		getStrokeBuilder().setDynamics(brushDynamics);
	}

	public StrokeBuilder getStrokeBuilder(){
		return strokeBuilder;
	}
	
//	public int getColor() {
//		return 0xff000000;
//	}
	
	public boolean isParticleScatteringEnabled(){
		return bParticleScatteringEnabled;
	}
	
	public boolean isPressureEnabled(){
		return bPressureEnabled;
	}
	
	public ScatterStrokeBrush getScatterStrokeBrush(){
		return scatterStrokeBrush;
	}
	
	public ShapeFillStrokeBrush getShapeFillStrokeBrush(){
		return shapeFillBrush;
	}

	public void setPressureEnabled(boolean bEnabled) {
		bPressureEnabled = bEnabled;
	}

	public void setParticleScatteringEnabled(boolean bEnabled) {
		bParticleScatteringEnabled = bEnabled;
		updateBrush();
	}
	
	public void updateBrush(){
		if (isParticleScatteringEnabled()){
			getScatterStrokeBrush().allocateTextures(context, null);
			strokeBuilder.getDynamics().getPropertyConfig(InputDynamicsType.Velocity, PropertyName.Alpha).setEnabled(true);
			strokeBuilder.getDynamics().getPropertyConfig(InputDynamicsType.Pressure, PropertyName.Alpha).setEnabled(true);
			inkCanvas.getStrokePaint().setStrokeBrush(getScatterStrokeBrush());
			inkCanvas.getStrokePaint().setColorRGB(getSelectedColor());
			inkCanvas.getStrokePaint().setRoundCapBeginning(true);
			inkCanvas.getStrokePaint().setRoundCapEnding(true);
		} else {
			strokeBuilder.getDynamics().getPropertyConfig(InputDynamicsType.Velocity, PropertyName.Alpha).setEnabled(false);
			strokeBuilder.getDynamics().getPropertyConfig(InputDynamicsType.Pressure, PropertyName.Alpha).setEnabled(false);
			inkCanvas.getStrokePaint().setStrokeBrush(getShapeFillStrokeBrush());
			inkCanvas.getStrokePaint().setColorRGB(getSelectedColor());
			inkCanvas.getStrokePaint().setRoundCapBeginning(true);
			inkCanvas.getStrokePaint().setRoundCapEnding(true);
		}
	}
	
	public int getSelectedPaperIdx() {
		return selectedPaperIdx;
	}

	public void setSelectedPaperIdx(int paperIdx) {
		selectedPaperIdx = paperIdx;
	}
	
	public int getSelectedPaperResId() {
		return papers.get(selectedPaperIdx);
	}
	
	public void setColorAtIdx(int idx, int color) {
		colors.put(idx, color);
	}

	public int getColorAtIdx(int colorIdx) {
		return colors.get(colorIdx);
	}
	
	public int getSelectedColorIdx() {
		return selectedColorIdx;
	}
	
	public void setSelectedColorIdx(int colorIdx) {
		selectedColorIdx = colorIdx;
	}
	
	public int getSelectedColor() {
		return colors.get(selectedColorIdx);
	}
	
	public void setPaperResIdAtIdx(int idx, int paperResId) {
		papers.put(idx, paperResId);
	}

	public DynamicsConfig getEraserDynamics() {
		return eraserDynamics;
	}
	
	public DynamicsConfig getBrushDynamics() {
		return brushDynamics;
	}
	
	public StrokeBrush getEraserBrush() {
		return eraserBrush;
	}

	public void setInking(boolean bInking) {
		this.bInking = bInking;
	}
	
	public boolean isInking() {
		return bInking;
	}
}