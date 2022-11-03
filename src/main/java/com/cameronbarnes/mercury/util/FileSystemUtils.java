package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.core.Main;
import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.core.SavedOngoing;
import com.cameronbarnes.mercury.stock.Bin;
import org.apache.commons.lang3.RandomStringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public final class FileSystemUtils {
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void createProjectDirs() {
		
		Options.IMPORT_FOLDER.mkdirs();
		Options.PROCESS_FOLDER.mkdirs();
		Options.SAVED_ONGOING_FOLDER.mkdirs();
		
	}
	
	public static String getLastSubstring(String str, String split) {
		int num = str.lastIndexOf(split);
		return num == -1 ? str : str.substring(++num);
	}
	
	public static void moveAllFromProcessToIngest() {
		
		Arrays.stream(Objects.requireNonNull(Options.PROCESS_FOLDER.listFiles())).dropWhile(File::isDirectory).forEach(file -> {
			try {
				Path outInIngest = Path.of(Options.IMPORT_FOLDER + File.separator + file.getName());
				if (outInIngest.toFile().getAbsoluteFile().exists()) {
					// We're adding a random bit of text to the end of the file name to prevent issues with duplicates
					String txt = outInIngest.toString();
					outInIngest = Path.of(
							txt.substring(0, txt.lastIndexOf(File.separator)) + File.separator
									+ file.getName().substring(0, file.getName().lastIndexOf("."))
									+ RandomStringUtils.random(5, true, true)
									+ "." + getLastSubstring(file.getName(), ".")
												 );
				}
				Files.move(file.toPath(), outInIngest);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static Optional<File> getFileForSaveOutput() {
		
		JFileChooser chooser = new JFileChooser(new File(".\\"));
		chooser.setDialogTitle("Save Output Spreadsheet");
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));
		chooser.setSelectedFile(new File("Cycle Count " + Date.from(Instant.now()).toString().replace(":", "-")+ ".xlsx"));
		boolean validFile = false;
		while (!validFile) {
			if (chooser.showSaveDialog(null) == JFileChooser.CANCEL_OPTION) {
				return Optional.empty();
			}
			File file = chooser.getSelectedFile();
			if (file == null) {
				validFile = true;
				continue;
			}
			if (file.isDirectory())
				return Optional.of(new File(file.getAbsolutePath() + File.separator + "Cycle Count " + Date.from(Instant.now()).toString().replace(":", "-")+ ".xlsx"));
			if (!file.getName().contains(".")) { // If the name entered by the user has no extension we'll add .xlsx to make it valid
				return Optional.of(new File(file.getAbsolutePath() + ".xlsx"));
			} else {
				if (getLastSubstring(file.getName(), ".").equalsIgnoreCase("xlsx")) {
					validFile = true;
				} else {
					JOptionPane.showMessageDialog(null, "File name must end with .xlsx");
					chooser.setSelectedFile(new File("Cycle Count " + Date.from(Instant.now()).toString().replace(":", "-")+ ".xlsx"));
				}
			}
		}
		
		return Optional.ofNullable(chooser.getSelectedFile());
		
	}
	
	public static Optional<File> getDirectoryWithFileChooser() {
		
		JFileChooser chooser = new JFileChooser(Options.IMPORT_FOLDER);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return Optional.ofNullable(chooser.getSelectedFile());
		}
		else return Optional.empty();
		
	}
	
	public static void writeStringToFile(String str, File file) {
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace(); // TODO probably handle this with the HomeAPI
		}
		
		try (FileWriter writer = new FileWriter(file)) {
			
			writer.write(str);
			writer.flush();
			
		}
		catch (IOException e) {
			e.printStackTrace(); // TODO probably handle this with the HomeAPI
		}
		
	}
	
	public static Optional<String> readStringFromFile(File file) {
		
		if (file.exists() && !file.isDirectory()) {
			
			try {
				String str = new String(Files.readAllBytes(file.toPath()));
				return str.isBlank() ? Optional.empty() : Optional.of(str);
			}
			catch (IOException e) {
				e.printStackTrace(); // TODO probably handle this with the HomeAPI
			}
			
		}
		
		return Optional.empty();
		
	}
	
	public static void writeOptions(Options options) {
		writeOptions(options, new File("options.json"), new File("version.json"));
	}
	
	public static void writeOptions(Options options, File optFile, File versionFile) {
		writeStringToFile(SerializationUtils.serializeOptions(options), optFile);
		writeStringToFile(SerializationUtils.serializeVersion(Main.VERSION), versionFile);
	}
	
	public static Options readOptions() {
		return readOptions(new File("options.json"), new File("version.json"));
	}
	
	public static Options readOptions(File optionsFile, File versionFile) {
	
		Options options = readStringFromFile(optionsFile).flatMap(SerializationUtils::deserializeOptions).orElse(new Options());
		readStringFromFile(versionFile).flatMap(SerializationUtils::deserializeVersion).ifPresent(options::setVersion);
		return options;
	
	}
	
	public static void saveOngoing(List<Bin> bins) {
		saveOngoing(bins, new File(Options.SAVED_ONGOING_FOLDER.getPath() + File.separator + Date.from(Instant.now()).toString().replace(":", "-")));
	}
	
	public static void saveOngoing(List<Bin> bins, File outDir) {
		
		//Create all the files for dirs we will need
		outDir.mkdirs();
		File stockstatusDir = new File(outDir.getAbsolutePath() + File.separator + "stockstatus");
		stockstatusDir.mkdirs();
		File binsDir = new File(outDir.getAbsolutePath() + File.separator + "bins");
		binsDir.mkdirs();
		
		//Write out all the parts from bins into json files
		bins.forEach(bin -> writeStringToFile(SerializationUtils.serializeBin(bin), new File(binsDir.getAbsolutePath() + File.separator + bin.getBinNum())));
		
		// Move all the stockstatus files into a folder with the save
		Arrays.stream(Objects.requireNonNull(Options.PROCESS_FOLDER.listFiles())).dropWhile(File::isDirectory).forEach(file -> {
			try {
				Files.move(file.toPath(), Path.of(stockstatusDir.getAbsolutePath() + File.separator + file.getName()));
			}
			catch (IOException e) {
				e.printStackTrace(); // TODO handle this with the HomeAPI
			}
		});
		
	}
	
	public static boolean hasSavedSessions() {
		return Objects.requireNonNull(Options.SAVED_ONGOING_FOLDER.listFiles()).length > 0;
	}
	
	public static List<SavedOngoing> getSavedSessions() {
		
		ArrayList<SavedOngoing> savedSessions = new ArrayList<>();
		Arrays.stream(Objects.requireNonNull(Options.SAVED_ONGOING_FOLDER.listFiles()))
		      .filter(File::isDirectory).map(FileSystemUtils::getSavedSessionFromDir).filter(Optional::isPresent).map(Optional::get).forEach(savedSessions::add);
		return savedSessions;
		
	}
	
	public static Optional<SavedOngoing> getSavedSessionFromDir(File dir) {
		
		// Require tha the provided directory exists,  contains a stockstatus directory, and contains a bins directory
		if (dir.exists() && Arrays.stream(Objects.requireNonNull(dir.listFiles())).anyMatch(file -> file.getName().equals("stockstatus")) &&
				    Arrays.stream(Objects.requireNonNull(dir.listFiles())).anyMatch(file -> file.getName().equals("bins"))) {
			
			ArrayList<Bin> bins = new ArrayList<>();
			
			// Get all files in the bins subdirectory, exclude directories, read the contents of each file as a string, deserialize from string to bin, add the valid bins to the array
			Arrays.stream(Objects.requireNonNull(new File(dir.getAbsolutePath() + "/bins").listFiles()))
			      .dropWhile(File::isDirectory).map(FileSystemUtils::readStringFromFile)
			      .filter(Optional::isPresent).map(Optional::get).map(SerializationUtils::deserializeBin).filter(Optional::isPresent).map(Optional::get).forEach(bins::add);
			
			if (!bins.isEmpty())
				return Optional.of(new SavedOngoing(dir, bins));
			
		}
		
		return Optional.empty();
		
	}
	
	public static void moveFilesFromSavedSession(SavedOngoing savedOngoing) {
		
		// We'll move all the stockstatus files from the saved data to the process folder
		Arrays.stream(Objects.requireNonNull(new File(savedOngoing.getSaveDir().getAbsolutePath() + File.separator + "stockstatus").listFiles())).dropWhile(File::isDirectory).forEach(file -> {
			try {
				Files.move(file.toPath(), Path.of(Options.PROCESS_FOLDER.getPath() + File.separator + file.getName()));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		deleteDir(savedOngoing.getSaveDir());
		
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void deleteDir(File dir) {
		
		if (!dir.exists())
			return;
		if (!dir.isDirectory()) // This shouldn't ever come up, but I'd rather this not fail silently if it does
			throw new RuntimeException("Provided file must be a directory"); // TODO handle this with the HomeAPI
		Arrays.stream(Objects.requireNonNull(dir.listFiles())).filter(File::isDirectory).forEach(FileSystemUtils::deleteDir);
		Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(File::delete);
		dir.delete();
		
	}
	
}
