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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class HttpMultiPartUploaderImp {

	private static final String BOUNDARY = "xxxYYYxxx";
	private static final String LINE_FEED = "\n";
	private Charset charset = StandardCharsets.UTF_8;
	private OutputStream outputStream;
	private PrintWriter writer;

	private HttpMultiPartUploaderImp(HttpURLConnection httpUrlConnection) {
		try {
			tryToCreateHttpMultiPartUploaderImp(httpUrlConnection);
		} catch (IOException e) {
			throw new RuntimeException("Failed to upload multipart", e);
		}
	}

	private void tryToCreateHttpMultiPartUploaderImp(HttpURLConnection httpUrlConnection)
			throws IOException {
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setDoOutput(true);
		httpUrlConnection.setDoInput(true);
		httpUrlConnection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + BOUNDARY);
		httpUrlConnection.setRequestProperty("User-Agent", "CodeJava Agent");

		outputStream = httpUrlConnection.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
	}

	public static HttpMultiPartUploaderImp usingURLConnection(HttpURLConnection httpUrlConnection) {
		return new HttpMultiPartUploaderImp(httpUrlConnection);
	}

	public void addFormField(String name, String value) {
		writer.append("--" + BOUNDARY).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	public void addFilePart(String fieldName, String fileName, InputStream stream)
			throws IOException {
		writer.append("--" + BOUNDARY).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\""
				+ fileName + "\"").append(LINE_FEED);
		writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
				.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = stream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		stream.close();
		writer.flush();
	}

	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}

	public void done() throws IOException {
		writer.append(LINE_FEED).flush();
		writer.append("--" + BOUNDARY + "--").append(LINE_FEED);
		writer.close();
	}

}
