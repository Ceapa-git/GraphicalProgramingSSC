package org.ssc;

import org.ssc.gui.MainWindow;
import org.ssc.model.Block;
import org.ssc.model.math.ComputeException;
import org.ssc.model.math.Operator;
import org.ssc.model.variable.ChangeVariable;
import org.ssc.model.variable.PrintVariable;
import org.ssc.model.variable.SetVariable;
import org.ssc.model.variable.Variable;
import org.ssc.model.variable.type.VArray;
import org.ssc.model.variable.type.VChar;
import org.ssc.model.variable.type.VFloat;
import org.ssc.model.variable.type.VInt;

import java.util.ArrayList;
import java.util.HashMap;

public class Controller {
    private static HashMap<String, Variable<?>> variables;

    public static void main(String[] args) {
        variables = new HashMap<>();
        MainWindow mainWindow = new MainWindow();
        Block start = new Block();
        //generateExample(start);
        mainWindow.addRunActionListener(e -> run(start, mainWindow));
        mainWindow.addBlocks(start);
    }

    private static void generateExample(Block start) {
        Block current = start;

        VInt amount1 = new VInt();
        amount1.setValue(10);
        VInt amount2 = new VInt();
        amount2.setValue(20);
        VInt amount3 = new VInt();
        amount3.setValue(20);

        current.setNext(new SetVariable("a"));
        current = current.getNext();
        current.setConnection(amount1);

        current.setNext(new PrintVariable("a"));
        current = current.getNext();

        current.setNext(new SetVariable("a"));
        current = current.getNext();
        current.setConnection(amount2);

        current.setNext(new PrintVariable("a"));
        current = current.getNext();

        current.setNext(new ChangeVariable("a"));
        current = current.getNext();
        current.setConnection(amount3);

        current.setNext(new PrintVariable("a"));
        current = current.getNext();

        //----------------------------------------------------

        VFloat floatAmount1 = new VFloat();
        floatAmount1.setValue(1.234f);
        VFloat floatAmount2 = new VFloat();
        floatAmount2.setValue(5.678);

        current.setNext(new SetVariable("b"));
        current = current.getNext();
        current.setConnection(floatAmount1);

        current.setNext(new PrintVariable("b"));
        current = current.getNext();

        current.setNext(new ChangeVariable("b"));
        current = current.getNext();
        current.setConnection(floatAmount2);

        current.setNext(new PrintVariable("b"));
        current = current.getNext();

        //----------------------------------------------------

        VChar c = new VChar();
        c.setValue('c');
        VInt charAmount = new VInt();
        charAmount.setValue(1);

        current.setNext(new SetVariable("c"));
        current = current.getNext();
        current.setConnection(c);

        current.setNext(new PrintVariable("c"));
        current = current.getNext();

        current.setNext(new ChangeVariable("c"));
        current = current.getNext();
        current.setConnection(charAmount);

        current.setNext(new PrintVariable("c"));
        current = current.getNext();

        //----------------------------------------------------

        VArray<VChar> string = new VArray<>(new VChar());
        string.setValue(new ArrayList<VChar>());
        string.getValue().add(c);
        string.getValue().add(c);
        string.getValue().add(c);

        current.setNext(new SetVariable("d"));
        current = current.getNext();
        current.setConnection(string);

        current.setNext(new PrintVariable("d"));
        current = current.getNext();

        //----------------------------------------------------

        VArray<VInt> intArray = new VArray<>();
        intArray.setValue(new ArrayList<VInt>());
        intArray.getValue().add(amount1);
        intArray.getValue().add(amount2);
        intArray.getValue().add(amount3);

        current.setNext(new SetVariable("e"));
        current = current.getNext();
        current.setConnection(intArray);

        current.setNext(new PrintVariable("e"));
        current = current.getNext();

        //----------------------------------------------------

        VInt int1 = new VInt();
        int1.setValue(15);
        VInt int2 = new VInt();
        int2.setValue(21);

        current.setNext(new PrintVariable("a"));
        current = current.getNext();

        current.setNext(new SetVariable("a"));
        current = current.getNext();
        current.setConnection(new Operator(Operator.Operation.MUL));
        current.getConnection(0).setConnection(int1, 0);
        current.getConnection(0).setConnection(int2, 1);

        current.setNext(new PrintVariable("a"));
    }

    private static void run(Block start, MainWindow mainWindow) {
        variables.clear();
        Block current = start;
        mainWindow.addTextNL("Running");
        try {
            while (current != null) {
                switch (current.getBlockName()) {
                    case "SetVariable": {
                        SetVariable block = (SetVariable) current;
                        String name = block.getName();
                        Variable<?> value = compute(block.getConnection(0));
                        variables.put(name, value.cloneVariable());
                        break;
                    }
                    case "PrintVariable": {
                        PrintVariable block = (PrintVariable) current;
                        String name = block.getName();
                        if (variables.containsKey(name))
                            mainWindow.addTextNL(variables.get(name).getPrint());
                        else {
                            mainWindow.addTextNL("Error");
                            mainWindow.addTextNL(name + " does not exist");
                            return;
                        }
                        break;
                    }
                    case "ChangeVariable": {
                        ChangeVariable block = (ChangeVariable) current;
                        String name = block.getName();
                        Variable<?> value = compute(block.getConnection(0));
                        if (variables.containsKey(name)) {
                            if (variables.get(name).getType().equals(value.getType()))
                                variables.get(name).changeValue(value.getValue());
                            else {
                                mainWindow.addTextNL("Error");
                                mainWindow.addTextNL(name + " is not " + value.getType().getName());
                                return;
                            }
                        } else {
                            mainWindow.addTextNL("Error");
                            mainWindow.addTextNL(name + " does not exist");
                            return;
                        }
                        break;
                    }
                }
                current = current.getNext();
            }
        } catch (ComputeException e) {
            mainWindow.addTextNL("Error");
            mainWindow.addTextNL(e.getMessageString());
            return;
        }
        mainWindow.addTextNL("Finished Successfully");
    }

    private static Variable<?> compute(Block current) throws ComputeException {
        if (current == null) throw new ComputeException("No connection");
        if (current instanceof Variable<?>) return (Variable<?>) current;
        Variable<?> value1, value2;//todo
        // considerat daca nu trebuie in switch la operator
        Variable<?> result = new VInt();

        if (current.getConnection(0) == null) throw new ComputeException("No connections");
        if (current.getConnection(1) == null) throw new ComputeException("Not enough connections");

        if (!(current.getConnection(0) instanceof Variable<?>)) value1 = compute(current.getConnection(0));
        else value1 = (Variable<?>) current.getConnection(0);
        if (!(current.getConnection(1) instanceof Variable<?>)) value2 = compute(current.getConnection(1));
        else value2 = (Variable<?>) current.getConnection(1);//pana aici todoul

        switch (current.getBlockName()) {
            case "Operator": {
                switch (((Operator) current).getOperation()) {
                    case ADD: {
                        result = value1.add(value2);
                        break;
                    }
                    case SUB: {
                        result = value1.sub(value2);
                        break;
                    }
                    case MUL: {
                        result = value1.mul(value2);
                        break;
                    }
                    case DIV: {
                        result = value1.div(value2);
                        break;
                    }
                    case MOD: {
                        result = value1.mod(value2);
                        break;
                    }
                    case UNDEFINED: {
                    }
                }
            }
            default: {
                break;
            }
        }
        return result;
    }
}
