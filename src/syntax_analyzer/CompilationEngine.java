package syntax_analyzer;

import java.io.FileOutputStream; 
import java.io.PrintWriter;

import util.Keyword;
import util.TokenType;

public class CompilationEngine 
{
	public PrintWriter out;
	public PrintWriter outTokens;
	public JackTokenizer tokenizer;
	
	//constructor for the CompilationEngine object
	public CompilationEngine(JackTokenizer t, FileOutputStream o, FileOutputStream ot) 
	{
		tokenizer = t; 
		out = new PrintWriter(o); // create print writer object for output file 
		outTokens = new PrintWriter(ot); 

		// Generate tokens file
		outTokens.println("<tokens>"); //start tokens tag
		while (tokenizer.hasMoreTokens()) //while there are more tokens
		{
			tokenizer.advance();//advance to the next token

			String value;

			switch (tokenizer.tokenType()) 
			{
			    // if token is a keyword , get keyword value
				case TokenType.KEYWORD: 
					value = tokenizer.keyword();
					break;
					
				// if token is a symbol , get symbol value
				case TokenType.SYMBOL: 
					value = String.valueOf(tokenizer.symbol());
					break;
					
				// if token is an identifier , get identifier value
				case TokenType.IDENTIFIER:
					value = tokenizer.identifier();
					break;
					
				// if token is an integer constant , get integer constant value
				case TokenType.INT_CONST:
					value = String.valueOf(tokenizer.intVal());	
					break;
					
				// if token is a string constant , get string constant value
				case TokenType.STRING_CONST: 
					value = tokenizer.stringVal();
				default:
					value = "";
			}
			
			// print token to tokens file 
			outTokens.println("\t<" + tokenizer.tokenType() + "> " + value + " </" + tokenizer.tokenType() + ">"); 	
		}

		outTokens.println("</tokens>\n"); // end tokens tag
		outTokens.flush(); 	
		outTokens.close();	
		
		//reset tokenizer to beginning of file
		tokenizer.reset();	
	}
	// write error to output file
	public String writeError(String parameter) 	
	{
		//return error message
		return "ERROR occured!";
	}

	// check if next token is of type and value
	public boolean nextIs(String tokenType, String tokenValue) 
	{

		String token;
		String type = tokenizer.tokenType(); // get token type
		switch (tokenType) 
		{
		    // if token is a keyword
			case TokenType.KEYWORD: 
				if (!type.equals(TokenType.KEYWORD)) // if token type is not keyword
				{
					return false;
				}

				if (tokenValue.isEmpty() && tokenType.equals(type)) // if token value is empty and token type is keyword
				{
					return true;
				}

				token = tokenizer.keyword(); // get keyword value from tokenizer
				if (!token.isEmpty() && !token.equals(tokenValue)) // if token value is not empty and token value is not equal to keyword value
				
				{
					return false;
				}

				return true;

			// if token is a symbol
			case TokenType.SYMBOL: 

				if (!type.equals(TokenType.SYMBOL)) // if token type is not symbol
				
				{
					return false;
				}

				token = tokenizer.symbol(); // get symbol value from tokenizer
				if (!tokenValue.isEmpty() && !token.equals(tokenValue)) // if token value is not empty and token value is not equal to symbol value
				{
					return false;
				}

				return true;

			// if token is an identifier
			case TokenType.IDENTIFIER: 	
				if (type.equals(TokenType.IDENTIFIER)) 	// if token type is identifier
				{
					return true;
				}

				else
					return false;

			// if token is a string constant
			case TokenType.STRING_CONST: 	
				if (type.equals(TokenType.STRING_CONST)) // if token type is string constant
				{
					return true;
				}

				return false;

			// if token is an integer constant
			case TokenType.INT_CONST: 	
				if (type.equals(TokenType.INT_CONST)) // if token type is integer constant
				 {
					return true;
				}

				return false;

			default:
				return false;
		}

	}
	/**
	 'class' className 
	 '{'
	    classVarDec*
	    subroutineDec*
	  '}'
	 */
	
