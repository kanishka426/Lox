package lox;
enum TokenType {
    // Single Symbols

    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, 
    COMMA, DOT, MINUS, PLUS, SEMICOLON, COLON, SLASH, STAR,
    QUESTION,
    
    // Double Symbols

    EQUAL, DOUBLE_EQUALS,
    BANG, BANG_EQUALS,
    GREATER, GREATER_EQUALS,
    LESS, LESS_EQUALS,

    // Literals

    IDENTIFIER, STRING, NUMBER,

    // Keywords

    VAR, AND, OR, IF, ELSE, ELIF, FOR, WHILE, TRUE, 
    FALSE, NIL, CLASS, THIS, SUPER, FUN, RETURN, PRINT,

    
    EOF

}