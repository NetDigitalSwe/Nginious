/**
 * Copyright 2012 NetDigital Sweden AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.nginious.http.xsp.expr;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Parses expressions strings building expression trees out of the parsed expression string. Each node in the expression
 * tree is a value, operator or function. Each operator holds its left and right side values as child nodes. A function
 * holds its arguments as child nodes. The parser supports values, arithmetic operators, comparison operators, 
 * boolean operators and functions.
 * 
 * <h3>Types</h3>
 * 
 * The following value types are supported by the parser. 
 * 
 * <ul>
 * <li>Integer - any value without decimals, represented by the value node {@link IntegerValue}.</li>
 * <li>Double - any value containing decimals, represented by the value node {@link DoubleValue}.</li>
 * <li>String - any value enclosed in single quotes, see value node {@link StringValue}.</li>
 * <li>Any - represents a value with unknown type at parse time, this can be variables or null values, 
 * 	see value node {@link NullValue} for an example.</li>
 * </ul>
 * 
 * Variables can be used in expressions which are resolved through a {@link Variables} context when an expression is
 * evaluated. Below is a list of supported variable types.
 * 
 * <ul>
 * <li>Variable - a variable reference, represented by the attribute node {@link AttributeValue}. An attribute reference is 
 * 	expressed with ${name} where 'name' is the attribute name.</li>
 * <li>Bean property - a bean property reference where the value is found in a bean property. The bean is found in a
 * 	variable context, see {@link BeanValue}. A bean property reference is expressed with ${beanName.propertyName} where
 * 	'beanName' is the bean attribute name and 'propertyName' is the bean property name.</li>
 * </ul>
 * 
 * When evaluating expressions automatic type conversion is done for all operators and functions according to the
 * following rules.
 * 
 * <ul>
 * <li>Arithmetic operators - if both values are integers then the result is also an integer, otherwise the result is
 * 	a double.</li>
 * <li>Comparison operators - type conversion is done in the same way as for arithmetic operators.</li>
 * <li>Boolean operators - values are converted to booleans. For integers <code>0</code> is interpreted as <code>false</code>,
 * 	all other values are interpreted as <code>true</code>. For doubles <code>0.0d</code> is interpreted as <code>false</code>,
 * 	all other values re interpreted as <code>true</code>. For strings the string literals <code>'true'</code> and
 * 	<code>'1'</code> are interpreted as <code>true</code>, all other values are interpreted as <code>false</code>.
 * <li>Math functions - result type depends on type of mathematical function.</li>
 * <li>String functions - result is always a string. Function arguments are converted into appropriate types.</li>
 * </ul>
 * 
 * Below is a list of supported arithmetic operators.
 * 
 * <ul>
 * <li>+ (addition) - adds two values, represented by the operator node {@link AddOperator}.</li>
 * <li>- (subtraction) - subtracts one value from another, represented by the operator node {@link SubOperator}.</li>
 * <li>* (multiplication) - multiplies one value with another, see operator node {@link MulOperator}.</li>
 * <li>/ (division) - divides one value with another, see operator node {@link DivOperator}.</li>
 * <li>% (modulo) - calculates the remainder of dividing one value with another, see operator node {@link ModOperator}.</li>
 * </ul>
 * 
 * The following comparison operators are supported.
 * 
 * <ul>
 * <li>== (equals) - compares one value to another for equality, represented by node {@link EqualsOperator}.</li>
 * <li>!= (not equals) - compares one value to another for inequality, represented by node {@link NotEqualsOperator}.</li>
 * <li>&lt; (less than) - checks if the left side value is less than the right side value, see node {@link LessOperator}.</li>
 * <li>&lt;= (less than or equals) - checks if the left side value is less than or equals to the right side value,
 * 	see node {@link LessEqualsOperator}.</li>
 * <li>&gt; (more than) - checks if the left side value is greater than the right side value, see node {@link MoreOperator}.</li>
 * <li>&gt;= (more than or equals) - checks if the left side value is greater than or equals than the right side value,
 * 	see node {@link MoreEqualsOperator}.</li>
 * </ul>
 * 
 * The following boolean operators are supported.
 * 
 * <ul>
 * <li>&& (and) - evaluates to <code>true</code> if both the left side and the right side expressions evaluate to
 * 	<code>true</code>, otherwise <code>false</code>. see {@link AndOperator}.</li>
 * <li>|| (or) - evaluates to <code>true</code> if the left side or the right side expression evaluates to
 * 	<code>true</code>, otherwise <code>false</code>, see {@link OrOperator}.</li>
 * </ul>
 * 
 * Operator precedence
 * 
 * <p>
 * Operator precedence is the same as in the Java programming language. Expressions are interpreted from left to right by 
 * the parser. But operators with higher precedence are evaluated before operators with lower precedence. If two operators 
 * have the same precedence the leftmost operator is evaluated first. Parenthesis can be used to change operator precedence.
 * Below is the operator precedence in descending order.
 * 
 * <ul>
 * <li>* (multiplication)</li>
 * <li>/ (division)</li>
 * <li>% (modulo)</li>
 * <li>+ (addition)</li>
 * <li>- (subtraction)</li>
 * <li>== (equals)</li>
 * <li>!= (not equals)</li>
 * <li>&lt; (less than)</li>
 * <li>&lt;= (less than or equal)</li>
 * <li>&gt; (greater than)</li>
 * <li>&gt;= (greater than or equal)</li>
 * <li>&& (and)</li>
 * <li>|| (or)</li>
 * </ul>
 * </p>
 * 
 * The following mathematical functions are supported.
 * 
 * <ul>
 * <li>abs - calculates the absolute value of a value, see function node {@link AbsFunction}.</li>
 * <li>acos - calculates the arc cosine of a value, see function node {@link AcosFunction}.</li>
 * <li>asin - calculates the arc sine of a value, represented by the function node {@link AsinFunction}.</li>
 * <li>atan - calculates the arc tangent of a value, see function node {@link AtanFunction}.</li>
 * <li>ceil - calculates the nearest mathematical integer to the argument value that is greater or equal, 
 * 	see function node {@link CeilFunction}.</li>
 * <li>cos - calculates the trigonometric sine of a value, represented by the function node {@link SinFunction}.</li>
 * <li>exp - calculates Euler's number e raised to the power of a value, see function node {@link ExpFunction}.</li>
 * <li>floor - calculates the nearest mathematical integer to the argument value this is less or equal,
 * 	see function node {@link FloorFunction}.</li>
 * <li>log - calculates the natural logarithm (base e) of a value, represented by the functio node {@link LogFunction}.</li>
 * <li>max - returns the greater of two values, see {@link MaxFunction}.</li>
 * <li>min - returns the smaller of two values, see {@link MinFunction}.</li>
 * <li>pow - calculates the value of the first argument raised to the power of the second argument,
 * 	see function node {@link PowFunction}.</li>
 * <li>rint - returns the value that is closest in value to the argument and is equal to a mathematical integer,
 * 	see {@link RintFunction}.</li>
 * <li>sin - calculates the trigonometric sine of a value, see function node {@link SinFunction}.</li>
 * <li>sqrt - calculates the positive square root of a value, see {@link SqrtFunction}.</li>
 * <li>tan - calculates the trigonometric tangent of a value, see {@link TanFunction}.</li>
 * </ul>
 * 
 * Below is a list of supported string functions.
 * 
 * <ul>
 * <li>left - returns a substring of a string starting at index 0 up to an end index, see {@link LeftFunction}.</li>
 * <li>right - returns a substring of a string starting at a begin index to the end of the string, 
 * 	see function node {@link RightFunction}.</li>
 * <li>substr - returns a substring of a string starting at a begin index and ending at an end index,
 * 	see function node {@link SubstrFunction}.</li>
 * </ul>
 * 
 * Expression examples
 * 
 * <ul>
 * <li><code>1 + 1</code> - simple addition.</li>
 * <li><code>2 * 2 + 1</code> - multiplication and addition.</li>
 * <li><code>2 + max(3, 4) == 6</code> - example function and equals comparison.</li>
 * <li><code>${value} + 4 != 2</code> - example variable and not equals comparison.</li>
 * <li><code>${user.loginCount} + 4 == 5</code> - example bean property and equals comparison.</li>
 * </ul>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 */
