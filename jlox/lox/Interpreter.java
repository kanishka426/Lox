package lox;

public class Interpreter implements Visitor<Object> {

    public String interpret(Expression expression) {
        Object value = expression.accept(this);
        if(value == null) {
            return "nil";
        } else {
            return value.toString(); 
        }
    }


    @Override
    public Object visitGrouping(Grouping expr) {
        return expr.expr.accept(this); 
    }
    @Override
    public Object visitUnary(Unary expr) {
        if(expr.operator.type == TokenType.MINUS) {
             return - (double) expr.expr.accept(this); 
        } else {
            return !isTruth(expr.expr); 
        }
    }
    @Override
    public Object visitBinary(Binary expr) {
        Token operator = expr.operator;
        Object left_value = expr.left_expr.accept(this); 
        Object right_value = expr.right_expr.accept(this);
        switch(operator.type) {
            case COMMA:
                return right_value;
            case MINUS:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value - (Double) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            case PLUS:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value + (Double) right_value;
                } else if(left_value instanceof String && right_value instanceof String) {
                    return (String) left_value + (String) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            case STAR:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value * (Double) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            case SLASH:
                if(left_value instanceof Double && right_value instanceof Double) {
                    if((Double) right_value != 0) {
                        return (Double) left_value / (Double) right_value;
                    } else {
                        throw new BinaryError(operator, left_value, right_value).invalidArgument(); 
                    }
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            
            case BANG_EQUALS:
                if(!isEqual(left_value, right_value)) {
                    return true;
                } else {
                    return false;
                }
            case DOUBLE_EQUALS:
                if(isEqual(left_value, right_value)) {
                    return true;
                } else {
                    return false;
                }
                
            case GREATER:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value > (Double) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            case GREATER_EQUALS:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value >= (Double) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            case LESS:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value < (Double) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
                
            case LESS_EQUALS:
                if(left_value instanceof Double && right_value instanceof Double) {
                    return (Double) left_value <= (Double) right_value;
                } else {
                    throw new BinaryError(operator, left_value, right_value).typeError();
                }
            
        }
        return new Object(); 
    }

    @Override
    public Object visitTernary(Ternary expr) {
        if(isTruth(expr.first_expr)) {
            return expr.sec_expr.accept(this); 
        } else {
            return expr.third_expr.accept(this); 
        }
    }

    @Override
    public Object visitLiteral(Literal expr) {
        return expr.value;
    }

    private boolean isTruth(Expression expr) {
        Object result = expr.accept(this); 
        if(result == null) {
            return false;
        }
        if(result instanceof Boolean) {
            return (Boolean) result;
        }
        return true;
    }

    private boolean isEqual(Object v, Object u) {
        if(v == null && u == null) {
            return true;
        } else if (v == null || u == null) {
            return false;
        } else {
            return v.equals(u);
        }
    }

}


abstract class InterpreterError extends RuntimeException{
    abstract public InterpreterError typeError();
    abstract public InterpreterError invalidArgument(); 
}

class BinaryError extends InterpreterError {
    private Token operator;
    private Object left_value;
    private Object right_value;
    
    BinaryError(Token operator, Object left_value, Object right_value) {
        this.operator = operator;
        this.left_value = left_value;
        this.right_value = right_value;
    }

    @Override
    public InterpreterError typeError() {
        Lox.runtimeError(operator.line, "The binary operator: '" + operator.lexeme + "' can't combine the values of type " + left_value.getClass().getName() + " and " + right_value.getClass().getName() + ".");
        return this;
    }

    @Override
    public InterpreterError invalidArgument() {
        Lox.runtimeError(operator.line, "Binary operator: '" + operator.lexeme + "' can't operate on values " + left_value.toString() + ", " + right_value.toString());
        return this;
    }
}