package org.grandviewtech.service.searching;

/*
 * #%L
 * Programmable Login Controller Inteface
 * %%
 * Copyright (C) 2016 GrandViewTech
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.grandviewtech.entity.bo.SearchResult;
import org.grandviewtech.entity.enums.Edge;
import org.grandviewtech.entity.enums.NoNc;
import org.grandviewtech.service.system.SystemFileLocation;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class SearchEngine
	{
		private static org.apache.log4j.Logger	logger				= org.apache.log4j.Logger.getLogger(SearchEngine.class);
		
		private static Analyzer					standardAnalyzer	= new StandardAnalyzer();
		
		private static String					fileLocation		= SystemFileLocation.INDEX_FILE_LOCATION;
		
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
										if (columnScreen.isBlank() == false)
											{
												Document document = new Document();
												document.add(new TextField("columnId", "" + columnScreen.getRowNumber() + "." + columnScreen.getColumnNumber(), Field.Store.NO));
												document.add(new TextField("rowNumber", "" + columnScreen.getRowNumber(), Field.Store.YES));
												document.add(new TextField("columnNumber", "" + columnScreen.getColumnNumber(), Field.Store.YES));
												document.add(new TextField("tag", columnScreen.getTag(), Field.Store.NO));
												document.add(new TextField("value", columnScreen.getValue(), Field.Store.NO));
												document.add(new TextField("coil", columnScreen.getCoilType().getCoilType(), Field.Store.NO));
												if (columnScreen.getNonc() != null && !columnScreen.getNonc().equals(NoNc.DEFAULT))
													{
														document.add(new TextField("nonc", columnScreen.getNonc().name(), Field.Store.NO));
													}
												if (columnScreen.getEdge() != null && !columnScreen.getEdge().equals(Edge.DEFAULT))
													{
														document.add(new TextField("edge", columnScreen.getEdge().name(), Field.Store.NO));
													}
												indexWriter.addDocument(document);
												indexWriter.commit();
											}
									}
								catch (Exception exception)
									{
										indexWriter.rollback();
										logger.error(exception.getLocalizedMessage(), exception);
									}
								finally
									{
										closeIndexWriter(directory, indexWriter);
									}
									
							}
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
		public static List<SearchResult> search(String value)
			{
				List<SearchResult> result = new ArrayList<SearchResult>();
				;
				try
					{
						value = value.trim().toLowerCase();
						BooleanQuery.Builder builder = new BooleanQuery.Builder();
						builder.add(new TermQuery(new Term("columnId", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("rowNumber", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("columnNumber", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("tag", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("value", value)), Occur.SHOULD);
						builder.add(new TermQuery(new Term("coil", value)), Occur.SHOULD);
						result = search(builder.build());
						result.sort(new Comparator<SearchResult>()
							{
								@Override
								public int compare(SearchResult object1, SearchResult object2)
									{
										return object1.compareTo(object2);
									}
							});
					}
				catch (org.apache.lucene.index.IndexNotFoundException indexNotFoundException)
					{
						logger.error("Index not Found");
					}
				catch (IOException ioException)
					{
						logger.error(ioException.getLocalizedMessage(), ioException);
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
				return result;
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
						logger.error(parseException.getLocalizedMessage(), parseException);
					}
				catch (IOException ioException)
					{
						logger.error(ioException.getLocalizedMessage(), ioException);
					}
				catch (Exception exception)
					{
						logger.error(exception.getLocalizedMessage(), exception);
					}
				return new ArrayList<SearchResult>();
			}
			
		private static List<SearchResult> search(Query query) throws IOException
			{
				List<SearchResult> results = new ArrayList<SearchResult>();
				try
					{
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
							
					}
				catch (org.apache.lucene.index.IndexNotFoundException indexNotFoundException)
					{
						logger.error(indexNotFoundException.getLocalizedMessage());
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
						logger.error(exception.getLocalizedMessage(), exception);
					}
			}
			
	}
