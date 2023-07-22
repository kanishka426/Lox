package lox;
import java.util.List; 

public class Parser {
    private int current = 0;
    private List<Token> tokens;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        Expression expr_tree;
        
        try{
            expr_tree = expression();
        } catch (ParseError error) {
            return null;
        }
        
        if(!isAtEnd()) {
            return null;
        }
        return expr_tree;
    }


    private Expression expression() {
        return comma(); 
    }

    private Expression comma() {
        Expression expr = ternary();

        if(match(TokenType.COMMA)) {
            Token operator = previous();
            Expression right_expr = comma();
            return new Binary(expr, operator, right_expr);  
        }
        return expr;
    }

    private Expression ternary() {
        Expression expr = equality();
        if(match(TokenType.QUESTION)) {
            Token operator1_2 = previous(); 
            Expression sec_expr = ternary();
            if(match(TokenType.COLON)) {
                Token operator2_3 = previous(); 
                Expression third_expr = ternary(); 
                return new Ternary(expr, operator1_2, sec_expr, operator2_3, third_expr); 
            } 
            throw error(previous(), "Expected ':' as the second operator of the ternary operator '?:'");
        }
        return expr;
    }

    private Expression equality() {
        Expression expr = comparison(); 

        if(match(TokenType.DOUBLE_EQUALS, TokenType.BANG_EQUALS)) {
            Token operator = previous(); 
            Expression right_expr = comparison(); 
            return new Binary(expr, operator, right_expr);
        }
        return expr;
        
    }

    private Expression comparison() {
        Expression expr = term(); 

        if(match(TokenType.GREATER_EQUALS, TokenType.GREATER, TokenType.LESS_EQUALS, TokenType.LESS)) {
            Token operator = previous(); 
            Expression right_expr = term();
            return new Binary(expr, operator, right_expr); 
        }
        return expr;
    }

    private Expression term() {
        Expression expr = factor(); 

        if(match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous(); 
            Expression right_expr = term(); 
            return new Binary(expr, operator, right_expr); 
        }

        return expr;

    }


    private Expression factor() {
        Expression expr = unary(); 
        if(match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous(); 
            Expression right_expr = factor(); 
            return new Binary(expr, operator, right_expr);
        }
        return expr;
    }

    private Expression unary() {
        if(match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous(); 
            Expression expr = primary(); 
            return new Unary(operator, expr); 
        }
        Expression expr = primary();
        return expr;

    }

    private Expression primary() {
        
        if(match(TokenType.NUMBER, TokenType.STRING, TokenType.FALSE, TokenType.TRUE, TokenType.NIL)) {
            Token literal = previous();
            return new Literal(literal.literal); 
        }
        if(match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')'");

            return new Grouping(expr); 
        }

        error(peek(), "Unidentified Token."); 
        return null; 
    }



    private void consume(TokenType type, String message) {
        if(peek().type == type) {
            getNext(); 
            return;
        }
        throw error(peek(), message); 
    }


    private ParseError error(Token token, String message) {
        Lox.error(token.line, message); 
        synchronize();
        return new ParseError();
    }


    private void synchronize() {
        getNext(); 
        while(!isAtEnd()) {
            TokenType type = peek().type;
            if(type == TokenType.SEMICOLON) {
                getNext(); 
                return;
            }
            switch(type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            getNext(); 
        }
    }


    private boolean match(TokenType... tokens) {
        Token peeked = peek(); 
        for(TokenType token: tokens) {
            if(peeked.type == token) {
                getNext(); 
                return true;
            }
        }
        return false;
    }


    private Token previous() {
        return tokens.get(current - 1); 
    }



    private Token peek() {
        return tokens.get(current); 
    }

    private Token getNext() {
        if(isAtEnd()) {
            return new Token(TokenType.EOF, "", null, -1); 
        }
        return tokens.get(current++); 
    }

    private boolean isAtEnd() {
        return tokens.get(current).type == TokenType.EOF; 
    }



}


class ParseError extends RuntimeException {

}