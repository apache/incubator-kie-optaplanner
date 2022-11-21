package org.optaplanner.examples.tsp.persistence;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.optaplanner.examples.tsp.domain.TspSolution;
import org.optaplanner.examples.tsp.domain.location.DistanceType;
import org.optaplanner.examples.tsp.domain.location.RoadLocation;
import org.optaplanner.persistence.jackson.impl.domain.solution.JacksonSolutionFileIO;

public class TspSolutionFileIO extends JacksonSolutionFileIO<TspSolution> {

    public TspSolutionFileIO() {
        super(TspSolution.class);
    }

    @Override
    public TspSolution read(File inputSolutionFile) {
        TspSolution tspSolution = super.read(inputSolutionFile);

        if (tspSolution.getDistanceType() == DistanceType.ROAD_DISTANCE) {
            deduplicateRoadLocations(tspSolution);
        }

        return tspSolution;
    }

    private void deduplicateRoadLocations(TspSolution tspSolution) {
        var roadLocationList = tspSolution.getLocationList().stream()
                .filter(location -> location instanceof RoadLocation)
                .map(location -> (RoadLocation) location)
                .collect(Collectors.toList());
        var locationsById = roadLocationList.stream()
                .collect(Collectors.toMap(RoadLocation::getId, Function.identity()));
        /*
         * Replace the duplicate RoadLocation instances in the travelDistanceMap by references to instances from
         * the locationList.
         */
        for (RoadLocation roadLocation : roadLocationList) {
            var newTravelDistanceMap = deduplicateMap(roadLocation.getTravelDistanceMap(),
                    locationsById, RoadLocation::getId);
            roadLocation.setTravelDistanceMap(newTravelDistanceMap);
        }

        // Domiciles and visits have locations as well.
        tspSolution.getDomicile()
                .setLocation(locationsById.get(tspSolution.getDomicile().getId()));
        tspSolution.getVisitList()
                .forEach(visit -> visit.setLocation(locationsById.get(visit.getLocation().getId())));
    }

    private <Key, Value, Index> Map<Key, Value> deduplicateMap(Map<Key, Value> originalMap, Map<Index, Key> index,
            Function<Key, Index> idFunction) {
        if (originalMap == null || originalMap.isEmpty()) {
            return originalMap;
        }

        var newMap = new LinkedHashMap<Key, Value>(originalMap.size());
        originalMap.forEach(
                (key, value) -> newMap.put(index.get(idFunction.apply(key)), value));
        return newMap;
    }

}
