package forth.ics.isl.controller;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import forth.ics.isl.service.DBService;

/**
 * The back-end controller for the Database Service
 *
 * @author Vangelis Kritsotakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class ConfigurationController {

	@Value("${service.url}")
    private String serviceUrl;
    @Value("${triplestore.namespace}")
    private String namespace;
    @Value("${uri.prefix}")
    private String uriPrefix;
    @Value("${service.max.result.count}")
    private String maxResultCountLimit;
    @Value("${portal.state}")
    private String portalState;

    /**
     * This service retrieves configuration options (service.url, triplestore.namespace and uri.prefix) from the property file
     * and returns them 
     */
    @RequestMapping(value = "/retrieve_service_model", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject retrieveServiceModelOptions(@RequestHeader(value = "Authorization") String authorizationToken) throws IOException {
        System.out.println("Works");
        JSONObject serviceModelJsonObject = new JSONObject();
    	serviceModelJsonObject.put("url", serviceUrl);
    	serviceModelJsonObject.put("namespace", namespace);
    	serviceModelJsonObject.put("uriPrefix", uriPrefix);
    	serviceModelJsonObject.put("maxResultCountLimit", maxResultCountLimit);
        return serviceModelJsonObject;
    }
    
    /**
     * This service retrieves the configuration option "portal.state" from the property file
     * and returns it's value (the value can either be "public" or "private"). If the value 
     * is "public" then the user role editing will be disabled.
     */
    @RequestMapping(value = "/retrieve_portal_state", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject retrievePortalStateOption() throws IOException {
        System.out.println("Works");
        JSONObject portaStateJsonObject = new JSONObject();
        portaStateJsonObject.put("portalState", portalState);
        return portaStateJsonObject;
    }
}
