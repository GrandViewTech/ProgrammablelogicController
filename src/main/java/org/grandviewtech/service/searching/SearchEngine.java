package org.grandviewtech.service.searching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.grandviewtech.entity.bo.PlcFile;
import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class SearchEngine
	{
		private static Analyzer	standardAnalyzer	= new StandardAnalyzer();
		
		private static PlcFile	plcFile				= PlcFile.getInstance();
		private static String	fileLocation		= "indexes" + File.separator + plcFile.getFileName();
		
		public static void index(ColumnScreen columnScreen)
			{
				try
					{
						if (columnScreen == null || columnScreen.isBlank() == true)
							{
								return;
							}
						else
							{
								File file = new File(fileLocation);
								if (file.exists() == false)
									{
										file.mkdirs();
									}
								IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
								Directory directory = FSDirectory.open((new File(fileLocation)).toPath());
								IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
								
								try
									{
										indexWriter.deleteDocuments(new QueryParser("columnId", standardAnalyzer).parse("" + columnScreen.getRowNumber() + "." + columnScreen.getColumnNumber()));
										
										indexWriter.commit();
										Document document = new Document();
										document.add(new TextField("columnId", "" + columnScreen.getRowNumber() + "." + columnScreen.getColumnNumber(), Field.Store.YES));
										document.add(new TextField("rowNumber", "" + columnScreen.getRowNumber(), Field.Store.YES));
										document.add(new TextField("columnNumber", "" + columnScreen.getColumnNumber(), Field.Store.YES));
										document.add(new TextField("tag", columnScreen.getTag(), Field.Store.NO));
										document.add(new TextField("value", columnScreen.getValue(), Field.Store.NO));
										document.add(new TextField("coil", columnScreen.getCoil(), Field.Store.NO));
										indexWriter.addDocument(document);
										indexWriter.commit();
									}
								catch (Exception exception)
									{
										indexWriter.rollback();
										exception.printStackTrace();
									}
								finally
									{
										closeIndexWriter(directory, indexWriter);
									}
									
							}
					}
				catch (Exception exception)
					{
						exception.printStackTrace();
					}
			}
			
		public static List<SearchResult> search(String value)
			{
				try
					{
						BooleanQuery.Builder builder = new BooleanQuery.Builder();
						builder.add(new TermQuery(new Term("columnId", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("rowNumber", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("columnNumber", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("tag", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("value", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("coil", value)), Occur.SHOULD);
						return search(builder.build());
					}
				catch (IOException ioException)
					{
						ioException.printStackTrace();
					}
				catch (Exception exception)
					{
						exception.printStackTrace();
					}
				return new ArrayList<SearchResult>();
			}
			
		public static List<SearchResult> search(String key, String value)
			{
				try
					{
						Query query = new QueryParser(key, standardAnalyzer).parse(value);
						return search(query);
					}
				catch (org.apache.lucene.queryparser.classic.ParseException parseException)
					{
						parseException.printStackTrace();
					}
				catch (IOException ioException)
					{
						ioException.printStackTrace();
					}
				catch (Exception exception)
					{
						exception.printStackTrace();
					}
				return new ArrayList<SearchResult>();
			}
			
		private static List<SearchResult> search(Query query) throws IOException
			{
				List<SearchResult> results = new ArrayList<SearchResult>();
				Directory directory = FSDirectory.open((new File(fileLocation)).toPath());
				IndexReader indexReader = DirectoryReader.open(directory);
				IndexSearcher searcher = new IndexSearcher(indexReader);
				int hitsPerPage = 10;
				TopDocs docs = searcher.search(query, hitsPerPage);
				ScoreDoc[] hits = docs.scoreDocs;
				for (ScoreDoc scoreDoc : hits)
					{
						int documentId = scoreDoc.doc;
						Document document = searcher.doc(documentId);
						results.add(new SearchResult(document));
					}
				return results;
			}
			
		private static void closeIndexWriter(Directory directory, IndexWriter indexWriter)
			{
				try
					{
						if (indexWriter != null)
							{
								indexWriter.flush();
								indexWriter.close();
								indexWriter = null;
								Lock lock = directory.obtainLock(IndexWriter.WRITE_LOCK_NAME);
								lock.close();
								directory.close();
							}
					}
				catch (Exception exception)
					{
						exception.printStackTrace();
					}
			}
			
	}
