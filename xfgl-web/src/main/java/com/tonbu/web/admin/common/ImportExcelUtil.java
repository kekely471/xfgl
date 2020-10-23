package com.tonbu.web.admin.common;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImportExcelUtil {

	public static List excelToList(MultipartFile file, Class<?> clazz, String[] properties, int sheetNum, int rowNum){
		Workbook wb = null;
		InputStream is = null;

		List objList = new ArrayList();

		try{
			String fileName = file.getOriginalFilename();
			is = file.getInputStream();
			//2007
			if(fileName.toUpperCase().indexOf("XLSX") != -1){
//				wb = new XSSFWorkbook(new BufferedInputStream(is));
			}else{
				wb = new HSSFWorkbook(new BufferedInputStream(is));
			}

			CellStyle cellStyle = wb.createCellStyle();//创建一个DataFormat对象
			DataFormat format = wb.createDataFormat();

			Sheet sheet = wb.getSheetAt(sheetNum);
			if(sheet == null){
				throw new RuntimeException("导入文件的sheet页错误");
			}
			while(true){
				Row row = sheet.getRow(rowNum);
				if(row == null){
					break;
				}
				Cell cell0 = row.getCell(0);//第一列值不能为空
				if(cell0 == null || getCellValue(cell0) == null || StringUtils.isBlank(getCellValue(cell0).toString())){
					break;
				}
				System.out.println();
				Object obj = clazz.newInstance();
				Class<?> clz = obj.getClass();
				for(int j = 0; j < properties.length; j++){
					Cell cell = row.getCell(j);
					if(cell == null){
						continue;
					}
					cell.setCellType(CellType.STRING);
					//创建一个样式
					//这样才能真正的控制单元格格式，@就是指文本型，具体格式的定义还是参考上面的原文吧
				//	cellStyle.setDataFormat(format.getFormat("@"));
					 //具体如何创建cell就省略了，最后设置单元格的格式这样写
				//	 cell.setCellStyle(cellStyle);


					String value = String.valueOf(getCellValue(cell)).trim();
					Field field = clz.getDeclaredField(properties[j]);
					String type = field.getGenericType().toString();
					System.out.print(type + " "+ value + " ");
					if(type.equals("class java.math.BigDecimal")){
						BeanUtils.setProperty(obj, properties[j], new BigDecimal(Double.parseDouble(value)));
					}else{
						BeanUtils.setProperty(obj, properties[j], value);
					}
				}
				objList.add(obj);
				rowNum++;
			}
			return objList;
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}finally{
			colseInputstream(is);
		}
	}


	/**
	 *
	 * @param file 文件
	 * @param properties  属性名
	 * @param sheetNum  第几个工作表
	 * @param rowNum 第几行开始读取
	 * @return
	 */
	public static List excelToList(MultipartFile file, String[] properties, int sheetNum, int rowNum){
		Workbook wb = null;
		InputStream is = null;
		
		List objList = new ArrayList();
		
		try{
			String fileName = file.getOriginalFilename();
			is = file.getInputStream();
			//2007
			if(fileName.toUpperCase().indexOf("XLSX") != -1){
//				wb = new XSSFWorkbook(new BufferedInputStream(is));
			}else{
				wb = new HSSFWorkbook(new BufferedInputStream(is));
			}
			
			Sheet sheet = wb.getSheetAt(sheetNum);
			if(sheet == null){
				throw new Exception("导入文件的sheet页错误");
			}
			while(true){
				Row row = sheet.getRow(rowNum);
				Map<String,String>  objMap = new HashMap<>();
				if(row == null){
					break;
				}			
				for(int j = 0; j < properties.length; j++){
					Cell cell = row.getCell(j);
					if(cell == null){
						continue;
					}
					String value = String.valueOf(getCellValue(cell)).trim();
					objMap.put(properties[j],value);
				}
				objList.add(objMap);
				rowNum++;
			}
			return objList;
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}finally{
			colseInputstream(is);
		}
	}
	
	private static Object getCellValue(Cell cell){
		int cellType = cell.getCellType();
		if(cellType ==  HSSFCell.CELL_TYPE_NUMERIC){
			double val = cell.getNumericCellValue();
			if(String.valueOf(val).toUpperCase().indexOf("E") > -1){
				return String.valueOf(new DecimalFormat("#.######").format(val)).trim();
			}else{
				return val;
			}
		}else{
			return cell.getStringCellValue().trim();
		}
	}
	
	private static void colseInputstream(InputStream is){
		if(is ==null){
			return;
		}
		try{
			is.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static byte[] listToByte(List<String[]> list) {
		StringBuilder result = new StringBuilder();
		for(int i=0; i<list.size(); i++){
			String[] obj = list.get(i);
			for(int j=0; j<obj.length; j++){
				result.append(obj[j] == null ? "" : obj[j]);
				result.append("\t");
			}
			result.append("\r\n");
		}
		return result.toString().getBytes();
	}
}
