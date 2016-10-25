/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.versul.main;

import com.versul.reports.TemplateDefault;
import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.HyperLinkBuilder;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListCellBuilder;
import net.sf.dynamicreports.report.builder.component.HyperLinkComponentBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.view.JRHyperlinkListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainClass {

    private final int DPI = 72;
    private final double CM_PER_INCH = 2.54D;
    private final TemplateDefault templateDefault = new TemplateDefault();
    private final JSONParser parser = new JSONParser();
    private JasperReportBuilder report = null;
    private StyleBuilder cardStyle;
    private long totalRows;
    private String reportName;
    private String reportTitle;
    private List<String> extractFields;
    private List<JSONObject> tableColumnName;
    private JSONObject filters;
    private JSONObject aggregations;
    private String created;
    private int startPage;

    public MainClass() {
        this.report = DynamicReports.report();
        createCardStyle();
    }

    public static void main(String[] args)
            throws Exception {

        try {
            MainClass main = new MainClass();
            if (args.length == 1) {
                log("cover");
                main.extractAndValidateCoverParams(args[0]);
                main.createCoverPage();
            } else if (args.length > 1) {
                log("report");
                main.extractAndValidateReportParams(args[1]);
                main.createReport();
            } else {
                throw new IllegalArgumentException("N��mero de par��metros inv��lido ou par��metros n��o foram recebidos.");
            }
        } catch (Exception e) {
            log(e);
        }
    }

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MainClass.class);

    private static void log(Throwable e)
            throws IOException {
        logger.info("paaah!", e);
    }

    private void createReport()
            throws IOException, DRException, Exception {
        log("CreatedInstance");
        reportCreateInstance();
        log("ReceiptData");
        reportReceiptData();
        log("Configure");
        reportConfigure();
        log("CreateColumns");
        reportCreateColumns();
        log("to file");
        reportToFile();
        log("done");
    }

    private void createCoverPage()
            throws Exception {
        log("CreatedInstance");
        reportCreateInstance();
        log("Configure");
        reportConfigure();
        this.report.setDataSource(new JREmptyDataSource());
        log("Createinfopage");
        reportCreateInfoPage();
        log("covert to file");
        reportToFile();
        log("done");
    }

    private void extractAndValidateReportParams(String jsonArgs)
            throws ParseException {
        JSONObject arguments = (JSONObject) this.parser.parse(jsonArgs);

        Object reportNameObject = arguments.get("report_name");
        if (reportNameObject == null) {
            throw new IllegalArgumentException("'report_name' argument is invalid");
        }
        this.reportName = reportNameObject.toString();
        this.extractFields = ((JSONArray) arguments.get("fields"));
        this.tableColumnName = ((JSONArray) arguments.get("columns"));
        this.created = ((String) arguments.get("created"));
        this.startPage = Integer.valueOf(arguments.get("page").toString()).intValue();
        if (this.reportName.isEmpty()) {
            throw new IllegalArgumentException("'report_name' argument is invalid");
        }
        if ((this.tableColumnName == null) || (this.tableColumnName.isEmpty())) {
            throw new IllegalArgumentException("'columns' argument is invalid");
        }
        if (this.extractFields.size() != this.tableColumnName.size()) {
            throw new IllegalArgumentException("fields and columns have different sizes.");
        }
    }

    private void extractAndValidateCoverParams(String jsonArgs)
            throws ParseException {
        JSONObject arguments = (JSONObject) this.parser.parse(jsonArgs);

        Object reportNameObject = arguments.get("report_name");
        Object reportTitleObject = arguments.get("report_title");
        if (reportNameObject == null) {
            throw new IllegalArgumentException("'report_name' argument is invalid");
        }
        if (reportTitleObject == null) {
            throw new IllegalArgumentException("'report_title' argument is invalid");
        }
        this.reportName = reportNameObject.toString();
        this.reportTitle = reportTitleObject.toString();
        this.extractFields = ((JSONArray) arguments.get("fields"));
        this.tableColumnName = ((JSONArray) arguments.get("columns"));
        this.filters = ((JSONObject) arguments.get("filters"));
        this.aggregations = ((JSONObject) arguments.get("aggs"));
        this.totalRows = Long.valueOf(arguments.get("count").toString()).longValue();
        this.created = ((String) arguments.get("created"));
        this.startPage = 1;
        if (this.reportName.isEmpty()) {
            throw new IllegalArgumentException("'report_name' argument is invalid");
        }
        if (this.reportTitle.isEmpty()) {
            throw new IllegalArgumentException("'report_title' argument is invalid");
        }
        if ((this.extractFields == null) || (this.extractFields.isEmpty())) {
            throw new IllegalArgumentException("'fields' argument is invalid");
        }
        if ((this.tableColumnName == null) || (this.tableColumnName.isEmpty())) {
            throw new IllegalArgumentException("'columns' argument is invalid");
        }
        if (this.extractFields.size() != this.tableColumnName.size()) {
            throw new IllegalArgumentException("fields and columns have different sizes.");
        }
    }

    private void reportCreateInstance() {
        this.report = DynamicReports.report();
    }

    private void reportReceiptData()
            throws JRException, DRException {
        this.report.setStartPageNumber(Integer.valueOf(this.startPage));
        JsonDataSource jsonDataSource = new JsonDataSource(System.in);
        this.report.setDataSource(jsonDataSource);
    }

    private static void log(String msg)
            throws IOException {
    }

    private int getDefaultColumnWidth(int totalColumns, List<JSONObject> columns) {
        int numColumnsWithWidth = 0;
        int accumWidth = 0;
        for (int i = 0; i < totalColumns; i++) {
            JSONObject column = (JSONObject) columns.get(i);
            try {
                log(column.toJSONString());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            Object width = column.get("width");
            if (width != null) {
                accumWidth += new Integer(String.valueOf(width)).intValue();
                numColumnsWithWidth++;
            }
        }
        if (numColumnsWithWidth == 0) {
            return 0;
        }
        int pageWithoutMargins = PageType.A4.getHeight() - 10 - 10;

        double accumWidthInPixels = mmToPixel(accumWidth);

        int defaultWidth = new Double((pageWithoutMargins - accumWidthInPixels) / (totalColumns - numColumnsWithWidth)).intValue();
        try {
            log(String.valueOf(PageType.A4.getHeight()));
            log(String.valueOf(accumWidthInPixels));
            log(String.valueOf(pageWithoutMargins));
            log(String.valueOf(defaultWidth));
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        return defaultWidth;
    }

    private double mmToPixel(int mm) {
        double pixelsPerMM = 2.8346456692913384D;
        return pixelsPerMM * mm;
    }

    private void reportCreateColumns()
            throws DRException {
        int cont = 0;
        DynamicReports.margin().getMargin().getTop();
        int size = this.extractFields.size();
        int defaultWidth = getDefaultColumnWidth(size, this.tableColumnName);
        while (cont < size) {
            JSONObject column = (JSONObject) this.tableColumnName.get(cont);
            String label = (String) column.get("label");
            String align = (String) column.get("align");
            Object widthParam = column.get("width");
            Integer width = null;
            if (widthParam == null) {
                width = Integer.valueOf(defaultWidth);
            } else {
                width = Integer.valueOf(new Double(mmToPixel(Integer.valueOf(String.valueOf(widthParam)).intValue())).intValue());
            }
            
            this.report.columns(new ColumnBuilder[]{
                DynamicReports.col
                .column(label, (String) this.extractFields.get(cont), DataTypes.stringType())
                .setFixedWidth(width)
                .setStretchWithOverflow(Boolean.valueOf(false))
                .setStyle(getAlignmentStyle(align))
            });

            cont++;
        }
    }

    private StyleBuilder getAlignmentStyle(String alignment) {
        HorizontalAlignment align = HorizontalAlignment.LEFT;
        if (alignment != null) {
            if (alignment.equalsIgnoreCase("CENTER")) {
                align = HorizontalAlignment.CENTER;
            } else if (alignment.equalsIgnoreCase("RIGHT")) {
                align = HorizontalAlignment.RIGHT;
            }
        }
        return (StyleBuilder) ((StyleBuilder) ((StyleBuilder) Styles.style().setHorizontalAlignment(align)).setLeftPadding(Integer.valueOf(2))).setRightPadding(Integer.valueOf(2));
    }

    private void reportConfigure() {
        this.report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);

        this.report.setTemplate(this.templateDefault);
        HyperLinkComponentBuilder creationTime;
        if (this.created == null) {
            creationTime = Components.currentDate().setPattern("dd/MM/yyyy HH:mm:ss").setHorizontalAlignment(HorizontalAlignment.RIGHT);
        } else {
            creationTime = DynamicReports.cmp.text(this.created).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        HorizontalListBuilder footerList = DynamicReports.cmp.horizontalList(new ComponentBuilder[]{DynamicReports.cmp
            .text("").setHorizontalAlignment(HorizontalAlignment.LEFT),
            Components.pageNumber().setHorizontalAlignment(HorizontalAlignment.CENTER), creationTime});

        this.report.pageFooter(new ComponentBuilder[]{footerList});
    }

    private void reportCreateInfoPage()
            throws MalformedURLException, Exception {
        HorizontalListBuilder horizontalList = DynamicReports.cmp.horizontalList();
        horizontalList.setStyle(DynamicReports.stl.style(Integer.valueOf(10)));

        ComponentBuilder<?, ?> compFilters = createFiltersComponent("Filtros Utilizados");
        ComponentBuilder<?, ?> compGroups = createGroupsComponent("Grupos (Top 10)");
        ComponentBuilder<?, ?> compSum = createSumComponent("Totais");
        if (compFilters != null) {
            horizontalList.add(new HorizontalListCellBuilder[]{DynamicReports.cmp.hListCell(compFilters).heightFixedOnTop()});
        }
        if (compGroups != null) {
            horizontalList.add(new HorizontalListCellBuilder[]{DynamicReports.cmp.hListCell(compGroups).heightFixedOnTop()});
        }
        if (compSum != null) {
            horizontalList.add(new HorizontalListCellBuilder[]{DynamicReports.cmp.hListCell(compSum).heightFixedOnTop()});
        }
        this.report.title(new ComponentBuilder[]{
            createTitleComponent(this.reportTitle), horizontalList, DynamicReports.cmp
            .verticalGap(10)});
    }

    private void reportToFile()
            throws DRException, JRException {
        JasperPdfExporterBuilder pdfExporter = DynamicReports.export.pdfExporter(System.out);
        this.report.toPdf(pdfExporter);
    }

    private ComponentBuilder<?, ?> createFiltersComponent(String label)
            throws IOException {
        if ((this.filters != null) && (!this.filters.isEmpty())) {
            List<String> andConditions = (List) this.filters.get("and");
            List<String> orConditions = (List) this.filters.get("or");

            HorizontalListBuilder superList = DynamicReports.cmp.horizontalList();
            HorizontalListBuilder card = createCardComponent();
            VerticalListBuilder verticalList = DynamicReports.cmp.verticalList();
            if ((orConditions != null) && (!orConditions.isEmpty())) {
                verticalList.add(new ComponentBuilder[]{DynamicReports.cmp.text("Ao menos uma condi����o atendida:").setStyle(TemplateDefault.boldStyleFont10)});
                for (String extractField : orConditions) {
                    verticalList.add(new ComponentBuilder[]{DynamicReports.cmp.text("    " + extractField).setStyle(getStyleMarkedUp())});
                }
            }
            if ((andConditions != null) && (!andConditions.isEmpty())) {
                verticalList.add(new ComponentBuilder[]{DynamicReports.cmp.text("Todas as condi����es atendidas:").setStyle(TemplateDefault.boldStyleFont10)});
                for (String extractField : andConditions) {
                    verticalList.add(new ComponentBuilder[]{DynamicReports.cmp.text("    " + extractField).setStyle(getStyleMarkedUp())});
                }
            }
            card.add(new ComponentBuilder[]{verticalList});
            superList.add(new ComponentBuilder[]{card});

            return DynamicReports.cmp.verticalList(new ComponentBuilder[]{DynamicReports.cmp
                .text(label).setStyle(TemplateDefault.boldStyle), superList});
        }
        return null;
    }

    private ComponentBuilder<?, ?> createGroupsComponent(String label)
            throws IOException {
        if ((this.aggregations != null) && (this.aggregations.containsKey("group"))) {
            JSONArray groups = (JSONArray) this.aggregations.get("group");
            if ((groups != null) && (!groups.isEmpty())) {
                VerticalListBuilder listSuper = DynamicReports.cmp.verticalList();
                for (Object groupObj : groups) {
                    HorizontalListBuilder cardComponent = createCardComponent();

                    JSONObject groupField = (JSONObject) groupObj;
                    String key = (String) groupField.keySet().toArray()[0];
                    JSONArray group = (JSONArray) groupField.get(key);

                    VerticalListBuilder content = DynamicReports.cmp.verticalList();
                    content.add(new ComponentBuilder[]{DynamicReports.cmp.text(key)});
                    for (Object segmentObj : group) {
                        String groupText = "";

                        JSONObject segment = (JSONObject) segmentObj;
                        Object[] segmentKeys = segment.keySet().toArray();
                        for (Object statisticKey : segmentKeys) {
                            groupText = groupText + "    " + statisticKey.toString() + ": ";
                            JSONObject statistics = (JSONObject) segment.get(statisticKey);
                            Object[] statisticKeys = statistics.keySet().toArray();

                            String totalKey = statisticKeys[0].toString();
                            String totalObject = statistics.get(totalKey).toString();

                            groupText = groupText + "<b>(" + totalKey + "</b> : " + totalObject;
                            if (statisticKeys.length > 1) {
                                groupText = groupText + " | ";
                                String amountKey = statisticKeys[1].toString();
                                String amountObject = statistics.get(amountKey).toString();
                                groupText = groupText + "<b>" + amountKey + "</b> : " + amountObject;
                            }
                            groupText = groupText + ")";

                            content.add(new ComponentBuilder[]{DynamicReports.cmp.text(groupText).setStyle(getStyleMarkedUp())});
                        }
                    }
                    cardComponent.add(new ComponentBuilder[]{content});
                    listSuper.add(new ComponentBuilder[]{cardComponent});
                }
                return DynamicReports.cmp.verticalList(new ComponentBuilder[]{DynamicReports.cmp
                    .text(label).setStyle(TemplateDefault.boldStyle), listSuper});
            }
        }
        return null;
    }

    private ComponentBuilder<?, ?> createSumComponent(String label)
            throws Exception {
        if ((this.aggregations != null) && (this.aggregations.containsKey("sum"))) {
            JSONArray sumArray = (JSONArray) this.aggregations.get("sum");
            if ((sumArray != null) && (!sumArray.isEmpty())) {
                VerticalListBuilder listSuper = DynamicReports.cmp.verticalList();
                for (Object sumObject : sumArray) {
                    HorizontalListBuilder cardComponent = createCardComponent();
                    VerticalListBuilder content = DynamicReports.cmp.verticalList();
                    HorizontalListBuilder labelSum = DynamicReports.cmp.horizontalList();

                    JSONObject sumField = (JSONObject) sumObject;

                    String key = sumField.keySet().toArray()[0].toString();

                    String value = sumField.get(key).toString();

                    labelSum.add(new ComponentBuilder[]{DynamicReports.cmp.text("<b>" + key + "</b>: " + value).setStyle(getStyleMarkedUp())});
                    content.add(new ComponentBuilder[]{labelSum});

                    cardComponent.add(new ComponentBuilder[]{content});

                    listSuper.add(new ComponentBuilder[]{cardComponent});
                }
                return DynamicReports.cmp.verticalList(new ComponentBuilder[]{DynamicReports.cmp
                    .text(label).setStyle(TemplateDefault.boldStyle), listSuper});
            }
        }
        return null;
    }

    private HorizontalListBuilder createCardComponent() {
        HorizontalListBuilder cardComponent = DynamicReports.cmp.horizontalList().setBaseStyle((ReportStyleBuilder) DynamicReports.stl.style().setLeftPadding(Integer.valueOf(5)));
        cardComponent.setStyle(this.cardStyle);
        return cardComponent;
    }

    private void createCardStyle() {
        PenBuilder pen1Point = DynamicReports.stl.pen1Point().setLineColor(Color.white);
        this.cardStyle = ((StyleBuilder) DynamicReports.stl.style(pen1Point).setPadding(Integer.valueOf(10)));
        this.cardStyle.setBackgroundColor(new Color(224, 224, 224));
    }

    private StyleBuilder getStyleMarkedUp() {
        return (StyleBuilder) DynamicReports.stl.style().setMarkup(Markup.STYLED);
    }

    private ComponentBuilder<?, ?> createTitleComponent(String label)
            throws MalformedURLException {
        URL logo = new URL("http://static-files04.cdnandroid.com/76/43/3e/08/imagen-rotativo-rondon-0thumb.jpg");
        String urlOperacao = "https://rondonopolis.s2way.com/";
        HyperLinkBuilder link = DynamicReports.hyperLink(urlOperacao);

        JRHyperlinkListener a = new JRHyperlinkListener() {
            public void gotoHyperlink(JRPrintHyperlink jrph)
                    throws JRException {
                try {
                    Desktop.getDesktop().browse(new URI(jrph.getHyperlinkAnchor()));
                } catch (Exception localException) {
                }
            }
        };
        ComponentBuilder<?, ?> dynamicReportsComponent = DynamicReports.cmp.horizontalList(new ComponentBuilder[]{
            ((TextFieldBuilder) DynamicReports.cmp.text(this.reportTitle).setStyle(TemplateDefault.bold22CenteredStyle)).setHorizontalAlignment(HorizontalAlignment.LEFT), DynamicReports.cmp
            .text("Total de Registros: " + this.totalRows).setStyle(TemplateDefault.testecapiroto)});

        return DynamicReports.cmp.horizontalList().add(new ComponentBuilder[]{dynamicReportsComponent}).newRow().add(new ComponentBuilder[]{DynamicReports.cmp.line()}).newRow().add(new ComponentBuilder[]{DynamicReports.cmp.verticalGap(10)});
    }

    private void addCustomerAttribute(HorizontalListBuilder list, String label, String value) {
        if (value != null) {
            list.add(new ComponentBuilder[]{DynamicReports.cmp.text(label + ":").setFixedColumns(Integer.valueOf(10)).setStyle(TemplateDefault.boldStyleFont10), DynamicReports.cmp.text(value)}).newRow();
        }
    }
}
