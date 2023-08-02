package lox;

import java.util.List;
import java.util.Map;

interface CallableEntity {
    public Object call(List<Object> arguments, Interpreter interpreter);
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
    public Object call(List<Object> arguments, Interpreter interpreter) {
        if(arguments.size() != parameters.size()) {
            throw new CallableError(parameters.get(0)).invalidNumberOfArguments(arguments.size(), parameters.size()); 
        }
        Environment funcEnv = new Environment(paren_env); 
        Environment prev = interpreter.env;
        interpreter.env = funcEnv;

        for(int i = 0; i < parameters.size(); i++) {
            funcEnv.put(parameters.get(i), arguments.get(i));
        }
        try { 
            interpreter.interpret(code);
        } catch(ReturnValue rv) {
            interpreter.env = prev;
            return rv.value;
        }
        interpreter.env = prev;
        return null;
    } 

    public FunctionObject bind(InstanceObject instance) {
        Environment env = new Environment(paren_env);
        env.put(new Token(TokenType.THIS, "this", null, -1), instance);
        return new FunctionObject(this.parameters, this.code, env);
    }
}


class ClassObject implements CallableEntity {
    final Token name;
    final Map<String, FunctionObject> methods;

    ClassObject(Token name, Map<String, FunctionObject> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public Object call(List<Object> arguments, Interpreter interpreter) {
        InstanceObject instance = new InstanceObject(this); 

        return instance;
    }


    public FunctionObject findMethod(String name) {
        if(methods.containsKey(name)) {
            return methods.get(name); 
        } else {
            return null;
        }
    }




}



class ReturnValue extends RuntimeException {
    public Object value;

    ReturnValue(Object value) {
        super(null, null, false, false); 
        this.value = value;
    }
}


