package syntax_analyzer;

import java.util.*;
import java.io.*;


public class JackAnalyzer
{ 

	public static void main(String[] args) 
	{
		ArrayList<File> jackFiles = new ArrayList<File>(); // list of jack files
		
		//inputFile = new File(filePath);
		File inputFile = new File("C:\\Users\\ANNAPOORNA\\Desktop\\ANNAPOORNA AK_AM.EN.U4AIE21114\\nand2tetris\\projects\\10\\Square");
		
		System.out.println("The converted files are:");

		if (inputFile.isDirectory()) //if the path given is a directory's
		{
			// Recursively look for .jack files
			File[] dirChildren = inputFile.listFiles(); // get all files in the directory

			if (dirChildren.length > 0) 
			{
				for (File f : dirChildren) 
				{
					if (f.getName().endsWith(".jack")) // if file is a .jack file
						jackFiles.add(f); // add to list of jack files
				}
			} 
			else 
			{
				System.out.println("This directory is empty!");
				System.exit(0); // exit if directory is empty
			}

		} 
		else if (inputFile.isFile()) 
		{
			jackFiles.add(inputFile);
		}

		// compiler object
		CompilationEngine compiler; 

		// for each jack file
		for (File f : jackFiles)
		{ 
			// create tokenizer object
			try (JackTokenizer tokenizer = new JackTokenizer(f.toPath()))
			{ 
				
				String filename = f.getName().replaceAll("\\..*", ""); //to get filename without extension
				//System.out.println(filename);
				String dir = f.getParent().toString(); // get directory
				//System.out.println(dir);

				System.out.println(dir + "/" + filename);
				
				FileOutputStream fileOut = new FileOutputStream(dir + "/" + filename + "Completed.xml");
				FileOutputStream fileTokens = new FileOutputStream(dir + "/" + filename + "TokensOnly.xml");
				
				compiler = new CompilationEngine(tokenizer, fileOut, fileTokens); // create compiler object
				compiler.compileClass(); // compile class
				
			}
			catch (Exception e)
			{
				System.out.println("ERROR!");
			}

		}

	}
}