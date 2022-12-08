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

package com.cameronbarnes.mercury.core;

import java.awt.*;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public interface IUnprotectedOptions {
	
	ResourceBundle getBundle();
	ResourceBundle getBundle(Locale locale);
	
	/**
	 * @return Returns true if the user is allowed to edit the expected physical quantity value for a part
	 */
	boolean isAllowedWritePhysicalQuantity();
	
	/**
	 * @return a Map<String, Boolean> containing settings for showing or not showing certain part properties in the count form
	 */
	Map<String, Boolean> getPartDetailSettings();
	
	/**
	 * @return true for include all parts in all bins in the progress bar, false for only show parts in the current bin
	 */
	boolean shouldShowAllPartsProgress();
	
	/**
	 * @return Returns true if the count process should automatically add an adjustment value when the counted value is not equal to the expected quantity
	 */
	boolean isAllowedAutoAdjustment();
	
	/**
	 * @return a font object with the set font size
	 */
	Font getFont();
	
	/**
	 * @return the set font size as an integer
	 */
	int getFontSize();
	
}
