package com.tonbu.support.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 通用工具类
 * 
 */
public class CommonUtils {
	/**
	 * 从身份证中解析生日
	 * @param idCard
	 * @return
	 */
	public static String getBirthdayFromIdCard(String idCard) {
		StringBuilder birthday = new StringBuilder();
		if (StringUtils.isBlank(idCard))
			return birthday.toString();

		if (idCard.length() == 18) {
			birthday.append(idCard.substring(6, 10)).append("-");
			birthday.append(idCard.substring(10, 12)).append("-");
			birthday.append(idCard.substring(12, 14));
		} else if (idCard.length() == 15) {
			birthday.append("19").append(idCard.substring(6, 8)).append("-");
			birthday.append(idCard.substring(8, 10)).append("-");
			birthday.append(idCard.substring(10, 12));
		}
		return birthday.toString();
	}

//	@SuppressWarnings("rawtypes")
//	public static List importFromExcel(File file, int sheetIndex, int startRowIndex) {
//		try {
//			return importFromExcel(new FileInputStream(file), sheetIndex, startRowIndex);
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		}
//	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static List importFromExcel(InputStream inputStream, int sheetIndex, int startRowIndex) {
//		List dataList = new ArrayList();
//		try {
//
//			POIFSFileSystem fs = new POIFSFileSystem(inputStream);
//			// XSSFWorkbook wb = new XSSFWorkbook(inputStream);
//			HSSFWorkbook wb = new HSSFWorkbook(fs);
//			HSSFSheet sheet = wb.getSheetAt(sheetIndex);
//
//			for (int i = startRowIndex; i <= sheet.getLastRowNum(); i++) {
//				HSSFRow row = sheet.getRow(i);
//
//				List rowList = new ArrayList();
//
//				for (int k = 0; (row != null) && (k < row.getPhysicalNumberOfCells()); k++) {
//					HSSFCell cell = row.getCell(k);
//					String val = null;
//					if (cell != null) {
//						if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC)
//							val = new Double(cell.getNumericCellValue()).toString();
//						else {
//							val = cell.getStringCellValue();
//						}
//					}
//					rowList.add(val);
//				}
//				dataList.add(rowList.toArray(new String[rowList.size()]));
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return dataList;
//	}
	/** 
	  * 判断是否为整数  
	  * @param s 字符串参数
	  * @return 是整数返回true,否则返回false
	  * @author zhangjie
	*/ 
	public final static boolean isNumeric(String s) {  
        if (s != null && !"".equals(s.trim()))  
            return s.matches("^[0-9]*$");  
        else  
            return false;  
    }
	/**
	 * 判断是否为身份证s
	 * @param  s 字符串参数
	 * @return 是身份证返回true,否则返回false
	 */
	public static boolean isIdcard(String s) {
        //第一代身份证正则表达式(15位)
        String isIDCard1 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        //第二代身份证正则表达式(18位)
        String isIDCard2 ="^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[A-Z])$";
        //验证身份证
        return (s.matches(isIDCard1) || s.matches(isIDCard2));
	}
	/** 
	  * 检查前台传参 
	  * @param args String参数  
	  * @author zhangjie  
	*/ 
	public final static void checkParam(String... args) {
		for(String arg : args){
			if(StringUtils.isBlank(arg)){
				throw new RuntimeException("参数异常.请检查.");
			}
		}
   }
	/** 
	  * 取当前系统时间
	  * @param dateFormat 时间格式
	  * @return String时间
	  * @author zhangjie  
	*/ 
	public final static String getStringDate(String dateFormat) {
		return new SimpleDateFormat(dateFormat).format(new Date());
  }


	// 转化字符串为十六进制编码
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}

	// 转化十六进制编码为字符串
	public static String toStringHex2(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}
}
