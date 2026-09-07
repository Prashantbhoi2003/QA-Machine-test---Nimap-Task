package com.nimap.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads rows from an Excel sheet and returns them as Object[][],
 * ready to be fed straight into a TestNG @DataProvider.
 * This is how "parameterization" for Login / Add Customer is achieved.
 *
 * NOTE: this resolves the file THREE different ways (in order) so that it
 * works no matter what the IDE's "working directory" happens to be set to
 * (Eclipse/IntelliJ run configs are a common source of silently-wrong
 * relative paths, which is what was happening before this fix):
 *   1) filePath exactly as given, resolved against the current working dir
 *   2) the same filename looked up on the test classpath (this always works
 *      regardless of working directory, since Maven copies
 *      src/test/resources onto the classpath)
 *   3) a clear, loud error if neither resolves - never silently continues
 *      with an empty/blank sheet.
 */
public class ExcelUtils {

    public static Object[][] readSheet(String filePath, String sheetName) {
        List<Object[]> rows = new ArrayList<>();

        try (InputStream is = resolveInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in " + filePath
                        + ". Available sheets: " + workbook.getNumberOfSheets());
            }

            int totalRows = sheet.getLastRowNum();
            int totalCols = sheet.getRow(0).getLastCellNum();
            System.out.println("[ExcelUtils] Workbook has " + workbook.getNumberOfSheets() + " sheet(s): ");
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                System.out.println("[ExcelUtils]   sheet[" + s + "] = '" + workbook.getSheetName(s) + "'");
            }
            System.out.println("[ExcelUtils] Reading sheet '" + sheetName + "' -> "
                    + totalRows + " data row(s), " + totalCols + " column(s).");

            DataFormatter formatter = new DataFormatter();

            // row 0 = header, so data starts from row 1
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                Object[] rowData = new Object[totalCols];
                for (int j = 0; j < totalCols; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String rawType = cell.getCellType().toString();
                    // DataFormatter safely renders ANY cell type (string, numeric,
                    // formula, blank) to text without mutating the cell - this
                    // avoids the setCellType() coercion that was silently
                    // producing blanks for this file.
                    String value = formatter.formatCellValue(cell);
                    System.out.println("[ExcelUtils]   row " + i + " col " + j
                            + " rawType=" + rawType + " value='" + value + "'");
                    rowData[j] = value;
                }
                System.out.println("[ExcelUtils] Row " + i + ": " + java.util.Arrays.toString(rowData));
                rows.add(rowData);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read Excel file: " + filePath, e);
        }

        return rows.toArray(new Object[0][0]);
    }

    private static InputStream resolveInputStream(String filePath) throws IOException {
        // 1) Try as a direct filesystem path (relative to wherever the JVM's
        // working directory currently is).
        File asFile = new File(filePath);
        if (asFile.exists()) {
            System.out.println("[ExcelUtils] Resolved from filesystem: " + asFile.getAbsolutePath());
            return new FileInputStream(asFile);
        }

        // 2) Fall back to the test classpath - this is location-independent,
        // so it works the same in Maven, Eclipse, IntelliJ, VS Code, CI, etc.
        // Strip any "src/test/resources/" style prefix since classpath
        // lookups are relative to the resources root, not the source tree.
        String classpathName = filePath.replace("\\", "/");
        int idx = classpathName.indexOf("src/test/resources/");
        if (idx >= 0) {
            classpathName = classpathName.substring(idx + "src/test/resources/".length());
        }
        InputStream cpStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(classpathName);
        if (cpStream != null) {
            System.out.println("[ExcelUtils] Resolved from classpath: " + classpathName);
            return cpStream;
        }

        // 3) Neither worked - fail loudly with the working directory printed,
        // instead of ever silently returning blank/empty data.
        throw new RuntimeException("Could not locate Excel file '" + filePath
                + "' as a filesystem path OR on the classpath as '" + classpathName
                + "'. Current working directory is: " + System.getProperty("user.dir"));
    }
}