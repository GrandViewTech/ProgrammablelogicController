package org.grandviewtech.service.system;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class RoutineFileReader
	{
		private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RoutineFileReader.class);
		
		public static String getRoutineCode(String filePath)
			{
				String content = readContentFromFile(filePath);
				logger.info(content);
				return content;
			}
			
		private static String readContentFromFile(String filePath)
			{
				File file = new File(filePath);
				String filename = file.getName();
				String extension = FilenameUtils.getExtension(filename);
				String content = "";
				switch (extension)
					{
						case "txt":
							{
								content = readContentFromTextFile(file);
							}
					}
				return analyseContent(content);
			}
			
		private static String readContentFromTextFile(File file)
			{
				String content = "";
				try
					{
						content = FileUtils.readFileToString(file, "UTF-8");
					}
				catch (IOException ioException)
					{
						logger.error(ioException.getLocalizedMessage(), ioException);
					}
				return content;
			}
			
		private static String analyseContent(String content)
			{
				String analysedContext = content;
				return analysedContext;
			}
	}
