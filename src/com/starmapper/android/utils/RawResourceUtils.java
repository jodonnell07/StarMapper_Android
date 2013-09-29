package com.starmapper.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import android.content.Context;

public abstract class RawResourceUtils {

	// Text file reading method
	public static String readTextFileFromRawResource(final Context context, final int resourceID) {
		final InputStream inputStream = context.getResources().openRawResource(resourceID);
		final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		final StringBuilder body = new StringBuilder();
		
		String nextLine;
		
		try {
			while ((nextLine = bufferedReader.readLine()) != null) {
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e) {
			return null;
		}
		return body.toString();
	}
	
	public static ArrayList<String> genConstellationDataArrayFromRawResource(final Context context, final int resourceID) {
		ArrayList<String> ConstellationsArrayList = new ArrayList<String>();
		String ConstellationSourceData = readTextFileFromRawResource(context, resourceID);
		BufferedReader bufReader = new BufferedReader(new StringReader(ConstellationSourceData));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				if (line.matches("^#.*")) {
				    continue;
				} else {
				    ConstellationsArrayList.add(line);
				}
			}
		}
		catch (IOException e) {
			return null;
		}
		return ConstellationsArrayList;
	}
}
