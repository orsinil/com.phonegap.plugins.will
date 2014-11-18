package com.wacom.toolsconfigurator;
import <app-package>.R;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TextureToolSettings
{
	private SparseArray<Texture> fillTextures;
	private SparseArray<Texture> shapeTextures;
	
	private CanvasModel canvasModel;
	private ViewGroup container;
	private Activity activity;
	
	public TextureToolSettings(Activity activity, CanvasModel canvasModel, View container)
	{
		fillTextures = new SparseArray<Texture>();
		shapeTextures = new SparseArray<Texture>();
		
		this.canvasModel = canvasModel;
		this.container = (ViewGroup)container;
		this.activity = activity;
		
		((TextView)container.findViewById(R.id.textureFill).findViewById(R.id.title)).setText(R.string.texture_fill);
		((TextView)container.findViewById(R.id.textureShape).findViewById(R.id.title)).setText(R.string.texture_shape);
		
		fillTextures.put(R.id.texture1, new Texture("fill 1", "tool_fill_6.png", container.findViewById(R.id.textureFill).findViewById(R.id.texture1)));
		fillTextures.put(R.id.texture2, new Texture("fill 2", "tool_fill_5.png", container.findViewById(R.id.textureFill).findViewById(R.id.texture2)));
		fillTextures.put(R.id.texture3, new Texture("fill 3", "tool_fill_3.png", container.findViewById(R.id.textureFill).findViewById(R.id.texture3)));
		
		shapeTextures.put(R.id.texture1, new Texture("shape 1", "tool_shape_6.png", container.findViewById(R.id.textureShape).findViewById(R.id.texture1)));
		shapeTextures.put(R.id.texture2, new Texture("shape 2", "tool_shape_5.png", container.findViewById(R.id.textureShape).findViewById(R.id.texture2)));
		shapeTextures.put(R.id.texture3, new Texture("shape 3", "tool_shape_4.png", container.findViewById(R.id.textureShape).findViewById(R.id.texture3)));
		
		try
		{
			for (int i = 0; i < fillTextures.size(); i++)
			{
				int key = fillTextures.keyAt(i);
				InputStream fillInputStream;
				
				fillInputStream = activity.getAssets().open(((Texture)fillTextures.get(key)).asset);
				
				((ImageView)container.findViewById(R.id.textureFill).findViewById(key).findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeStream(fillInputStream));
				((TextView)container.findViewById(R.id.textureFill).findViewById(key).findViewById(R.id.name)).setText(fillTextures.get(key).name);
				
				container.findViewById(R.id.textureFill).findViewById(key).setOnClickListener(onTextureFillClicked);
				
				if (fillTextures.get(key).asset.equals(canvasModel.getScatterStrokeBrush().getFillTextureFilename())) {
					selectTexture(fillTextures, key);
				}
			}
			
			for (int i = 0; i < shapeTextures.size(); i++)
			{
				int key = shapeTextures.keyAt(i);
				InputStream shapeInputStream;
				
				shapeInputStream = activity.getAssets().open(((Texture)shapeTextures.get(key)).asset);
				
				((ImageView)container.findViewById(R.id.textureShape).findViewById(key).findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeStream(shapeInputStream));
				((TextView)container.findViewById(R.id.textureShape).findViewById(key).findViewById(R.id.name)).setText(shapeTextures.get(key).name);
				
				container.findViewById(R.id.textureShape).findViewById(key).setOnClickListener(onTextureShapeClicked);
				
				if (shapeTextures.get(key).asset.equals(canvasModel.getScatterStrokeBrush().getShapeTextureFilename())) {
					selectTexture(shapeTextures, key);
				}
			}
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private OnClickListener onTextureFillClicked  = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Log.e("XXX", "JJJX " + v.getId() + " / " + v.isEnabled());
			if(v.isEnabled())
			{
				String fillTextureFilename = fillTextures.get(R.id.texture1).asset;
				
				switch (v.getId())
				{
					case R.id.texture1:
						fillTextureFilename = fillTextures.get(R.id.texture1).asset;
						break;
					case R.id.texture2:
						fillTextureFilename = fillTextures.get(R.id.texture2).asset;
						break;
					case R.id.texture3:
						fillTextureFilename = fillTextures.get(R.id.texture3).asset;
						break;
				}
				
				canvasModel.getScatterStrokeBrush().setFillTextureFilename(fillTextureFilename);
				canvasModel.getScatterStrokeBrush().allocateTextures(activity, null);
				selectTexture(fillTextures, v.getId());
			}
		}
	};
	
	private OnClickListener onTextureShapeClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v.isEnabled())
			{
				String shapeTextureFilename = shapeTextures.get(R.id.texture1).asset;
				
				switch (v.getId())
				{
					case R.id.texture1:
						shapeTextureFilename = shapeTextures.get(R.id.texture1).asset;
						break;
					case R.id.texture2:
						shapeTextureFilename = shapeTextures.get(R.id.texture2).asset;
						break;
					case R.id.texture3:
						shapeTextureFilename = shapeTextures.get(R.id.texture3).asset;
						break;
				}
				
				canvasModel.getScatterStrokeBrush().setShapeTextureFilename(shapeTextureFilename);
				canvasModel.getScatterStrokeBrush().allocateTextures(activity, null);
				
				selectTexture(shapeTextures, v.getId());
			}
		}
	};
	
	private void selectTexture(SparseArray<Texture> textures, int selectedKey)
	{
		for (int i = 0; i < textures.size(); i++)
		{
			int key = textures.keyAt(i);
			
			if(key == selectedKey)
			{
				textures.get(key).view.setBackgroundColor(0xFF0EBFE9);
			}
			else
			{
				textures.get(key).view.setBackgroundColor(Color.TRANSPARENT);
			}
		}
	}
}

class Texture
{
	public String name;
	public String asset;
	public View view;
	
	public Texture(String name, String asset, View view)
	{
		this.name = name;
		this.asset = asset;
		this.view = view;
	}
}
