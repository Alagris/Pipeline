/*
 * Lexer Rules
 */
grammar Selector;

shift : NUM				# ShiftExact
	| '-' shift			# ShiftExactNeg
	| shift '+' shift	# ShiftPlus	
	| shift '-' shift	# ShiftMinus
	| '(' shift ')'		# ShiftBracket
	;

ident : ID 			# Id
	| 'alias' ID	# Alias
	| 'name' ID		# Name
	;

ref : ident '-' shift	# RefMinus
	| ident '+' shift	# RefPlus
	| ident 			# RefExact
	| shift				# RefAbs
	;
	
	
expr : ref '<' '*'	# ExprLt
	| ref '<=' '*'	# ExprLe
	| ref '>' '*'	# ExprGt
	| ref '>=' '*'	# ExprGe
	| '*' '<'  ref	# ExprGt
	| '*' '<=' ref	# ExprGe
	| '*' '>' ref	# ExprLt
	| '*' '>=' ref	# ExprLe
	| '*' '==' ref	# ExprEq
	| ref '==' '*'	# ExprEq
	| ref 			# ExprEq
	;
	
op : '(' op ')' 	# OpBracket
	|'!' op 		# OpNot
	| op '&&' op	# OpAnd
	| op '||' op 	# OpOr
	| expr			# OpExpr
	;
	
start : op EOF ;

OR		: '||' ;
AND		: '&&' ;
NOT		: '!' ;
MINUS	: '-' ;
PLUS	: '+' ;
GE		: '>=' ;
LE		: '<=' ;
GT		: '>' ;
LT		: '<' ;    
ALL		: '*' ;
OPEN	: '(' ;
CLOSE	: ')' ;
NAME	: 'name' ;
ALIAS	: 'alias' ;
ID 		: [a-zA-Z_.][a-zA-Z_.0-9]* ;
NUM     : [0-9]+ ;
WS  	: [ \t\n]+ -> skip ;
