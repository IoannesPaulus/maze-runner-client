package com.github.ioannespaulus.mazerunner;

import java.awt.Dimension;
import java.awt.Point;
import java.io.StringReader;
import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.*;

import org.apache.log4j.Logger;

public class RESTMapExplorer implements MapExplorer {

	private String code;
	private URI url;
	private Point currentPos;
	private static final Logger LOG = Logger.getLogger(RESTMapExplorer.class);
	
	public RESTMapExplorer(String code, URI url) {
		this.code = code;
		this.url = url;
		setStartPos();
	}
	
	private void setStartPos() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url).path("/mazes/" + code + "/position/start");
		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
		try {
			Response response = invocationBuilder.get();
			LOG.debug("HTTP GET to " + target.getUri());
			int status = response.getStatus();
			LOG.debug("Response status code: " + status);
			LOG.debug("----------------------------------------");
			if (status == 404) {
				LOG.error("HTTP GET to " + target.getUri() + " returned: Maze " + code + " not found");
				currentPos = new Point(-1, -1);
			} else if (status > 400) {
				LOG.error("HTTP GET to " + target.getUri() + " returned: " + response.getStatusInfo().getReasonPhrase());
				currentPos = new Point(-1, -1);
			} else if (status == 200) {
				String respBody = response.readEntity(String.class);
				LOG.debug("Response body:");
				LOG.debug(respBody);
				LOG.debug("----------------------------------------");
				JsonReader rdr = Json.createReader(new StringReader(respBody));
				JsonObject obj = rdr.readObject();
				currentPos = new Point(obj.getInt("x"), obj.getInt("y"));
			} else
				currentPos = new Point(-1, -1);
		} catch (ProcessingException pe) {
			LOG.error("HTTP GET to " + target.getUri() + " failed");
			LOG.error(pe.getMessage());
			currentPos = new Point(-1, -1);
		} catch (Exception e) {
			LOG.error("An error occured while recieving response to HTTP GET to " + target.getUri());
			LOG.error(e.getMessage());
			currentPos = new Point(-1, -1);
		} finally {
			client.close();
		}
	}
	public Point getPos() {
		return currentPos;
	}
	public Point getStartPos() {
		return new Point(currentPos);
	}
	public Dimension getDimensions() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url).path("/mazes");
		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
		try {
			Response response = invocationBuilder.get();
			LOG.debug("HTTP GET to " + target.getUri());
			int status = response.getStatus();
			LOG.debug("Response status code: " + status);
			LOG.debug("----------------------------------------");
			if (status > 400)
				LOG.error("HTTP GET to " + target.getUri() + " returned: " + response.getStatusInfo().getReasonPhrase());
			else if (status == 200) {
				String respBody = response.readEntity(String.class);
				LOG.debug("Response body:");
				LOG.debug(respBody);
				LOG.debug("----------------------------------------");
				JsonReader rdr = Json.createReader(new StringReader(respBody));
				JsonObject obj = rdr.readObject();
				JsonArray results = obj.getJsonArray("mazes");
				for (JsonObject result : results.getValuesAs(JsonObject.class)) {
					if (result.getString("code").equals(code)) {
						Dimension dim = new Dimension(result.getInt("width"), result.getInt("height"));
						LOG.debug(code + " width: " + dim.width + ", height: " + dim.height);
						client.close();
						return dim;
					}
				}
				LOG.warn("No dimension information found for maze " + code);
			}
			client.close();
			return new Dimension(-1, -1);
		} catch (ProcessingException pe) {
			LOG.error("HTTP GET to " + target.getUri() + " failed");
			LOG.error(pe.getMessage());
			client.close();
			return new Dimension(-1, -1);
		} catch (Exception e) {
			LOG.error("An error occured while recieving response to HTTP GET to " + target.getUri());
			LOG.error(e.getMessage());
			client.close();
			return new Dimension(-1, -1);
		}
	}

	public NeighbourType lookAhead(int posX, int posY, Direction d) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(url).path("/mazes/" + code + "/position");
		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
		JsonObject neighbourRequest = Json.createObjectBuilder().add("from", Json.createObjectBuilder()
																.add("x", posX)
																.add("y", posY))
													.add("direction", d.toString())
													.build();
		try {
			Response response = invocationBuilder.post(Entity.json(neighbourRequest.toString()));
			LOG.debug("HTTP POST to " + target.getUri());
			int status = response.getStatus();
			LOG.debug("Response status code: " + status);
			LOG.debug("----------------------------------------");
			if (status == 418) {
				LOG.debug("Hit wall! Invalid move: " + d.toString() + " from (" + posX + ", " + posY + ")");
				client.close();
				return NeighbourType.WALL;
			} else if (status == 404)
				LOG.error("HTTP POST to " + target.getUri() + " returned: Maze " + code + " not found");
			else if (status > 400)
				LOG.error("HTTP POST to " + target.getUri() + " returned: " + response.getStatusInfo().getReasonPhrase());
			else if (status == 200) {
				LOG.debug("Attempted move: " + d.toString() + " from (" + posX + ", " + posY + ")");
				String respBody = response.readEntity(String.class);
				LOG.debug("Response body:");
				LOG.debug(respBody);
				LOG.debug("----------------------------------------");
				JsonReader rdr = Json.createReader(new StringReader(respBody));
				JsonObject obj = rdr.readObject();
				currentPos.setLocation(obj.getJsonObject("position").getInt("x"), obj.getJsonObject("position").getInt("y"));
				String field = obj.getString("field");
				if (field.equals(".")) {
					client.close();
					return NeighbourType.WAY;
				}
				else if (field.equals("x")) {
					client.close();
					return NeighbourType.EXIT;
				}
			}
			client.close();
			return NeighbourType.UNKNOWN;
		} catch (ResponseProcessingException rpe) {
			LOG.error("Processing response to HTTP POST to " + target.getUri() + " failed");
			LOG.error(rpe.getMessage());
			client.close();
			return NeighbourType.UNKNOWN;
		} catch (ProcessingException pe) {
			LOG.error("HTTP POST to " + target.getUri() + " failed");
			LOG.error(pe.getMessage());
			client.close();
			return NeighbourType.UNKNOWN;
		} catch (Exception e) {
			LOG.error("An error occured while recieving response to HTTP POST to " + target.getUri());
			LOG.error(e.getMessage());
			client.close();
			return NeighbourType.UNKNOWN;
		}
	}
}
