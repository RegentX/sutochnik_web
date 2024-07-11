package org.example.sutochnikweb.services;

import org.example.sutochnikweb.models.Action;
import org.example.sutochnikweb.models.ActionType;
import org.example.sutochnikweb.models.HeightRange;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
@Service
public class SVGService {
    public Map<String, HeightRange> parseSvg(File svgFile) {
        try {
            // Указание полного пути к файлу SVG, если не фуричит значит что-то с кодировкой в проекте
            // Создание фабрики и билдера для парсинга XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Парсинг XML-документа
            Document document = builder.parse(svgFile);
            document.getDocumentElement().normalize();

            // Получение списка всех элементов <g> в документе
            NodeList gList = document.getElementsByTagName("g");

            // Диапазоны высот, которые нас интересуют (высота учитывается согласно y)
            int documentSize = 138;
            int[] heightRanges = new int[documentSize]; // Общее количество диапазонов
            int rangeStart = 51;
            for (int i = 0; i < heightRanges.length; i++) {
                heightRanges[i] = rangeStart + (i * 40);
            }

            Map<Integer, HeightRange> heightRangesMap = new LinkedHashMap<>();
            int numOfOperationInLine = 0;
            double elementStartX = 0.0;
            double elementEndX = 0.0;
            double elementWidth = 0.0;
            String elementText;
            int elementY = 0; // высота элемента для дальнейшего определения его в диапазон

            for (int i = 0; i < gList.getLength(); i++) { // первый прогон для именования диапазонов
                Element gElement = (Element) gList.item(i);
                String gId = gElement.getAttribute("id");
                if ("DailyDiagramCaptionView".equals(gId)) {
                    NodeList childGList = gElement.getElementsByTagName("g");
                    // Проходим по списку дочерних <g> элементов
                    for (int j = 0; j < childGList.getLength(); j++) {
                        Element childGElement = (Element) childGList.item(j);
                        NodeList textList = childGElement.getElementsByTagName("text");
                        if (textList.getLength() > 0) {
                            Element textElement = (Element) textList.item(0);
                            String textContent = textElement.getTextContent().trim();

                            // Извлекаем координаты текста
                            String textY = textElement.getAttribute("y");
                            double textYValue = Double.parseDouble(textY);

                            // Находим соответствующий диапазон высоты
                            for (int k = 0; k < heightRanges.length; k++) {
                                int range = heightRanges[k];
                                if (textYValue >= range && textYValue <= range + 39) {
                                    // Создаем объект HeightRange и добавляем в Map
                                    HeightRange heightRange = new HeightRange(textContent);
                                    heightRangesMap.put(range, heightRange);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // в код что выше просто верьте

            // Перебор каждого элемента <g>
            for (int i = 0; i < gList.getLength(); i++) { // основной прогон с определением элементов и добавлением их в диапазон

                Element gElement = (Element) gList.item(i);
                NodeList childNodes = gElement.getChildNodes(); // Получаем список всех дочерних узлов внутри <g>
                int gChildNodeCount = 0; // количество дочерних элеменотов, с этим удобно отсекать лишнее в купе с количеством элементов определённого вида
                for (int j = 0; j < childNodes.getLength(); j++) { // Подсчитываем количество дочерних элементов типа ELEMENT_NODE, фигуры или текст если проще, что рисуется
                    Node node = childNodes.item(j);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        gChildNodeCount++;
                    }
                }

                // тут списки всех элементов внутри g
                NodeList lineList = gElement.getElementsByTagName("line");
                NodeList rectList = gElement.getElementsByTagName("rect");
                NodeList pathList = gElement.getElementsByTagName("path");
                NodeList ellipseList = gElement.getElementsByTagName("ellipse");
                NodeList textList = gElement.getElementsByTagName("text");

                elementText = "0";
                if (textList.getLength() == 1) {
                    Element textElement = (Element) textList.item(0);
                    elementText = textElement.getTextContent();
                }
                //- прием поезда
                //- прием пассажирского поезда
                //- отправление пассажирского поезда
                //- отправление поезда
                // прием и отправление любого поезда (красные и черные треугольники)
                if (pathList.getLength() == 1 && rectList.getLength() == 0) {
                    Element pathElement = (Element) pathList.item(0);
                    String pathFill = pathElement.getAttribute("fill");
                    // отсюда начинается поиск треугольников
                    // в условии отсекаем линии чтобы не захватить стрелки
                    if (lineList.getLength() == 0) {
                        String dAttribute = pathElement.getAttribute("d");
                        // Разбиение значения атрибута 'd' на отдельные команды
                        String[] commands = dAttribute.split("\\s+");
                        elementStartX = Double.parseDouble(commands[7]);
                        elementEndX = Double.parseDouble(commands[4]);
                        elementY = (int) Math.round(Double.parseDouble(commands[2]));
                        for (int range : heightRanges) { // определение в диапазон по высоте
                            if (elementY >= range && elementY <= range + 39) { // определение в диапазон по высоте
                                if(commands[1].equals(commands[4]) && commands[4].equals(commands[10])) { // треугольник на подъем
                                    if(pathFill.equals("#FF0000")){
                                        numOfOperationInLine = heightRangesMap.get(range).addAction(ActionType.PASSENGER_TRAIN_ARRIVAL, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                        heightRangesMap.get(range).getActions().get(numOfOperationInLine - 1).setOtherNumInfo(Integer.parseInt(elementText)); // номер поезда
                                        break;
                                    } else if(pathFill.equals("#000000")){
                                        numOfOperationInLine = heightRangesMap.get(range).addAction(ActionType.TRAIN_ARRIVAL, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                        heightRangesMap.get(range).getActions().get(numOfOperationInLine - 1).setOtherNumInfo(Integer.parseInt(elementText)); // номер поезда
                                        break;
                                    }
                                } else if(commands[1].equals(commands[7]) && commands[7].equals(commands[10])){ // треугольник на спуск
                                    if(pathFill.equals("#FF0000")){
                                        numOfOperationInLine = heightRangesMap.get(range).addAction(ActionType.PASSENGER_TRAIN_DEPARTURE, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                        heightRangesMap.get(range).getActions().get(numOfOperationInLine - 1).setOtherNumInfo(Integer.parseInt(elementText)); // номер поезда
                                        break;
                                    } else if(pathFill.equals("#000000")){
                                        numOfOperationInLine = heightRangesMap.get(range).addAction(ActionType.TRAIN_DEPARTURE, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                        heightRangesMap.get(range).getActions().get(numOfOperationInLine - 1).setOtherNumInfo(Integer.parseInt(elementText)); // номер поезда
                                        break;
                                    } else if (pathFill.equals("#FFFFFF")){
                                        heightRangesMap.get(range).addAction(ActionType.FORMATION_COMPLETION, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                //- перестановка X
                //- уборка поездного локомотива V
                //- подача поездного локомотива ^
                //- надвиг ≡
                //- движение локомотива резервом o
                //- уборка с подъездного пути (стреска вниз)
                //- подача на подъездной путь (стрелка вверх)
                //- еще много чего)
                // прямоугольники с: X, V, ^, o, ≡, стрелка вниз, стрелка вверх, -, полосой по диагонали идущей  вниз и вверх
                else if(rectList.getLength() == 1) {
                    Element rectElement = (Element) rectList.item(0);
                    String rectFill = rectElement.getAttribute("fill");
                    String stroke = rectElement.getAttribute("stroke");
                    elementStartX =  Double.parseDouble(rectElement.getAttribute("x"));
                    elementWidth = Double.parseDouble(rectElement.getAttribute("width"));
                    elementEndX = elementStartX + elementWidth;
                    if (rectFill.equals("#FFFFFF") && stroke.equals("#000000")) { // Проверка на нужный прямоугольник
                        String yAttribute = rectElement.getAttribute("y");
                        elementY = Integer.parseInt(yAttribute);
                        for (int range : heightRanges) { // определение в диапазон по высоте
                            if (elementY >= range && elementY <= range + 39) { // определение в диапазон по высоте
                                if (gChildNodeCount == 1) {
                                    heightRangesMap.get(range).addAction(ActionType.TRAIN_INSPECTION_STOP, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                    break;
                                } else if (gChildNodeCount == 2 && textList.getLength() == 1) {

                                }
                                if (lineList.getLength() == 3) { // прямоугольник с 3 линиями ≡
                                    heightRangesMap.get(range).addAction(ActionType.ADVANCEMENT, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                    break;
                                } else if (lineList.getLength() == 2){ // прямоугольник с 2 линиями
                                    Element firstLineElement = (Element) lineList.item(0);
                                    String firstLineX1Attribute = firstLineElement.getAttribute("x1");
                                    double firstLineY1Attribute = Double.parseDouble(firstLineElement.getAttribute("y1"));
                                    Element secondLineElement = (Element) lineList.item(1);
                                    String secondLineX1Attribute = secondLineElement.getAttribute("x1");
                                    double secondLineY1Attribute = Double.parseDouble(secondLineElement.getAttribute("y1"));
                                    if(firstLineX1Attribute.equals(secondLineX1Attribute)){ // крест X
                                        heightRangesMap.get(range).addAction(ActionType.SHUNTING, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    } else if (firstLineY1Attribute < secondLineY1Attribute){ // V
                                        heightRangesMap.get(range).addAction(ActionType.LOCOMOTIVE_CLEANING, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    } else if (firstLineY1Attribute > secondLineY1Attribute){ // ^
                                        heightRangesMap.get(range).addAction(ActionType.LOCOMOTIVE_PROVISION, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    }
                                } else if(lineList.getLength() == 1 && pathList.getLength() == 1 && gChildNodeCount == 3){ // прямоугольник со стрелкой
                                    Element firstLineElement = (Element) lineList.item(0);
                                    double firstLineY1Attribute = Double.parseDouble(firstLineElement.getAttribute("y1"));
                                    if (firstLineY1Attribute > Double.parseDouble(yAttribute)) { // стрелка вверх
                                        heightRangesMap.get(range).addAction(ActionType.SIDETRACK_PROVISION, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    } else if (firstLineY1Attribute == Double.parseDouble(yAttribute)) { // стрелка вниз
                                        heightRangesMap.get(range).addAction(ActionType.SIDETRACK_CLEANING, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    }
                                } else if(lineList.getLength() == 1 && gChildNodeCount == 2){
                                    Element firstLineElement = (Element) lineList.item(0);
                                    double firstLineY1Attribute = Double.parseDouble(firstLineElement.getAttribute("y1"));
                                    double firstLineY2Attribute = Double.parseDouble(firstLineElement.getAttribute("y2"));
                                    if (firstLineY1Attribute == firstLineY2Attribute) { // прямая линия
                                        heightRangesMap.get(range).addAction(ActionType.BRAKE_TESTING, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    } else if(firstLineY1Attribute > firstLineY2Attribute){ // прямоугольник с линией идущей снизу вверх
                                        heightRangesMap.get(range).addAction(ActionType.SHUNTING_LOCOMOTIVE_ATTACHMENT, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    } else if(firstLineY1Attribute < firstLineY2Attribute){ // прямоугольник с линией идущей сверху вниз
                                        heightRangesMap.get(range).addAction(ActionType.SHUNTING_LOCOMOTIVE_DETACHMENT, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                        break;
                                    }
                                }
                                if (pathList.getLength() == 1 && gChildNodeCount == 2) { // прямоугольник с треугольником
                                    Element pathElement = (Element) pathList.item(0);
                                    String pathFill = pathElement.getAttribute("fill");
                                    String dAttribute = pathElement.getAttribute("d");
                                    String[] commands = dAttribute.split("\\s+");
                                    if (commands[1].equals(commands[4]) && commands[4].equals(commands[10])) { // треугольник на подъем
                                        System.out.println();
                                    } else if (commands[1].equals(commands[7]) && commands[7].equals(commands[10])) { // треугольник на спуск
                                        if (commands[2].equals(commands[5])) { // отзеркаленый треугольник на подъём(треугольник в верхнем левом углу)
                                            heightRangesMap.get(range).addAction(ActionType.TRAIN_LOCOMOTIVE_ATTACHMENT, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                            break;
                                        } else if (pathFill.equals("#8B4513")) { //D2691E другой оттенок, надо спросить
                                            heightRangesMap.get(range).addAction(ActionType.UNLOADING, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                            break;
                                        }
                                    } else if (commands[1].equals(commands[10]) && commands[4].equals(commands[7])) {  // отзеркаленый треугольник на спуск(треугольник в верхнем правом углу)
                                        heightRangesMap.get(range).addAction(ActionType.TRAIN_LOCOMOTIVE_DETACHMENT, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementStartX, elementEndX));
                                        break;
                                    }
                                }
                                if (ellipseList.getLength() == 1 && gChildNodeCount == 2){ // o
                                    heightRangesMap.get(range).addAction(ActionType.LOCOMOTIVE_MOVEMENT_RESERVE, calculateTime(elementStartX), calculateTime(elementEndX), calculateTimeDuration(elementWidth));
                                    break;
                                }
                            }
                        }
                    }
                }

                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры
                // TODO: Остальные фигуры

            }
            // Вывод информации о каждом диапазоне и действиях в нём
            //TODO: Костыли!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Map<String, HeightRange> stringHeightRangeHashMap = new LinkedHashMap<>();
            for (Map.Entry<Integer, HeightRange> entry : heightRangesMap.entrySet()) {
                HeightRange range = entry.getValue();
                stringHeightRangeHashMap.put(range.getName(), entry.getValue());
            }
            /*for (Map.Entry<String, HeightRange> entry : stringHeightRangeHashMap.entrySet()) {
                HeightRange range = entry.getValue();
                System.out.println("Диапазон " + " имеет название: " + range.getName());
                for (Action action : range.getActions()) {
                    System.out.println(action);
                }
            }*/
            return stringHeightRangeHashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Метод для подсчёта времени по x в минутах
    // TODO: Позже нужен фикс
    // тут округление идёт, потом нужно переделать, так как иногда даёт нули и округление это плохо, но работает исправно
    public static int calculateTime(double x) {
        x = x - 250; // ноль находится на 250
        return (int) (Math.abs(x) / 180 * 60); // в часу 180
    }

    // Метод для подсчёта продолжительности в минутах по двум координатам в минутах
    // TODO: Позже нужен фикс
    // тут округление идёт, потом нужно переделать, так как иногда даёт нули и округление это плохо, но работает исправно
    public static int calculateTimeDuration(double elementStartX, double elementEndX) {
        return (int) (Math.abs(elementEndX - elementStartX) / 180 * 60);
    }

    // Метод для подсчёта продолжительности по ширине в минутах
    // TODO: Позже нужен фикс
    // тут округление идёт, потом нужно переделать, так как иногда даёт нули и округление это плохо, но работает исправно
    public static int calculateTimeDuration(double elementWidth) {
        return (int) (Math.abs(elementWidth) / 180 * 60); // в часу 180
    }


}
