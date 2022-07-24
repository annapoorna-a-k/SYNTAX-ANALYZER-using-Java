package syntax_analyzer;

import java.util.*;

//regex is a Java API used to define a pattern for searching or manipulating strings.
//role: efficient pattern matching
import java.util.regex.Matcher; // used to match patterns in strings
import java.util.regex.Pattern; // used to define patterns in strings 
import java.io.*;

import java.nio.file.Path; // used to get path of file

import util.Keyword;
import util.TokenType;


//implements AutoCloseable to close input stream
public class JackTokenizer implements AutoCloseable 
{ 
	
	// regex for keywords
	public String keywordRegex = "class|method|function|constructor|int|boolean|char|void|var|static|field|let|do|if|else|while|return|true|false|null|this"; 
	// regex for symbols
	public String symbolRegex = "[\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|\\.|\\;|\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\=|\\~]";
	// regex for integers
	public String intRegex = "[0-9]+"; 
	// regex for strings
	public String stringRegex = "\"[^\"\n]*\""; 
	// regex for identifiers (any word character or starting with underscore)
	public String identifierRegex = "[\\w_]+"; 

	public BufferedReader input; 
	public ArrayList<String> tokens = new ArrayList<String>(); // list of tokens

	public String tokenType;
	public int currToken;

	
	// Constructing a JackTokenizer constructor, here the  path is the path of the file to be tokenized 
	public JackTokenizer(Path path) 
	{
		currToken = -1;

		String fileRaw = "";
		String line = "";

		try {
			// to create input stream, here path.toString() returns the path of the file as a string 
			input = new BufferedReader(new FileReader(path.toString())); 

			while ((line = input.readLine()) != null) // read line by line
			{
				//remove everything after (single-line comments)
				line = line.replaceAll("//.*", ""); 
				// add line to fileRaw, trim() removes leading and trailing whitespace
				fileRaw += line.trim();  
			}
			// add last character to fileRaw, read() returns the next character from the input stream or -1 if the end of the stream has been reached.
			fileRaw += String.valueOf((char) input.read()); 
		} 
		catch (IOException e) 
		{
			System.out.println("ERROR");
		}

		String commentless = removeBlockComments(fileRaw); // remove block comments 
		//System.out.println(commentless);

		//every possible token we are looking for
		// compile regex pattern - make it into one statement and create a matcher object which is used to check whether the tokens in the file are present in the tokenPattern
		Pattern tokenPattern = Pattern.compile(keywordRegex + "|" + symbolRegex + "|" + intRegex + "|" + stringRegex + "|" + identifierRegex); 
		Matcher tokenSpotter = (tokenPattern.matcher(commentless)); // create matcher object to match regex pattern to string
		
		//System.out.println(tokenPattern);
		//System.out.println(tokenSpotter);
		
		// Find all of the tokens - as long as the patterns match with the tokens
		while (tokenSpotter.find())  // find() returns true if the regular expression matches the string, otherwise it returns false.
		{
			tokens.add(tokenSpotter.group()); // add token to list of tokens
		}

	
	}

	// reset currToken to -1 ,used to reset tokenizer to beginning of file
	public void reset() 
	{
		currToken = -1;
	}
	

	// return index of current token (used in the writeError method in CompilationEngine)
	public int tokenIndex()
	 {
		return currToken;
	}

	// return true if there are more tokens
	public boolean hasMoreTokens() 
	{
		 // return true if currToken is less than size of tokens - 1
		return currToken < tokens.size() - 1;
	}
	

	public void advance() // advance to next token
	{
		// if there are more tokens
		if (hasMoreTokens()) 		
			currToken++;

		// get token at currToken
		String token = tokens.get(currToken); 		

		// if token is a keyword
		if (token.matches(keywordRegex))		
		 {
			// set tokenType to KEYWORD
			tokenType = TokenType.KEYWORD;	
			return;
		}

		if (token.matches(symbolRegex)) 
		{
			tokenType = TokenType.SYMBOL;
			return;
		}

		if (token.matches(stringRegex)) 
		{
			tokenType = TokenType.STRING_CONST;
			return;
		}

		if (token.matches(intRegex)) 
		{
			tokenType = TokenType.INT_CONST;
			return;
		}

		if (token.matches(identifierRegex))
		{
			tokenType = TokenType.IDENTIFIER;
			return;
		}

	}

	public void previousToken() // return to previous token
	{
		if (currToken > 0)	// if currToken is greater than 0
			currToken--;	// decrement currToken
		
		// get token at currToken
		String token = tokens.get(currToken); 

		if (token.matches(keywordRegex)) 
		{
			tokenType = TokenType.KEYWORD;
			return;
		}

		if (token.matches(symbolRegex))
		{
			tokenType = TokenType.SYMBOL;
			return;
		}

		if (token.matches(stringRegex)) 
		{
			tokenType = TokenType.STRING_CONST;
			return;
		}

		if (token.matches(intRegex)) 
		{
			tokenType = TokenType.INT_CONST;
			return;
		}

		if (token.matches(identifierRegex)) 
		{
			tokenType = TokenType.IDENTIFIER;
			return;
		}
	}

	// return tokenType
	public String tokenType() 	
	{
		return tokenType;
	}

	//to return keyword
    public String keyword() 	
	{
		String token = tokens.get(currToken);	// get token at currToken

		if (tokenType == TokenType.KEYWORD) // if tokenType is KEYWORD
		{	
			for (String k : Keyword.values()) // for each keyword in Keyword enum
			{	
				if (token.equalsIgnoreCase(k.toString()))// if token is equal to keyword
				{ 	
					return k;// return keyword
				}
			}
		}

		return "NOT A KEYWORD!";	// return NOTKEYWORD if token is not a keyword

	}

    //to return symbol
    public String symbol() 
    {
		String token = tokens.get(currToken);

		if (tokenType == TokenType.SYMBOL) 
		{
			return token;
		}

		return "NOT A SYMBOL!";
	}

     //to return identifier 
	public String identifier() 
	{			String token = tokens.get(currToken);

		if (tokenType == TokenType.IDENTIFIER) 
		{
			return token;
		}

		return "NOT AN IDENTIFIER!";
	}

	// if token is an int, return intVal
	public int intVal()
	{	
		String token = tokens.get(currToken);

		if (tokenType == TokenType.INT_CONST)
		{
			return Integer.parseInt(token);
		}

		return -1;
	}

	// if token is a string, return stringVal
	public String stringVal()
	{	
		String token = tokens.get(currToken);

		if (tokenType == TokenType.STRING_CONST) 
		{
			return token;
		}

		return "NOT A STRINGVAL!";
	}
	
	
	//to remove block comments
	public String removeBlockComments(String s) 
	{
		/** if string does not contain /**
		    return string   */
		if (!s.contains("/**")) 
			return s;

		String out = "";
		boolean commentMode = false;
		
		for (int i = 0; i < s.length() - 1; i++) // for each character in string
		{
			String commentTest = s.substring(i, i + 2); // get substring of string from i to i + 2
			if (!commentMode) // if commentMode is false
			{
				if (commentTest.equals("/*")) // if commentTest is /*
				{
					commentMode = true;
				} 
				else
				 {
					out += s.substring(i, i + 1); // add character to out 
				}
			}
			 else if (commentMode && commentTest.equals("*/")) // if commentMode is true and commentTest is */
			 {
				commentMode = false;
				// Skip over the comment notation
				i++;
			}

		}

		return out;

	}

	@Override 
	public void close() throws Exception 
	{
	}

}