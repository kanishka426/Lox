package lox;
import java.util.List; 
import java.util.ArrayList;

public class Parser {
    private int current = 0;
    private List<Token> tokens;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> program = new ArrayList<>();
        while(!isAtEnd()) {
            try {
                program.add(declaration());
            } catch(ParseError error) {
                continue;
            }
        }
        
        if(!isAtEnd()) {
            return null;
        }
        return program;
    }


    private LoxFunction parseFunction(FunctionType type) {
        if(match(TokenType.IDENTIFIER)) {
            Token name = previous(); 
            List<Token> parameters = new ArrayList<>();
            consume(TokenType.LEFT_PAREN, "Expected a '(' after function identifier."); 
            while(!match(TokenType.RIGHT_PAREN)) {
                Expression para = primary(); 
                if(para instanceof Variable) {
                    parameters.add(((Variable) para).name);
                } else {
                    error(previous(), "Parameters can only be identifiers and not other expressions.");
                }
                if(peek().type != TokenType.RIGHT_PAREN) {
                    consume(TokenType.COMMA, "Expected a ',' to separate the parameters.");
                }
            }
            consume(TokenType.LEFT_BRACE, "Expected a '{' token.");
            List<Statement> funCode = new ArrayList<>();
            while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                try {
                    funCode.add(declaration());
                } catch(ParseError error) {
                    continue;
                }
            }
            if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                throw error(previous(), "Expected a '}' token."); 
            }

            return new LoxFunction(name, parameters, funCode, type); 

        }
        throw error(previous(), "A function statement must have an identifier.");
    }

    private Statement declaration() {
        if(match(TokenType.VAR)) {
            if(match(TokenType.IDENTIFIER)) {
                Token name = previous(); 
                if(match(TokenType.EQUAL)) {
                    Expression expr = expression(); 
                    checkSemiColon();
                    return new Var(name, expr); 
                } else {
                    checkSemiColon();
                    return new Var(name, null); 
                }
            } else {
                throw error(previous(), "Expected Identifier for variable declaration.");    
            }
        } else if(match(TokenType.FUN)) {
            return parseFunction(FunctionType.FUNCTION);
        } else if(match(TokenType.CLASS)) {
            if(match(TokenType.IDENTIFIER)) {
                Token name = previous(); 
                consume(TokenType.LEFT_BRACE, "Expected a '{' token.");
                List<LoxFunction> methods = new ArrayList<>(); 
                while(!match(TokenType.RIGHT_BRACE)) {
                    methods.add(parseFunction(FunctionType.METHOD));
                }
                return new LoxClass(name, methods); 
            } else {
                throw error(previous(), "A class statement must have an identifier.");
            }
        }

        return block();
    }

    private Statement block() {
        if(match(TokenType.LEFT_BRACE)) {
            Token left_brace = previous();
            List<Statement> statements = new ArrayList<>();

            while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                try {
                    statements.add(declaration());
                } catch (ParseError error) {
                    continue;
                }
            } 
            if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                throw error(left_brace, "Unpaired brace found."); 
            }

            return new Block(statements); 
        }

        return statement();

    }


    private Statement statement() {

        if(match(TokenType.FOR)) {
            consume(TokenType.LEFT_PAREN, "Expected a '(' token.");

            Var init = forDeclaration();
            checkSemiColon();
            
            Expression forClause = null;
            if(peek().type != TokenType.SEMICOLON) {
                try {
                    forClause = expression();
                } catch(ParseError error) {
                    
                    throw error;
                } 
            }
            checkSemiColon();
            
            Expression forComp = null; 
            if(peek().type != TokenType.RIGHT_PAREN) forComp = expression(); 
            
            consume(TokenType.RIGHT_PAREN, "Expected a ')' token.");
            consume(TokenType.LEFT_BRACE, "Exprected a '{' token");
            List<Statement> forCode = new ArrayList<>(); 
            while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                try {
                    forCode.add(declaration());
                } catch(ParseError error) {
                    continue;
                }
            }
            if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                throw error(previous(), "Expected a '{' token."); 
            }   
            
            return new For(init, forClause, forComp, new Block(forCode));


        }

        if(match(TokenType.WHILE)) {
            consume(TokenType.LEFT_PAREN, "Expected a '(' token.");
            Expression whileClause = expression();
            consume(TokenType.RIGHT_PAREN, "Expected a ')' token."); 
            consume(TokenType.LEFT_BRACE, "Exprected a '{' token");
            List<Statement> whileCode = new ArrayList<>();
            while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                try{
                    whileCode.add(declaration()); 
                } catch(ParseError error) {
                    continue;
                }
            }

            if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                throw error(previous(), "Expected a '{' token."); 
            }   


            return new While(whileClause, new Block(whileCode));
        }
        
        
        if(match(TokenType.IF)) {
            boolean hasElif = false;
            boolean hasElse = false;
            consume(TokenType.LEFT_PAREN, "Expected a '(' token.");
            Expression ifClause = expression();
            consume(TokenType.RIGHT_PAREN, "Expected a ')' token");
            consume(TokenType.LEFT_BRACE, "Exprected a '{' token");
            List<Statement> ifCode = new ArrayList<>();
            while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                try{
                    if(match(TokenType.VAR, TokenType.FUN, TokenType.CLASS)) {
                        throw error(previous(), "Can't declare anything in an 'if' block.");
                    }
                    ifCode.add(block()); 
                } catch(ParseError error) {
                    continue;
                }
            } 

            if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                throw error(previous(), "Expected a '{' token."); 
            }   
            

            List< List<Statement> > elifCode = new ArrayList<>();
            List <Expression> elifClause = new ArrayList<>();    
            while(match(TokenType.ELIF)) {    
                hasElif = true;            
                consume(TokenType.LEFT_PAREN, "Expected a '(' token.");
                Expression clause = expression();
                consume(TokenType.RIGHT_PAREN, "Expected a ')' token");
                consume(TokenType.LEFT_BRACE, "Exprected a '{' token");
                List<Statement> code = new ArrayList<>(); 
                while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    try{
                        if(peek().type == TokenType.VAR) {
                            throw error(previous(), "Can't declare variables in an 'elif' block.");
                        }
                        code.add(block()); 
                    } catch(ParseError error) {
                        continue;
                    }
                }

                if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                    throw error(previous(), "Expected a '{' token."); 
                }   

                
                elifClause.add(clause);
                elifCode.add(code);
            }
            List<Statement> elseCode = new ArrayList<>();

            if(match(TokenType.ELSE)) {
                hasElse = true;
                consume(TokenType.LEFT_BRACE, "Exprected a '{' token"); 
                while(!match(TokenType.RIGHT_BRACE) && !isAtEnd()) {
                    try{
                        if(peek().type == TokenType.VAR) {
                            throw error(previous(), "Can't declare variables in an 'else' block.");
                        }
                        elseCode.add(block()); 
                    } catch(ParseError error) {
                        continue;
                    }
                }

                if(isAtEnd() && previous().type != TokenType.RIGHT_BRACE) {
                    throw error(previous(), "Expected a '{' token."); 
                }   


            }

            return new If(ifClause, ifCode, (hasElif) ? elifClause : null, (hasElif) ? elifCode : null, (hasElse) ? elseCode : null); 

        }

        if(match(TokenType.ELSE)) {
            throw error(previous(), "An 'else' block must be preceded by an 'if' block.");
        }

        if(match(TokenType.PRINT)) {
            Expression expr = expression(); 
            checkSemiColon();
            return new Print(expr);  
        }

        if(match(TokenType.RETURN)) {
            Expression expr = expression();
            checkSemiColon();
            return new Return(previous(), expr); 
        }

        Expression expr = expression();
        checkSemiColon();
        return new Expr(expr); 
    }

    private Var forDeclaration() {
        
        if(match(TokenType.VAR)) {
            if(match(TokenType.IDENTIFIER)) {
                Token name = previous();
                if(match(TokenType.EQUAL)) {
                    Expression value = expression(); 
                    return new Var(name, value); 
                } else {
                    throw error(previous(), "Can't declare a variable without initialization in a 'for' statement.");
                }
            } else {
                throw error(previous(), "The keyword 'var' must be followed by an identifier.");
            }
        } else if(peek().type == TokenType.SEMICOLON) {
            return null;
        }

        throw error(previous(), "'for' statement must begin with an initializing declaration.");
    }


    private Expression expression() {
        return comma();
    }

    private Expression comma() {
        Expression expr = assignment();

        if(match(TokenType.COMMA)) {
            Token operator = previous();
            Expression right_expr = comma();
            return new Binary(expr, operator, right_expr);  
        }
        return expr;
    }

    private Expression assignment() {
        Expression expr = set(); 
        if(match(TokenType.EQUAL)) {
            if(expr instanceof Variable) {
                Token name = ((Variable) expr).name; 
                Expression value = expression(); 
                return new Assign(name, value);
            } else if(expr instanceof This) {
                throw error(previous(), "Can't assign to the 'this' keyword."); 
            } else { 
                throw error(previous(), "Can't assign to an expression."); 
            }
        }
        return expr;
    }

    private Expression set() {
        Expression expr = ternary(); 
        if(expr instanceof Get) {
            if(match(TokenType.EQUAL)) {
                Get getExpr = (Get) expr;
                Expression value = expression(); 
                return new Set(getExpr.variable, getExpr.name, value);
            } else{
                return expr;
            }
        }
        return expr; 
    }

    private Expression ternary() {
        Expression expr = logic_or();
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

    private Expression logic_or() {
        Expression expr = logic_and(); 
        if(match(TokenType.OR)) {
            Token operator = previous();
            Expression rexpr = logic_or();
            return new Logical(expr, operator, rexpr);  
        }
        return expr;
    }

    private Expression logic_and() {
        Expression expr = equality(); 
        if(match(TokenType.AND)) {
            Token operator = previous();
            Expression rexpr = logic_and();
            return new Logical(expr, operator, rexpr); 
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
        Expression expr = callable();
        return expr;

    }

    private Expression callable() {
        Expression expr = get();
        if(expr instanceof Get || expr instanceof Variable) {
            while(match(TokenType.LEFT_PAREN)) {
                List<Expression> arguments = new ArrayList<>();
                while(!match(TokenType.RIGHT_PAREN)) {
                    arguments.add(ternary());
                    if(peek().type != TokenType.RIGHT_PAREN) {
                        consume(TokenType.COMMA, "Expected a ',' to separate the arguments.");
                    }
                }
                expr = new Callable(expr, previous(), arguments); 
            }
        }
        return expr;
    }


    private Expression get() {
        Expression expr = primary();
        while(match(TokenType.DOT)) {
            if(expr instanceof Variable || expr instanceof Get || expr instanceof This) {
                if(match(TokenType.IDENTIFIER)) {
                    expr = new Get(expr, previous()); 
                } 
            } else {
                throw error(previous(), "The dot accessor can only run on objects.");
            }
        }
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
        if(match(TokenType.IDENTIFIER)) { 
            return new Variable(previous());
        }

        if(match(TokenType.THIS)) {
            return new This(previous()); 
        }

        throw error(peek(), "Unidentified Token."); 

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

    private void addErrorInfo(String message) {

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

    void checkSemiColon() {
        consume(TokenType.SEMICOLON, "Expected ';' after statement.");
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