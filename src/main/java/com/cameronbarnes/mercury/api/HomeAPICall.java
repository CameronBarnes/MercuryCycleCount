package com.cameronbarnes.mercury.api;

public abstract class HomeAPICall {
	private final CallType mCallType;
	protected String mText;
	protected String mVersionText;
	protected String[] mObjects;
	protected String[] mTags;
	
	protected HomeAPICall(CallType type, String versionText) {
		mCallType = type;
		mVersionText = versionText;
	}
	
	public CallType getCallType() {
		return mCallType;
	}

}
