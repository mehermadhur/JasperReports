/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.versul.main;

import com.versul.reports.TemplateDefault;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.HyperLinkComponentBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JsonDataSource;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainClass {

    private final int DEFAULT_BODY_FONT_SIZE = 8;
    private static Logger logger = Logger.getLogger(MainClass.class);
    private final JSONParser parser = new JSONParser();
    private JasperReportBuilder report = null;
    private StyleBuilder cardStyle;
    private long totalRows;
    private int bodyFontSize;
    private String reportTitle;
    private List<String> extractFields;
    private List<JSONObject> tableColumnName;
    private List<String> filters;
    private JSONObject aggregations;
    private String created;
    private List<String> logos;
    private String hyperlinkLogo;
    private final String IMAGES_PATH = "images/";
    private String logoImage = null;

    public MainClass() {
        this.report = DynamicReports.report();
        createCardStyle();
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            throw new IllegalArgumentException("Application parameters not received!");
        }

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
            }
        } catch (Exception e) {
            log(e);
        }
    }

    private void extractAndValidateCoverParams(String jsonArgs) throws ParseException {
        JSONObject arguments = (JSONObject) this.parser.parse(jsonArgs);

        Object reportTitleObject = arguments.get("report_title");
        if (reportTitleObject == null) {
            throw new IllegalArgumentException("'report_title' argument is invalid");
        }
        this.hyperlinkLogo = arguments.get("logo_hyperlink") == null ? "" : arguments.get("logo_hyperlink").toString();
        this.logos = ((JSONArray) arguments.get("logo"));
        this.reportTitle = reportTitleObject.toString();
        this.filters = ((List) arguments.get("filters"));
        this.aggregations = ((JSONObject) arguments.get("aggs"));
        this.totalRows = Long.parseLong(arguments.get("count").toString());
        this.created = ((String) arguments.get("created"));
        if (this.reportTitle.isEmpty()) {
            throw new IllegalArgumentException("'report_title' argument is invalid");
        }

        validateLogos();
    }

    private void extractAndValidateReportParams(String jsonArgs) throws ParseException {
        JSONObject arguments = (JSONObject) this.parser.parse(jsonArgs);

        this.hyperlinkLogo = arguments.get("logo_hyperlink") == null ? "" : arguments.get("logo_hyperlink").toString();
        this.logos = ((JSONArray) arguments.get("logo"));
        this.extractFields = ((JSONArray) arguments.get("fields"));
        this.tableColumnName = ((JSONArray) arguments.get("columns"));
        this.created = ((String) arguments.get("created"));
        this.bodyFontSize = arguments.containsKey("body_font_size") ? Integer.parseInt(arguments.get("body_font_size").toString()) : DEFAULT_BODY_FONT_SIZE;
        if ((this.tableColumnName == null) || (this.tableColumnName.isEmpty())) {
            throw new IllegalArgumentException("'columns' argument is invalid");
        }
        if (this.extractFields.size() != this.tableColumnName.size()) {
            throw new IllegalArgumentException("fields and columns have different sizes.");
        }

        validateLogos();
    }

    /**
     * Método que encontra o primeiro logo existente no arquivo de imagens
     */
    private void validateLogos() {

        if (logos == null) {
            return;
        }

        // Caso a operação tenha logos por área
        for (String logoName : logos) {
            // Adiciona extensão apenas para procurar o arquivo
            String fullLogoName = logoName + ".png";
            File logoFile = new File(IMAGES_PATH + fullLogoName);
            if (logoFile.exists() && logoFile.isFile()) {
                // Adiciona o name original pois a extensão é adicionado depois
                this.logoImage = logoName;
                break;
            }
        }
    }

    private void createCoverPage() throws Exception {
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

    private void createReport() throws IOException, DRException, Exception {
        log("CreatedInstance");
        reportCreateInstance();
        log("ReceiptData");
        reportReceiptData();
        log("Configure");
        reportConfigure();
        log("CreateDefailHeader");
        createReportDetailHeader();
        log("CreateColumns");
        reportCreateColumns();
        log("to file");
        reportToFile();
        log("done");
    }

    private void reportCreateInstance() {
        this.report = DynamicReports.report();
    }

    private void reportReceiptData() throws JRException, DRException {
        JsonDataSource jsonDataSource = new JsonDataSource(System.in);
        this.report.setDataSource(jsonDataSource);
    }

    private int getDefaultColumnWidth(int totalColumns, List<JSONObject> columns) {
        int numColumnsWithWidth = 0;
        int accumWidth = 0;
        for (int i = 0; i < totalColumns; i++) {
            JSONObject column = (JSONObject) columns.get(i);
            log(column.toJSONString());
            Object width = column.get("width");
            if (width != null) {
                accumWidth += Integer.parseInt(width.toString());
                numColumnsWithWidth++;
            }
        }
        if (numColumnsWithWidth == 0) {
            return 0;
        }
        int pageWithoutMargins = PageType.A4.getHeight() - 10 - 10;

        double accumWidthInPixels = mmToPixel(accumWidth);

        int defaultWidth = new Double((pageWithoutMargins - accumWidthInPixels) / (totalColumns - numColumnsWithWidth)).intValue();
        log(String.valueOf(PageType.A4.getHeight()));
        log(String.valueOf(accumWidthInPixels));
        log(String.valueOf(pageWithoutMargins));
        log(String.valueOf(defaultWidth));
        return defaultWidth;
    }

    private double mmToPixel(int mm) {
        double pixelsPerMM = 2.8346456692913384D;
        return pixelsPerMM * mm;
    }

    private void reportCreateColumns() throws DRException, MalformedURLException {
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
                width = defaultWidth;
            } else {
                width = Integer.valueOf(new Double(mmToPixel(Integer.valueOf(String.valueOf(widthParam)).intValue())).intValue());
            }

            this.report.columns(
                    DynamicReports.col
                    .column(label, this.extractFields.get(cont), DataTypes.stringType())
                    .setFixedWidth(width)
                    .setStretchWithOverflow(false)
                    .setStyle(getAlignmentStyle(align))
            );
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

        return Styles.style().setHorizontalAlignment(align).setLeftPadding(2).setRightPadding(2).setFontSize(this.bodyFontSize);
    }

    private void reportConfigure() {
        this.report.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);

        this.report.setTemplate(new TemplateDefault());
        HyperLinkComponentBuilder creationTime;
        if (this.created == null) {
            creationTime = Components.currentDate().setPattern("dd/MM/yyyy HH:mm:ss").setHorizontalAlignment(HorizontalAlignment.RIGHT);
        } else {
            creationTime = DynamicReports.cmp.text(this.created).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        }
        HorizontalListBuilder footerList = DynamicReports.cmp.horizontalList(
                DynamicReports.cmp.text("").setHorizontalAlignment(HorizontalAlignment.LEFT),
                creationTime
        );

        this.report.pageFooter(footerList);
    }

    private void reportCreateInfoPage() throws Exception {
        HorizontalListBuilder horizontalList = DynamicReports.cmp.horizontalList();
        horizontalList.setStyle(DynamicReports.stl.style(10));

        ComponentBuilder<?, ?> compFilters = createFiltersComponent("Filtros Utilizados");
        ComponentBuilder<?, ?> compGroups = createGroupsComponent("Grupos (Top 10)");
        ComponentBuilder<?, ?> compSum = createSumComponent("Totais");
        if (compFilters != null) {
            horizontalList.add(DynamicReports.cmp.hListCell(compFilters).heightFixedOnTop());
        }
        if (compGroups != null) {
            horizontalList.add(DynamicReports.cmp.hListCell(compGroups).heightFixedOnTop());
        }
        if (compSum != null) {
            horizontalList.add(DynamicReports.cmp.hListCell(compSum).heightFixedOnTop());
        }

        this.report.title(createCoverHeaderComponent(), horizontalList, DynamicReports.cmp.verticalGap(10));
    }

    private void reportToFile() throws DRException, JRException {
        JasperPdfExporterBuilder pdfExporter = DynamicReports.export.pdfExporter(System.out);
        this.report.toPdf(pdfExporter);
    }

    private ComponentBuilder<?, ?> createFiltersComponent(String label) throws IOException {
        if ((this.filters != null) && (!this.filters.isEmpty())) {
            HorizontalListBuilder superList = DynamicReports.cmp.horizontalList();
            HorizontalListBuilder card = createCardComponent();
            VerticalListBuilder verticalList = DynamicReports.cmp.verticalList();

            for (String filter : this.filters) {
                verticalList.add(DynamicReports.cmp.text("    " + StringEscapeUtils.escapeXml(filter)).setStyle(getStyleMarkedUp()));
            }

            card.add(verticalList);
            superList.add(card);

            return DynamicReports.cmp.verticalList(DynamicReports.cmp.text(label).setStyle(TemplateDefault.boldStyle), superList);
        }
        return null;
    }

    private ComponentBuilder<?, ?> createGroupsComponent(String label) throws IOException {
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
                    content.add(DynamicReports.cmp.text(key));
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

                            content.add(DynamicReports.cmp.text(groupText).setStyle(getStyleMarkedUp()));
                        }
                    }
                    cardComponent.add(content);
                    listSuper.add(cardComponent);
                }
                return DynamicReports.cmp.verticalList(DynamicReports.cmp.text(label).setStyle(TemplateDefault.boldStyle), listSuper);
            }
        }
        return null;
    }

    private ComponentBuilder<?, ?> createSumComponent(String label) throws Exception {
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

                    labelSum.add(DynamicReports.cmp.text("<b>" + key + "</b>: " + value).setStyle(getStyleMarkedUp()));
                    content.add(labelSum);

                    cardComponent.add(content);

                    listSuper.add(cardComponent);
                }
                return DynamicReports.cmp.verticalList(DynamicReports.cmp.text(label).setStyle(TemplateDefault.boldStyle), listSuper);
            }
        }
        return null;
    }

    private HorizontalListBuilder createCardComponent() {
        HorizontalListBuilder cardComponent = DynamicReports.cmp.horizontalList().setBaseStyle(DynamicReports.stl.style().setLeftPadding(5));
        cardComponent.setStyle(this.cardStyle);
        return cardComponent;
    }

    private void createCardStyle() {
        PenBuilder pen1Point = DynamicReports.stl.pen1Point().setLineColor(Color.white);
        this.cardStyle = DynamicReports.stl.style(pen1Point).setPadding(10);
        this.cardStyle.setBackgroundColor(new Color(224, 224, 224));
    }

    private StyleBuilder getStyleMarkedUp() {
        return (StyleBuilder) DynamicReports.stl.style().setMarkup(Markup.STYLED);
    }

    /**
     * Monta HEADER da página de capa
     *
     * @param label
     * @return
     * @throws MalformedURLException
     */
    private ComponentBuilder<?, ?> createCoverHeaderComponent() throws MalformedURLException {

        // Instância do logo, caso exista
        final ImageBuilder logoComponent = getLogoComponent();

        // Cria componente do logo e titulo do relatório
        final HorizontalListBuilder leftTitleData = DynamicReports.cmp.horizontalList();
        // Só adiciona logo, se o mesmo existir
        if (logoComponent != null) {
            leftTitleData.add(DynamicReports.cmp.verticalList(logoComponent.setStyle(TemplateDefault.verticalCenterAligment)));
            leftTitleData.add(DynamicReports.cmp.gap(10, 0));
        }
        // Adiciona título do relatório
        leftTitleData.add(DynamicReports.cmp.text(this.reportTitle).setStyle(TemplateDefault.bold22TitleStyle));

        // Cria componente que indica total de registros
        final TextFieldBuilder<String> rightTitleData = DynamicReports.cmp.text("Total de Registros: " + this.totalRows)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setStyle(TemplateDefault.verticalBottomAligment);

        // Retorna componente com o título completo
        return DynamicReports.cmp.horizontalList()
                .add(leftTitleData, rightTitleData)
                .newRow()
                .add(DynamicReports.cmp.gap(10, 10))
                .newRow()
                .add(DynamicReports.cmp.line())
                .newRow()
                .add(DynamicReports.cmp.verticalGap(10));
    }

    /**
     * Monta HEADER das páginas de dados
     *
     * @throws MalformedURLException
     */
    private void createReportDetailHeader() throws MalformedURLException {

        final ImageBuilder logoComponent = getLogoComponent();

        // Caso exista logo, deverá adicionar. Caso contrário, não deverá adicionar o padding na página de dados
        if (logoComponent != null) {
            final HorizontalListBuilder horizontalList = DynamicReports.cmp.horizontalList(
                    DynamicReports.cmp.text("").setHorizontalAlignment(HorizontalAlignment.LEFT),
                    logoComponent.setHorizontalAlignment(HorizontalAlignment.CENTER),
                    DynamicReports.cmp.text("").setHorizontalAlignment(HorizontalAlignment.RIGHT)
            ).newRow().add(DynamicReports.cmp.verticalGap(10));

            this.report.pageHeader(horizontalList);
        }
    }

    /**
     * Retorna componente de imagem do logo da operação
     *
     * Exemplo para caso queira logo da web URL logo = new
     * URL("http://www.imagemdaweb.com.br/imagem,png"); final ImageBuilder image
     * = DynamicReports.cmp.image(logo);
     *
     * @return
     * @throws MalformedURLException
     */
    private ImageBuilder getLogoComponent() throws MalformedURLException {

        log("dentro get logo");
        // Caso operação não use logo
        if (this.logoImage == null) {
            log("null");
            return null;
        }
        log("fora");

        final ImageBuilder image = DynamicReports.cmp.image(IMAGES_PATH + this.logoImage + ".png");
        // Seta tamanho default
        image.setFixedDimension(60, 60);
        // Vincula url (caso tenha sido passado) ao logo
        if (this.hyperlinkLogo != null && !this.hyperlinkLogo.isEmpty()) {
            image.setHyperLink(DynamicReports.hyperLink(this.hyperlinkLogo));
        }
        return image;
    }

    private static void log(Throwable e) {
//        logger.info("paaah!", e);
    }

    /**
     * Este método serve apenas para debugar. Quando for versão oficial, deverá
     * comentá-lo.
     */
    private static void log(String msg) {
//        logger.info(msg);
    }

}
