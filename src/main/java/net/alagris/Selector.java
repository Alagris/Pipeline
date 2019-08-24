package net.alagris;

import java.util.BitSet;
import java.util.Objects;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import net.alagris.parser.SelectorBaseVisitor;
import net.alagris.parser.SelectorLexer;
import net.alagris.parser.SelectorParser;
import net.alagris.parser.SelectorParser.AliasContext;
import net.alagris.parser.SelectorParser.ExprEqContext;
import net.alagris.parser.SelectorParser.ExprGeContext;
import net.alagris.parser.SelectorParser.ExprGtContext;
import net.alagris.parser.SelectorParser.ExprLeContext;
import net.alagris.parser.SelectorParser.ExprLtContext;
import net.alagris.parser.SelectorParser.IdContext;
import net.alagris.parser.SelectorParser.NameContext;
import net.alagris.parser.SelectorParser.OpAndContext;
import net.alagris.parser.SelectorParser.OpBracketContext;
import net.alagris.parser.SelectorParser.OpContext;
import net.alagris.parser.SelectorParser.OpExprContext;
import net.alagris.parser.SelectorParser.OpNotContext;
import net.alagris.parser.SelectorParser.OpOrContext;
import net.alagris.parser.SelectorParser.RefAbsContext;
import net.alagris.parser.SelectorParser.RefExactContext;
import net.alagris.parser.SelectorParser.RefMinusContext;
import net.alagris.parser.SelectorParser.RefPlusContext;
import net.alagris.parser.SelectorParser.ShiftBracketContext;
import net.alagris.parser.SelectorParser.ShiftExactContext;
import net.alagris.parser.SelectorParser.ShiftExactNegContext;
import net.alagris.parser.SelectorParser.ShiftMinusContext;
import net.alagris.parser.SelectorParser.ShiftPlusContext;
import net.alagris.parser.SelectorParser.StartContext;

public class Selector {

	private interface Op {

		public boolean match(Node node);

	}

	private static class OpNot implements Op {
		Op negated;

		public OpNot(Op negated) {
			this.negated = negated;
		}

		@Override
		public String toString() {
			return "!" + subOpStr(negated);
		}

		@Override
		public boolean match(Node node) {
			return !negated.match(node);
		}
	}

	private static String subOpStr(Op op) {
		if (op instanceof OpOr || op instanceof OpAnd || op instanceof OpNot) {
			return "(" + op.toString() + ")";
		}
		return op.toString();
	}

	private static class OpOr implements Op {
		Op left, right;

		public OpOr(Op left, Op right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return subOpStr(left) + "||" + subOpStr(right);
		}

		@Override
		public boolean match(Node node) {
			return left.match(node) || right.match(node);
		}
	}

	private static class OpAnd implements Op {
		Op left, right;

		public OpAnd(Op left, Op right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return subOpStr(left) + "&&" + subOpStr(right);
		}

		@Override
		public boolean match(Node node) {
			return left.match(node) && right.match(node);
		}
	}

	private static class OpLt implements Op {
		OpRef ref;

		public OpLt(OpRef ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			return ref.toString() + "<*";
		}

		@Override
		public boolean match(Node node) {
			Boolean found = node.forEachToTheLeft(n -> ref.match(n) ? Boolean.TRUE : null);
			return found == Boolean.TRUE;
		}
	}

	private static class OpGt implements Op {
		OpRef ref;

		public OpGt(OpRef ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			return ref.toString() + ">*";
		}

		@Override
		public boolean match(Node node) {
			Boolean found = node.forEachToTheRight(n -> ref.match(n) ? Boolean.TRUE : null);
			return found == Boolean.TRUE;
		}
	}

	private static class OpGe implements Op {
		OpRef ref;

		public OpGe(OpRef ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			return ref.toString() + ">=*";
		}

		@Override
		public boolean match(Node node) {
			if (ref.match(node))
				return true;
			Boolean found = node.forEachToTheRight(n -> ref.match(n) ? Boolean.TRUE : null);
			return found == Boolean.TRUE;
		}
	}

	private static class OpLe implements Op {
		OpRef ref;

		public OpLe(OpRef ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			return ref.toString() + "<=*";
		}

		@Override
		public boolean match(Node node) {
			if (ref.match(node))
				return true;
			Boolean found = node.forEachToTheLeft(n -> ref.match(n) ? Boolean.TRUE : null);
			return found == Boolean.TRUE;
		}
	}

	private static class OpEq implements Op {
		OpRef ref;

		public OpEq(OpRef ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			return ref.toString();
		}

		@Override
		public boolean match(Node node) {
			return ref.match(node);
		}
	}

	private static enum Type {
		ID, NAME, ALIAS
	}

	private static class OpRef implements Op {
		String identifier;
		Type type;
		int shift;

		public OpRef(String identifier, Type type) {
			this.identifier = identifier;
			this.type = type;
		}

		public OpRef(int shift) {
			this.identifier = null;
			this.type = null;
			this.shift = shift;
		}

		@Override
		public String toString() {
			if (type == null) {
				return String.valueOf(shift);
			}
			String shift = this.shift == 0 ? "" : ((this.shift > 0 ? "+" : "") + String.valueOf(this.shift));
			switch (type) {
			case ALIAS:
				return "alias " + identifier + shift;
			case ID:
				return identifier + shift;
			case NAME:
				return "name " + identifier + shift;
			}
			throw new IllegalStateException(type.name());
		}

