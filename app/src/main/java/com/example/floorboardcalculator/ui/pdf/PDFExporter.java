package com.example.floorboardcalculator.ui.pdf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.constant.BuildingType;
import com.example.floorboardcalculator.core.constant.StateList;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DottedBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PDFExporter implements Serializable {
    private static final String APP_PREF = "TWO_BROTHER_SETTING";

    private Context parent;
    private final Customer customer;
    private PDFDoneListener listener;
    private PdfFont mainFont;
    private PreferenceItem setting;
    private List<FloorType> floorTypes;
    private Config rateConfig;
    private double totalArea = 0.0;

    public PDFExporter(Context ctx, @NonNull Customer customer, PDFDoneListener listener) {
        this.parent = ctx;
        this.customer = customer;
        this.listener = listener;
        initSetting();
    }

    public PDFExporter(Context ctx, @NonNull Customer customer, PDFDoneListener listener, List<FloorType> types, Config rateConfig) {
        this.parent = ctx;
        this.customer = customer;
        this.listener = listener;
        this.floorTypes = types;
        this.rateConfig = rateConfig;
        initSetting();
    }

    public void generate() {
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            String fileName = "CustomerPlan_" + customer.get_id().toHexString();

            String mFilePath = Environment.getExternalStorageDirectory() + "/" + fileName + ".pdf";
            String dirPath = Environment.getExternalStorageDirectory() + "";

            // PDF Core Initialize
            PdfWriter writer = new PdfWriter(mFilePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A4);

            // Document Creator
            Document doc = new Document(pdfDocument);
            mainFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
            doc.setFont(mainFont);

            // Header footer listeners
            PageHeader header = new PageHeader(doc, parent);
            PageFooter footer = new PageFooter(doc);
            pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, header);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, footer);
            float topMargin = 20 + header.getTableHeight();
            doc.setTopMargin(topMargin);

            // Company Heading
            Paragraph formTitle = new Paragraph("CUSTOMER FLOOR PLAN CHECKLIST").setBold();
            formTitle.setFontSize(13f);
            formTitle.setTextAlignment(TextAlignment.CENTER);
            formTitle.setMultipliedLeading(1);
            formTitle.setMarginTop(10);

            // 1st Table Item (Form Item)
            Table formInf = new Table(UnitValue.createPercentArray(12)).useAllAvailableWidth();
            formInf.setBorder(Border.NO_BORDER);
            formInf.setMarginTop(10.5f);

            Bitmap bmp = QrGenerator();
            Cell[] firstTb;

            if(bmp != null) {
                firstTb= new Cell[9];

                Text e1_1 = new Text("Record ID: "), e1_2 = new Text(customer.get_id().toHexString()).setBold();
                firstTb[0] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e1_1));
                firstTb[1] = new Cell(1, 6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e1_2));

                Image qr_img = new Image(ImageDataFactory.create(bmpCompressor(bmp)));
                qr_img.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                qr_img.setMarginTop(-2.0f);
                firstTb[2] = new Cell(4, 4).add(qr_img);

                Text e2_1 = new Text("Print Date: "), e2_2 = new Text(dateFormat.format(new Date())).setBold();
                firstTb[3] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e2_1));
                firstTb[4] = new Cell(1,6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e2_2));

                Text e4_1 = new Text("Create Date: "), e4_2 = new Text(dateFormat.format(customer.getAddDate())).setBold();
                firstTb[5] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e4_1));
                firstTb[6] = new Cell(1,6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e4_2));

                Text e3_1 = new Text("Referral: "), e3_2 = new Text(customer.getReferral().equals("-")?"No Referral":customer.getReferral()).setBold();
                firstTb[7] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e3_1));
                firstTb[8] = new Cell(1, 6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e3_2));

            }
            else {
                firstTb = new Cell[8];

                Text e1_1 = new Text("Record ID: "), e1_2 = new Text(customer.get_id().toHexString()).setBold();
                firstTb[0] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e1_1));
                firstTb[1] = new Cell(1, 6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e1_2));

                Text e2_1 = new Text("Print Date: "), e2_2 = new Text(dateFormat.format(new Date())).setBold();
                firstTb[2] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e2_1));
                firstTb[3] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e2_2));

                Text e3_1 = new Text("Referral: "), e3_2 = new Text(customer.getReferral().equals("-")?"No Referral":customer.getReferral()).setBold();
                firstTb[4] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e3_1));
                firstTb[5] = new Cell(1, 6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e3_2));

                Text e4_1 = new Text("Create Date: "), e4_2 = new Text(dateFormat.format(customer.getAddDate())).setBold();
                firstTb[6] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e4_1));
                firstTb[7] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(e4_2));
            }

            for(Cell i : firstTb) {
                i.setBorder(Border.NO_BORDER);
                i.setFontSize(10.5f);
                formInf.addCell(i);
            }

            // 2nd Table Item
            Table custInf = new Table(UnitValue.createPercentArray(12)).useAllAvailableWidth();
            custInf.setBorder(Border.NO_BORDER);
            custInf.setMarginTop(11f);

            Paragraph secTableTitle = new Paragraph("Customer Information").setBold();
            secTableTitle.setFontSize(12f);
            secTableTitle.setMultipliedLeading(1);

            custInf.addCell(new Cell(1,12).setTextAlignment(TextAlignment.LEFT).add(secTableTitle).setBorder(new DottedBorder(2f)));

            Cell[] secTb = new Cell[16];

            Text c1_1 = new Text("Customer Name: "), c1_2 = new Text(customer.getCustName()).setBold();
            secTb[0] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c1_1));
            secTb[1] = new Cell(1, 4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c1_2));

            Text c2_1 = new Text("Building Type: "), c2_2 = new Text(BuildingType.getType(customer.getBuildingType() - 1)).setBold();
            secTb[2] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c2_1));
            secTb[3] = new Cell(1,4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c2_2));

            Text c3_1 = new Text("Contact No: "), c3_2 = new Text(customer.getContactNo()).setBold();
            secTb[4] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c3_1));
            secTb[5] = new Cell(1, 4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c3_2));

            Text c4_1 = new Text("Postal Code: "), c4_2 = new Text(customer.getPostalCode()).setBold();
            secTb[6] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c4_1));
            secTb[7] = new Cell(1,4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c4_2));

            Text c5_1 = new Text("Address: "), c5_2 = new Text(customer.getAddress()).setBold();
            secTb[8] = new Cell(2, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c5_1));
            secTb[9] = new Cell(2, 4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c5_2));

            Text c6_1 = new Text("City: "), c6_2 = new Text(customer.getCity()).setBold();
            secTb[10] = new Cell(1,2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c6_1));
            secTb[11] = new Cell(1,4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c6_2));

            Text c7_1 = new Text("State: "), c7_2 = new Text(StateList.getState(customer.getState())).setBold();
            secTb[12] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c7_1));
            secTb[13] = new Cell(1, 4).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c7_2));

            Text c8_1 = new Text("Notes: "), c8_2 = new Text((customer.getNotes().length() > 0) ? customer.getNotes() : "No notes.").setBold();
            secTb[14] = new Cell(1, 2).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c8_1)).setPaddingBottom(10f);
            secTb[15] = new Cell(1, 10).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c8_2)).setPaddingBottom(10f);

            for(Cell i : secTb) {
                i.setBorder(new DottedBorder(2f));
                i.setFontSize(10f);
                custInf.addCell(i);
            }

            // 3rd Table Item
            Table floorTb = new Table(UnitValue.createPercentArray(12)).useAllAvailableWidth();
            floorTb.setBorder(Border.NO_BORDER);
            floorTb.setMarginTop(11f);

            Paragraph tdTableTitle = new Paragraph("Floor Calculation Information").setBold();
            tdTableTitle.setFontSize(12f);
            tdTableTitle.setMultipliedLeading(1);

            floorTb.addCell(new Cell(1,12).setTextAlignment(TextAlignment.LEFT).add(tdTableTitle).setBorder(new DottedBorder(2f)));

            Cell[] thdTb = new Cell[10];

            double totalMeasured = 0.0; int extendType = -1;
            String txtMeasured_1, txtMeasured_2, txtSkirting_1, txtSkirting_2, txtExtended_1, txtExtended_2, txtCalculated_1, txtCalculated_2;
            Paragraph d1_2, d2_2, d3_2, d5_2;

            for(FloorPlan plan : customer.getFloorPlan()) {
                totalMeasured += (double) (plan.getWidth() * plan.getHeight());
            }

            txtMeasured_1 = String.format("%.2f", (totalMeasured / 10000f)) + " m";
            txtMeasured_2 = String.format("%.2f", (totalMeasured / 929f)) + " ft";

            txtSkirting_1 = String.format("%.2f", (customer.getFloorInf().getSkirtingLen() / 100f)) + " m";
            txtSkirting_2 = String.format("%.2f", (customer.getFloorInf().getSkirtingLen() / 30.48f)) + " ft";

            if(totalMeasured >= 371612f) { // equal to more than 400 sq ft (sq cm to sq ft)
                txtExtended_1 = String.format("%.2f", (totalMeasured/10000f)*0.05f) + " m";
                txtExtended_2 = String.format("%.2f", (totalMeasured/929f)*0.05f) + " ft";

                txtCalculated_1 = String.format("%.2f", (totalMeasured*1.05f)/10000f) + "m";
                txtCalculated_2 = String.format("%.2f", (totalMeasured*1.05f)/929f) + " ft";

                extendType = 3;

                totalArea = (totalMeasured*1.05f)/929f;
            }
            else if(totalMeasured >= 185806f) {  // equal to more than 200 sq ft (sq cm to sq ft)
                txtExtended_1 = String.format("%.2f", (totalMeasured/10000f)*0.08f) + " m";
                txtExtended_2 = String.format("%.2f", (totalMeasured/929f)*0.08f) + " ft";

                txtCalculated_1 = String.format("%.2f", (totalMeasured*1.08f)/10000f) + "m";
                txtCalculated_2 = String.format("%.2f", (totalMeasured*1.08f)/929f) + " ft";

                extendType = 2;

                totalArea = (totalMeasured*1.08f)/929f;
            }
            else {
                txtExtended_1 = String.format("%.2f", (totalMeasured/10000f)*0.1f) + " m";
                txtExtended_2 = String.format("%.2f", (totalMeasured/929f)*0.1f) + " ft";

                txtCalculated_1 = String.format("%.2f", (totalMeasured*1.1f)/10000f) + " m";
                txtCalculated_2 = String.format("%.2f", (totalMeasured*1.1f)/929f) + " ft";

                extendType = 1;

                totalArea = (totalMeasured*1.1f)/929f;
            }

            if(setting.isDoubleUnit()) {
                d1_2 = new Paragraph().add(txtMeasured_1).add(superSquare()).add(" / ").add(txtMeasured_2).add(superSquare()).setBold();
                d2_2 = new Paragraph().add(txtSkirting_1).add(" / ").add(txtSkirting_2).setBold();
                d3_2 = new Paragraph().add(txtExtended_1).add(superSquare()).add(" / ").add(txtExtended_2).add(superSquare())
                        .add(((extendType==1)?" (10%)":(extendType==2)?" (8%)":" (5%)")).setBold();
                d5_2 = new Paragraph().add(txtCalculated_1).add(superSquare()).add(" / ").add(txtCalculated_2).add(superSquare()).setBold();
            }
            else {
                if(setting.isUnit()) {
                    d1_2 = new Paragraph().add(txtMeasured_1).add(superSquare()).setBold();
                    d2_2 = new Paragraph().add(txtSkirting_1).setBold();
                    d3_2 = new Paragraph().add(txtExtended_1).add(superSquare()).add(((extendType==1)?" (10%)":(extendType==2)?" (8%)":" (5%)")).setBold();
                    d5_2 = new Paragraph().add(txtCalculated_1).add(superSquare()).setBold();
                }
                else {
                    d1_2 = new Paragraph().add(txtMeasured_2).add(superSquare()).setBold();
                    d2_2 = new Paragraph().add(txtSkirting_2).setBold();
                    d3_2 = new Paragraph().add(txtExtended_2).add(superSquare())
                            .add(((extendType==1)?" (10%)":(extendType==2)?" (8%)":" (5%)")).setBold();
                    d5_2 = new Paragraph().add(txtCalculated_2).add(superSquare()).setBold();
                }
            }

            Text d1_1 = new Text("Total Measured Area: ");
            thdTb[0] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(d1_1));
            thdTb[1] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(d1_2);

            Text d2_1 = new Text("Total Skirting Length: ");
            thdTb[2] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(d2_1));
            thdTb[3] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(d2_2);

            Text d3_1 = new Text("Extended Calculation: ");
            thdTb[4] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(d3_1));
            thdTb[5] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(d3_2);

            Text d4_1 = new Text("Has Curved Area: "), d4_2 = new Text((customer.getFloorInf().isCurvedArea())?"YES":"NO").setBold();
            thdTb[6] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(d4_1));
            thdTb[7] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(d4_2));

            Text d5_1 = new Text("Total Calculated Area (Chargeable Area): ");
            thdTb[8] = new Cell(1, 6).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph().add(d5_1));
            thdTb[9] = new Cell(1, 6).setTextAlignment(TextAlignment.LEFT).add(d5_2);

            for(Cell i : thdTb) {
                i.setBorder(new DottedBorder(2f));
                i.setFontSize(10f);
                floorTb.addCell(i);
            }

            // 4th Table Item
            Table planTb = new Table(UnitValue.createPercentArray(16)).useAllAvailableWidth();
            planTb.setBorder(Border.NO_BORDER);
            planTb.setMarginTop(11f);

            Paragraph forthTbTitle = new Paragraph("Floor Plan (Area)").setBold();
            forthTbTitle.setFontSize(12f);
            forthTbTitle.setMultipliedLeading(1);

            planTb.addCell(new Cell(1,16).setTextAlignment(TextAlignment.LEFT).add(forthTbTitle).setBorder(new DottedBorder(2f)));

            // (4th) add th
            Paragraph ttl1, ttl2, ttl3;

            if(setting.isDoubleUnit()) {
                ttl1 = new Paragraph("Length (mm / ft)");
                ttl2 = new Paragraph("Width (mm / ft)");
                ttl3 = new Paragraph("Area (m").add(superSquare()).add(" / ft").add(superSquare()).add(")");
            }
            else {
                if(setting.isUnit()) {
                    ttl1 = new Paragraph("Length (mm)");
                    ttl2 = new Paragraph("Width (mm)");
                    ttl3 = new Paragraph("Area (m").add(superSquare()).add(")");
                }
                else {
                    ttl1 = new Paragraph("Length (ft)");
                    ttl2 = new Paragraph("Width (ft)");
                    ttl3 = new Paragraph("Area (ft").add(superSquare()).add(")");
                }
            }

            planTb.addCell(new Cell(1,1).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("No")).setFontSize(10f).setBorder(new DottedBorder(2f)));
            planTb.addCell(new Cell(1,6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("Area Name")).setFontSize(10f).setBorder(new DottedBorder(2f)));
            planTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(ttl1).setFontSize(10f).setBorder(new DottedBorder(2f)));
            planTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(ttl2).setFontSize(10f).setBorder(new DottedBorder(2f)));
            planTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(ttl3).setFontSize(10f).setBorder(new DottedBorder(2f)));

            for(int i=0; i<customer.getFloorPlan().size(); i++) {
                FloorPlan plan = customer.getFloorPlan().get(i);
                Cell[] row = new Cell[5];

                row[0] = new Cell(1,1).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.valueOf(i+1))).setBorder(new DottedBorder(2f));
                row[1] = new Cell(1,6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph(plan.getName())).setBorder(new DottedBorder(2f));

                float planAreaMetric = (float) ((plan.getWidth() * plan.getHeight()) / 10000f);
                float planAreaImp = (float) ((plan.getWidth() * plan.getHeight()) / 929f);

                if(setting.isDoubleUnit()) {
                    row[2] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER)
                            .add(new Paragraph(String.format("%.1f", plan.getWidth() * 10f) + " / " + String.format("%.1f", plan.getWidth() / 30.48f))).setBorder(new DottedBorder(2f));
                    row[3] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER)
                            .add(new Paragraph(String.format("%.1f", plan.getHeight() * 10f) + " / " + String.format("%.1f", plan.getHeight() / 30.48f))).setBorder(new DottedBorder(2f));
                    row[4] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).setBold()
                            .add(new Paragraph(String.format("%.1f", planAreaMetric) + " / " + String.format("%.1f", planAreaImp))).setBorder(new DottedBorder(2f));
                }
                else {
                    if(setting.isUnit()) {
                        row[2] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.1f", plan.getWidth() * 10f))).setBorder(new DottedBorder(2f));
                        row[3] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.1f", plan.getHeight() * 10f))).setBorder(new DottedBorder(2f));
                        row[4] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.1f", planAreaMetric))).setBorder(new DottedBorder(2f)).setBold();
                    }
                    else {
                        row[2] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.1f", plan.getWidth() / 30.48f))).setBorder(new DottedBorder(2f));
                        row[3] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.1f", plan.getHeight() / 30.48f))).setBorder(new DottedBorder(2f));
                        row[4] = new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.1f", planAreaImp))).setBorder(new DottedBorder(2f)).setBold();
                    }
                }

                for(Cell t : row) {
                    t.setBorder(new DottedBorder(2f));
                    t.setFontSize(10f);
                    planTb.addCell(t);
                }
            }

            Paragraph p = new Paragraph("Computer generated report, no signature required.").setFontSize(8f).setItalic().setTextAlignment(TextAlignment.CENTER).setMarginTop(30f);

            // Element Adding
            doc.add(formTitle);
            doc.add(formInf);
            doc.add(custInf);
            doc.add(floorTb);
            doc.add(planTb);

            if(setting.isPriceExp())
                doc.add(WritePricingSpace());

            doc.add(p);

            doc.close();

            listener.onPdfDone(mFilePath, dirPath);
        }
        catch(Exception e){
            Log.e("ERROR", Objects.requireNonNull(e.getMessage()));
        }
    }

    private byte @NotNull [] bmpCompressor(@NotNull Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return outputStream.toByteArray();
    }

    @Nullable
    private Bitmap QrGenerator() {
        String recordId = customer.get_id().toHexString();

        try{
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode("FLOORQR=" + recordId, BarcodeFormat.QR_CODE, 100, 100);
            BarcodeEncoder encoder = new BarcodeEncoder();

            return encoder.createBitmap(matrix);
        }
        catch(Exception e) {
            return null;
        }
    }

    @NotNull
    private Table WritePricingSpace() throws IOException {
        float range1dist = (float)Double.parseDouble(rateConfig.data1) / 929f;
        float range2dist = (float)Double.parseDouble(rateConfig.data2) / 929f;
        float penalty = (float)Double.parseDouble(rateConfig.data4) / 929f;

        Table priceTb = new Table(UnitValue.createPercentArray(16)).useAllAvailableWidth();
        priceTb.setBorder(Border.NO_BORDER);
        priceTb.setMarginTop(11f);

        Paragraph fifthTitle = new Paragraph("Product Price").setBold();
        fifthTitle.setFontSize(12f);
        fifthTitle.setMultipliedLeading(1);

        priceTb.addCell(new Cell(1,16).setTextAlignment(TextAlignment.LEFT).add(fifthTitle).setBorder(new DottedBorder(2f)));

        Cell[] firstData = new Cell[4];

        Text c1_1 = new Text("Currency: "), c1_2 = new Text("Ringgit Malaysia (RM)").setBold();
        firstData[0] = new Cell(1, 3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c1_1));
        firstData[1] = new Cell(1, 5).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c1_2));

        Text c2_1 = new Text("Penalty: "), c2_2 = new Text((totalArea >= penalty)?"0.00":"200.00").setBold();
        firstData[2] = new Cell(1,3).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c2_1));
        firstData[3] = new Cell(1,5).setTextAlignment(TextAlignment.LEFT).add(new Paragraph().add(c2_2));

        for(Cell i : firstData){
            i.setBorder(new DottedBorder(2f));
            i.setFontSize(10f);
            priceTb.addCell(i);
        }

        Paragraph header1, header2;

        if(setting.isUnit()) {
            header1 = new Paragraph("Price per m").add(superSquare());
            header2 = new Paragraph("Quantity (m").add(superSquare()).add(")");
        }
        else {
            header1 = new Paragraph("Price per ft").add(superSquare());
            header2 = new Paragraph("Quantity (ft").add(superSquare()).add(")");
        }

        priceTb.addCell(new Cell(1,1).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("No")).setFontSize(10f).setBorder(new DottedBorder(2f)));
        priceTb.addCell(new Cell(1,6).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("Product")).setFontSize(10f).setBorder(new DottedBorder(2f)));
        priceTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(header1).setFontSize(10f).setBorder(new DottedBorder(2f)));
        priceTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(header2).setFontSize(10f).setBorder(new DottedBorder(2f)));
        priceTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("Total Amount")).setFontSize(10f).setBorder(new DottedBorder(2f)));

        int nums = 0;
        int totalCeilArea = (int) Math.ceil(totalArea);

        for(FloorType ft : floorTypes) {
            Paragraph unitP, ttlP, qtyP;
            float unitPrice; double ttlAmt;

            if(totalCeilArea > range2dist) {
                if(setting.isUnit()) {
                    unitPrice = (float) (ft.base_15 * 10.764);
                    ttlAmt = unitPrice * (totalCeilArea / 10.764);
                }
                else {
                    unitPrice = (float) (ft.base_15);
                    ttlAmt = unitPrice * totalCeilArea;
                }
            }
            else if(totalCeilArea > range1dist) {
                if(setting.isUnit()) {
                    unitPrice = (float) (ft.base_8 * 10.764);
                    ttlAmt = unitPrice * (totalCeilArea / 10.764);
                }
                else {
                    unitPrice = (float) (ft.base_8);
                    ttlAmt = unitPrice * totalCeilArea;
                }
            }
            else {
                if(totalCeilArea < penalty) {
                    if(setting.isUnit()) {
                        unitPrice = (float) (ft.base * 10.764);
                        ttlAmt = (unitPrice * (totalCeilArea / 10.764)) + 200;
                    }
                    else {
                        unitPrice = (float) (ft.base);
                        ttlAmt = (unitPrice * totalCeilArea) + 200;
                    }
                }
                else {
                    if(setting.isUnit()) {
                        unitPrice = (float) (ft.base * 10.764);
                        ttlAmt = unitPrice * (totalCeilArea / 10.764);
                    }
                    else {
                        unitPrice = (float) (ft.base);
                        ttlAmt = unitPrice * totalCeilArea;
                    }
                }
            }

            unitP = new Paragraph(String.format("%.2f", unitPrice));
            qtyP = new Paragraph((setting.isUnit())? String.format("%.2f", totalCeilArea/10.764) : String.valueOf(totalCeilArea));
            ttlP = new Paragraph(String.format("%,.2f", ttlAmt)).setBold();

            priceTb.addCell(new Cell(1,1).setTextAlignment(TextAlignment.CENTER).setFontSize(10f).add(new Paragraph(String.valueOf(nums+1))).setBorder(new DottedBorder(2f)));
            priceTb.addCell(new Cell(1,6).setTextAlignment(TextAlignment.LEFT).setFontSize(10f).add(new Paragraph(ft.full)).setBorder(new DottedBorder(2f)));
            priceTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).setFontSize(10f).add(unitP).setBorder(new DottedBorder(2f)));
            priceTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).setFontSize(10f).add(qtyP).setBorder(new DottedBorder(2f)));
            priceTb.addCell(new Cell(1,3).setTextAlignment(TextAlignment.CENTER).setFontSize(10f).add(ttlP).setBorder(new DottedBorder(2f)));
            nums++;
        }

        return priceTb;
    }

    @NotNull
    private Text superSquare() throws IOException {
        PdfFont small = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

        Text superscript = new Text("2");
        superscript.setTextRise(5f);
        superscript.setFontSize(5);
        superscript.setFont(small);

        return superscript;
    }

    private void initSetting() {
        SharedPreferences preferences = parent.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        setting = new PreferenceItem();

        setting.setUnit(preferences.getBoolean("unit", false));
        setting.setDoubleUnit(preferences.getBoolean("doubleUnit", false));
        setting.setUnitSelect(preferences.getInt("unitSelect", -1));
        setting.setPriceExp(preferences.getBoolean("priceExp", true));
    }
}
