package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.stock.Bin;

import java.io.File;
import java.util.List;

public class HomeAPIUtils {
	
	public static void sendFeedback(String text) {
	
		if (text == null || text.isBlank()) {
			return;
		}
		
		
	
	}
	
	public static void handleExcelExporterError(Exception e, File fileOut, List<Bin>bins) {
	
	
	
	}
	
	public static void sendBugReport(String text, Session session) {
	
	
	
	}
	
}
