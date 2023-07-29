package lox;
import java.util.List;

interface CallableEntity {
    public Object call(List<Object> arguments);
}

class FunctionObject implements CallableEntity {
    final List<Token> parameters;
    final List<Statement> code;
    final Environment paren_env;

    FunctionObject(List<Token> parameters, List<Statement> code, Environment paren_env) {
        this.parameters = parameters; 
        this.code = code;
        this.paren_env = paren_env;
    }

    @Override
    public Object call(List<Object> arguments) {
        Interpreter functionInterpreter = new Interpreter(paren_env);
        
        try { 
            functionInterpreter.interpret(code);
        } catch(ReturnValue rv) {
            return rv.value;
        }
        return null;
    } 
}



class ReturnValue extends RuntimeException {
    public Object value;

    ReturnValue(Object value) {
        this.value = value;
    }
}