	public void compileClass() 		
	{
		String output = "";

		// KEYWORD::class
		tokenizer.advance();
		// if next token is not class
		if (!nextIs(TokenType.KEYWORD, Keyword.CLASS)) 
		{
			output += writeError("CLASS"); // write error to output file
		}
		output += "<class>\n"; // write class tag to output file
		output += "<keyword> " + tokenizer.keyword() + " </keyword>\n"; // write class keyword to output file 	

		// IDENTIFIER::classname
		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) // if next token is not identifier
		{
			output += writeError("IDENTIFIER"); // write error to output file
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n"; // write class name to output file

		// SYMBOL::{
		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{"))	// if next token is not { 		
		{
			output += writeError("SYMBOL"); // write error to output file
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write { to output file

		// classVarDec*
		output += compileClassVarDec(); // compile classVarDec	

		// subroutineDec*
		output += compileSubroutine(); // compile subroutineDec

		// SYMBOL::}
		tokenizer.advance();	// advance tokenizer to next token
		if (!nextIs(TokenType.SYMBOL, "}")) // if next token is not }
		{
			output += writeError("SYMBOL");// write error to output file
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n"; // write } to output file

		output += "</class>"; // write class end tag to output file

		out.println(output); // write output to output file
		out.flush();
		out.close();
	}

	// ('static'|'field') type varName (',' varName)* ';'
	public String compileClassVarDec() 	
	 {
		String output = "";

		tokenizer.advance();
		// while next token is static or field 
		while (nextIs(TokenType.KEYWORD, Keyword.STATIC) || nextIs(TokenType.KEYWORD, Keyword.FIELD)) 
		{
			output += "<classVarDec>\n"; 	// write classVarDec tag to output file
			output += "<keyword> " + tokenizer.keyword() + " </keyword>\n"; // write static or field keyword to output file
			output += compileVarDec(); 
			output += "</classVarDec>\n"; // write classVarDec end tag to output file
			tokenizer.advance(); // advance tokenizer to next token
		}
		tokenizer.previousToken(); // return tokenizer to previous token
		return output;	
	}

	
	// ('constructor'|'function'|'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
	public String compileSubroutine() 
	{
		String output = "";

		tokenizer.advance();
		
		// while next token is constructor, function, or method
		while (nextIs(TokenType.KEYWORD, Keyword.CONSTRUCTOR) || nextIs(TokenType.KEYWORD, Keyword.FUNCTION) || nextIs(TokenType.KEYWORD,Keyword.METHOD)) 	
		 {
			output += "<subroutineDec> \n" + "<keyword> " + tokenizer.keyword() + " </keyword>\n"; 	// write subroutineDec tag to output file

			tokenizer.advance();
			
			// if next token is void, int, char, or boolean
			if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(TokenType.KEYWORD, Keyword.BOOLEAN)) 
			{
				output += "<keyword> " + tokenizer.keyword() + " </keyword>\n"; // write void, int, char, or boolean keyword to output file

			}
			 else if (nextIs(TokenType.IDENTIFIER, "")) // if next token is identifier could be a classname
			{
				output += "<identifier> " + tokenizer.identifier() + " </identifier>\n"; // write identifier to output file
			} 
			else
			{
				output += writeError("KEYWORD' || 'IDENTIFIER"); // write error to output file
			}

			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) 	// if next token is not identifier
	
			{
				output += writeError("IDENTIFIER"); 	// write error to output file
			}
			
			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n"; // write subroutine name to output file

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "(")) // if next token is not (
			{
				output += writeError("SYMBOL");	// write error to output file
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n"; // write ( to output file

			output += "<parameterList>\n"; 	// write parameterList tag to output file
			output += compileParameterList();	
			output += "</parameterList>\n";	// write parameterList end tag to output file

			tokenizer.advance();

			if (!nextIs(TokenType.SYMBOL, ")")) // if next token is not )
			 {
				output += writeError("SYMBOL"); // write error to output file
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" + "<subroutineBody>\n"; // write ) to output file

			// Subroutine body
			tokenizer.advance();
			
			if (!nextIs(TokenType.SYMBOL, "{")) // if next token is not {
			{
				output += writeError("SYMBOL"); 
			}
			output += "<symbol> { </symbol>\n";	// write { to output file

			tokenizer.advance();
			
			//for variable declarations eg: var char x;
			while (nextIs(TokenType.KEYWORD, Keyword.VAR))// while next token is var
			 {
				output += "<varDec>\n";	// write varDec tag to output file
				output += "<keyword> var </keyword>\n";	// write var keyword to output file

				output += compileVarDec();	

				output += "</varDec>\n";

				tokenizer.advance();
			}
			tokenizer.previousToken();	// return tokenizer to previous token

			output += compileStatements(); 	// compile statements

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "}")) // if next token is not }
			{
				output += writeError("SYMBOL");
			}
			output += "<symbol> } </symbol>\n" + "</subroutineBody>\n" + "</subroutineDec>\n"; // write } to output file

			tokenizer.advance();
		}

		return output;
	}
	
	
    //((type varName) (',' type varName)*)?
	public String compileParameterList()  
	{
		String output = "";

		tokenizer.advance();
		
		// if next token is void, int, char, or boolean
		if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(TokenType.KEYWORD, Keyword.BOOLEAN)) 	
		{
			// write void, int, char, or boolean keyword to output file
			output += "<keyword> " + tokenizer.keyword() + " </keyword> \n"; 
		} 
		else if (nextIs(TokenType.IDENTIFIER, "")) // if next token is identifier
		{
			output += "<identifier> " + tokenizer.identifier() + " </identifier> \n"; // write identifier to output file
		} 
		else
		{
			tokenizer.previousToken();	// return tokenizer to previous token
			return "";
		}

