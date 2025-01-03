package aoc2024;

import java.util.*;

public class Day12 implements Day {
    @Override
    public long executePart1(String input) {
        GardenMap map = GardenMap.create(input);
        Garden garden = Garden.create(map);
        return garden.price();
    }

    @Override
    public long executePart2(String input) {
        GardenMap map = GardenMap.create(input);
        Garden garden = Garden.create(map);
        return garden.priceWithDiscount();
    }

    record GardenMap(char[][] map) {
        public static GardenMap create(String input) {
            String[] lines = input.strip().split("\n");
            char[][] map = new char[lines.length][lines[0].length()];
            for (int i = 0; i < lines.length; i++) {
                map[i] = lines[i].toCharArray();
            }
            return new GardenMap(map);
        }

        public int length() {
            return map[0].length;
        }

        public int height() {
            return map.length;
        }

        public boolean isInside(int x, int y) {
            return x >= 0 && x < length() && y >= 0 && y < height();
        }

        public int perimeter(int x, int y) {
            int perimeter = 0;
            char label = map[x][y];
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

            for (int[] dir : directions) {
                int newX = x + dir[0];
                int newY = y + dir[1];
                if (isInside(newX, newY)) {
                    if (map[newX][newY] != label) perimeter++;
                } else {
                    perimeter++;
                }
            }
            return perimeter;
        }

    }

    record Garden(List<Region> regions) {
        public static Garden create(GardenMap gardenMap) {
            List<Region> regions = calculateRegions(gardenMap);
            return new Garden(regions);
        }

        public static List<Region> calculateRegions(GardenMap gardenMap) {
            Map<Character, List<ParcelGroup>> parcelsGroupsMap = new LinkedHashMap<>();
            char[][] map = gardenMap.map();
            for (int y = 0; y < map[0].length; y++) {
                for (int x = 0; x < map.length; x++) {
                    char label = map[x][y];
                    int perimeter = gardenMap.perimeter(x, y);
                    Parcel parcel = new Parcel(x, y, perimeter);
                    parcelsGroupsMap.computeIfAbsent(label, k -> new ArrayList<>());
                    List<ParcelGroup> parcelGroups = parcelsGroupsMap.get(label);
                    List<ParcelGroup> parcelGroupContiguous = parcelGroups.stream()
                            .filter(pg -> pg.isContiguous(parcel))
                            .toList();
                    if (parcelGroupContiguous.isEmpty()) {
                        ParcelGroup newParcelGroup = new ParcelGroup(label, new HashSet<>());
                        parcelGroups.add(newParcelGroup);
                        newParcelGroup.add(parcel);
                    } else if (parcelGroupContiguous.size() == 1) {
                        parcelGroupContiguous.getFirst().add(parcel);
                    } else {
                        ParcelGroup firstGroup = parcelGroupContiguous.getFirst();
                        firstGroup.parcels.add(parcel);
                        parcelGroupContiguous.stream()
                                .filter(pg -> pg != firstGroup)
                                .forEach(pg -> {
                                    firstGroup.parcels().addAll(pg.parcels());
                                    parcelGroups.remove(pg);
                                });
                    }
                }
            }
            return parcelsGroupsMap.entrySet().stream()
                    .map(entry -> new Region(entry.getKey(), entry.getValue()))
                    .toList();
        }

        public long price() {
            return regions.stream()
                    .mapToLong(Region::price)
                    .sum();
        }

        public long priceWithDiscount() {
            return regions.stream()
                    .mapToLong(Region::priceWithDiscount)
                    .sum();
        }
    }

    record Parcel(int x, int y, int perimeter) {
        public boolean isAdjacent(Parcel parcel) {
            return Math.abs(x - parcel.x) + Math.abs(y - parcel.y) == 1;
        }

        public boolean hasAdjacentToRight(Parcel p) {
            return x == p.x + 1 && y == p.y;
        }

        public boolean hasAdjacentToLeft(Parcel p) {
            return x == p.x - 1 && y == p.y;
        }

        public boolean hasAdjacentToUp(Parcel p) {
            return x == p.x && y == p.y + 1;
        }

        public boolean hasAdjacentToDown(Parcel p) {
            return x == p.x && y == p.y - 1;
        }

    }

    record ParcelGroup(char label, Set<Parcel> parcels) {
        public boolean isContiguous(Parcel parcel) {
            return parcels.stream().anyMatch(p -> p.isAdjacent(parcel));
        }

        public void add(Parcel parcel) {
            parcels.add(parcel);
        }

        public int area() {
            return parcels.size();
        }

        public int sides() {
            int vertices = 0;
            for (Parcel parcel : parcels) {
                boolean existsUpVertex = parcels.stream()
                        .filter(p -> !p.equals(parcel))
                        .anyMatch(p -> !parcel.hasAdjacentToRight(p) || (parcel.hasAdjacentToRight(p) &&
                                (parcels.stream().anyMatch(p::hasAdjacentToUp) || parcels.stream().anyMatch(p::hasAdjacentToDown))));
                if (existsUpVertex) vertices++;
                boolean existsRightVertex = parcels.stream()
                        .filter(p -> !p.equals(parcel))
                        .anyMatch(p -> !parcel.hasAdjacentToDown(p) || (parcel.hasAdjacentToDown(p) &&
                                (parcels.stream().anyMatch(p::hasAdjacentToLeft) || parcels.stream().anyMatch(p::hasAdjacentToRight))));
                if (existsRightVertex) vertices++;
                boolean existsDownVertex = parcels.stream()
                        .filter(p -> !p.equals(parcel))
                        .anyMatch(p -> !parcel.hasAdjacentToLeft(p) || (parcel.hasAdjacentToLeft(p) &&
                                (parcels.stream().anyMatch(p::hasAdjacentToUp) || parcels.stream().anyMatch(p::hasAdjacentToDown))));
                if (existsDownVertex) vertices++;
                boolean existsLeftVertex = parcels.stream()
                        .filter(p -> !p.equals(parcel))
                        .anyMatch(p -> !parcel.hasAdjacentToUp(p) || (parcel.hasAdjacentToUp(p) &&
                                (parcels.stream().anyMatch(p::hasAdjacentToRight) || parcels.stream().anyMatch(p::hasAdjacentToLeft))));
                if (existsLeftVertex) vertices++;
            }
            return vertices;
        }

        private int perimeter() {
            return parcels.stream().mapToInt(Parcel::perimeter).sum();
        }

        public long price() {
            return (long) perimeter() * area();
        }

        public long priceWithDiscount() {
            return (long) area() * sides();
        }
    }

    record Region(char label, List<ParcelGroup> parcelGroups) {

        public long price() {
            return parcelGroups.stream().mapToLong(ParcelGroup::price).sum();
        }


        public long priceWithDiscount() {
            return parcelGroups.stream().mapToLong(ParcelGroup::priceWithDiscount).sum();
        }

        public String toString() {
            return label + " " + price();
        }
    }
}
