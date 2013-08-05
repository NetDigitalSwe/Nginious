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

package com.nginious.http.xsp;

/**
 * Parses a XSP page into a tree structure of nodes. The root node is an instance of {@link DocumentPart}
 * which represent the whole XSP page. A XSP page is parsed into the following possible parts.
 * 
 * <ul>
 * <li>{@link DocumentPart} - Root node part of a XSP page.</li>
 * <li>{@link StaticPart} - Static content, for example HTML or a XSP expression.</li>
 * <li>{@link IfTagPart} - Part with an expression. Sub parts are executed if expression evaluates to true.</li>
 * <li>{@link ForEachTagPart} - Part which iterates over a collection. Sub parts are executed for each element in the collection.</li>
 * <li>{@link MessageTagPart} - Part which formats a text message according to locale found in HTTP response.</li>
 * <li>{@link FormatDateTagPart} - Part which formats a date according to locale found in HTTP response.</li>
 * <li>{@link FormatNumberTagPart} - Part which formats a number according to locale found in HTTP response.</li>
 * </ul>
 * 
 * Below is an example XSP page.
 * 
 * <pre>
 *&lt;html&gt;
 *  &lt;head&gt;
 *    &lt;xsp:meta name="package" content="se.netdigital.http.xsp" /&gt;
 *    &lt;xsp:meta name="Content-Type" content="text/html; charset=utf-8" /&gt;
 *    &lt;title&gt;XspTest&lt;/title&gt;
 *  &lt;/head&gt;
 *  &lt;body&gt;
 *    &lt;h1&gt;XspTest&lt;/h1&gt;
 *    &lt;xsp:if test="${test == 'Hello world!'}"&gt;
 *      &lt;xsp:forEach set="${testset}" var="var" start="0" end="8" step="2"&gt;
 *        &lt;h2&gt;${test} ${var}&lt;/h2&gt;
 *      &lt;/xsp:forEach&gt;
 *    &lt;/xsp:if&gt;
 *  &lt;/body&gt;
 *&lt;/html&gt;
 * </pre
 *
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
class XspParser {
	
	private enum State {
		START, START_EXPR, IN_EXPR, START_TAG, 
		TAG_NAME, ATTRIBUTE_NAME, ATTRIBUTE_VALUE_START, ATTRIBUTE_VALUE, 
		END_START_TAG, IN_TAG, END_TAG
	}
	
    private static final byte[] START_TAG_LOWER_CASE = { 'x', 's', 'p', ':' };

    private static final byte[] START_TAG_UPPER_CASE = { 'X', 'S', 'P', ':' };

    private static final byte[] END_TAG_LOWER_CASE = { '/', 'x', 's', 'p', ':' };

    private static final byte[] END_TAG_UPPER_CASE = { '/', 'X', 'S', 'P', ':' };

    private PartFactory factory;
    
    /**
     * Constructs a new XSP parser.
     */
    XspParser() {
        super();
        this.factory = new PartFactory();
    }
    
    /**
     * Parses the XSP page contained in the specified byte array located at the specified source file path.
     * 
     * @param srcFilePath the XSP page file path
     * @param b the byte array containing the contents of the XSP page
     * @return the document root node of the constructed tree structure
     * @throws XspException if unable to parse XSP page
     */
    DocumentPart parse(String srcFilePath, byte[] b) throws XspException {
    	DocumentPart document = new DocumentPart(srcFilePath, 1);
        parse(srcFilePath, b, 0, b.length, document);
        return document;
    }
    
    /**
     * Called recursively to parse the part of the XSP page contained within the specified start and end positions within the 
     * specified byte array. The constructed parts resulting from parsing the part are added to the specified parent part.
     * 
     * @param srcFilePath the XSP page file path
     * @param b the byte array containing the contents of the XSP page
     * @param start the start parse position inclusive within the  byte array
     * @param end the end parse position exclusive within the byte array
     * @param parentPart the parent part to add all created parts to
     * @throws XspException if unable to parse the XSP page part
     */
    @SuppressWarnings("incomplete-switch")
	private void parse(String srcFilePath, byte[] b, int start, int end, ParentPart parentPart) throws XspException {
        State state = State.START;
        int pos = start;
        int partStart = start;
        int subPartStart = start;
        int lineNo = 1;
        
        TagPart curTagPart = null;
        MetaTagPart curMetaTagPart = null;
        String attributeName = null;
        byte attributeValueDelimiter = 0;
        boolean escape = false;
        byte eol = 0;

        while(pos < end) {
            byte ch = b[pos];
            
            if(eol == 0x0d && ch == 0x0a) {
            	eol = 0x0a;
            } else if(ch == 0x0a){
            	lineNo++;
            	eol = ch;
            }

            switch(state) {
                case START:
                    switch(ch) {
                        case '<':
                            state = State.START_TAG;
                            break;

                        case '$':
                            state = State.START_EXPR;
                            break;
                    }
                    break;

                case START_EXPR:
                    switch(ch) {
                        case '<':
                            state = State.START_TAG;
                            break;

                        case '$':
                            state = State.START_EXPR;
                            break;

                        case '{':
                            state = State.IN_EXPR;

                            if(partStart < pos - 1) {
                                StaticPart staticPart = new StaticPart(b, partStart, pos - partStart - 1, srcFilePath, lineNo);
                                parentPart.addPart(staticPart);
                            }

                            partStart = pos - 1;
                            subPartStart = pos - 1;
                            break;

                        default:
                            state = State.START;
                    }
                    break;

                case IN_EXPR:
                    if(ch == '}') {
                        state = State.START;

                        if(partStart < pos) {
                            StaticPart part = new StaticPart(b, partStart, pos - partStart + 1, srcFilePath, lineNo);
                            parentPart.addPart(part);
                        }

                        partStart = pos + 1;
                        subPartStart = pos + 1;
                    }
                    break;

                case START_TAG:
                    switch(ch) {
                        case '<':
                            state = State.START_TAG;
                            break;

                        case '$':
                            state = State.START_EXPR;
                            break;

                        case '>':
                            state = State.START;

                            break;

                        default:
                            if(peek(b, pos, START_TAG_LOWER_CASE) || peek(b, pos, START_TAG_UPPER_CASE)) {
                                state = State.TAG_NAME;
                                partStart = pos - 1;
                            }
                            break;
                    }
                     break;

                case TAG_NAME:
                    switch(ch) {
                        case '<':
                            state = State.START_TAG;
                            partStart = pos;
                            break;

                        case '$':
                            state = State.START_EXPR;
                            partStart = pos;
                            break;

                        case '>':
                            state = State.START;
                            partStart = pos;
                            break;

                        default:
                            if(Character.isWhitespace(ch)) {
                                String tagName = new String(b, partStart + 5, pos - partStart - 5);
                                
                                if(tagName.equals("meta")) {
                                	curMetaTagPart = factory.createMetaTagPart(tagName, srcFilePath, lineNo);
                                	curTagPart = curMetaTagPart;
                                } else {
                                    curTagPart = factory.createTagPart(tagName, srcFilePath, lineNo);
                                }
                                
                                int staticLen = pos - subPartStart - tagName.length() - "<xsp:".length();
                                StaticPart part = new StaticPart(b, subPartStart, staticLen, srcFilePath, lineNo);
                                
                                if(!parentPart.isLastTagPart() || !part.isWhitespace()) {
                                	parentPart.addPart(part);
                                }

                                partStart = pos;
                                state = State.ATTRIBUTE_NAME;
                            }
                            break;
                    }
                    break;

                case ATTRIBUTE_NAME:
                    switch(ch) {
                        case '=':
                            attributeName = new String(b, partStart, pos - partStart).trim();
                            state = State.ATTRIBUTE_VALUE_START;
                            partStart = pos + 1;
                            break;

                        case '>':
                            state = State.IN_TAG;
                            partStart = pos + 1;
                            subPartStart = pos + 1;
                            break;
                            
                        case '/':
                        	state = State.END_START_TAG;
                        	break;

                        default:
                            break;
                    }
                    break;

                case ATTRIBUTE_VALUE_START:
                    switch(ch) {
                        case '"':
                            attributeValueDelimiter = '"';
                            state = State.ATTRIBUTE_VALUE;
                            partStart = pos + 1;
                            break;

                        case '\'':
                            attributeValueDelimiter = '\'';
                            state = State.ATTRIBUTE_VALUE;
                            partStart = pos + 1;
                            break;

                        default:
                            if(!Character.isWhitespace(ch)) {
                                state = State.ATTRIBUTE_VALUE;
                                partStart = pos;
                            }
                            break;
                    }
                    break;

                case ATTRIBUTE_VALUE:
                    switch(ch) {
                        case '"':
                        case '\'':
                            if(!escape && attributeValueDelimiter == ch) {
                            	StaticPart part = new StaticPart(b, partStart, pos - partStart, srcFilePath, lineNo);
                            	curTagPart.addAttribute(attributeName, part);
                                state = State.ATTRIBUTE_NAME;
                                attributeName = null;
                                attributeValueDelimiter = 0;
                                partStart = pos + 1;
                            }

                            escape = false;
                            break;

                        case '\\':
                            escape = true;
                            break;

                        default:
                            escape = false;
                            break;
                    }
                    break;
                
                case IN_TAG:
                    switch(ch) {
                        case '<':
                            if(peek(b, pos + 1, END_TAG_LOWER_CASE) || peek(b, pos + 1, END_TAG_UPPER_CASE)) {
                                state = State.END_TAG;
                                partStart = pos;
                            }
                            break;
                    }
                    break;
                    
                case END_START_TAG:
                	switch(ch) {
                	case '>':
                		state = State.START;
                		subPartStart = pos + 1;
                		
                		if(curMetaTagPart != null) {
                        	parentPart.addMetaTagPart(curMetaTagPart);
                        	parentPart.addTagPart(curMetaTagPart);
                        	curMetaTagPart = null;
                        } else {
                        	parentPart.addPart(curTagPart);
                        }
                        
                        curTagPart = null;
                		break;
                		
                	default:
                        if(!Character.isWhitespace(ch)) {
                            state = State.ATTRIBUTE_NAME;
                        }
                        break;
                	}
                	break;
                	
                case END_TAG:
                    switch(ch) {
                        case '>':
                        	int endStart = partStart + "<xsp:".length() + 1;
                        	int endLen = pos - partStart - "<xsp:".length() - 1;
                            String tagName = new String(b, endStart, endLen).trim();

                            if(tagName.equals(curTagPart.getName())) {
                                state = State.START;
                                
                                if(curMetaTagPart != null) {
                                	parentPart.addMetaTagPart(curMetaTagPart);
                                	curMetaTagPart = null;
                                } else {
                                	parentPart.addPart(curTagPart);
                                }
                                
                                int subPartLen = pos - subPartStart - 1 - tagName.length() - "<xsp:".length();
                                parse(srcFilePath, b, subPartStart, subPartStart + subPartLen, curTagPart);
                                subPartStart = pos + 1;
                                curTagPart = null;
                            } else {
                                state = State.IN_TAG;
                            }
                            
                            break;
                    }
                    break;
            }

            pos++;
        }
        
        switch(state) {
            case START:
            case START_EXPR:
            case IN_EXPR:
            case START_TAG:
                if(subPartStart < pos - 1) {
                    StaticPart part = new StaticPart(b, subPartStart, pos - subPartStart, srcFilePath, lineNo);
                    parentPart.addPart(part);
                }
                break;
            
            case TAG_NAME:
            case ATTRIBUTE_NAME:
            case ATTRIBUTE_VALUE_START:
            case ATTRIBUTE_VALUE:
            case IN_TAG:
            case END_TAG:
                throw new XspException("Unterminated XSP structure");
        }
    }
    
    /**
     * Checks if the specified byte array contains the specified data starting at the specified position.
     * 
     * @param b the byte array to check
     * @param pos the position in the byte array
     * @param data the data to check for
     * @return <code>true</code> if the byte array contains the data at the given position, <code>false</code> otherwise
     */
    private boolean peek(byte[] b, int pos, byte[] data) {
        if(b.length - pos < data.length) {
            return false;
        }

        for(int i = 0; i < data.length; i++) {
            if(data[i] != b[pos + i]) {
                return false;
            }
        }

        return true;
    }
}
