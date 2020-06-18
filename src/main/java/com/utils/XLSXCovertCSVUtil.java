package com.utils;


import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XLSXCovertCSVUtil {
    private static int startRowNumber = 1;
    private final OPCPackage xlsxPackage;
    private String sheetName;
    private long sheetIndex;
    private boolean readByIndex = true;

    private XLSXCovertCSVUtil(OPCPackage pkg, String sheetName) {
        this.xlsxPackage = pkg;
        this.sheetName = sheetName;
        this.readByIndex = false;
    }

    private XLSXCovertCSVUtil(OPCPackage pkg, long sheetIndex) {
        this.xlsxPackage = pkg;
        this.sheetIndex = sheetIndex;
    }

    public static List<List<String>> readXLSX(File file, String sheetName, int startRowNum, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        startRowNumber = startRowNum;
        OPCPackage p = OPCPackage.open(file, PackageAccess.READ);
        XLSXCovertCSVUtil xlsx2csv = new XLSXCovertCSVUtil(p, sheetName);
        List<List<String>> list = xlsx2csv.process(columns);
        p.close();
        return list;
    }

    public static List<List<String>> readXLSX(File file, String sheetName, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        return readXLSX(file, sheetName, 1, columns);
    }

    public static List<List<String>> readXLSX(File file, long sheetIndex, int startRowNum, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        startRowNumber = startRowNum;
        OPCPackage p = OPCPackage.open(file, PackageAccess.READ);
        XLSXCovertCSVUtil xlsx2csv = new XLSXCovertCSVUtil(p, sheetIndex);
        List<List<String>> list = xlsx2csv.process(columns);
        p.close();
        return list;
    }

    public static List<List<String>> readXLSX(File file, long sheetIndex, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        return readXLSX(file, sheetIndex, 1, columns);
    }

    private List<List<String>> processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream, int columns) throws IOException, ParserConfigurationException, SAXException {
        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        MyXSSFSheetHandler handler = new MyXSSFSheetHandler(styles, strings, columns);
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
        return handler.getRows();
    }

    private List<List<String>> process(int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
        XSSFReaderExt xssfReader = new XSSFReaderExt(this.xlsxPackage);
        List<List<String>> list = null;
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReaderExt.SheetIterator iter = (XSSFReaderExt.SheetIterator) xssfReader.getSheetsData();

        while (iter.hasNext()) {
            InputStream stream = iter.next();
            if (this.readByIndex) {
                if (this.sheetIndex == iter.getSheetId()) {
                    list = this.processSheet(styles, strings, stream, columns);
                    stream.close();
                }
            } else if (this.sheetName.equalsIgnoreCase(iter.getSheetName())) {
                list = this.processSheet(styles, strings, stream, columns);
                stream.close();
            }
        }

        return list;
    }

    enum xssfDataType {
        BOOL,
        ERROR,
        FORMULA,
        INLINESTR,
        SSTINDEX,
        NUMBER;

        xssfDataType() {
        }
    }

    class MyXSSFSheetHandler extends DefaultHandler {
        private final DataFormatter formatter;
        private final StylesTable stylesTable;
        private final ReadOnlySharedStringsTable sharedStringsTable;
        private final StringBuffer value;
        private final String[] record;
        private boolean vIsOpen;
        private xssfDataType nextDataType;
        private short formatIndex;
        private String formatString;
        private List<List<String>> rows = new ArrayList();
        private int thisColumn = -1;
        private int currentRow = 0;

        public MyXSSFSheetHandler(StylesTable styles, ReadOnlySharedStringsTable strings, int columns) {
            this.stylesTable = styles;
            this.sharedStringsTable = strings;
            this.value = new StringBuffer();
            this.nextDataType = xssfDataType.NUMBER;
            this.formatter = new DataFormatter();
            this.record = new String[columns];
            this.rows.clear();
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (!"inlineStr".equals(name) && !"v".equals(name)) {
                if ("c".equals(name)) {
                    String r = attributes.getValue("r");
                    int firstDigit = -1;

                    for (int c = 0; c < r.length(); ++c) {
                        if (Character.isDigit(r.charAt(c))) {
                            firstDigit = c;
                            break;
                        }
                    }

                    this.thisColumn = this.nameToColumn(r.substring(0, firstDigit));
                    this.nextDataType = xssfDataType.NUMBER;
                    this.formatIndex = -1;
                    this.formatString = null;
                    String cellType = attributes.getValue("t");
                    String cellStyleStr = attributes.getValue("s");
                    if ("b".equals(cellType)) {
                        this.nextDataType = xssfDataType.BOOL;
                    } else if ("e".equals(cellType)) {
                        this.nextDataType = xssfDataType.ERROR;
                    } else if ("inlineStr".equals(cellType)) {
                        this.nextDataType = xssfDataType.INLINESTR;
                    } else if ("s".equals(cellType)) {
                        this.nextDataType = xssfDataType.SSTINDEX;
                    } else if ("str".equals(cellType)) {
                        this.nextDataType = xssfDataType.FORMULA;
                    } else if (cellStyleStr != null) {
                        int styleIndex = Integer.parseInt(cellStyleStr);
                        XSSFCellStyle style = this.stylesTable.getStyleAt(styleIndex);
                        this.formatIndex = style.getDataFormat();
                        this.formatString = style.getDataFormatString();
                        if (this.formatString == null) {
                            this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
                        }
                    }
                }
            } else {
                this.vIsOpen = true;
                this.value.setLength(0);
            }

        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            String thisStr = null;
            int i$;
            if ("v".equals(name)) {
                switch (this.nextDataType) {
                    case BOOL:
                        char first = this.value.charAt(0);
                        thisStr = first == '0' ? "FALSE" : "TRUE";
                        break;
                    case ERROR:
                        thisStr = "ERROR:" + this.value.toString();
                        break;
                    case FORMULA:
                        thisStr = this.value.toString();
                        break;
                    case INLINESTR:
                        XSSFRichTextString rtsi = new XSSFRichTextString(this.value.toString());
                        thisStr = rtsi.toString();
                        break;
                    case SSTINDEX:
                        String sstIndex = this.value.toString();

                        try {
                            i$ = Integer.parseInt(sstIndex);
                            XSSFRichTextString rtss = new XSSFRichTextString(this.sharedStringsTable.getEntryAt(i$));
                            thisStr = rtss.toString();
                        } catch (NumberFormatException var11) {
                            var11.printStackTrace();
                        }
                        break;
                    case NUMBER:
                        String n = this.value.toString();
                        if (HSSFDateUtil.isADateFormat(this.formatIndex, n)) {
                            Double d = Double.parseDouble(n);
                            Date date = HSSFDateUtil.getJavaDate(d);
                            thisStr = this.formateDateToString(date);
                        } else if (this.formatString != null) {
                            thisStr = this.formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
                        } else {
                            thisStr = n;
                        }
                        break;
                    default:
                        thisStr = "(TODO: Unexpected type: " + this.nextDataType + ")";
                }

                if (this.thisColumn < this.record.length) {
                    this.record[this.thisColumn] = thisStr;
                }
            } else if ("row".equals(name)) {
                ++this.currentRow;
                if (this.currentRow >= XLSXCovertCSVUtil.startRowNumber) {
                    int cellBlankCount = 0;
                    String[] arr$ = this.record;
                    int i = arr$.length;

                    for (i$ = 0; i$ < i; ++i$) {
                        String val = arr$[i$];
                        if (!StringUtils.hasText(val)) {
                            ++cellBlankCount;
                        }
                    }

                    if (cellBlankCount != this.record.length) {
                        List<String> newRecord = new ArrayList();

                        for (i = 0; i < this.record.length; ++i) {
                            newRecord.add(this.record[i]);
                            this.record[i] = null;
                        }

                        this.rows.add(newRecord);
                    }
                }
            }

        }

        public List<List<String>> getRows() {
            return this.rows;
        }

        public void setRows(List<List<String>> rows) {
            this.rows = rows;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.vIsOpen) {
                this.value.append(ch, start, length);
            }
        }

        private int nameToColumn(String name) {
            int column = -1;

            for (int i = 0; i < name.length(); ++i) {
                int c = name.charAt(i);
                column = (column + 1) * 26 + c - 65;
            }

            return column;
        }

        private String formateDateToString(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        }
    }
}

