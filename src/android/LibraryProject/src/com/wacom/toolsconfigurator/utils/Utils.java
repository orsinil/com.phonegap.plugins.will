package com.wacom.toolsconfigurator.utils;

import android.view.View;
import android.view.ViewGroup;

public class Utils {
	public static void setViewEnabled(View view, boolean enabled) {
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				setViewEnabled(((ViewGroup) view).getChildAt(i), enabled);
			}
		}
		view.setEnabled(enabled);
	}

	public static View getView(View container, int parentId, int childId) {
		View parent = container.findViewById(parentId);
		if (parent != null) {
			return parent.findViewById(childId);
		}
		return null;
	}

	public static void setViewsWithTagEnabled(View view, String tag, boolean enabled) {
		if (view == null || tag == null)
			return;

		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				View childView = group.getChildAt(i);
				if (tag.equals(childView.getTag())) {
					Utils.setViewEnabled(childView, enabled);
				} else {
					Utils.setViewsWithTagEnabled(childView, tag, enabled);
				}
			}
		} else if (tag.equals(view.getTag())) {
			view.setEnabled(enabled);
		}
	}
}