		tokenizer.advance();
		
		if (!nextIs(TokenType.IDENTIFIER, "")) // if next token is not identifier
		{
			output += writeError("IDENTIFIER"); // write error to output file
			return "";
		}
		
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";	// write parameter name to output file

		tokenizer.advance();
		
		if (nextIs(TokenType.SYMBOL, ",")) 	// if next token is ,
		{
			output += "<symbol> , </symbol>\n"; // write , to output file
			 output += compileParameterList(); 
		}
		else 
		{
			tokenizer.previousToken();
		}
		return output;
	}

	// 'var' type varName (',' varName)* ';'
	public String compileVarDec() 
	{
		String output = "";

		tokenizer.advance();

		// if next token is void, int, char, or boolean
		if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(TokenType.KEYWORD, Keyword.BOOLEAN)) 	
		{
			// write void, int, char, or boolean keyword to output file
			output += "<keyword> " + tokenizer.keyword() + " </keyword> \n"; 

		}
		else if (nextIs(TokenType.IDENTIFIER, "")) // if next token is identifier
		{
			output += "<identifier> " + tokenizer.identifier() + " </identifier> \n";// write identifier to output file
		} 
		 else 
		{
			output += writeError("KEYWORD' || 'IDENTIFIER"); // write error to output file
		}

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, ""))// if next token is not identifier
		 {
			output += writeError("IDENTIFIER");	// write error to output file
			
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";// write var name to output file

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, ",")) // while next token is ,
		{
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write , to output file

			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) 	// if next token is not identifier
			{
				output += writeError("IDENTIFIER");	// write error to output file
				
			}

			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";// write var name to output file
			tokenizer.advance();// advance tokenizer to next token

		}

		if (!nextIs(TokenType.SYMBOL, ";")) // if next token is not ;
		{
			output += writeError("SYMBOL"); // write error to output file
			// return;
		}
		output += "<symbol> ; </symbol>\n";	// write ; to output file
		return output;	
	}
	

	// let,if,while,do,return
	public String compileStatements() 
	{
		String output = "";
		output += "<statements>\n";	// write statements tag to output file
		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, "")) // while next token is keyword
		{
			switch (tokenizer.keyword()) 		
			{
			    // if next token is let, write letStatement to output file
				case Keyword.LET:	
						output += "<letStatement>\n"
								+ "<keyword> " + tokenizer.keyword() + " </keyword>\n" 
								+ compileLet() 
								+ "</letStatement>\n";	
					break;
					
				// if next token is if , write ifStatement to output file
				case Keyword.IF:	
							output += "<ifStatement>\n"
									+ "<keyword> " + tokenizer.keyword() + " </keyword>\n"
									+ compileIf()
									+ "</ifStatement>\n";
					break;
					
				// if next token is while , write whileStatement to output file
				case Keyword.WHILE:
						output += "<whileStatement>\n"
								+ "<keyword> " + tokenizer.keyword() + " </keyword>\n"
								+ compileWhile()
								+ "</whileStatement>\n";
					break;
					
				// if next token is do , write doStatement to output file
				case Keyword.DO:
							output += "<doStatement>\n"
									+ "<keyword> " + tokenizer.keyword() + " </keyword>\n" 
									+ compileDo()
									+ "</doStatement>\n";
					break;
					
				// if next token is return , write returnStatement to output file
				case Keyword.RETURN:
						output += "<returnStatement>\n"
								+ "<keyword> " + tokenizer.keyword() + " </keyword>\n" 
								+ compileReturn()
								+ "</returnStatement>\n";
					break;
					
				default:
					break;
			}

			tokenizer.advance();
		}

		tokenizer.previousToken();
		output += "</statements>\n";// write statements end tag to output file
		return output;
	}

	// 'do' subroutineCall ';'
	public String compileDo() 
	{
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) // if next token is not identifier
		{
			output += writeError("IDENTIFIER");	// write error to output file
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n"; 	// write subroutine name to output file 	

		tokenizer.advance();
		// if next token is not . or (
		if (!nextIs(TokenType.SYMBOL, ".") && !nextIs(TokenType.SYMBOL, "("))	
		 {
			output += writeError("SYMBOL");	// write error to output file
		}

		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write . or ( to output file

		if (nextIs(TokenType.SYMBOL, ".")) 	// if next token is .
		{
			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) // if next token is not identifier
			{
				output += writeError("IDENTIFIER");	// write error to output file
			}
			
			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";	// write subroutine name to output file

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "("))	// if next token is not (
			 {
				output += writeError("SYMBOL");	// write error to output file
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write ( to output file
		}

		output += compileExpressionList();	// write expressionList to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) // if next token is not )
		{
			output += writeError("SYMBOL");	
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write ) to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) // if next token is not ;
		{
			output += writeError("SYMBOL");	
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write ; to output file

		return output;
	}

	// 'let' varName ('[' expression ']')? '=' expression ';'
	public String compileLet()
	 {
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, ""))// if next token is not identifier
		{
			output += writeError("IDENTIFIER"); // write error to output file
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";	// write varName to output file

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, "[")) 	// if next token is [
		{
			// write [ to output file and write expression to output file
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" + compileExpression();	

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "]")) // if next token is not ]
			{
				output += writeError("SYMBOL");	// write error to output file
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ] to output file

			tokenizer.advance();

		}


		if (!nextIs(TokenType.SYMBOL, "=")) // if next token is not =
		{
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write = to output file

		output += compileExpression();	// write expression to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) // if next token is not ;
		{
			output += writeError("SYMBOL");	// write error to output file
		}

		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ; to output file

		return output;
	}

	// 'while' '(' expression ')' '{' statements '}'
	public String compileWhile() 
	{
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "(")) // if next token is not (
		{
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ( to output file

		output += compileExpression();	// write expression to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")"))	// if next token is not )
		 {
			output += writeError("SYMBOL");	
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ) to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) 	// if next token is not {
		{
			output += writeError("SYMBOL");	
		}
		
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write { to output file

		output += compileStatements();	// write statements to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}"))	// if next token is not }
		{
			output += writeError("SYMBOL");	
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write } to output file

		return output;
	}

	// 'return' expression? ';'
	public String compileReturn() 
	{
		String output = "";

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, ";"))	// if next token is ;
		 {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ; to output file
			return output;
		}
		tokenizer.previousToken();// go back to previous token

		output += compileExpression();// write expression to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) // if next token is not ;
		{
			output += writeError("SYMBOL");	// write error to output file
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write ; to output file

		return output;

	}

	//'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
	public String compileIf() 
	{
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "("))	// if next token is not (
		 
		 {
			output += writeError("SYMBOL");	
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ( to output file

		output += compileExpression();	// write expression to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) // if next token is not )
		{
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ) to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{"))	// if next token is not {
		 
		 {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write { to output file

		output += compileStatements();	// write statements to output file

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) 	// if next token is not }
		{
			output += writeError("SYMBOL");	
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write } to output file

		tokenizer.advance();
		if (nextIs(TokenType.KEYWORD, Keyword.ELSE)) 
		{
			output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";	// write else to output file

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "{")) // if next token is not {
			{
				output += writeError("SYMBOL");
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write { to output file

			output += compileStatements();	// write statements to output file

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "}")) 	// if next token is not }
			{
				output += writeError("SYMBOL");	
			}
			output += "<symbol> " + tokenizer.symbol() + "</symbol>\n";	// write } to output file
		} 
		else// if next token is not else
		 {	
			tokenizer.previousToken();
		}

		return output;

	}

	//term (op term)*
	public String compileExpression()
	 {
		String output = "";
		output += compileTerm();// write term to output file

		tokenizer.advance();
		
		// while next token is symbol and symbol is +,-,*,/,&,|,<,>,=
		while (nextIs(TokenType.SYMBOL, "") && (tokenizer.symbol().matches("[\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\= ]")))		
		 {
			output += "<symbol> ";// write symbol to output file

			switch (tokenizer.symbol()) 	
			{
				case "<":
					output += "&lt;";
					break;
				case ">":
					output += "&gt;";
					break;
				case "&":
					output += "&amp'";
					break;
				default:
					output += tokenizer.symbol(); 	
			}

			output += " </symbol>\n";// write symbol end tag to output file

			output += compileTerm();// write term to output file
			tokenizer.advance();
		}
		tokenizer.previousToken();

		if(output.isEmpty())// if output is empty
		{
			return "";
		}
		
		return "<expression>\n" + output + "</expression>\n";// write expression to output file

	}

		/*
		*
		* integerConstant | stringConstant | keywordConstant | varName |
		* varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
		*/	
	public String compileTerm() 	
	{
		String output = "";

		tokenizer.advance();
		
		// if next token is integer constant, write integer constant to output file
		if (nextIs(TokenType.INT_CONST, "")) 
		{
			output += "<integer_constant> " + tokenizer.intVal() + " </integer_constant>\n";
		}
		
		// if next token is string constant, write string constant to output file
		else if (nextIs(TokenType.STRING_CONST, ""))
		{
			output += "<string_constant> " + tokenizer.stringVal() + " </string_constant>\n";
		}
		
		// if next token is keyword, write keyword to output file (keyword constants)
		else if (nextIs(TokenType.KEYWORD, "true") || nextIs(TokenType.KEYWORD, "false") || nextIs(TokenType.KEYWORD, "null") || nextIs(TokenType.KEYWORD, "this"))	
		{
			output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";	
		}
		//
		else if (nextIs(TokenType.SYMBOL, "(")) // if next token is (
		{
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ( to output file
			output += compileExpression();	// write expression to output file

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, ")")) // if next token is not )
			{
				output += writeError("SYMBOL");	
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ) to output file
		}
		
		else if (nextIs(TokenType.IDENTIFIER, "")) // if next token is identifier (for array variables)
		{
			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";// write identifier to output file

			tokenizer.advance();
			if (nextIs(TokenType.SYMBOL, "["))	// if next token is [
			 {

				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" + compileExpression();	// write [ to output file 
				tokenizer.advance();
				
				if (!nextIs(TokenType.SYMBOL, "]")) // if next token is not ]
				{
					output += writeError("SYMBOL");	
				}
				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ] to output file
			}
			//
			else if (nextIs(TokenType.SYMBOL, "(") || nextIs(TokenType.SYMBOL, ".")) // if next token is ( or .
			 {

				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write ( or . to output file

				if (nextIs(TokenType.SYMBOL, ".")) // if next token is .
				{
					tokenizer.advance();
					if (!nextIs(TokenType.IDENTIFIER, "")) // if next token is not identifier
					{
						output += writeError("IDENTIFIER");	
					}
					output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";// write identifier to output file

					tokenizer.advance();
					if (!nextIs(TokenType.SYMBOL, "("))	// if next token is not (
					 {
						output += writeError("SYMBOL");	
					}
					output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write ( to output file
				}

				output += compileExpressionList();	// write expression list to output file

				tokenizer.advance();
				if (!nextIs(TokenType.SYMBOL, ")")) 	// if next token is not )
				{
					output += writeError("SYMBOL");
				}
				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write ) to output file

			}
			 else 
			{
				tokenizer.previousToken();	// if next token is not ( or .	
			}

		}
		 else if (nextIs(TokenType.SYMBOL, "-") || nextIs(TokenType.SYMBOL, "~")) 	// if next token is - or ~
		 {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";// write - or ~ to output file
			output += compileTerm();	// write term to output file

		} 
		else 
		{
			tokenizer.previousToken();// if next token is not integer constant, string constant, keyword, (, -, or ~
		}
		
		if(output.isEmpty())// if output is empty
		{
			return "";
		}

		return "<term>\n" + output + "</term>\n";	// write term to output file and return output	
	}

	//(expression (','expression)*)?
	public String compileExpressionList() 
	{
		String output = "";

		output += compileExpression();	// write expression to output file

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, ",")) 	// if next token is ,
		{
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";	// write , to output file

			output += compileExpression();	// write expression to output file	
			tokenizer.advance();
		}
		tokenizer.previousToken();

		return "<expressionList>\n" + output + "</expressionList>\n";// write expression list to output file and return output	

	}
}