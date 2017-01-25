package org.recap.security;

import org.jasig.cas.client.util.CommonUtils;

import java.net.URL;

/**
 * Created by sheiks on 18/01/17.
 */
public abstract class ReCAPAbstractCasProtocolUrlBasedTicketValidator  extends ReCAPAbstractUrlBasedTicketValidator {

    protected ReCAPAbstractCasProtocolUrlBasedTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    protected final void setDisableXmlSchemaValidation(final boolean disable) {
        // nothing to do
    }

    /**
     * Retrieves the response from the server by opening a connection and merely reading the response.
     */
    protected final String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        return CommonUtils.getResponseFromServer(validationUrl, getURLConnectionFactory(), getEncoding());
    }
}
