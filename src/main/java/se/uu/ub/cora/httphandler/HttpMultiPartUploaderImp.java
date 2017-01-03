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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class HttpMultiPartUploaderImp {

	private static final int INITIAL_BUFFER_SIZE = 4096;
	private static final int STATUS_INTERNAL_SERVER_ERROR = 500;
	private HttpURLConnection urlConnection;

	private static final String BOUNDARY = "xxxYYYxxx";
	private static final String LINE_FEED = "\n";
	private Charset charset = StandardCharsets.UTF_8;
	private OutputStream outputStream;
	private PrintWriter writer;

	private HttpMultiPartUploaderImp(HttpURLConnection httpUrlConnection) {
		try {
			this.urlConnection = httpUrlConnection;
			this.urlConnection.setUseCaches(false);
			this.urlConnection.setDoOutput(true);
			this.urlConnection.setDoInput(true);
			this.urlConnection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);
			this.urlConnection.setRequestProperty("User-Agent", "CodeJava Agent");

			outputStream = this.urlConnection.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
		} catch (IOException e) {
			throw new RuntimeException("Failed to upload multipart", e);
		}
	}

	public static HttpMultiPartUploaderImp usingURLConnection(HttpURLConnection httpUrlConnection) {
		return new HttpMultiPartUploaderImp(httpUrlConnection);
	}

	public String getResponseText() {
		try {
			return tryToGetResponseText();
		} catch (Exception e) {
			throw new RuntimeException("Error getting response text: ", e);
		}
	}

	private String tryToGetResponseText() throws IOException {
		InputStream inputStream = urlConnection.getInputStream();
		return getTextFromInputStream(inputStream);
	}

	private String getTextFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	public int getResponseCode() {
		try {
			return urlConnection.getResponseCode();
		} catch (IOException e) {
			return STATUS_INTERNAL_SERVER_ERROR;
		}
	}

	public String getErrorText() {
		try {
			return tryToGetErrorText();
		} catch (Exception e) {
			throw new RuntimeException("Error getting response text: ", e);
		}
	}

	private String tryToGetErrorText() throws IOException {
		InputStream inputStream = urlConnection.getErrorStream();
		return getTextFromInputStream(inputStream);
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

		byte[] buffer = new byte[INITIAL_BUFFER_SIZE];
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