public class ExpressionParser {

    private enum Function {
    	
        ABS("abs", 1),

        ACOS("acos", 1),

        ASIN("asin", 1),

        ATAN("atan", 1),

        CEIL("ceil", 1),

        COS("cos", 1),

        EXP("exp", 1),

        FLOOR("floor", 1),

        LEFT("left", 2),

        LENGTH("length", 1),

        LOG("log", 1),

        MAX("max", 2),

        MIN("min", 2),

        POW("pow", 2),

        RIGHT("right", 2),

        RINT("rint", 1),

        SIN("sin", 1),

        SQRT("sqrt", 1),

        SUBSTR("substr", 3),

        TAN("tan", 1);
        
        private static HashMap<String, Function> lookup = new HashMap<String, Function>();
        
        static {
        	lookup.put("abs", ABS);
        	lookup.put("acos", ACOS);
        	lookup.put("asin", ASIN);
        	lookup.put("atan", ATAN);
        	lookup.put("ceil", CEIL);
        	lookup.put("cos", COS);
        	lookup.put("exp", EXP);
        	lookup.put("floor", FLOOR);
        	lookup.put("left", LEFT);
        	lookup.put("length", LENGTH);
        	lookup.put("log", LOG);
        	lookup.put("max", MAX);
        	lookup.put("min", MIN);
        	lookup.put("pow", POW);
        	lookup.put("right", RIGHT);
        	lookup.put("rint", RINT);
        	lookup.put("sin", SIN);
        	lookup.put("sqrt", SQRT);
        	lookup.put("substr", SUBSTR);
        	lookup.put("tan", TAN);
        }
        
