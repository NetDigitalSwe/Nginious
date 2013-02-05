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

package com.nginious.http.cmd;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nginious.http.annotation.CommandLine;

/**
 * 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class CommandLineArguments {
	
	private HashMap<String, CommandLineArgument> longNameArgs;
	
	private HashMap<String, CommandLineArgument> shortNameArgs;
	
	private List<CommandLineArgument> args;
	
	/**
	 * Constructs a new command line arguments with the specified long name arguments map,
	 * short name arguments map and list of arguments.
	 * 
	 * @param longNameArgs long name argument to command line argument mapping
	 * @param shortNameArgs short argument name to command line argument mapping
	 * @param args command line arguments
	 */
	private CommandLineArguments(HashMap<String, CommandLineArgument> longNameArgs, 
			HashMap<String, CommandLineArgument> shortNameArgs,
			List<CommandLineArgument> args) {
		super();
		this.longNameArgs = longNameArgs;
		this.shortNameArgs = shortNameArgs;
		this.args = args;
	}
	
	/**
	 * Constructs a command line arguments for the specified target bean through introspection.
	 * The bean is searched for matching get and set methods where the set method has a command
	 * line mapping annotation which describes the command line arguments for the set method. See
	 * {@link CommandLine}.
	 * 
	 * @param target the target bean to create command line arguments for
	 * @return command line arguments
	 */
	public static CommandLineArguments createInstance(Object target) {
		Class<?> targetClazz = target.getClass();
		Method[] methods = targetClazz.getMethods();
		HashMap<String, CommandLineArgument> longNameArgs = new HashMap<String, CommandLineArgument>();
		HashMap<String, CommandLineArgument> shortNameArgs = new HashMap<String, CommandLineArgument>();
		ArrayList<CommandLineArgument> args = new ArrayList<CommandLineArgument>();
		
		for(int i = 0; i < methods.length; i++) {
			if(methods[i].getName().startsWith("set")) {
				CommandLine mapping = methods[i].getAnnotation(CommandLine.class);
				
				if(mapping != null) {
					String getMethodName = methods[i].getName().replaceFirst("set", "get");
					
					try {
						Method getMethod = targetClazz.getMethod(getMethodName);
						
						if(getMethod == null) {
							String property = createPropertyNameFromMethodName(getMethodName);
							throw new RuntimeException("Target object is not a bean. Get method for property '" + property + "' is missing.");
						}
					
						CommandLineArgument arg = new CommandLineArgument(mapping, getMethod, methods[i], target);
						String longName = arg.getLongName();
						String shortName = arg.getShortName();
						
						if(!longName.equals("")) {
							longNameArgs.put(arg.getLongName(), arg);
						}
						
						if(!shortName.equals("")) {
							shortNameArgs.put(arg.getShortName(), arg);
						}
						
						args.add(arg);
					} catch(NoSuchMethodException e) {
						String property = createPropertyNameFromMethodName(getMethodName);
						throw new RuntimeException("Target object is not a bean. Set method for property '" + property + "' is missing");
					}
				}
			}
		}
		
		CommandLineArguments arguments = new CommandLineArguments(longNameArgs, shortNameArgs, args);
		return arguments;
	}
	
	/**
	 * Pretty prints help for this command line arguments to the specified writer. Short for names,
	 * long form names and descriptions for each argument are printed.
	 * 
	 * @param writer the writer
	 */
	public void help(PrintWriter writer) {
		String spaces = "                                                  ";
		
		for(CommandLineArgument arg : this.args) {
			String shortName = arg.getShortName();
			String longName = arg.getLongName();
			boolean expectsValue = arg.expectsValue();
			String cmd = null;
			
			if(expectsValue) {
				cmd = createArgHelpWithValue(arg, shortName, longName);
			} else {
				cmd = createArgHelpWithoutValue(arg, shortName, longName);
			}
			
			if(cmd.length() > 25) {
				writer.println(cmd);
				writer.print(spaces.substring(0, 25));
				writer.println(arg.getDescription());
			} else {
				writer.print(cmd);
				writer.print(spaces.substring(0, 25 - cmd.length()));
				writer.println(arg.getDescription());
				
			}
		}
		
		writer.flush();
	}
	
	/**
	 * Creates a help string for the specified command line argument without value.
	 * 
	 * @param arg the command line argument
	 * @param shortName the short argument name
	 * @param longName the long argument name
	 * @return the help string
	 */
	private String createArgHelpWithoutValue(CommandLineArgument arg, String shortName, String longName) {
		StringBuffer value = new StringBuffer();
		
		if(!shortName.equals("")) {
			value.append(shortName);
		}
		
		if(!longName.equals("")) {
			if(!shortName.equals("")) {
				value.append(", ");
			}
			
			value.append(longName);
		}
		
		return value.toString();		
	}
	
	/**
	 * Creates a help string for the specified command line argument with value.
	 * 
	 * @param arg the command line argument
	 * @param shortName the short argument name
	 * @param longName the long argument name
	 * @return the help string
	 */
	private String createArgHelpWithValue(CommandLineArgument arg, String shortName, String longName) {
		StringBuffer value = new StringBuffer();
		
		if(!shortName.equals("")) {
			value.append(shortName);
			value.append(" #");
		}
		
		if(!longName.equals("")) {
			if(!shortName.equals("")) {
				value.append(", ");
			}
			
			value.append(longName);
			value.append("=#");
		}
		
		return value.toString();
	}
	
	/**
	 * Parses the specified array of command line arguments and calls the beans set methods with
	 * values.
	 * 
	 * @param args array of command line arguments
	 * @throws CommandLineException if any of the command line arguments are invalid or an expected command
	 * 	line argument is missing
	 */
	public void parse(String[] args) throws CommandLineException {
		int index = 0;
		
		while(index < args.length) {
			if(args[index].matches("-[a-zA-Z]")) {
				index = parseShortArg(args, index);
			} else if(args[index].matches("--[a-zA-Z]+=.+")) {
				index = parseLongValueArg(args, index);
			} else if(args[index].matches("--[a-zA-Z]+")) {
				index = parseLongArg(args, index);
			} else {
				throw new CommandLineException("Unknown argument '" + args[index] + "'");
			}
		}
		
		for(CommandLineArgument arg : this.args) {
			if(arg.isMandatory() && !arg.isSet()) {
				throw new CommandLineException("Missing '" + arg.getLongName() + "' argument");
			}
		}
	}
	
	/**
	 * Parses short name argument at the specified index in the specified list of command line arguments.
	 * 
	 * @param args the array of command line arguments
	 * @param index index of argument to parse
	 * @return next index in command line arguments array to parse
	 * @throws CommandLineException if argument is invalid
	 */
	private int parseShortArg(String[] args, int index) throws CommandLineException {
		String name = args[index];
		CommandLineArgument arg = shortNameArgs.get(name);
		
		if(arg == null) {
			throw new CommandLineException("Unknown argument '" + args[index] + "'");
		}
		
		String possibleValue = parsePossibleValue(args, index + 1);
		return arg.setShortFormValue(possibleValue) ? index + 2 : index + 1;
	}
	
	/**
	 * Parses long name argument at the specified index in the specified list of command line arguments.
	 * The argument is not expected to have a value
	 * 
	 * @param args the array of command line arguments
	 * @param index index of argument to parse
	 * @return next index in command line arguments array to parse
	 * @throws CommandLineException if argument is invalid
	 */
	private int parseLongArg(String[] args, int index) throws CommandLineException {
		String name = args[index];
		CommandLineArgument arg = longNameArgs.get(name);
		
		if(arg == null) {
			throw new CommandLineException("Unknown argument '" + args[index] + "'");
		}
		
		arg.setLongFormValue(null);
		return index + 1;
	}
	
	/**
	 * Parses long name argument at the specified index in the specified list of command line arguments.
	 * The argument is expected to have a value
	 * 
	 * @param args the array of command line arguments
	 * @param index index of argument to parse
	 * @return next index in command line arguments array to parse
	 * @throws CommandLineException if argument is invalid
	 */
	private int parseLongValueArg(String[] args, int index) throws CommandLineException {
		String nameValue = args[index];
		String[] nameValuePair = nameValue.split("=");
		CommandLineArgument arg = longNameArgs.get(nameValuePair[0]);
		
		if(arg == null) {
			throw new CommandLineException("Unknown argument '" + args[index] + "'");
		}
		
		arg.setLongFormValue(nameValuePair[1]);
		return index + 1;		
	}
	
	/**
	 * Parses possible value at the specified index in the specified command line argument array.
	 * 
	 * @param argv the command line argument array
	 * @param index the index
	 * @return value or <code>null</code> if index in command line argument array does not contain a value
	 */
	private String parsePossibleValue(String[] argv, int index) {
		if(index >= argv.length) {
			return null;
		}
		
		String possibleValue = argv[index];
		
		if(possibleValue.matches("-[a-zA-Z]") || possibleValue.matches("--[a-zA-Z]+") || possibleValue.matches("--[a-zA-Z]+=.+")) {
			return null;
		}
		
		return possibleValue;
	}
	
	/**
	 * Creates property name from method name.
	 * 
	 * @param methodName the method name
	 * @return the property name
	 */
	static String createPropertyNameFromMethodName(String methodName) {
		return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
	}	

}
