
import java.util.*;

public class logicalExpression {

    private String uniqueSymbol = null; // null if sentence is a more complex expression
    private String connective = null; // null if sentence is a _UNIQUE_ symbol
    private Vector<logicalExpression> subexpressions = null; // a vector of logicalExpressions ( basically a vector of
                                                             // unique
                                                             // symbols and subexpressions )

    public void setSubexpression(logicalExpression newSub) {
        this.subexpressions.add(newSub);
    }

    public void setSubexpressions(Vector<logicalExpression> symbols) {
        this.subexpressions = symbols;

    }

    public String getUniqueSymbol() {
        return this.uniqueSymbol;
    }

    public String getConnective() {
        return this.connective;
    }

    public logicalExpression getNextSubexpression() {
        return this.subexpressions.lastElement();
    }

    public Vector getSubexpressions() {
        return this.subexpressions;
    }

    public void setUniqueSymbol(String newSymbol) {
        boolean valid = true;

        // remove the leading whitespace
        newSymbol.trim();

        if (this.uniqueSymbol != null) {
            System.out.println("setUniqueSymbol(): - this LE already has a unique symbol!!!" + "\nswapping :->"
                    + this.uniqueSymbol + "<- for ->" + newSymbol + "<-\n");
        } else if (valid) {
            this.uniqueSymbol = newSymbol;
        }
    }

    public String setConnective(String inputString) {

        String connect;

        inputString.trim();

        if (inputString.startsWith("(")) {
            inputString = inputString.substring(inputString.indexOf('('), inputString.length());

            inputString.trim();
        }

        if (inputString.contains(" ")) {
            // remove the connective out of the string
            connect = inputString.substring(0, inputString.indexOf(" "));
            inputString = inputString.substring((connect.length() + 1), inputString.length());

        } else {
            // just set to get checked and empty the inputString
            // huh?
            connect = inputString;
            inputString = "";
        }

        // if connect is a proper connective
        if (connect.equalsIgnoreCase("if") || connect.equalsIgnoreCase("iff") || connect.equalsIgnoreCase("and")
                || connect.equalsIgnoreCase("or") || connect.equalsIgnoreCase("xor")
                || connect.equalsIgnoreCase("not")) {
            // ok, first word in the string is a valid connective

            // set the connective
            this.connective = connect;

            return inputString;

        } else {
            System.out.println("unexpected character!!! : invalid connective!! - setConnective():-" + inputString);
            logicalExpression.exit_function(0);
        }

        // invalid connective - no clue who it would get here
        System.out.println(" invalid connective! : setConnective:-" + inputString);
        return inputString;
    }

    public void print_expression(String separator) {

        if (this.uniqueSymbol != null) {
            System.out.print(this.uniqueSymbol.toUpperCase());
        } else {
            // else the symbol is a nested logical expression not a unique symbol
            logicalExpression nextExpression;

            // print the connective
            System.out.print("(" + this.connective.toUpperCase());

            // enumerate over the 'symbols' ( logicalExpression objects ) and print them
            for (Enumeration e = this.subexpressions.elements(); e.hasMoreElements();) {
                nextExpression = (logicalExpression) e.nextElement();

                System.out.print(" ");
                nextExpression.print_expression("");
                System.out.print(separator);
            }

            System.out.print(")");
        }
    }

    public boolean solve_expressions(HashMap<String, Boolean> model) {

        if (this.getUniqueSymbol() != null) {
            symbol_stack.push(this.getUniqueSymbol());
        } else {
            logicalExpression nextExpression;

            symbol_stack.push(this.getConnective());
            for (Enumeration e = this.subexpressions.elements(); e.hasMoreElements();) {
                nextExpression = (logicalExpression) e.nextElement();

                nextExpression.solve_expressions(model);
            }
            final_result = popUniqueSymbolsAndEvaluateResult(model);
        }
        return final_result;
    }

    private final String TRUE = "T";
    private final String FALSE = "F";

    public static Stack<String> symbol_stack = new Stack<String>();
    private static boolean final_result;

    // constructor
    public logicalExpression() {
        this.subexpressions = new Vector<logicalExpression>();
    }

