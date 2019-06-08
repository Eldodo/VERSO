package verso.representation.cubeLandscape.Lucene;

import java.awt.Dimension;
import java.awt.Label;
import java.awt.TextArea;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import verso.representation.cubeLandscape.filter.SearchFilter;

public class LuceneReadIndexFilter {
	private static int currentSimilarity = -1;
	private static String currentLevel = "";
	private static PerFieldAnalyzerWrapper analyzer;
	
	private static String INDEX_DIR=null;
	private IndexSearcher searcher;
	private ArrayList<String> fileNameExceptions;
	

	public LuceneReadIndexFilter(String index, PerFieldAnalyzerWrapper a) {
		this(index, a, -1);
	}
	
	public LuceneReadIndexFilter(String index,PerFieldAnalyzerWrapper a, int similarity) {
		this.analyzer = a;
		currentSimilarity = similarity;
		INDEX_DIR = index+"\\indexedFiles";
		//Create lucene searcher. It search over a single IndexReader.
		try{
			searcher = createSearcher();
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	public TopDocs search(String content, String level) {
		currentLevel = level;
		TopDocs ret = null;
		try {
			ret = searchInContent(content, searcher);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
		
	private static TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception {
		//Create search query

		QueryParser qp = new QueryParser(currentLevel, analyzer);
		
		Query query = qp.parse(textToFind);
		 
		//search the index
		TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
		return hits;
	}
	 
	private static IndexSearcher createSearcher() throws IOException{
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		         
		//It is an interface for accessing a point-in-time view of a lucene index
		IndexReader reader = DirectoryReader.open(dir);
		 
		//Index searcher
		IndexSearcher searcher = new IndexSearcher(reader);
		if(currentSimilarity==SearchFilter.TFIDF) {
			TFIDFSimilarity similarity = new ClassicSimilarity();
			searcher.setSimilarity(similarity);
		}
		else if(currentSimilarity==SearchFilter.LSI) {
			
		}
		return searcher;
	}
	
	public IndexSearcher getIndexSearcher() {
		return this.searcher;
	}
	
}
