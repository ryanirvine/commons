/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.charon.core.protocol.endpoints;

import org.wso2.charon.core.encoder.Decoder;
import org.wso2.charon.core.encoder.Encoder;
import org.wso2.charon.core.encoder.json.JSONDecoder;
import org.wso2.charon.core.encoder.json.JSONEncoder;
import org.wso2.charon.core.exceptions.AbstractCharonException;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.exceptions.FormatNotSupportedException;
import org.wso2.charon.core.protocol.ResponseCodeConstants;
import org.wso2.charon.core.protocol.SCIMResponse;
import org.wso2.charon.core.schema.SCIMConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an abstract layer for all the resource endpoint to abstract out common
 * operations to all resource endpoints.
 */
public abstract class AbstractResourceEndpoint implements ResourceEndpoint {

    //Keeps a map of supported encoders of SCIM server side.
    private static Map<String, Encoder> encoderMap = new ConcurrentHashMap<String, Encoder>();

    //Keeps a map of supported encoders of SCIM server side.
    private static Map<String, Decoder> decoderMap = new ConcurrentHashMap<String, Decoder>();

    //Keeps  a map of endpoint urls of the exposed resources.
    private static Map<String, String> endpointURLMap;

    /**
     * Returns the encoder given the encoding format.
     *
     * @param format
     * @return
     * @throws FormatNotSupportedException
     */
    public Encoder getEncoder(String format)
            throws FormatNotSupportedException, CharonException {
        //if the encoder map is empty, register default json encoder
        if (encoderMap.isEmpty()) {
            encoderMap.put(SCIMConstants.JSON, new JSONEncoder());
        }
        //if the requested format not supported, return an error.
        if (!encoderMap.containsKey(format)) {
            //Error is logged by the caller.
            throw new FormatNotSupportedException(ResponseCodeConstants.CODE_FORMAT_NOT_SUPPORTED,
                                                  ResponseCodeConstants.DESC_FORMAT_NOT_SUPPORTED);
        }
        return encoderMap.get(format);
    }

    public Decoder getDecoder(String format)
            throws FormatNotSupportedException, CharonException {

        //if the decoder map is empty, register default json encoder
        if (decoderMap.isEmpty()) {
            decoderMap.put(SCIMConstants.JSON, new JSONDecoder());
        }
        //if the requested format not supported, return an error.
        if (!decoderMap.containsKey(format)) {
            //Error is logged by the caller.
            throw new FormatNotSupportedException(ResponseCodeConstants.CODE_FORMAT_NOT_SUPPORTED,
                                                  ResponseCodeConstants.DESC_FORMAT_NOT_SUPPORTED);
        }
        return decoderMap.get(format);

    }

    /**
     * Register encoders to be supported by SCIM Server Side, which will be used in Charon-API.
     *
     * @param format  - format that the registering encoder supports.
     * @param encoder
     */
    public static void registerEncoder(String format, Encoder encoder) throws CharonException {
        if (encoderMap.containsKey(format)) {
            //log the error and throw.
            String error = "Encoder for the given format is already registered.";
            throw new CharonException(error);
        } else {
            encoderMap.put(format, encoder);
        }
    }

    /**
     * Register decoders to be supported by SCIM Server Side, which will be used in Charon-API.
     *
     * @param format
     * @param decoder
     * @throws CharonException
     */
    public static void registerDecoder(String format, Decoder decoder) throws CharonException {
        if (decoderMap.containsKey(format)) {
            //log the error and throw.
            String error = "Decoder for the given format is already registered.";
            throw new CharonException(error);
        } else {
            decoderMap.put(format, decoder);
        }
    }

    /**
     * Endpoint URLs defined in configuration needs to be registered here for the API to use them
     * in Location header etc.
     *
     * @param endpointURLs
     */
    public static void registerResourceEndpointURLs(Map<String, String> endpointURLs) {
        endpointURLMap = endpointURLs;
    }

    public static String getResourceEndpointURL(String resource) {
        if (endpointURLMap != null && endpointURLMap.size() != 0) {
            return endpointURLMap.get(resource);
        } else {
            return null;
        }
    }

    /**
     * Util method to encode the exception and construct the SCIMResponse, given the encoder
     * and the exception
     *
     * @param encoder
     * @param exception
     * @return
     */
    public static SCIMResponse encodeSCIMException(Encoder encoder,
                                                   AbstractCharonException exception) {
        Map<String, String> httpHeaders = new HashMap<String, String>();
        httpHeaders.put(SCIMConstants.CONTENT_TYPE_HEADER, SCIMConstants.identifyContentType(encoder.getFormat()));
        return new SCIMResponse(exception.getCode(), encoder.encodeSCIMException(exception), httpHeaders);
    }
    /**
     * Build SCIM Response given the response code and response message.
     * @param responseCode
     * @param responseMessage
     * @return
     */
    /*protected SCIMResponse buildResponse(String responseCode, String responseMessage) {
        return new SCIMResponse(responseCode, responseMessage);
    }*/

    /**
     * Obtain the AttributeFactory to be used in current deployment. If a custom implementation is
     * not registered, use the DefaultAttributeFactory.
     *
     * @return
     */
    /*public static AttributeFactory getAttributeFactory() {
        if (attributeFactory != null) {
            return attributeFactory;
        } else {
            attributeFactory = new DefaultAttributeFactory();
            return attributeFactory;
        }
    }*/
}
