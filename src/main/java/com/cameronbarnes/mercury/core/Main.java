package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.gui.MainFrame;
import com.cameronbarnes.mercury.util.FileSystemUtils;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
	
	public static void main(String[] args) {
		
		FileSystemUtils.createProjectDirs();
		
		Options options = FileSystemUtils.readOptions();
		
		MainFrame mainFrame = new MainFrame(options);
		Session session = new Session(mainFrame, options);
		
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
	
}
