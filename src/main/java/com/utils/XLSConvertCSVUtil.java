package com.utils;


import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XLSConvertCSVUtil implements HSSFListener {
    private static String targetSheetName;
    private static int targetSheetIndex;
    private static boolean readByIndex = true;
    private static int startRowNumber = 1;
    private final List<List<String>> allRecords;
    private final String[] recordArray;
    private final POIFSFileSystem fs;
    private final boolean outputFormulaValues;
    private final ArrayList boundSheetRecords;
    boolean readThisSheet;
    private SheetRecordCollectingListener workbookBuildingListener;
    private HSSFWorkbook stubWorkbook;
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;
    private int currentSheetIndex;
    private BoundSheetRecord[] orderedBSRs;
    private int nextRow;
    private int nextColumn;
    private boolean outputNextStringRecord;

    private XLSConvertCSVUtil(POIFSFileSystem fs, int minColumns) {
        this.allRecords = new ArrayList();
        this.outputFormulaValues = true;
        this.currentSheetIndex = -1;
        this.boundSheetRecords = new ArrayList();
        this.readThisSheet = false;
        this.fs = fs;
        this.recordArray = new String[minColumns];
        this.allRecords.clear();
    }

    private XLSConvertCSVUtil(String filename, int minColumns) throws IOException {
        this(new POIFSFileSystem(new FileInputStream(filename)), minColumns);
    }

    public static List<List<String>> readXLS(File file, String sheetName, int startRowNum, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        startRowNumber = startRowNum - 1;
        if (startRowNumber < 0) {
            startRowNumber = 0;
        }

        targetSheetName = sheetName;
        readByIndex = false;
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        XLSConvertCSVUtil xls2csv = new XLSConvertCSVUtil(fs, columns);
        xls2csv.process();
        return xls2csv.getAllRecords();
    }

    public static List<List<String>> readXLS(File file, String sheetName, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        return readXLS(file, sheetName, 1, columns);
    }

    public static List<List<String>> readXLS(File file, int sheetIndex, int startRowNum, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        startRowNumber = startRowNum - 1;
        if (startRowNumber < 0) {
            startRowNumber = 0;
        }

        targetSheetIndex = sheetIndex;
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        XLSConvertCSVUtil xls2csv = new XLSConvertCSVUtil(fs, columns);
        xls2csv.process();
        return xls2csv.getAllRecords();
    }

    public static List<List<String>> readXLS(File file, int sheetIndex, int columns) throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
        return readXLS(file, sheetIndex, 1, columns);
    }

    private void process() throws IOException {
        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        this.formatListener = new FormatTrackingHSSFListener(listener);
        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();
        if (this.outputFormulaValues) {
            request.addListenerForAllRecords(this.formatListener);
        } else {
            this.workbookBuildingListener = new SheetRecordCollectingListener(this.formatListener);
            request.addListenerForAllRecords(this.workbookBuildingListener);
        }

        factory.processWorkbookEvents(request, this.fs);
    }

    @Override
    public void processRecord(Record record) {
        int thisColumn = -1;
        String thisStr = null;
        if (record.getSid() == 133) {
            this.boundSheetRecords.add(record);
        }

        if (record.getSid() == 2057) {
            BOFRecord br = (BOFRecord) record;
            if (br.getType() == 16) {
                if (this.workbookBuildingListener != null && this.stubWorkbook == null) {
                    this.stubWorkbook = this.workbookBuildingListener.getStubHSSFWorkbook();
                }

                ++this.currentSheetIndex;
                if (this.orderedBSRs == null) {
                    this.orderedBSRs = BoundSheetRecord.orderByBofPosition(this.boundSheetRecords);
                }

                if (readByIndex) {
                    this.readThisSheet = targetSheetIndex - 1 == this.currentSheetIndex;
                } else
                    this.readThisSheet = targetSheetName.equalsIgnoreCase(this.orderedBSRs[this.currentSheetIndex].getSheetname());
            } else {
                this.readThisSheet = false;
            }
        }

        if (record.getSid() == 252) {
            this.sstRecord = (SSTRecord) record;
        }

        if (this.readThisSheet) {
            boolean skipThisRow = false;
            int thisRow;
            switch (record.getSid()) {
                case 6:
                    FormulaRecord frec = (FormulaRecord) record;
                    thisRow = frec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = frec.getColumn();
                        if (this.outputFormulaValues) {
                            if (Double.isNaN(frec.getValue())) {
                                this.outputNextStringRecord = true;
                                this.nextRow = frec.getRow();
                                this.nextColumn = frec.getColumn();
                            } else {
                                thisStr = this.formatListener.formatNumberDateCell(frec);
                            }
                        } else {
                            thisStr = HSSFFormulaParser.toFormulaString(this.stubWorkbook, frec.getParsedExpression());
                        }
                    }
                    break;
                case 28:
                    NoteRecord nrec = (NoteRecord) record;
                    thisRow = nrec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = nrec.getColumn();
                        thisStr = "(NoteRecord)";
                    }
                    break;
                case 253:
                    LabelSSTRecord lsrec = (LabelSSTRecord) record;
                    thisRow = lsrec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = lsrec.getColumn();
                        if (this.sstRecord == null) {
                            thisStr = "";
                        } else {
                            thisStr = this.sstRecord.getString(lsrec.getSSTIndex()).toString();
                        }
                    }
                    break;
                case 513:
                    BlankRecord brec = (BlankRecord) record;
                    thisRow = brec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = brec.getColumn();
                        thisStr = "";
                    }
                    break;
                case 515:
                    NumberRecord numrec = (NumberRecord) record;
                    thisRow = numrec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = numrec.getColumn();
                        thisStr = this.formatListener.formatNumberDateCell(numrec);
                    }
                    break;
                case 516:
                    LabelRecord lrec = (LabelRecord) record;
                    thisRow = lrec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = lrec.getColumn();
                        thisStr = lrec.getValue();
                    }
                    break;
                case 517:
                    BoolErrRecord berec = (BoolErrRecord) record;
                    thisRow = berec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = berec.getColumn();
                        thisStr = "";
                    }
                    break;
                case 519:
                    if (this.outputNextStringRecord) {
                        StringRecord srec = (StringRecord) record;
                        thisStr = srec.getString();
                        thisRow = this.nextRow;
                        thisColumn = this.nextColumn;
                        this.outputNextStringRecord = false;
                    }
                    break;
                case 638:
                    RKRecord rkrec = (RKRecord) record;
                    thisRow = rkrec.getRow();
                    if (startRowNumber > thisRow) {
                        skipThisRow = true;
                    } else {
                        thisColumn = rkrec.getColumn();
                        thisStr = "(RKRecord)";
                    }
                default:
                    break;
            }

            if (record instanceof MissingCellDummyRecord) {
                MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
                thisRow = mc.getRow();
                if (startRowNumber > thisRow) {
                    skipThisRow = true;
                } else {
                    thisColumn = mc.getColumn();
                    thisStr = "";
                }
            }

            if (!skipThisRow && thisColumn >= 0 && thisColumn < this.recordArray.length) {
                this.recordArray[thisColumn] = thisStr;
            }

            if (record instanceof LastCellOfRowDummyRecord && !skipThisRow) {
                int cellBlankCount = 0;
                String[] arr$ = this.recordArray;
                int i = arr$.length;

                for (int i$ = 0; i$ < i; ++i$) {
                    String val = arr$[i$];
                    if (!StringUtils.hasText(val)) {
                        ++cellBlankCount;
                    }
                }

                if (cellBlankCount != this.recordArray.length) {
                    List<String> newRecord = new ArrayList();

                    for (i = 0; i < this.recordArray.length; ++i) {
                        newRecord.add(this.recordArray[i]);
                        this.recordArray[i] = null;
                    }

                    this.allRecords.add(newRecord);
                }
            }
        }

    }

    private List<List<String>> getAllRecords() {
        return this.allRecords;
    }
}

