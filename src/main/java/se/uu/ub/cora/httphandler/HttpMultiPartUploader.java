/*
 * Copyright 2016 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.httphandler;

import java.io.IOException;
import java.io.InputStream;

public interface HttpMultiPartUploader {

	String getResponseText();

	int getResponseCode();

	String getErrorText();

	void addFormField(String name, String value);

	void addFilePart(String fieldName, String fileName, InputStream stream) throws IOException;

	/**
	 * Add the header to the connection
	 * 
	 * @param name
	 *            It is the name of the header
	 * @param value
	 *            It is the value of the header
	 */

	void addHeaderField(String name, String value);

	void done() throws IOException;

}