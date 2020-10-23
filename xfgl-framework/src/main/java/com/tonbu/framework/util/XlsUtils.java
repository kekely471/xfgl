package com.tonbu.framework.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

public class XlsUtils {

    protected static Logger logger=LogManager.getLogger(ZipUtils.class.getName());

    private static final Integer SHEET_MAX_ROW = 65530;
    private static final Integer COLOUMN_MAX_WIDTH = 50;

    public XlsUtils() {
    }

    public static String gridExportToExcel(List<Map<String, String>> gridcolumns, List<Map<String, Object>> dataList) {
        File tempfile = null;

        try {
            Long xlsname = System.currentTimeMillis();
            tempfile = File.createTempFile(String.valueOf(xlsname), ".xls");
            FileOutputStream out = new FileOutputStream(tempfile);
            gridExportToExcel(gridcolumns, dataList, out);
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return tempfile.getName();
    }

    private static void setDisplayHeader(HSSFRow row, List<Map<String, String>> gridcolumns, HSSFCellStyle headerStyle, Map<Integer, Integer> columnWidth) {
        for(int i = 0; i < gridcolumns.size(); ++i) {
            Map<String, String> attr = (Map)gridcolumns.get(i);
            String display = (String)attr.get("display");
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(display);
            cell.setCellStyle(headerStyle);
            int width = display.getBytes().length + 4;
            Integer lastWidth = (Integer)columnWidth.get(i);
            if (lastWidth == null || lastWidth < width) {
                columnWidth.put(i, width);
            }
        }

    }

    private static void formatSheet(HSSFSheet sheet, Map<Integer, Integer> columnWidth, int columnCount) {
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, columnCount - 1));
        sheet.createFreezePane(0, 1);

