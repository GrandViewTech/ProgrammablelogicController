package org.grandviewtech.constants;

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

import javax.swing.ImageIcon;

public interface Icons
	{
		static ImageIcon	NEW		= new ImageIcon("icons" + File.separator + "new.png");
		static ImageIcon	DELETE	= new ImageIcon("icons" + File.separator + "delete.png");
		static ImageIcon	COPY	= new ImageIcon("icons" + File.separator + "copy.png");
		static ImageIcon	PASTE	= new ImageIcon("icons" + File.separator + "paste.png");
		static ImageIcon	CUT		= new ImageIcon("icons" + File.separator + "cut.png");
		static ImageIcon	SETTING	= new ImageIcon("icons" + File.separator + "setting.png");
		static ImageIcon	EDIT	= new ImageIcon("icons" + File.separator + "edit.png");
	}
