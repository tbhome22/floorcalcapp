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

        new Canvas(canvas, rect)
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

        Paragraph companyP = new Paragraph("Two Brothers Home Renovation (SA0551750-X)").setBold();
        companyP.setFontSize(10f);
        companyP.setTextAlignment(TextAlignment.LEFT);
        companyP.setFixedLeading(0);
        companyP.setMultipliedLeading(0.5f);
        companyP.setMarginBottom(1f);
        companyP.setMarginTop(10f);
        companyP.setMarginLeft(30f);

        Paragraph slogan = new Paragraph("(Specialty in Smart Home Build & Flooring)").setItalic();
        slogan.setFontSize(5.5f);
        slogan.setTextAlignment(TextAlignment.LEFT);
        slogan.setMarginLeft(30f);

        table.addCell(new Cell(1, 1).add(companyP).add(slogan).setBorder(Border.NO_BORDER));

        Paragraph companyF1 = new Paragraph("12A, Lilitan Batu Maung, Taman Seri Indah");
        companyF1.setFontSize(7f);
        companyF1.setTextAlignment(TextAlignment.LEFT);
        companyF1.setMarginLeft(30f);
        companyF1.setMultipliedLeading(0.5f);

        Paragraph companyF2 = new Paragraph("11960 Batu Maung, Pulau Pinang");
        companyF2.setFontSize(7f);
        companyF2.setTextAlignment(TextAlignment.LEFT);
        companyF2.setMarginLeft(30f);

        Paragraph companyF3 = new Paragraph("H/P: 016-721 2087 (Kelvin) / 012-439 1156 (Oscar), Email: tb_homerenovation@gmail.com");
        companyF3.setFontSize(7f);
        companyF3.setTextAlignment(TextAlignment.LEFT);
        companyF3.setMarginLeft(30f);

        table.addCell(new Cell(1,1).add(companyF1).add(companyF2).add(companyF3).setBorder(Border.NO_BORDER).setMarginTop(4f));

        table.addCell(new Cell(1,2).add(separator));

        table.setBorder(new DottedBorder(2f));
    }
}
