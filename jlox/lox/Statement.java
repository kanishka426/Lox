package lox;
import java.util.List;
abstract class Statement {
	abstract <T> T accept(Visitor<T> visitor);
	interface Visitor<T> { 
		T visitPrint(Print expression);
		T visitReturn(Return expression);
		T visitExpr(Expr expression);
		T visitVar(Var expression);
		T visitBlock(Block expression);
		T visitIf(If expression);
		T visitWhile(While expression);
		T visitFor(For expression);
		T visitLoxFunction(LoxFunction expression);
	}
}

class Print extends Statement {
	final Expression expr;

	Print(Expression expr) {
		this.expr = expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitPrint(this);
	}
}

class Return extends Statement {
	final Expression expr;

	Return(Expression expr) {
		this.expr = expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitReturn(this);
	}
}

class Expr extends Statement {
	final Expression expr;

	Expr(Expression expr) {
		this.expr = expr;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitExpr(this);
	}
}

class Var extends Statement {
	final Token name;
	final Expression value;

	Var(Token name, Expression value) {
		this.name = name;
		this.value = value;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitVar(this);
	}
}

class Block extends Statement {
	final List<Statement> statements;

	Block(List<Statement> statements) {
		this.statements = statements;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitBlock(this);
	}
}

class If extends Statement {
	final Expression ifClause;
	final List<Statement> ifCode;
	final List<Expression> elifClause;
	final List<List<Statement>> elifCode;
	final List<Statement> elseCode;

	If(Expression ifClause, List<Statement> ifCode, List<Expression> elifClause, List<List<Statement>> elifCode, List<Statement> elseCode) {
		this.ifClause = ifClause;
		this.ifCode = ifCode;
		this.elifClause = elifClause;
		this.elifCode = elifCode;
		this.elseCode = elseCode;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitIf(this);
	}
}

class While extends Statement {
	final Expression whileClause;
	final List<Statement> whileCode;

	While(Expression whileClause, List<Statement> whileCode) {
		this.whileClause = whileClause;
		this.whileCode = whileCode;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitWhile(this);
	}
}

class For extends Statement {
	final Var init;
	final Expression forClause;
	final Expression forComp;
	final List<Statement> forCode;

	For(Var init, Expression forClause, Expression forComp, List<Statement> forCode) {
		this.init = init;
		this.forClause = forClause;
		this.forComp = forComp;
		this.forCode = forCode;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitFor(this);
	}
}

class LoxFunction extends Statement {
	final Token name;
	final List<Token> parameters;
	final List<Statement> funCode;

	LoxFunction(Token name, List<Token> parameters, List<Statement> funCode) {
		this.name = name;
		this.parameters = parameters;
		this.funCode = funCode;
	}
	@Override
	<T> T accept(Visitor<T> visitor) {
		return visitor.visitLoxFunction(this);
	}
}
