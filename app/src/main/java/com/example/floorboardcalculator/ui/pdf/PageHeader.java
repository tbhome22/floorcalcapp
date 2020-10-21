package com.example.floorboardcalculator.ui.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.example.floorboardcalculator.R;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DottedBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.renderer.DocumentRenderer;
import com.itextpdf.layout.renderer.TableRenderer;

import java.io.ByteArrayOutputStream;

public class PageHeader implements IEventHandler {
    private Table table;
    private Document doc;
    private float tableHeight;
    private Context parent;

    public PageHeader(Document doc, Context context) {
        this.doc = doc;
        this.parent = context;

        initTable();

        TableRenderer renderer = (TableRenderer) table.createRendererSubTree();
        renderer.setParent(new DocumentRenderer(doc));

        LayoutResult result = renderer.layout(new LayoutContext(new LayoutArea(0, PageSize.A4)));
        tableHeight = result.getOccupiedArea().getBBox().getHeight();
    }

    @Override
    public void handleEvent(Event currentEvent) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) currentEvent;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

        PageSize pageSize = pdfDoc.getDefaultPageSize();
        float coordX = pageSize.getX() + doc.getLeftMargin();
        float coordY = pageSize.getTop() - doc.getTopMargin();
        float width = pageSize.getWidth() - doc.getRightMargin() - doc.getLeftMargin();
        float height = getTableHeight();

        Rectangle rect = new Rectangle(coordX, coordY, width, height);

        new Canvas(canvas, pdfDoc, rect)
                .add(table)
                .close();
    }

    public float getTableHeight() {
        return tableHeight;
    }

    public void initTable() {
        table = new Table(2).useAllAvailableWidth();

        LineSeparator separator = new LineSeparator(new SeparatorDoc(.8f));

        ImageView image = new ImageView(parent);
        image.setImageDrawable(parent.getDrawable(R.drawable.main_logo_sm));

        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();

        Image img = new Image(ImageDataFactory.create(imageInByte));
        img.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        img.setMarginTop(10f);
        img.setMarginBottom(10f);

        table.addCell(new Cell(2, 1).add(img).setBorder(Border.NO_BORDER));

        Paragraph companyP = new Paragraph("Two Brothers Home Renovation").setBold();
        companyP.setFontSize(14f);
        companyP.setTextAlignment(TextAlignment.LEFT);
        companyP.setFixedLeading(0);
        companyP.setMultipliedLeading(0.5f);
        companyP.setMarginBottom(7f);
        companyP.setMarginTop(16f);
        companyP.setMarginLeft(30f);
        table.addCell(new Cell(1, 1).add(companyP).setBorder(Border.NO_BORDER));

        Paragraph companyReg = new Paragraph("Reg No: SA0551750-X");
        companyReg.setFontSize(11f);
        companyReg.setTextAlignment(TextAlignment.LEFT);
        companyReg.setFixedLeading(0);
        companyReg.setMultipliedLeading(1);
        companyReg.setMarginBottom(5f);
        companyReg.setMarginLeft(30f);
        table.addCell(new Cell(1,1).add(companyReg).setBorder(Border.NO_BORDER));

        table.addCell(new Cell(1,2).add(separator));

        table.setBorder(new DottedBorder(2f));
    }
}
