package org.example.sutochnikweb.models;

public enum ActionType {
    TRAIN_ARRIVAL("Прием поезда"),
    PASSENGER_TRAIN_ARRIVAL("Прием пассажирского поезда"),
    PASSENGER_TRAIN_DEPARTURE("Отправление пассажирского поезда"),
    TRAIN_DEPARTURE("Отправление поезда"),
    SHUNTING("Перестановка"),
    LOCOMOTIVE_CLEANING("Уборка поездного локомотива"),
    LOCOMOTIVE_PROVISION("Подача поездного локомотива"),
    ADVANCEMENT("Надвиг"),
    LOCOMOTIVE_MOVEMENT_RESERVE("Движение локомотива резервом"),
    SIDETRACK_CLEANING("Уборка с подъездного пути"),
    SIDETRACK_PROVISION("Подача на подъездной путь"),
    TRAIN_INSPECTION_STOP("Осмотр состава/стоянка"),
    FORMATION_COMPLETION("Окончание формирования"),
    BRAKE_TESTING("Опробование тормозов"),
    TRAIN_SECURING("Закрепление состава"),
    PASSENGER_TRAIN_STOP("Стоянка пассажирского поезда"),
    FRONT_ALIGNMENT("Расстановка по фронтам"),
    UNLOADING("Выгрузка"),
    LOADING("Погрузка"),
    CAR_DETACHMENT("Отцепка вагонов"),
    TRAIN_LOCOMOTIVE_DETACHMENT("Отцепка поездного локомотива"),
    SHUNTING_LOCOMOTIVE_DETACHMENT("Отцепка маневрового локомотива"),
    SHUNTING_LOCOMOTIVE_ATTACHMENT("Прицепка маневрового локомотива"),
    TRAIN_LOCOMOTIVE_ATTACHMENT("Прицепка поездного локомотива"),
    TRAIN_PRESENTATION("Предъявление состава"),
    TRAIN_HANDOVER("Отдача состава"),
    IDLE_TIME("Простой"),
    MOVEMENT_WAIT("Ожидание движения"),
    SLOT_WAIT("Ожидание нитки"),
    CREW_WAIT("Ожидание бригады"),
    TRAIN_LOCOMOTIVE_ENTRY("Заезд поездного локомотива"),
    TRAIN_DISSOLUTION("Роспуск состава"),
    SHUNTING_LOCOMOTIVE_RECOUPLING("Перецепка маневрового локомотива"),
    HUMP_LOCOMOTIVE_ATTACHMENT_FOR_ADVANCEMENT("Прицепка горочного локомотива для надвига"),
    CAR_PUSHBACK("Осаживание вагонов"),
    DISSOLUTION_PERMISSION_WAIT("Ожидание разрешения на роспуск"),
    ACCUMULATION("Накопление");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
