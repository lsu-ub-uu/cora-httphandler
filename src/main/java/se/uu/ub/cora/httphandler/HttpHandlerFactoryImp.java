/*
 * Copyright 2016, 2018, 2023 Uppsala University Library
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;

import se.uu.ub.cora.httphandler.internal.HttpHandlerImp;
import se.uu.ub.cora.httphandler.internal.HttpMultiPartUploaderImp;

public class HttpHandlerFactoryImp implements HttpHandlerFactory {

	@Override
	public HttpHandler factor(String urlString) {
		try {
			return tryToFactor(urlString);
		} catch (Exception e) {
			throw new RuntimeException("Error factoring HttpHandler: ", e);
		}
	}

	private HttpHandler tryToFactor(String urlString) throws IOException {
		Builder builder = HttpRequest.newBuilder().uri(URI.create(urlString));
		HttpClient httpClient = HttpClient.newHttpClient();
		return HttpHandlerImp.usingBuilderAndHttpClient(builder, httpClient);
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String urlString) {
		try {
			return tryToFactorHttpMultiPartUploader(urlString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private HttpMultiPartUploader tryToFactorHttpMultiPartUploader(String urlString)
			throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		return HttpMultiPartUploaderImp.usingURLConnection(urlConnection);
	}
}