		@Override
		public boolean match(Node node) {
			if (type == null) {
				return node.isInRoot() ? node.getIndex() == shift : false;
			} else {
				Node neighbor = node.selectNeighbour(-shift);
				if (neighbor == null)
					return false;
				switch (type) {
				case ALIAS:
					return neighbor.hasAlias(identifier);
				case ID:
					return Objects.equals(neighbor.getId(), identifier);
				case NAME:
					return Objects.equals(neighbor.getName(), identifier);
				}
				throw new IllegalStateException(type.name());
			}
		}

	}

	private static class OpNumber implements Op {
		int num;

		public OpNumber(int num) {
			this.num = num;
		}

		@Override
		public boolean match(Node node) {
			return false;
		}
	}

	private final Op root;

	public Selector(Op root) {
		this.root = root;
	}


	public static Selector compile(String selector) {
		CharStream inputStream = CharStreams.fromString(selector);
		SelectorLexer lexer = new SelectorLexer(inputStream) {
			@Override
			public void recover(LexerNoViableAltException e) {
				throw new ParseCancellationException(e);
			}
		};
		SelectorParser parser = new SelectorParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());
		parser.addErrorListener(new ANTLRErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new ParseCancellationException(msg + " at position " + charPositionInLine);
			}

			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					int prediction, ATNConfigSet configs) {

			}

			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {

			}

			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {

			}
		});
		StartContext op = parser.start();
		class Visitor extends SelectorBaseVisitor<Op> {

			@Override
			public Op visitShiftExact(ShiftExactContext ctx) {
				return new OpNumber(Integer.valueOf(ctx.NUM().getText()));
			}

			@Override
			public Op visitShiftBracket(ShiftBracketContext ctx) {
				return visit(ctx.shift());
			}

			@Override
			public Op visitShiftExactNeg(ShiftExactNegContext ctx) {
				OpNumber shift = (OpNumber) visit(ctx.shift());
				shift.num = -shift.num;
				return shift;
			}

			@Override
			public Op visitShiftMinus(ShiftMinusContext ctx) {
				OpNumber lhs = (OpNumber) visit(ctx.shift(0));
				OpNumber rhs = (OpNumber) visit(ctx.shift(1));
				lhs.num = lhs.num - rhs.num;
				return lhs;
			}

			@Override
			public Op visitShiftPlus(ShiftPlusContext ctx) {
				OpNumber lhs = (OpNumber) visit(ctx.shift(0));
				OpNumber rhs = (OpNumber) visit(ctx.shift(1));
				lhs.num = lhs.num + rhs.num;
				return lhs;
			}

			@Override
			public Op visitId(IdContext ctx) {
				return new OpRef(ctx.ID().getText(), Type.ID);
			}

			@Override
			public Op visitAlias(AliasContext ctx) {
				return new OpRef(ctx.ID().getText(), Type.ALIAS);
			}

			@Override
			public Op visitName(NameContext ctx) {
				return new OpRef(ctx.ID().getText(), Type.NAME);
			}

			@Override
			public Op visitRefAbs(RefAbsContext ctx) {
				OpNumber shift = (OpNumber) visit(ctx.shift());
				return new OpRef(shift.num);
			}

			@Override
			public Op visitRefExact(RefExactContext ctx) {
				return visit(ctx.ident());
			}

			@Override
			public Op visitRefMinus(RefMinusContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ident());
				OpNumber shift = (OpNumber) visit(ctx.shift());
				ref.shift = -shift.num;
				return ref;
			}

			@Override
			public Op visitRefPlus(RefPlusContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ident());
				OpNumber shift = (OpNumber) visit(ctx.shift());
				ref.shift = shift.num;
				return ref;
			}

			@Override
			public Op visitExprEq(ExprEqContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ref());
				return new OpEq(ref);
			}

			@Override
			public Op visitExprGe(ExprGeContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ref());
				return new OpGe(ref);
			}

			@Override
			public Op visitExprGt(ExprGtContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ref());
				return new OpGt(ref);
			}

			@Override
			public Op visitExprLe(ExprLeContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ref());
				return new OpLe(ref);
			}

			@Override
			public Op visitExprLt(ExprLtContext ctx) {
				OpRef ref = (OpRef) visit(ctx.ref());
				return new OpLt(ref);
			}

			@Override
			public Op visitOpBracket(OpBracketContext ctx) {
				return visit(ctx.op());
			}

			@Override
			public Op visitOpAnd(OpAndContext ctx) {
				return new OpAnd(visit(ctx.op(0)), visit(ctx.op(1)));
			}

			@Override
			public Op visitOpOr(OpOrContext ctx) {
				return new OpOr(visit(ctx.op(0)), visit(ctx.op(1)));
			}

			@Override
			public Op visitOpNot(OpNotContext ctx) {
				return new OpNot(visit(ctx.op()));
			}

			@Override
			public Op visitOpExpr(OpExprContext ctx) {
				return visit(ctx.expr());
			}
			@Override
			public Op visitStart(StartContext ctx) {
				return  visit(ctx.op());
			}
		}
		Visitor visitor = new Visitor();
		Op out = visitor.visit(op);
		return new Selector(out);

	}

	@Override
	public String toString() {
		return root.toString();
	}

	public boolean match(Node node) {
		return root.match(node);
	}

}