        @SuppressWarnings("unused")
        String name;
        
        int parameterCount;
        
        private Function(String name, int parameterCount) {
        	this.name = name;
        	this.parameterCount = parameterCount;
        }
        
        static Function get(String name) {
        	return lookup.get(name);
        }
    }
    
    private enum Operator {
    	
    	EQUALS("==", 3),
    	
    	NOT_EQUALS("!=", 3),
    	
    	LESS_THAN_OR_EQUALS("<=", 3),
    	
    	MORE_THAN_OR_EQUALS(">=", 3),
    	
    	LESS_THAN("<", 3),
    	
    	MORE_THAN(">", 3),
    	
    	AND("&&", 4),
    	
    	OR("||", 5),
    	
    	ADD("+", 2),
    	
    	SUB("-", 2),
    	
    	MUL("*", 1),
    	
    	DIV("/", 1),
    	
    	MOD("%", 1);
    	
    	private static HashMap<String, Operator> lookup = new HashMap<String, Operator>();
    	
    	static {
    		lookup.put("==", EQUALS);
    		lookup.put("!=", NOT_EQUALS);
    		lookup.put("<=", LESS_THAN_OR_EQUALS);
    		lookup.put(">=", MORE_THAN_OR_EQUALS);
    		lookup.put("<", LESS_THAN);
    		lookup.put(">", MORE_THAN);
    		lookup.put("&&", AND);
    		lookup.put("||", OR);
    		lookup.put("+", ADD);
    		lookup.put("-", SUB);
    		lookup.put("*", MUL);
    		lookup.put("/", DIV);
    		lookup.put("%", MOD);
    	}
    	
        @SuppressWarnings("unused")
    	String operator;
    	
    	int precedence;
    	
    	private Operator(String operator, int precedence) {
    		this.operator = operator;
    		this.precedence = precedence;
    	}
    	
    	static Operator get(String operator) {
    		return lookup.get(operator);
    	}
    }
    
    private String expression;
    
    private int expressionPos;
    
    private int expressionLength;

