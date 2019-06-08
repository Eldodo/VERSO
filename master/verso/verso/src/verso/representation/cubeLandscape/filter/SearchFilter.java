package verso.representation.cubeLandscape.filter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import verso.representation.cubeLandscape.Lucene.LuceneReadIndexFilter;
import verso.representation.cubeLandscape.Lucene.LuceneWriteIndexFilter;
import verso.representation.cubeLandscape.representationModel.SystemRepresentation;

public class SearchFilter {
	public static final int TFIDF = 1;
	public static final int LSI = 0;
	
	
	public static final String ALL_FILE = "allFile";
	public static final String CLASS_NAME = "className";
	public static final String METHOD_NAME = "methodName";
	public static final String ALL_NAME = "allName";
	public static final String ONLY_COMMENTS = "onlyComments";
	public static final String WITHOUT_COMMENTS = "withoutComments";
	
	private static ArrayList<ScoreDoc> results;
	private static ArrayList<String> resultsPath;
	private static IndexSearcher searcher;
	
	private static LuceneWriteIndexFilter mWriter;
	private static LuceneReadIndexFilter mReader;
	
	private static SearchFilter INSTANCE=null;

	private SearchFilter(String path) {
		mWriter = new LuceneWriteIndexFilter(path);
		mReader = new LuceneReadIndexFilter(path, mWriter.getAnalyzer(),  SearchFilter.TFIDF);
		searcher = mReader.getIndexSearcher();
		results = new ArrayList<ScoreDoc>();
		resultsPath = new ArrayList<String>();
		System.out.println("on refait");
	}
	
	public static ArrayList<String> getResultsPath(){
		return resultsPath;
	}
	
	public static SearchFilter getInstance(String path) {
		if(INSTANCE==null) {
			INSTANCE = new SearchFilter(path);
		}
		return INSTANCE;
	}
	
	/**
	 * Reset the indexation phase
	 * @param path 
	 */
	public static void resetIndex(String path) {
		File indexFolder = new File(path+"\\indexedFiles");
		mWriter = new LuceneWriteIndexFilter(path, true);
		mReader = new LuceneReadIndexFilter(path, mWriter.getAnalyzer(),  SearchFilter.TFIDF);
		searcher = mReader.getIndexSearcher();
	}
	
	/**
	 * Make a research. This method initiate the results.
	 * @param content The content you are looking for
	 * @param level The level of your research : Plain text, method/class names etc..
	 */
	public void search(String content, String level) {
		results = new ArrayList<ScoreDoc>();
		resultsPath = new ArrayList<String>();
		TopDocs res = mReader.search(content, level);
		try {
			results = toArrayList(res);
			for (ScoreDoc sd : results){
				Document d = searcher.doc(sd.doc);
				String path = d.get("path");
				resultsPath.add(path);
			}
		} catch(Exception e) {e.printStackTrace();}
	}
	
	
	/**
	 * Show the result in String.
	 * File : -path-, Score: -score- ...
	 */
	public String toString() {
		String ret = "";
		try {
			ret+="Nombre de resultats: "+results.size()+"\n";
			System.out.println(ret);
			for (ScoreDoc sd : results){
				Document d = searcher.doc(sd.doc);
//				String pathResult = d.get("path");
				System.out.println("File : "+ d.get("path") + ", Score : " + sd.score+"\n"+d.get(SearchFilter.METHOD_NAME));
				ret+="File : "+ d.get("path") + ", Score : " + sd.score+"\n";
			}
		}
		catch(Exception e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 * Convert TopDocs to ArrayList<ScoreDoc>. Please read Lucene API to learn more about TopDocs and ScoreDoc classes.
	 * @param docs TopDocs result
	 * @return the result in ScoreDoc Array
	 */
	private ArrayList<ScoreDoc> toArrayList(TopDocs docs){
		ArrayList<ScoreDoc> ret = new ArrayList<ScoreDoc>();
		for (ScoreDoc sd : docs.scoreDocs){
			//if(notAlreadyAdded(sd, ret))
				ret.add(sd);
		}
		return ret;
	}
	
}
