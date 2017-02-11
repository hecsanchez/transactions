/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *******************************************************************************/

package org.openengine.wink.example.payflow;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import paypal.payflow.ClientInfo;
import paypal.payflow.InquiryTransaction;
import paypal.payflow.PayflowConnectionData;
import paypal.payflow.PayflowUtility;
import paypal.payflow.SDKProperties;
import paypal.payflow.TransactionResponse;
import paypal.payflow.UserInfo;

/**
 * Simple example - Hello World!
 * <p/>
 * The SDK dispatches HTTP requests for URI
 * <code>http://[host]:[port]/HelloWorld/rest/world</code>, where
 * <code>HelloWorld</code> is the context root, to this class. A simple Atom
 * entry is returned in HTTP response.
 * <p/>
 * The service document is available at URI
 * <code>http://[host]:[port]/HelloWorld/rest</code> but it is empty because
 * this simple demo doesn't contain any collection of resources.
 * <p/>
 * This resource must be registered within a JAX-RS application, this example
 * uses the default usage of application /WEB-INF/application
 *
 *
// This class uses the Payflow SDK Data Objects to do an Inquiry transaction.
//
// You perform an inquiry using a reference to an original transaction either the PNREF
// value returned for the original transaction or the CUSTREF value that you specified for the original
// transaction.
//
// While the amount of information returned in an Inquiry transaction depends upon the VERBOSITY setting,
// Inquiry responses mimic the verbosity level of the original transaction as much as possible.
//
// TransactionResource results (especially values for declines and error conditions) returned by each PayPal-supported
// processor vary in detail level and in format. The Payflow Pro Verbosity parameter enables you to control
// the kind and level of information you want returned.  By default, Verbosity is set to LOW.
// A LOW setting causes PayPal to normalize the transaction result values. Normalizing the values limits
// them to a standardized set of values and simplifies the process of integrating Payflow Pro.
// By setting Verbosity to MEDIUM, you can view the processor?s raw response values. This setting is more
// "verbose" than the LOW setting in that it returns more detailed, processor-specific information.
// The request is sent as a Data Object and the response received is also a Data Object.
 */
@Path("/transactions")
public class TransactionResource {

    /**
     * This method is called by the SDK for HTTP GET method requests where the
     * Accept header allows the Atom media type application/atom+xml. A
     * SyndEntry is created with basic information. Serialization of the
     * SyndEntry to Atom entry is performed by the SDK automatically. The
     * default status code of 200 (OK) is returned in the response.
     *
     * @return SyndEntry of the requested resource
     */
    @GET
    @Path("{pnref}")
    @Produces(MediaType.APPLICATION_JSON)
    public String doInquiry(@PathParam("pnref") String pnref,
    							@HeaderParam("user") String userName,
    							@HeaderParam("vendor") String vendorName,
    							@HeaderParam("partner") String partnerName,
    							@HeaderParam("pwd") String password) {
        try {
        	if(userName!=null && password!=null)
        		return getTxnStatus(pnref, userName, vendorName, partnerName, password).toString();
        	else
        		throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		} catch (JSONException e) {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
    }

    private JSONObject getTxnStatus(String pnref, String userName, String vendorName, String partnerName, String password) throws JSONException {
        System.out.println("------------------------------------------------------");
        System.out.println("Executing Sample from File: DOInquiry.java");
        System.out.println("------------------------------------------------------");


        // Payflow Pro Host Name. This is the host name for the PayPal Payment Gateway.
        // For testing: 	pilot-payflowpro.paypal.com
        // For production:  payflowpro.paypal.com
        // DO NOT use payflow.verisign.com or test-payflow.verisign.com!
        SDKProperties.setHostAddress("pilot-payflowpro.paypal.com");
        SDKProperties.setHostPort(443);
        SDKProperties.setTimeOut(45);

        // Logging is by default off. To turn logging on uncomment the following lines:
        //SDKProperties.setLogFileName("payflow_java.log");
        //SDKProperties.setLoggingLevel(PayflowConstants.SEVERITY_DEBUG);
        //SDKProperties.setMaxLogFileSize(100000);

        // Uncomment the lines below and set the proxy address and port, if a proxy has to be used.
        //SDKProperties.setProxyAddress("");
        //SDKProperties.setProxyPort(0);

        //USER=winktest&VENDOR=winktest&PARTNER=PayPal&PWD=wink123
        // Create the Data Objects.
        // Create the User data object with the required user details.
        UserInfo user = new UserInfo(userName, vendorName, partnerName, password);

        // Create the Payflow Connection data object with the required connection details.
        PayflowConnectionData connection = new PayflowConnectionData();

        // Create a new Inquiry TransactionResource.
        // Replace <PNREF> with a previous transaction ID that you processed on your account. V19A2A192BE9
        InquiryTransaction trans = new InquiryTransaction(pnref, user, connection, PayflowUtility.getRequestId());

        // To use CUSTREF instead of PNREF you need to set the CustRef and include the INVOICE object in your
        // request.  Since you will be using CUSTREF instead of PNREF, PNREF will be "" (null).
        // Create a new Invoice data object with the Amount, Billing Address etc. details.
        //Invoice inv = new Invoice();
        //inv.setCustRef("CUSTREF1");
        //InquiryTransaction trans = new InquiryTransaction("", user, connection, inv, PayflowUtility.getRequestId());

        // Refer to the Payflow Pro Developer's Guide for more information regarding the parameters returned
        // when VERBOSITY is set.
        trans.setVerbosity("LOW"); // Change VERBOSITY to MEDIUM to display additional information.
        JSONObject jsonObject = new JSONObject();
        // Submit the TransactionResource
        paypal.payflow.Response resp = trans.submitTransaction();

        // Display the transaction response parameters.
        if (resp != null) {
            // Get the TransactionResource Response parameters.
            TransactionResponse trxnResponse = resp.getTransactionResponse();

            // Create a new Client Information data object.
            ClientInfo clInfo = new ClientInfo();

            // Set the ClientInfo object of the transaction object.
            trans.setClientInfo(clInfo);

            if (trxnResponse != null) {

            	jsonObject = jsonObject
                				.put("RESULT",trxnResponse.getResult())
                				.put("PNREF",trxnResponse.getPnref())
                				.put("RESPMSG",trxnResponse.getRespMsg())
                				.put("AUTHCODE",trxnResponse.getAuthCode())
                				.put("CVV2MATCH",trxnResponse.getCvv2Match())
                				.put("AVSADDR",trxnResponse.getAvsAddr())
                				.put("AVSZIP",trxnResponse.getAvsZip())
                				.put("IAVS",trxnResponse.getIavs())
                				.put("CARDSECURE",trxnResponse.getProcCardSecure());
            }


        }
        return jsonObject;
    }


}
