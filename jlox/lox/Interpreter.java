package lox;
import java.util.List;
import java.util.ArrayList;


public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private Environment env;

    Interpreter() {
        env = new Environment(null);
    } 
    Interpreter(Environment parent) {
        env = new Environment(parent); 
    }

    String stringify(Object value) {
        if(value == null) {
            return "nil";
        } else {
            return value.toString(); 
        }
    }

    public void interpret(List<Statement> program) {
        for(Statement statement: program) {
            execute(statement);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

 

    @Override
    public Void visitLoxFunction(LoxFunction statement) {
        Object funcObject = new FunctionObject(statement.parameters, statement.funCode, env); 
        env.put(statement.name, funcObject); 
        return null;
    }

    @Override 
    public Void visitBlock(Block statement) {
        Environment nenv = new Environment(env); 
        Environment prev = env;
        env = nenv;
        for(Statement stmt: statement.statements) {
            execute(stmt);
        }
        env = prev;

        return null;
    }

    @Override
    public Void visitReturn(Return statement) {
        Object value = statement.expr.accept(this);
        throw new ReturnValue(value);
    }

    @Override
    public Void visitPrint(Print statement) {
        Object value = statement.expr.accept(this);
        System.out.println(stringify(value));
        return null;
    }

    @Override 
    public Void visitExpr(Expr statement) {
        statement.expr.accept(this);
        return null; 
    }

    @Override
    public Void visitVar(Var statement) {
        Token name = statement.name;
        Object value = null; 
        if(statement.value != null) value = statement.value.accept(this); 
        env.put(name, value);
        return null;
    }

    @Override
    public Void visitIf(If statement) {
        if(isTruth(statement.ifClause)) {
            for(Statement stmt: statement.ifCode) {
                execute(stmt); 
            } 
            return null;
        }

        for(int i = 0; i < statement.elifClause.size(); i++) {
            if(isTruth(statement.elifClause.get(i))) {
                for(Statement stmt: statement.elifCode.get(i)) {
                    execute(stmt); 
                }
                return null;
            }
        }

        if(statement.elseCode != null) {
            for(Statement stmt: statement.elseCode) {
                execute(stmt); 
            }
        }


        return null;
    }

    @Override
    public Void visitWhile(While statement) {
        Environment nenv = new Environment(env); 
        Environment prev = env;
        env = nenv;
        while(isTruth(statement.whileClause)) {
            for(Statement stmt: statement.whileCode) {
                execute(stmt); 
            }
        }
        env = prev;
        return null;

    }   


    @Override 
    public Void visitFor(For statement) {
        Environment nenv = new Environment(env);
        Environment prev = env;
        env = nenv;
        if(statement.init != null) execute(statement.init); 

        while(isTruth(statement.forClause) || statement.forClause == null) {
            for(Statement stmt: statement.forCode) {
                execute(stmt);
            }
            if(statement.forComp != null) statement.forComp.accept(this);
        }
        env = prev; 
        return null; 
    }



    @Override
    public Object visitAssign(Assign expr) {
        Object value = expr.value.accept(this); 
        env.assign(expr.name, value);
        return value;
    }


    @Override
    public Object visitGrouping(Grouping expr) {
        return expr.expr.accept(this); 
    }

    @Override
    public Object visitLogical(Logical expr) {
        if(isTruth(expr.left_expr)) {
            if(expr.operator.type == TokenType.OR) {
                return true;
            } else {
                if(isTruth(expr.right_expr)) {
                    return true; 
                } else {
                    return false;
                }
            }
        } else {
            if(expr.operator.type == TokenType.OR) {
                if(isTruth(expr.right_expr)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
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

    @Override
    public Object visitVariable(Variable expr) {
        return env.get(expr.name); 
    }


    @Override 
    public Object visitCallable(Callable expr) {
        Object callableValue = expr.name.accept(this); 
        if(callableValue instanceof CallableEntity) {
            CallableEntity callableObject = (CallableEntity) callableValue;
            List<Object> arguments = new ArrayList<>();
            for(Expression e: expr.arguments) {
                arguments.add(e.accept(this));
            }
            return callableObject.call(arguments); 
        } else {
            throw new CallableError(callableValue, expr.paren);
        }
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
    
    public InterpreterError typeError() {
        Lox.runtimeError(operator.line, "The binary operator: '" + operator.lexeme + "' can't combine the values of type " + left_value.getClass().getName() + " and " + right_value.getClass().getName() + ".");
        return this;
    }

    public InterpreterError invalidArgument() {
        Lox.runtimeError(operator.line, "Binary operator: '" + operator.lexeme + "' can't operate on values " + left_value.toString() + ", " + right_value.toString());
        return this;
    }
}

class CallableError extends InterpreterError {
    private Object value;
    private Token location;
    CallableError(Object value, Token location) {
        this.value = value;
        this.location = location;
    }
    public InterpreterError invalidCallable() {
        Lox.runtimeError(location.line, "The object of type " + value.getClass().getName() + " cannot be called.");
        return this;
    }
}

class EnvironmentError extends InterpreterError {
    public InterpreterError noNameError(Token name) {
        Lox.runtimeError(name.line, "'" + name.lexeme + "'" + " is not defined in the environment.");
        return this;
    }
}