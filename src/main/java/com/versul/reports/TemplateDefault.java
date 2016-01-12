/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.versul.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import java.awt.Color;
import java.util.Locale;
import net.sf.dynamicreports.report.builder.HyperLinkBuilder;
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.tableofcontents.TableOfContentsCustomizerBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.VerticalAlignment;

/**
 *
 * @author desenv08
 */
public class TemplateDefault extends ReportTemplateBuilder {

    public static final StyleBuilder rootStyle = stl.style().setPadding(2);
    public static final StyleBuilder boldStyle = stl.style(rootStyle).bold();
    public static final StyleBuilder styleFont5 = stl.style(rootStyle).setFontSize(5);
    public static final StyleBuilder boldStyleFont5 = stl.style(rootStyle).bold().setFontSize(5);
    public static final StyleBuilder boldStyleFont10 = stl.style(rootStyle).bold().setFontSize(10);
    public static final StyleBuilder italicStyle = stl.style(rootStyle).italic();
    public static final StyleBuilder boldCenteredStyle = stl.style(boldStyle).setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
    public static final StyleBuilder bold12CenteredStyle = stl.style(boldCenteredStyle).setFontSize(12);
    public static final StyleBuilder bold18CenteredStyle = stl.style(boldCenteredStyle).setFontSize(18);
    public static final StyleBuilder bold22CenteredStyle = stl.style(boldCenteredStyle).setFontSize(22);
    public static final StyleBuilder columnStyle = stl.style(rootStyle).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(8);
    public static final StyleBuilder columnTitleStyle = stl.style(columnStyle).setBorder(stl.pen1Point()).setHorizontalAlignment(HorizontalAlignment.CENTER).setBackgroundColor(Color.LIGHT_GRAY).bold().setFontSize(10);
    public static final StyleBuilder groupStyle = stl.style(bold12CenteredStyle).setHorizontalAlignment(HorizontalAlignment.LEFT);
    public static final StyleBuilder subtotalStyle = stl.style(boldStyle).setTopBorder(stl.pen1Point());
    public static final StyleBuilder crosstabGroupStyle = stl.style(columnTitleStyle);
    public static final StyleBuilder crosstabGroupTotalStyle = stl.style(columnTitleStyle).setBackgroundColor(new Color(170, 170, 170));
    public static final StyleBuilder crosstabGrandTotalStyle = stl.style(columnTitleStyle).setBackgroundColor(new Color(140, 140, 140));
    public static final StyleBuilder crosstabCellStyle = stl.style(columnStyle).setBorder(stl.pen1Point());
    public static final TableOfContentsCustomizerBuilder tableOfContentsCustomizer = tableOfContentsCustomizer().setHeadingStyle(0, stl.style(rootStyle).bold());
    public static HyperLinkBuilder link = hyperLink("http://www.s2way.com.br");

    public TemplateDefault() {
        setLocale(new Locale("pt", "BR"));
        setColumnStyle(columnStyle);
        setColumnTitleStyle(columnTitleStyle);
        setGroupStyle(groupStyle);
        setGroupTitleStyle(groupStyle);
        setSubtotalStyle(subtotalStyle);
        highlightDetailEvenRows();
        crosstabHighlightEvenRows();
        setCrosstabGroupStyle(crosstabGroupStyle);
        setCrosstabGroupTotalStyle(crosstabGroupTotalStyle);
        setCrosstabGrandTotalStyle(crosstabGrandTotalStyle);
        setCrosstabCellStyle(crosstabCellStyle);
        setTableOfContentsCustomizer(tableOfContentsCustomizer);
    }

}
