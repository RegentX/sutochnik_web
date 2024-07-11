package org.example.sutochnikweb.models;

import java.util.ArrayList;
import java.util.List;

// Класс для представления диапазона высот с операциями
public class HeightRange {
    private final String name;
    private List<Action> actions;

    public HeightRange(String name) {
        this.name = name;
        this.actions = new ArrayList<>();
    }

    public int addAction(ActionType type, int start, int end, int duration) {
        int operationNumber = actions.size() + 1;
        actions.add(new Action(type, start, end, duration, operationNumber));
        return operationNumber;
    }

    public String getName() {
        return name;
    }

    public List<Action> getActions() {
        return actions;
    }
}