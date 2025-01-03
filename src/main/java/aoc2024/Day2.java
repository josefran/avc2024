package aoc2024;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Day2 implements Day {

    @Override
    public long executePart1(String input) {
        List<Report> reports = buildReports(input);
        return reports.stream()
                .filter(Report::isSecure)
                .count();
    }

    @Override
    public long executePart2(String input) {
        List<Report> reports = buildReports(input);
        return reports.stream()
                .filter(Report::isSecureWithTolerance)
                .count();
    }

    private static List<Report> buildReports(String input) {
        List<String> lines = List.of(input.trim().split("\n"));
        List<Report> reports = new LinkedList<>();
        for (String line : lines) {
            line = line.trim();
            String[] levels = line.split("\\s+");
            reports.add(Report.create(levels));
        }
        return reports;
    }

    record Report(List<Integer> levels) {
        static Report create(String[] levels) {
            List<Integer> levelsList = Arrays.stream(levels).map(Integer::parseInt).toList();
            return new Report(levelsList);
        }

        boolean isSecure() {
            TypeLevel typeLevel = typeOfIncrement(0);
            if (typeLevel == TypeLevel.SAME_LEVEL) return false;
            for (int i = 0; i < levels.size(); i++) {
                int nextIndex = i + 1;
                if (nextIndex < levels.size()) {
                    if (changeIncrement(typeLevel, i)) return false;
                    if (isDistanceExcessive(i)) return false;
                }
            }
            return true;
        }

        @SuppressWarnings("SuspiciousListRemoveInLoop")
        boolean isSecureWithTolerance() {
            if (isSecure()) return true;
            for (int i = 0; i < levels.size(); i++) {
                List<Integer> auxLevels = new LinkedList<>(levels);
                auxLevels.remove(i);
                boolean secure = new Report(auxLevels).isSecure();
                if (secure) return true;
            }
            return false;
        }

        private boolean isDistanceExcessive(int i) {
            return !checkDistanceLessThan(i);
        }

        private boolean changeIncrement(TypeLevel typeLevel, int i) {
            return typeLevel != typeOfIncrement(i);
        }

        boolean checkDistanceLessThan(int i1) {
            return Math.abs(levels.get(i1 + 1) - levels.get(i1)) < 4; // && 0 < Math.abs(levels.get(i2) - levels.get(i1)) ;
        }

        TypeLevel typeOfIncrement(int i1) {
            if (levels.get(i1).equals(levels.get(i1 + 1))) return TypeLevel.SAME_LEVEL;
            else if (levels.get(i1) - levels.get(i1 + 1) > 0) return TypeLevel.INCREASE;
            else return TypeLevel.DECREASE;
        }

    }

    enum TypeLevel {
        INCREASE,
        DECREASE,
        SAME_LEVEL
    }
}
