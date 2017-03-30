grammar Plan;

plan
   : sequentialPlan | temporalPlan
   ;

sequentialPlan
   : (sequentialAction)*
   ;

temporalPlan
   : (temporalAction)*
   ;

sequentialAction
   : '(' action (object)* ')'
   ;

temporalAction
   : time ':' sequentialAction '[' duration ']'
   ;

object
   : NAME
   ;

action
   : NAME
   ;

time
   : NUMBER
   ;

duration
   : NUMBER
   ;

NUMBER
   : DIGIT+ ( '.' DIGIT+ )?
   ;

fragment DIGIT
   : '0' .. '9'
   ;

NAME
   : LETTER ANY_CHAR*
   ;

fragment LETTER
   : 'a' .. 'z' | 'A' .. 'Z'
   ;

fragment ANY_CHAR
   : LETTER | '0' .. '9' | '-' | '_'
   ;

LINE_COMMENT
   : ';' ~ ( '\n' | '\r' )* '\r'? '\n' -> skip
   ;

WHITESPACE
   : ( ' ' | '\t' | '\r' | '\n' )+ -> skip
   ;
