package com.wacom.toolsconfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.pinaround.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wacom.ink.utils.Utils;

public class ExportDialogFragment extends DialogFragment {
	private MainActivity activity;
	private CanvasModel canvasModel;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		activity = (MainActivity)getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		//builder.setView(inflater.inflate(R.layout.exportdialog, null));

		builder.setView(inflater.inflate(R.layout.exportdialog, null))
		.setTitle(R.string.export_dialog_title)
		.setPositiveButton(R.string.export_dialog_btn_ok, null)
		.setNegativeButton(R.string.export_dialog_btn_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ExportDialogFragment.this.getDialog().cancel();
			}
		});      
		final AlertDialog alertDialog = builder.create();
		
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dlg) {
				Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String filename = Environment.getExternalStorageDirectory() + "/" + "toolconfig.json";
						try {
							String value;
							String name;
							int identifier;
							value = ((TextView)alertDialog.findViewById(R.id.export_dialog_tool_name)).getText().toString();
							name = value;
							
							value = ((TextView)alertDialog.findViewById(R.id.export_dialog_tool_identifier)).getText().toString();
							try{
								identifier = Integer.parseInt(value);
							} catch (NumberFormatException ex){
								identifier = 0;
							}
							
							if (name==null || name.trim().equals("") || identifier<0){
								//TODO: ERROR
								Toast.makeText(activity, "Invalid tool name or identifier.", Toast.LENGTH_LONG).show();
							} else {
								OutputStream os = new FileOutputStream(filename);
								//canvasModel.getToolSet().saveTool(activity, canvasModel.getDefaultTool(), identifier, name, os);
								os.close();
								Utils.startIntentSendFile(getActivity(), getResources().getString(R.string.export_intent_chooser_title), Uri.fromFile(new File(filename)), "*/*", "");
								alertDialog.dismiss();
							}
						} catch (Exception e) {
							Toast.makeText(activity, "Error while generating and/or sending the tool config file.", Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
						
					}
				});
			}
		});
		return alertDialog;
	}

	public void setModel(CanvasModel canvasModel) {
		this.canvasModel = canvasModel;
	}
	

}