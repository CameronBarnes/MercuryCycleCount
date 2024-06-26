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

import com.cameronbarnes.mercury.gui.MainFrame;
import com.cameronbarnes.mercury.util.FileSystemUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

public class Main {
	
	public static final Version VERSION = new Version(1, 2, 2, ReleaseType.BETA);
	public static final boolean DEBUG = false;
	
	public static void main(String[] args) {
		
		FileSystemUtils.createProjectDirs();
		
		Options options = FileSystemUtils.readOptions();
		
		MainFrame mainFrame = new MainFrame(options);
		Session session = new Session(mainFrame, options);
		
		if (options.setVersion(VERSION)) {
			mainFrame.showChangeLog();
		}
		
		final ResourceBundle bundle = options.getBundle();
		
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				
				if (mainFrame.isCount() && !session.getBins().isEmpty()) {
					String[] choices = new String[]{bundle.getString("word_save"), bundle.getString("word_dont_save")};
					int choice = JOptionPane.showOptionDialog(
							mainFrame.getRootPane(),
							bundle.getString("question_save_work"),
							bundle.getString("query_save"),
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							choices,
							choices[0]
					);
					if (choice == 0) {
						FileSystemUtils.saveOngoing(session.getBins());
					} else {
						FileSystemUtils.moveAllFromProcessToIngest();
					}
				} else {
					FileSystemUtils.moveAllFromProcessToIngest();
				}
				
				FileSystemUtils.writeOptions(options);
				System.exit(0);
			}
		});
		
		session.mainMenu();
	
	}
	
	/**
	 * The Release Type, RELEASE for full releases, ALPHA or BETA for development versions
	 */
	public enum ReleaseType {
		ALPHA,
		BETA,
		RELEASE
	}
	
	/**
	 *
	 * @param major The major version number
	 * @param minor The minor version number
	 * @param patch The patch version number
	 * @param label The release type, RELEASE for full releases, ALPHA or BETA for development versions
	 */
	public record Version(int major, int minor, int patch, ReleaseType label) {
		@Override
		public String toString() {
			return major + "_" + minor + "_" + patch + "_" + label.toString();
		}
		
		public String toNiceString() {
			
			return major + "." + minor + "." + patch + " " + label.toString();
			
		}
		
		public int compareTo(Version v) {
			
			if (v.major > major)
				return -1;
			else if (v.major < major)
				return 1;
			
			if (v.minor > minor)
				return -1;
			else if (v.minor < minor)
				return 1;
			
			if(v.patch > patch)
				return -1;
			else if (v.patch < patch)
				return 1;
			
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			
			if (!(o instanceof Version version))
				return false;
			
			return this.compareTo(version) == 0;
			
		}
		
	}
	
}
