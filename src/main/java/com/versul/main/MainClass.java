/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.versul.main;

import com.versul.reports.TemplateDefault;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.export;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import net.sf.dynamicreports.report.builder.HyperLinkBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory;
import net.sf.jasperreports.view.JRHyperlinkListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static net.sf.dynamicreports.report.builder.DynamicReports.hyperLink;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 *
 * @author desenv12
 */
public class MainClass {

    private final TemplateDefault templateDefault = new TemplateDefault();
    private final JSONParser parser = new JSONParser();
    private JasperReportBuilder report = null;
    private StyleBuilder cardStyle;
    private final int MAX_FILTERS_ROWS = 10;

    // Arguments
    private String reportName;
    private String reportTitle;
    private List<String> extractFields;
    private List<String> tableColumnName;
    private List<String> filters;
    private JSONObject aggregations;

    public MainClass() {
        report = report();
        createCardStyle();
    }

    public static void main(String[] args) throws Exception {

        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Número de parâmetros inválido ou parâmetros não foram recebidos.");
        }

        BasicConfigurator.configure();

        try {
            MainClass main = new MainClass();
            main.extractAndValidateParams(args[0]);
            main.createReport();
        } catch (Exception e) {
            log(e);
        }
    }

    private static Logger logger = Logger.getLogger(MainClass.class);

    private static void log(Throwable e) throws IOException {
        logger.info("paaah!", e);
    }

    private static void log(String msg) throws IOException {
//        logger.info(msg);
    }

    private void extractAndValidateParams(String jsonArgs) throws ParseException {

        JSONObject arguments = (JSONObject) parser.parse(jsonArgs);

        Object reportNameObject = arguments.get("report_name");
        Object reportTitleObject = arguments.get("report_title");

        if (reportNameObject == null) {
            throw new IllegalArgumentException("'report_name' argument is invalid");
        }

        if (reportTitleObject == null) {
            throw new IllegalArgumentException("'report_title' argument is invalid");
        }

        reportName = reportNameObject.toString();
        reportTitle = reportTitleObject.toString();
        extractFields = (JSONArray) arguments.get("fields");
        tableColumnName = (JSONArray) arguments.get("columns");
        filters = (List) arguments.get("filters");
        aggregations = (JSONObject) arguments.get("aggs");

        if (reportName.isEmpty()) {
            throw new IllegalArgumentException("'report_name' argument is invalid");
        }

        if (reportTitle.isEmpty()) {
            throw new IllegalArgumentException("'report_title' argument is invalid");
        }

        if (extractFields == null || extractFields.isEmpty()) {
            throw new IllegalArgumentException("'fields' argument is invalid");
        }

        if (tableColumnName == null || tableColumnName.isEmpty()) {
            throw new IllegalArgumentException("'columns' argument is invalid");
        }

        if (extractFields.size() != tableColumnName.size()) {
            throw new IllegalArgumentException("fields and columns have a different sizes.");
        }
    }

    private void createReport() throws SQLException, IOException, JRException, DRException {
        log("CreatedInstance");
        reportCreateInstance();
        log("ReceiptData");
        reportReceiptData();
        log("Configure");
        reportConfigure();
        log("CreateColumns");
        reportCreateColumns();
        log("Createinfopage");
        reportCreateInfoPage();
        log("export");
        reportExport();
        log("done");
    }

    private void reportCreateInstance() {
        report = DynamicReports.report();
//        report.title(Components.text(reportTitle)
//                .setStyle(TemplateDefault.bold12CenteredStyle)
//                .setHorizontalAlignment(HorizontalAlignment.CENTER)
//                .setFixedRows(2));
    }

    private void reportReceiptData() throws JRException {
        JsonDataSource jsonDataSource = new JsonDataSource(System.in);
        report.setDataSource(jsonDataSource);
    }

    private void reportCreateColumns() throws DRException {
        int cont = 0;
        int size = extractFields.size();
        while (cont < size) {
            report.columns(col.column(tableColumnName.get(cont), extractFields.get(cont), (DRIDataType) DataTypes.detectType("String")));
            cont++;
        }
    }

    private void reportConfigure() {
        // Orientação da página
        report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);

        report.setTemplate(templateDefault);

        HorizontalListBuilder footerList = cmp.horizontalList(
                cmp.text("").setHorizontalAlignment(HorizontalAlignment.LEFT),
                Components.pageXofY().setHorizontalAlignment(HorizontalAlignment.CENTER),
                Components.currentDate().setHorizontalAlignment(HorizontalAlignment.RIGHT)
        );

        report.pageFooter(footerList);
    }

    private void reportCreateInfoPage() throws MalformedURLException, IOException {

        HorizontalListBuilder horizontalList = cmp.horizontalList();
        horizontalList.setStyle(stl.style(10));
        
        ComponentBuilder<?, ?> compFilters = createFiltersComponent("Filters");
        ComponentBuilder<?, ?> compGroups = createGroupsComponent("Groups");
        ComponentBuilder<?, ?> compSum = createSumComponent("Sum");
        
        if (compFilters != null) {
            horizontalList.add(cmp.hListCell(compFilters).heightFixedOnTop());
        }
        
        if (compGroups != null) {
            horizontalList.add(cmp.hListCell(compGroups).heightFixedOnTop());
        }
        
        if (compSum != null) {
            horizontalList.add(cmp.hListCell(compSum).heightFixedOnTop());
        }

        report.title(
                createTitleComponent(reportTitle),
                horizontalList,
                cmp.verticalGap(10)
        );
    }

    private void reportExport() throws DRException {
        JasperPdfExporterBuilder pdfExporter = export.pdfExporter(System.out);
        report.toPdf(pdfExporter);
//        report.show(true);
    }

    private ComponentBuilder<?, ?> createFiltersComponent(String label) throws IOException {
        if (filters != null && !filters.isEmpty()) {

            HorizontalListBuilder superList = cmp.horizontalList();
            HorizontalListBuilder card1 = null;
            VerticalListBuilder vertList1 = null;

            int cont = 1;

            for (String extractField : filters) {
                log(extractField);

                if (cont == 1) {
                    card1 = createCardComponent();
                    vertList1 = cmp.verticalList();
                }

                HorizontalListBuilder horizontalData = cmp.horizontalList();
                horizontalData.add(cmp.text(extractField).setStyle(getStyleMarkedUp()));

                vertList1.add(horizontalData);

                if (cont == MAX_FILTERS_ROWS || cont == filters.size()) {
                    cont = 1;

                    card1.add(vertList1);
                    superList.add(card1);
                } else {
                    cont++;
                }
            }
            return cmp.verticalList(
                    cmp.text(label).setStyle(templateDefault.boldStyle),
                    superList);
        }
        return null;
    }

    private ComponentBuilder<?, ?> createGroupsComponent(String label) throws IOException {
        if (aggregations != null && aggregations.containsKey("group")) {

            JSONArray groups = (JSONArray) aggregations.get("group");

            if (groups != null && !groups.isEmpty()) {
                VerticalListBuilder listSuper = cmp.verticalList();

                String key;
                JSONArray group;

                for (Object groupObj : groups) {
                    HorizontalListBuilder cardComponent = createCardComponent();
                    // Aggregation object
                    JSONObject groupField = (JSONObject) groupObj;
                    key = (String) groupField.keySet().toArray()[0];
                    group = (JSONArray) groupField.get(key);

                    VerticalListBuilder content = cmp.verticalList();
                    content.add(cmp.text(key).setStyle(templateDefault.boldStyleFont10));

                    for (Object segmentObj : group) {
                        JSONObject segment = (JSONObject) segmentObj;
                        Object[] segmentKeys = segment.keySet().toArray();
                        for (Object statisticKey : segmentKeys) {
                            content.add(cmp.text("   " + statisticKey.toString() + ":").setStyle(templateDefault.boldStyleFont10));
                            JSONObject statistics = (JSONObject) segment.get(statisticKey);
                            Object[] statisticKeys = statistics.keySet().toArray();

                            String totalKey = statisticKeys[0].toString();
                            String totalObject = statistics.get(totalKey).toString();

                            content.add(cmp.text("        <b>" + totalKey + "</b> : " + totalObject).setStyle(getStyleMarkedUp()));

                            if (statisticKeys.length > 1) {
                                String amountKey = statisticKeys[1].toString();
                                String amountObject = statistics.get(amountKey).toString();
                                content.add(cmp.text("        <b>" + amountKey + "</b> : " + amountObject).setStyle(getStyleMarkedUp()));
                            }
                        }
                    }

                    cardComponent.add(content);
                    listSuper.add(cardComponent);
                }

                return cmp.verticalList(
                        cmp.text(label).setStyle(templateDefault.boldStyle),
                        listSuper);
            }
        }
        return null;
    }

    private ComponentBuilder<?, ?> createSumComponent(String label) throws IOException {
        if (aggregations != null && aggregations.containsKey("sum")) {

            JSONArray sumArray = (JSONArray) aggregations.get("sum");

            if (sumArray != null && !sumArray.isEmpty()) {

                String key, value;

                VerticalListBuilder listSuper = cmp.verticalList();

                for (Object sumObject : sumArray) {

                    HorizontalListBuilder cardComponent = createCardComponent();

                    JSONObject sumField = (JSONObject) sumObject;

                    key = (String) sumField.keySet().toArray()[0];
                    log("key " + key);

                    value = String.valueOf(sumField.get(key));
                    log("value " + sumField.get(key));

                    VerticalListBuilder content = cmp.verticalList();

                    HorizontalListBuilder labelSum = cmp.horizontalList();
                    labelSum.add(cmp.text("<b>" + key + "</b> : " + value).setStyle(getStyleMarkedUp()));
                    content.add(labelSum);
//                    content.setFixedWidth(150);

                    cardComponent.add(content);

                    listSuper.add(cardComponent);
                }

                return cmp.verticalList(
                        cmp.text(label).setStyle(templateDefault.boldStyle),
                        listSuper);
            }
        }
        return null;
    }

    private HorizontalListBuilder createCardComponent() {
        HorizontalListBuilder cardComponent = cmp.horizontalList().setBaseStyle(stl.style().setLeftPadding(5));
        cardComponent.setStyle(cardStyle);
        return cardComponent;
    }

    private void createCardStyle() {
        PenBuilder pen1Point = stl.pen1Point().setLineColor(Color.white);
        cardStyle = stl.style(pen1Point).setPadding(10);
        cardStyle.setBackgroundColor(new Color(224, 224, 224));
    }

    private StyleBuilder getStyleMarkedUp() {
        return stl.style().setMarkup(Markup.STYLED);
    }

    private ComponentBuilder<?, ?> createTitleComponent(String label) throws MalformedURLException {

        URL logo = new URL("http://static-files04.cdnandroid.com/76/43/3e/08/imagen-rotativo-rondon-0thumb.jpg");
        String urlOperacao = "https://rondonopolis.s2way.com/";
        HyperLinkBuilder link = hyperLink(urlOperacao);

        JRHyperlinkListener a = new JRHyperlinkListener() {
            @Override
            public void gotoHyperlink(JRPrintHyperlink jrph) throws JRException {
                try {
                    Desktop.getDesktop().browse(new URI(jrph.getHyperlinkAnchor()));
                } catch (Exception ex) {
//                    Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };

        ComponentBuilder<?, ?> dynamicReportsComponent
                = cmp.horizontalList(
                        cmp.verticalList(
                                cmp.text(reportTitle).setStyle(templateDefault.bold22CenteredStyle).setHorizontalAlignment(HorizontalAlignment.LEFT)
                        //                                ,cmp.text(urlOperacao).setStyle(templateDefault.italicStyle)
                        )).setFixedWidth(300);

        return cmp.horizontalList()
                .add(dynamicReportsComponent)
                .newRow()
                .add(cmp.line())
                .newRow()
                .add(cmp.verticalGap(10));

//        ComponentBuilder<?, ?> dynamicReportsComponent
//                = cmp.horizontalList(
//                        cmp.image(logo).setFixedDimension(60, 60),
//                        cmp.verticalList(
////                                cmp.text("s2Way - Rotativo Rondon").setStyle(templateDefault.bold22CenteredStyle).setHorizontalAlignment(HorizontalAlignment.LEFT),
//                                cmp.text("s2Way - Rotativo Rondon").setStyle(templateDefault.bold22CenteredStyle).setHorizontalAlignment(HorizontalAlignment.LEFT),
//                                cmp.text(urlOperacao).setStyle(templateDefault.italicStyle).setHyperLink(link))).setFixedWidth(300);
//
//        return cmp.horizontalList()
//                .add(
//                        dynamicReportsComponent,
//                        cmp.text(label).setStyle(templateDefault.bold18CenteredStyle).setHorizontalAlignment(HorizontalAlignment.RIGHT))
//                .newRow()
//                .add(cmp.line())
//                .newRow()
//                .add(cmp.verticalGap(10));
    }

    private void addCustomerAttribute(HorizontalListBuilder list, String label, String value) {
        if (value != null) {
            list.add(cmp.text(label + ":").setFixedColumns(10).setStyle(templateDefault.boldStyleFont10), cmp.text(value)).newRow();
        }
    }

    private void old() throws JRException, FileNotFoundException, IOException {
        InputStream inputStream = getClass().getResourceAsStream("/report/teste_json_report.jasper");
        JasperPrint impressao;

        JsonDataSource jsonDataSource = new JsonDataSource(System.in);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(JsonQueryExecuterFactory.JSON_DATE_PATTERN, "yyyy-MM-dd");
        params.put(JsonQueryExecuterFactory.JSON_NUMBER_PATTERN, "#,##0.##");
        params.put(JsonQueryExecuterFactory.JSON_LOCALE, Locale.ENGLISH);
        params.put(JRParameter.REPORT_LOCALE, Locale.US);

        impressao = JasperFillManager.fillReport(inputStream, params, jsonDataSource);

        OutputStream outputStream = new FileOutputStream(new File("JasperReport.pdf"));
        JasperExportManager.exportReportToPdfStream(impressao, outputStream);

        inputStream.close();
        outputStream.close();
    }
}
