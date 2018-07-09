package com.spldeolin.beginningmind.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.google.common.collect.Lists;
import com.spldeolin.beginningmind.core.constant.Abbreviation;
import com.spldeolin.beginningmind.core.util.excel.ExcelAnalyzeException;
import com.spldeolin.beginningmind.core.util.excel.ExcelColumn;
import com.spldeolin.beginningmind.core.util.excel.ExcelDefinition;
import com.spldeolin.beginningmind.core.util.excel.ExcelSheet;
import com.spldeolin.beginningmind.core.util.excel.Formatter;
import com.spldeolin.beginningmind.core.util.excel.ParseInvalid;
import com.spldeolin.beginningmind.core.util.excel.ParseInvalidException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import tk.mybatis.mapper.util.StringUtil;

/**
 * @author Deolin 2018/07/07
 */
@UtilityClass
@Log4j2
public class Excels2 {

    public static <T> List<T> readExcel(File file, Class<T> clazz) throws ParseInvalidException {
        ExcelDefinition excelDefinition = new ExcelDefinition();
        try {
            analyzeFile(excelDefinition, file);
            analyzeModel(excelDefinition, clazz);
            Workbook workbook = openWorkbook(excelDefinition);
            Sheet sheet = openSheet(excelDefinition, workbook);
            analyzeModelFields(excelDefinition, clazz, sheet);
            // TODO 解析完成后excelDefinition做本地缓存
            analyzeColumns(excelDefinition, clazz, sheet);
            List<T> result = Lists.newArrayList();
            List<ParseInvalid> parseInvalids = Lists.newArrayList();
            for (Row row : listValidRows(excelDefinition, sheet)) {
                if (row != null) {
                    try {
                        result.add(parseRow(clazz, excelDefinition.getColumnDefinitions(), row));
                    } catch (ParseInvalidException e) {
                        parseInvalids.addAll(e.getParseInvalids());
                    }
                }
            }
            if (parseInvalids.size() > 0) {
                throw new ParseInvalidException().setParseInvalids(parseInvalids);
            }
            return result;
        } catch (IOException e) {
            throw new ExcelAnalyzeException("文件读写失败");
        } finally {
            close(excelDefinition);
        }
    }

    /* TODO 重载MultipartFile */
    private static void analyzeFile(ExcelDefinition excelDefinition, File file) throws IOException {
        String filename = file.getName();
        excelDefinition.setFileName(FilenameUtils.getBaseName(filename));
        excelDefinition.setFileExtension(FilenameUtils.getExtension(filename));
        excelDefinition.setFileInputStream(FileUtils.openInputStream(file));
    }

    private static <T> void analyzeModel(ExcelDefinition excelDefinition, Class<T> clazz) {
        ExcelSheet sheetAnno = clazz.getAnnotation(ExcelSheet.class);
        if (sheetAnno == null) {
            throw new RuntimeException("Model [" + clazz.getSimpleName() + "]未声明@ExcelSheet");
        }
        excelDefinition.setSheetIndex(sheetAnno.sheetIndex());
        excelDefinition.setRowOffSet(sheetAnno.startingRowNumber());
    }

