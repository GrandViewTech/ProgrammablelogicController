package org.grandviewtech.service.system;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class RoutineFileReader
	{
		private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RoutineFileReader.class);
		
		public static Object[] getRoutineCode(String filePath)
			{
				Object[] content = readContentFromFile(filePath);
				logger.info("Content : " + (String) content[0]);
				logger.info("Variable Count : " + content[1]);
				logger.info("Constant Count : " + content[2]);
				return content;
			}
			
		private static Object[] readContentFromFile(String filePath)
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
								break;
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
			
		private static Object[] analyseContent(String content)
			{
				String analysedContext = content;
				analysedContext = StringUtils.normalizeSpace(analysedContext);
				return new Object[]
					{ analysedContext, StringUtils.countMatches(analysedContext, PropertyReader.getProperties("varivableKeyword")), StringUtils.countMatches(analysedContext, PropertyReader.getProperties("constantKeyword")) };
			}
	}
