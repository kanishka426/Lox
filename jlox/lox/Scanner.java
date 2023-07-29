package lox;


import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int line = 1;
    private int start = 0; 
    private int current = 0;
    private static final Map<String, TokenType> keywords;
    
    
    static {
        keywords = new HashMap<>(); 
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("elif",   TokenType.ELIF);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }




    Scanner (String source) { 
        this.source = source;
    }

    public List<Token> scanTokens() { 
        while(!isAtEnd()) {
            this.start = this.current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, this.line));

        return this.tokens;

    }

    
    private void scanToken() {
        char c = this.readNext();
        switch(c) {
            case '(' : addToken(TokenType.LEFT_PAREN); break;
            case ')' : addToken(TokenType.RIGHT_PAREN); break;
            case '{' : addToken(TokenType.LEFT_BRACE); break;
            case '}' : addToken(TokenType.RIGHT_BRACE); break;
            case ',' : addToken(TokenType.COMMA); break; 
            case '.' : addToken(TokenType.DOT); break;
            case '+' : addToken(TokenType.PLUS); break;
            case '-' : addToken(TokenType.MINUS); break;
            case '*' : addToken(TokenType.STAR); break;
            case '?' : addToken(TokenType.QUESTION); break;
            case ';' : addToken(TokenType.SEMICOLON); break;
            case ':' : addToken(TokenType.COLON); break;
            case ' ' : break;
            case '\r': break;
            case '\t': break;
            case '\n':
                this.line++;
                break;
            

            case '=' :
                if(match('=')) addToken(TokenType.DOUBLE_EQUALS); else addToken(TokenType.EQUAL);
                break;
            case '!' :
                if(match('=')) addToken(TokenType.BANG_EQUALS); else addToken(TokenType.BANG);
                break;    
            case '>' :
                if(match('=')) addToken(TokenType.GREATER_EQUALS); else addToken(TokenType.GREATER);
                break;
            case '<' :
                if(match('=')) addToken(TokenType.LESS_EQUALS); else addToken(TokenType.LESS); 
                break;
            
            case '/' : 
                if(match('/')) {
                    while(!isAtEnd() && (this.readNext() != '\n'));
                } else {
                    addToken(TokenType.SLASH); 
                }
                break;
            
            case '"' :
                addStringLiteral();
                break;
            
            default:
                if(isDigit(c)) {
                    addNumber();
                } else if (isIdentifier(c)) {
                    addIdentifier();
                } else {
                    Lox.error(this.line, "Unrecognized Character");
                    Lox.hadError = true;
                }
                break;
            
            
                
        }
        

    }


    // Source Accessors

    private char getCharAt(int i) {
        return this.source.charAt(i); 
    } 

    private char peek() {
        return !isAtEnd() ? this.source.charAt(this.current) : '\0';
    }


    private boolean match(char x) {
        if(!isAtEnd() && this.source.charAt(this.current) == x) {
            this.readNext();
            return true;
        } else {
            return false;
        }
    }

    private char readNext() {
        return this.source.charAt(this.current++);
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    // Token adders and helpers

    private void addStringLiteral() {
        while(!isAtEnd() && this.peek() != '"') {
            if(this.peek() == '\n')
                line++;
            this.readNext(); 
        }

        if(isAtEnd()) {
            Lox.error(this.line, "Unterminated String."); 
            return;
        } 
        
        String value = this.source.substring(start + 1, current); 
        this.readNext(); 
        this.addToken(TokenType.STRING, value); 
        return;
    }

    private void addNumber() {
        char first = this.getCharAt(this.start);
        boolean floatYet = false;
        double x = (double) (first - 48);
        int d = 10;
        while(true) {
            char c = this.peek();
            if(c <= 57 && c >= 48) {
                this.readNext();
                if (!floatYet) 
                    x = x*10 + (c - 48);
                else {
                    x += ((double) (c - 48)) / d;
                    d *= 10; 
                }
            } else if (c == '.' && !floatYet) {
                this.readNext();
                floatYet = true;
            } else {
                this.addToken(TokenType.NUMBER, x);
                break;
            }
        }
    }


    private boolean isDigit(char x) {
        return (x >= 48) && (x <= 57);
    }


    private void addIdentifier() {
        parseLexeme();
        TokenType type = getLexemeType();
        if(type == TokenType.TRUE || type == TokenType.FALSE)
            addToken(type, type == TokenType.TRUE); 
        else
            addToken(type);
    }

    private void parseLexeme() {
        
        while(!isAtEnd()) {
            char x = this.peek(); 
            if(isIdentifier(x) || isDigit(x)) {
                this.readNext(); 
            } else {
                break;
            }
        }

    }

    private TokenType getLexemeType() {
        if(keywords.containsKey(this.source.substring(this.start, this.current))) {
            return keywords.get(this.source.substring(this.start, this.current));
        } else {
            return TokenType.IDENTIFIER;
        }
    }


    private boolean isIdentifier(char x) {
        return ((x >= 65) && x <= (90)) || ((x >= 97) && (x <= 122)) || (x == 95);
    }



    private void addToken(TokenType type) {
        this.addToken(type, null); 
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = this.source.substring(start, current);
        this.tokens.add(new Token(type, lexeme, literal, this.line));
    }





}
