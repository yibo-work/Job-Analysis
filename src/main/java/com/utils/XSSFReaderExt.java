package com.utils;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument.Factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XSSFReaderExt {
    private final OPCPackage pkg;
    private final PackagePart workbookPart;

    public XSSFReaderExt(OPCPackage pkg) throws IOException, OpenXML4JException {
        this.pkg = pkg;
        PackageRelationship coreDocRelationship = this.pkg.getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument").getRelationship(0);
        this.workbookPart = this.pkg.getPart(coreDocRelationship);
    }

    public SharedStringsTable getSharedStringsTable() throws IOException, InvalidFormatException {
        ArrayList<PackagePart> parts = this.pkg.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());
        return parts.size() == 0 ? null : new SharedStringsTable(parts.get(0), null);
    }

    public StylesTable getStylesTable() throws IOException, InvalidFormatException {
        ArrayList<PackagePart> parts = this.pkg.getPartsByContentType(XSSFRelation.STYLES.getContentType());
        if (parts.size() == 0) {
            return null;
        } else {
            StylesTable styles = new StylesTable(parts.get(0), null);
            parts = this.pkg.getPartsByContentType(XSSFRelation.THEME.getContentType());
            if (parts.size() != 0) {
                styles.setTheme(new ThemesTable(parts.get(0), null));
            }

            return styles;
        }
    }

    public InputStream getSharedStringsData() throws IOException, InvalidFormatException {
        return XSSFRelation.SHARED_STRINGS.getContents(this.workbookPart);
    }

    public InputStream getStylesData() throws IOException, InvalidFormatException {
        return XSSFRelation.STYLES.getContents(this.workbookPart);
    }

    public InputStream getThemesData() throws IOException, InvalidFormatException {
        return XSSFRelation.THEME.getContents(this.workbookPart);
    }

    public InputStream getWorkbookData() throws IOException, InvalidFormatException {
        return this.workbookPart.getInputStream();
    }

    public InputStream getSheet(String relId) throws IOException, InvalidFormatException {
        PackageRelationship rel = this.workbookPart.getRelationship(relId);
        if (rel == null) {
            throw new IllegalArgumentException("No Sheet found with r:id " + relId);
        } else {
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart sheet = this.pkg.getPart(relName);
            if (sheet == null) {
                throw new IllegalArgumentException("No data found for Sheet with r:id " + relId);
            } else {
                return sheet.getInputStream();
            }
        }
    }

    public Iterator<InputStream> getSheetsData() throws IOException, InvalidFormatException {
        return new SheetIterator(this.workbookPart);
    }

    public static class SheetIterator implements Iterator<InputStream> {
        private final Map<String, PackagePart> sheetMap;
        private final Iterator<CTSheet> sheetIterator;
        private CTSheet ctSheet;

        private SheetIterator(PackagePart wb) throws IOException {
            try {
                this.sheetMap = new HashMap<String, PackagePart>();
                Iterator i = wb.getRelationships().iterator();

                while (true) {
                    PackageRelationship rel;
                    do {
                        if (!i.hasNext()) {
                            CTWorkbook wbBean = Factory.parse(wb.getInputStream()).getWorkbook();
                            this.sheetIterator = wbBean.getSheets().getSheetList().iterator();
                            return;
                        }

                        rel = (PackageRelationship) i.next();
                    } while (!rel.getRelationshipType().equals(XSSFRelation.WORKSHEET.getRelation()) && !rel.getRelationshipType().equals(XSSFRelation.CHARTSHEET.getRelation()));

                    PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
                    this.sheetMap.put(rel.getId(), wb.getPackage().getPart(relName));
                }
            } catch (InvalidFormatException | XmlException var5) {
                throw new POIXMLException(var5);
            }
        }

        @Override
        public boolean hasNext() {
            return this.sheetIterator.hasNext();
        }

        @Override
        public InputStream next() {
            this.ctSheet = this.sheetIterator.next();
            String sheetId = this.ctSheet.getId();

            try {
                PackagePart sheetPkg = this.sheetMap.get(sheetId);
                return sheetPkg.getInputStream();
            } catch (IOException var3) {
                throw new POIXMLException(var3);
            }
        }

        public String getSheetName() {
            return this.ctSheet.getName();
        }

        public long getSheetId() {
            return this.ctSheet.getSheetId();
        }

        public CommentsTable getSheetComments() {
            PackagePart sheetPkg = this.getSheetPart();

            try {
                PackageRelationshipCollection commentsList = sheetPkg.getRelationshipsByType(XSSFRelation.SHEET_COMMENTS.getRelation());
                if (commentsList.size() > 0) {
                    PackageRelationship comments = commentsList.getRelationship(0);
                    PackagePartName commentsName = PackagingURIHelper.createPartName(comments.getTargetURI());
                    PackagePart commentsPart = sheetPkg.getPackage().getPart(commentsName);
                    return new CommentsTable(commentsPart, comments);
                } else {
                    return null;
                }
            } catch (InvalidFormatException | IOException var6) {
                return null;
            }
        }

        public PackagePart getSheetPart() {
            String sheetId = this.ctSheet.getId();
            return this.sheetMap.get(sheetId);
        }

        @Override
        public void remove() {
            throw new IllegalStateException("Not supported");
        }
    }
}
