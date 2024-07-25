package org.example.sutochnikweb.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathData {
    private PointAccum startPoint;
    private List<PointAccum> changePoints;
    private PointAccum endPoint;

    public PathData(PointAccum startPoint) {
        this.startPoint = startPoint;
        this.changePoints = new ArrayList<>();
    }

    public void setEndPoint(PointAccum endPoint) {
        this.endPoint = endPoint;
    }

    public PointAccum getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(PointAccum startPoint) {
        this.startPoint = startPoint;
    }

    public List<PointAccum> getChangePoints() {
        return changePoints;
    }

    public void setChangePoints(List<PointAccum> changePoints) {
        this.changePoints = changePoints;
    }

    public PointAccum getEndPoint() {
        return endPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathData pathData = (PathData) o;
        return Objects.equals(startPoint, pathData.startPoint) && Objects.equals(changePoints, pathData.changePoints) && Objects.equals(endPoint, pathData.endPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPoint, changePoints, endPoint);
    }
}
