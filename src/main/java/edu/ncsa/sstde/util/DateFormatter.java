package edu.ncsa.sstde.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DateFormatter {
	private static SimpleDateFormat FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS");
	public static DateFormat getInstance(){
		return FORMAT;
	}
}
