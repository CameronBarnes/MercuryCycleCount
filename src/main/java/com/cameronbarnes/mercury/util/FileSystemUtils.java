package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.core.Main;
import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.core.SavedOngoing;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.RandomStringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public final class FileSystemUtils {
	
	public static void createProjectDirs() {
		
		Options.IMPORT_FOLDER.mkdirs();
		Options.PROCESS_FOLDER.mkdirs();
		Options.SAVED_ONGOING_FOLDER.mkdirs();
		
	}
	
	public static String getLastSubstring(String str, String split) {
		int num = str.lastIndexOf(split);
		return num == -1 ? str : str.substring(++num);
	}
	
	public static void clearImportFolder() {
		
		Arrays.stream(Objects.requireNonNull(Options.IMPORT_FOLDER.listFiles())).dropWhile(File::isDirectory).forEach(File::delete);
	}
	
	public static void clearProcessFolder() {
		
		Arrays.stream(Objects.requireNonNull(Options.PROCESS_FOLDER.listFiles())).dropWhile(File::isDirectory).forEach(File::delete);
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
			if (getLastSubstring(file.getName(), ".").equalsIgnoreCase("xlsx")) {
				validFile = true;
			} else {
				JOptionPane.showMessageDialog(null, "File name must end with .xlsx");
				chooser.setSelectedFile(new File("Cycle Count " + Date.from(Instant.now()).toString().replace(":", "-")+ ".xlsx"));
			}
		}
		
		return Optional.ofNullable(chooser.getSelectedFile());
		
	}
	
	public static File getDirectoryWithFileChooser() { // Change this to use a Optional with Optional.ofNullable
		
		JFileChooser chooser = new JFileChooser(Options.IMPORT_FOLDER);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		else return Options.IMPORT_FOLDER;
		
	}
	
	public static void writeOptions(Options options) {
		writeOptions(options, new File("options.json"));
		writeVersion(new File("version.json"));
	}
	
	public static void writeOptions(Options options, File file) {
		
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try (FileWriter writer = new FileWriter(file)) {
			
			Gson gson = new Gson();
			writer.write(gson.toJson(options));
			writer.flush();
			
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static void writeVersion(File file) {
		
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try (FileWriter writer = new FileWriter(file)) {
			
			Gson gson = new Gson();
			writer.write(gson.toJson(Main.VERSION));
			writer.flush();
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Optional<Main.Version> readVersion () {
		return readVersion(new File("version.json"));
	}
	
	public static Optional<Main.Version> readVersion(File file) {
		
		try {
			
			String value = new String(Files.readAllBytes(Path.of(file.getAbsolutePath())));
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Main.Version.class, new VersionDeserializer());
			Gson gson = builder.create();
			return Optional.ofNullable(gson.fromJson(value, Main.Version.class));
			
		}
		catch (NoSuchFileException ignored) {}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return Optional.empty();
		
	}
	
	public static Options readOptions() {
		return readOptions(new File("options.json"));
	}
	
	public static Options readOptions(File in) {
		
		try {
			
			String file = new String(Files.readAllBytes(in.toPath()));
			Gson gson = new Gson();
			Options options = gson.fromJson(file, Options.class);
			options.generateFont();
			options.ensureAllNewPropertiesArePresent();
			return options;
			
		}
		catch (IOException ignored) {} // This is fine because then we can just return a new options object
		
		return new Options();
		
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
		
		// Write out all the parts from bins into json files
		Gson gson =  new GsonBuilder().serializeNulls().create();
		bins.forEach(bin -> {
			File out = new File(binsDir.getAbsolutePath() + File.separator + bin.getBinNum());
			try {
				out.createNewFile();
				try (FileWriter writer = new FileWriter(out)) {
					
					writer.write(gson.toJson(new BinData(bin.getBinNum(), bin.getWarehouse(), gson.toJson(bin.getParts()))));
					writer.flush();
					
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
		// Move all the stockstatus files into a folder with the save
		Arrays.stream(Objects.requireNonNull(Options.PROCESS_FOLDER.listFiles())).dropWhile(File::isDirectory).forEach(file -> {
			try {
				Files.move(file.toPath(), Path.of(stockstatusDir.getAbsolutePath() + File.separator + file.getName()));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	public static boolean hasSavedSessions() {
		
		return Objects.requireNonNull(Options.SAVED_ONGOING_FOLDER.listFiles()).length > 0;
		
	}
	
	private static Optional<Bin> loadBinFromFile(File file) {
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(BinData.class, new BinDataDeserializer());
		Gson gson = builder.create();
		try {
			String json = new String(Files.readAllBytes(file.toPath()));
			BinData binData = gson.fromJson(json, BinData.class);
			return Optional.of(binData.toBin());
		}
		catch (IOException e) {
			return Optional.empty();
		}
		
	}
	
	public static List<SavedOngoing> getSavedSessions() {
		
		ArrayList<SavedOngoing> savedSessions = new ArrayList<>();
		Arrays.stream(Objects.requireNonNull(Options.SAVED_ONGOING_FOLDER.listFiles()))
		      .filter(File::isDirectory).map(FileSystemUtils::getSavedSessionFromDir).filter(Optional::isPresent).map(Optional::get).forEach(savedSessions::add);
		return savedSessions;
		
	}
	
	public static Optional<SavedOngoing> getSavedSessionFromDir(File dir) {
		
		if (Arrays.stream(Objects.requireNonNull(dir.listFiles())).anyMatch(file -> file.getName().equals("stockstatus")) &&
				    Arrays.stream(Objects.requireNonNull(dir.listFiles())).anyMatch(file -> file.getName().equals("bins"))) {
			
			ArrayList<Bin> bins = new ArrayList<>();
			
			Arrays.stream(Objects.requireNonNull(new File(dir.getAbsolutePath() + "/bins").listFiles()))
			      .dropWhile(File::isDirectory).forEach(file -> loadBinFromFile(file).ifPresent(bins::add));
			
			if (!bins.isEmpty())
				return Optional.of(new SavedOngoing(
						dir,
						(int) Arrays.stream(Objects.requireNonNull(new File(dir.getAbsolutePath() + File.separator + "stockstatus").listFiles())).dropWhile(File::isDirectory).count(),
						bins)
				);
			
		}
		
		return Optional.empty();
		
	}
	
	public static void moveFilesFromSavedSession(SavedOngoing savedOngoing) {
		
		// We'll move all the stockstatus files from the saveed data to the process folder
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
	
	public static void deleteDir(File dir) {
		
		if (!dir.exists())
			return;
		Arrays.stream(Objects.requireNonNull(dir.listFiles())).filter(File::isDirectory).forEach(FileSystemUtils::deleteDir);
		Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(File::delete);
		dir.delete();
		
	}
	
	private record BinData(String BinNumber, String WareHouse, String Parts) {
		
		Bin toBin() {
			
			Gson gson = new Gson();
			Type type = new TypeToken<ArrayList<Part>>(){}.getType();
			ArrayList<Part> parts = gson.fromJson(Parts, type);
			return new Bin(BinNumber, WareHouse, parts);
			
		}
		
	}
	
	private static final class BinDataDeserializer implements JsonDeserializer<BinData> {
		@Override
		public BinData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			return new BinData(jsonObject.get("BinNumber").getAsString(), jsonObject.get("WareHouse").getAsString(), jsonObject.get("Parts").getAsString());
		}
		
	}
	
	private static final class VersionDeserializer implements JsonDeserializer<Main.Version> {
		@Override
		public Main.Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			return new Main.Version(jsonObject.get("major").getAsInt(), jsonObject.get("minor").getAsInt(), jsonObject.get("patch").getAsInt(), Main.ReleaseType.valueOf(jsonObject.get("label").getAsString()));
		}
		
	}
	
}
