/*
 * Copyright 2015 JBoss Inc
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

package org.optaplanner.webexamples.vehiclerouting.rest.service;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.swing.impl.TangoColorFactory;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.webexamples.vehiclerouting.rest.cdi.VehicleRoutingSolverManager;
import org.optaplanner.webexamples.vehiclerouting.rest.domain.JsonCustomer;
import org.optaplanner.webexamples.vehiclerouting.rest.domain.JsonMessage;
import org.optaplanner.webexamples.vehiclerouting.rest.domain.JsonVehicleRoute;
import org.optaplanner.webexamples.vehiclerouting.rest.domain.JsonVehicleRoutingSolution;

import ws.arete.arms.engine.domain.geojson.GeoJsonCustomer;
import ws.arete.arms.engine.domain.geojson.GeoJsonCustomerProperties;
import ws.arete.arms.engine.domain.geojson.GeoJsonGeometry;
import ws.arete.arms.engine.domain.geojson.GeoJsonRoute;
import ws.arete.arms.engine.domain.geojson.GeoJsonRouteProperties;
import ws.arete.arms.engine.domain.geojson.GeoJsonVehicleRoutingSolution;

public class DefaultVehicleRoutingRestService implements VehicleRoutingRestService {

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,##0.00");

    @Inject
    private VehicleRoutingSolverManager solverManager;

    @Context
    private HttpServletRequest request;

    @Override
    public JsonVehicleRoutingSolution getSolution() {
        VehicleRoutingSolution solution = solverManager.retrieveOrCreateSolution(request.getSession().getId());
        return convertToJsonVehicleRoutingSolution(solution);
    }

    @Override
    public GeoJsonVehicleRoutingSolution getSolutionGeoJson() {
        VehicleRoutingSolution solution = solverManager.retrieveOrCreateSolution(request.getSession().getId());
        GeoJsonVehicleRoutingSolution jsonsolution = convertToGeoJsonVehicleRoutingSolution(solution);

        return jsonsolution;
    }
    
    protected static GeoJsonVehicleRoutingSolution convertToGeoJsonVehicleRoutingSolution(VehicleRoutingSolution solution) {
        GeoJsonVehicleRoutingSolution jsonSolution = new GeoJsonVehicleRoutingSolution();

        jsonSolution.setType("FeatureCollection");
        List<Object> jsonFeatureList = new ArrayList<Object>(solution.getCustomerList().size());
        for (Customer customer : solution.getCustomerList()) {
            
            Location customerLocation = customer.getLocation();

            List<Object> coordinates= new ArrayList<Object>();
            coordinates.add(customerLocation.getLongitude());
            coordinates.add(customerLocation.getLatitude());

            GeoJsonGeometry geometry = new GeoJsonGeometry("Point", coordinates);

            GeoJsonCustomerProperties properties = new GeoJsonCustomerProperties();
            properties.setMarkersymbol("hospital");
            properties.setMarkersize("small");
            properties.setId(customerLocation.getId());
            properties.setName(customerLocation.getName());

//            private Long twe;
//            private Long tws;
//            private Double arrivalTime;
//            private Double departureTime;
//            private Double deliveryRangeStart;
//            private Double deliveryRangeEnd;


            jsonFeatureList.add(new GeoJsonCustomer(geometry, properties, "Feature"));
        }

        for (Vehicle vehicle : solution.getVehicleList()) {
            
            
            Location depotLocation = vehicle.getDepot().getLocation();

            Customer customer = vehicle.getNextCustomer();
            
            if (customer != null)
            {
            List<GeoJsonRoute> jsonVehicleCustomerList = new ArrayList<GeoJsonRoute>();
            List<Object> coordinatesList = new ArrayList<Object>();

            GeoJsonRouteProperties properties = new GeoJsonRouteProperties();
            properties.setTrip(vehicle.getId());
            properties.setStroke();

            ArrayList<Double> depotCoordinates= new ArrayList<Double>();
            depotCoordinates.add(depotLocation.getLongitude());
            depotCoordinates.add(depotLocation.getLatitude());
            coordinatesList.add(depotCoordinates);
            
            while (customer != null) {
                Location customerLocation = customer.getLocation();

                ArrayList<Double> coordinates= new ArrayList<Double>();
                coordinates.add(customerLocation.getLongitude());
                coordinates.add(customerLocation.getLatitude());

                coordinatesList.add(coordinates);

                customer = customer.getNextCustomer();
            }
            GeoJsonGeometry geometry = new GeoJsonGeometry("LineString", coordinatesList);
            
            jsonFeatureList.add(new GeoJsonRoute(geometry, properties, "Feature"));
            }
        }
        jsonSolution.setFeatures(jsonFeatureList);
        return jsonSolution;
    }
    
    protected JsonVehicleRoutingSolution convertToJsonVehicleRoutingSolution(VehicleRoutingSolution solution) {
        JsonVehicleRoutingSolution jsonSolution = new JsonVehicleRoutingSolution();
        jsonSolution.setName(solution.getName());
        List<JsonCustomer> jsonCustomerList = new ArrayList<JsonCustomer>(solution.getCustomerList().size());
        for (Customer customer : solution.getCustomerList()) {
            Location customerLocation = customer.getLocation();
            jsonCustomerList.add(new JsonCustomer(customerLocation.getName(),
                    customerLocation.getLatitude(), customerLocation.getLongitude(), customer.getDemand()));
        }
        jsonSolution.setCustomerList(jsonCustomerList);
        List<JsonVehicleRoute> jsonVehicleRouteList = new ArrayList<JsonVehicleRoute>(solution.getVehicleList().size());
        TangoColorFactory tangoColorFactory = new TangoColorFactory();
        for (Vehicle vehicle : solution.getVehicleList()) {
            JsonVehicleRoute jsonVehicleRoute = new JsonVehicleRoute();
            Location depotLocation = vehicle.getDepot().getLocation();
            jsonVehicleRoute.setDepotLocationName(depotLocation.getName());
            jsonVehicleRoute.setDepotLatitude(depotLocation.getLatitude());
            jsonVehicleRoute.setDepotLongitude(depotLocation.getLongitude());
            jsonVehicleRoute.setCapacity(vehicle.getCapacity());
            Color color = tangoColorFactory.pickColor(vehicle);
            jsonVehicleRoute.setHexColor(
                    String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
            Customer customer = vehicle.getNextCustomer();
            int demandTotal = 0;
            List<JsonCustomer> jsonVehicleCustomerList = new ArrayList<JsonCustomer>();
            while (customer != null) {
                Location customerLocation = customer.getLocation();
                demandTotal += customer.getDemand();
                jsonVehicleCustomerList.add(new JsonCustomer(customerLocation.getName(),
                        customerLocation.getLatitude(), customerLocation.getLongitude(), customer.getDemand()));
                customer = customer.getNextCustomer();
            }
            jsonVehicleRoute.setDemandTotal(demandTotal);
            jsonVehicleRoute.setCustomerList(jsonVehicleCustomerList);
            jsonVehicleRouteList.add(jsonVehicleRoute);
        }
        jsonSolution.setVehicleRouteList(jsonVehicleRouteList);
        HardSoftLongScore score = solution.getScore();
        jsonSolution.setFeasible(score != null && score.isFeasible());
        jsonSolution.setDistance(solution.getDistanceString(NUMBER_FORMAT));
        return jsonSolution;
    }

    @Override
    public JsonMessage solve() {
        boolean success = solverManager.solve(request.getSession().getId());
        return new JsonMessage(success ? "Solving started." : "Solver was already running.");
    }

    @Override
    public JsonMessage terminateEarly() {
        boolean success = solverManager.terminateEarly(request.getSession().getId());
        return new JsonMessage(success ? "Solver terminating early." : "Solver was already terminated.");
    }

}
