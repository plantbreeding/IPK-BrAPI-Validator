package de.ipk_gatersleben.bit.bi.bridge.brapicomp.apiresources;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Provider;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.ResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.JsonMessageManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.ApiResourceService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RunnerService;

/**
 * Currently groups the Administrator related queries.
 * Mostly running tests on endpoints stored in the database.
 * Error codes: X0XX
 */
@Path("/admin")
@RequestScoped
public class AdminResource {

    private static final Logger LOGGER = LogManager.getLogger(AdminResource.class.getName());

    /**
     * Register new public endpoint
     *
     * @param res Json containing the endpoint's url.
     * @return Json message.
     */
    @POST
    @Path("/resources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEndpoint(@Context HttpHeaders headers,
                                   Resource res) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New POST /admin/resources call.");
        
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);

        try {
        	
        	//Check auth header
            if (!auth(headers)) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4001);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }
        	
        	res.setEmail(null);
        	res.setPublic(true);
            endpointDao.create(res);
            return Response.status(Status.ACCEPTED).entity(JsonMessageManager.jsonMessage(200, "Public endpoint added.", 2101)).build();
        
        } catch (SQLException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5002);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }

    /**
     * Register new public endpoint
     *
     * @param res Json containing the endpoint's url.
     * @return Json message.
     */
    @POST
    @Path("/providers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProvider(@Context HttpHeaders headers,
                                   Provider prov) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New POST /admin/provider call.");
        
        Dao<Provider, UUID> providerDao = DataSourceManager.getDao(Provider.class);

        try {
        	
        	//Check auth header
            if (!auth(headers)) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4002);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }

            providerDao.create(prov);
            return Response.status(Status.ACCEPTED).entity(JsonMessageManager.jsonMessage(200, "Provider added.", 2102)).build();
        
        } catch (SQLException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "Internal server error", 5002);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    
    /**
     * Run the default test on all public resources
     *
     * @return Empty response with status code
     */
    @GET
    @Path("/testallpublic")
    public Response generalTest(@Context HttpHeaders headers, @QueryParam("version") @DefaultValue("") String version) {

        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        LOGGER.debug("New GET /admin/testallpublic call.");
        try {

            //Check auth header
            if (!auth(headers)) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4002);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }
            if (!version.equals("v1.0") && !version.equals("v1.1")) {
                String jsonError = JsonMessageManager.jsonMessage(400, "Missing or invalid version parameter", 4202);
                return Response.status(Status.BAD_REQUEST).encoding(jsonError).build();
            }
            
            ObjectMapper mapper = new ObjectMapper();

            boolean allowAdditional = false; // Strict mode by default -- No additional fields allowed. 
            
            InputStream inJson = TestCollection.class.getResourceAsStream("/collections/CompleteBrapiTest." + version +".json");
            TestCollection tc = mapper.readValue(inJson, TestCollection.class);
            List<Resource> publicResources = ResourceService.getAllPublicEndpoints();
            publicResources.forEach(resource -> {
            	try {
					RunnerService.TestEndpointWithCallAndSaveReport(resource, tc, allowAdditional);
				} catch (SQLException | JsonProcessingException e) {
					e.printStackTrace();
				} 
            });
            System.out.println("Done");
            return Response.ok().build();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5003);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    
    /**
     * Update database information using public json document.
     *
     * @return Empty response with status code
     */
    @POST
    @Path("/updateproviders")
    public Response updateProviders(@Context HttpHeaders headers) {
        
        if (Config.get("advancedMode") == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        LOGGER.debug("New GET /admin/testallpublic call.");
        
        int resourcesUpdated = 0;
        
        try {

            //Check auth header
            if (!auth(headers)) {
                String e = JsonMessageManager.jsonMessage(401, "unauthorized", 4002);
                return Response.status(Status.UNAUTHORIZED).entity(e).build();
            }
            
            ObjectMapper mapper = new ObjectMapper();
            // Required because some resource properties are arrays and some objects.
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            
            // Download Json
            URL url = new URL(Config.get("resourceJsonUrl"));
            
            URLConnection connection;
            if (!Config.get("http.proxyHost").equals("")) {
            	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Config.get("http.proxyHost"), Integer.parseInt(Config.get("http.proxyPort"))));
            	connection = url.openConnection(proxy);
            } else {
            	connection = url.openConnection();
            }
            
            connection.connect();
            InputStream is = connection.getInputStream();
            JsonNode node = mapper.readTree(is).at("/brapi-providers/category");
            
            Dao<Provider, UUID> providerDao = DataSourceManager.getDao(Provider.class);
            Dao<Resource, UUID>  resourceDao = DataSourceManager.getDao(Resource.class);
            
            // Iterate through providers.
            for (int i = 0; i < node.size(); i++) {
            	Provider providerJson = mapper.treeToValue(node.get(i), Provider.class);
            	// Check if provider existed (by name)
            	Provider providerDb = providerDao.queryBuilder().where()            	
            		.eq(Provider.NAME_FIELD_NAME, providerJson.getName())
            		.queryForFirst();
            	
            	if (providerDb == null) {
            		//Not found in DB, generate
            		providerDao.create(providerJson);
            		providerDao.refresh(providerJson);
            		Iterator<Resource> it = providerJson.getResources().iterator();
            		while(it.hasNext()) {
            			Resource res = it.next();
            			res.setProvider(providerJson);
            			res.setPublic(true);
            			resourceDao.create(res);
            			resourcesUpdated++;
            		}
            		
            	} else {
            		// Found in DB, update.
            		providerDb.setDescription(providerJson.getDescription());
            		providerDb.setLogo(providerJson.getLogo());
            		Iterator<Resource> it = providerJson.getResources().iterator();
            		while(it.hasNext()) {
            			Resource res = it.next();
            			// Check resources in DB
            			Resource resDb = resourceDao.queryBuilder().where()
            					.eq(Resource.URL_FIELD_NAME, res.getUrl()).and()
            					.eq(Resource.PROVIDER_FIELD_NAME, providerDb.getId())
            					.queryForFirst();
            			
            			if (resDb == null) {
            				// Not found, create
            				res.setProvider(providerDb);
            				res.setPublic(true);
            				resourceDao.create(res);
            				resourcesUpdated++;
            			} else {
            				// Found, generate
            				resDb.setCertificate(res.getCertificate());
            				resDb.setName(res.getName());
            				resDb.setLogo(res.getLogo());
            				resDb.setPublic(true);
            				resourceDao.update(resDb);
            				resourcesUpdated++;
            			}
            		}
            		providerDao.update(providerDb);
            	}
            }

            String response = JsonMessageManager.jsonMessage(200, "" + resourcesUpdated + " resources updated.", 2000);
            return Response.ok().entity(response).build();
        } catch (IOException | SQLException e) {
        	e.printStackTrace();
            String e1 = JsonMessageManager.jsonMessage(500, "internal server error", 5003);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e1).build();
        }
    }
    
    
    private boolean auth(HttpHeaders headers) {
    	String[] auth = ApiResourceService.getAuth(headers);
        //Check auth header
        if (auth == null || auth.length != 2) {
            return false;
        }

        //Check if api key is correct.
        if (!auth[1].equals(Config.get("adminkey"))) {
            return false;
        }
    	
    	return true;
    }
}
