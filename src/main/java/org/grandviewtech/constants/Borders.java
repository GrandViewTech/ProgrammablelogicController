package org.grandviewtech.constants;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.grandviewtech.userinterface.helper.CustomBorder;

public interface Borders
	{
		Border	PADDING					= BorderFactory.createEmptyBorder(2, 5, 5, 2);
		Border	WHITE_BORDER			= BorderFactory.createLineBorder(Color.white);
		Border	PADDED_DEFAULT			= BorderFactory.createCompoundBorder(WHITE_BORDER, PADDING);
		Border	DASHED_BORDER			= BorderFactory.createDashedBorder(java.awt.Color.BLUE);
		Border	DASHED_BORDER_RED			= BorderFactory.createDashedBorder(java.awt.Color.RED);
		Border	PADDED_DASHED_BORDER	= BorderFactory.createCompoundBorder(DASHED_BORDER, PADDING);
		Border	CUSTOM_BORDER			= new CustomBorder();
	}
