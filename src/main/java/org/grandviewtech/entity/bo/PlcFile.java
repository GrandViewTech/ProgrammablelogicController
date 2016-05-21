package org.grandviewtech.entity.bo;

final public class PlcFile
	{
		private PlcFile()
			{
			};
			
		private final static class PlCFileInstanceFactory
			{
				static PlcFile instance = new PlcFile();
			}
			
		public final static PlcFile getInstance()
			{
				return PlCFileInstanceFactory.instance;
			}
			
		private String fileName;
		
		public String getFileName()
			{
				return fileName;
			}
			
		public void setFileName(String fileName)
			{
				this.fileName = fileName;
			}
			
	}
