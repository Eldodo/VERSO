package verso.representation.cubeLandscape.Lucene;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import verso.representation.cubeLandscape.filter.SearchFilter;

public class LuceneWriteIndexFilter {
	
	protected String DOCS_PATH;
	protected String INDEX_PATH;
	private static final String keywords[] = { "abstract", "assert", "boolean",
             "break", "byte", "case", "catch", "char", "class", "const",
             "continue", "default", "do", "double", "else", "extends", "false",
             "final", "finally", "float", "for", "goto", "if", "implements",
             "import", "instanceof", "int", "interface", "long", "native",
             "new", "null", "package", "private", "protected", "public",
             "return", "short", "static", "strictfp", "super", "switch",
             "synchronized", "this", "throw", "throws", "transient", "true",
             "try", "void", "volatile", "while" };
	
	private PerFieldAnalyzerWrapper analyzer;

	public LuceneWriteIndexFilter(String index) {
		this(index, false);
	}
	
	public LuceneWriteIndexFilter(String index, boolean reindex) {
		 //Input folder
        DOCS_PATH = index+"\\src";
         
        //Output folder
        INDEX_PATH = index+"\\indexedFiles";
 
        //Input Path Variable
        final Path docDir = Paths.get(DOCS_PATH);
        
        try {
            //org.apache.lucene.store.Directory instance
        	boolean ex = new File(INDEX_PATH).exists();
        	

            Directory dir = FSDirectory.open( Paths.get(INDEX_PATH) );
            
            
            
            CharArraySet stopWords = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
            stopWords.addAll(Arrays.asList(keywords));
            
            System.out.println(stopWords);
            
            //analyzer with the default stop words
            //Analyzer analyzer = new StandardAnalyzer(stopWords);
            /*
            * Pour n'avoir que la racine des mots, seulement, le soucis c'est que pour walked, il faudra taper
            * "walk" pour le trouver... et si on tape le mot "walked" il ne trouve rien.. il faudrait avoir la
            * racine + le mot entier ?
            */
            //Analyzer analyzer = new StandardAnalyzer(stopWords);
            
            Map<String,Analyzer> analyzerList = new HashMap<String,Analyzer>();
            analyzerList.put("stemmedText", new EnglishAnalyzer(stopWords));
            analyzerList.put("unstemmedText", new StandardAnalyzer(stopWords));
            analyzer = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(stopWords), analyzerList);
            
            //IndexWriter Configuration
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if(reindex)
            	iwc.setOpenMode(OpenMode.CREATE);
            else iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            
             
            //IndexWriter writes new index files to the directory
            
            IndexWriter writer = new IndexWriter(dir, iwc);
            
            if(!ex || reindex)
            	indexDocs(writer, docDir);
 
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch(ClassNotFoundException e) {
        	e.printStackTrace();
        }
	}
	
