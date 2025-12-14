package functions;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction implements TabulatedFunction, Serializable {
    private class FunctionNode {
        private FunctionPoint point;
        private FunctionNode prev;
        private FunctionNode next;
        
        public FunctionNode(FunctionPoint point, FunctionNode prev, FunctionNode next) {
            this.point = point;
            this.prev = prev;
            this.next = next;
        }

        public FunctionPoint getPoint() {
            return point;
        }


        public FunctionNode getPrev() {
            return prev;
        }

        public void setPrev(FunctionNode prev) {
            this.prev = prev;
        }

        public FunctionNode getNext() {
            return next;
        }

        public void setNext(FunctionNode next) {
            this.next = next;
        }
    }
    
    private FunctionNode head;
    private int pointsCount;
    private final double EPSILON_DOUBLE = 1e-9;
    
    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс" + index + " лежит вне границ массива" + pointsCount);
        }

        FunctionNode current;

        if (index < pointsCount / 2) {
            current = head.getNext();
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }
        } else {
            current = head;
            for (int i = pointsCount; i > index; i--) {
                current = current.getPrev();
            }
        }

        return current;
    }
    
    private FunctionNode addNodeToTail(FunctionPoint point) {
        FunctionNode newNode = new FunctionNode(point, head.getPrev(), head);
        FunctionNode tail = head.getPrev();
        tail.setNext(newNode);
        head.setPrev(newNode);
        pointsCount++;
        return newNode;
    }

    private FunctionNode addNodeByIndex(int index, FunctionPoint point) {
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " лежит вне границ массива " + pointsCount);
        }
        if (index == pointsCount) {
            return addNodeToTail(point);
        }
        FunctionNode nextNode = getNodeByIndex(index);
        FunctionNode prevNode = nextNode.getPrev();
        FunctionNode newNode = new FunctionNode(point, prevNode, nextNode);
        prevNode.setNext(newNode);
        nextNode.setPrev(newNode);
        pointsCount++;
        return newNode;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " лежит вне границ массива " + pointsCount);
        }
        FunctionNode nodeToDelete = getNodeByIndex(index);
        FunctionNode prevNode = nodeToDelete.getPrev();
        FunctionNode nextNode = nodeToDelete.getNext();
        prevNode.setNext(nextNode);
        nextNode.setPrev(prevNode);
        pointsCount--;
        return nodeToDelete;
    }
    
    public LinkedListTabulatedFunction(FunctionPoint[] points) {
        if (points.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        for (int i = 1; i < points.length; i++) {
            if (points[i].getX() < points[i - 1].getX()) {
                throw new IllegalArgumentException("Массив не упорядочен по координатам X");
            }
        }
        pointsCount = 0;
        head = new FunctionNode(null, null, null);
        head.setNext(head);
        head.setPrev(head);
        for (FunctionPoint point : points) {
            addNodeToTail(new FunctionPoint(point));
        }
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница больше или равна правой");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        this.pointsCount = 0;
        head = new FunctionNode(null, null, null);
        head.setNext(head);
        head.setPrev(head);
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            addNodeToTail(new FunctionPoint(leftX + step * i, 0));
        }
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница больше или равна правой");
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        this.pointsCount = 0;
        head = new FunctionNode(null, null, null);
        head.setNext(head);
        head.setPrev(head);
        double step = (rightX - leftX) / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            addNodeToTail(new FunctionPoint(leftX + step * i, values[i]));
        }
    }

    @Override
    public double getLeftDomainBorder() {
        if (pointsCount == 0) {
            return Double.NaN;
        }
        return head.getNext().getPoint().getX();
    }

    @Override
    public double getRightDomainBorder() {
        if (pointsCount == 0) {
            return Double.NaN;
        }
        return head.getPrev().getPoint().getX();
    }

    @Override
    public double getFunctionValue(double x) {
        if (pointsCount == 0) {
            return Double.NaN;
        }
        
        double leftBorder = getLeftDomainBorder();
        double rightBorder = getRightDomainBorder();
        
        if (x < leftBorder - EPSILON_DOUBLE || x > rightBorder + EPSILON_DOUBLE) {
            return Double.NaN;
        }

        if (Math.abs(x - leftBorder) < EPSILON_DOUBLE) {
            return head.getNext().getPoint().getY();
        }
        if (Math.abs(x - rightBorder) < EPSILON_DOUBLE) {
            return head.getPrev().getPoint().getY();
        }

        FunctionNode current = head.getNext();
        while (current != head) {
            FunctionNode next = current.getNext();
            if (next == head) break;
            
            double x1 = current.getPoint().getX();
            double x2 = next.getPoint().getX();
            
            if (x >= x1 - EPSILON_DOUBLE && x <= x2 + EPSILON_DOUBLE) {
                if (Math.abs(x - x1) < EPSILON_DOUBLE) {
                    return current.getPoint().getY();
                }
                if (Math.abs(x - x2) < EPSILON_DOUBLE) {
                    return next.getPoint().getY();
                }
                
                double y1 = current.getPoint().getY();
                double y2 = next.getPoint().getY();
                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
            current = next;
        }
        
        return Double.NaN;
    }

    @Override
    public int getPointsCount() {
        return pointsCount;
    }
    
    @Override
    public FunctionPoint getPoint(int index) {
        return new FunctionPoint(getNodeByIndex(index).getPoint());
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Задать точку с индексом " + index + " невозможно, так как размер массива " + this.pointsCount);
        }
        
        FunctionNode node = getNodeByIndex(index);
        double newX = point.getX();
        double currentX = node.getPoint().getX();
        
        if (Math.abs(newX - currentX) < EPSILON_DOUBLE) {
            node.getPoint().setY(point.getY());
            return;
        }
        
        if (index > 0) {
            double prevX = node.getPrev().getPoint().getX();
            if (newX <= prevX + EPSILON_DOUBLE) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + newX + ") должна быть больше предыдущей (" + prevX + ")");
            }
        }
        
        if (index < pointsCount - 1) {
            double nextX = node.getNext().getPoint().getX();
            if (newX >= nextX - EPSILON_DOUBLE) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + newX + ") должна быть меньше следующей (" + nextX + ")");
            }
        }
        
        // Устанавливаем новые координаты
        node.getPoint().setX(newX);
        node.getPoint().setY(point.getY());
    }

    @Override
    public double getPointX(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Получить координату X с индексом " + index + " невозможно, так как размер массива " + this.pointsCount);
        }
        return getNodeByIndex(index).getPoint().getX();
    }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Задать координату X с индексом " + index + " невозможно, так как размер массива" + this.pointsCount);
        }
        
        FunctionNode node = getNodeByIndex(index);
        double currentX = node.getPoint().getX();
        
        if (Math.abs(x - currentX) < EPSILON_DOUBLE) {
            return; 
        }
        
        if (index > 0) {
            double prevX = node.getPrev().getPoint().getX();
            if (x <= prevX + EPSILON_DOUBLE) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + x + ") должна быть больше предыдущей (" + prevX + ")");
            }
        }
        
        if (index < pointsCount - 1) {
            double nextX = node.getNext().getPoint().getX();
            if (x >= nextX - EPSILON_DOUBLE) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + x + ") должна быть меньше следующей (" + nextX + ")");
            }
        }
        
        node.getPoint().setX(x);
    }

    @Override
    public double getPointY(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Получить координату Y с индексом " + index + " невозможно, так как размер массива " + this.pointsCount);
        }
        return getNodeByIndex(index).getPoint().getY();
    }

    @Override
    public void setPointY(int index, double y) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Задать координату Y с индексом " + index + " невозможно, так как размер массива " + this.pointsCount);
        }
        getNodeByIndex(index).getPoint().setY(y);
    }
    
    @Override
    public void deletePoint(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException("Удалить точку с индексом " + index + " невозможно, так как размер массива " + this.pointsCount);
        } 
        if (pointsCount <= 2) {
            throw new IllegalStateException("Невозможно удалить точку: минимальное количество точек - 2");
        }
        deleteNodeByIndex(index);
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        int insertIndex = 0;
        FunctionNode current = head.getNext();
        
        while (current != head && point.getX() > current.getPoint().getX() + EPSILON_DOUBLE) {
            current = current.getNext();
            insertIndex++;
        }

        if (current != head && Math.abs(point.getX() - current.getPoint().getX()) < EPSILON_DOUBLE) {
            throw new InappropriateFunctionPointException(
                "Точка с координатой X = " + point.getX() + " уже существует");
        }

        addNodeByIndex(insertIndex, new FunctionPoint(point));
    }


    
