package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.gui.MainFrame;
import com.cameronbarnes.mercury.util.FileSystemUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
	
	public static final Version VERSION = new Version(1, 1, 0, ReleaseType.RELEASE);
	public static final boolean DEBUG = true;
	
	public static void main(String[] args) {
		
		FileSystemUtils.createProjectDirs();
		
		Options options = FileSystemUtils.readOptions();
		FileSystemUtils.readVersion().ifPresent(options::setVersion);
		
		MainFrame mainFrame = new MainFrame(options);
		Session session = new Session(mainFrame, options);
		
		if (options.setVersion(VERSION)) {
			mainFrame.showChangeLog();
		}
		
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				
				if (mainFrame.isCount() && !session.getBins().isEmpty()) {
					String[] choices = new String[]{"Save", "Don't Save"};
					int choice = JOptionPane.showOptionDialog(
							mainFrame.getRootPane(),
							"Save your work to resume later?",
							"Save?",
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
	
	public enum ReleaseType {
		ALPHA,
		BETA,
		RELEASE
	}
	
	public record Version(int major, int minor, int patch, ReleaseType label) {
		@Override
		public String toString() {
			return major + "_" + minor + "_" + patch + "_" + label.toString();
		}
		
		public String toNiceString() {
			
			return major + "." + minor + "." + patch + " " + label.toString();
			
		}
		
		public int compareTo(Version v) {
			if (label.compareTo(v.label) > 0) // I think this is the way I want to do this, prefer release versions but only with the newer version
				return -1;
			
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
	}
	
}
