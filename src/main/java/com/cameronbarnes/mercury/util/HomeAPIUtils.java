/*
 *     Copyright (c) 2022.  Cameron Barnes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.api.BugFeedbackCall;
import com.cameronbarnes.mercury.api.HomeAPICall;
import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.stock.Bin;

import java.io.File;
import java.util.List;

public class HomeAPIUtils {
	
	public static void sendFeedback(String text, Session session) {
	
		if (text == null || text.isBlank()) {
			return;
		}
		
		HomeAPICall apiCall = new BugFeedbackCall(text, session, false);
	
	}
	
	public static void handleExcelExporterError(Exception e, File fileOut, List<Bin>bins) {
	
	
	
	}
	
	public static void handleStandardException(Exception e, Session session) {
	
	
	
	}
	
	public static void sendBugReport(String text, Session session) {
		
		if (text == null || text.isBlank()) {
			return;
		}
		
		HomeAPICall apiCall = new BugFeedbackCall(text, session, true);
	
	}
	
}