    /**
     * Constructs a new expression parser.
     */
    public ExpressionParser() {
    	super();
    }
    
    private enum ParserState {
    	EXPECT_VALUE, EXPECT_OPERATOR;
    }
    
    /**
     * Parses the specified expression and breaks it up into a value node tree
     * where each nodes represents a value, function or operator.
     * 
     * @param expression the expression to parse
     * @return a tree expression
     * @throws ExpressionException if unable to parse expression
     * @see Value
     * @see Function
     * @see ArithmeticOperator
     * @see BooleanOperator
     * @see ComparisonOperator
     */
    public TreeExpression parse(String expression) throws ExpressionException {
    	this.expression = expression;
    	this.expressionLength = expression.length();
    	this.expressionPos = 0;
    	
    	Value exp = parse(false, false);
    	return new TreeExpression(exp);
    }
    
    private Value parse(boolean parenthesis, boolean comma) throws ExpressionException {
        LinkedList<Value> values = new LinkedList<Value>();
        LinkedList<Operator> operators = new LinkedList<Operator>();
        String part;
        ParserState state = ParserState.EXPECT_VALUE;

        while((part = nextToken(state == ParserState.EXPECT_OPERATOR)) != null) {
            switch(state) {
                case EXPECT_VALUE:
                    if(part.equals("(")) {
                    	Value exp = parse(true, false);
                    	values.add(exp);
                    } else {
                    	Value value = parseValue(part);
                    	values.add(value);
                    }

                    state = ParserState.EXPECT_OPERATOR;
                    break;

                case EXPECT_OPERATOR:
                	if(part.equals(")")) {
                		if(comma) {
                            throw new ExpressionException("Mismatched parenthesis");
                        }
                		
                		if(parenthesis) {
                			return buildExpression(values, operators);
                		} else {
                			throw new ExpressionException("Mismatched parenthesis");
                		}
                    } else if (part.equals(",")) {
                        if(!comma) {
                            throw new ExpressionException("Wrong number of parameters to function");
                        }

                        return buildExpression(values, operators);
                    }
                	
                	Operator operator = Operator.get(part);
                	operators.add(operator);
                    state = ParserState.EXPECT_VALUE;
                    break;
            }
        }

        if(state == ParserState.EXPECT_VALUE) {
            throw new ExpressionException("Expected value at end of expression");
        }
        
        return buildExpression(values, operators);
    }
    
    /**
     * Builds expression tree nodes from the specified list of values and operators. Operator precedence is taken
     * into account when building the expression tree.
     * 
     * @param values the list of value
     * @param operators the list of operators
     * @return the root value node of the build expression tree
     * @throws ExpressionException if unable to resolve operator
     */
    private Value buildExpression(LinkedList<Value> values, LinkedList<Operator> operators) throws ExpressionException {
    	for(int i = 1; i <= 5; i++) {
    		int pos = 0;
    		Operator[] operatorsArray = operators.toArray(new Operator[operators.size()]);
    		
    		for(Operator operator : operatorsArray) {
    			if(operator.precedence == i) {
    				Value value1 = values.remove(pos);
    				Value value2 = values.remove(pos);
    				operators.remove(operator);
    				Value operatorValue = createOperator(operator, value1, value2);
    				values.add(pos, operatorValue);
    			} else {
    				pos++;
    			}
    		}
    	}
    	
    	return values.get(0);
    }
    
    /**
     * Parses a function.
     * 
     * @param function the function
     * @return the root value node of the expression tree for the function
     * @throws ExpressionException if expression is invalid
     */
    private Value parseFunction(Function function) throws ExpressionException {
        String part = nextToken(true);
        int num = function.parameterCount;
        Value[] params = new Value[num];

        if(part == null || !part.equals("(")) {
            throw new ExpressionException("Malformed function, expected parenthesis");
        }

        for(int i = 0; i < num; i++) {
            params[i] = parse(i + 1 == num, i + 1 < num);
        }

        return createFunction(function, params);
    }
    