	protected static void indexDocs(final IndexWriter writer, Path path) throws IOException, ClassNotFoundException{
        //Directory?
        if (Files.isDirectory(path)){
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
                    try {
                        //Index this file
                    	//System.out.println("On index: "+file.toString());
                    	if(file.toAbsolutePath().toString().contains(".java"))
                    		indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    }
                    catch (IOException ioe){
                    	System.out.println("For the file: "+file.toAbsolutePath().toString());
                        ioe.printStackTrace();
                    }
                    catch (ClassNotFoundException e) {
                    	e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else{
            //Index this file
        	//System.out.println("On index: "+path.toString());
        	if(path.toAbsolutePath().toString().contains(".java"))
				indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }
 
    protected static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException, ClassNotFoundException {
        try (InputStream stream = Files.newInputStream(file)){
            //Create lucene Document
            
            
            //INITIALISATION

            //Chemin du fichier
            String path = file.toString();
            //Le nom entier avec package
            String allname = path.substring(path.indexOf("\\src\\")+5).replaceAll("\\\\", ".").replace(".java", "");
            //Le nom du fichier seul
            String nameFile = file.getFileName().toString().replace(".java", "");
            //Fichier entier
            String allFile = new String(Files.readAllBytes(file));
            //Sans commentaires
            String withoutComments = removeComments(allFile);
            
            //On récupère le nom des classes
            ArrayList<String> classNames = getClassName(withoutComments);
            
            //Si c'est une classe
            if(classNames.size()>0) {
            	for(String mClassName : classNames) {
                	String mPath = allname;
                	String mSourceCode = allFile;
                	ArrayList<String> methodNames = new ArrayList<String>();
                	//Si c'est une classe interne
                	if(!mClassName.equals(nameFile)) {
                		mPath+="#"+mClassName;
                		mSourceCode = getSourceCodeOfClass(withoutComments, mClassName);
                	}
                	//On récupère le nom des méthodes du code source de la classe
                	methodNames = getMethodName(mSourceCode);
                	Document doc = indexIt(mPath, mClassName, mSourceCode, arrayListToString(methodNames), lastModified, writer);
                	//System.out.println("Name : "+mClassName);
                	writer.updateDocument(new Term("path", file.toString()), doc);
                }
            }
            //Si c'est autre chose
            else {
            	System.out.println("Other : "+allname);
            	throw new ClassNotFoundException("Error with "+allname+".\nNo class/interface/enum found.");
            }
            
            
        }
    }
    
    private static Document indexIt(String path, String name, String code, String methodNames, long lastModified, IndexWriter writer) {
    	Document doc = new Document();
    	doc.add(new StringField("path", path, Field.Store.YES));
        doc.add(new LongPoint("modified", lastModified));
        doc.add(new TextField(SearchFilter.ALL_FILE, code, Field.Store.YES));
        doc.add(new TextField(SearchFilter.CLASS_NAME, name, Field.Store.YES));
        doc.add(new TextField(SearchFilter.METHOD_NAME, methodNames, Field.Store.YES));
        doc.add(new TextField(SearchFilter.ALL_NAME, name+" "+methodNames, Store.YES));
        return doc;
    }
    
    /**
     * Get the source code of a class, for exemple if you have intern class
     * @param allFile : Entire source code
     * @param className : The name of the class you need the source code
     * @return The source code of the class
     */
    private static String getSourceCodeOfClass(String allFile, String className) {
    	String sourceCode = allFile;
    	int begin = sourceCode.indexOf("{", sourceCode.indexOf("class "+className))+1;
    	int end=begin;
    	int cpt=1;
    	//System.out.println("class "+className);
    	//if(cpt<5)System.out.println("{="+sourceCode.indexOf("{", end)+" - }="+sourceCode.indexOf("}", end)+" - cpt="+cpt+" - end="+end);
    	while(cpt!=0) {
    		
    		if(sourceCode.indexOf("{", end)<sourceCode.indexOf("}", end) && sourceCode.indexOf("{", end)>0) {
    			end = sourceCode.indexOf("{", end)+1;
    			cpt++;
    		}
    		else {
    			end = sourceCode.indexOf("}", end)+1;
    			cpt--;
    		}
    		//if(cpt<5)System.out.println("{="+sourceCode.indexOf("{", end)+" - }="+sourceCode.indexOf("}", end)+" - cpt="+cpt+" - end="+end);
    		//System.out.println(sourceCode.substring(end-10, end));
    	}
    	sourceCode = sourceCode.substring(begin, end);
    	return sourceCode;
    }

    /**
     * Get a list of all the methodnames of a source code given in parameters
     * @param s : source code
     * @return List of methodname
     * */
    private static ArrayList<String> getMethodName(String s) {
    	ArrayList<String> ret = new ArrayList<String>();
    	
    	//On définit l'expression régulière de la déclaration d'une méthode
    	Pattern pattern = Pattern.compile("(public|private|protected|static|final|native|synchronized|abstract|transient) (\\w)+ (\\w)+( )*\\(");
        Matcher matcher = pattern.matcher(s);
        //Pour tous les matchs, on récupère le nom
        while (matcher.find()) {
        	
        	String tmpName = matcher.group(0);
        	tmpName = tmpName.replaceAll("( )*\\(", "");
        	tmpName = tmpName.replaceAll("\n", "");
        	
        	if(!tmpName.equals("")) {
	        	String[] tmpNames = tmpName.split(" ");
	        	String methodName = tmpNames[tmpNames.length-1];
	        	String nameTokkenized = methodName;
	        	
	        	ArrayList<Integer> w = new ArrayList<Integer>();
	        	w.add(0);
	        	//Egalement on tokkenise le nom de méthode : ex: getMethodName = getMethodName + get + Method + Name
	        	for(int i = 0 ; i< methodName.length() ; i++) {
	        		if(Character.isUpperCase(methodName.charAt(i))) {
	        			if(!w.contains(i)) w.add(i);
	        		}
	        	}
	        	if(!w.contains(methodName.length())) w.add(methodName.length());
	        	for(int i=0; i<w.size()-1; i++) {
	        		nameTokkenized+=" "+methodName.substring(w.get(i), w.get(i+1));
	        	}
	        	//System.out.println(nameTokkenized);
	        	ret.add(nameTokkenized);
        	}
        }
        
    	return ret;
    }
    

    
    private static String removeQuotes(String s) {
    	return s.replaceAll("((?<![\\\\])['\"])((?:.(?!(?<![\\\\])\\1))*.?)\\1", "");
    }
    
    /**
     * Get a list of all the classnames of a source code given in parameters
     * @param s : source code
     * @return List of classname
     * */
    private static ArrayList<String> getClassName(String s) {
    	//String sSansQuote = removeQuotes(s);
    	ArrayList<String> ret = new ArrayList<String>();
    	//Ensuite on cherche les class
    	//Pattern pattern = Pattern.compile("(?m)^((?!static).)*\\b(class|enum|interface) ([a-zA-Z0-9'])+");
    	Pattern pattern = Pattern.compile("(class|enum|interface) ([a-zA-Z0-9'])+");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
        	String name = matcher.group(0).replaceAll(".*(class|interface|enum) ", "");
        	if(!ret.contains(name))ret.add(name);
        }
    	return ret;
    }
   
	private static String arrayListToString(ArrayList<String> array) {
		String ret="";
		for(int i=0 ; i<array.size() ; i++) {
			if(i!=0) ret+= " ";
			ret+=array.get(i);
		}
		return ret;
	}

	private static String removeComments(String s) {
		return removeQuotes(s).replaceAll("//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "");
	}
	
	/**
	 * Get the lucene analyzer
	 * @return analyser
	 */
	public PerFieldAnalyzerWrapper getAnalyzer() {
		return this.analyzer;
	}
	
	
 /*
  * 
  */

}
