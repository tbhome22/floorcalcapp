package com.example.floorboardcalculator.ui.pdf;

import com.itextpdf.io.IOException;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

public class PageFooter implements IEventHandler {
    protected Document doc;

    public PageFooter(Document doc) {
        this.doc = doc;
    }

    @Override
    public void handleEvent(Event current) {
        PdfDocumentEvent documentEvent = (PdfDocumentEvent) current;
        PdfDocument pdfDoc = documentEvent.getDocument();
        int pageNumber = pdfDoc.getPageNumber(documentEvent.getPage());
        int pageTotal = pdfDoc.getNumberOfPages();

        Paragraph p = new Paragraph("Page " + pageNumber + " of " + pageTotal);

        Rectangle pageSize = documentEvent.getPage().getPageSize();
        PdfFont font = null;
        try{
            font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        float coordX = ((pageSize.getLeft() + doc.getLeftMargin())
                + (pageSize.getRight() - doc.getRightMargin())) / 2;
        float footerY = doc.getBottomMargin();
        Canvas canvas = new Canvas(documentEvent.getPage(), pageSize);

        canvas
                .setFont(font)
                .setFontSize(8)
                .showTextAligned(p, coordX, footerY, TextAlignment.CENTER)
                .close();
    }

}
