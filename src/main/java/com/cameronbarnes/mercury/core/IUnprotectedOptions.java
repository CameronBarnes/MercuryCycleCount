package com.cameronbarnes.mercury.core;

import java.awt.*;
import java.util.Map;

public interface IUnprotectedOptions {
	
	boolean isAllowedWritePhysicalQuantity();
	
	Map<String, Boolean> getPartDetailSettings();
	
	boolean shouldShowAllPartsProgress();
	
	boolean isAllowedAutoAdjustment();
	
	boolean shouldCreateEmptyBinBeforeStart();
	
	Font getFont();
	
	int getFontSize();
	
}
