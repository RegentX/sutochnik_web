package org.example.sutochnikweb.models;

// Перечисление для типов операций
public enum ActionType {
    TRAIN_ARRIVAL, // прием поезда, готово
    PASSENGER_TRAIN_ARRIVAL, // прием пассажирского поезда, готово
    PASSENGER_TRAIN_DEPARTURE, // отправление пассажирского поезда, готово
    TRAIN_DEPARTURE, // отправление поезда, готово
    SHUNTING, // перестановка, готово
    LOCOMOTIVE_CLEANING, // уборка поездного локомотива, готово
    LOCOMOTIVE_PROVISION, // подача поездного локомотива, готово
    ADVANCEMENT, // надвиг, готово
    LOCOMOTIVE_MOVEMENT_RESERVE, // движение локомотива резервом, готово
    SIDETRACK_CLEANING, // уборка с подъездного пути, готово
    SIDETRACK_PROVISION, // подача на подъездной путь, готово
    TRAIN_INSPECTION_STOP, // осмотр состава/стоянка, готово
    FORMATION_COMPLETION, // окончание формирования, готово
    BRAKE_TESTING, // опробование тормозов, готово
    TRAIN_SECURING, // закрепление состава
    PASSENGER_TRAIN_STOP, // стоянка пассажирского поезда
    FRONT_ALIGNMENT, // расстановка по фронтам
    UNLOADING, // выгрузка, готово
    CAR_DETACHMENT, // отцепка вагонов
    TRAIN_LOCOMOTIVE_DETACHMENT, // отцепка поездного локомотива, готово
    SHUNTING_LOCOMOTIVE_DETACHMENT, // отцепка маневрового локомотива, готово
    SHUNTING_LOCOMOTIVE_ATTACHMENT, // прицепка маневрового локомотива, готово
    TRAIN_LOCOMOTIVE_ATTACHMENT, // прицепка поездного локомотива, готово
    TRAIN_PRESENTATION, // предъявление состава
    TRAIN_HANDOVER, // отдача состава
    IDLE_TIME, // простой
    MOVEMENT_WAIT, // ожидание движения
    SLOT_WAIT, // ожидание нитки
    CREW_WAIT, // ожидание бригады
    TRAIN_LOCOMOTIVE_ENTRY, // заезд поездного локомотива
}