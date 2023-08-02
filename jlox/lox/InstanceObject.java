package lox;

import java.util.Map;
import java.util.HashMap; 

public class InstanceObject {
    final ClassObject klass;
    final Map<String, Object> fields = new HashMap<>();

    InstanceObject(ClassObject klass) {
        this.klass = klass;
    }

    public Object getField(Token name) {
        if(fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme); 
        } 
        FunctionObject method = klass.findMethod(name.lexeme); 
        if(method != null) {
            return bindMethod(method); 
        } else {
            throw new InstanceError(this).fieldNotFound(name); 
        }
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value); 
    }


    private FunctionObject bindMethod(FunctionObject func) {
        return func.bind(this); 
    }


}



