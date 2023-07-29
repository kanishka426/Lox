package lox;
import java.util.Map;
import java.util.HashMap;

public class Environment {
    private final Environment parent;
    private final Map<String, Object> values = new HashMap<>();

    Environment(Environment parent) {
        this.parent = parent;
    }

    public void put(Token name, Object value) {
        values.put(name.lexeme, value); 
    }

    public Object get(Token name) {
        if(values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        } else {
            if(parent == null)
                throw new EnvironmentError().noNameError(name);
            else 
                return parent.get(name); 
        }
    }

    public void assign(Token name, Object value) {
        if(values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else {
            if(parent == null) 
                throw new EnvironmentError().noNameError(name);
            else 
                parent.assign(name, value); 
        }
    }

}
