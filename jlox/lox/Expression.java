package lox;
import java.util.List;
abstract class Expression {
	abstract <T> T accept(Visitor<T> visitor);
	interface Visitor<T> { 
		T visitAssign(Assign expression);
		T visitGrouping(Grouping expression);
		T visitLogical(Logical expression);
		T visitUnary(Unary expression);
		T visitBinary(Binary expression);
		T visitTernary(Ternary expression);
		T visitLiteral(Literal expression);
		T visitVariable(Variable expression);
		T visitThis(This expression);
		T visitGet(Get expression);
		T visitSet(Set expression);
		T visitCallable(Callable expression);
		T visitSuper(Super expression);
	}
}

class Assign extends Expression {
	final Token name;
	final Expression value;

	Assign(Token name, Expression value) {
		this.name = name;
		this.value = value;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitAssign(this);
	}
}

class Grouping extends Expression {
	final Expression expr;

	Grouping(Expression expr) {
		this.expr = expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitGrouping(this);
	}
}

class Logical extends Expression {
	final Expression left_expr;
	final Token operator;
	final Expression right_expr;

	Logical(Expression left_expr, Token operator, Expression right_expr) {
		this.left_expr = left_expr;
		this.operator = operator;
		this.right_expr = right_expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitLogical(this);
	}
}

class Unary extends Expression {
	final Token operator;
	final Expression expr;

	Unary(Token operator, Expression expr) {
		this.operator = operator;
		this.expr = expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitUnary(this);
	}
}

class Binary extends Expression {
	final Expression left_expr;
	final Token operator;
	final Expression right_expr;

	Binary(Expression left_expr, Token operator, Expression right_expr) {
		this.left_expr = left_expr;
		this.operator = operator;
		this.right_expr = right_expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitBinary(this);
	}
}

class Ternary extends Expression {
	final Expression first_expr;
	final Token operator1_2;
	final Expression sec_expr;
	final Token operator2_3;
	final Expression third_expr;

	Ternary(Expression first_expr, Token operator1_2, Expression sec_expr, Token operator2_3, Expression third_expr) {
		this.first_expr = first_expr;
		this.operator1_2 = operator1_2;
		this.sec_expr = sec_expr;
		this.operator2_3 = operator2_3;
		this.third_expr = third_expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitTernary(this);
	}
}

class Literal extends Expression {
	final Object value;

	Literal(Object value) {
		this.value = value;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitLiteral(this);
	}
}

class Variable extends Expression {
	final Token name;

	Variable(Token name) {
		this.name = name;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitVariable(this);
	}
}

class This extends Expression {
	final Token dis;

	This(Token dis) {
		this.dis = dis;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitThis(this);
	}
}

class Get extends Expression {
	final Expression variable;
	final Token name;

	Get(Expression variable, Token name) {
		this.variable = variable;
		this.name = name;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitGet(this);
	}
}

class Set extends Expression {
	final Expression variable;
	final Token name;
	final Expression value;

	Set(Expression variable, Token name, Expression value) {
		this.variable = variable;
		this.name = name;
		this.value = value;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitSet(this);
	}
}

class Callable extends Expression {
	final Expression name;
	final Token paren;
	final List<Expression> arguments;

	Callable(Expression name, Token paren, List<Expression> arguments) {
		this.name = name;
		this.paren = paren;
		this.arguments = arguments;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitCallable(this);
	}
}

class Super extends Expression {
	final Token ssup;
	final Token name;

	Super(Token ssup, Token name) {
		this.ssup = ssup;
		this.name = name;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitSuper(this);
	}
}
