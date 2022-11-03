package com.cameronbarnes.mercury.core;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

public final class Options implements IUnprotectedOptions {
	
	public static final File IMPORT_FOLDER = new File("." + File.separator + "import_stockstatus").getAbsoluteFile();
	public static final File PROCESS_FOLDER = new File("." + File.separator + "import_stockstatus" + File.separator + "process").getAbsoluteFile();
	public static final File SAVED_ONGOING_FOLDER = new File("." + File.separator + "saved_ongoing").getAbsoluteFile();
	
	private transient Main.Version mVersion = null;
	
	private boolean mIsAllowedWritePhysicalQuantity = false;
	private boolean mShowAllPartsProgress = false;
	
	private boolean mIsAllowedAutoAdjustment = true;
	
	private transient Font mFont;
	
	private int mFontSize = 12;
	
	private String mLocale = Locale.getDefault().toString();
	private transient ResourceBundle mBundle;
	private final Map<String, Boolean> mShowPartProperties = new TreeMap<>();
	
	public Options() {
		
		//Setting up showPartProperty stuff here, we'll fill this with default values for now
		ensureAllNewPropertiesArePresent();
		generateFont();
		
	}
	
	/**
	 * @return Returns true if the user is allowed to edit the expected physical quantity value for a part
	 */
	@Override
	public boolean isAllowedWritePhysicalQuantity() {
		return mIsAllowedWritePhysicalQuantity;
	}
	
	/**
	 * @param allowed true if the user is allowed to edit the expected physical quantity value for a part
	 */
	public void setAllowedWritePhysicalQuantity(boolean allowed) {
		mIsAllowedWritePhysicalQuantity = allowed;
	}
	
	/**
	 * @return a Map < String, Boolean > containing settings for showing or not showing certain part properties in the count form
	 */
	@Override
	public Map<String, Boolean> getPartDetailSettings() {
		return mShowPartProperties;
	}
	
	/**
	 * @param showAllPartsProgress true for include all parts in all bins in the progress bar, false for only show parts in the current bin
	 */
	public void setShowAllPartsProgress(boolean showAllPartsProgress) {
		mShowAllPartsProgress = showAllPartsProgress;
	}
	
	/**
	 * @return true for include all parts in all bins in the progress bar, false for only show parts in the current bin
	 */
	@Override
	public boolean shouldShowAllPartsProgress() {
		return mShowAllPartsProgress;
	}
	
	public void setAllowedAutoAdjustment(boolean value) {
		mIsAllowedAutoAdjustment = value;
	}
	
	/**
	 * @return Returns true if the count process should automatically add an adjustment value when the counted value is not equal to the expected quantity
	 */
	@Override
	public boolean isAllowedAutoAdjustment() {
		return  mIsAllowedAutoAdjustment;
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
		
		if (!mLocale.equals(opt.mLocale))
			return false;
		
		return this.mVersion.equals(opt.mVersion);
		
	}
	
	public Main.Version getVersion() {
		return mVersion;
	}
	
	/**
	 * This function makes sure the parts properties settings have values for all the different properties after the options object is read from disk
	 */
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
	
	/**
	 * We're using this to generate a custom font object which has the desired font size instead of creating the object over and over again when we need it
	 */
	public void generateFont() {
		
		if (mFont == null)
			mFont = UIManager.getDefaults().getFont("Table.font");
		mFont = mFont.deriveFont(Font.PLAIN, mFontSize);
		
	}
	
	/**
	 * @return a font object with the set font size
	 */
	@Override
	public Font getFont() {
		return mFont;
	}
	
	public void setFontSize(int size) {
		mFontSize = size;
		generateFont();
	}
	
	/**
	 * @return the set font size as an integer
	 */
	@Override
	public int getFontSize() {
		return mFontSize;
	}
	
	/**
	 * Sets the current version of the application, pretty sure this should always just be the current version that we're setting, so I can probably remove the parameter later
	 * //TODO look into removing the parameter later if it doesn't really get used
	 * @param version The new or current version of the application to store in the options object
	 * @return if the previous version was less than the current or new version
	 */
	public boolean setVersion(Main.Version version) {
	
		if (mVersion.compareTo(version) < 0) {
			mVersion = version;
			return true;
		}
		
		mVersion = version;
		return false;
	
	}
	
	public void setLocale(String locale) {
		
		Locale.setDefault(Locale.forLanguageTag(locale));
		mLocale = locale;
		
	}
	
	public ResourceBundle getBundle() {
		return getBundle(Locale.forLanguageTag(mLocale));
	}
	
	public ResourceBundle getBundle(Locale locale) {
		
		if (mBundle != null && mBundle.getLocale().equals(locale)) {
			return mBundle;
		}
		
		try {
			mBundle = ResourceBundle.getBundle("labels", locale);
		} catch (Exception e) {
			mBundle = ResourceBundle.getBundle("labels");
		}
		return mBundle;
		
	}
	
	public static ResourceBundle getDefaultBundle() {
		return ResourceBundle.getBundle("labels");
	}
	
}
