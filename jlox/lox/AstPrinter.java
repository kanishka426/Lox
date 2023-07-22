package lox;

public class AstPrinter implements Visitor<String> { 
    

    public void print(Expression expr) {
        System.out.println(expr.accept(this)); 
    }

    @Override
    public String visitUnary(Unary expression) {
        return "(" + expression.operator.lexeme + " " + expression.expr.accept(this) + ")"; 
    }

    @Override
    public String visitGrouping(Grouping expression) {
        return "(group " + expression.expr.accept(this) + ")"; 
    }

    @Override
    public String visitBinary(Binary expression) {
        return "(" + expression.operator.lexeme + " " + expression.left_expr.accept(this) + " " + expression.right_expr.accept(this) + ")";  
    }

    @Override
    public String visitTernary(Ternary expression) {
        return "(" + expression.operator1_2.lexeme + " " + expression.first_expr.accept(this) + " (" + expression.operator2_3.lexeme + " " +
            expression.sec_expr.accept(this) + " " + expression.third_expr.accept(this) + "))";
    }

    @Override
    public String visitLiteral(Literal expression) {
        return expression.value.toString(); 
    } 


}