    /**
     * Creates a value from the specified string. The value is examined to find the
     * type with the following rules.
     * 
     * <ul>
     * <li>If the value starts with a ' and ends with a ' it is handled as a string.</li>
     * <li>If the value is all digits with decimals it is handled as a double.</li>
     * <li>If the value is all digits it is handled as an integer.</li>
     * <li>If the value starts with ${ and ends with } it is handled as a bean property or a variable.</li>
     * <li>If the value is a function name it is parsed as a function.</li>
     * <li>If the value is the string literal <code>NaN</code> it is handled as a double infinity.</li>
     * <li>IF the value us the string literal <code>null</code> it is handled as a null value.</li>
     * </ul>
     * 
     * @param value the string value
     * @return the created value
     * @throws ExpressionException if unable to parse value
     */
    private Value parseValue(String value) throws ExpressionException {
        Value exp = null;
        
        if(value.charAt(0) == '\'') {
        	value = value.substring(1, value.length() - 1);
        	exp = new StringValue(value);
        } else if(value.matches("-?[0-9]+\\.[0-9]+")) {
        	exp = new DoubleValue(Double.parseDouble(value));
        } else if(value.matches("-?[0-9]+")) {
        	exp = new IntegerValue(Integer.parseInt(value));
        } else if(value.matches("[a-zA-Z]+\\.[a-zA-Z][a-zA-Z0-9]+")) {
        	String[] beanMethodName = value.split("\\.");
        	exp = new BeanValue(beanMethodName[0], beanMethodName[1]);
        } else if(Function.get(value) != null) {
        	Function func = Function.get(value);
        	exp = parseFunction(func);
        } else if(value.matches("[a-zA-Z]+")) {
        	if(value.equals("NaN")) {
        		exp = new DoubleValue(Double.NaN);
        	} else if(value.equals("null")) {
        		exp = new NullValue();
        	} else {
        		exp = new AttributeValue(value);
        	}
        }
        
        if(exp == null) {
        	throw new ExpressionException("Unknown value type '" + value + "'");
        }
        
        return exp;
    }

    /**
     * Creates an operator value node of the specified type with the specified left and right values.
     * 
     * @param operator the operator
     * @param left the left side value
     * @param right the right side value
     * @return the created operator value node
     * @throws ExpressionException If any of the values are invalid for the operator type
     */
    private Value createOperator(Operator operator, Value left, Value right) throws ExpressionException {
        Value exp = null;

        switch(operator) {
            case EQUALS:
                exp = new EqualsOperator(left, right);
                break;

            case NOT_EQUALS:
                exp = new NotEqualsOperator(left, right);
                break;

            case LESS_THAN_OR_EQUALS:
                exp = new LessEqualsOperator(left, right);
                break;

            case MORE_THAN_OR_EQUALS:
                exp = new MoreEqualsOperator(left, right);
                break;

            case LESS_THAN:
                exp = new LessOperator(left, right);
                break;

            case MORE_THAN:
                exp = new MoreOperator(left, right);
                break;

            case AND:
                exp = new AndOperator(left, right);
                break;

            case OR:
                exp = new OrOperator(left, right);
                break;

            case ADD:
                exp = new AddOperator(left, right);
                break;

            case SUB:
                exp = new SubOperator(left, right);
                break;

            case MUL:
                exp = new MulOperator(left, right);
                break;

            case DIV:
                exp = new DivOperator(left, right);
                break;

            case MOD:
                exp = new ModOperator(left, right);
                break;
        }

        return exp;
    }
    
