package com.cameronbarnes.mercury.core;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Options implements IUnprotectedOptions {
	
	public static final File IMPORT_FOLDER = new File("." + File.separator + "import_stockstatus").getAbsoluteFile();
	public static final File PROCESS_FOLDER = new File("." + File.separator + "import_stockstatus" + File.separator + "process").getAbsoluteFile();
	
	public static final File SAVED_ONGOING_FOLDER = new File("." + File.separator + "saved_ongoing").getAbsoluteFile();
	
	private transient Main.Version mVersion = null;
	
	private boolean mIsAllowedWritePhysicalQuantity = false;
	private boolean mShowAllPartsProgress = false;
	
	private boolean mIsAllowedAutoAdjustment = true;
	
	private boolean mShouldCreateEmptyBinBeforeStart = false;
	
	private transient Font mFont;
	
	private int mFontSize = 12;
	
	@Override
	public boolean isAllowedWritePhysicalQuantity() {
		return mIsAllowedWritePhysicalQuantity;
	}
	
	public void setAllowedWritePhysicalQuantity(boolean allowed) {
		mIsAllowedWritePhysicalQuantity = allowed;
	}
	
	private final Map<String, Boolean> mShowPartProperties = new TreeMap<>();
	
	public Options() {
		
		//Setting up showPartProperty stuff here, we'll fill this with default values for now
		ensureAllNewPropertiesArePresent();
		generateFont();
		
	}
	
	@Override
	public Map<String, Boolean> getPartDetailSettings() {
		return mShowPartProperties;
	}
	
	public void setShowAllPartsProgress(boolean showAllPartsProgress) {
		mShowAllPartsProgress = showAllPartsProgress;
	}
	
	public boolean shouldShowAllPartsProgress() {
		return mShowAllPartsProgress;
	}
	
	public void setAllowedAutoAdjustment(boolean value) {
		mIsAllowedAutoAdjustment = value;
	}
	
	@Override
	public boolean isAllowedAutoAdjustment() {
		return  mIsAllowedAutoAdjustment;
	}
	
	public void setShouldCreateEmptyBinBeforeStart(boolean val) {
		mShouldCreateEmptyBinBeforeStart = val;
	}
	
	@Override
	public boolean shouldCreateEmptyBinBeforeStart() {
		return mShouldCreateEmptyBinBeforeStart;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (!(o instanceof Options opt))
			return false;
		
		if (mIsAllowedAutoAdjustment != opt.mIsAllowedAutoAdjustment ||
					mShowAllPartsProgress != opt.mShowAllPartsProgress ||
					mIsAllowedWritePhysicalQuantity != opt.mIsAllowedWritePhysicalQuantity
		) {
			return false;
		}
		
		if (mShowPartProperties.size() != opt.mShowPartProperties.size())
			return false;
		
		for (String key: mShowPartProperties.keySet()) {
			if (!mShowPartProperties.get(key).equals(opt.mShowPartProperties.get(key)))
				return false;
		}
		
		return true;
		
	}
	
	public void ensureAllNewPropertiesArePresent() {
		
		TreeMap<String, Boolean> expected = new TreeMap<>();
		expected.put("WareHouse", false);
		expected.put("Bin", false);
		expected.put("AllocatedQuantity", false);
		expected.put("FreeQuantity", false);
		expected.put("Comments", false);
		
		expected.forEach(mShowPartProperties::putIfAbsent);
		
		if (mVersion == null)
			mVersion = Main.VERSION;
		
	}
	
	public void generateFont() {
		
		if (mFont == null)
			mFont = UIManager.getDefaults().getFont("Table.font");
		mFont = mFont.deriveFont(Font.PLAIN, mFontSize);
		
	}
	
	@Override
	public Font getFont() {
		return mFont;
	}
	
	public void setFontSize(int size) {
		mFontSize = size;
		generateFont();
	}
	
	@Override
	public int getFontSize() {
		return mFontSize;
	}
	
	public boolean setVersion(Main.Version version) {
	
		if (mVersion.compareTo(version) < 0) {
			mVersion = version;
			return true;
		}
		
		mVersion = version;
		return false;
	
	}
	
}
