package org.grandviewtech.userinterface.misc;

import org.grandviewtech.constants.Borders;
import org.grandviewtech.constants.PreferredDimension;
import org.grandviewtech.entity.bo.ClipBoard;
import org.grandviewtech.userinterface.helper.ColumnScreenGenerator;
import org.grandviewtech.userinterface.screen.ColumnScreen;

public class BaseBot implements Borders, PreferredDimension
	{
		
		protected static final ClipBoard	CLIP_BOARD	= ClipBoard.getInstance();
		
		protected static void pasteIndividualColumn(ColumnScreen source, ColumnScreen target)
			{
				target.setValue(source.getValue());
				target.setCoil(source.getCoil());
				ColumnScreenGenerator.createColumnNeighbourHood(ClipBoard.SCREEN.getRow(target.getRowNumber()), target);
				target.repaint();
			}
			
	}