    /**
     * Creates a function of the specified type with the specified argument values.
     * 
     * @param function the function
     * @param arguments the function argument values
     * @return the created function value node
     */
    private Value createFunction(Function function, Value[] arguments) {
        Value exp = null;

        switch(function) {
            case ABS:
                exp = new AbsFunction(arguments[0]);
                break;

            case ACOS:
                exp = new AcosFunction(arguments[0]);
                break;

            case ASIN:
                exp = new AsinFunction(arguments[0]);
                break;

            case ATAN:
                exp = new AtanFunction(arguments[0]);
                break;

            case CEIL:
                exp = new CeilFunction(arguments[0]);
                break;

            case COS:
                exp = new CosFunction(arguments[0]);
                break;

            case EXP:
                exp = new ExpFunction(arguments[0]);
                break;

            case FLOOR:
                exp = new FloorFunction(arguments[0]);
                break;

            case LEFT:
                exp = new LeftFunction(arguments[0], arguments[1]);
                break;

            case LENGTH:
                exp = new LengthFunction(arguments[0]);
                break;

            case LOG:
                exp = new LogFunction(arguments[0]);
                break;

            case MAX:
                exp = new MaxFunction(arguments[0], arguments[1]);
                break;

            case MIN:
                exp = new MinFunction(arguments[0], arguments[1]);
                break;

            case POW:
                exp = new PowFunction(arguments[0], arguments[1]);
                break;

            case RIGHT:
                exp = new RightFunction(arguments[0], arguments[1]);
                break;

            case RINT:
                exp = new RintFunction(arguments[0]);
                break;

            case SIN:
                exp = new SinFunction(arguments[0]);
                break;

            case SQRT:
                exp = new SqrtFunction(arguments[0]);
                break;

            case SUBSTR:
                exp = new SubstrFunction(arguments[0], arguments[1], arguments[2]);
                break;

            case TAN:
                exp = new TanFunction(arguments[0]);
                break;
        }

        return exp;
    }
    
    private enum TokenizerState {
    	
        /**
         * Constant defining a state where the tokenizer is in an initial state.
         */
    	NONE,
    	
        /**
         * Constant defining a state where the tokenizer has found an operator.
         */
    	OPERATOR;
    }
    
    /**
     * Returns the next token while parsing the expression.
     * 
     * @param operator whether or not an operator is expected
     * @return the found token
     * @throws ExpressionException if unable to parse expression
     */
    private String nextToken(boolean operator) throws ExpressionException {
    	StringBuffer part = new StringBuffer();
    	boolean quote = false;
    	boolean escape = false;
    	TokenizerState state = TokenizerState.NONE;
    	
    	while(this.expressionPos < this.expressionLength) {
    		char ch = expression.charAt(this.expressionPos++);
    		
    		switch(ch) {
    		case '\\':
    			escape = true;
    			break;
    		
    		case '\'':
    			if(state == TokenizerState.OPERATOR) {
    				this.expressionPos--;
                    return part.toString();
                }

                if(!quote) {
                    quote = true;
                    part.append('\'');
                } else if(escape) {
                    escape = false;
                    part.append('\'');
                } else {
                    part.append('\'');
                    return part.toString();
                }
    			break;
    		
    		case '&':
            case '|':
            case '!':
            case '=':
            case '<':
            case '>':
            	escape = false;
                part.append(ch);

                if(!quote) {
                    state = TokenizerState.OPERATOR;
                }
            	break;
            
            case '-':
            case '+':
            	if(!quote && operator) {
                    return Character.toString(ch);
                } else if (quote || state != TokenizerState.OPERATOR) {
                    part.append(ch);
                }
            	break;
            	
            case '%':
            case '*':
            case ',':
            case '/':
            case '(':
            case ')':
            	if(!quote) {
            		if(part.length() > 0) {
            			this.expressionPos--;
            			return part.toString();
            		} else {
            			return Character.toString(ch);
            		}
                }

                escape = false;
                part.append(ch);
            	break;
            
            default:
            	if(Character.isWhitespace(ch) || Character.isSpaceChar(ch)) {
            		if(quote) {
                        part.append(' ');
                        break;
                    }

                    if(part.length() > 0) {
                        return part.toString();
                    }
            	} else if(state == TokenizerState.OPERATOR) {
            		this.expressionPos--;
                    return part.toString();
            	} else {
            		part.append(ch);
                }
            	
                escape = false;
            	break;
    		}
    	}
    	
    	// EOF
    	return part.length() > 0 ? part.toString() : null;
    }
}