    public logicalExpression(logicalExpression oldExpression) {

        if (oldExpression.getUniqueSymbol() == null) {
            this.uniqueSymbol = oldExpression.getUniqueSymbol();
        } else {
            // create a new logical expression from the one passed to it
            this.connective = oldExpression.getConnective();

            // now get all of the subExpressions
            // hint, enumerate over the subexpression vector of newExpression
            for (Enumeration e = oldExpression.getSubexpressions().elements(); e.hasMoreElements();) {
                logicalExpression nextExpression = (logicalExpression) e.nextElement();

                this.subexpressions.add(nextExpression);
            }
        }

    }

    private boolean popUniqueSymbolsAndEvaluateResult(HashMap<String, Boolean> model) {
        // TODO Auto-generated method stub

        ArrayList<String> uniqueSymbole = new ArrayList<String>();
        String symbol, connective;
        boolean result = false;

        do {
            symbol = symbol_stack.pop();
            uniqueSymbole.add(symbol);
        } while (!isConnective(symbol));

        uniqueSymbole.remove(symbol);
        connective = symbol;

        if (connective.equalsIgnoreCase("or")) { // can have more than two unique symbols
            result = false;
            while (!uniqueSymbole.isEmpty() && !result) {
                result = result || getValue(uniqueSymbole.remove(0), model);
            }
        } else if (connective.equalsIgnoreCase("and")) { // can have more than two unique symbols
            result = true;
            while (!uniqueSymbole.isEmpty() && result) {
                result = result && getValue(uniqueSymbole.remove(0), model);
            }
        } else if (connective.equalsIgnoreCase("not")) {
            result = true;
            result = !getValue(uniqueSymbole.remove(0), model);
        } else if (connective.equalsIgnoreCase("xor")) { // result = a'b + ab'
                                                         // can have more than two unique symbols
            result = false;
            int no_of_true_symbol = 0;
            while (!uniqueSymbole.isEmpty()) {
                if (getValue(uniqueSymbole.remove(0), model)) {
                    no_of_true_symbol++;
                }
            }
            if (no_of_true_symbol == 1) {
                result = true;
            }
        } else if (connective.equalsIgnoreCase("if")) { // required exactly two symbols
            result = true;
            if (uniqueSymbole.size() == 2) {
                if (getValue(uniqueSymbole.get(1), model) && !getValue(uniqueSymbole.get(0), model)) {
                    result = false;
                }
            }
        } else if (connective.equalsIgnoreCase("iff")) { // result = a'b' + ab
                                                         // required exactly two symbols
            result = false;
            if (uniqueSymbole.size() == 2) {
                boolean symbol1 = getValue(uniqueSymbole.get(1), model);
                boolean symbol2 = getValue(uniqueSymbole.get(0), model);
                if ((!symbol1 && !symbol2) || (symbol1 && symbol2)) {
                    result = true;
                }
            }
        } else {
            System.out.println("Oops..incorrect connective!!");
        }

        if (result) { // push evaluated result again on top of stack for further use
            symbol_stack.push(TRUE);
        } else {
            symbol_stack.push(FALSE);
        }

        return result;
    }

    public static void clearStack() {
        if (symbol_stack != null) {
            symbol_stack.clear();
        }
    }

    private static void exit_function(int value) {
        System.out.println("exiting from logicalExpression");
        System.exit(value);
    }

    private boolean isConnective(String symbol) {
        // TODO Auto-generated method stub
        return (symbol.equalsIgnoreCase("if") || symbol.equalsIgnoreCase("iff") || symbol.equalsIgnoreCase("and")
                || symbol.equalsIgnoreCase("or") || symbol.equalsIgnoreCase("xor") || symbol.equalsIgnoreCase("not"));
    }

    private boolean getValue(String symbol, HashMap<String, Boolean> model) {
        if (symbol.equalsIgnoreCase(TRUE)) {
            return true;
        } else if (symbol.equalsIgnoreCase(FALSE)) {
            return false;
        } else if (model.get(symbol) == null) {
            return checkTrueFalse.getValueFromArray(symbol);
        } else {
            return model.get(symbol);
        }
    }

}