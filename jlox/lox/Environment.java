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

    public Object get(Token name, int scope) {
        if(scope == 0) 
            return values.get(name.lexeme); 
        else
            return parent.get(name, scope - 1);
    }

    public void assign(Token name, Object value, int scope) {
        if(scope == 0) {
            values.put(name.lexeme, value);
        } else {
            parent.assign(name, value, scope - 1);
        }
    }

}