    private static <T> void analyzeModelFields(ExcelDefinition excelDefinition, Class<T> clazz, Sheet sheet) {
        List<ExcelDefinition.ColumnDefinition> columnDefinitions = Lists.newArrayList();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelColumn columnAnno = field.getAnnotation(ExcelColumn.class);
            if (columnAnno == null) {
                continue;
            }
            ExcelDefinition.ColumnDefinition columnDefinition = new ExcelDefinition.ColumnDefinition();
            columnDefinition.setFirstColumnName(columnAnno.firstColumnName());
            columnDefinition.setModelField(field);
            Class<? extends Formatter> formatter = columnAnno.formatter();
            if (formatter != Formatter.class) {
                columnDefinition.setFormatter(Abbreviation.objs.newInstance(formatter));
            }
            columnDefinition.setDefaultValue(columnAnno.defaultValue());
            columnDefinitions.add(columnDefinition);
        }
        if (columnDefinitions.size() == 0) {
            throw new RuntimeException("Model [" + clazz.getSimpleName() + "]中不存在@ExcelColumn字段");
        }
        excelDefinition.setColumnDefinitions(columnDefinitions);
    }

    private static <T> void analyzeColumns(ExcelDefinition excelDefinition, Class<T> clazz, Sheet sheet) {
        for (ExcelDefinition.ColumnDefinition columnDefinition : excelDefinition.getColumnDefinitions()) {
            String columnLetter = findColumnLetterByFirstColumnName(sheet, columnDefinition.getFirstColumnName());
            columnDefinition.setColumnLetter(columnLetter);
            if (StringUtils.isNotBlank(columnLetter)) {
                columnDefinition.setColumnNumber(letterToNumber(columnLetter));
            }
        }
    }

    private static String findColumnLetterByFirstColumnName(Sheet sheet, String firstColumnName) {
        if (StringUtils.isBlank(firstColumnName)) {
            return "A";
        }
        Row row = sheet.getRow(1);
        if (row == null) {
            throw new RuntimeException("工作表[" + sheet.getSheetName() + "] 的首行不存在");
        }
        String result = null;
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.toString().equals(firstColumnName)) {
                result = numberToLetter(cell.getColumnIndex());
            }
        }
        return result;
    }

    private static Workbook openWorkbook(ExcelDefinition excelDefinition) throws IOException {
        Workbook workBook;
        String fileExtension = excelDefinition.getFileExtension();
        InputStream inputStream = excelDefinition.getFileInputStream();
        if ("xlsx".equals(fileExtension)) {
            workBook = new XSSFWorkbook(inputStream);
        } else if ("xls".equals(fileExtension)) {
            workBook = new HSSFWorkbook(inputStream);
        } else {
            throw new ExcelAnalyzeException("文件拓展名不正确");
        }
        return workBook;
    }

    private static Sheet openSheet(ExcelDefinition excelDefinition, Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new ExcelAnalyzeException("工作簿中不存在工作表");
        }
        Sheet sheet;
        try {
            sheet = workbook.getSheetAt(excelDefinition.getSheetIndex());
        } catch (IllegalArgumentException e) {
            int sheetNumber = excelDefinition.getSheetIndex() + 1;
            throw new ExcelAnalyzeException("第" + sheetNumber + "个Sheet不存在");
        }
        return sheet;
    }

    private static List<Row> listValidRows(ExcelDefinition excelDefinition, Sheet sheet) {
        List<Row> rows = Lists.newArrayList();
        int startRowNum = sheet.getFirstRowNum();
        int offsetRowNum = excelDefinition.getRowOffSet() - 1;
        if (offsetRowNum >= startRowNum) {
            startRowNum = offsetRowNum;
        }
        for (int rownum = startRowNum; rownum <= sheet.getLastRowNum(); rownum++) {
            Row row = sheet.getRow(rownum);
            List<Integer> cellNumbers = excelDefinition.getColumnDefinitions().stream().map(
                    ExcelDefinition.ColumnDefinition::getColumnNumber).collect(Collectors.toList());
            if (row != null && !rowIsAllBlankInCellNumbers(row, cellNumbers)) {
                rows.add(row);
            }

        }
        if (rows.size() == 0) {
            throw new ExcelAnalyzeException("工作表中没有内容");
        }
        return rows;
    }

    /**
     * 判断行中指定列的单元格的内容是否全部为空白
     */
    private static boolean rowIsAllBlankInCellNumbers(Row row, List<Integer> cellNumbers) {
        List<String> contents = Lists.newArrayList();
        for (Integer cellNumber : cellNumbers) {
            int cellIndex = cellNumber - 1;
            Cell cell = row.getCell(cellIndex);
            if (cell == null) {
                contents.add(null);
            } else {
                contents.add(cell.toString());
            }
        }
        return StringUtils.isAllBlank(contents.toArray(new String[0]));
    }

    private static <T> T parseRow(Class<T> clazz, List<ExcelDefinition.ColumnDefinition> columnDefinitions,
            Row row) throws ParseInvalidException {
        T t = Abbreviation.objs.newInstance(clazz);
        List<ParseInvalid> parseInvalids = Lists.newArrayList();
        for (ExcelDefinition.ColumnDefinition columnDefinition : columnDefinitions) {
            Integer columnNumber = columnDefinition.getColumnNumber();
            if (columnNumber == null) {
                continue;
            }
            // cell在row中是从0开始的
            int cellIndex = columnNumber - 1;
            Cell cell = row.getCell(cellIndex);
            String cellContent;
            if (cell == null) {
                cellContent = columnDefinition.getDefaultValue();
            } else {
                cellContent = cell.toString().trim();
            }
            Formatter formatter = columnDefinition.getFormatter();
            boolean assignedFormatter = formatter != null && formatter.getClass() != Formatter.class;
            Field field = columnDefinition.getModelField();
            Object fieldValue = null;
            try {
                if (StringUtil.isNotEmpty(cellContent)) {
                    if (!assignedFormatter) {
                        // 没有指定formatter，尝试用缺省方式指定常用formatter
                        Class fieldType = field.getType();
                        if (fieldType == String.class) {
                            fieldValue = cellContent;
                        } else if (fieldType == Integer.class) {
                            fieldValue = NumberUtils.createInteger(cellContent);
                        } else if (fieldType == Long.class) {
                            fieldValue = NumberUtils.createLong(cellContent);
                        } else if (fieldType == Float.class) {
                            fieldValue = NumberUtils.createFloat(cellContent);
                        } else if (fieldType == Double.class) {
                            fieldValue = NumberUtils.createDouble(cellContent);
                        } else if (fieldType == BigDecimal.class) {
                            fieldValue = NumberUtils.createBigDecimal(cellContent);
                        } else if (fieldType == Boolean.class) {
                            fieldValue = BooleanUtils.toBoolean(cellContent);
                        } else if (fieldType == LocalDateTime.class) {
                            fieldValue = LocalDateTime.parse(cellContent, Times.DEFAULT_DATE_TIME_FORMATTER);
                        } else {
                            throw new RuntimeException("工具类Excels未为 [" + fieldType.getSimpleName() + field.getName() +
                                    "]提供缺省转换策略，请在@ExcelColumn中指定具体formatter");
                        }
                    } else {
                        // 指定了formatter
                        fieldValue = formatter.parse(cellContent);
                    }
                }
                field.setAccessible(true);
                field.set(t, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                ParseInvalid parseInvalid = ParseInvalid.builder().rowNumber(row.getRowNum()).columnLetter(
                        columnDefinition.getColumnLetter()).cause("数据格式非法").build();
                if (assignedFormatter) {
                    parseInvalid.setCause(e.getMessage());
                }
                parseInvalids.add(parseInvalid);
            }
        }
        if (parseInvalids.size() > 0) {
            throw new ParseInvalidException().setParseInvalids(parseInvalids);
        }
        return t;
    }

    private static void close(ExcelDefinition excelDefinition) {
        InputStream inputStream = excelDefinition.getFileInputStream();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignored) {}
        }
    }

    private static int letterToNumber(String columnLetter) {
        columnLetter = columnLetter.toUpperCase();
        int number = 0;
        for (int i = 0; i < columnLetter.length(); i++) {
            number *= 26;
            number += (columnLetter.charAt(i) - 'A' + 1);
        }
        if (number == 0) {
            number = 1;
        }
        return number;
    }

    private static String numberToLetter(int number) {
        String rs = "";
        do {
            number--;
            rs = ((char) (number % 26 + (int) 'A')) + rs;
            number = (number - number % 26) / 26;
        } while (number > 0);
        return rs;
    }

}
