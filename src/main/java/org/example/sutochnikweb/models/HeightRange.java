package org.example.sutochnikweb.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Класс для представления диапазона высот с операциями
public class HeightRange {
    private final String name;
    private List<Action> actions;

    public HeightRange(String name) {
        this.name = name;
        this.actions = new ArrayList<>();
    }

    public void addAction(ActionType type, int start, int end, int duration) {
        actions.add(new Action(type, start, end, duration));
    }

    public void addAction(ActionType type, int start, int end, int duration, int otherNumInfo) {
        actions.add(new Action(type, start, end, duration, otherNumInfo));
    }

    public void addAction(ActionType type, int start, int end, int duration, String otherInfo) {
        actions.add(new Action(type, start, end, duration, otherInfo));
    }

    public void addAction(ActionType type, int start, int end, int duration, int otherNumInfo, String otherInfo) {
        actions.add(new Action(type, start, end, duration, otherNumInfo, otherInfo));
    }

    public String getName() {
        return name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void numberActionsByStart() {
        // Сортировка действий по полю start
        actions.sort(Comparator.comparingInt(Action::getStart));
        // Нумерация действий
        for (int i = 0; i < actions.size(); i++) {
            actions.get(i).setOperationNumber(i + 1);
        }
    }
}