@Override
    public Iterator<FunctionPoint> iterator() {
        return new Iterator<FunctionPoint>() {
            private FunctionNode currentNode = head.getNext();
            
            @Override
            public boolean hasNext() {
                return currentNode != head;
            }
            
            @Override
            public FunctionPoint next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Нет следующего элемента");
                }
                // Возвращаем копию точки для защиты инкапсуляции
                FunctionPoint point = new FunctionPoint(currentNode.getPoint());
                currentNode = currentNode.getNext();
                return point;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Удаление не поддерживается");
            }
        };
    }

    
    public static class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {
        @Override
        public TabulatedFunction createTabulatedFunction(FunctionPoint[] points) {
            return new LinkedListTabulatedFunction(points);
        }
        
        @Override
        public TabulatedFunction createTabulatedFunction(double leftX, double rightX, int pointsCount) {
            return new LinkedListTabulatedFunction(leftX, rightX, pointsCount);
        }
        
        @Override
        public TabulatedFunction createTabulatedFunction(double leftX, double rightX, double[] values) {
            return new LinkedListTabulatedFunction(leftX, rightX, values);
        }
    }

    @Override
    public String toString() {
        if (pointsCount == 0) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        FunctionNode current = head.getNext();
        while (current != head) {
            FunctionPoint point = current.getPoint();
            sb.append(String.format("(%.3f; %.3f)", point.getX(), point.getY()));
            
            current = current.getNext();
            if (current != head) {
                sb.append(", ");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (!(o instanceof TabulatedFunction)) {
            return false;
        }
        
        TabulatedFunction other = (TabulatedFunction) o;
        
        if (this.pointsCount != other.getPointsCount()) {
            return false;
        }
        
        if (o instanceof LinkedListTabulatedFunction) {
            // Оптимизированное сравнение для объектов того же класса
            LinkedListTabulatedFunction otherList = (LinkedListTabulatedFunction) o;
            
            FunctionNode current1 = this.head.getNext();
            FunctionNode current2 = otherList.head.getNext();
            
            while (current1 != this.head && current2 != otherList.head) {
                FunctionPoint p1 = current1.getPoint();
                FunctionPoint p2 = current2.getPoint();
                
                if (Math.abs(p1.getX() - p2.getX()) > EPSILON_DOUBLE ||
                    Math.abs(p1.getY() - p2.getY()) > EPSILON_DOUBLE) {
                    return false;
                }
                
                current1 = current1.getNext();
                current2 = current2.getNext();
            }
            return true;
        } else {
            // Общее сравнение для любых реализаций TabulatedFunction
            for (int i = 0; i < pointsCount; i++) {
                try {
                    FunctionPoint thisPoint = this.getPoint(i);
                    FunctionPoint otherPoint = other.getPoint(i);
                    
                    if (Math.abs(thisPoint.getX() - otherPoint.getX()) > EPSILON_DOUBLE ||
                        Math.abs(thisPoint.getY() - otherPoint.getY()) > EPSILON_DOUBLE) {
                        return false;
                    }
                } catch (FunctionPointIndexOutOfBoundsException e) {
                    return false; // Не должно происходить, но для надежности
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        int result = pointsCount; // Включаем количество точек для различия функций с разным числом точек
        
        FunctionNode current = head.getNext();
        while (current != head) {
            FunctionPoint point = current.getPoint();
            
            // Используем хэш-код точек, но преобразуем double к int для хэширования
            long xBits = Double.doubleToLongBits(point.getX());
            long yBits = Double.doubleToLongBits(point.getY());
            
            int xHash = (int)(xBits ^ (xBits >>> 32));
            int yHash = (int)(yBits ^ (yBits >>> 32));
            
            // Комбинируем хэши
            result = result ^ xHash ^ yHash;
            
            current = current.getNext();
        }
        
        return result;
    }

    @Override
    public TabulatedFunction clone() {
        FunctionPoint[] pointsArray = new FunctionPoint[pointsCount];
        FunctionNode current = head.getNext();
        for (int i = 0; i < pointsCount; i++) {
            pointsArray[i] = new FunctionPoint(current.getPoint());
            current = current.getNext();
        }
        return new LinkedListTabulatedFunction(pointsArray);
    }
}