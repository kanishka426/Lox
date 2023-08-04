package lox;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Stack;




public class Resolver implements Statement.Visitor<Void>, Expression.Visitor<Void>{
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean> > scopes = new Stack<>(); 
    private final Stack<FunctionType> inFunction = new Stack<>();


    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        beginScope(); 
        inFunction.add(FunctionType.NONE);
    }

    public void performResolution(List<Statement> program) {
        for(Statement statement: program) {
            try{
                resolve(statement); 
            } catch(ResolverError error) {
                continue; 
            }
        }
    }


    private Void resolve(Statement statement) {
        return statement.accept(this);
    }
    private Void resolve(Expression expr) {
        if(expr == null) {
            return null;
        }
        return expr.accept(this); 
    }




    public Void visitBlock(Block statement) {
        beginScope();
        performResolution(statement.statements);
        endScope(); 
        return null;
    }

    private void beginScope() {
        scopes.add(new HashMap<>()); 
    }

    private void endScope() {
        scopes.pop(); 
    }

    public Void visitVar(Var statement) {
        if(scopes.size() > 1) {
            if(scopes.peek().containsKey(statement.name.lexeme)) {
                throw new ResolverError().containsDeclaration(statement.name);
            }
        } 
        scopes.peek().put(statement.name.lexeme, false); 
        resolve(statement.value);
        scopes.peek().put(statement.name.lexeme, true); 
        return null;
    }

    public Void visitVariable(Variable expr) {
        if(scopes.peek().containsKey(expr.name.lexeme) && scopes.peek().get(expr.name.lexeme)  == (Boolean) false) {
            throw new ResolverError().declarationUsage(expr.name); 
        } 
        int scope = findBinding(expr.name); 
        if(scope == -1) {
            throw new ResolverError().undeclaredVariableUsed(expr.name); 
        } else {
            interpreter.resolve(expr, scope); 
        }
        return null;
    }

    public Void visitThis(This expr) {
        int scope = findBinding(expr.dis);
        if(scope == -1) {
            throw new ResolverError().undeclaredVariableUsed(expr.dis); 
        } else {
            interpreter.resolve(expr, scope); 
        }
        return null;
    }




    private int findBinding(Token name) {
        for(int i = scopes.size() - 1; i >= 0; i--) {
            if(scopes.get(i).containsKey(name.lexeme)) {
                return (scopes.size() - 1 - i); 
            }
        }
        throw new ResolverError().undeclaredVariableUsed(name);  
    }

    public Void visitLoxFunction(LoxFunction statement) {
        if(statement.type == FunctionType.FUNCTION) {
            inFunction.add(FunctionType.FUNCTION);
            scopes.peek().put(statement.name.lexeme, true);
        }
        beginScope(); 
        if(statement.type == FunctionType.METHOD || statement.type == FunctionType.INITIALIZER) {
            inFunction.add(statement.type); 
            scopes.peek().put("this", true); 
            beginScope();
        }
        for(int i = 0; i < statement.parameters.size(); i++) {
            scopes.peek().put(statement.parameters.get(i).lexeme, true); 
        } 
        performResolution(statement.funCode);
        
        endScope(); 
        inFunction.pop(); 
        if(statement.type == FunctionType.METHOD || statement.type == FunctionType.INITIALIZER) {
            endScope(); 
        }
        return null;
    }



    public Void visitGet(Get expr) {
        resolve(expr.variable); 
        return null;
    }

    public Void visitSet(Set expr) {
        resolve(expr.variable); 
        resolve(expr.value); 
        return null;
    }



    public Void visitSuper(Super expr) {
        int scope = findBinding(expr.ssup);
        interpreter.resolve(expr, scope);
        return null; 
    }


    public Void visitLoxClass(LoxClass statement) {
        scopes.peek().put(statement.name.lexeme, true); 
        if(statement.parentClass != null) {
            int scope = findBinding(statement.parentClass.name); 
            interpreter.resolve(statement.parentClass, scope);
            beginScope();
            scopes.peek().put("super", true);  
        }   
        for(LoxFunction func: statement.methods) {
            resolve(func); 
        }
        if(statement.parentClass != null) {
            endScope(); 
        }
        return null;
    }


    public Void visitCallable(Callable expr) {
        resolve(expr.name); 
        for(int i = 0; i < expr.arguments.size(); i++) {
            resolve(expr.arguments.get(i)); 
        } 
        return null;
    }


    public Void visitLiteral(Literal expr) {
        return null;
    }

    public Void visitTernary(Ternary expr) {
        resolve(expr.first_expr);
        resolve(expr.sec_expr);
        resolve(expr.third_expr); 
        return null;
    }

    public Void visitUnary(Unary expr) {
        resolve(expr.expr); 
        return null;
    }

    public Void visitBinary(Binary expr) {
        resolve(expr.left_expr);
        resolve(expr.right_expr);
        return null;
    }

    public Void visitLogical(Logical expr) {
        resolve(expr.left_expr);
        resolve(expr.right_expr);
        return null;
    }

    public Void visitGrouping(Grouping expr) {
        resolve(expr.expr);
        return null;
    }

    public Void visitAssign(Assign expr) {
        int scope = findBinding(expr.name); 
        interpreter.resolve(expr, scope); 
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitPrint(Print expression) {
        resolve(expression.expr);
        return null;
    }

    @Override
    public Void visitReturn(Return expression) {
        if(inFunction.peek() == FunctionType.NONE) {
            throw new ResolverError().returnFromGlobalFrame(expression.name); 
        } else if(inFunction.peek() == FunctionType.INITIALIZER) {
            if(!(expression.expr instanceof This)) {
                throw new ResolverError().returnFromInit(expression.name); 
            }
        }
        if(expression.expr != null) resolve(expression.expr);
        return null;
    }

    @Override
    public Void visitExpr(Expr expression) {
        resolve(expression.expr); 
        return null;
    }

    @Override
    public Void visitIf(If expression) {
        resolve(expression.ifClause);
        performResolution(expression.ifCode);
        if(expression.elifClause != null) {
            for(Expression expr: expression.elifClause) {
                resolve(expr); 
            }
            for(List<Statement> elifCode: expression.elifCode) {
                performResolution(elifCode);
            }
        }
        
        if(expression.elseCode != null) {
            performResolution(expression.elseCode);
        }
        return null;
    }

    @Override
    public Void visitWhile(While expression) {
        resolve(expression.whileClause);
        resolve(expression.whileCode);
        return null;
    }

    @Override
    public Void visitFor(For expression) {
        beginScope();
        resolve(expression.init);
        resolve(expression.forClause); 
        resolve(expression.forComp);
        resolve(expression.forCode);
        endScope();
        return null; 
    }

}

class ResolverError extends RuntimeException {


    public ResolverError declarationUsage(Token name) {
        Lox.error(name.line, "Can't use a variable as its own initializer while declaration.");
        return this;
    }

    public ResolverError returnFromGlobalFrame(Token name) {
        Lox.error(name.line, "Attempting to return from global frame.");
        return this;
    }

    public ResolverError undeclaredVariableUsed(Token name) {
        Lox.error(name.line, "Undeclared variable used.");
        return this;
    }
    public ResolverError containsDeclaration(Token name) {
        Lox.error(name.line, "Cannot redeclare variables in frames that are not global.");
        return this;
    }

    public ResolverError returnFromInit(Token name) {
        Lox.error(name.line, "Can't return an object that is not the instance that called the 'init' method."); 
        return this; 
    }
    
}

