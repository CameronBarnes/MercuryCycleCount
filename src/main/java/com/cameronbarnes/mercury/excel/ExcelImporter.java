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

package com.cameronbarnes.mercury.excel;

import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

public final class ExcelImporter {
	
	// The regex pattern required to parse a part from the html version of the spreadsheet
	private static final Pattern PATTERN = Pattern.compile("<td>(.+?)</td>");
	
	/**
	 * Imports a bin from a stockstatus file, either from an actual Excel workbook, or a html document pretending to be one
	 * @param file The file to import from
	 * @return an Optional Bin if the stockstatus file contains valid data
	 */
	public static Optional<Bin> importBinFromStockStatusFile(File file) {
		
		if (!file.exists())
			return Optional.empty();
		
		FileTime time;
		try {
			// We're going to get the file creation time, so that we can compare which stockstatus file is more recent if more than one for the same bin is added
			time = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
		} catch (IOException e) {
			//TODO handle this with the HomeAPI
			//I'm not sure exactly what causes this error, so I'm going to rethrow it until the home API has it covered so that I can watch for it
			time = null;
		}
		
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			
			// This throws a NotOfficeXmlFileException if the file isn't actually an Excel file, which we are handling for
			// Just catching the error if it happens probably isn't the best way to do this, it's definitely not especially clean, but it functions without issue in testing
			Workbook workbook = new XSSFWorkbook(fileInputStream);
			Sheet sheet = workbook.getSheetAt(0);
			ArrayList<Part> parts = new ArrayList<>();
			
			for (Row row: sheet) {
				getPartFromXSSFSheetRow(row).ifPresent(parts::add); //If the part is valid we'll add it to the part list
			}
			
			//If there are no valid parts, we won't return this bin object
			if (parts.isEmpty())
				return Optional.empty();
			
			//All the parts in the bin will have the same Bin Number and Warehouse values, which are the other values we need for the bin object
			return Optional.of(new Bin(parts.get(0).getBinNum(), parts.get(0).getWarehouse(), parts, time));
		
		} catch (EmptyFileException e) {
			
			// If the file is actually empty then I'm just going to delete it
			file.delete();
			return Optional.empty();
			
		} catch (NotOfficeXmlFileException | ZipException e) {
			/*
			sneaky HTMobbitses
			 So apparently CSS8 outputs an HTML file instead of a xls file, but still calls it a xls file, and Excel will read it as a xls file, but Apache POI will not
			 So we're going to have to work some regex magic to make this data useful
			 I should probably be checking for this rather than just catching it as an exception, but this seems to be the least messy way to do this
			*/

            ArrayList<Part> parts = new ArrayList<>();
			
			// The charset for this file is VERY weird, it took me hours to figure this out and this try with resources looks like crap, but it works
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(file), false), StandardCharsets.UTF_16LE))) {
				
				while (reader.ready()) {
					
					String line = reader.readLine();
					
					if (line.contains("<b>") || line.isEmpty())
						continue;
					
					getPartFromHTM_XLS_String(line).ifPresent(parts::add);
					
				}
				
			}
			catch (IOException ex) { //TODO decide if I want to handle this error with the HomeAPI
				return Optional.empty();
			}
			
			//If there are no valid parts, we won't return this bin object
			if (parts.isEmpty())
				return Optional.empty();
			
			//All the parts in the bin will have the same Bin Number and Warehouse values, which are the other values we need for the bin object
			return Optional.of(new Bin(parts.get(0).getBinNum(), parts.get(0).getWarehouse(), parts, time));
			
		} catch (IOException e) { //TODO handle this with the HomeAPI
			System.err.println("Debug: Exception in Excel Bin Importer. Exception text is as follows");
			e.printStackTrace();
			return Optional.empty();
		}
		
	}
	
	/**
	 * sneaky HTMobbitses, apparently CSS8 outputs an HTML file instead of a xls file, so this handles reading part data from that
	 * @param line This takes each new line from the HTM file
	 * @return Returns an Optional with a part object if valid, otherwise returns an empty optional
	 */
	private static Optional<Part> getPartFromHTM_XLS_String(String line) {
		
		Matcher matcher = PATTERN.matcher(line);
		
		ArrayList<MatchResult> results = new ArrayList<>();
		matcher.results().forEach(results::add);
		if (results.size() == 14) {
		
			Part part = new Part(
					results.get(0).group(1), // PartNumber
					results.get(1).group(1), // PartDescription
					results.get(2).group(1), // WareHouse
					results.get(3).group(1), // Bin
					Integer.parseInt(results.get(4).group(1)), // PhysicalQty
					Integer.parseInt(results.get(5).group(1)), // AllocatedQty
					Integer.parseInt(results.get(6).group(1)), // FreeQty
					Double.parseDouble(results.get(7).group(1)) // Cost
					);
			
			return Optional.of(part);
		
		}
		
		return Optional.empty();
	
	}
	
	/**
	 * Reads a part object in from an Excel workbook row
	 * @param row the row to read a part from
	 * @return an Optional containing a part if valid, otherwise an empty Optional
	 */
	private static Optional<Part> getPartFromXSSFSheetRow(Row row) {
		
		//First we'll validate the row as having the correct data types to be a part
		if (row.getPhysicalNumberOfCells() != 14) { //There should be 14 cells, from PartNumber to Last3MonthConsumed
			System.out.println("Debug: We're expecting exactly 14 cells for a valid row, instead we read " + row.getPhysicalNumberOfCells());
			return Optional.empty();
		}
		
		if (row.getCell(4).getCellType() != CellType.NUMERIC) { //This cell should be for PhysicalQuantity, which is an integer, if this cell type is a String then we know it's the row at the very top with the titles
			System.out.println("Debug: Cell type is not numeric where we expect the PhysicalQuantity value to be");
			return Optional.empty();
		}
		
		//I think the above checking is probably good, but I'll come back to it if it's not
		Part part = new Part(
				row.getCell(0).getStringCellValue(),
				row.getCell(1).getStringCellValue(),
				row.getCell(2).getStringCellValue(),
				row.getCell(3).getStringCellValue(),
				(int) row.getCell(4).getNumericCellValue(),
				(int) row.getCell(5).getNumericCellValue(),
				(int) row.getCell(6).getNumericCellValue(),
				row.getCell(7).getNumericCellValue()
		);
		
		return Optional.of(part);
		
	}

}
