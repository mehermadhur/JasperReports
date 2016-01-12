/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.versul.testes;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.exception.DRException;

/**
 *
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 *
 */
public class CardReport {

    public CardReport() {
        build();
    }

    private void build() {
        ComponentBuilder<?, ?> cardComponent = createCardComponent();
        
        HorizontalListBuilder cards = cmp.horizontalFlowList();
        
        for (int i = 0; i < 1; i++) {
            cards.add(cardComponent);
        }
        try {

            report()
                    .setTemplate(Templates.reportTemplate)
                    .setTextStyle(stl.style())
                    .setPageFormat(PageType.A5)
                    .title(
                            Templates.createTitleComponent("Card"),
                            cards)
                    .show();

        } catch (DRException e) {

            e.printStackTrace();

        }
    }

    private ComponentBuilder<?, ?> createCardComponent() {
        HorizontalListBuilder cardComponent = cmp.horizontalList();
        StyleBuilder cardStyle = stl.style(stl.pen1Point()).setPadding(10);
        cardComponent.setStyle(cardStyle);
//        ImageBuilder image = cmp.image(Templates.class.getResource("images/user_male.png")).setFixedDimension(60, 60);
//        cardComponent.add(cmp.hListCell(image).heightFixedOnMiddle());
//        cardComponent.add(cmp.horizontalGap(10));

        StyleBuilder boldStyle = stl.style().bold();
        
        HorizontalListBuilder a = cmp.horizontalList();
        a.setBaseGap(10);
        a.add(cmp.text("Name:").setStyle(boldStyle), cmp.text("Peter Marsh")).newRow(5).setFixedWidth(100);
        
        VerticalListBuilder content = cmp.verticalList(
                a,
                cmp.text("Address:").setStyle(boldStyle),
                cmp.text("23 Baden Av."),
                cmp.text("City:").setStyle(boldStyle),
                cmp.text("New York").setWidth(5));
        cardComponent.add(content);
        return cardComponent;
    }

    public static void main(String[] args) {
        new CardReport();
    }
}
