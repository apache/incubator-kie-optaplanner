/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.examples.tsp.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.optaplanner.examples.common.swingui.TangoColorFactory;
import org.optaplanner.examples.common.swingui.latitudelongitude.LatitudeLongitudeTranslator;
import org.optaplanner.examples.tsp.domain.City;
import org.optaplanner.examples.tsp.domain.Domicile;
import org.optaplanner.examples.tsp.domain.TravelingSalesmanTour;
import org.optaplanner.examples.tsp.domain.Visit;

/**
 * TODO this code is highly unoptimized
 */
public class TspWorldPanel extends JPanel {

    private static final int TEXT_SIZE = 8;

    private final TspPanel tspPanel;

    private BufferedImage canvas = null;
    private LatitudeLongitudeTranslator translator = null;

    private ImageIcon europaBackground;

    public TspWorldPanel(TspPanel tspPanel) {
        this.tspPanel = tspPanel;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // TODO Not thread-safe during solving
                TravelingSalesmanTour travelingSalesmanTour = TspWorldPanel.this.tspPanel.getTravelingSalesmanTour();
                if (travelingSalesmanTour != null) {
                    resetPanel(travelingSalesmanTour);
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (translator != null) {
                    double longitude = translator.translateXToLongitude(e.getX());
                    double latitude = translator.translateYToLatitude(e.getY());
                    TspWorldPanel.this.tspPanel.insertCityAndVisit(longitude, latitude);
                }
            }
        });
        europaBackground = new ImageIcon(getClass().getResource(
                "/org/optaplanner/examples/tsp/swingui/europaBackground.png"));
    }

    public void resetPanel(TravelingSalesmanTour travelingSalesmanTour) {
        translator = new LatitudeLongitudeTranslator();
        for (City city : travelingSalesmanTour.getCityList()) {
            translator.addCoordinates(city.getLatitude(), city.getLongitude());
        }

        Dimension size = getSize();
        double width = size.getWidth();
        double height = size.getHeight();
        translator.prepareFor(width, height);

        Graphics g = createCanvas(width, height);
        String tourName = travelingSalesmanTour.getName();
        if (tourName.startsWith("europe")) {
            g.drawImage(europaBackground.getImage(), 0, 0, translator.getImageWidth(), translator.getImageHeight(), this);
        }
        g.setFont(g.getFont().deriveFont((float) TEXT_SIZE));
        g.setColor(TangoColorFactory.PLUM_2);
        for (Visit visit : travelingSalesmanTour.getVisitList()) {
            City city = visit.getCity();
            int x = translator.translateLongitudeToX(city.getLongitude());
            int y = translator.translateLatitudeToY(city.getLatitude());
            g.fillRect(x - 1, y - 1, 3, 3);
            if (city.getName() != null) {
                g.drawString(city.getName(), x + 3, y - 3);
            }
        }
        g.setColor(TangoColorFactory.ALUMINIUM_4);
        for (Domicile domicile : travelingSalesmanTour.getDomicileList()) {
            City city = domicile.getCity();
            int x = translator.translateLongitudeToX(city.getLongitude());
            int y = translator.translateLatitudeToY(city.getLatitude());
            g.fillRect(x - 2, y - 2, 5, 5);
            if (city.getName() != null) {
                g.drawString(city.getName(), x + 3, y - 3);
            }
        }
        Set<Visit> needsBackToDomicileLineSet = new HashSet<Visit>(travelingSalesmanTour.getVisitList());
        for (Visit trailingVisit : travelingSalesmanTour.getVisitList()) {
            if (trailingVisit.getPreviousStandstill() instanceof Visit) {
                needsBackToDomicileLineSet.remove(trailingVisit.getPreviousStandstill());
            }
        }
        g.setColor(TangoColorFactory.CHOCOLATE_1);
        for (Visit visit : travelingSalesmanTour.getVisitList()) {
            if (visit.getPreviousStandstill() != null) {
                City previousCity = visit.getPreviousStandstill().getCity();
                int previousX = translator.translateLongitudeToX(previousCity.getLongitude());
                int previousY = translator.translateLatitudeToY(previousCity.getLatitude());
                City city = visit.getCity();
                int x = translator.translateLongitudeToX(city.getLongitude());
                int y = translator.translateLatitudeToY(city.getLatitude());
                g.drawLine(previousX, previousY, x, y);
                // Back to domicile line
                if (needsBackToDomicileLineSet.contains(visit)) {
                    // TODO support more than 1 domicile
                    Domicile domicile = travelingSalesmanTour.getDomicileList().get(0);
                    City domicileCity = domicile.getCity();
                    int domicileX = translator.translateLongitudeToX(domicileCity.getLongitude());
                    int domicileY = translator.translateLatitudeToY(domicileCity.getLatitude());
                    g.drawLine(x, y,domicileX, domicileY);
                }
            }
        }
        // Legend
        g.setColor(TangoColorFactory.ALUMINIUM_4);
        g.fillRect(5, (int) height - 20, 5, 5);
        g.drawString("Domicile", 15, (int) height - 15);
        g.setColor(TangoColorFactory.PLUM_2);
        g.fillRect(6, (int) height - 9, 3, 3);
        g.drawString("Visit", 15, (int) height - 5);
        g.setColor(TangoColorFactory.ALUMINIUM_5);
        String clickString = "Click anywhere in the map to add a visit.";
        g.drawString(clickString, (int) width - 5 - g.getFontMetrics().stringWidth(clickString),(int) height - 5);
        repaint();
    }

    public void updatePanel(TravelingSalesmanTour travelingSalesmanTour) {
        resetPanel(travelingSalesmanTour);
    }

    private Graphics createCanvas(double width, double height) {
        int canvasWidth = (int) Math.ceil(width) + 1;
        int canvasHeight = (int) Math.ceil(height) + 1;
        canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = canvas.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        return g;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas != null) {
            g.drawImage(canvas, 0, 0, this);
        }
    }

}
