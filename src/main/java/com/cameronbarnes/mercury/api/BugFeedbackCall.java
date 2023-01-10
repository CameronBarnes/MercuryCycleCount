package com.cameronbarnes.mercury.api;

import com.cameronbarnes.mercury.core.Main;
import com.cameronbarnes.mercury.core.Session;

public class BugFeedbackCall extends HomeAPICall {
	
	public BugFeedbackCall(String text, Session session, boolean bug) {
		
		super(bug ? CallType.BUG : CallType.FEEDBACK, Main.VERSION.toNiceString());
		
		if (bug && session.getUnprotectedOptions().isAllowedSendSessionDataToHomeAPI()) {
			
			this.mObjects = new String[]{session.getBins().toString(), session.getUnprotectedOptions().getRawOptionsData()};
			this.mText = text;
			this.mTags = new String[]{"Bug", "Session Data"};
			
		} else if (bug) {
			
			this.mObjects = new String[]{session.getUnprotectedOptions().getRawOptionsData()};
			this.mText = text;
			this.mTags = new String[]{"Bug"};
			
		}else {
		
			this.mText = text;
			this.mTags = new String[]{"Feedback"};
		
		}
		
	}
	
}
