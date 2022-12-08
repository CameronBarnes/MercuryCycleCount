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
import com.cameronbarnes.mercury.util.HomeAPIUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.OptionalInt;

public final class ExcelExporter {
	
	/**
	 * Exports the count to an output Excel file
	 * @param bins the bins to export
	 * @param out the file to write to
	 * @return if the operation was successful
	 */
	public static boolean exportCycleCount(List<Bin> bins, File out) {
		
		if (bins == null || bins.isEmpty() || bins.stream().allMatch(Bin::isEmpty))
			return false;
		
		try (Workbook workbook = new XSSFWorkbook()) {
		
			for (Bin bin: bins) {
				Sheet sheet = workbook.createSheet(bin.getBinNum());
				exportBinToSheet(sheet, bin);
			}
			
			try (FileOutputStream outputStream = new FileOutputStream(out)) {
				workbook.write(outputStream);
			}
		
		} catch (Exception e) {
			// If this throws an exception I as a developer probably want to know about it, so I'm going to prompt the user to send the information to me
			HomeAPIUtils.handleExcelExporterError(e, out, bins);
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * Each bin in the output spreadsheet should be on its own sheet, this function fills the sheet for that bin with the relevant data
	 * @param sheet the sheet to write to
	 * @param bin the bin to output to the sheet
	 */
	private static void exportBinToSheet(Sheet sheet, Bin bin) {
		
		OptionalInt longestPartDescription = bin.getParts().stream().map(Part::getPartDescription).mapToInt(String::length).max(); // Get the length of the longest part description in this bin
		int partDescriptionCharWidth; // This is the default value, though I doubt it'll ever get used at this point
		if (longestPartDescription.isEmpty() || longestPartDescription.getAsInt() <  15) {
			partDescriptionCharWidth = 18;
		} else {
			partDescriptionCharWidth = longestPartDescription.getAsInt() + 5;
		}
		
		boolean hasComments = bin.getParts().stream().anyMatch(Part::hasComments);
		OptionalInt longestComment = bin.getParts().stream().map(Part::getComments).mapToInt(String::length).max();
		int partCommentsWidth = 0;
		if (longestComment.isPresent()) {
			partCommentsWidth = longestComment.getAsInt() + 5;
		}
		
		// Set up the sheet
		sheet.setColumnWidth(0, 13 * 256); // PartNumber
		sheet.setColumnWidth(1, partDescriptionCharWidth * 256); // PartDescription
		sheet.setColumnWidth(2, 15 * 256); // WareHouse
		sheet.setColumnWidth(3, 11 * 256); // Bin
		sheet.setColumnWidth(4, 12 * 256); // PhysicalQty
		sheet.setColumnWidth(5, 12 * 256); // CountedQty
		sheet.setColumnWidth(6, 7 * 256);  // Cost
		sheet.setColumnWidth(7, 12 * 256); // Adjustment
		if (hasComments) {
			sheet.setColumnWidth(8, partCommentsWidth * 256); // Part Comments
		}
		
		// Set up the header
		CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		headerStyle.setBorderTop(BorderStyle.THIN);
		
		XSSFFont font = ((XSSFWorkbook) sheet.getWorkbook()).createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		
		Row header = sheet.createRow(0);
		
		Cell h0 = header.createCell(0, CellType.STRING);
		h0.setCellValue("PartNumber");
		h0.setCellStyle(headerStyle);
		
		Cell h1 = header.createCell(1, CellType.STRING);
		h1.setCellValue("PartDescription");
		h1.setCellStyle(headerStyle);
		
		Cell h2 = header.createCell(2, CellType.STRING);
		h2.setCellValue("WareHouse");
		h2.setCellStyle(headerStyle);
		
		Cell h3 = header.createCell(3, CellType.STRING);
		h3.setCellValue("Bin");
		h3.setCellStyle(headerStyle);
		
		Cell h4 = header.createCell(4, CellType.STRING);
		h4.setCellValue("PhysicalQty");
		h4.setCellStyle(headerStyle);
		
		Cell h5 = header.createCell(5, CellType.STRING);
		h5.setCellValue("CountedQty");
		h5.setCellStyle(headerStyle);
		
		Cell h6 = header.createCell(6, CellType.STRING);
		h6.setCellValue("Cost");
		h6.setCellStyle(headerStyle);
		
		Cell h7 = header.createCell(7, CellType.STRING);
		h7.setCellValue("Adjustment");
		h7.setCellStyle(headerStyle);
		
		// We don't need a column for Comment data if there are no comments
		if (hasComments) {
			Cell h8 = header.createCell(8, CellType.STRING);
			h8.setCellValue("Comments");
			h8.setCellStyle(headerStyle);
		}
		
		if (bin.isEmpty()) { // We're going to display some custom data for this
			CellStyle noStockStyle = sheet.getWorkbook().createCellStyle();
			noStockStyle.setBorderBottom(BorderStyle.THIN);
			noStockStyle.setBorderLeft(BorderStyle.THIN);
			noStockStyle.setBorderRight(BorderStyle.THIN);
			noStockStyle.setBorderTop(BorderStyle.THIN);
			XSSFFont bigFont = ((XSSFWorkbook) sheet.getWorkbook()).createFont();
			bigFont.setBold(true);
			bigFont.setFontHeightInPoints((short) 36);
			noStockStyle.setFont(bigFont);
			
			Row row = sheet.createRow(7);
			Cell cell = row.createCell(0 , CellType.STRING);
			cell.setCellValue("NO AVAILABLE STOCK ON " + bin.getBinNum().toUpperCase());
			cell.setCellStyle(noStockStyle);
			sheet.addMergedRegion(CellRangeAddress.valueOf("A8:I8"));
			
		} else {
			// Get all the parts for the bin and add them to the sheet
			for (int i = 0; i < bin.getParts().size(); i++) {
				
				Row row = sheet.createRow(i + 1);
				exportPartToRow(row, bin.getParts().get(i), hasComments);
				
			}
		}
	
	}
	
	/**
	 * Each part in the bin should be displayed on a separate row in the sheet, this function fills that row with data from the part
	 * @param row the row to output data to
	 * @param part the part to output data from
	 * @param comment if any part in this bin has a comment, as if it does the comment column needs to have a border drawn for the cell even if it's empty
	 */
	private static void exportPartToRow(Row row, Part part, boolean comment) {
		
		CellStyle style = row.getSheet().getWorkbook().createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		
		Cell h0 = row.createCell(0, CellType.STRING);
		h0.setCellValue(part.getPartNumber());
		h0.setCellStyle(style);
		
		Cell h1 = row.createCell(1, CellType.STRING);
		h1.setCellValue(part.getPartDescription());
		h1.setCellStyle(style);
		
		Cell h2 = row.createCell(2, CellType.STRING);
		h2.setCellValue(part.getWarehouse());
		h2.setCellStyle(style);
		
		Cell h3 = row.createCell(3, CellType.STRING);
		h3.setCellValue(part.getBinNum());
		h3.setCellStyle(style);
		
		Cell h4 = row.createCell(4, CellType.NUMERIC);
		h4.setCellValue(part.getPhysicalQuantity());
		h4.setCellStyle(style);
		
		Cell h5 = row.createCell(5, CellType.NUMERIC);
		h5.setCellValue(part.getCountedQuantity());
		h5.setCellStyle(style);
		
		Cell h6 = row.createCell(6, CellType.NUMERIC);
		h6.setCellValue(part.getCost());
		h6.setCellStyle(style);
		
		Cell h7 = row.createCell(7, CellType.NUMERIC);
		h7.setCellValue(part.getAdjustment());
		h7.setCellStyle(style);
		
		if (comment) {
			Cell h8 = row.createCell(8, CellType.STRING);
			h8.setCellValue(part.getComments());
			h8.setCellStyle(style);
		}
	
	}
	
}
