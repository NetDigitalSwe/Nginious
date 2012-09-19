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

/**
 * Evaluates an expression.
 * 
 * @author Bojan Pisler. NetDigital Sweden aB
 *
 */
public abstract class Expression {
	
	protected static final ThreadLocal<Variables> varsThreadLocal = new ThreadLocal<Variables>();
	
	/**
	 * Constructs a new expression.
	 */
	protected Expression() {
		super();
	}
	
	/**
	 * Evaluates this expression and returns a boolean as result. The result is converted to
	 * a boolean if necessary. The specified expressions variable context is used for resolving 
	 * variable names in the expression.
	 * 
	 * @param vars the expressions variable context
	 * @return <code>true</code> when expression evaluates to true, <code>false</code> otherwise
	 */
	public boolean evaluateBoolean(Variables vars) {
		try {
			varsThreadLocal.set(vars);
			return evaluateBoolean();
		} finally {
			varsThreadLocal.remove();
		}
	}
	
    /**
     * Evaluates this expression and returns the result as a boolean. The result
     * is converted to a boolean if needed.
     * 
     * @return the result of evaluating this expression converted to a boolean
     */
	protected abstract boolean evaluateBoolean();
	
	/**
	 * Evaluates this expression and returns an integer as result. The result is converted to
	 * an integer if necessary. The specified expressions variable context is used for resolving
	 * variable names in the expression.
	 * 
	 * @param vars the expressions variable context
	 * @return the result of evaluating this expression
	 */
	public int evaluateInt(Variables vars) {
		try {
			varsThreadLocal.set(vars);
			return evaluateInt();
		} finally {
			varsThreadLocal.remove();
		}
	}
	
    /**
     * Evaluates this expression and returns the result as an integer.
     * The result is converted to an integer if needed.
     * 
     * @return the result of evaluating this expression converted to an integer
     */
	protected abstract int evaluateInt();
	
	/**
	 * Evaluates this expression and returns a double as result. The result is converted to a
	 * double if necessary. The specified expressions variable context is used for resolving 
	 * variable names in the expression.
	 * 
	 * @param vars the expressions variable context
	 * @return the result of evaluating this expression
	 */
	public double evaluateDouble(Variables vars) {
		try {
			varsThreadLocal.set(vars);
			return evaluateDouble();
		} finally {
			varsThreadLocal.remove();
		}
	}
	
    /**
     * Evaluates this expression and returns the result as a double.
     * The result is converted to a double if needed.
     * 
     * @return the result of evaluating this expression converted to a double
     */
	protected abstract double evaluateDouble();
	
	/**
	 * Evaluates this expression and returns a string as result. The result is converted to a
	 * string if necessary. The specified expressions variable context is used for resolving
	 * variable names in the expression.
	 * 
	 * @param vars the expressions variable context
	 * @return the result of evaluating this expression
	 */
	public String evaluateString(Variables vars) {
		try {
			varsThreadLocal.set(vars);
			return evaluateString();
		} finally {
			varsThreadLocal.remove();
		}
	}
	
    /**
     * Evaluates this expression and returns the result as a string.
     * The result is converted to a string if needed.
     * 
     * @return the result of evaluating this expression converted to a string
     */
	protected abstract String evaluateString();
	
	/**
	 * Returns the type for the result value when this expression is evaluated.
	 * 
	 * @return the type
	 */
	public abstract Type getType();
	
	/**
	 * Sets this expressions variable context to the specified variable context.
	 * The variable context is used to resolve variable names when this expression
	 * is evaluated.
	 * 
	 * @param vars the variable context
	 */
	public static void setVariables(Variables vars) {
		varsThreadLocal.set(vars);
	}
	
	/**
	 * Removes the variable context from this expression.
	 */
	public static void removeVariables() {
		varsThreadLocal.remove();
	}
	
	/**
	 * Returns variable value with the specified name from this expressions variable
	 * context.
	 * 
	 * @param name the variable name
	 * @return the variable value or <code>null</code> if no variable with the given
	 * 	name exists in the variable context or no variable context set.
	 */
	public static Object getVariable(String name) {
		Variables vars = varsThreadLocal.get();
		
		if(vars != null) {
			return vars.getVariable(name);
		}
		
		return null;
	}
}
