package com.cameronbarnes.mercury.excel;

import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelImporter {
	
	private static final Pattern PATTERN = Pattern.compile("<td>(.+?)</td>");

	public static Optional<Bin> importBinFromStockStatusFile(File file) {
		
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			
			Workbook workbook = new XSSFWorkbook(fileInputStream);
			Sheet sheet = workbook.getSheetAt(0);
			ArrayList<Part> parts = new ArrayList<>();
			
			for (Row row: sheet) {
				getPartFromXSSFSheetRow(row).ifPresent(parts::add); //If the part is valid we'll add it to the partlist
			}
			
			//If there are no valid parts, we wont return this bin object
			if (parts.isEmpty())
				return Optional.empty();
			
			//All the parts in the bin will have the same Bin Number and Warehouse values, which are the other values we need for the bin object
			return Optional.of(new Bin(parts.get(0).getBinNum(), parts.get(0).getWarehouse(), parts));
		
		}
		catch (IOException e) {
			System.err.println("Debug: Exception in Excel Bin Importer. Exception text is as follows");
			e.printStackTrace();
			return Optional.empty();
		} catch (NotOfficeXmlFileException e) {
			//sneaky HTMobbitses
			//So apparently CSS outputs a HTML file instead of a xls file, but still calls it an xls file, and excel will read it as an xls file, but Apache POI will not
			//So we're going to have to work some regex magic to make this data useful
			
			ArrayList<Part> parts = new ArrayList<>();
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(file), false), StandardCharsets.UTF_16LE))) {
				
				while (reader.ready()) {
					
					String line = reader.readLine();
					//ByteBuffer bb = Charset.forName("utf-16le").encode(CharBuffer.wrap(line));
					//CharBuffer ascii = Charset.forName("US-ASCII").decode(bb);
					//line = ascii.toString();
					
					if (line.contains("<b>") || line.isEmpty())
						continue;
					
					getPartFromHTM_XLS_String(line).ifPresent(parts::add);
					
				}
				
			}
			catch (IOException ex) {
				return Optional.empty();
			}
			
			//If there are no valid parts, we wont return this bin object
			if (parts.isEmpty())
				return Optional.empty();
			
			//All the parts in the bin will have the same Bin Number and Warehouse values, which are the other values we need for the bin object
			return Optional.of(new Bin(parts.get(0).getBinNum(), parts.get(0).getWarehouse(), parts));
			
		}
		
	}
	
	/**
	 * sneaky HTMobbitses
	 * @param line This takes each new line from the HTM file
	 * @return Returns an option with a part object if valid, otherwise returns an empty optional
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
