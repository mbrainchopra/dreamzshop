package org.example.dreamzshop.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.OrderItem;
import org.example.dreamzshop.service.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    public byte[] generateInvoice(Order order) {

        if (order == null) {
            throw new IllegalArgumentException("Order is required.");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot generate invoice for an order without items."
            );
        }

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4, 36, 36, 36, 36);

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            // =====================================================
            // FONTS
            // =====================================================

            Font companyFont = new Font(
                    Font.HELVETICA,
                    22,
                    Font.BOLD
            );

            Font invoiceFont = new Font(
                    Font.HELVETICA,
                    16,
                    Font.BOLD
            );

            Font sectionFont = new Font(
                    Font.HELVETICA,
                    11,
                    Font.BOLD
            );

            Font normalFont = new Font(
                    Font.HELVETICA,
                    9,
                    Font.NORMAL
            );

            Font smallFont = new Font(
                    Font.HELVETICA,
                    8,
                    Font.NORMAL
            );

            Font boldFont = new Font(
                    Font.HELVETICA,
                    9,
                    Font.BOLD
            );

            // =====================================================
            // HEADER
            // =====================================================

            PdfPTable headerTable =
                    new PdfPTable(2);

            headerTable.setWidthPercentage(100);

            headerTable.setWidths(
                    new float[]{65, 35}
            );

            PdfPCell companyCell =
                    new PdfPCell();

            companyCell.setBorder(Rectangle.NO_BORDER);

            Paragraph companyName =
                    new Paragraph(
                            "DREAMZ SHOP",
                            companyFont
                    );

            companyName.setAlignment(
                    Element.ALIGN_LEFT
            );

            companyCell.addElement(companyName);

            Paragraph companyTagline =
                    new Paragraph(
                            "Your trusted online shopping destination",
                            smallFont
                    );

            companyCell.addElement(companyTagline);

            headerTable.addCell(companyCell);

            PdfPCell invoiceCell =
                    new PdfPCell();

            invoiceCell.setBorder(Rectangle.NO_BORDER);

            Paragraph invoiceTitle =
                    new Paragraph(
                            "INVOICE",
                            invoiceFont
                    );

            invoiceTitle.setAlignment(
                    Element.ALIGN_RIGHT
            );

            invoiceCell.addElement(invoiceTitle);

            Paragraph invoiceNumber =
                    new Paragraph(
                            "Invoice No: " +
                                    generateInvoiceNumber(order),
                            normalFont
                    );

            invoiceNumber.setAlignment(
                    Element.ALIGN_RIGHT
            );

            invoiceCell.addElement(invoiceNumber);

            headerTable.addCell(invoiceCell);

            document.add(headerTable);

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // ORDER INFORMATION
            // =====================================================

            PdfPTable orderInfoTable =
                    new PdfPTable(2);

            orderInfoTable.setWidthPercentage(100);

            orderInfoTable.setWidths(
                    new float[]{50, 50}
            );

            PdfPCell orderCell =
                    createBorderlessCell();

            orderCell.addElement(
                    new Paragraph(
                            "ORDER INFORMATION",
                            sectionFont
                    )
            );

            orderCell.addElement(
                    new Paragraph(
                            "Order No: " +
                                    safe(order.getOrderNumber()),
                            normalFont
                    )
            );

            String orderDate =
                    order.getCreatedAt() != null
                            ? order.getCreatedAt()
                            .format(DATE_FORMATTER)
                            : "-";

            orderCell.addElement(
                    new Paragraph(
                            "Order Date: " + orderDate,
                            normalFont
                    )
            );

            orderCell.addElement(
                    new Paragraph(
                            "Order Status: " +
                                    safeEnum(order.getOrderStatus()),
                            normalFont
                    )
            );

            orderInfoTable.addCell(orderCell);

            PdfPCell paymentCell =
                    createBorderlessCell();

            paymentCell.addElement(
                    new Paragraph(
                            "PAYMENT INFORMATION",
                            sectionFont
                    )
            );

            paymentCell.addElement(
                    new Paragraph(
                            "Payment Method: " +
                                    safeEnum(order.getPaymentMethod()),
                            normalFont
                    )
            );

            paymentCell.addElement(
                    new Paragraph(
                            "Payment Status: " +
                                    safeEnum(order.getPaymentStatus()),
                            normalFont
                    )
            );

            orderInfoTable.addCell(paymentCell);

            document.add(orderInfoTable);

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // CUSTOMER / SHIPPING ADDRESS
            // =====================================================

            PdfPTable customerTable =
                    new PdfPTable(1);

            customerTable.setWidthPercentage(100);

            PdfPCell customerCell =
                    new PdfPCell();

            customerCell.setPadding(8);

            customerCell.addElement(
                    new Paragraph(
                            "BILL TO / SHIPPING ADDRESS",
                            sectionFont
                    )
            );

            customerCell.addElement(
                    new Paragraph(
                            safe(order.getShippingFullName()),
                            boldFont
                    )
            );

            customerCell.addElement(
                    new Paragraph(
                            "Phone: " +
                                    safe(order.getShippingPhone()),
                            normalFont
                    )
            );

            customerCell.addElement(
                    new Paragraph(
                            safe(order.getShippingAddressLine1()),
                            normalFont
                    )
            );

            if (order.getShippingAddressLine2() != null
                    && !order.getShippingAddressLine2().isBlank()) {

                customerCell.addElement(
                        new Paragraph(
                                order.getShippingAddressLine2(),
                                normalFont
                        )
                );
            }

            customerCell.addElement(
                    new Paragraph(
                            safe(order.getShippingCity()) +
                                    ", " +
                                    safe(order.getShippingState()) +
                                    " - " +
                                    safe(order.getShippingPincode()),
                            normalFont
                    )
            );

            if (order.getShippingLandmark() != null
                    && !order.getShippingLandmark().isBlank()) {

                customerCell.addElement(
                        new Paragraph(
                                "Landmark: " +
                                        order.getShippingLandmark(),
                                normalFont
                        )
                );
            }

            customerTable.addCell(customerCell);

            document.add(customerTable);

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // PRODUCT TABLE
            // =====================================================

            PdfPTable productTable =
                    new PdfPTable(5);

            productTable.setWidthPercentage(100);

            productTable.setWidths(
                    new float[]{35, 18, 10, 17, 20}
            );

            addHeaderCell(
                    productTable,
                    "Product",
                    boldFont
            );

            addHeaderCell(
                    productTable,
                    "SKU",
                    boldFont
            );

            addHeaderCell(
                    productTable,
                    "Qty",
                    boldFont
            );

            addHeaderCell(
                    productTable,
                    "Unit Price",
                    boldFont
            );

            addHeaderCell(
                    productTable,
                    "Total",
                    boldFont
            );

            for (OrderItem item : order.getItems()) {

                addProductCell(
                        productTable,
                        safe(item.getProductName()),
                        normalFont,
                        Element.ALIGN_LEFT
                );

                addProductCell(
                        productTable,
                        safe(item.getProductSku()),
                        normalFont,
                        Element.ALIGN_LEFT
                );

                addProductCell(
                        productTable,
                        item.getQuantity() != null
                                ? String.valueOf(item.getQuantity())
                                : "0",
                        normalFont,
                        Element.ALIGN_CENTER
                );

                addProductCell(
                        productTable,
                        money(item.getUnitPrice()),
                        normalFont,
                        Element.ALIGN_RIGHT
                );

                addProductCell(
                        productTable,
                        money(item.getSubtotal()),
                        normalFont,
                        Element.ALIGN_RIGHT
                );
            }

            document.add(productTable);

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // SUMMARY
            // =====================================================

            PdfPTable summaryTable =
                    new PdfPTable(2);

            summaryTable.setWidthPercentage(45);

            summaryTable.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            summaryTable.setWidths(
                    new float[]{60, 40}
            );

            addSummaryRow(
                    summaryTable,
                    "Subtotal",
                    money(order.getSubtotal()),
                    normalFont,
                    false
            );

            addSummaryRow(
                    summaryTable,
                    "Offer Discount",
                    "-" + money(order.getDiscountAmount()),
                    normalFont,
                    false
            );

            addSummaryRow(
                    summaryTable,
                    "Coupon Discount",
                    "-" + money(order.getCouponDiscount()),
                    normalFont,
                    false
            );

            addSummaryRow(
                    summaryTable,
                    "Tax",
                    money(order.getTaxAmount()),
                    normalFont,
                    false
            );

            addSummaryRow(
                    summaryTable,
                    "Delivery Charge",
                    money(order.getDeliveryCharge()),
                    normalFont,
                    false
            );

            addSummaryRow(
                    summaryTable,
                    "GRAND TOTAL",
                    money(order.getGrandTotal()),
                    boldFont,
                    true
            );

            document.add(summaryTable);

            document.add(
                    new Paragraph(" ")
            );

            // =====================================================
            // CUSTOMER NOTES
            // =====================================================

            if (order.getCustomerNotes() != null
                    && !order.getCustomerNotes().isBlank()) {

                PdfPTable notesTable =
                        new PdfPTable(1);

                notesTable.setWidthPercentage(100);

                PdfPCell notesCell =
                        new PdfPCell();

                notesCell.setPadding(8);

                notesCell.addElement(
                        new Paragraph(
                                "CUSTOMER NOTES",
                                sectionFont
                        )
                );

                notesCell.addElement(
                        new Paragraph(
                                order.getCustomerNotes(),
                                normalFont
                        )
                );

                notesTable.addCell(notesCell);

                document.add(notesTable);

                document.add(
                        new Paragraph(" ")
                );
            }

            // =====================================================
            // FOOTER
            // =====================================================

            Paragraph footer =
                    new Paragraph(
                            "Thank you for shopping with Dreamz Shop!",
                            sectionFont
                    );

            footer.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(footer);

            Paragraph footerSmall =
                    new Paragraph(
                            "This is a computer-generated invoice.",
                            smallFont
                    );

            footerSmall.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(footerSmall);

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException e) {

            throw new IllegalStateException(
                    "Failed to generate invoice PDF.",
                    e
            );
        }
    }

    // =============================================================
    // INVOICE NUMBER
    // =============================================================

    private String generateInvoiceNumber(Order order) {

        if (order.getId() != null) {

            return String.format(
                    "INV-DS-%06d",
                    order.getId()
            );
        }

        if (order.getOrderNumber() != null
                && !order.getOrderNumber().isBlank()) {

            return "INV-DS-" + order.getOrderNumber();
        }

        return "INV-DS-GENERATED";
    }

    // =============================================================
    // HEADER CELL
    // =============================================================

    private void addHeaderCell(
            PdfPTable table,
            String text,
            Font font
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(text, font)
                );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(6);

        table.addCell(cell);
    }

    // =============================================================
    // PRODUCT CELL
    // =============================================================

    private void addProductCell(
            PdfPTable table,
            String text,
            Font font,
            int alignment
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(text, font)
                );

        cell.setHorizontalAlignment(
                alignment
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(5);

        table.addCell(cell);
    }

    // =============================================================
    // SUMMARY ROW
    // =============================================================

    private void addSummaryRow(
            PdfPTable table,
            String label,
            String value,
            Font font,
            boolean grandTotal
    ) {

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(label, font)
                );

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(value, font)
                );

        labelCell.setHorizontalAlignment(
                Element.ALIGN_LEFT
        );

        valueCell.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        labelCell.setPadding(5);
        valueCell.setPadding(5);

        if (grandTotal) {

            labelCell.setBorderWidthTop(1.5f);
            valueCell.setBorderWidthTop(1.5f);

            labelCell.setPaddingTop(7);
            valueCell.setPaddingTop(7);
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // =============================================================
    // BORDERLESS CELL
    // =============================================================

    private PdfPCell createBorderlessCell() {

        PdfPCell cell =
                new PdfPCell();

        cell.setBorder(
                Rectangle.NO_BORDER
        );

        cell.setPadding(5);

        return cell;
    }

    // =============================================================
    // MONEY FORMAT
    // =============================================================

    private String money(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return "Rs. " + amount.setScale(
                2,
                java.math.RoundingMode.HALF_UP
        );
    }

    // =============================================================
    // SAFE STRING
    // =============================================================

    private String safe(String value) {

        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

    // =============================================================
    // SAFE ENUM
    // =============================================================

    private String safeEnum(Enum<?> value) {

        if (value == null) {
            return "-";
        }

        return value.name()
                .replace("_", " ");
    }
}