/*
 * Copyright 2012 JBoss Inc
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

package org.optaplanner.examples.vehiclerouting.swingui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.text.NumberFormat;
import javax.swing.ImageIcon;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.examples.common.swingui.TangoColorFactory;
import org.optaplanner.examples.common.swingui.latitudelongitude.LatitudeLongitudeTranslator;
import org.optaplanner.examples.vehiclerouting.domain.VrpCustomer;
import org.optaplanner.examples.vehiclerouting.domain.VrpDepot;
import org.optaplanner.examples.vehiclerouting.domain.VrpLocation;
import org.optaplanner.examples.vehiclerouting.domain.VrpSchedule;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.VrpTimeWindowedCustomer;
import org.optaplanner.examples.vehiclerouting.domain.VrpVehicle;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.VrpTimeWindowedDepot;
import org.optaplanner.examples.vehiclerouting.domain.timewindowed.VrpTimeWindowedSchedule;

public class VehicleRoutingSchedulePainter {

    private static final int TEXT_SIZE = 12;
    private static final int TEXT_SPACING_SIZE = 4;

    private static final String IMAGE_PATH_PREFIX = "/org/optaplanner/examples/vehiclerouting/swingui/";

    private ImageIcon depotImageIcon;
    private ImageIcon[] vehicleImageIcons;
    private NumberFormat numberFormat = NumberFormat.getInstance();

    private BufferedImage canvas = null;
    private LatitudeLongitudeTranslator translator = null;

    public VehicleRoutingSchedulePainter() {
        depotImageIcon = new ImageIcon(getClass().getResource(IMAGE_PATH_PREFIX + "depot.png"));
        vehicleImageIcons = new ImageIcon[] {
                new ImageIcon(getClass().getResource(IMAGE_PATH_PREFIX + "vehicleChameleon.png")),
                new ImageIcon(getClass().getResource(IMAGE_PATH_PREFIX + "vehicleButter.png")),
                new ImageIcon(getClass().getResource(IMAGE_PATH_PREFIX + "vehicleSkyBlue.png")),
                new ImageIcon(getClass().getResource(IMAGE_PATH_PREFIX + "vehicleChocolate.png")),
                new ImageIcon(getClass().getResource(IMAGE_PATH_PREFIX + "vehiclePlum.png")),
        };
        if (vehicleImageIcons.length != TangoColorFactory.SEQUENCE_1.length) {
            throw new IllegalStateException("The vehicleImageIcons length (" + vehicleImageIcons.length
                    + ") should be equal to the TangoColorFactory.SEQUENCE length ("
                    + TangoColorFactory.SEQUENCE_1.length + ").");
        }
    }

    public BufferedImage getCanvas() {
        return canvas;
    }

    public LatitudeLongitudeTranslator getTranslator() {
        return translator;
    }

    public void reset(VrpSchedule schedule, Dimension size, ImageObserver imageObserver) {
        translator = new LatitudeLongitudeTranslator();
        for (VrpLocation location : schedule.getLocationList()) {
            translator.addCoordinates(location.getLatitude(), location.getLongitude());
        }

        double width = size.getWidth();
        double height = size.getHeight();
        translator.prepareFor(width, height);

        Graphics g = createCanvas(width, height);
        g.setColor(TangoColorFactory.ORANGE_2);
        g.setFont(g.getFont().deriveFont((float) TEXT_SIZE));
        for (VrpCustomer customer : schedule.getCustomerList()) {
            VrpLocation location = customer.getLocation();
            int x = translator.translateLongitudeToX(location.getLongitude());
            int y = translator.translateLatitudeToY(location.getLatitude());
            g.fillRect(x - 1, y - 1, 3, 3);
            g.drawString(Integer.toString(customer.getDemand()), x + 3, y - 3);
            if (customer instanceof VrpTimeWindowedCustomer) {
                VrpTimeWindowedCustomer timeWindowedCustomer = (VrpTimeWindowedCustomer) customer;
                g.drawString(timeWindowedCustomer.getTimeWindowLabel(), x + 3, y - 3 + TEXT_SIZE + TEXT_SPACING_SIZE);
            }
        }
        g.setColor(TangoColorFactory.ALUMINIUM_4);
        for (VrpDepot depot : schedule.getDepotList()) {
            int x = translator.translateLongitudeToX(depot.getLocation().getLongitude());
            int y = translator.translateLatitudeToY(depot.getLocation().getLatitude());
            g.fillRect(x - 2, y - 2, 5, 5);
            g.drawImage(depotImageIcon.getImage(),
                    x - depotImageIcon.getIconWidth() / 2, y - 2 - depotImageIcon.getIconHeight(), imageObserver);
            if (depot instanceof VrpTimeWindowedDepot) {
                VrpTimeWindowedDepot timeWindowedDepot = (VrpTimeWindowedDepot) depot;
                g.drawString(timeWindowedDepot.getTimeWindowLabel(), x + 3, y - 3 + TEXT_SIZE + TEXT_SPACING_SIZE);
            }
        }
        int colorIndex = 0;
        // TODO Too many nested for loops
        for (VrpVehicle vehicle : schedule.getVehicleList()) {
            g.setColor(TangoColorFactory.SEQUENCE_2[colorIndex]);
            VrpCustomer vehicleInfoCustomer = null;
            int longestNonDepotDistance = -1;
            int load = 0;
            for (VrpCustomer customer : schedule.getCustomerList()) {
                if (customer.getPreviousStandstill() != null && customer.getVehicle() == vehicle) {
                    load += customer.getDemand();
                    VrpLocation previousLocation = customer.getPreviousStandstill().getLocation();
                    int previousX = translator.translateLongitudeToX(previousLocation.getLongitude());
                    int previousY = translator.translateLatitudeToY(previousLocation.getLatitude());
                    VrpLocation location = customer.getLocation();
                    int x = translator.translateLongitudeToX(location.getLongitude());
                    int y = translator.translateLatitudeToY(location.getLatitude());
                    g.drawLine(previousX, previousY, x, y);
                    // Determine where to draw the vehicle info
                    int distance = customer.getDistanceToPreviousStandstill();
                    if (customer.getPreviousStandstill() instanceof VrpCustomer) {
                        if (longestNonDepotDistance < distance) {
                            longestNonDepotDistance = distance;
                            vehicleInfoCustomer = customer;
                        }
                    } else if (vehicleInfoCustomer == null) {
                        // If there is only 1 customer in this chain, draw it on a line to the Depot anyway
                        vehicleInfoCustomer = customer;
                    }
                    // Line back to the vehicle depot
                    boolean needsBackToVehicleLineDraw = true;
                    for (VrpCustomer trailingCustomer : schedule.getCustomerList()) {
                        if (trailingCustomer.getPreviousStandstill() == customer) {
                            needsBackToVehicleLineDraw = false;
                            break;
                        }
                    }
                    if (needsBackToVehicleLineDraw) {
                        VrpLocation vehicleLocation = vehicle.getLocation();
                        int vehicleX = translator.translateLongitudeToX(vehicleLocation.getLongitude());
                        int vehicleY = translator.translateLatitudeToY(vehicleLocation.getLatitude());
                        g.drawLine(x, y,vehicleX, vehicleY);
                    }
                }
            }
            // Draw vehicle info
            if (vehicleInfoCustomer != null) {
                if (load > vehicle.getCapacity()) {
                    g.setColor(TangoColorFactory.SCARLET_2);
                }
                VrpLocation previousLocation = vehicleInfoCustomer.getPreviousStandstill().getLocation();
                VrpLocation location = vehicleInfoCustomer.getLocation();
                double longitude = (previousLocation.getLongitude() + location.getLongitude()) / 2.0;
                int x = translator.translateLongitudeToX(longitude);
                double latitude = (previousLocation.getLatitude() + location.getLatitude()) / 2.0;
                int y = translator.translateLatitudeToY(latitude);
                boolean ascending = (previousLocation.getLongitude() < location.getLongitude())
                        ^ (previousLocation.getLatitude() < location.getLatitude());

                ImageIcon vehicleImageIcon = vehicleImageIcons[colorIndex];
                int vehicleInfoHeight = vehicleImageIcon.getIconHeight() + 2 + TEXT_SIZE;
                g.drawImage(vehicleImageIcon.getImage(), x + 1, (ascending ? y - vehicleInfoHeight - 1 : y + 1), imageObserver);
                g.drawString(load + " / " + vehicle.getCapacity(), x + 1, (ascending ? y - 1 : y + vehicleInfoHeight + 1));
            }
            colorIndex = (colorIndex + 1) % TangoColorFactory.SEQUENCE_2.length;
        }

        // Legend
        g.setColor(TangoColorFactory.ALUMINIUM_4);
        g.fillRect(5, (int) height - 12 - TEXT_SIZE - (TEXT_SIZE / 2), 5, 5);
        g.drawString((schedule instanceof VrpTimeWindowedSchedule)
                ? "Depot: time window" : "Depot", 15, (int) height - 10 - TEXT_SIZE);
        g.setColor(TangoColorFactory.ORANGE_2);
        g.fillRect(6, (int) height - 6 - (TEXT_SIZE / 2), 3, 3);
        g.drawString((schedule instanceof VrpTimeWindowedSchedule)
                ? "Customer: demand and time window" : "Customer: demand", 15, (int) height - 5);
        // Show soft score
        g.setColor(TangoColorFactory.SCARLET_2);
        HardSoftScore score = schedule.getScore();
        if (score != null) {
            String totalDistanceString;
            if (!score.isFeasible()) {
                totalDistanceString = "Not feasible";
            } else {
                totalDistanceString = numberFormat.format(- score.getSoftScore()) + " fuel";
            }
            g.setFont( g.getFont().deriveFont(Font.BOLD, (float) TEXT_SIZE * 2));
            g.drawString(totalDistanceString,
                    (int) width - g.getFontMetrics().stringWidth(totalDistanceString) - 10, (int) height - 10);
        }
    }

    public Graphics createCanvas(double width, double height) {
        int canvasWidth = (int) Math.ceil(width) + 1;
        int canvasHeight = (int) Math.ceil(height) + 1;
        canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = canvas.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        return g;
    }

}
