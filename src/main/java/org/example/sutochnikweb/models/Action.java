package org.example.sutochnikweb.models;

// Класс действия (операции)
public class Action {
    private final ActionType type;
    private final int start;
    private final int end;
    private final int duration;
    private final int operationNumber;
    private int otherNumInfo;
    private String otherInfo;

    public Action(ActionType type, int start, int end, int duration, int operationNumber) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.operationNumber = operationNumber;
    }

    public ActionType getType() {
        return type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getDuration() {
        return duration;
    }

    public int getOperationNumber() {
        return operationNumber;
    }

    public int getOtherNumInfo() {
        return otherNumInfo;
    }

    public void setOtherNumInfo(int otherNumInfo) {
        this.otherNumInfo = otherNumInfo;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", start=" + start +
                ", end=" + end +
                ", duration=" + duration +
                ", operationNumber=" + operationNumber +
                ", otherNumInfo=" + otherNumInfo +
                ", otherInfo='" + otherInfo + '\'' +
                '}';
    }
}