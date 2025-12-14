package functions;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class TabulatedFunctions {
    
    private static TabulatedFunctionFactory factory = new ArrayTabulatedFunction.ArrayTabulatedFunctionFactory();
    
    public static void setTabulatedFunctionFactory(TabulatedFunctionFactory factory) {
        TabulatedFunctions.factory = factory;
    }
    
    public static TabulatedFunction createTabulatedFunction(FunctionPoint[] points) {
        return factory.createTabulatedFunction(points);
    }
    
    public static TabulatedFunction createTabulatedFunction(double leftX, double rightX, int pointsCount) {
        return factory.createTabulatedFunction(leftX, rightX, pointsCount);
    }
    
    public static TabulatedFunction createTabulatedFunction(double leftX, double rightX, double[] values) {
        return factory.createTabulatedFunction(leftX, rightX, values);
    }
    
    public static TabulatedFunction createTabulatedFunction(Class<? extends TabulatedFunction> functionClass, 
                                                           FunctionPoint[] points) {
        try {
            Constructor<? extends TabulatedFunction> constructor = 
                functionClass.getConstructor(FunctionPoint[].class);
            
            return constructor.newInstance((Object) points);
        } catch (NoSuchMethodException | InstantiationException | 
                 IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Ошибка при создании объекта через рефлексию", e);
        }
    }
    
    public static TabulatedFunction createTabulatedFunction(Class<? extends TabulatedFunction> functionClass, 
                                                           double leftX, double rightX, int pointsCount) {
        try {
            Constructor<? extends TabulatedFunction> constructor = 
                functionClass.getConstructor(double.class, double.class, int.class);
            
            return constructor.newInstance(leftX, rightX, pointsCount);
        } catch (NoSuchMethodException | InstantiationException | 
                 IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Ошибка при создании объекта через рефлексию", e);
        }
    }
    
    public static TabulatedFunction createTabulatedFunction(Class<? extends TabulatedFunction> functionClass, 
                                                           double leftX, double rightX, double[] values) {
        try {
            Constructor<? extends TabulatedFunction> constructor = 
                functionClass.getConstructor(double.class, double.class, double[].class);
            
            return constructor.newInstance(leftX, rightX, values);
        } catch (NoSuchMethodException | InstantiationException | 
                 IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Ошибка при создании объекта через рефлексию", e);
        }
    }

    public static TabulatedFunction tabulate(Function function, double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница (" + leftX + ") должна быть меньше правой (" + rightX + ")");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1);
        
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            points[i] = new FunctionPoint(x, function.getFunctionValue(x));
        }
        
        return createTabulatedFunction(points);
    }
    
    public static TabulatedFunction tabulate(Class<? extends TabulatedFunction> functionClass, 
                                            Function function, double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница (" + leftX + ") должна быть меньше правой (" + rightX + ")");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }
        
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1);
        
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            points[i] = new FunctionPoint(x, function.getFunctionValue(x));
        }
        
        return createTabulatedFunction(functionClass, points);
    }

    public static TabulatedFunction inputTabulatedFunction(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in); 
        int pointCount = dis.readInt();
        FunctionPoint[] points = new FunctionPoint[pointCount];

        for (int i = 0; i < pointCount; i++) {
            points[i] = new FunctionPoint(dis.readDouble(), dis.readDouble());
        }
        
        return createTabulatedFunction(points);
    }

    public static TabulatedFunction readTabulatedFunction(Reader in) throws IOException {
        StreamTokenizer st = new StreamTokenizer(in);
        st.nextToken();
        int pointsCount = (int) st.nval;
        FunctionPoint[] points = new FunctionPoint[pointsCount];
        
        for (int i = 0; i < pointsCount; i++) {
            st.nextToken();
            double x = st.nval;
            st.nextToken();
            double y = st.nval;
            points[i] = new FunctionPoint(x, y);
        }

        return createTabulatedFunction(points);
    }

    public static void outputTabulatedFunction(TabulatedFunction function, OutputStream out) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(function.getPointsCount());
        for (int i = 0; i < function.getPointsCount(); ++i) {
            FunctionPoint point = function.getPoint(i);
            dataOut.writeDouble(point.getX());
            dataOut.writeDouble(point.getY());
        }
        dataOut.flush();
    }

    public static void writeTabulatedFunction(TabulatedFunction function, Writer out) throws IOException {
        BufferedWriter Writer = new BufferedWriter(out);
        int pointsCount = function.getPointsCount();
        Writer.write(" " + pointsCount);

        for (int i = 0; i < pointsCount; i++) {
            FunctionPoint point = function.getPoint(i);
            Writer.write("\n " + point.getX());
            Writer.write(" " + point.getY());
        }
        Writer.flush();
    }
}
