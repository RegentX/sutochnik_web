package org.example.sutochnikweb.models;

// Класс действия (операции)
public class Action {
    private final ActionType type;
    private final int start;
    private final int end;
    private final int duration;
    private int operationNumber;
    private final int otherNumInfo;
    private final String otherInfo;

    public Action(ActionType type, int start, int end, int duration, int otherNumInfo, String otherInfo) {
        this.type = type;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.otherNumInfo = otherNumInfo;
        this.otherInfo = otherInfo;
    }

    public Action(ActionType type, int start, int end, int duration) {
        this(type, start, end, duration, 0, "");
    }

    public Action(ActionType type, int start, int end, int duration, int otherNumInfo) {
        this(type, start, end, duration, otherNumInfo, "");
    }

    public Action(ActionType type, int start, int end, int duration, String otherInfo) {
        this(type, start, end, duration, 0, otherInfo);
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

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOperationNumber(int operationNumber) {
        this.operationNumber = operationNumber;
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", start=" + Double.parseDouble(String.valueOf(start)) / 60 / 1000 +
                ", end=" + Double.parseDouble(String.valueOf(end)) / 60 / 1000 +
                ", duration=" + duration +
                ", operationNumber=" + operationNumber +
                ", otherNumInfo=" + otherNumInfo +
                ", otherInfo='" + otherInfo + '\'' +
                '}';
    }
}