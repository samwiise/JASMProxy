/**
 * 
 */
package org.asmmr.jasmproxy;

import org.asmmr.http.HttpResponseMessage;

/**
 * @author asim.ali
 *
 */
public interface IHttpRequestHandler {

	public void sendErrorToClient(String message);
	public void sendResponseToClient(HttpResponseMessage response);
	public void sendResponseToClient(String message);
}