        for(int i = 0; i < columnCount; ++i) {
            if (columnWidth.containsKey(i)) {
                sheet.setColumnWidth(i, ((Integer)columnWidth.get(i) + 1) * 256);
            } else {
                sheet.autoSizeColumn(i);
            }
        }

    }

    public static void gridExportToExcel(List<Map<String, String>> gridcolumns, List<Map<String, Object>> dataList, OutputStream out) {
        if (gridcolumns != null && gridcolumns.size() != 0) {
            if (dataList != null && dataList.size() != 0) {
                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFCellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setAlignment(HorizontalAlignment.forInt(2));
                HSSFFont font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                HSSFCellStyle dateStyle = workbook.createCellStyle();
                dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
                HSSFCellStyle timeStyle = workbook.createCellStyle();
                timeStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("h:mm:ss"));
                HSSFCellStyle timestampStyle = workbook.createCellStyle();
                timestampStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
                HSSFSheet sheet = workbook.createSheet();
                HSSFRow row = null;
                Map<Integer, Integer> columnWidth = new HashMap();
                int index = 0;
                setDisplayHeader(sheet.createRow(index), gridcolumns, headerStyle, columnWidth);
                Iterator var15 = dataList.iterator();

                while(var15.hasNext()) {
                    Map<String, Object> map = (Map)var15.next();
                    if (index + 1 > SHEET_MAX_ROW) {
                        formatSheet(sheet, columnWidth, gridcolumns.size());
                        columnWidth.clear();
                        index = 0;
                        sheet = workbook.createSheet();
                        setDisplayHeader(sheet.createRow(index), gridcolumns, headerStyle, columnWidth);
                    }

                    ++index;
                    row = sheet.createRow(index);

                    for(int j = 0; j < gridcolumns.size(); ++j) {
                        Map<String, String> attr = (Map)gridcolumns.get(j);
                        String name = (String)attr.get("name");
                        Object val = "";
                        name = StringUtils.upperCase(name);
                        if (map.containsKey(name)) {
                            val = map.get(name);
                        }

                        HSSFCell cell = row.createCell(j);
                        if (val == null) {
                            val = "";
                        }

                        int width = String.valueOf(val).getBytes().length;
                        if (Number.class.isAssignableFrom(val.getClass())) {
                            cell.setCellValue((new BigDecimal(String.valueOf(val))).doubleValue());
                        } else {
                            cell.setCellValue(String.valueOf(val));
                        }

                        if (Date.class.isAssignableFrom(val.getClass())) {
                            cell.setCellValue((Date)val);
                            if (Timestamp.class.isAssignableFrom(val.getClass())) {
                                cell.setCellStyle(timestampStyle);
                                width = 16;
                            } else if (Time.class.isAssignableFrom(val.getClass())) {
                                cell.setCellStyle(timeStyle);
                                width = 10;
                            } else {
                                cell.setCellStyle(dateStyle);
                                width = 10;
                            }
                        }

                        Integer lastWidth = (Integer)columnWidth.get(j);
                        width = width > COLOUMN_MAX_WIDTH ? COLOUMN_MAX_WIDTH : width;
                        if (lastWidth == null || lastWidth < width) {
                            columnWidth.put(j, width);
                        }
                    }
                }

                formatSheet(sheet, columnWidth, gridcolumns.size());

                try {
                    workbook.write(out);
                } catch (Exception var22) {
                    throw new RuntimeException(var22);
                }
            }
        }
    }

    public static void exportToExcel(List dataList, OutputStream out) {
        if (dataList != null && dataList.size() != 0) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet();
            HSSFRow row = null;

            for(int i = 0; i < dataList.size(); ++i) {
                Object entry = dataList.get(i);
                row = sheet.createRow(i);
                int k;
                HSSFCell cell;
                if (entry instanceof String[]) {
                    String[] emt = (String[])entry;

                    for(k = 0; k < emt.length; ++k) {
                        cell = row.createCell(k);
                        cell.setCellValue(emt[k]);
                    }
                } else if (entry instanceof Map) {
                    Iterator it = ((Map)entry).keySet().iterator();
                    k = 0;

                    while(it.hasNext()) {
                        cell = row.createCell(k++);
                        cell.setCellValue(((Map)entry).get(it.next()).toString());
                    }
                }
            }

            try {
                workbook.write(out);
            } catch (Exception var10) {
                throw new RuntimeException(var10);
            }
        }
    }

    public static void exportToExcel(List dataList, File file) {
        try {
            exportToExcel(dataList, (OutputStream)(new FileOutputStream(file)));
        } catch (FileNotFoundException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static List importFromExcel(File file, int sheetIndex, int startRowIndex) {
        try {
            return importFromExcel((InputStream)(new FileInputStream(file)), sheetIndex, startRowIndex);
        } catch (FileNotFoundException var4) {
            throw new RuntimeException(var4);
        }
    }

    public static List importFromExcel(InputStream inputStream, int sheetIndex, int startRowIndex) {
        ArrayList dataList = new ArrayList();

        try {
            POIFSFileSystem fs = new POIFSFileSystem(inputStream);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(sheetIndex);

            for(int i = startRowIndex; i <= sheet.getLastRowNum(); ++i) {
                HSSFRow row = sheet.getRow(i);
                List rowList = new ArrayList();

                for(int k = 0; row != null && k < row.getPhysicalNumberOfCells(); ++k) {
                    HSSFCell cell = row.getCell(k);
                    String val = null;
                    if (cell != null) {
                        if (cell.getCellType() == 0) {
                            val = (new Double(cell.getNumericCellValue())).toString();
                        } else {
                            val = cell.getStringCellValue();
                        }
                    }

                    rowList.add(val);
                }

                dataList.add(rowList.toArray(new String[rowList.size()]));
            }

            return dataList;
        } catch (Exception var13) {
            throw new RuntimeException(var13);
        }
    }

    public static List importFromExcel(File file) {
        return importFromExcel((File)file, 0, 0);
    }

    public static List importFromExcel(InputStream inputStream) {
        return importFromExcel((InputStream)inputStream, 0, 0);
    }
}
