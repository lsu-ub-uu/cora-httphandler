/*
 * Copyright 2016, 2023 Uppsala University Library
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

/**
 * HttpHandlerFactory is used to factor {@link HttpHandler}s to do http requests. Each factored
 * HttpHandler or HttpMultiPartUploader is expected to only do a single request, and new handlers be
 * factored for each request.
 */
public interface HttpHandlerFactory {
	/**
	 * factor factors a {@link HttpHandler} to use for a normal http request.
	 * 
	 * @param url
	 *            A String with the URL to do a request to
	 * @return A HttpHandler
	 */
	HttpHandler factor(String url);

	/**
	 * factorHttpMulitPartUploader factors a {@link HttpMultiPartUploader} to use for a multipart
	 * http request.
	 * 
	 * @param url
	 *            A String with the URL to do a request to
	 * @return A HttpMultiPartUploader
	 */
	HttpMultiPartUploader factorHttpMultiPartUploader(String url);
}
